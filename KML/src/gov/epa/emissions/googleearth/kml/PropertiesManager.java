package gov.epa.emissions.googleearth.kml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class PropertiesManager {

	private static PropertiesManager instance = new PropertiesManager();

	public enum PropertyKey {

		PLOT_TITLE("plot.title", String.class, "Title", false, false,
				"Main title of plot."), // 
		PLOT_SUBTITLE("plot.subtitle", String.class, "Subtitle", false, false,
				"Subtitle of plot."), // 
		LABEL_COLUMNNAME("label.columnName", String.class, "Label Column",
				false, true,
				"Column used to label and identified the data point."), // 
		FILTER_COLUMNNAME("filter.columnName", String.class, "Filter Column",
				false, true,
				"Column by which to filter the data (e.g., \"pollutant\")."), //
		FILTER_VALUE(
				"filter.value",
				String.class,
				"Filter Value",
				false,
				false,
				"Value to filter by (e.g., the pollutant to be viewed). Leaving this field blank will turn off the filter."), //
		DATA_COLUMNNAME("data.columnName", String.class, "Data Column", false,
				true, "Data column of interest"), //
		DATA_MINIMUM("data.minimum", String.class, "Minimum Data Cutoff",
				false, false, "Data with values below this will be excluded."), //
		ICON_SCALE("icon.scale", Double.class, "Icon Scale", false, false,
				"Scale of icons used for display. Valid values are between 0 and 1.");//

		private String key;
		private Class type;
		private String displayName;
		private boolean hidden;
		private boolean column;
		private String description;

		private PropertyKey(String key, Class type, String displayName,
				boolean hidden, boolean column, String description) {

			this.key = key;
			this.type = type;
			this.displayName = displayName;
			this.hidden = hidden;
			this.column = column;
			this.description = description;
		}

		@Override
		public String toString() {
			return this.key;
		}

		public String getKey() {
			return this.key;
		}

		public static Class getType(String key) {

			Class type = null;
			PropertyKey[] values = PropertyKey.values();
			for (PropertyKey propertyKey : values) {

				if (propertyKey.key.equals(key)) {

					type = propertyKey.type;
					break;
				}
			}

			if (type == null) {
				throw new RuntimeException("Unable to find ProopertyKey '"
						+ key + "'");
			}

			return type;
		}

		public String getDisplayName() {
			return displayName;
		}

		public boolean isHidden() {
			return hidden;
		}

		public boolean isColumn() {
			return this.column;
		}

		public boolean isFilterColumn() {
			return this.equals(PropertyKey.FILTER_COLUMNNAME);
		}

		public static PropertyKey getPropertKey(String keyString) {

			PropertyKey propertyKey = null;
			PropertyKey[] values = PropertyKey.values();
			for (PropertyKey key : values) {

				if (key.key.equals(keyString)) {

					propertyKey = key;
					break;
				}
			}

			return propertyKey;
		}

		public String getDescription() {
			return description;
		}
	}

	private Properties properties;

	public static synchronized PropertiesManager getInstance() {
		return instance;
	}

	private PropertiesManager() {

		this.properties = new Properties();
		// this.properties.setProperty(PropertyKey.DATA_MINIMUM.getKey(), "0");

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
			for (Object key : this.properties.keySet()) {
				System.out.println(key + "=" + this.properties.get(key));
			}
		}

		this.setValue(PropertyKey.DATA_MINIMUM.getKey(), "0");
		this.setValue(PropertyKey.ICON_SCALE.getKey(), ".3");
	}

	public void initProperties(String[] parameters)
			throws KMZGeneratorException {

		try {
			this.properties.clear();
			this.properties.load(new FileInputStream(parameters[1]));

			for (int i = 2; i < parameters.length;) {

				String key = parameters[i++].substring(1);
				String value = parameters[i++];
				this.properties.setProperty(key, value);
			}

			if (ConfigurationManager.getInstance().getValueAsBoolean(
					ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
				for (Object key : this.properties.keySet()) {
					System.out.println(key + "=" + this.properties.get(key));
				}
			}

			this.validateInput();

		} catch (Exception e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_GENERAL,
					"Error while reading properties file '" + parameters[1]
							+ "': " + e.getLocalizedMessage());
		}
	}

	public void initProperties(String inputFile) throws KMZGeneratorException {

		try {
			this.properties.clear();
			this.properties.load(new FileInputStream(inputFile));

			if (ConfigurationManager.getInstance().getValueAsBoolean(
					ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
				for (Object key : this.properties.keySet()) {
					System.out.println(key + " " + this.properties.get(key));
				}
			}

			this.validateInput();

		} catch (Exception e) {
			throw new KMZGeneratorException(
					KMZGeneratorException.ERROR_CODE_PROCESSING_PROPERTIES_FILE,
					"Error while processing properties file '" + inputFile
							+ "': " + e.getLocalizedMessage());
		}
	}

	public void storeProperties(File file) throws IOException {

		FileWriter fw = new FileWriter(file);

		this.properties.store(fw, "Saved properties file");

		fw.flush();
		fw.close();
	}

	protected void validateInput() throws RuntimeException {

		PropertyKey[] values = PropertyKey.values();
		for (PropertyKey propertyKeys : values) {

			String key = propertyKeys.getKey();
			if (!this.properties.containsKey(key)) {
				throw new RuntimeException("Missing required property: " + key);
			}
		}
	}

	public Set<String> getKeys() {
		return this.properties.stringPropertyNames();
	}

	public String getValue(String key) {
		return this.properties.getProperty(key);
	}

	public void setValue(String key, String value) {
		this.properties.setProperty(key, value);

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
			System.out.println(key + "=" + this.properties.get(key));
		}
	}

	public static void main(String[] args) throws KMZGeneratorException {

		PropertiesManager
				.getInstance()
				.initProperties(
						new String[] { "D:/cep/GoogleEarth/src/resources/properties/input.properties" });
	}
}
