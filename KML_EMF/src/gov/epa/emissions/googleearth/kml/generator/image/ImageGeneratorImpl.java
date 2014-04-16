package gov.epa.emissions.googleearth.kml.generator.image;

import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;
import gov.epa.emissions.googleearth.kml.generator.BinnedMultiGeometrySourceGenerator;
import gov.epa.emissions.googleearth.kml.generator.BinnedPointSourceGenerator;
import gov.epa.emissions.googleearth.kml.generator.preprocessor.PreProcessor;
import gov.epa.emissions.googleearth.kml.image.ImageWriter;
import gov.epa.emissions.googleearth.kml.image.LegendImageWriter;
import gov.epa.emissions.googleearth.kml.image.LegendStatsWriter;
import gov.epa.emissions.googleearth.kml.image.LegendTitleWriter;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageGeneratorImpl implements ImageGenerator {

	private ArrayList<File> images;
	private File legend;
	private boolean drawTitleLegend;
	private File titleLegend;
	private File statsLegend;

	public ImageGeneratorImpl() {

	}

	@Override
	public void generateImages(BinnedPointSourceGenerator generator) {

		this.images = new ArrayList<File>();

		ImageWriter imageWriter = new ImageWriter(16);

		try {

			List<Color> colors = new ArrayList<Color>();
			for (Integer rgb : generator.getRGBs()) {
				colors.add(new Color(rgb));
			}

			for (Color color : colors) {
				imageWriter.drawImage(color);
				File file = File.createTempFile("icon", ".png");
				this.images.add(file);
				imageWriter.writeImage(file);
			}

			LegendImageWriter legendImageWriter = new LegendImageWriter();
			legendImageWriter.drawImage(colors, generator.getBinRangeManager());
			this.legend = File.createTempFile("legend", ".png");
			legendImageWriter.writeImage(this.legend);

			String title = PropertiesManager.getInstance().getValue(
					PropertyKey.PLOT_TITLE);
			String subtitle = PropertiesManager.getInstance().getValue(
					PropertyKey.PLOT_SUBTITLE);

			LegendTitleWriter legendTitleWriter = new LegendTitleWriter();
			legendTitleWriter.drawImage(title, subtitle);

			this.drawTitleLegend = !legendTitleWriter.isEmpty();
			this.titleLegend = File.createTempFile("titleLegend", ".png");
			legendTitleWriter.writeImage(this.titleLegend);

			if (generator.isShowStats()) {

				LegendStatsWriter legendStatsWriter = new LegendStatsWriter();
				PreProcessor preProcessor = generator.getPreProcessor();
				legendStatsWriter
						.drawImage(preProcessor.getMinValue(), preProcessor
								.getMaxValue(), preProcessor.getMeanValue());

				this.statsLegend = File.createTempFile("statsLegend", ".png");
				legendStatsWriter.writeImage(this.statsLegend);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean shouldDrawTitleLegend() {
		return drawTitleLegend;
	}

	public ArrayList<File> getImages() {
		return images;
	}

	public File getLegend() {
		return legend;
	}

	public File getTitleLegend() {
		return titleLegend;
	}

	public File getStatsLegend() {
		return statsLegend;
	}

	@Override
	public void generateImages(BinnedMultiGeometrySourceGenerator generator) {
		// TODO Auto-generated method stub
		
	}
}
