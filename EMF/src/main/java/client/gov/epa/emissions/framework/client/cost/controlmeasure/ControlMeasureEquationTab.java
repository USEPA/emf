package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.Pollutants;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.EquationType;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

//import java.util.ArrayList;
//import java.util.List;

public class ControlMeasureEquationTab extends JPanel implements ControlMeasureTabView {

    private MessagePanel messagePanel;

//    private ControlMeasure measure;
    private ControlMeasurePresenter controlMeasurePresenter;
    private EmfConsole parent;
    private EmfSession session;
    
    private ControlMeasure measure;
    private Button addButton;
    private Button removeButton;
    private JPanel mainPanel;
    
    
    private EditableEmfTableModel tableModel;
    private CMEquationsTableData tableData;
    private ManageChangeables changeables;
    private Pollutants pollutants;
    private EquationType currentEqType;
    
    public ControlMeasureEquationTab(ControlMeasure measure, EmfSession session, ManageChangeables changeables,
            MessagePanel messagePanel, EmfConsole parent,
            ControlMeasurePresenter controlMeasurePresenter) throws EmfException{
        
        this.mainPanel = new JPanel(new BorderLayout());
        this.parent = parent;
       
        this.session = session; 
        this.messagePanel = messagePanel;
        this.measure = measure;
        this.changeables = changeables;
        this.controlMeasurePresenter = controlMeasurePresenter;
        this.pollutants = new Pollutants(controlMeasurePresenter.getPollutants());
        
        if (measure.getEquations() != null && measure.getEquations().length > 0)
            this.currentEqType = measure.getEquations()[0].getEquationType();
     
//       mainPanel = new JPanel(new BorderLayout());
        doLayout(measure);
        
        super.setName("CMEquation tab");
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
    }
 

    private void doLayout(ControlMeasure measure){
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        mainPanel.add(equationInfoPanel(), BorderLayout.NORTH);
        mainPanel.add(createTable(measure.getEquations()), BorderLayout.CENTER);
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(buttonPanel(), BorderLayout.SOUTH);
    }

    private JPanel equationInfoPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Equation Type:", new JLabel(), panel);
        layoutGenerator.addLabelWidgetPair("Name:", new JLabel(currentEqType == null ? "" : currentEqType.getName()), panel);
        layoutGenerator.addLabelWidgetPair("Description:", new JLabel(currentEqType == null ? "" : currentEqType.getDescription()), panel);
        layoutGenerator.addLabelWidgetPair("Inventory Fields:", new JLabel(currentEqType == null ? "" : currentEqType.getInventoryFields()), panel);
        layoutGenerator.addLabelWidgetPair("Equations:", getEquation(), panel);
        layoutGenerator.makeCompactGrid(panel, 5, 2, 10, 10, 5, 10);

