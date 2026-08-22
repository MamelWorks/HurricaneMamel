# MamelWorks Fork — Differences from Nightdawg's Hurricane

This is a personal fork of [Nightdawg/Hurricane](https://github.com/Nightdawg/Hurricane) that
tracks upstream but adds a number of custom features (several ported/adapted from the archived
[Cediner/ArdClient](https://github.com/Cediner/ArdClient)). Everything below is on top of stock
Hurricane.

## Search & filtering (ItemFilter tag queries)
Both **Action Search** (Ctrl+F) and **Inventory Search** (Ctrl+Shift+F) accept ArdClient-style
tag queries in addition to plain fuzzy name matching. A **"Filter Help"** button lists them all.
Supported tags (with `>` `<` `=` `+` `~` operators):
- `fep:str>10` — Food Event Points by attribute (str/agi/int/cha/dex/con/wil/psy/per)
- `nrg:>50` / `hunger:<3` — food energy / hunger
- `attr:agi>2` — attribute/skill bonuses on equipment & gildings
- `q:min<12` / `q:ess+21` — quality (min/max/average/essence/substance/vitality)
- `armor:hard>3` — hard/soft/total armor
- `has:water>2` — container contents
- `lp>100` / `xp` / `mw` — curiosity learning points / experience / mental weight
- `symb:fep>2` — symbel (feast) bonuses
- `from:board` — craft recipe ingredients

Inventory Search highlights matching items in open inventories (stacks included).

## Food / FEP
- **Food Event Points hover preview** — with the Character Sheet open, hovering any food draws
  translucent, per-attribute ghost segments on the *Food Event Points* bar, showing how much (and
  which attributes) that food would add (efficiency-adjusted for satiation, glut, verified/subscribed
  and table feast bonuses).
- **Hide Extra FEP Details in Food Tooltips** (option, default on) — hides the weighted/actual FEP
  totals, the "fill your FEP bar to X%" line and the modifier list from food tooltips.
- **Stack tooltips** show the first contained item's info (food values, etc.) instead of the generic
  "To stack items…" help text.
- **Check Water Quality** script (menu button).

## World interaction
- **Right-Click Proximity** (option in *Altered Gameplay Settings* + radius slider) — a right-click
  that misses every object snaps to the nearest object instead. Distance is measured to the object's
  **hitbox edge** (falling back to its center when no collision data exists, e.g. forageables), and
  the clicked object briefly flashes so you can see what was hit.
- **Stockpile shortcuts** (in the world): **Ctrl+Right-click** takes one item into your inventory
  (leaves the window open for repeated grabs); **Ctrl+Shift+Right-click** deposits as many matching
  items from your inventory as will fit.
- **Gob alarm-sound menu** — **Shift+Middle-click** any object in the world to open a small flower
  menu: *Add Sound* (opens the Custom Alarm Manager with the object's resource path pre-filled, so
  you only pick a sound and hit Add Alarm) or *Remove Sound* if it already has one.
- **Equip Stone Axe and Shield** quick-switch button (menu grid → *QuickSwitchFromBelt*), alongside
  the existing sword+shield / two-hander equip-from-belt buttons.
- **Ctrl+Shift+Middle-click** an inventory item opens its [ringofbrodgar](https://ringofbrodgar.com)
  wiki page.
- **Toggle Map Icon (Cursor)** keybind — hover an object and press the key to toggle its minimap
  icon on/off without opening Map Icons Settings.

## Alarms
- **Custom Alarm Manager sound picker** — the *Sound File* field is a scrollable dropdown of every
  `.wav` in the `AlarmSounds` folder (10 shown at a time, mouse-wheel + scrollbar), instead of a
  free-text box. Applies to existing alarms and the "create new alarm" row.

## Combat UI
- **Opponents Panel offset** (two sliders in *Combat Settings*) — move the top-right combat panel
  (opponent portraits / Pursue / maneuvers) anywhere on screen; the X/Y offset is saved.

## Camera
- **Free Camera Zoom Speed** slider maximum tripled, for much faster zooming.

## Map & markers
- **Squirrel Cache** minimap icon.
- **Auto-mark Cave Entrances/Exits** (option) — automatically places permanent "Cave In" / "Cave Out"
  markers on natural cave-passage gobs, similar to how Burrows are auto-marked.
- Fixed a minimap tooltip texture (VRAM) leak that could hard-freeze the whole system on hover.

## Integration & build
- **Discord Rich Presence** (world detection, solo/party mode).
- **Steam** integration and a **multi-instance launcher** (`Play_WithSteam.bat`).
- SQLite databases (`static_data.db`, `saved_routes.db`, `hitboxes.db`) live in the project root so
  they survive rebuilds.

---

# Hurricane Client

This is just another custom client you can use to play the wonderful game,
Haven & Hearth. This client is built on top of the "Vanilla" Client, and
does not depend on any other custom clients.
I try to merge all of the code changes that are done to the base client
by Loftar, and I try to keep it up to date, to avoid crashes.

This client can be played standalone, or through Steam, by subscribing to
the Steam Workshop item.

Important Note:
- This client does not send any data to any place besides the official Seatribe server, unless you set it to do so.

## Links:

Forum Thread:
https://www.havenandhearth.com/forum/viewtopic.php?t=76544

Discord Server:
https://discord.gg/7Ct4t6uME6

Steam Workshop:
https://steamcommunity.com/sharedfiles/filedetails/?id=3423755273

## Downloading/Updating the Hurricane Client (Outside of Steam):
Use the Hurricane Updater: https://github.com/Nightdawg/Hurricane-Updater/releases/latest/download/HurricaneUpdater.jar
(Source Code: https://github.com/Nightdawg/Hurricane-Updater)

### If the updater doesn't work:
1. Make sure your installed Java version is **any version between Java 17 and Java 25**
2. You might need to add the updater file (HurricaneUpdater.jar) to your anti-virus exceptions list.

## Launching the Hurricane Client (Outside of Steam):

Run the Play.bat file inside the client folder, or Play_Linux.sh (for Linux/MacOS)

The client works with **any version between Java 17 and Java 25**
I've also been playing on GraalVM (some different open-source java distribution based on OpenJDK),
and I seem to get like 15-20 extra FPS out of the client.

### If the client doesn't launch:
1. Make sure your installed Java version is **any version between Java 17 and Java 25**
2. You might need to add the launcher file (Play.bat or Play_Linux.sh) to your anti-virus exceptions list.


## This client also supports Cediner's Web Map server (you set up your own private map server, it's not a public map):
https://github.com/Cediner/hnh-map-vuetify 

Ganhart/Aritain's updated version: https://github.com/Aritain/hnh-map-updated

## OR you can use dafels' Mapping service (or set up your own private map server):
https://www.havenandhearth.com/forum/viewtopic.php?f=49&t=79701

## Additionally, the client also supports the cookbook integration (disabled by default).
You can either use a token from a public cookbook, or host your own (for example, https://github.com/Cediner/hnh-food-book)
