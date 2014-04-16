package gov.epa.emissions.googleearth.kml.generator.color;

import java.awt.Color;

public class ColorPaletteImpl implements ColorPalette {

	private String name;
	private Color[] colors;

	public ColorPaletteImpl(String name, Color[] colors) {

		this.name = name;
		this.colors = colors;
	}

	@Override
	public Color[] getColors() {
		return this.colors;
	}

	@Override
	public String getName() {
		return this.name;
	}
}
