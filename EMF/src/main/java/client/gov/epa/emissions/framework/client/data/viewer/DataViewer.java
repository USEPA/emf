package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.notes.NewNoteDialog;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Dimensions;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;

public class DataViewer extends DisposableInteralFrame implements DataView {

    private JPanel layout;

    private MessagePanel messagePanel;

    private DataViewPresenter presenter;

    private EmfDataset dataset;
    private String filter=""; 
    private EmfConsole parent;

    private boolean editable;
    private ViewerPanel viewerPanel;

    public DataViewer(EmfDataset dataset, EmfConsole parent, DesktopManager desktopManager) {
        this(dataset, parent, desktopManager, true);
    }

    public DataViewer(EmfDataset dataset, EmfConsole parent, DesktopManager desktopManager, boolean editable) {
        super("Data Viewer [Dataset:" + dataset.getName(), desktopManager);
        setDimension();
        this.dataset = dataset;
        this.parent = parent;

        this.editable = editable;
        
        layout = new JPanel(new BorderLayout());
        layout.add(topPanel(), BorderLayout.PAGE_START);

        this.getContentPane().add(layout);
    }

    public DataViewer(EmfDataset dataset, EmfConsole parent, DesktopManager desktopManager, String filter) {
        this(dataset, parent, desktopManager, filter, true);
    }
    
    public DataViewer(EmfDataset dataset, EmfConsole parent, DesktopManager desktopManager, String filter, boolean editable) {
        super("Data Viewer [Dataset:" + dataset.getName(), desktopManager);
        setDimension();
        this.dataset = dataset;
        this.parent = parent;
        this.filter = filter; 

        this.editable = editable;

        layout = new JPanel(new BorderLayout());
        layout.add(topPanel(), BorderLayout.PAGE_START);

        this.getContentPane().add(layout);
    }

    private void setDimension() {
        Dimension dim = new Dimensions().getSize(0.7, 0.7);
        setSize(dim);
    }

    private JPanel topPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        return panel;
    }

    public void observe(DataViewPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Version version, String table, TableMetadata tableMetadata) {
        updateTitle(version, table);
        super.setName("dataViewer:" + version.getDatasetId() + ":" + version.getId());

        JPanel container = new JPanel(new BorderLayout());
        container.add(tablePanel(tableMetadata), BorderLayout.CENTER);
        container.add(controlsPanel(), BorderLayout.PAGE_END);
        layout.add(container, BorderLayout.CENTER);

        super.display();
    }

    private void updateTitle(Version version, String table) {
        String label = super.getTitle();
        label += ", Version: " + version.getName();
        label += ", Table: " + table + "]";
        super.setTitle(label);
    }

    private JPanel tablePanel(TableMetadata tableMetadata) {
        viewerPanel = new ViewerPanel(messagePanel, dataset, tableMetadata, filter);
        try {
            presenter.displayTable(viewerPanel);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        return viewerPanel;
    }

    private JPanel controlsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(leftPanel(), BorderLayout.LINE_START);
        panel.add(rightPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel leftPanel() {
        JPanel leftPanel = new JPanel();
        Button addNote = new AddButton("Add Note", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doAddNote();
            }
        });
        addNote.setEnabled(this.editable);
        
        leftPanel.add(addNote);
        return leftPanel;
    }

    private JPanel rightPanel() {
        JPanel rightPanel = new JPanel();
        Button close = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        });
        rightPanel.add(close);
        return rightPanel;
    }

    private void doAddNote() {
        NewNoteDialog view = new NewNoteDialog(parent);
        try {
            presenter.doAddNote(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void doClose() {
        try {
            viewerPanel.saveColPref();
            presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    public void windowClosing() {
        doClose();
    }

}
