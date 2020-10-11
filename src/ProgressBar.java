import java.awt.Color;
import java.awt.Rectangle;

import processing.core.PGraphics;

public class ProgressBar extends Rectangle {
	private static final long serialVersionUID = -6200269577343689741L;

	protected float progress = 0.0f;
	private final Color backgroundColor = new Color(50, 50, 50);
	private final Color backgroundStroke = new Color(166, 191, 28);
	private final Color progressColor = new Color(100, 100, 15);
	private final Color textColor = new Color(255, 255, 255);

	public ProgressBar(final int x, final int y, final int width, final int height) {
		super(x, y, width, height);
	}

	public void draw(final PGraphics g) {
		// Draw Background Bar
		g.noStroke();
		g.fill(backgroundColor.getRGB());
		g.rect(this.x, this.y, this.width, this.height);

		// Draw Progress
		g.fill(progressColor.getRGB());
		g.rect(this.x, this.y, this.width * this.progress, this.height);

		// Draw Outline
		g.noFill();
		g.stroke(backgroundStroke.getRGB());
		g.strokeWeight(2);
		g.rect(this.x, this.y, this.width, this.height);

		// Draw Percentage
		g.fill(textColor.getRGB());
		g.textAlign(PGraphics.CENTER, PGraphics.CENTER);
		g.text((int) Math.floor(this.progress * 100) + "%", this.x + this.width / 2, this.y + this.height / 2);
	}
}
