package gov.epa.emissions.googleearth.kml.generator.color;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;

public class ColorPaletteGeneratorImpl implements ColorPaletteGenerator {

	private Map<String, ColorPalette> diffColorPaletteMap;
	private Map<String, ColorPalette> regularColorPaletteMap;

	private ColorPalette spectrum6ColorPalette;
	private ColorPalette spectrum8ColorPalette;
	private ColorPalette diff7SpectrumColorPalette;
	private ColorPalette diff9SpectrumColorPalette;

	public ColorPaletteGeneratorImpl() {

		this.diffColorPaletteMap = new HashMap<String, ColorPalette>();
		this.regularColorPaletteMap = new HashMap<String, ColorPalette>();

		this.initializeAdditionalPalettes();
	}

	private void initializeAdditionalPalettes() {

		List<Color> colors = new ArrayList<Color>();
		colors.add(Color.BLUE);
		colors.add(Color.CYAN);
		colors.add(Color.GREEN);
		colors.add(Color.YELLOW);
		colors.add(Color.ORANGE);
		colors.add(Color.RED);

		this.spectrum6ColorPalette = new ColorPaletteImpl("6 Color Spectrum",
				colors.toArray(new Color[0]));

		colors = new ArrayList<Color>();
		colors.add(Color.BLUE);
		colors.add(Color.CYAN.darker());
		colors.add(Color.CYAN);
		colors.add(Color.GREEN);
		colors.add(Color.YELLOW);
		colors.add(Color.ORANGE);
		colors.add(Color.RED);
		colors.add(Color.RED.darker());

		this.spectrum8ColorPalette = new ColorPaletteImpl("8 Color Spectrum",
				colors.toArray(new Color[0]));

		colors = new ArrayList<Color>();
		colors.add(Color.BLUE);
		colors.add(Color.CYAN);
		colors.add(Color.GREEN);
		colors.add(Color.LIGHT_GRAY);
		colors.add(Color.YELLOW);
		colors.add(Color.ORANGE);
		colors.add(Color.RED);

		this.diff7SpectrumColorPalette = new ColorPaletteImpl(
				"7 Color Spectrum", colors.toArray(new Color[0]));

		colors = new ArrayList<Color>();
		colors.add(Color.BLUE);
		colors.add(Color.CYAN.darker());
		colors.add(Color.CYAN);
		colors.add(Color.GREEN);
		colors.add(Color.LIGHT_GRAY);
		colors.add(Color.YELLOW);
		colors.add(Color.ORANGE);
		colors.add(Color.RED);
		colors.add(Color.RED.darker());

		this.diff9SpectrumColorPalette = new ColorPaletteImpl(
				"9 Color Spectrum", colors.toArray(new Color[0]));

	}

	@Override
	public void createColorPalettes(int numColors) throws KMZGeneratorException {

		ColorBrewer colorBrewer = ColorBrewer.instance();
		BrewerPalette[] regularColorPalettes = colorBrewer.getPalettes(
				ColorBrewer.ALL, numColors);
		BrewerPalette[] diffColorPalettes = colorBrewer.getPalettes(
				ColorBrewer.DIVERGING, numColors);

		if (regularColorPalettes.length == 0 || diffColorPalettes.length == 0) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_INCORRECT_VALUE,
					"No color palettes found for bin count value of "
							+ numColors);
		}

		this.generatorDiffColorPalettes(diffColorPalettes, numColors);
		this.generatorRegularColorPalettes(regularColorPalettes, numColors);
	}

	private void addAdditionalPalettes(Map<String, ColorPalette> paletteMap,
			int numColors) {

		if (numColors == this.spectrum6ColorPalette.getColors().length) {
			paletteMap.put(this.spectrum6ColorPalette.getName(),
					this.spectrum6ColorPalette);
		} else if (numColors == this.spectrum8ColorPalette.getColors().length) {
			paletteMap.put(this.spectrum8ColorPalette.getName(),
					this.spectrum8ColorPalette);
		} else if (numColors == this.diff7SpectrumColorPalette.getColors().length) {
			paletteMap.put(this.diff7SpectrumColorPalette.getName(),
					this.diff7SpectrumColorPalette);
		} else if (numColors == this.diff9SpectrumColorPalette.getColors().length) {
			paletteMap.put(this.diff9SpectrumColorPalette.getName(),
					this.diff9SpectrumColorPalette);
		}
	}

	private void generatorDiffColorPalettes(BrewerPalette[] palettes,
			int numColors) {

		this.diffColorPaletteMap.clear();

		this.addAdditionalPalettes(this.diffColorPaletteMap, numColors);

		for (BrewerPalette brewerPalette : palettes) {

			String colorPaletteName = brewerPalette.getName() + " ("
					+ brewerPalette.getDescription() + ")";
			Color[] colors = brewerPalette.getColors(numColors);
			this.diffColorPaletteMap.put(colorPaletteName,
					new DiffColorPalette(colorPaletteName, colors));
		}
	}

	private void generatorRegularColorPalettes(BrewerPalette[] palettes,
			int numColors) {

		this.regularColorPaletteMap.clear();

		this.addAdditionalPalettes(this.regularColorPaletteMap, numColors);

		for (BrewerPalette brewerPalette : palettes) {

			String colorPaletteName = brewerPalette.getName() + " ("
					+ brewerPalette.getDescription() + ")";
			Color[] colors = brewerPalette.getColors(numColors);

			this.regularColorPaletteMap.put(colorPaletteName,
					new DiffColorPalette(colorPaletteName, colors));
		}
	}

	@Override
	public List<ColorPalette> getDiffColorPalettes() {

		List<ColorPalette> colorPalettes = new ArrayList<ColorPalette>(
				this.diffColorPaletteMap.values());
		Collections.sort(colorPalettes, new Comparator<ColorPalette>() {

			@Override
			public int compare(ColorPalette o1, ColorPalette o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});

		return colorPalettes;
	}

	@Override
	public List<ColorPalette> getRegularColorPalettes() {

		List<ColorPalette> colorPalettes = new ArrayList<ColorPalette>(
				this.regularColorPaletteMap.values());
		Collections.sort(colorPalettes, new Comparator<ColorPalette>() {

			@Override
			public int compare(ColorPalette o1, ColorPalette o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});

		return colorPalettes;
	}

	@Override
	public ColorPalette getDiffColorPalette(String name) {
		return this.diffColorPaletteMap.get(name);
	}

	@Override
	public ColorPalette getRegularColorPalette(String name) {
		return this.regularColorPaletteMap.get(name);
	}
}
