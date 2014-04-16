package gov.epa.emissions.googleearth.kml.vgrid;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

public class RectangularLegend {

	private BufferedImage bufferedImage;

	public RectangularLegend() {
	}

	public void drawImage(int size, List<Color> colors, List<Float> values,
			String comment) {

		List<String> textList = createStringList(values, comment);

		Font f = new Font("Arial", Font.BOLD, size / 2);
		int fontSize = f.getSize();

		int longestText = getLongestStringLength(textList, f);

		this.bufferedImage = new BufferedImage(3 * size / 2 + longestText, size
				* (colors.size() + 2), BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = (Graphics2D) this.bufferedImage.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g.setFont(f);

		int border = 2;
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, this.bufferedImage.getWidth() - 1, this.bufferedImage
				.getHeight() - 1);

		g.setColor(Color.WHITE);
		g.fillRect(border, border, this.bufferedImage.getWidth() - 1 - 2
				* border, this.bufferedImage.getHeight() - 1 - 2 * border);

		int i = 0;
		int x = size / 2;
		int y = size / 6;
		int width = size / 2;
		int height = size;

		for (; i < colors.size(); i++) {
			y += size;

			g.setColor(colors.get(i));
			g.fillRect(x, y, width, height);

			g.setColor(Color.DARK_GRAY);
			g.drawString(textList.get(i), x + size, y + fontSize / 2);
		}

		y += size;
		g.setColor(Color.DARK_GRAY);
		g.drawString(textList.get(i), x + size, y + fontSize / 2);

//		g.drawString(textList.get(++i), longestText / 3, y + 2 * fontSize);
	}

	public void drawTitleImage(int size, int bottom, String title) {

		List<String> textList = Arrays.asList(new String[] { title });

		Font f = new Font("Arial", Font.BOLD, size);

		int longestText = getLongestStringLength(textList, f);

		this.bufferedImage = new BufferedImage(2 * size + longestText, 2 * size + bottom,
				BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = (Graphics2D) this.bufferedImage.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g.setFont(f);

		int border = 2;
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, this.bufferedImage.getWidth() - 1, this.bufferedImage
				.getHeight() - 1);

		g.setColor(Color.WHITE);
		g.fillRect(border, border, this.bufferedImage.getWidth() - 1 - 2
				* border, this.bufferedImage.getHeight() - 1 - 2 * border);

		int x = size / 2;
		int y = size + 5;

		g.setColor(Color.BLUE);
		g.drawString(title, x, y);
	}

	private static int getLongestStringLength(List<String> strings, Font f) {

		int retVal = 0;

		FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(f);

		for (String string : strings) {
			retVal = Math.max(retVal, fontMetrics.stringWidth(string));
		}

		return retVal;
	}

	private static List<String> createStringList(List<Float> values,
			String comment) {
		List<String> textList = new ArrayList<String>();
		NumberFormat formatter = new DecimalFormat("0.00E0");

		for (int i = 0; i < values.size(); i++)
			textList.add(formatter.format(values.get(i)));

		textList.add(comment);

		return textList;
	}

	public void writeImage(File file) throws FileNotFoundException, IOException {
		ImageIO.write(this.bufferedImage, "png", new FileOutputStream(file));
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		RectangularLegend legend = new RectangularLegend();

		List<Color> colors = new ArrayList<Color>();
		colors.add(Color.RED);
		colors.add(Color.ORANGE);
		colors.add(Color.YELLOW);
		colors.add(Color.GREEN);
		colors.add(Color.CYAN);
		colors.add(Color.BLUE);

		List<Float> values = new ArrayList<Float>();
		values.add(234.567f);
		values.add(123f);
		values.add(3847f);
		values.add(2.333f);
		values.add(123.89f);
		values.add(1.0f);
		values.add(.000376f);

		legend.drawImage(32, colors, values, "mol/hr");
		legend.writeImage(new File("D:\\vis\\legend.png"));
	}
}
