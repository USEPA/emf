package gov.epa.emissions.googleearth.kml.generator.color;

import gov.epa.emissions.googleearth.kml.KMZGeneratorException;

import java.util.List;

public interface ColorPaletteGenerator {

	void createColorPalettes(int numColors) throws KMZGeneratorException;

	List<ColorPalette> getRegularColorPalettes();

	List<ColorPalette> getDiffColorPalettes();

	ColorPalette getRegularColorPalette(String name);

	ColorPalette getDiffColorPalette(String name);
}
