package haven;

import java.awt.Color;

public class CenteredMessageOverlay extends Widget {
	private Tex textTex;
	private double startTime;
	private double duration;
	private boolean expired = false;

	public CenteredMessageOverlay(String message, Color color, double durationSeconds) {
		this.textTex = Text.renderstroked(message, color, Color.BLACK, Text.num12boldFnd).tex();
		this.duration = durationSeconds;
		this.startTime = Utils.rtime();
	}

	@Override
	public void draw(GOut g) {
		super.draw(g);
		double elapsed = Utils.rtime() - startTime;

		if (elapsed > duration) {
			if (!expired) {
				expired = true;
				this.destroy();
			}
			return;
		}

		if (parent == null) {
			return;
		}

		// Center the text on the screen
		Coord screenCenter = parent.sz.div(2);
		Coord textSize = textTex.sz();
		Coord textPos = screenCenter.sub(textSize.div(2));

		// Draw semi-transparent background
		int padding = UI.scale(10);
		Coord bgPos = textPos.sub(padding, padding);
		Coord bgSize = textSize.add(padding * 2, padding * 2);
		g.chcolor(0, 0, 0, 192);
		g.frect(bgPos, bgSize);
		g.chcolor();

		// Draw the text
		g.image(textTex, textPos);
	}
}
