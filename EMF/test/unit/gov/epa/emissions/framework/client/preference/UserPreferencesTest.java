package gov.epa.emissions.framework.client.preference;

import java.util.Properties;

import junit.framework.TestCase;

public class UserPreferencesTest extends TestCase {

    public void testFetchLocalInputFolder() {
        CommentedProperties props = new CommentedProperties();
        props.put("local.input.drive", "d:\\");
        props.put("default.input.folder", "emf_input");

        UserPreference pref = new DefaultUserPreferences(props);

        assertEquals("d:\\emf_input", pref.inputFolder());
    }

    public void testFetchLocalOutputFolder() {
        CommentedProperties props = new CommentedProperties();
        props.put("local.output.drive", "d:\\");
        props.put("default.output.folder", "emf_output");

        UserPreference pref = new DefaultUserPreferences(props);

        assertEquals("d:\\emf_output", pref.outputFolder());
    }

}
