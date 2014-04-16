package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

public class DataFindReplaceBasicTab extends JPanel implements DataFindReplaceView{
    
    private String[] cols = new String[0];

    private SingleLineMessagePanel messagePanel;

    private FindReplaceViewPresenter presenter;
    
    private ComboBox columnNames;

    private TextField find;

    private TextField replaceWith;

    private JLabel filterLabel;
    
    private String table;
    
    private Version version;

    private JTextArea sortOrder;
    
    private ManageChangeables listOfChangeables;
    
    private JLabel filterFromParentWindow;

    private DataFindReplaceWindow dataFindReplaceWindow;

    public DataFindReplaceBasicTab(String table, Version version, JLabel filterFromParentWindow, 
            JTextArea sortOrder, String[] cols, ManageChangeables listOfChangeables, SingleLineMessagePanel messagePanel, DataFindReplaceWindow dataFindReplaceWindow){
        super.setName("Basic");
        this.cols = cols;
        this.table = table;
        this.version = version;
        this.listOfChangeables = listOfChangeables;
        this.filterFromParentWindow = filterFromParentWindow;
        this.filterLabel = new JLabel(filterFromParentWindow.getText());
        this.sortOrder = sortOrder;
        this.messagePanel = messagePanel;
        this.dataFindReplaceWindow = dataFindReplaceWindow;
    }
    
    public void display(){
        this.filterLabel.setText(filterFromParentWindow.getText());
        this.filterLabel.validate();
        setLayout();
    }

    private void setLayout() {
        super.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        panel.add(selectionPanel());
        super.add(panel, BorderLayout.CENTER);
    }
    
    private JPanel selectionPanel() {
        JPanel panel = new JPanel(new SpringLayout());
//        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Make Selections",
//              0, 0, Font.decode(""), Color.BLUE));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
         
        layoutGenerator.addLabelWidgetPair("Row Filter ", filterLabel, panel);

        columnNames = new ComboBox("Select a column", this.cols);
        columnNames.setToolTipText("Select a column name to find and replace column values");
        layoutGenerator.addLabelWidgetPair("Column  ", columnNames, panel);

        find = new TextField("findColValue", 30);
        layoutGenerator.addLabelWidgetPair("Find  ", find, panel);

        replaceWith = new TextField("replaceColValuesWith", 30);
        layoutGenerator.addLabelWidgetPair("Replace with  ", replaceWith, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }
    
    public void apply() throws EmfException {
        clearMsgPanel();

        if (!validateFields())
            return;

        final String col = columnNames.getSelectedItem().toString();
        final String findString = (find.getText() == null) ?  "": find.getText().trim();
        final String replaceString = (replaceWith.getText() == null) ? "" : replaceWith.getText().trim();
        final String rowFilter = (filterLabel.getText().equals("NO FILTER")) ? "" : filterLabel.getText().trim();

        this.dataFindReplaceWindow.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); //turn on the wait cursor
        this.dataFindReplaceWindow.setOkButtonEnableState(false);
        setMsg("Note some find replace operations could take several minutes to finish");

        //long running methods.....
        
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        class FindReplaceTask extends SwingWorker<Void, Void> {
            
            private DataFindReplaceWindow parentContainer;

            public FindReplaceTask(DataFindReplaceWindow parentContainer) {
                this.parentContainer = parentContainer;
            }

            /*
             * Main task. Executed in background thread.
             * don't update gui here
             */
            @Override
            public Void doInBackground() throws EmfException  {
                presenter.replaceColValues(table, col, findString, replaceString, version, rowFilter);
                return null;
            }

            /*
             * Executed in event dispatching thread
             */
            @Override
            public void done() {
                try {
                    //make sure something didn't happen
                    get();
                    
                    presenter.applyConstraint(rowFilter, sortOrder.getText().trim());
                    resetDataeditorRevisionField();
                    setMsg("Successfully performed find and replace operation.");
                
                
                } catch (InterruptedException e1) {
                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
                    setErrorMsg(e1.getCause().getMessage());
                } catch (EmfException e2) {
                    setErrorMsg(e2.getMessage());
                } finally {
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                    this.parentContainer.setOkButtonEnableState(true);
                }
            }
        };
        new FindReplaceTask(this.dataFindReplaceWindow).execute();
        
    }
    
    public void observe(FindReplaceViewPresenter presenter) {
        this.presenter = presenter;
    }
    
    private boolean validateFields() throws EmfException {
        String findString = (find.getText() == null) ?  "": find.getText();
        String replaceString = (replaceWith.getText() == null) ? "" : replaceWith.getText().trim();
        
        if (columnNames.getSelectedItem() == null)
            throw new EmfException("Please select a valid column");
        if (findString.equals(replaceString))
            throw new EmfException("Please specify different find and replace values.");
        if (replaceString.isEmpty()){
            String message = "Replace field is empty, would you like to continue?";
            int selection = JOptionPane.showConfirmDialog( this, message, "Warning", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (selection == JOptionPane.NO_OPTION)   
                return false;
        }
        return true; 
    }
    
    private void resetDataeditorRevisionField() {
        boolean nofilter = filterLabel.getText().equals("NO FILTER");
        ((DataEditor)listOfChangeables).setHasReplacedValues(true);
        ((DataEditor)listOfChangeables).append2WhatField("Replaced '" + find.getText() + "' with '" + replaceWith.getText() + "' for column '" +
                columnNames.getSelectedItem().toString() + "' "+ (nofilter ? "" : " using filter '" + filterLabel.getText() + "'"));
    }
  
    private void clearMsgPanel() {
        messagePanel.clear();
    }
    
    public void setErrorMsg(String errorMsg) {
        messagePanel.setError(errorMsg);
    }

    private void setMsg(String msg) {
        messagePanel.setMessage(msg);
    }
    
}

    