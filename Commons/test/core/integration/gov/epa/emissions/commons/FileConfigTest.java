package gov.epa.emissions.commons;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.configuration.ConfigurationException;

public class FileConfigTest extends TestCase {

    private FileConfig config;

    protected void setUp() throws ConfigurationException {
        config = new FileConfig("test/core/integration/gov/epa/emissions/commons/config-test.conf") {
            public String driver() {
                return null;
            }

            public String url() {
                return null;
            }

            public String username() {
                return null;
            }

            public String password() {
                return null;
            }
        };
    }

    public void testShouldLoadProperties() throws Exception {
        assertEquals("EMF", config.value("database.name"));
    }

    public void testShouldReturnProperties() throws Exception {
        Properties p = config.properties();
        assertTrue(p.size() > 5);
    }
}
