package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.Date;

public class QAStepRowSource implements RowSource {

    private QAStep source;
    
    private QAStepResult qaStepResult;

    public QAStepRowSource(QAStep source, QAStepResult qaStepResult) {
        this.source = source;
        this.qaStepResult = qaStepResult;
    }

    public Object[] values() {
        String comments = source.getComments();
        if (comments != null && comments.length() > 50)
            comments = comments.substring(0, 45) + "  ...";

        return new Object[] { source.getName(), new Integer(source.getVersion()), Boolean.valueOf(source.isRequired()),
                new Float(source.getOrder()), source.getStatus(), getStepResult(), format(source.getDate()), source.getWho(), comments,
                program(source.getProgram()), getShortenedProgramArguments(source.getProgramArguments()), 
                source.getConfiguration() };
    }
    
    private String getShortenedProgramArguments(String programArguments) {
        if (programArguments == null)
           return programArguments;
        else if (programArguments.length() > 70)
           return programArguments.substring(0, 70);   
        else 
           return programArguments;
    }

    private String getStepResult(){
        if (qaStepResult ==null) return "";
        return qaStepResult.getTableCreationStatus();
    }

    private String program(QAProgram program) {
        return (program != null) ? program.getName() : "";
    }

    private Object format(Date date) {
        return date == null ? "N/A" : CustomDateFormat.format_YYYY_MM_DD_HH_MM(date);
    }

    public Object source() {
        return source;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}