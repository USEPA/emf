package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class ViewableKeywordsTableData extends AbstractTableData {
    private List rows;

    public ViewableKeywordsTableData(Keyword[] keywordsList) {
        this.rows = createRows(keywordsList);
    }

    public String[] columns() {
        return new String[] { "Keyword" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    void add(String keyword) {
        rows.add(row(new Keyword(keyword)));
    }

    private List createRows(Keyword[] keywordsList) {
        List rows = new ArrayList();
        for (int i = 0; i < keywordsList.length; i++)
            rows.add(row(keywordsList[i]));

        return rows;
    }

    private ViewableRow row(Keyword keyword) {
        return new ViewableRow(keyword, new Object[] { keyword.getName() });
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

}
