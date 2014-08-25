package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.data.DoubleRenderer;
import gov.epa.emissions.framework.client.data.PaginationPanel;
import gov.epa.emissions.framework.client.data.viewer.TablePresenter;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class EditorPanel extends JPanel implements EditorPanelView {

    private JPanel pageContainer;

    private PaginationPanel paginationPanel;

    private EmfDataset dataset;
    
    private Version version;

    private EditablePage editablePage;

    private MessagePanel messagePanel;

    private EditablePagePanel editablePagePanel;

    private DataSortFilterPanelEditor sortFilterPanel;
    
    private ManageChangeables changeablesList;

    private TableMetadata tableMetadata;
    
    private TablePresenter tablePresenter;

    private DesktopManager desktopManager;

    private EmfSession emfSession;

    private DoubleRenderer doubleRenderer; 

    private Page page;
    
    private DataEditor dataEditor;

    public EditorPanel(EmfDataset dataset, Version version, TableMetadata tableMetadata, 
            MessagePanel messagePanel, ManageChangeables changeablesList, DataEditor dataEditor) {
        super(new BorderLayout());
        super.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        this.doubleRenderer = new DoubleRenderer(); 
        this.doubleRenderer.setGroup(true);
        this.doubleRenderer.setDecimalPlaces(4);

        this.dataset = dataset;
        this.version = version;
        this.tableMetadata = tableMetadata;
        this.messagePanel = messagePanel;
        this.changeablesList = changeablesList;
        this.dataEditor = dataEditor;
        doLayout(messagePanel);
    }

    private void doLayout(MessagePanel messagePanel) {
        super.add(topPanel(messagePanel), BorderLayout.PAGE_START);
        
        pageContainer = new JPanel(new BorderLayout());
        super.add(pageContainer, BorderLayout.CENTER);
    }

    private JPanel topPanel(MessagePanel messagePanel) {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(sortFilterPanel(messagePanel), BorderLayout.CENTER);
            panel.add(paginationPanel(messagePanel), BorderLayout.EAST);
    
            return panel;
        }

    private JPanel paginationPanel(MessagePanel messagePanel) {
            JPanel panel = new JPanel();
        paginationPanel = new PaginationPanel(messagePanel);
            paginationPanel.revalidate();
            panel.add(paginationPanel);

        return panel;
    }

    private DataSortFilterPanelEditor sortFilterPanel(MessagePanel messagePanel) {
        sortFilterPanel = new DataSortFilterPanelEditor(messagePanel, dataset, "", this.doubleRenderer);
        return sortFilterPanel;
    }

    public void observe(TablePresenter presenter) {
            paginationPanel.observe(presenter);
            sortFilterPanel.observe(presenter);
            paginationPanel.init();
            sortFilterPanel.init();
        tablePresenter = presenter;
    }

    public void display(Page page) {
        if (page != null) {
            this.page = page;
        }
    //        paginationPanel.init(page);
    //        sortFilterPanel.init();

        pageContainer.removeAll();
        paginationPanel.updateStatus(this.page);
        pageContainer.add(createEditablePage(this.page), BorderLayout.CENTER);
        pageContainer.validate();
    }

    private EditablePagePanel createEditablePage(Page page) {
        if ( editablePagePanel == null) {
            editablePage = new EditablePage(dataset.getId(), version, page, tableMetadata);
            editablePage.setDatasetName(dataset.getName());

            editablePagePanel = new EditablePagePanel(editablePage, paginationPanel, messagePanel, changeablesList,
                    this.doubleRenderer, this.dataEditor);
            editablePagePanel.setDesktopManager(desktopManager);
            editablePagePanel.setEmfSession(emfSession);
            editablePagePanel.setRowFilter(sortFilterPanel.getRowFilter());
            editablePagePanel.setSortOrder(sortFilterPanel.getSortOrder());
            editablePagePanel.setTablePresenter(tablePresenter);
        }
        else {
            editablePage = new EditablePage(dataset.getId(), version, page, tableMetadata);
            editablePage.setDatasetName(dataset.getName());
            editablePagePanel.clearCopied();
            editablePagePanel.clearCopiedRecords();
            editablePagePanel.refresh(editablePage);
        }

        return editablePagePanel;
    }


    public ChangeSet changeset() {
        // if not initialized, no changes
        return editablePage != null ? editablePage.changeset() : new ChangeSet();
    }

    public void updateFilteredRecordsCount(int filtered) {
        paginationPanel.updateFilteredRecordsCount(filtered);
    }

    public void scrollToPageEnd() {
        editablePagePanel.scrollToPageEnd();
    }
    
    public TableMetadata tableMetadata() {
        return tableMetadata;
    }

    public String getRowFilter() {
            return sortFilterPanel.getRowFilter().getText();
    }
    
    public DesktopManager getDesktopManager() {
        return this.desktopManager;
    }
    
    public void setDesktopManager(DesktopManager desktopManager) {
        this.desktopManager = desktopManager;
    }

    public void setEmfSession(EmfSession emfSession) {
        this.emfSession = emfSession;
    }
    
    @Override
    public void clear() {
        //onlu do if page already exists, if the page doesn't exist then table isn't built yet
        if (page != null) {
            page.setRecords(new VersionedRecord[] {});
            editablePage = new EditablePage(dataset.getId(), version, page, tableMetadata);
            editablePage.setDatasetName(dataset.getName());
            editablePagePanel.clearCopied();
            editablePagePanel.clearCopiedRecords();
            editablePagePanel.refresh(editablePage);
        }
    }
}
