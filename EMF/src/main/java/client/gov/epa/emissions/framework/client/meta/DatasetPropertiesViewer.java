package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportPresenterImpl;
import gov.epa.emissions.framework.client.exim.ExportWindow;
import gov.epa.emissions.framework.client.meta.info.InfoTab;
import gov.epa.emissions.framework.client.meta.keywords.KeywordsTab;
import gov.epa.emissions.framework.client.meta.logs.LogsTab;
import gov.epa.emissions.framework.client.meta.notes.NotesTab;
import gov.epa.emissions.framework.client.meta.qa.QATab;
import gov.epa.emissions.framework.client.meta.revisions.RevisionsTab;
import gov.epa.emissions.framework.client.meta.summary.SummaryTab;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DatasetPropertiesViewer extends DisposableInteralFrame implements PropertiesView {

    private PropertiesViewPresenter presenter;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private DesktopManager desktopManager;
    
    private EmfSession session;
    
    private EmfDataset dataset;
    
    private Version version; 
    
    private boolean sourcesLoaded;

    private JTabbedPane tabbedPane;

    private boolean editable;

    public DatasetPropertiesViewer(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager, boolean editable) {

        super("Dataset Properties View", new Dimension(750, 500), desktopManager);
        this.parentConsole = parentConsole;
        this.session=session;
        this.desktopManager = desktopManager;
        this.editable = editable;
    }

    public DatasetPropertiesViewer(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        this(session, parentConsole, desktopManager, true);
    }

    public void display(EmfDataset dataset, Version version) {
        super.setTitle("Dataset Properties View: " + dataset.getName()+ " (ID = "+dataset.getId() + ")");
        super.setName("datasetPropertiesView:" + dataset.getId());
        this.version=version; 
        this.dataset=dataset; 
        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        tabbedPane = new JTabbedPane();
        panel.add(createTabbedPane(), BorderLayout.CENTER);
        panel.add(createBottomPanel(), BorderLayout.PAGE_END);

        super.getContentPane().add(panel);

        super.display();
    }

    private JTabbedPane createTabbedPane() {
        tabbedPane.setName("tabbedPane");

        tabbedPane.addTab("Summary", createSummaryTab());
        tabbedPane.addTab("Data", createDataTab(parentConsole));
        tabbedPane.addTab("Keywords", createKeywordsTab());
        tabbedPane.addTab("Notes", createNotesTab(parentConsole));
        tabbedPane.addTab("Revisions", createRevisionsTab(parentConsole));
        tabbedPane.addTab("History", createLogsTab(parentConsole));
        tabbedPane.addTab("Sources", createInfoTab(parentConsole));
        tabbedPane.addTab("QA", createQAStepsTab(desktopManager));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        
        final MessagePanel localMsgPanel = this.messagePanel;
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                localMsgPanel.clear();
                
                int tabIndex = tabbedPane.getSelectedIndex();
                String tabTitle = tabbedPane.getTitleAt(tabIndex);

                if (tabTitle.equals("Sources") && !sourcesLoaded) {
                    Component cp = tabbedPane.getSelectedComponent();
                    
                    try {
                        ((RefreshObserver)cp).doRefresh();
                    } catch (Exception e1) {
                        messagePanel.setError(e1.getMessage());
                    }
                    
                    sourcesLoaded = true;
                }
            }
        });

        return tabbedPane;
    }

    private JPanel createSummaryTab() {
        SummaryTab view = new SummaryTab(dataset, version);
        presenter.set(view);
        return view;
    }

    private JPanel createDataTab(EmfConsole parentConsole) {
        DataTab view = new DataTab(parentConsole, desktopManager, messagePanel, this.editable);
        presenter.set(view);
        return view;
    }

    private JPanel createInfoTab(EmfConsole parentConsole) {
        InfoTab view = new InfoTab(messagePanel, null, parentConsole, true);
        presenter.set(view);
        return view;
    }

    private JPanel createKeywordsTab() {
        KeywordsTab view = new KeywordsTab();
        presenter.set(view);
        return view;
    }

    private JPanel createNotesTab(EmfConsole parentConsole) {
        try {
            NotesTab view = new NotesTab(parentConsole, desktopManager);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            messagePanel.setError("Could not load Notes tab. Failed communication with remote EMF Services.");
            return createErrorTab("Could not load Notes tab. Failed communication with remote EMF Services.");
        }
    }

    private JPanel createRevisionsTab(EmfConsole parentConsole) {
        try {
            RevisionsTab view = new RevisionsTab(parentConsole, desktopManager, messagePanel, editable);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            messagePanel.setError("Could not load Revisions tab. Failed communication with remote EMF Services.");
            return createErrorTab("Could not load Revisions tab. Failed communication with remote EMF Services.");
        }
    }

    private JPanel createLogsTab(EmfConsole parentConsole) {
        try {
            LogsTab view = new LogsTab(parentConsole);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            messagePanel.setError("Could not load Logs tab. Failed communication with remote Logging Services.");
            return createErrorTab("Could not load Logs tab. Failed communication with remote Logging Services.");
        }
    }

    private Component createQAStepsTab(DesktopManager desktopManager) {
        try {
            QATab view = new QATab(messagePanel, parentConsole, desktopManager); //BUG3615
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            return createErrorTab("Could not load QASteps from QA Service.");
        }
    }

    private JPanel createErrorTab(String message) {
        JPanel panel = new JPanel(false);
        JLabel label = new JLabel(message);
        label.setForeground(Color.RED);
        panel.add(label);

        return panel;
    }

    private JPanel createBottomPanel() {
        
        JPanel left = new JPanel();
        Button property = new Button("Edit Properties", editPropertyAction());
        property.setMnemonic('E');
        property.setEnabled(this.editable);
        
        Button data = new Button("Edit Data", editDataAction());
        data.setMnemonic('a');
        data.setEnabled(this.editable);

        left.add(property);
        left.add(data);

        JPanel right = new JPanel();
        Button refresh = new Button("Refresh", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    refreshCurrentTab();
                } catch (EmfException e) {
                    showError(e.getMessage());
                }
            }
        });
        right.add(refresh);
        refresh.setToolTipText("Refresh only the current tab with focus.");
        
        Button exportButton = new ExportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                exportDataset(dataset);
            }
        });
        exportButton.setToolTipText("Export dataset");
        right.add(exportButton);
        Button close = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        right.add(close);
        getRootPane().setDefaultButton(close);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(left, BorderLayout.LINE_START);
        panel.add(right, BorderLayout.LINE_END);

        return panel;
    }
    
    private void refreshCurrentTab() throws EmfException {
        RefreshObserver tab = (RefreshObserver) tabbedPane.getSelectedComponent();

        try {
            messagePanel.clear();
            tab.doRefresh();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    private void exportDataset(EmfDataset dataset) {
        EmfDataset[] emfDatasets = {dataset};

        ExportWindow exportView = new ExportWindow(emfDatasets, desktopManager, parentConsole, session, "", "");
        getDesktopPane().add(exportView);

        ExportPresenter exportPresenter = new ExportPresenterImpl(this.session);

        presenter.doExport(exportView, exportPresenter);
    }

    private Action editPropertyAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                doDisplayPropertiesEditor();
                presenter.doClose();
            }
        };
    }

    private void doDisplayPropertiesEditor() {
        try {
            presenter.doDisplayPropertiesEditor(parentConsole, desktopManager);
        } catch (EmfException e) {
            showError(e.getMessage());
        }
    }

    private Action editDataAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                doDisplayVersionedData();
            }
        };
    }

    protected void doDisplayVersionedData() {
        presenter.doDisplayVersionedData(parentConsole, desktopManager);
    }

    public void observe(PropertiesViewPresenter presenter) {
        this.presenter = presenter;
    }

    public void showError(String message) {
        messagePanel.setError(message);
    }

    public void clearMessage() {
        messagePanel.clear();
    }

    public void setDefaultTab(int index) {
        this.tabbedPane.setSelectedIndex(index);
    }

}
