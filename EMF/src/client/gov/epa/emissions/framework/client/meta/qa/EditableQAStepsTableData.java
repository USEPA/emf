package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EditableQAStepsTableData extends AbstractTableData {

    private List rows;

    private QASteps steps;
    
    private List<QAStepResult> qaStepResults;

    public EditableQAStepsTableData(QAStep[] steps, QAStepResult[] qaStepResults) {
        this.steps = new QASteps(steps);
        this.qaStepResults = new ArrayList();
        this.qaStepResults.addAll(Arrays.asList(qaStepResults));
        this.rows = createRows(this.steps);
    }
    
    public EditableQAStepsTableData(QAStep[] steps) {
        this.steps = new QASteps(steps);
        this.qaStepResults = new ArrayList();
        this.rows = createRows(this.steps);
    }

    public void refresh() {
        this.rows = createRows(steps);
    }
    
    public void add(QAStep step, QAStepResult qaStepResult) {
        steps.filterDuplicates(new QAStep[]{step});
        QAStepResult result = findMatchedResult(step.getId());
        if (result != null )
            qaStepResults.remove(result);
        qaStepResults.add(qaStepResult);
    }

    public void add(QAStep step) {
        steps.add(step);
        rows.add(row(step, null));
    }

    public String[] columns() {
        return new String[] { "Name", "Version", "Required", "Order", "QA Status", "Run Status", "When", "Who", "Comment", "Program",
                "Arguments", "Configuration" };
    }

    public List rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        return (col == 0) ? true : false;
    }

    private List createRows(QASteps steps) {
        List rows = new ArrayList();
        for (int i = 0; i < steps.size(); i++)
            rows.add(row(steps.get(i), findMatchedResult(steps.get(i).getId())));

        return rows;
    }
    
    private QAStepResult findMatchedResult(int stepId){
        if ( qaStepResults == null || qaStepResults.size() ==0) return null;
        for (int i=0; i<qaStepResults.size(); i++){
            QAStepResult result=qaStepResults.get(i);
            if (result!=null && result.getQaStepId() == stepId)
                return result;
        }
        return null; 
    }


    private EditableRow row(QAStep step, QAStepResult qaStepResult ) {
        RowSource source = new QAStepRowSource(step, qaStepResult);
        return new EditableRow(source);
    }

    public Class getColumnClass(int col) {
        if (col == 2)
            return Boolean.class;

        if (col == 6)
            return User.class;

        return String.class;
    }

    public QAStep[] sources() {
        List sources = sourcesList();
        return (QAStep[]) sources.toArray(new QAStep[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            QAStepRowSource rowSource = (QAStepRowSource) row.rowSource();
            sources.add(rowSource.source());
        }
        return sources;
    }

}
