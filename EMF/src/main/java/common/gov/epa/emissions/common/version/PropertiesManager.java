package gov.epa.emissions.common.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class PropertiesManager {

    private static PropertiesManager instance = new PropertiesManager();

    public enum PropertyKey {

        SCHEMA_CASE_VERSION("schema.case.version", Integer.class), // 
        SCHEMA_CASE_CHANGED("schema.case.changed", Boolean.class), // 
        SCHEMA_EMF_VERSION("schema.emf.version", Integer.class), // 
        SCHEMA_EMF_CHANGED("schema.emf.changed", Boolean.class), // 
        SCHEMA_COST_VERSION("schema.cost.version", Integer.class), // 
        SCHEMA_COST_CHANGED("schema.cost.changed", Boolean.class), // 
        SCHEMA_PUBLIC_VERSION("schema.public.version", Integer.class), // 
        SCHEMA_PUBLIC_CHANGED("schema.public.changed", Boolean.class), // 
        SCHEMA_REFERENCE_VERSION("schema.reference.version", Integer.class), // 
        SCHEMA_REFERENCE_CHANGED("schema.reference.changed", Boolean.class), // 
        CODEBASE_JAVA_VERSION("codebase.java.version", Integer.class), // 
        CODEBASE_JAVA_CHANGED("codebase.java.changed", Boolean.class); // 

        private String key;

        private Class<?> type;

        private PropertyKey(String key, Class<?> type) {

            this.key = key;
            this.type = type;
        }

        @Override
        public String toString() {
            return this.key;
        }

        public String getKey() {
            return this.key;
        }

        public static Class<?> getType(String key) {

            Class<?> type = null;
            PropertyKey[] values = PropertyKey.values();
            for (PropertyKey propertyKey : values) {

                if (propertyKey.key.equals(key)) {

                    type = propertyKey.type;
                    break;
                }
            }

            if (type == null) {
                throw new RuntimeException("Unable to find ProopertyKey '" + key + "'");
            }

            return type;
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

    private Properties properties;

    public static synchronized PropertiesManager getInstance() {
        return instance;
    }

    private PropertiesManager() {
        this.properties = new Properties();
    }

    public void initProperties(String[] parameters) throws FileNotFoundException, IOException {

        System.out.println("Initializing properties:");

        this.properties.clear();
        this.properties.load(new FileInputStream(parameters[1]));

        for (int i = 2; i < parameters.length;) {

            String key = parameters[i++].substring(1);
            String value = parameters[i++];
            this.properties.setProperty(key, value);
        }

        for (Object key : this.properties.keySet()) {
            System.out.println("  " + key + "=" + this.properties.get(key));
        }

        this.validateInput();
    }

    public void initProperties(String inputFile) throws FileNotFoundException, IOException {

        System.out.println("Initializing properties from file " + inputFile + ":");

        this.properties.clear();
        this.properties.load(new FileInputStream(inputFile));

        for (Object key : this.properties.keySet()) {
            System.out.println("  " + key + "=" + this.properties.get(key));
        }

        this.validateInput();
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

    public Object getValue(String key) {
        return this.properties.getProperty(key);
    }

    public int getValueAsInt(String key) {
        return Integer.parseInt(this.properties.getProperty(key));
    }

    public boolean getValueAsBoolean(String key) {
        return Boolean.parseBoolean(this.properties.getProperty(key));
    }

    public void setValue(String key, String value) {

        this.properties.setProperty(key, value);
        System.out.println(key + "=" + this.properties.get(key));
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {

        PropertiesManager.getInstance().initProperties(
                new String[] { "D:/cep/GoogleEarth/src/resources/properties/input.properties" });
    }
}
