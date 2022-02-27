package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.datasettype.DatasetTypeKeyValueTableData;
import gov.epa.emissions.framework.client.swingworker.RefreshSwingWorkerTasks;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;
import java.awt.Cursor;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class EditableKeywordsTab extends JPanel implements EditableKeywordsTabView, Editor, RefreshObserver {

    private EditableKeyValueTableData tableData;
    
    private EditableKeywordsTabPresenter presenter;

    private EditableKeywordsPanel editableKeywordsPanel;

    private ManageChangeables changeablesList;

    private EmfConsole parent;
    
    private EmfDataset dataset;

    private MessagePanel messagePanel;

    public EditableKeywordsTab(ManageChangeables changeablesList, EmfConsole parent,
            MessagePanel messagePanel) {
        this.changeablesList = changeablesList;
        this.parent = parent;
        this.messagePanel = messagePanel;
        super.setName("editKeywordsTab");
        //super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(EmfDataset dataset, Keywords masterKeywords) {       
        this.dataset = dataset;
        //super.removeAll();
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //super.add(messagePanel);
        super.add(createDSTypeKeywordsPanel(dataset.getDatasetType().getKeyVals()));
        super.add(createDSKeywordsPanel(masterKeywords));
        //super.validate();
    }

    private JPanel createDSKeywordsPanel(Keywords masterKeywords) {
        tableData = new EditableKeyValueTableData(dataset.getKeyVals(), masterKeywords);
        editableKeywordsPanel = new EditableKeywordsPanel(null, tableData, masterKeywords, changeablesList, parent);
        editableKeywordsPanel.setBorder(BorderFactory.createTitledBorder("Keywords Specific to Dataset"));
        
        return editableKeywordsPanel;
    }

    private JPanel createDSTypeKeywordsPanel(KeyVal[] vals) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Keywords Specific to Dataset Type"));

        TableData tableData = new DatasetTypeKeyValueTableData(vals);
        JTable table = new JTable(new EmfTableModel(tableData));
        table.setRowHeight(16);
        table.getAccessibleContext().setAccessibleName("List of keywords specific to the dataset type used by this dataset");

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    public KeyVal[] updates() throws EmfException {
        return tableData.sources();
    }

    public void commit() {
        editableKeywordsPanel.commit();
    }
    
    public EmfDataset getDataset() {
        return this.dataset;
    }
    
    public void doRefresh(EmfDataset dataset, Keywords masterKeywords) {       
        this.dataset = dataset;
        super.removeAll();
        super.add(createDSTypeKeywordsPanel(dataset.getDatasetType().getKeyVals()));
        super.add(createDSKeywordsPanel(masterKeywords));
        super.validate();
    }

    public void doRefresh() throws EmfException {
        try {   
            new RefreshSwingWorkerTasks(this, messagePanel, presenter).execute();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
//            try {
//                presenter.checkIfLockedByCurrentUser();
//            } catch (Exception e) {
//                throw new EmfException(e.getMessage());
//            }
        }           
    }

    public void observe(EditableKeywordsTabPresenter presenter) {
        this.presenter = presenter;
        
    }

}
