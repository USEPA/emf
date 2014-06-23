package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.client.data.dataset.KeywordRowSource;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.InlineEditableTableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KeywordsTableData extends AbstractEditableTableData implements InlineEditableTableData {
    private List rows;

    private Keywords masterKeywords;

    public KeywordsTableData(Keyword[] keywordsList, Keywords masterKeywords) {
        this.masterKeywords = masterKeywords;
        this.rows = createRows(keywordsList, masterKeywords);
    }

    public String[] columns() {
        return new String[] { "Select", "Keyword" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return true;
    }

    void add(String keyword) {
        rows.add(row(new Keyword(keyword), masterKeywords));
    }

    public Keyword[] sources() throws EmfException {
        List sources = sourcesList();
        return (Keyword[]) sources.toArray(new Keyword[0]);
    }

    public void addBlankRow() {
        add("");
    }

    public void removeSelected() {
        remove(getSelected());
    }

    private List createRows(Keyword[] keywordsList, Keywords masterKeywords) {
        List rows = new ArrayList();
        for (int i = 0; i < keywordsList.length; i++)
            rows.add(row(keywordsList[i], masterKeywords));

        return rows;
    }

    void remove(Keyword keyword) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            Keyword source = (Keyword) row.source();
            if (source == keyword) {
                rows.remove(row);
                return;
            }
        }
    }

    private EditableRow row(Keyword keyword, Keywords masterKeywords) {
        RowSource source = new KeywordRowSource(keyword, masterKeywords);
        return new EditableRow(source);
    }

    private Keyword[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            KeywordRowSource rowSource = (KeywordRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (Keyword[]) selected.toArray(new Keyword[0]);
    }

    private void remove(Keyword[] keywords) {
        for (int i = 0; i < keywords.length; i++)
            remove(keywords[i]);
    }

    private List sourcesList() throws EmfException {
        List sources = new ArrayList();
        int rowNumber = 0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            rowNumber++;
            EditableRow row = (EditableRow) iter.next();
            row.validate(rowNumber);
            sources.add(row.source());
        }
        return sources;
    }

    public Class getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;

        return String.class;
    }

}
