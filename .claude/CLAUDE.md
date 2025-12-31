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
