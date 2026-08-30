package haven.automated;

import haven.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static haven.OCache.posres;
import static java.lang.Thread.sleep;

/* ND: Ctrl + Shift + Right-click a storage container (cupboard, crate, chest, ...) in the
 * world to look at what's inside and then deposit every matching item type from your main
 * inventory into it. Matching is by display name (minus the ", stack of" suffix, so loose
 * items and stacks of the same thing count as one type). Depositing uses the native
 * "transfer" message, which moves the item into the topmost open inventory (the container we
 * just opened) and lets the server stack it onto an existing stack when possible. The
 * container window is left open. Mirrors DepositAllToStockpile, but for slot inventories. */
public class DepositMatchingToContainer implements Runnable {
    private final GameUI gui;
    private final Gob gob;

    private static final String STACK_SUFFIX = ", stack of";

    // ND: Gob resource-name fragments that identify a storage container with a slot inventory.
    // Extend this if you find a container type that isn't picked up. Deliberately avoids bare
    // "box" so crafting stations like the steel box aren't caught.
    private static final String[] CONTAINER_NAMES = {
        "cupboard", "chest", "crate", "coffer", "cabinet", "wardrobe", "basket", "trunk"
    };

    public DepositMatchingToContainer(GameUI gui, Gob gob) {
        this.gui = gui;
        this.gob = gob;
    }

    public static boolean isContainer(String resname) {
        if (resname == null)
            return false;
        for (String c : CONTAINER_NAMES) {
            if (resname.contains(c))
                return true;
        }
        return false;
    }

    @Override
    public void run() {
        try {
            if (gui.vhand != null) {
                gui.error("Empty your hands before depositing to a container!");
                return;
            }

            // Which container inventories are already open, so we can spot the one we open now.
            Set<Inventory> before = containerInventories();

            // A plain right-click on the container opens its window.
            gui.map.wdgmsg("click", Coord.z, gob.rc.floor(posres), 3, 0, 0, (int) gob.id, gob.rc.floor(posres), 0, -1);

            // Wait for the container's inventory to appear (the one that wasn't open before).
            Inventory cont = null;
            for (int i = 0; i < 75 && cont == null; i++) {
                sleep(20);
                for (Inventory inv : containerInventories()) {
                    if (!before.contains(inv)) {
                        cont = inv;
                        break;
                    }
                }
            }
            // Fallback: the container was already open (nothing new appeared) and it's the only one.
            if (cont == null) {
                Set<Inventory> now = containerInventories();
                if (now.size() == 1)
                    cont = now.iterator().next();
            }
            if (cont == null) {
                gui.error("Couldn't find the container's contents.");
                return;
            }

            // Give the contents a moment to stream in from the server.
            Set<String> wanted = new HashSet<>();
            for (int i = 0; i < 30; i++) {
                wanted = distinctNames(cont);
                if (!wanted.isEmpty())
                    break;
                sleep(20);
            }
            if (wanted.isEmpty()) {
                gui.ui.msg("Container is empty - nothing to match.");
                return;
            }

            // Transfer every main-inventory item whose type is present in the container.
            int moved = 0;
            for (WItem w : new ArrayList<>(gui.maininv.getAllItems())) {
                if (gui.vhand != null)
                    break; // safety: something got picked up onto the cursor
                String n = norm(w);
                if (n != null && wanted.contains(n)) {
                    w.item.wdgmsg("transfer", new Coord(w.sz.x / 2, w.sz.y / 2), 1);
                    moved++;
                    sleep(15);
                }
            }
            gui.ui.msg("Deposited " + moved + " matching item(s) into the container.");
        } catch (InterruptedException e) {
            // interrupted - just stop
        } catch (Exception ignored) {
        }
    }

    private Set<String> distinctNames(Inventory inv) {
        Set<String> names = new HashSet<>();
        for (WItem w : inv.getAllItems()) {
            String n = norm(w);
            if (n != null)
                names.add(n);
        }
        return names;
    }

    // ND: Normalize an item to its type name: display name minus the ", stack of" suffix so a
    // loose item and a stack of the same thing are treated as one type.
    private String norm(WItem w) {
        try {
            String name = w.item.getname();
            if (name == null || name.isEmpty())
                return null;
            if (name.endsWith(STACK_SUFFIX))
                name = name.substring(0, name.length() - STACK_SUFFIX.length());
            return name;
        } catch (Exception e) {
            return null;
        }
    }

    // ND: Every open slot inventory that isn't one of the player's own (Inventory/Belt/etc.).
    private Set<Inventory> containerInventories() {
        Set<Inventory> res = new LinkedHashSet<>();
        for (Inventory inv : gui.ui.root.children(Inventory.class)) {
            if (inv == gui.maininv)
                continue;
            Window wnd = inv.getparent(Window.class);
            String cap = (wnd != null) ? wnd.cap : null;
            if (cap != null && Inventory.PLAYER_INVENTORY_NAMES.contains(cap))
                continue;
            res.add(inv);
        }
        return res;
    }
}
