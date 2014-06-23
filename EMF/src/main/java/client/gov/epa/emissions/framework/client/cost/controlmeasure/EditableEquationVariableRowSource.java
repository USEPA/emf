package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.EquationType;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;
import gov.epa.emissions.framework.ui.RowSource;

public class EditableEquationVariableRowSource implements RowSource {

    private EquationTypeVariable variable;

    private EquationType equationType;

    private Boolean selected;
    
//    private final static Double NAN_VALUE = new Double(Double.NaN);
//
    public EditableEquationVariableRowSource(EquationType equationType, EquationTypeVariable variable) {
        this.equationType = equationType;
        this.variable = variable;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        Object[] values = {equationType.getName(), 
                variable != null ? variable.getName() : "NO VARIABLES", 
                variable.getValue()!= null ? variable.getValue() : "" };
        return values;
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
//        case 0:
//            selected = (Boolean) val;
//            break;
//        case 1:
//            inputDataset.setKeyword(keyword(val));
//            break;
        case 2:
            //maybe add some logic if the value is non-numeric...
            variable.setValue(val + "");
//            if (variable.getFileColPosition() > 0) {
//                variable.setValue(val);
//            } else {
//                if (variable.getName().equals("Cost Year")) {
//                    variable.setValue((Double) val);
//                }else if (variable.getName().equals("Pollutant")) {
//                    variable.setValue((Double) val);
//                }
//            }
            break;
        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    public Object source() {
        return variable;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }

    public void validate(int rowNumber) {
        //add code to validate value - make sure its a number, etc...
        //controlMeasureEquationTypeVariable.getValue();
        
        
        
//        Keyword keyword = inputDataset.getKeyword();
//        if (keyword == null || keyword.getName().trim().length() == 0) {
//            throw new EmfException("On Keywords panel, empty keyword at row "+rowNumber);
//        }
//        String value = source.getValue();
//        if (value == null || value.trim().length() == 0) {
//            throw new EmfException("On Keywords panel, empty keyword value at row "+rowNumber);
//        }
    }
}