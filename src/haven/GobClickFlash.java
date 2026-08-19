package haven;

import haven.render.Pipe;

import java.awt.Color;

/* ND: Brief, subtle highlight applied to a gob that was clicked via right-click
 * proximity, so you can tell which object actually got clicked. Auto-removed after
 * a short delay by MapView (see flashGob). */
public class GobClickFlash extends GAttrib implements Gob.SetupMod {

    public static ColorMask flashColor = new ColorMask(new Color(255, 236, 140, 120));

    public GobClickFlash(Gob g) {
        super(g);
    }

    public Pipe.Op gobstate() {
        return flashColor;
    }
}
