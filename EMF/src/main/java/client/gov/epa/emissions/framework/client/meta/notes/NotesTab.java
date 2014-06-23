package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class NotesTab extends JPanel implements NotesTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private SelectableSortFilterWrapper table;

    private NotesTabPresenter presenter;

    private DesktopManager desktopManager;

    public NotesTab(EmfConsole parentConsole, DesktopManager desktopManager) {
        super.setName("notesTab");
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(DatasetNote[] notes, NotesTabPresenter presenter) {
        this.presenter = presenter;
        super.removeAll();
        super.add(createLayout(notes));
    }

    private JPanel createLayout(DatasetNote[] notes) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(tablePanel(notes, parentConsole), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);

        return layout;
    }

    private JPanel tablePanel(DatasetNote[] notes, EmfConsole parentConsole) {
        JPanel tablePanel = new JPanel(new BorderLayout());
        if (table == null)
            table = new SelectableSortFilterWrapper(parentConsole, new NotesTableData(notes), null);
        else 
            table.refresh(new NotesTableData(notes));
        
        tablePanel.add(table);
        return tablePanel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JButton viewButton = new JButton("View");
        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                viewNotes();
            }
        });
        buttonPanel.add(viewButton);

        panel.add(buttonPanel, BorderLayout.LINE_START);

        return panel;
    }

    private void viewNotes() {
        List selected = table.selected();
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ViewNoteWindow view = new ViewNoteWindow(desktopManager);
            presenter.doViewNote((DatasetNote) iter.next(), view);    
        }
    }
    
    public void doRefresh() throws EmfException {        
        try {           
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            //presenter.display();
            super.removeAll();
            super.add(createLayout(presenter.getDatasetNotes()));
            super.validate();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}
