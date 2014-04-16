package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.ui.RowSource;

public class KeywordRowSource implements RowSource {

    private Boolean selected;

    private Keyword keyword;

    private Keywords masterKeywords;

    public KeywordRowSource(Keyword keyword, Keywords masterKeywords) {
        this.keyword = keyword;
        this.masterKeywords = masterKeywords;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, keyword.getName() };
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
        case 1:
            keyword = masterKeywords.get((String) val);
            break;
        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    public Object source() {
        return keyword;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }

    public void validate(int rowNumber) throws EmfException {
        if (keyword == null || keyword.getName().trim().length() == 0) {
            throw new EmfException("empty keyword at row " + rowNumber);
        }
    }
}