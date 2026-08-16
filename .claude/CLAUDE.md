# Claude Code Instructions for Hurricane Project

## Commit Message Preferences

**DO NOT include** the following in commit messages:
- "🤖 Generated with [Claude Code]" footer
- "Co-Authored-By: Claude Sonnet" footer
- Any AI-generation attribution

Keep commit messages clean and simple.

## File Editing Guidelines

### CRITICAL: Use Relative Paths for File Operations

**ALWAYS use relative paths** when using Read, Edit, and Write tools. DO NOT use absolute paths like `C:/Users/...`.

**Examples:**
- ✅ CORRECT: `src/haven/Discord.java`
- ❌ WRONG: `C:/Users/Mamel/Desktop/Hurricane/src/haven/Discord.java`

This is essential for file editing tools to work properly.

### CRITICAL: Java File Whitespace Handling

**This codebase uses TABS for indentation** (following the original Haven & Hearth coding style).

**General Approach:**
1. **Trust that this codebase uses TABS** - Don't ask for permission to verify with `sed` every time
2. **Use the Read tool** to read the file and see the indentation pattern
3. **Match the indentation visually** - if you see indentation in Read output, use TABS in your Edit
4. **Only verify with `sed | cat -A`** if you're genuinely uncertain and the edit is critical

**Why This Matters:**

The Read tool output doesn't visually distinguish tabs from spaces, which can cause Edit operations to fail when the old_string doesn't match exactly. However, you can TRUST that all indentation in `.java` files uses TABS, not spaces.

### Recommended Workflow

```bash
# 1. Read the file to see the code
Read file at lines X-Y

# 2. Match the indentation pattern you see
# If lines are indented, use TABS (not spaces) in your old_string

# 3. Only if genuinely uncertain, verify with:
sed -n 'X,Yp' file.java | cat -A
# Where ^I = tab, spaces = spaces, $ = end of line
```

**Key Rule:** When you see indentation in Java files, assume TABS unless you have a specific reason to doubt it.

## Project Overview

Hurricane is a custom client for Haven & Hearth, built on top of the "Vanilla" Client. This is a fork of a fork of the original Haven & Hearth client. The developers allow modifications.

- **Steam AppID:** 3051280
- **Can be played:** Standalone or through Steam
- **Steam Workshop:** https://steamcommunity.com/sharedfiles/filedetails/?id=3423755273
- **Forum Thread:** https://www.havenandhearth.com/forum/viewtopic.php?t=76544

## Build & Run

### Building
- **Build tool:** Apache Ant (via `Build.bat`)
- **Default build:** `./Build.bat` (uses bundled ant at `apache-ant-1.10.15/bin/ant.bat`)
- **DO NOT** call `ant` directly from git bash - it won't be found. Always use `./Build.bat`
- **Build.bat features:** Sets up JAVA_HOME and PATH automatically for bundled JDK 21
- **Main output:** `bin/hafen.jar`
- **Ant targets available:** `deftgt` (default), `jar`, `bin`, `jars`, `clean`, `run`

### Running
- **Windows:** Run `Play.bat` in the project root
- **Linux:** Run `Play_Linux.sh`
- **JVM:** Uses bundled `jdk-21.0.6+7/bin/java.exe`
- **Memory:** -Xms1024m -Xmx4096m
- **Steam Integration:** Controlled by `-DrunningThroughSteam` flag and `steam_appid.txt` file

## Project Structure

- `src/` - Java source files (packages: haven/, dolda/, com/, org/, plus root-level files)
- `res/` - Resources (gfx/, sfx/, paginae/, customclient/)
- `bin/` - Compiled output (hafen.jar and dependencies) - **gitignored, DELETED on rebuild**
- `build/` - Build artifacts - gitignored
- `lib/` - External libraries
- `.claude/` - Claude Code configuration - gitignored
- `*.db` - SQLite databases (static_data.db, saved_routes.db, hitboxes.db) - stored in project root, gitignored

### Database Locations

**IMPORTANT:** All SQLite databases use paths like `jdbc:sqlite:../database.db` to store files in the **project root**, not in `bin/`.

**Why:** The `bin/` directory is deleted during every build (`ant clean` target). Databases must be in the project root to persist across rebuilds.

**Current databases:**
- `static_data.db` - Flower menu auto-select settings
- `saved_routes.db` - Checkpoint/route data
- `hitboxes.db` - Collision box data

