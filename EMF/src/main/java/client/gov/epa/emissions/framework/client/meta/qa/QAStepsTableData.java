package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class QAStepsTableData extends AbstractTableData {

    private List rows;

    private QAStep[] values;
    
    private QAStepResult[] results; 

    public QAStepsTableData(QAStep[] values, QAStepResult[] results) {
        this.values = values;
        this.results = results; 
        this.rows = createRows();
    }

    public String[] columns() {
        return new String[] { "Name", "Version", "Required", "Order", "QA Status", "Run Status", "When", "Who", "Comment", "Program",
                "Arguments", "Configuration" };
    }

    public Class getColumnClass(int col) {
        if (col == 2)
            return Boolean.class;

        if (col == 6)
            return User.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    private List createRows() {
        List rows = new ArrayList();
        
        for (int i = 0; i < values.length; i++){
            QAStepResult result = findMatchedResult(values[i].getId()); 
            rows.add(row(values[i], result));
        }
        return rows;
    }
    
    private QAStepResult findMatchedResult(int stepId){
        if ( results==null || results.length ==0) return null;
        for (int i=0; i<results.length; i++){
            if (results[i].getQaStepId() == stepId)
                return results[i];
        }
        return null; 
    }

    private Row row(QAStep step, QAStepResult result) {
        return new ViewableRow(new QAStepRowSource(step, result));
    }

    public boolean isEditable(int col) {
        return false;
    }

    public QAStep[] getValues() {
        return values;
    }

}
