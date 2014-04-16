package gov.epa.emissions.googleearth.kml.generator.color;

import java.awt.Color;

public class DiffColorPalette extends ColorPaletteImpl {

	public DiffColorPalette(String name, Color[] posColors, Color[] negColors,
			Color zeroColor) {
		this(name, createColors(posColors, negColors, zeroColor));
	}

	public DiffColorPalette(String name, Color[] divergentColors) {

		super(name, divergentColors);
		assert (divergentColors.length % 2 == 1);
	}

	private static Color[] createColors(Color[] posColors, Color[] negColors,
			Color zeroColor) {

		assert (posColors.length == negColors.length && zeroColor != null);
		Color[] newColors = new Color[posColors.length + negColors.length + 1];

		int colorIndex = 0;
		for (Color color : posColors) {
			newColors[colorIndex++] = color;
		}

		newColors[colorIndex++] = zeroColor;

		for (Color color : negColors) {
			newColors[colorIndex++] = color;
		}

		return newColors;
	}
}
