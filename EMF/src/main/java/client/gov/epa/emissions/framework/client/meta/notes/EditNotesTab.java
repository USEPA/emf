package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.ViewButton;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;

public class EditNotesTab extends JPanel implements EditNotesTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private NotesTableData tableData;

    private SelectableSortFilterWrapper table;

    private JPanel tablePanel;

    private ManageChangeables changeables;

    private MessagePanel messagePanel;
    
    private EditNotesTabPresenter presenter;

    private DesktopManager desktopManager;

    public EditNotesTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager) {
        super.setName("editNotesTab");
        this.parentConsole = parentConsole;
        this.changeables = changeables;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;

        super.setLayout(new BorderLayout());
    }

    public void display(DatasetNote[] datasetNotes) {
        super.removeAll();
        super.add(createLayout(datasetNotes), BorderLayout.CENTER);
    }

    private JPanel createLayout(DatasetNote[] datasetNotes) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(tablePanel(datasetNotes), BorderLayout.CENTER);
        layout.add(controlPanel(), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(DatasetNote[] datasetNotes) {
        setupTableModel(datasetNotes);
        changeables.addChangeable(tableData);
        if ( table == null){
            tablePanel = new JPanel(new BorderLayout());
            table = new SelectableSortFilterWrapper(parentConsole, tableData, null);
            tablePanel.add(table);
        }else
            refresh();
        return tablePanel;
    }
    
    private void setupTableModel(DatasetNote[] datasetNotes) {
        tableData = new NotesTableData(datasetNotes);
    }

    private JPanel controlPanel() {
        JPanel container = new JPanel();

        Button add = new AddButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNewNote();
            }
        });
        add.setToolTipText("add a new note");
        container.add(add);
        
        Button addExisting = new AddButton("Add Existing", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addExistingNotes();
            }
        });
        addExisting.setToolTipText("add an existing note");
        container.add(addExisting);

        Button view = new ViewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doViewNote();
            }
        });
        container.add(view);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    protected void doViewNote() {
        List notes = table.selected();
        for (Iterator iter = notes.iterator(); iter.hasNext();) {
            ViewNoteWindow window = new ViewNoteWindow(desktopManager);
            presenter.doViewNote((DatasetNote) iter.next(), window);
        }
    }

    protected void doNewNote() {
        NewNoteDialog view = new NewNoteDialog(parentConsole);
        try {
            presenter.doAddNote(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }
    
    protected void addExistingNotes() {
        AddExistingNotesDialog view = new AddExistingNotesDialog(parentConsole);
        try {
            presenter.addExistingNotes(view);
            if (view.check()){
                DatasetNote[] selectedNotes=presenter.getSelectedNotes(view);

//              System.out.println("length of selected notes " + selectedNotes.length );
                if (selectedNotes.length>0){
                    tableData.add( selectedNotes);
                    refresh();
                }
            }
        } catch (EmfException e) {
            e.printStackTrace();
            messagePanel.setError("Could not add existing notes" +e.getMessage());
        }
    }

    public void addNote(DatasetNote note) {
        tableData.add(note);
        refresh();
    }
    
    private void refresh(){
        table.refresh(tableData);
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.validate();
    }

    public DatasetNote[] additions() {
        return tableData.additions();
    }
    
    public void doRefresh() throws EmfException {        
        try {
            messagePanel.setMessage("Please wait while retrieving all dataset notes...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            //presenter.display();
            super.removeAll();
            super.add(createLayout(presenter.getDatasetNotes()));
            super.validate();
            messagePanel.setMessage("Finished loading dataset notes.");
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    public void observe(EditNotesTabPresenter presenter){
        this.presenter = presenter; 
    }

}
