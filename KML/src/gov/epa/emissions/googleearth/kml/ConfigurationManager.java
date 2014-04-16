package gov.epa.emissions.googleearth.kml;

import gov.epa.emissions.googleearth.kml.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;

public class ConfigurationManager {

	private static ConfigurationManager instance = new ConfigurationManager();
	private Properties properties;

	public enum PropertyKey {

		SHOW_OUTPUT("show.ouput", Boolean.class, "Show Output", true);//

		private String key;
		private Class type;
		private String displayName;
		private boolean hidden;

		private PropertyKey(String key, Class type, String displayName,
				boolean hidden) {

			this.key = key;
			this.type = type;
			this.displayName = displayName;
			this.hidden = hidden;
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
	}

	private ConfigurationManager() {
		this.properties = new Properties();
	}

	public static synchronized ConfigurationManager getInstance() {
		return instance;
	}

	public void init(String[] inputFiles) throws KMZGeneratorException {

		for (String inputFile : inputFiles) {

			String path = null;
			try {

				URL resource = this.getClass().getResource(inputFile);

				if (resource == null) {
					new KMZGeneratorException(
							KMZGeneratorException.ERROR_CODE_PROPERTIES_FILE_DOESNT_EXIST,
							"Unable to locate properties file resource '"
									+ inputFile + "'");
				}

				path = resource.getPath();
				File file = new File(path);

				this.properties.load(new FileInputStream(file));

				if (ConfigurationManager.getInstance().getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
					for (Object key : this.properties.keySet()) {
						System.out
								.println(key + "=" + this.properties.get(key));
					}
				}
			} catch (FileNotFoundException e) {

				String message = "Error while opening properties file " + path
						+ "\n\n" + e.getLocalizedMessage();
				if (ConfigurationManager.getInstance().getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
					System.err.println(message);
				}

				JOptionPane.showMessageDialog(null,
						Utils.wrapLine(message, 80), "Open Error",
						JOptionPane.ERROR_MESSAGE);

			} catch (IOException e) {

				String message = "Error while reading properties file " + path
						+ "\n\n" + e.getLocalizedMessage();
				if (ConfigurationManager.getInstance().getValueAsBoolean(
						ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
					System.err.println(message);
				}

				JOptionPane.showMessageDialog(null,
						Utils.wrapLine(message, 80), "Read Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public Set<String> getKeys() {
		return this.properties.stringPropertyNames();
	}

	public String getValue(String key) {
		return this.properties.getProperty(key);
	}

	public Boolean getValueAsBoolean(String key) {
		return Boolean.parseBoolean(this.properties.getProperty(key));
	}

	public void setValue(String key, String value) {
		this.properties.setProperty(key, value);

		if (ConfigurationManager.getInstance().getValueAsBoolean(
				ConfigurationManager.PropertyKey.SHOW_OUTPUT.getKey())) {
			for (Object o : this.properties.keySet()) {
				System.out.println(o + "=" + this.properties.get(o));
			}
		}
	}

	public static void main(String[] args) throws KMZGeneratorException {
		ConfigurationManager.getInstance().init(
				new String[] { "/default.config" });
	}
}
