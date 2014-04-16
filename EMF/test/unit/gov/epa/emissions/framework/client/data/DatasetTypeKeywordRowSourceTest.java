package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.client.data.dataset.KeywordRowSource;
import gov.epa.emissions.framework.services.data.Keywords;
import junit.framework.TestCase;

public class DatasetTypeKeywordRowSourceTest extends TestCase {

    public void testShouldCreateNewKeywordOnNewValue() {
        KeywordRowSource s = new KeywordRowSource(null, new Keywords(new Keyword[0]));

        s.setValueAt(1, "new");
        assertEquals("new", ((Keyword) s.source()).getName());
    }

    public void testShouldUseExistingKeywordOnUsingNameOfExistingKeyword() {
        Keyword[] keywords = { new Keyword("key1"), new Keyword("key2") };
        KeywordRowSource s = new KeywordRowSource(null, new Keywords(keywords));

        s.setValueAt(1, "key1");
        assertSame(keywords[0], s.source());
        
        s.setValueAt(1, "key1");
        assertSame(keywords[0], s.source());
        
        s.setValueAt(1, "KeY1");
        assertSame(keywords[0], s.source());
        
        s.setValueAt(1, " KeY1 ");
        assertSame(keywords[0], s.source());
        
        s.setValueAt(1, "key 1");
        assertNotSame(keywords[0], s.source());
    }
}
