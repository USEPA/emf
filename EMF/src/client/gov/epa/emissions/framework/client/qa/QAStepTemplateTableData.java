package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class QAStepTemplateTableData extends AbstractTableData {
    private List rows;

    public QAStepTemplateTableData(QAStepTemplate[] templates) {
        this.rows = createRows(templates);
    }

    public String[] columns() {
        return new String[] { "Name", "Program", "Arguments", "Required", "Order" };
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(QAStepTemplate[] templates) {
        List rows = new ArrayList();
        for (int i = 0; i < templates.length; i++)
            rows.add(row(templates[i]));

        return rows;
    }

    private ViewableRow row(QAStepTemplate template) {
        return new ViewableRow(template,
                new Object[] { template.getName(), template.getProgram(), 
                shortenProgramArguments(template.getProgramArguments()),
                        Boolean.valueOf(template.isRequired()), Float.valueOf(template.getOrder()) });
    }
    private String shortenProgramArguments(String arguments)
    {
        if (arguments == null) return null;
        else if (arguments.length() > 50) return arguments.substring(0, 50);
        return arguments;
    }

    public Class getColumnClass(int col) {
        if (col == 3) 
            return Boolean.class;
        if(col == 4)
            return Float.class;
        return String.class;
    }

}
