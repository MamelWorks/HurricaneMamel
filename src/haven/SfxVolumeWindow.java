package haven;

import java.util.List;

/**
 * ND: Settings window for per-sound custom volumes (see {@link SfxCustomVolume}).
 * Shows every sound heard so far with a 0-100% slider, a search filter, and a Clear button.
 */
public class SfxVolumeWindow extends Window {
	private static SfxVolumeWindow instance;
	private final Scrollport scroll;
	private String filter = "";

	public SfxVolumeWindow() {
		super(UI.scale(370, 20), "Custom Sound Volumes", true);

		add(new Label("Per-sound volume. Sounds appear here as you hear them in game."), UI.scale(5, 2));

		add(new Label("Search:"), UI.scale(5, 24));
		add(new TextEntry(UI.scale(200), "") {
			protected void changed() {
				filter = this.buf.line();
				populate();
			}
		}, UI.scale(60, 22));

		add(new Button(UI.scale(80), "Clear") {
			public void click() {
				SfxCustomVolume.clear();
				populate();
			}
		}, UI.scale(280, 21));

		scroll = add(new Scrollport(UI.scale(new Coord(360, 380))), UI.scale(5, 48));
		populate();
		pack();
	}

	private void clearRows() {
		Widget cont = scroll.cont;
		java.util.List<Widget> kids = new java.util.ArrayList<>();
		for(Widget w = cont.child; w != null; w = w.next)
			kids.add(w);
		for(Widget w : kids)
			w.destroy();
	}

	private void populate() {
		clearRows();
		Widget cont = scroll.cont;
		int y = 0;
		String f = filter.toLowerCase();
		List<String> names = SfxCustomVolume.names();
		int shown = 0;
		for(String name : names) {
			if(!f.isEmpty() && !name.toLowerCase().contains(f))
				continue;
			final String nm = name;
			String disp = name.startsWith("sfx/") ? name.substring(4) : name;
			if(disp.length() > 40)
				disp = "…" + disp.substring(disp.length() - 39);

			cont.add(new Label(disp), UI.scale(2), y + UI.scale(3));
			final Label vlbl = new Label(SfxCustomVolume.getRegister(name) + "%");
			cont.add(new HSlider(UI.scale(110), 0, 100, SfxCustomVolume.getRegister(name)) {
				public void changed() {
					SfxCustomVolume.set(nm, this.val);
					vlbl.settext(this.val + "%");
				}

				public Object tooltip(Coord c, Widget prev) {
					return Text.render(nm + ": " + this.val + "%").tex();
				}
			}, new Coord(cont.sz.x - UI.scale(150), y));
			cont.add(vlbl, new Coord(cont.sz.x - UI.scale(34), y + UI.scale(3)));
			y += UI.scale(20);
			shown++;
		}
		if(shown == 0)
			cont.add(new Label(names.isEmpty() ? "(no sounds heard yet)" : "(no matches)"), UI.scale(2), y);
	}

	public static void toggle(UI ui) {
		if(instance != null) {
			instance.reqdestroy();
			instance = null;
			return;
		}
		instance = new SfxVolumeWindow();
		Widget root = (ui.gui != null) ? ui.gui : ui.root;
		Coord c = root.sz.sub(instance.sz).div(2);
		root.add(instance, new Coord(Math.max(0, c.x), Math.max(0, c.y)));
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if((sender == this) && msg.equals("close")) {
			SfxCustomVolume.save();
			instance = null;
			reqdestroy();
			return;
		}
		super.wdgmsg(sender, msg, args);
	}
}
