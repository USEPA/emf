package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.YesNoDialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

public class DataFindReplaceAdvancedTab extends JPanel implements DataFindReplaceView {

    private SingleLineMessagePanel messagePanel;

    private FindReplaceViewPresenter presenter;

    private TextArea filter;
    
    private JLabel filterLabel;

    private TextArea replaceWith;
    
    private String table;
    
    private Version version;

    private JTextArea sortOrder;
    
    private ManageChangeables listOfChangeables;

    private JLabel filterFromParentWindow;

    private DataFindReplaceWindow dataFindReplaceWindow;

    public DataFindReplaceAdvancedTab(String table, Version version, JLabel filterFromParentWindow, 
            JTextArea sortOrder, ManageChangeables listOfChangeables, SingleLineMessagePanel messagePanel,
            DataFindReplaceWindow dataFindReplaceWindow){
        super.setName("Advanced");
        this.table = table;
        this.version = version;
        this.filterFromParentWindow = filterFromParentWindow;
        this.filterLabel = new JLabel(filterFromParentWindow.getText());
        this.listOfChangeables = listOfChangeables;
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
//                0, 0, Font.decode(""), Color.BLUE));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Row Filter ", filterLabel, panel);
         
        filter = new TextArea("filterValue", "", 40,3);
        filter.setToolTipText("<html>SQL Filter"
                + "<br/><br/>This is a SQL WHERE clause that is used to filter the current dataset."
                + "<br/>The expressions in the WHERE clause must contain valid column(s). "
                + "<br/><br/>Sample SQL Filter:"
                + "<br/><br/>For example to filter on a certain state and scc codes,<br/>substring(fips,1,2) = '37' and SCC in ('10100202','10100203')<br/>or<br/>fips like '37%' and  and SCC like '101002%'</html>");
        ScrollableComponent scrollableFilter = ScrollableComponent.createWithVerticalScrollBar(this.filter);
        scrollableFilter.setPreferredSize(new Dimension(350, 105));
        layoutGenerator.addLabelWidgetPair("SQL Filter ", scrollableFilter, panel);

        //        ScrollableComponent scrollableFilter = ScrollableComponent.createWithVerticalScrollBar(this.filter);
//        scrollableFilter.setPreferredSize(new Dimension(300, 105));
//        layoutGenerator.addLabelWidgetPair("SQL Filter  ", scrollableFilter, panel);

        replaceWith = new TextArea("setValues", "", 40,3);
        replaceWith.setToolTipText("<html>Set Values"
                + "<br/><br/>This is a SQL UPDATE SET clause that is used to set column(s) values of the current dataset. " 
                + "<br/>The expressions in the UPDATE SET clause must contain valid column(s) and separated by ','"
                + "<br/><br/>Sample SQL UPDATE SET Statement:"
                + "<br/><br/>stack_flow_rate = 1.25 * stack_flow_rate, <br/>ann_emis = 0.5 * ann_emis <html>" );
        
        ScrollableComponent scrollableSet = ScrollableComponent.createWithVerticalScrollBar(this.replaceWith);
        scrollableSet.setPreferredSize(new Dimension(350, 105));
        layoutGenerator.addLabelWidgetPair("UPDATE SET SQL  ", scrollableSet, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }
    
    public void apply() throws EmfException {
        
        clearMsgPanel();
        String message = "<html> You are about to change all records match the filter, "
              + "<br/> are you sure you want to do this? Y/N ?<html>";
        YesNoDialog dialog = new YesNoDialog(this, "Warning", message);
        if (dialog.confirm()) {
            if (!validateFields())
                return;

            final String findFilter = (filter.getText() == null) ?  "": filter.getText().trim();
            final String setString = (replaceWith.getText() == null) ? "" : replaceWith.getText().trim();
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
                    presenter.replaceValues(table, findFilter, setString, version, rowFilter);
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
                        setMsg("Successfully replaced column values.");
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
    }

    public void observe(FindReplaceViewPresenter presenter) {
        this.presenter = presenter;
    }

    private boolean validateFields() throws EmfException {
        String rowFilterString = (filterLabel.getText().equals("NO FILTER"))? "": filterLabel.getText();
        String filterString = (filter.getText() == null) ?  "": filter.getText();
        String replaceString = (replaceWith.getText() == null) ? "" : replaceWith.getText().trim();
        
        if (filterString.trim().isEmpty() && rowFilterString.trim().isEmpty())
            throw new EmfException("Please specify the filter value.");
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
        boolean sqlfilter = filter.getText().trim().equals("");
        ((DataEditor)listOfChangeables).setHasReplacedValues(true);
        ((DataEditor)listOfChangeables).append2WhatField("Replaced expressions, '" + replaceWith.getText() + "' "
                + (nofilter ? "" : " using filter, '" + filterLabel.getText() + "'" )
                + ( nofilter && sqlfilter ? "": " and ")
                + (sqlfilter ? "": "'" + filter.getText() + "'"));
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

    