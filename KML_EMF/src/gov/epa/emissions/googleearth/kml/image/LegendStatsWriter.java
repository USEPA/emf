package gov.epa.emissions.googleearth.kml.image;

import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LegendStatsWriter {

	private BufferedImage bufferedImage;

	private static final BufferedImage NULL_BUFFERED_IMAGE = new BufferedImage(
			1, 1, BufferedImage.TYPE_INT_ARGB);

	public LegendStatsWriter() {
	}

	public void writeImage(File outputFile) throws IOException {
		ImageIO.write(this.bufferedImage, "png", outputFile);
	}

	public void drawImage(double min, double max, double mean) {

		System.out.println("min: " + min);
		System.out.println("max: " + max);
		System.out.println("mean: " + mean);

		Graphics graphics = NULL_BUFFERED_IMAGE.getGraphics();
		FontMetrics fm = graphics.getFontMetrics();

		int buffer = 10;
		int tab = 40;

		int fontHeight = fm.getHeight();
		int fontAscent = fm.getAscent();

		this.bufferedImage = new BufferedImage(120,
				3 * fontHeight + 2 * buffer, BufferedImage.TYPE_INT_ARGB);

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
		
		String minStr = Utils.format(min, min);//Utils.format(min, Utils.getFormat(min));
		String maxStr = Utils.format(max, max);//Utils.format(max, Utils.getFormat(max));
		String meanStr = Utils.format(mean, mean);//Utils.format(mean, Utils.getFormat(mean));
		// TODO: do not use range for now
		//double net = max-min;
		//if ( net<0) net = 0-net;
		//String minStr = Utils.format(min, Utils.getFormat(net));
		//String maxStr = Utils.format(max, Utils.getFormat(net));
		//String meanStr = Utils.format(mean, Utils.getFormat(net));
		

		g.drawString(minStr, buffer + tab, fontAscent + buffer);
		g.drawString(maxStr, buffer + tab, fontAscent + fontHeight + buffer);
		g.drawString(meanStr, buffer + tab, fontAscent + 2 * fontHeight
				+ buffer);

		g.drawString("Min:", buffer, fontAscent + buffer);
		g.drawString("Max:", buffer, fontAscent + fontHeight + buffer);
		g.drawString("Mean:", buffer, fontAscent + 2 * fontHeight + buffer);
	}

	public static void main(String[] args) throws IOException {

		LegendStatsWriter legendWriter = new LegendStatsWriter();
		legendWriter.drawImage(0, 1, .234234234234);
		legendWriter.writeImage(new File("./statsLegend.png"));
	}
}
