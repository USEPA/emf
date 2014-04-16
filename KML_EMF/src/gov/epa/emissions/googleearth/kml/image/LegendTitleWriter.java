package gov.epa.emissions.googleearth.kml.image;

import gov.epa.emissions.googleearth.kml.ConfigurationManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LegendTitleWriter {

	private boolean empty = true;

	private BufferedImage bufferedImage;

	private static final BufferedImage NULL_BUFFERED_IMAGE = new BufferedImage(
			1, 1, BufferedImage.TYPE_INT_ARGB);

	public LegendTitleWriter() {
	}

	public void writeImage(File outputFile) throws IOException {
		ImageIO.write(this.bufferedImage, "png", outputFile);
	}

	public void drawImage(String title, String subtitle) {

		if (title == null) {
			title = "";
		}

		if (subtitle == null) {
			subtitle = "";
		}

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {

			System.out.println("Title: " + title);
			System.out.println("Subtitle: " + subtitle);
		}

		Graphics graphics = NULL_BUFFERED_IMAGE.getGraphics();
		Font font = graphics.getFont();
		Font titleFont = new Font(font.getName(), Font.BOLD, 16);
		graphics.setFont(titleFont);
		FontMetrics fm = graphics.getFontMetrics();

		int titleBuffer = 10;

		int titleWidth = fm.stringWidth(title) + 2 * titleBuffer;
		int titleFontHeight = fm.getHeight();
		int titleFontAscent = fm.getAscent();

		Font subtitleFont = new Font(font.getName(), Font.PLAIN, 14);
		graphics.setFont(subtitleFont);
		fm = graphics.getFontMetrics();

		int subtitleWidth = fm.stringWidth(subtitle) + 2 * titleBuffer;
		int subtitleFontHeight = fm.getHeight();
		int subtitleFontAscent = fm.getAscent();

		int imageWidth = Math.max(titleWidth, subtitleWidth);

		boolean titleEmpty = title.isEmpty();
		boolean subtitleEmpty = subtitle.isEmpty();

		int y = 0;

		if (!titleEmpty) {

			this.empty = false;
			y += titleFontHeight;
		}

		if (!subtitleEmpty) {

			this.empty = false;
			y += subtitleFontHeight;
		}

		this.bufferedImage = new BufferedImage(imageWidth, y + 2 * titleBuffer,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = (Graphics2D) this.bufferedImage.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, this.bufferedImage.getWidth() - 1, this.bufferedImage
				.getHeight() - 1);

		g.setColor(Color.WHITE);
		g.fillRect(2, 2, this.bufferedImage.getWidth() - 5, this.bufferedImage
				.getHeight() - 5);

		g.setColor(Color.BLACK);

		int titlePosY = 0;
		if (!titleEmpty) {

			titlePosY += titleFontAscent + titleBuffer;
			g.setFont(titleFont);
			g.drawString(title, (imageWidth - titleWidth) / 2 + titleBuffer,
					titlePosY);

			titlePosY += subtitleFontHeight;
		} else {
			titlePosY = titleBuffer + subtitleFontAscent;
		}

		if (!subtitleEmpty) {

			g.setFont(subtitleFont);
			g.drawString(subtitle, (imageWidth - subtitleWidth) / 2
					+ titleBuffer, titlePosY);
		}
	}

	public boolean isEmpty() {
		return this.empty;
	}

	public static void main(String[] args) throws IOException {

		LegendTitleWriter legendWriter = new LegendTitleWriter();
		legendWriter.drawImage("my title", "my subttile");
		legendWriter.writeImage(new File("./titleLegend.png"));
	}
}
