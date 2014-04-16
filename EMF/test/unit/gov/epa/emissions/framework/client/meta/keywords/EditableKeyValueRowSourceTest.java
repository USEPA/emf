package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeyValueRowSource;
import gov.epa.emissions.framework.services.data.Keywords;

import org.jmock.cglib.MockObjectTestCase;

public class EditableKeyValueRowSourceTest extends MockObjectTestCase {

    public void testShouldSetNewKeywordOnKeyValueIfKeywordDoesNotExist() {
        Keyword[] keywords = new Keyword[0];
        KeyVal keyval = new KeyVal();
        EditableKeyValueRowSource source = new EditableKeyValueRowSource(keyval, new Keywords(keywords));

        source.setValueAt(1, "new-key");

        KeyVal result = (KeyVal) source.source();
        assertEquals("new-key", result.getKeyword().getName());
    }

    public void testShouldSetExistingKeywordOnKeyValueIfKeywordDoesExist() {
        Keyword[] keywords = { new Keyword("1"), new Keyword("2") };
        KeyVal keyval = new KeyVal();
        EditableKeyValueRowSource source = new EditableKeyValueRowSource(keyval, new Keywords(keywords));

        source.setValueAt(1, "1");

        KeyVal result = (KeyVal) source.source();
        assertSame(keywords[0], result.getKeyword());
    }

    public void testShouldReturnKeywordNameAsColumnTwo() {
        KeyVal keyval = new KeyVal();
        Keyword keyword = new Keyword("1");
        keyval.setKeyword(keyword);
        EditableKeyValueRowSource source = new EditableKeyValueRowSource(keyval, null);

        assertSame(keyword.getName(), source.values()[1]);
    }
}
