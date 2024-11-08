package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.commons.gui.buttons.ImportButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.exim.DatasetsBrowserAwareImportPresenter;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportPresenterImpl;
import gov.epa.emissions.framework.client.exim.ExportWindow;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditor;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataWindow;
import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfDatasetTableData;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class DatasetsBrowserWindow extends DisposableInteralFrame implements DatasetsBrowserView, RefreshObserver {

    private MessagePanel messagePanel;

    private DatasetsBrowserPresenter presenter;

    private EmfConsole parentConsole;

    private EmfSession session;

    private ComboBox dsTypesBox;
    private DatasetType dsTypeLoaded;
    
    private TextField textFilter;

    private DatasetType[] allDSTypes = new DatasetType[] {};

    private SelectableSortFilterWrapper table;
    private Button removeButton;
    
    public DatasetsBrowserWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Dataset Manager", new Dimension(850, 450), desktopManager);
        super.setName("datasetsBrowser");
        this.session = session;
        this.parentConsole = parentConsole;
    }

    public void display(EmfDataset[] datasets) throws EmfException {
//        getAllDSTypes();
        createDSTypesComboBox();
        JPanel panel = createLayout(datasets);
        Container contentPane = getContentPane();
        contentPane.add(panel);
        messagePanel.setMessage("Fetching dataset types...");
        super.display();
        populate();
    }

    private void getAllDSTypes() throws EmfException {
        List<DatasetType> dbDSTypes = new ArrayList<DatasetType>();
        dbDSTypes.add(new DatasetType("All"));
        dbDSTypes.addAll(Arrays.asList(presenter.getDSTypes()));
        this.allDSTypes = dbDSTypes.toArray(new DatasetType[0]);
    }
    
    private void typeAction() {
        DatasetType type = getSelectedDSType();
        try {
            // count the number of datasets and do refresh
            if (dsTypesBox.getSelectedIndex() > 0)
                doRefresh();          
        } catch (EmfException e1) {
            messagePanel.setError("Could not retrieve all datasets for dataset type " + type.getName());
        }
    }    

    private void createDSTypesComboBox() {
        dsTypesBox = new ComboBox("Select one", allDSTypes, "Dataset Types");
        dsTypesBox.setPreferredSize(new Dimension(360, 25));
        dsTypesBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!dsTypesBox.isPopupVisible() &&
                        getSelectedDSType() != dsTypeLoaded) {
                    typeAction();
                }
            }
        });
        dsTypesBox.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (getSelectedDSType() != dsTypeLoaded) typeAction();
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                //
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
                //
            }
        });
    }

    public DatasetType getSelectedDSType() {
        Object selected = dsTypesBox.getSelectedItem();

        if (selected == null)
            return new DatasetType("Select one");

        return (DatasetType) selected;
    }
    
    public void setDSTypeSelection(int index) {
        dsTypesBox.setSelectedIndex(index);
    }

    private JPanel createLayout(EmfDataset[] datasets) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(createTopPanel(), BorderLayout.NORTH);
        panel.add(createBrowserPanel(datasets), BorderLayout.CENTER);
        panel.add(createControlPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBrowserPanel(EmfDataset[] datasets) {
        EmfDatasetTableData tableData = new EmfDatasetTableData(datasets);
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        table.getTable().getAccessibleContext().setAccessibleName("List of datasets");

        JPanel browserPanel = new JPanel(new BorderLayout());
        browserPanel.add(table);

        return browserPanel;
    }

    private SortCriteria sortCriteria() {
        String[] columnNames = { "Last Modified Date" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true });
    }

    private JPanel createTopPanel() {
        JPanel msgRefreshPanel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        msgRefreshPanel.add(messagePanel, BorderLayout.CENTER);
        Button button = new RefreshButton(this, "Refresh Datasets", messagePanel);
        msgRefreshPanel.add(button, BorderLayout.EAST);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        textFilter = new TextField("textfilter", 10, "Name contains text filter");
        textFilter.setEditable(true);
        textFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                typeAction();
            }
        });
        
        Button advButton = new Button("Advanced", new AbstractAction(){
            public void actionPerformed(ActionEvent arg0) {
                advancedSearch();
                notifyAdvancedSearch();
            }
        });
        advButton.setMnemonic(KeyEvent.VK_D);
        advButton.setToolTipText("Advanced search");
        
        JPanel advPanel = new JPanel(new BorderLayout(5, 2));
        JLabel jlabel = new JLabel("Name Contains:");
        jlabel.setHorizontalAlignment(JLabel.RIGHT);
        jlabel.setLabelFor(textFilter);
        advPanel.add(jlabel, BorderLayout.WEST);
        advPanel.add(textFilter, BorderLayout.CENTER);
        advPanel.add(advButton, BorderLayout.EAST);
        advPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
        
        topPanel.add(getDSTypePanel("Show Datasets of Type:", dsTypesBox), BorderLayout.LINE_START);
        topPanel.add(advPanel, BorderLayout.CENTER);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(msgRefreshPanel);
        panel.add(topPanel);

        return panel;
    }



    public void notifyAdvancedSearch() {
        if (textFilter != null) {
            textFilter.setText("");
            textFilter.setEnabled(false);
        }
    }
    
    public void notifyAdvancedSearchOff() {
        if (textFilter != null) {
            textFilter.setEnabled(true);
        }
    }

    private void advancedSearch() {
        DatasetSearchWindow view = new DatasetSearchWindow("Advanced Dataset Search", parentConsole, desktopManager, getSelectedDSType());
        
        if (textFilter != null && textFilter.getText() != null && !textFilter.getText().trim().isEmpty())
            view.setNameText(textFilter.getText().trim());
        
        view.observe(presenter);
        view.display();
    }

    private JPanel getDSTypePanel(String label, JComboBox box) {
        JPanel panel = new JPanel(new BorderLayout(5, 2));
        JLabel jlabel = new JLabel(label);
        jlabel.setHorizontalAlignment(JLabel.RIGHT);
        jlabel.setLabelFor(box);
        panel.add(jlabel, BorderLayout.WEST);
        panel.add(box, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 10));

        return panel;
    }
    
