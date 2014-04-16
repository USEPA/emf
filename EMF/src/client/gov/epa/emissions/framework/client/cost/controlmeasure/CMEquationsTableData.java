package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.EquationType;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CMEquationsTableData extends AbstractEditableTableData {

    private List<EditableRow> rows;
    private boolean editable = true;
    
    public CMEquationsTableData(ControlMeasureEquation[] equations) {
        rows = createRows(equations);
    }

    private List<EditableRow> createRows(ControlMeasureEquation[] equations) {
        rows = new ArrayList<EditableRow>();
        for (ControlMeasureEquation equation : equations) {
            //add Pollutant
            EquationTypeVariable variable = new EquationTypeVariable("Pollutant");
            variable.setEquationType(equation.getEquationType());
            variable.setValue(equation.getPollutant().getName());
            rows.add(row(equation.getEquationType(), variable));
            //add Cost Year
            variable = new EquationTypeVariable("Cost Year");
            variable.setEquationType(equation.getEquationType());
            variable.setValue(equation.getCostYear() + "");
            rows.add(row(equation.getEquationType(), variable));
            EquationTypeVariable[] equationTypeVariables = equation.getEquationType().getEquationTypeVariables();
            for (EquationTypeVariable equationTypeVariable : equationTypeVariables) {
                equationTypeVariable.setEquationType(equation.getEquationType());
                if (equationTypeVariable.getFileColPosition() == 1) {
                    equationTypeVariable.setValue(equation.getValue1() != null ? equation.getValue1() + "" : "");
                } else if (equationTypeVariable.getFileColPosition() == 2) {
                    equationTypeVariable.setValue(equation.getValue2() != null ? equation.getValue2() + "" : "");
                } else if (equationTypeVariable.getFileColPosition() == 3) {
                    equationTypeVariable.setValue(equation.getValue3() != null ? equation.getValue3() + "" : "");
                } else if (equationTypeVariable.getFileColPosition() == 4) {
                    equationTypeVariable.setValue(equation.getValue4() != null ? equation.getValue4() + "" : "");
                } else if (equationTypeVariable.getFileColPosition() == 5) {
                    equationTypeVariable.setValue(equation.getValue5() != null ? equation.getValue5() + "" : "");
                } else if (equationTypeVariable.getFileColPosition() == 6) {
                    equationTypeVariable.setValue(equation.getValue6() != null ? equation.getValue6() + "" : "");
                } else if (equationTypeVariable.getFileColPosition() == 7) {
                    equationTypeVariable.setValue(equation.getValue7() != null ? equation.getValue7() + "" : "");
                } else if (equationTypeVariable.getFileColPosition() == 8) {
                    equationTypeVariable.setValue(equation.getValue8() != null ? equation.getValue8() + "" : "");
                } else if (equationTypeVariable.getFileColPosition() == 9) {
                    equationTypeVariable.setValue(equation.getValue9() != null ? equation.getValue9() + "" : "");
                } else if (equationTypeVariable.getFileColPosition() == 10) {
                    equationTypeVariable.setValue(equation.getValue10() != null ? equation.getValue10() + "" : "");
                } else if (equationTypeVariable.getFileColPosition() == 11) {
                    equationTypeVariable.setValue(equation.getValue11() != null ? equation.getValue11() + "" : "");
                }
                rows.add(row(equation.getEquationType(), equationTypeVariable));
            }
        }
        return rows;
    }

    private EditableRow row(EquationType equationType, EquationTypeVariable equationTypeVariable) {
        RowSource source = new EditableEquationVariableRowSource(equationType, equationTypeVariable);
        return new EditableRow(source);
    }

    public String[] columns() {
        return new String[] { "Equation Type", "Variable Name", "Value"};
    }

    public Class getColumnClass(int col) {
//        if (col==2)
//            return Double.class;
        return String.class;
    }

    public List rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        if (this.editable && col == 2)
            return true;
        return false;
    }

    public void setEditable(boolean editable) {
        this.editable  = editable;
    }

    public void refresh() {
       //
    }

    public EquationTypeVariable[] sources() {
        List<EquationTypeVariable> sources = sourcesList();
        return sources.toArray(new EquationTypeVariable[0]);
    }

    private List<EquationTypeVariable> sourcesList() {
        List<EquationTypeVariable> sources = new ArrayList<EquationTypeVariable>();
        int rowNumber = 0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            rowNumber++;
            EditableRow row = (EditableRow) iter.next();
            EditableEquationVariableRowSource rowSource = (EditableEquationVariableRowSource) row.rowSource();
            rowSource.validate(rowNumber);
            sources.add((EquationTypeVariable)rowSource.source());
        }
        return sources;
    }
//
//    private List sourcesList() {
//        List sources = new ArrayList();
//        for (Iterator iter = rows.iterator(); iter.hasNext();) {
//            ViewableRow row = (ViewableRow) iter.next();
//            sources.add(row.source());
//        }
//
//        return sources;
//    }

//    private void remove() {
//        for (Iterator iter = rows.iterator(); iter.hasNext();) {
//            ViewableRow row = (ViewableRow) iter.next();
//            List source = row.source();
//            if (source == record) {
//                rows.remove(row);
//                return;
//            }
//        }
//    }
//
//    public void remove(Scc[] records) {
//        for (int i = 0; i < records.length; i++)
//            remove(records[i]);
//
//        refresh();
//    }

}