        return panel;
    }


    private JComponent getEquation() {
        TextArea equation = new TextArea("equation", currentEqType == null ? "" : currentEqType.getEquation(), 56);
        equation.setEditable(false);
        equation.setOpaque(false);
        ScrollableComponent scrolpane = new ScrollableComponent(equation);
        
        return scrolpane;
    }


    private void updateMainPanel(ControlMeasureEquation[] equations){
        mainPanel.removeAll(); 
        mainPanel.add(equationInfoPanel(), BorderLayout.NORTH);
        mainPanel.add(createTable(equations), BorderLayout.CENTER);
        mainPanel.validate();

    }
    
    private JScrollPane createTable(ControlMeasureEquation[] cmEquations) {
        
        tableData = new CMEquationsTableData(cmEquations);
        tableModel = new EditableEmfTableModel(tableData);

        EditableTable table = new EditableTable(tableModel);
//        table.setColWidthsBasedOnColNames();
        changeables.addChangeable(table);
 //       table.disableScrolling();
 //       table.setRowHeight(20);
        return new JScrollPane(table);
    }
 
    
    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        addButton = new AddButton(addAction());
        panel.add(addButton);
        removeButton = new RemoveButton(removeAction());
        panel.add(removeButton);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.LINE_START);

        return container;
    }

    private Action addAction() {
        final Component dialogParent  = this;
        return new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if(tableData.rows().size()>0)  
                    messagePanel.setError("Please remove the old equation before adding a new one" );
                else {
                    EquationTypeSelectionView view = new EquationTypeSelectionDialog(parent, dialogParent, changeables);
                    EquationTypeSelectionPresenter presenter = new EquationTypeSelectionPresenter(view, session);
                    try {
                        presenter.display();
                        //get Equation Type...
                        EquationType equationType = presenter.getEquationType();
                        currentEqType = equationType;
    
                        if (equationType!=null){
                            ControlMeasureEquation equation = new ControlMeasureEquation(equationType);
                            equation.setPollutant(measure.getMajorPollutant());
                            equation.setCostYear(CostYearTable.REFERENCE_COST_YEAR);
                            updateMainPanel(new ControlMeasureEquation[] { equation });
                            tableModel.refresh(tableData);
                        }
                        
                        //messagePanel.setError(equationType.getName());
                        //wrap it with ControlMeasureEquationType...
    //                    cmEquationTypes
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        messagePanel.setError("Could not return equation type: " + e.getMessage());
                    }
                }
            }
        };
        
    }
    

//    private void addEequation() {
//        EquationType[] equationTypes=getEquationTypes();
//        EquationTypeSelectionView view = new  EquationTypeSelectionView(new EquationType[0]);
//        SectorChooser sectorSelector = new SectorChooser(allSectors, sectorsList, parent);
//        sectorSelector.display();
//        
//    }    

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
               
                try {
                    messagePanel.clear();
                    doRemove(); 
                    
                } catch (Exception e1) {
                    messagePanel.setError("Could not remove equation type");
                }
            

            }
        };
    }
    private void doRemove(){
 //       ControlMeasureEquation[] equations = new ControlMeasureEquation[]{};
        
        if (tableData.rows().size()==0)
            return; 
        String title = "Warning";
        String message = "Are you sure you want to remove the equation information?";
        int selection = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            if(tableData.rows().size()>0){
                this.currentEqType = null;
                ControlMeasureEquation[] cmEquations=new ControlMeasureEquation[]{};
                refresh(measure);
                updateMainPanel(cmEquations);
                tableModel.refresh(tableData);
                }
            }
    }
    
 
//    private void refreshTable() {
//
//        this.removeAll();
//        doLayout();
//        repaint();
//         
//
//    } 
//    here is an example of how to parse the new objects.
//
//    //get available equations...
//    EquationType[] equationTypes = session.controlMeasureService().getEquationTypes();
//    System.out.println(equationTypes.length);
//    for (int i = 0; i < equationTypes.length; i++) {
//        System.out.println(equationTypes[i].getName() + " " + equationTypes[i].getEquationTypeVariables().length);
//        for (int j = 0; j < equationTypes[i].getEquationTypeVariables().length; j++) {
//
//System.out.println(equationTypes[i].getEquationTypeVariables()[j].getName());
//        }
//    }
//
//    //get measure equations type information...
//    ControlMeasureEquationType[] controlMeasureEquationTypes = measure.getEquationTypes();
//    System.out.println("Number of Equations = " + controlMeasureEquationTypes.length);
//    for (int i = 0; i < controlMeasureEquationTypes.length; i++) {
//        System.out.println("Name = " + controlMeasureEquationTypes[i].getEquationType().getName() + ", Number of variables = " + controlMeasureEquationTypes[i].getControlMeasureEquationTypeVariables().length);
//        for (int j = 0; j < controlMeasureEquationTypes[i].getControlMeasureEquationTypeVariables().length; j++) {
//            System.out.println("Name of variable = " + controlMeasureEquationTypes[i].getControlMeasureEquationTypeVariables()[j].getEquationTypeVariable().getName());
//            System.out.println("Value of variable = " + controlMeasureEq                        for(int i=0; i<n; i++){
//    ControlMeasureEquationTypeVariable controlMeasureEquationTypeVariable = new ControlMeasureEquationTypeVariable(equationType.getEquationTypeVariables()[i], 0.0);
//    variables[i] = controlMeasureEquationTypeVariable;   
//}
//uationTypes[i].getControlMeasureEquationTypeVariables()[j].getValue());
//        } 

