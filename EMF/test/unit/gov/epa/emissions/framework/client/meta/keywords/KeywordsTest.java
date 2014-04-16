package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.services.data.Keywords;
import junit.framework.TestCase;

public class KeywordsTest extends TestCase {

    public void testShouldFindExistingKeyword() {
        Keyword[] keywordsList = { new Keyword("1"), new Keyword("2") };
        Keywords keywords = new Keywords(keywordsList);

        assertTrue("Should contain keyword with name - '1'", keywords.contains("1"));
    }
}