All database paths use `../` prefix because the working directory when running is `bin/`, so `../` goes up to the project root.

## Development Workflow

**When to rebuild and restart the client:**

After completing a logical unit of work (e.g., implementing a feature, fixing a bug), rebuild and restart so the user can test:

```bash
# Kill any running Java processes (use // for git bash compatibility)
taskkill //F //IM java.exe 2>/dev/null || echo "No Java process running"

# Build the client using Build.bat (not ant directly)
./Build.bat

# Start the client in its own window (NOT as a background task)
cmd //c start Play.bat
```

**IMPORTANT:**
- Use `//` (double slashes) in taskkill and cmd commands when running from git bash, not `/`.
- Use `cmd //c start Play.bat` to launch in a separate window, NOT `run_in_background=true`
- Use `/dev/null` for null device in git bash on Windows, NOT `nul` (which creates a file named "nul")

**Complete rebuild and restart:**
```bash
taskkill //F //IM java.exe 2>/dev/null ; ./Build.bat && cmd //c start Play.bat
```

**Do this:**
- After implementing a complete feature (even if it touched multiple files)
- After fixing a bug
- When you want the user to test your changes

**Don't do this:**
- After every single file edit during multi-file changes
- For work-in-progress edits

### Merging Upstream Changes

**When the user says "fork updated" or similar phrases indicating upstream has new changes:**
- Immediately proceed with the merge workflow below
- DO NOT ask for confirmation - the user statement IS the confirmation
- Proceed directly to fetching, merging, cleaning, rebuilding, and pushing

When merging updates from upstream (Nightdawg/Hurricane), follow these steps to ensure the version number updates correctly:

```bash
# 1. Fetch latest changes from upstream
git fetch upstream

# 2. Merge upstream changes
git merge upstream/master

# 3. Clean build artifacts (preserves Discord libraries in lib/ext/discord)
./Build.bat clean

# 4. Rebuild the client with updated version
./Build.bat

# 5. Push to your fork
git push origin master
```

**Why the clean build is necessary:**
- The version number is defined in `src/haven/Config.java` (line 42: `clientVersion`)
- The compiled JAR in `bin/hafen.jar` caches the old version string
- Running `./Build.bat clean` deletes `bin/` and `build/` directories
- The Discord libraries in `lib/ext/discord/` are preserved (not deleted by clean)
- A fresh build compiles the new version number into the JAR

**Version checking:**
- The client checks `Nightdawg/Hurricane` GitHub releases for updates
- This is intentional - allows seeing when upstream has new versions
- Located in `LoginScreen.java:233` and `GitHubVersionFetcher.java`

## Technical Details

- **Operating System:** Windows
- **Claude Code Shell Environment:** Git Bash (MINGW64 `/usr/bin/bash`)
  - **IMPORTANT:** Claude Code executes Bash tool commands through Git Bash, NOT PowerShell or CMD
  - This is true regardless of what terminal you're running Claude Code from
  - Git Bash provides Unix-like commands (`ls`, `pwd`, `grep`) but runs on Windows
- **Java version:** 21 (compatible with Java 15-21)
- **Encoding:** UTF-8
- **Line endings:** Mix of LF and CRLF (Windows environment)
- **Dependencies:** JOGL, LWJGL, Steamworks SDK, Discord IPC

### Discord Rich Presence Libraries

**CRITICAL:** The Discord IPC libraries are stored in `lib/ext/discord/` and are **committed to git** to preserve the exact working versions.