//    public void save(ControlMeasure measure) {
//        List sccsList = tableData.rows();
//        sccs = new Scc[sccsList.size()];
//        for (int i = 0; i < sccsList.size(); i++) {
//            ViewableRow row = (ViewableRow) sccsList.get(i);
//            Scc scc = (Scc) row.source();
//            sccs[i] = new Scc(scc.getCode(), "");
//        }
//    }

 
    public void modify() {
        controlMeasurePresenter.doModify();
    }

    public void viewOnly() {
        
        addButton.setVisible(false);
        removeButton.setVisible(false);
        this.tableData.setEditable(false);
    }




    public void refresh(ControlMeasure measure) {
        
//        measure.setEquationTypes(cmEquationTypes);
       
    }




    public void save(ControlMeasure measure) throws EmfException {
        List<ControlMeasureEquation> equationList = new ArrayList<ControlMeasureEquation>();
        for (EquationTypeVariable equationTypeVariable : tableData.sources()) {
            EquationType equationType = equationTypeVariable.getEquationType();
            ControlMeasureEquation equation = new ControlMeasureEquation(equationType);
            for (EquationTypeVariable equationTypeVariable2 : tableData.sources()) {
                if (equationTypeVariable2.getName().equals("Cost Year")) {
                    equation.setCostYear(Integer.parseInt((String)equationTypeVariable2.getValue()));
                } else if (equationTypeVariable2.getName().equals("Pollutant")) {
                    equation.setPollutant(pollutants.get((String)equationTypeVariable2.getValue()));
                }
            }

            int indexOfEquation = equationList.indexOf(equation);
            if (indexOfEquation == -1) 
                equationList.add(equation);
            else
                equation = equationList.get(indexOfEquation);
            String value = (String)equationTypeVariable.getValue();
            String variableName = equationTypeVariable.getName();
            if (equationTypeVariable.getFileColPosition() == 1) {
                equation.setValue1(getValue(variableName, value));
            } else if (equationTypeVariable.getFileColPosition() == 2) {
                equation.setValue2(getValue(variableName, value));
            } else if (equationTypeVariable.getFileColPosition() == 3) {
                equation.setValue3(getValue(variableName, value));
            } else if (equationTypeVariable.getFileColPosition() == 4) {
                equation.setValue4(getValue(variableName, value));
            } else if (equationTypeVariable.getFileColPosition() == 5) {
                equation.setValue5(getValue(variableName, value));
            } else if (equationTypeVariable.getFileColPosition() == 6) {
                equation.setValue6(getValue(variableName, value));
            } else if (equationTypeVariable.getFileColPosition() == 7) {
                equation.setValue7(getValue(variableName, value));
            } else if (equationTypeVariable.getFileColPosition() == 8) {
                equation.setValue8(getValue(variableName, value));
            } else if (equationTypeVariable.getFileColPosition() == 9) {
                equation.setValue9(getValue(variableName, value));
            } else if (equationTypeVariable.getFileColPosition() == 10) {
                equation.setValue10(getValue(variableName, value));
            } else if (equationTypeVariable.getFileColPosition() == 11) {
                equation.setValue11(getValue(variableName, value));
            }
//            if (indexOfEquation != 0) {
//                equationList.remove(indexOfEquation);
//            }
//            equationList.add(equation);
        }
        measure.setEquations(equationList.toArray(new ControlMeasureEquation[0]));
    }

    private Double getValue(String variableName, String value) throws EmfException {
        if (value == null) return null;

        //trim excess space
        value = value.trim();

        if (value.length() == 0) return null;

        //store as double
        Double dblValue = null;
        try {
            dblValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new EmfException(variableName + ", value must be a double number.");
        }
        return dblValue;
    }
}