//    private JPanel nameContains(String label, TextField textFilter) {
//        JPanel panel = new JPanel(new BorderLayout(5, 2));
//        JLabel jlabel = new JLabel(label);
//        jlabel.setHorizontalAlignment(JLabel.RIGHT);
//        panel.add(jlabel, BorderLayout.WEST);
//        panel.add(textFilter, BorderLayout.CENTER);
//        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//
//        return panel;
//    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.add(createRightControlPanel(), BorderLayout.LINE_END);
        controlPanel.add(createLeftControlPanel(), BorderLayout.LINE_START);

        return controlPanel;
    }

    private JPanel createLeftControlPanel() {
        JPanel panel = new JPanel();

        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);
        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction(), table, confirmDialog);
        SelectAwareButton propButton = new SelectAwareButton("Edit Properties", editPropAction(), table, confirmDialog);
        SelectAwareButton dataButton = new SelectAwareButton("Edit Data", editDataAction(), table, confirmDialog);
        removeButton = new RemoveButton(removeAction());

        viewButton.setToolTipText("View dataset properties window");
        propButton.setToolTipText("Edit dataset properties window");
        dataButton.setToolTipText("Edit dataset data window");
        removeButton.setToolTipText("Remove datasets action");

        dataButton.setMnemonic(KeyEvent.VK_A);

        panel.add(viewButton);
        panel.add(propButton);
        panel.add(dataButton);
        panel.add(removeButton);

        return panel;
    }

    private Action viewAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    doDisplayPropertiesViewer();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    private Action editPropAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMessage();
                doDisplayPropertiesEditor();
            }
        };
    }

    private Action editDataAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    doDisplayVersionedData();
                } catch (EmfException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doRemove();
            }
        };
    }

    protected void importDataset() throws EmfException {
        ImportWindow importView = new ImportWindow(session, session.dataCommonsService(), desktopManager, parentConsole,
                getSelectedDSType());

        ImportPresenter importPresenter = new DatasetsBrowserAwareImportPresenter(session, session.user(), session
                    .eximService(), session.dataService(), this);

        presenter.doImport(importView, importPresenter);
    }

    private JPanel createRightControlPanel() {
        JPanel panel = new JPanel();

        Button importButton = new ImportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    importDataset();
                } catch (EmfException e) {
                    showError("Could not open Import window (for creation of a new dataset)");
                }
            }
        });
        importButton.setToolTipText("Import a new dataset");
        panel.add(importButton);

        Button exportButton = new ExportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                exportSelectedDatasets();
            }
        });
        exportButton.setToolTipText("Export existing dataset(s)");
        panel.add(exportButton);

        Button purgeButton = new Button("Purge", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    purgeDelectedDatasets();
                } catch (EmfException e) {
                    if (e.getMessage().length() > 100)
                        messagePanel.setMessage(e.getMessage().substring(0, 100) + "...");
                    else
                        messagePanel.setMessage(e.getMessage());
                }
            }
        });
        purgeButton.setToolTipText("Purge deleted dataset(s)");
        purgeButton.setMnemonic(KeyEvent.VK_P);
        panel.add(purgeButton);

        Button closeButton = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        closeButton.setToolTipText("Close dataset manager");
        panel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        return panel;
    }

    protected void exportSelectedDatasets() {
        // NOTE: get selected dataset will give you all the dataset selected in the base model of sort filter table
        // model
        // EmfDataset[] emfDatasets = getNonExternalDatasets(getSelectedDatasets()); //export external datasets allowed
        // now
        EmfDataset[] emfDatasets = getSelectedDatasets().toArray(new EmfDataset[0]);

        ExportWindow exportView = new ExportWindow(emfDatasets, desktopManager, parentConsole, session, "", "");
        getDesktopPane().add(exportView);

        ExportPresenter exportPresenter = new ExportPresenterImpl(this.session);
        
        presenter.doExport(exportView, exportPresenter, emfDatasets);
    }

    protected void purgeDelectedDatasets() throws EmfException {
        int numDelDatasets = presenter.getNumOfDeletedDatasets();
        String ls = System.getProperty("line.separator");

        String message = "You have " + numDelDatasets + " dataset" + (numDelDatasets > 0 ? "s " : " ")
                + "marked as deleted."
                + (numDelDatasets > 0 ? ls + "Are you sure you want to remove them permanently?" : "");

        int selection;

        if (presenter.isAdminUser()) {
            message = "As an admin user, purge datasets will clear up all the datasets that do not have emissions data."
                    + ls + "Are you sure you want to remove them permanently?";
            UIManager.put("OptionPane.okButtonMnemonic", "79");  // for Setting 'O' as mnemonic
            selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
        } else {
            if (numDelDatasets == 0) {
                JOptionPane.showMessageDialog(parentConsole, message);
                return;
            }
            
            selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
        }
        
        if (selection == JOptionPane.NO_OPTION)
            return;

        if (selection == JOptionPane.YES_OPTION) {
            Thread populateThread = new Thread(new Runnable() {
                public void run() {
                    waitOnPurgingDatasets();
                }
            });
            populateThread.start();
        }
    }

    private synchronized void waitOnPurgingDatasets() {
        try {
            messagePanel.setMessage("Please wait while purging all datasets...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            presenter.purgeDeletedDatasets();
            messagePanel.clear();
            setCursor(Cursor.getDefaultCursor());
        } catch (Exception e) {
            String msg = e.getMessage();

            if (msg != null && msg.length() > 100)
                msg = msg.substring(0, 100);

            messagePanel.setError("Error purging datasets: " + msg);
            e.printStackTrace();
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private List<EmfDataset> getSelectedDatasets() {
        return (List<EmfDataset>) table.selected();
    }

    protected void doDisplayPropertiesViewer() throws EmfException {
        clearMessage();
        // for now, don't get updated copies of datasets - see updateSelectedDatasets
        // List datasets = updateSelectedDatasets(getSelectedDatasets());
        final List<EmfDataset> datasets = getSelectedDatasets();
        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more Datasets");
            return;
        }
        //long running methods.....
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(this, false);

        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        class ViewDatasetPropertiesTask extends SwingWorker<Void, Void> {
            
            private Container parentContainer;

            public ViewDatasetPropertiesTask(Container parentContainer) {
                this.parentContainer = parentContainer;
            }

            /*
             * Main task. Executed in background thread.
             * don't update gui here
             */
            @Override
            public Void doInBackground() throws EmfException  {
                for (Iterator iter = datasets.iterator(); iter.hasNext();) {
                    DatasetPropertiesViewer view = new DatasetPropertiesViewer(session, parentConsole, desktopManager);
                    EmfDataset dataset = (EmfDataset) iter.next();
                    presenter.doDisplayPropertiesView(view, dataset);
                }
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
                    
                } catch (InterruptedException e1) {
//                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
//                    messagePanel.setError(e1.getCause().getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } finally {
//                    this.parentContainer.setCursor(null); //turn off the wait cursor
//                    this.parentContainer.
                    ComponentUtility.enableComponents(parentContainer, true);
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                }
            }
        };
        new ViewDatasetPropertiesTask(this).execute();
    }

    protected void doDisplayPropertiesEditor() {
        clearMessage();
        final List<EmfDataset> datasets = getSelectedDatasets();

        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more Datasets");
            return;
        }
        //long running methods.....
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(this, false);

        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        class EditDatasetPropertiesTask extends SwingWorker<Void, Void> {
            
            private Container parentContainer;

            public EditDatasetPropertiesTask(Container parentContainer) {
                this.parentContainer = parentContainer;
            }

            /*
             * Main task. Executed in background thread.
             * don't update gui here
             */
            @Override
            public Void doInBackground() throws EmfException  {
                for (Iterator iter = datasets.iterator(); iter.hasNext();) {
                    EmfDataset dataset = (EmfDataset) iter.next();
                    DatasetPropertiesEditor view = new DatasetPropertiesEditor(session, parentConsole, desktopManager);
                    try {
                        presenter.doDisplayPropertiesEditor(view, dataset);
                    } catch (EmfException e) {
                        showError(e.getMessage());
                    }
                }
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
                    
                } catch (InterruptedException e1) {
//                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
//                    messagePanel.setError(e1.getCause().getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } finally {
//                    this.parentContainer.setCursor(null); //turn off the wait cursor
//                    this.parentContainer.
                    ComponentUtility.enableComponents(parentContainer, true);
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                }
            }
        };
        new EditDatasetPropertiesTask(this).execute();
    }

    protected void doDisplayVersionedData() throws EmfException {
        clearMessage();
        final List datasets = getSelectedDatasets();

        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more Datasets");
            return;
        }
            for (Iterator iter = datasets.iterator(); iter.hasNext();) {
                    VersionedDataWindow view = new VersionedDataWindow(parentConsole, desktopManager);
                    presenter.doDisplayVersionedData(view, (EmfDataset) iter.next());
                }
    }

    private void doRemove() {
        clearMessage();
        final List<EmfDataset> datasets = getSelectedDatasets();

        if (datasets.isEmpty()) {
            messagePanel.setMessage("Please select one or more Datasets");
            return;
        }

        String message = "Are you sure you want to remove the selected " + datasets.size() + " dataset(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, new Label("", message), "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (selection == JOptionPane.YES_OPTION) {
            //long running methods.....
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            ComponentUtility.enableComponents(this, false);

            //Instances of javax.swing.SwingWorker are not reusuable, so
            //we create new instances as needed.
            class RemoveDatasetsTask extends SwingWorker<Void, Void> {
                
                private Container parentContainer;

                public RemoveDatasetsTask(Container parentContainer) {
                    this.parentContainer = parentContainer;
                }

                /*
                 * Main task. Executed in background thread.
                 * don't update gui here
                 */
                @Override
                public Void doInBackground() throws EmfException  {
                    try {
                        presenter.doDeleteDataset(datasets.toArray(new EmfDataset[0]));
                    } catch (EmfException e) {
                        messagePanel.setMessage(e.getMessage());
                    }
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
                        
                    } catch (InterruptedException e1) {
//                        messagePanel.setError(e1.getMessage());
//                        setErrorMsg(e1.getMessage());
                    } catch (ExecutionException e1) {
//                        messagePanel.setError(e1.getCause().getMessage());
//                        setErrorMsg(e1.getCause().getMessage());
                    } finally {
//                        this.parentContainer.setCursor(null); //turn off the wait cursor
//                        this.parentContainer.
                        ComponentUtility.enableComponents(parentContainer, true);
                        this.parentContainer.setCursor(null); //turn off the wait cursor
                        removeButton.grabFocus();
                    }
                }
            };
            new RemoveDatasetsTask(this).execute();
        }
    }

    public void showError(String message) {
        messagePanel.setError(message);
        super.refreshLayout();
    }

    public void observe(DatasetsBrowserPresenter presenter) {
        this.presenter = presenter;
    }

    public void refresh(EmfDataset[] datasets) {
        clearMessage();
        table.refresh(new EmfDatasetTableData(datasets));
        super.refreshLayout();
    }

    public void showMessage(String message) {
        messagePanel.setMessage(message);
        super.refreshLayout();
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    public void doRefresh() throws EmfException {  
        int numDataset = presenter.getNumOfDatasets(getSelectedDSType(), textFilter.getText());
        if ( numDataset >= 300){
            String message = "<html>There are " + numDataset + " datasets, which could take a while to transfer, would you like to continue?<br>"
            +"[Hint: if you choose not to continue, enter a filter in Name Contains before proceeding]</html>";
            int selection = JOptionPane.showConfirmDialog(parentConsole, new Label("", message), "Warning", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (selection == JOptionPane.NO_OPTION ||
                    selection == JOptionPane.CANCEL_OPTION) {
                return; 
            }
        }
        
        //long running methods.....
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(this, false);

        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        class RefreshDatasetsTask extends SwingWorker<EmfDataset[], Void> {
            
            private Container parentContainer;

            public RefreshDatasetsTask(Container parentContainer) {
                this.parentContainer = parentContainer;
            }

            /*
             * Main task. Executed in background thread.
             * don't update gui here
             */
            @Override
            public EmfDataset[] doInBackground() throws EmfException  {
                return presenter.getEmfDatasets(getSelectedDSType(), textFilter.getText());
            }

            /*
             * Executed in event dispatching thread
             */
            @Override
            public void done() {
                try {
                    //make sure something didn't happen
                    refresh(get());
                    dsTypeLoaded = getSelectedDSType();
                } catch (InterruptedException e1) {
//                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
//                    messagePanel.setError(e1.getCause().getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } finally {
//                    this.parentContainer.setCursor(null); //turn off the wait cursor
//                    this.parentContainer.
                    ComponentUtility.enableComponents(parentContainer, true);
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                    dsTypesBox.grabFocus();
                }
            }
        };
        new RefreshDatasetsTask(this).execute();
    }
    
    public void notifyLockFailure(EmfDataset dataset) {
        clearMessage();
        showError("Cannot obtain a lock for dataset \"" + dataset.getName() + "\".");
    }

    public EmfDataset[] getSelected() {
        List<EmfDataset> selected = getSelectedDatasets();
        
        if (selected != null && selected.size() > 0)
            selected.toArray(new EmfDataset[0]);
            
        return null;
    }

    public String getNameContains() {
        return textFilter.getText();
        
    }

    public void populate() {
        //long running methods.....
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(this, false);

        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        class GetDatasetTypesTask extends SwingWorker<Void, Void> {
            
            private Container parentContainer;

            public GetDatasetTypesTask(Container parentContainer) {
                this.parentContainer = parentContainer;
            }

            /*
             * Main task. Executed in background thread.
             * don't update gui here
             */
            @Override
            public Void doInBackground() throws EmfException  {
                getAllDSTypes();
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
                    dsTypesBox.resetModel(allDSTypes);
                } catch (InterruptedException e1) {
//                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
//                    messagePanel.setError(e1.getCause().getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } finally {
//                    this.parentContainer.setCursor(null); //turn off the wait cursor
//                    this.parentContainer.
                    ComponentUtility.enableComponents(parentContainer, true);
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                    messagePanel.setMessage("Please select a dataset type to display datasets.");
                }
            }
        };
        new GetDatasetTypesTask(this).execute();
    }
}