**Working library versions (DO NOT CHANGE):**
- **DiscordIPC.jar** - Downloaded from https://jitpack.io/com/github/jagrosh/DiscordIPC/master-SNAPSHOT/DiscordIPC-master-SNAPSHOT.jar
  - Source: jagrosh/DiscordIPC (GitHub: https://github.com/jagrosh/DiscordIPC)
  - Version: master-SNAPSHOT (~28KB)
  - **CRITICAL:** Use the jagrosh version, NOT the CDAGaming fork (incompatible API)
- **json.jar** - org.json:json:20230227 (72KB)
- **slf4j-api.jar** - org.slf4j:slf4j-api:2.0.7 (63KB)
- **slf4j-simple.jar** - org.slf4j:slf4j-simple:2.0.7 (16KB)
- **junixsocket-common.jar** - com.kohlschutter.junixsocket:junixsocket-common:2.6.2 (429KB)
- **junixsocket-native-common.jar** - com.kohlschutter.junixsocket:junixsocket-native-common:2.6.2 (669KB)

**If libraries are missing or deleted:**

```bash
cd lib/ext/discord

# DiscordIPC (jagrosh version - accepts OffsetDateTime)
curl -L -o DiscordIPC.jar https://jitpack.io/com/github/jagrosh/DiscordIPC/master-SNAPSHOT/DiscordIPC-master-SNAPSHOT.jar

# Dependencies (exact versions from Maven)
curl -L -o json.jar https://repo1.maven.org/maven2/org/json/json/20230227/json-20230227.jar
curl -L -o slf4j-api.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.7/slf4j-api-2.0.7.jar
curl -L -o slf4j-simple.jar https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.7/slf4j-simple-2.0.7.jar
curl -L -o junixsocket-common.jar https://repo1.maven.org/maven2/com/kohlschutter/junixsocket/junixsocket-common/2.6.2/junixsocket-common-2.6.2.jar
curl -L -o junixsocket-native-common.jar https://repo1.maven.org/maven2/com/kohlschutter/junixsocket/junixsocket-native-common/2.6.2/junixsocket-native-common-2.6.2.jar
```

**Why these specific versions:**
- The jagrosh DiscordIPC library accepts `OffsetDateTime.now()` directly for timestamps
- The CDAGaming fork (io.github.CDAGaming:DiscordIPC:0.10.3) has an incompatible API (uses `long` timestamps and different method signatures)
- Discord.java was written for the jagrosh version and should NOT be modified for other versions

### Git Bash on Windows - Command Syntax

When Claude Code runs commands, they execute in Git Bash (MINGW64), which requires special syntax:

**Windows Commands:**
- Use `//` instead of `/` for Windows executables: `taskkill //F //IM`, `cmd //c`
- This is because Git Bash interprets `/` as a path, so `//` escapes to a single `/` for Windows

**Null Device:**
- Use `/dev/null` (Unix-style), NOT `nul` (CMD) or `$null` (PowerShell)
- Using `nul` in Git Bash creates a file named "nul" in the current directory

**Running Scripts:**
- Use `./script.bat` to run batch files
- Paths use forward slashes in Git Bash but batch files see Windows paths

## Haven Resource (.res) File Format

Haven resource files are binary files used for menu buttons, images, and other game resources.

### File Structure

```
"Haven Resource 1"  (16 bytes, NOT null-terminated)
uint16 version      (2 bytes, little-endian)

while not end of file:
    string layer_type  (null-terminated)
    int32 layer_length (4 bytes, little-endian)
    byte[] layer_data  (layer_length bytes)
```

### Action Layer Structure

For menu buttons, the `action` layer contains:
```
string parent_resource  (e.g., "customclient/menugrid/OtherScriptsAndTools")
uint16 parent_version
string name            (button tooltip text)
string prereq_skill    (usually empty "")
uint16 hotkey          (character code)
uint16 ad_length       (always 3 for custom buttons)
string ad[0]           (always "@" for custom buttons)
string ad[1]           (category, e.g., "OtherScriptsAndTools")
string ad[2]           (action identifier for MenuGrid.useCustom())
```

### Creating New Menu Buttons

**Template code to edit a .res file:**

See the working example in git history - search for "FinalResEdit.java" which properly:
1. Reads the 16-byte signature
2. Reads uint16 version
3. Parses each layer (string type + int32 length + data)
4. Modifies the action layer fields
5. Recalculates layer length
6. Writes output with correct binary format

**Key points:**
- Signature is EXACTLY 16 bytes (no null)
- Strings are null-terminated
- uint16 is 2 bytes, little-endian
- int32 is 4 bytes, little-endian
- Layer length must be recalculated when modifying content

### Adding a New Script Button

1. Create the script class in `src/haven/automated/YourScript.java`
2. Add thread field in `GameUI.java`: `public Thread yourScriptThread;`
3. Add handler in `MenuGrid.java` under `useCustom()` → `OtherScriptsAndTools` section
4. Create/edit .res file with proper action layer
5. Add `makeLocal("customclient/menugrid/OtherScriptsAndTools/YourScript")` in `MenuGrid.loadCustomActionButtons()`

## Common Script Patterns

### FlowerMenu (Context Menu) Selection

Use `FlowerMenu.setNextSelection()` to pre-select an option before opening a context menu:

```java
// Set the selection
FlowerMenu.setNextSelection("Empty");
// Trigger right-click (iact with button 3)
container.item.wdgmsg("iact", Coord.z, 3);
Thread.sleep(300);
// Clear the selection
FlowerMenu.setNextSelection(null);
```

**CRITICAL:** You must interact with the item BEFORE picking it up. Right-clicking an item in hand won't work - you get a "wdgmsg sender is not in rwidgets" error.

### Equipment Slots

Access equipment pouches and slots:

```java
Equipory equipory = gui.getequipory();
WItem leftPouch = equipory.slots[19];   // Left equipment pouch
WItem rightPouch = equipory.slots[20];  // Right equipment pouch

// Drop back to equipment slot
equipory.wdgmsg("drop", slotNumber);
```

**Resource name differences:**
- Equipment items: `"gfx/invobjs/small/waterskin"`
- Inventory items: `"gfx/invobjs/waterskin"`

### Inventory Coordinate Conversion

Inventory uses two coordinate systems:

```java
// Convert calculated position (pixel) to index position (grid slot)
Coord sqsz = Inventory.sqsz;
Coord indexPos = calculatedPos.sub(1, 1).div(sqsz);

// Convert index position back to calculated position
Coord calculatedPos = indexPos.mul(sqsz).add(1, 1);

// Drop to inventory position (uses index coordinates)
gui.maininv.wdgmsg("drop", indexPos);
```

### Widget Reference Invalidation

**CRITICAL:** After dropping an item, the `WItem` reference becomes invalid. You must re-find the item:

```java
// Take item
container.item.wdgmsg("take", Coord.z);
Thread.sleep(100);

// Do something with item in hand
gui.map.wdgmsg("itemact", Coord.z, position, 0);
Thread.sleep(400);

// Drop it back
gui.maininv.wdgmsg("drop", containerPos);

// The old 'container' reference is now INVALID
// Must re-find the item:
WItem newContainer = findWaterContainerAt(containerPos);
```

### Retry Loops for Timing Issues

Always use retry loops when waiting for items or data to become available:

```java
// Wait for item to appear in slot after dropping
WItem item = null;
for (int attempt = 0; attempt < 10; attempt++) {
    Thread.sleep(50);
    item = equipory.slots[slotNumber];
    if (item != null) {
        break;
    }
}

// Wait for item info to be ready
Double quality = null;
for (int attempt = 0; attempt < 10; attempt++) {
    quality = extractQuality(item);
    if (quality != null) {
        break;
    }
    Thread.sleep(100);
}
```

### Accessing Container Contents and Quality

```java
// Get container contents
private ItemInfo.Contents.Content getContent(GItem item) {
    for (ItemInfo info : item.info()) {
        if (info instanceof ItemInfo.Contents) {
            return ((ItemInfo.Contents) info).content;
        }
    }
    return null;
}

// Extract quality from water in container
private Double extractWaterQuality(WItem container) {
    List<ItemInfo> infos = container.item.info();
    for (ItemInfo info : infos) {
        if (info instanceof ItemInfo.Contents) {
            ItemInfo.Contents contents = (ItemInfo.Contents) info;
            if (contents.sub != null) {
                for (ItemInfo subInfo : contents.sub) {
                    if (subInfo instanceof QBuff) {
                        return ((QBuff) subInfo).q;
                    }
                }
            }
        }
    }
    return null;
}
```

### Displaying Messages

```java
// In-game message (appears in message log)
gui.ui.msg("Water Refilled!");

// Error message (red text)
gui.ui.error("Must be on water!");
```

### Common Script Structure

```java
public class YourScript implements Runnable {
    private GameUI gui;

    public YourScript(GameUI gui) {
        this.gui = gui;
    }

    @Override
    public void run() {
        try {
            // Your script logic here

        } catch (InterruptedException e) {
            // User interrupted the script
        } catch (Exception e) {
            gui.ui.error("Error: " + e.getMessage());
        }
    }
}
```

## Map Icons & Markers System

### Adding a Gob to the Map Icons list (GobIconsCustom)

`src/haven/GobIconsCustom.java` maps a **gob resource name → a custom map icon** resource. Add one line:

```java
put("gfx/terobjs/map/squirrelcache", "customclient/mapicons/squirrelcache");
```

This does two things automatically:
1. `getIcon(Gob)` gives the gob a minimap icon.
2. `addCustomSettings()` adds it to the **Map Icons Settings** window (shown by default, toggleable).

**CRITICAL caveat:** `Gob.updateCustomIcons()` (Gob.java:1447) only applies the custom icon **if the gob has no native GobIcon** (`getattr(GobIcon.class) == null`). Objects the server already tags with an icon (e.g. cave passages, see below) will **ignore** a GobIconsCustom mapping — you'd have to override the native icon instead.

### Cave passages are GOBS, not tiles

`gfx/tiles/ridges/cavein`, `cavein2`, `caveout` look like tiles by name but are **gobs** with a native GobIcon named "Cave Passage" (confirmed via the extended tooltip: `GAttribs: haven.Drawable, haven.GobIcon`). To detect them, **iterate the gob cache**, never scan `MCache` tiles:

```java
synchronized (ui.sess.glob.oc) {
    for (Gob gob : ui.sess.glob.oc) {
        Resource r = gob.getres();          // may be null / throw Loading
        if (r != null && r.name.equals("gfx/tiles/ridges/cavein")) { ... }
    }
}
```

### Placing a permanent map marker programmatically (SMarker)

Markers live in `ui.gui.mapfile.file` (a `MapFile`). `SMarker` = resource-icon marker; `PMarker` = colored flag. Pattern (mirrors `MiniMap.markobjs()` / `MapWnd.markobj()`), converting a **tile coord** to a **segment coord**:

```java
Coord tc = gob.rc.floor(tilesz);                     // world -> tile coord
MCache.Grid obg = ui.sess.glob.map.getgrid(tc.div(cmaps));
if (!file.lock.writeLock().tryLock()) return;
try {
    MapFile.GridInfo info = file.gridinfo.get(obg.id);
    if (info == null) return;
    Coord sc = tc.add(info.sc.sub(obg.gc).mul(cmaps));   // segment coord
    // proximity dedup: skip if a same-kind marker already exists nearby
    for (MapFile.Marker mk : file.markers) {
        if (mk instanceof MapFile.SMarker sm && sm.seg == info.seg
            && sm.res.name.equals(iconName) && sm.tc.dist(sc) < MERGE_DIST) return;
    }
    file.add(new MapFile.SMarker(file, info.seg, sc, "Cave In", UID.nil,
        new Resource.Saved(Resource.remote(), iconName, -1), new byte[0]));
} finally { file.lock.writeLock().unlock(); }
```

- Use `UID.nil` for the oid; dedup by position via `file.smarker(name, seg, sc)` or a proximity loop (cave ridges span several gobs → without dedup you get marker spam).
- `SMarker` is always reconstructed on reload with `Resource.remote()`. That's fine for **local** `customclient/mapicons/...` icons because **`Resource.remote()`'s parent pool is `Resource.local()`** (Resource.java:888), so local resources resolve through it and markers survive a restart.

### Native gob auto-marking (like Burrows)

`MiniMap.markobjs()` (called each map-window tick) auto-marks gobs whose icon is **markable + default**. Markability comes from the icon resource's `mm/mark` info: `2` = DEFAULT (auto-marked), `1` = NONDEFAULT (markable, off by default → user ticks **"Place permanent marker"** in Map Icons Settings), else UNMARKABLE. This is why Burrows auto-mark but many icons don't.

### Creating a custom map icon (.res format)

Map icons are Haven `.res` files under `res/customclient/mapicons/`. Minimal icon layout:

```
"Haven Resource 1"        (16 bytes, no null)
uint16 version = 1
layer "image"   : 11-byte header  00 00 00 00 00 ff ff 00 00 00 00  (z=0,subz=0,fl=0,id=-1,off=0,0) + PNG bytes (128x128 works)
layer "tooltip" : the display name as raw bytes (length-delimited, NOT null-terminated), e.g. "Squirrel Cache"
```

Icons render as `res.flayer(Resource.imgc).img` (scaled down on the minimap). To make an icon **from a screenshot** (e.g. how the object looks in-game): crop it, remove the dark background to transparency (luminance threshold + connected-component denoise to drop scattered specks), fit into 128x128, then pack the PNG into the `.res` with the header above. Full working Python for both the image processing and the `.res` packing was used in git history — grep commits for "squirrelcache".

**Python + Pillow** are available for this at `C:/Users/Mamel/AppData/Local/Programs/Python/Python312/python.exe` (the `python3`/`python` on PATH is a Windows Store stub — don't use it). Install libs with `<that python> -m pip install Pillow`. The **Read tool can display PNG/JPG**, so extract icon images and Read them to preview; use `cmd //c start file.png` to show the user (images from Read are only visible to you, not the user).

## Keybindings

- Define with `KeyBinding.get("id", KeyMatch.forchar('P', 0))` (0 = no modifier). Handle in a widget's `globtype(GlobKeyEvent ev)`: `if (kb.key().match(ev)) { ...; return true; }`. `globtype` fires for hover-style hotkeys (not consumed by focused textboxes).
- **A KeyBinding does NOT appear in the settings UI automatically.** Add it to `OptWnd`'s `BindingPanel` with `addbtn(cont, "Label", MyClass.kb_x, y)` or `addbtnImproved(cont, "Label", "tooltip", color, kb, y)` under the "Other Custom features" section. The panel is reached via **Options → Advanced Settings → Interface Settings**.

## Misc client APIs learned

- **Gob under cursor**: `new MapView.Hittest(mapview.currentCursorLocation) { protected void hit(Coord pc, Coord2d mc, ClickData inf) { ... } }.run();` — `inf.clickargs()[1]` is the gob id (`Integer`), resolve with `glob.oc.getgob(Long.valueOf(...))`. `currentCursorLocation` is set in `MapView.mousemove` (local widget coords).
- **Open a URL in a browser**: `ui.wnd.toolkit().browse(new java.net.URI(url))` (throws `IOException`/`URISyntaxException`). The old `WebBrowser` class was **removed** in the toolkit refactor — don't use it.
- **Messages**: `ui.msg(String)` (single-arg lives on `UI`), `ui.gui.msg(String, Color)`, `ui.gui.error(String)`. There is no `GameUI.msg(String)`.
- **Mixed indentation**: `MapView.java`, `MiniMap.java`, `OptWnd.java` use loftar's style — **4 spaces for member declarations, TAB for the first statement level, then TAB + 4 spaces per nested level**. Other files (e.g. automation scripts, WItem's search block) use pure TABs. Always `sed -n 'X,Yp' file | cat -A` to confirm before editing these three.

## Resource loading paths

- The running client uses `Client.gameDir` (`""` when standalone) so `Resource.local()` reads `res/` relative to the working dir = **`bin/res/`**. `customclient/` icons are loose files there (not in a jar).
- The `bin` ant target **copies the whole project `res/` → `bin/res/`** (build.xml ~line 212), so a new file under `res/customclient/mapicons/` is bundled on the next `./Build.bat` (full build, not `jar`).
- `builtin-res.jar` / `hafen-res.jar` are **downloaded** from `${ext-lib-base}` = `http://www.havenandhearth.com/java` (the original H&H server — upstream's upstream), gated by `has-res-jar` (true if `bin/builtin-res.jar` exists).

## Upstream merge – hard-won gotchas

- **Always clean-build after a merge** (`./Build.bat clean` then `./Build.bat`). A plain incremental build keeps a **stale `bin/builtin-res.jar`**; new upstream code that references a newly-added resource (e.g. `gfx/hud/buffs/cframe-m`, loaded by `Buff.<clinit>` → `Fightsess.<clinit>`) then throws `NoSuchResourceException` → `ExceptionInInitializerError` → **black screen after login**. Clean build re-downloads the current res jars.
- **Resolving with `-X theirs` drops the Discord jars from `build.xml`'s manifest Class-Path** (upstream has no Discord). The surviving custom Discord code then throws `NoClassDefFoundError: com/jagrosh/discordipc/...` at runtime (client launches with `-jar`, so only the manifest Class-Path counts). Re-add `DiscordIPC.jar json.jar slf4j-api.jar slf4j-simple.jar junixsocket-common.jar junixsocket-native-common.jar` after `steamworks4j.jar`.
- After `-X theirs`, custom code in **non-conflicting** files may reference symbols that were dropped from **conflicting** hunks (e.g. `OptWnd.cursorSizeSlider`, `MainFrame.runningThroughDiscord`, old `InventorySorter` constructor). Build, then fix each dangling reference.
- **Diagnosing runtime errors**: `Play.bat` uses `javaw` (no console) so stderr is lost. Relaunch `bin/hafen.jar` with `java.exe` (console) redirecting `> log 2>&1` to capture exceptions.
- **Ask before killing a running client** during a live play session (do not `taskkill` java without confirming).
