package gov.epa.emissions.googleearth.kml.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

public class LegendImageWriter {

	private BufferedImage bufferedImage;
	private int size = 16;

	public LegendImageWriter() {
	}

	public void writeImage(File outputFile) throws IOException {
		ImageIO.write(this.bufferedImage, "png", outputFile);
	}

	public void drawImage(List<Color> colors, List<String> ranges) {

		this.bufferedImage = new BufferedImage(2 * size + 140, 3 * size
				* colors.size() / 2, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = (Graphics2D) this.bufferedImage.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, this.bufferedImage.getWidth() - 1, this.bufferedImage
				.getHeight() - 1);

		g.setColor(Color.WHITE);
		g.fillRect(2, 2, this.bufferedImage.getWidth() - 5, this.bufferedImage
				.getHeight() - 5);

		int i = 0;
		for (Color color : colors) {

			g.setColor(color);
			g.fillOval(1 + size / 4, 1 + size / 4 + 3 * size * i / 2,
					this.size - 2, this.size - 2);
			i++;
		}

		int tab1 = 3 * size / 2;
		int tab2 = tab1 + 8;
		int tab3 = tab2 + 54;
		int tab4 = tab3 + 14;

		g.setColor(Color.BLACK);
		for (int j = 0; j < ranges.size() - 1; j++) {

			String low = ranges.get(j);
			String high = ranges.get(j + 1);

			if (low.equals(high)) {
				g.drawString(low, tab2, size + 3 * size * j / 2);
			} else {
				g.drawString(low, tab2, size + 3 * size * j / 2);
				g.drawString("to", tab3, size + 3 * size * j / 2);
				g.drawString(high, tab4, size + 3 * size * j / 2);
			}

		}

		int last = ranges.size() - 1;
		String low = ranges.get(ranges.size() - 1);
		g.drawString(">", tab1, size + 3 * size * last / 2);
		g.drawString(low, tab2, size + 3 * size * last / 2);

	}

	public static void main(String[] args) throws IOException {

		LegendImageWriter legendWriter = new LegendImageWriter();
		legendWriter.drawImage(Arrays
				.asList(new Color[] { Color.BLUE, Color.CYAN, Color.GREEN,
						Color.YELLOW, Color.ORANGE, Color.RED }), Arrays
				.asList(new String[] { "1.234", "1.234", "1.234", "1.234",
						"1.234", "1.234" }));
		legendWriter.writeImage(new File("./legend.png"));
	}
}
