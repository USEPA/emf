package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.ui.RowSource;

public class EditableQAStepTemplateRowSource implements RowSource {

    private QAStepTemplate source;

    private Boolean selected = Boolean.FALSE;

    public EditableQAStepTemplateRowSource(QAStepTemplate source) {
        if (source == null) throw new IllegalArgumentException("QA template source is null");
        this.source = source;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, source.getName(), source.getProgram(), 
                shortenProgramArguments(source.getProgramArguments()),
                Boolean.valueOf(source.isRequired()), Float.valueOf(source.getOrder()) };
    }
    
    private String shortenProgramArguments(String arguments)
    {
        if (arguments == null) return null;
        else if (arguments.length() > 50) return arguments.substring(0, 50);
        return arguments;
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
        case 1:
            source.setName((String) val);
            break;
        case 2:
            source.setProgram((QAProgram) val);
            break;
        case 3:
            source.setProgramArguments((String) val);
            break;
        case 4:
            source.setRequired(((Boolean) val).booleanValue());
            break;
        case 5:
            source.setOrder(((Float) val).floatValue());
            break;
        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    public Object source() {
        return source;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }

    public void validate(int rowNumber) {
        // NOTE no validation needed

    }

}
