package gov.epa.emissions.googleearth.kml.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageWriter {

	private BufferedImage bufferedImage;
	private int size;

	public ImageWriter(int size) {

		this.size = size;
		this.bufferedImage = new BufferedImage(size, size,
				BufferedImage.TYPE_INT_ARGB);
	}

	public void writeImage(File outputFile) throws IOException {
		ImageIO.write(this.bufferedImage, "png", outputFile);
	}

	public void drawImage(Color color) {

		Graphics2D g = (Graphics2D) this.bufferedImage.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(color);
		g.fillOval(1, 1, this.size - 2, this.size - 2);
	}

	public static void main(String[] args) throws IOException {

		ImageWriter imageWriter = new ImageWriter(16);
		imageWriter.drawImage(Color.RED);
		imageWriter.writeImage(new File("./red.png"));
		imageWriter.drawImage(Color.ORANGE);
		imageWriter.writeImage(new File("./orange.png"));
		imageWriter.drawImage(Color.YELLOW);
		imageWriter.writeImage(new File("./yellow.png"));
		imageWriter.drawImage(Color.GREEN);
		imageWriter.writeImage(new File("./green.png"));
		imageWriter.drawImage(Color.BLUE);
		imageWriter.writeImage(new File("./blue.png"));
	}
}
