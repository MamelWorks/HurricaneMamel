package haven.automated;

import haven.*;

import static haven.OCache.posres;
import static java.lang.Thread.sleep;

/* ND: Ctrl + Right-click a stockpile in the world to pull a single item into your
 * inventory. Taking from a stockpile is server-side via the ISBox "xfer2", which only
 * exists while the window is open, so we open it (if needed) and transfer one. The
 * window is left open so repeated Ctrl+Right-clicks quickly pull out several. */
public class TakeOneFromStockpile implements Runnable {
    private final GameUI gui;
    private final Gob gob;

    public TakeOneFromStockpile(GameUI gui, Gob gob) {
        this.gui = gui;
        this.gob = gob;
    }

    @Override
    public void run() {
        try {
            boolean alreadyOpen = findStockpileWindow() != null;
            if (!alreadyOpen) {
                // A plain right-click on the stockpile opens its window.
                gui.map.wdgmsg("click", Coord.z, gob.rc.floor(posres), 3, 0, 0, (int) gob.id, gob.rc.floor(posres), 0, -1);
            }
            Window win = null;
            ISBox box = null;
            for (int i = 0; i < 60 && box == null; i++) {
                sleep(20);
                win = findStockpileWindow();
                if (win != null)
                    box = win.getchild(ISBox.class);
            }
            if (box != null) {
                box.transfer(-1, 1); // pull one item into the inventory; leave the window open
            }
        } catch (InterruptedException e) {
        } catch (Exception ignored) {
        }
    }

    private Window findStockpileWindow() {
        try {
            for (Window w : gui.getAllWindows()) {
                if (w.cap != null && (w.cap.equals("Stockpile") || w.cap.equals("Sack")))
                    return w;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
