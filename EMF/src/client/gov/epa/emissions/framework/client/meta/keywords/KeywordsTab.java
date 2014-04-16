package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypeKeyValueTableData;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;
import java.awt.Cursor;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class KeywordsTab extends JPanel implements KeywordsTabView, RefreshObserver {

    //private SingleLineMessagePanel messagePanel;
    private EmfDataset dataset;
    private KeywordsTabPresenter presenter;
    
    public KeywordsTab() {
        super.setName("viewKeywordsTab");
        //messagePanel = new SingleLineMessagePanel();
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(EmfDataset dataset, KeywordsTabPresenter presenter) {
        this.dataset = dataset;
        this.presenter = presenter; 
        
        createLayout();
    }
    
    private void createLayout(){
        //super.add(messagePanel);
        super.add(createDSTypeKeywordsPanel());
        super.add(createDSKeywordsPanel());
        
    }
    
    private JPanel createDSTypeKeywordsPanel() {
        KeyVal[] vals = dataset.getDatasetType().getKeyVals();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Keywords Specific to Dataset Type"));

        TableData tableData = new DatasetTypeKeyValueTableData(vals);
        JTable table = new JTable(new EmfTableModel(tableData));
        table.setRowHeight(16);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createDSKeywordsPanel() {
        KeyVal[] values = dataset.getKeyVals();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Keywords Specific to Dataset"));

        KeyValueTableData tableData = new KeyValueTableData(values);
        JTable table = new JTable(new EmfTableModel(tableData));
        table.setRowHeight(20);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);


        return panel;
    }

    public void doRefresh() throws EmfException {
        try {           
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            this.dataset = presenter.reloadDataset();
            super.removeAll();
            createLayout();
            super.validate();
            //messagePanel.setMessage("Finished loading keywords.");
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }    
    }

}
