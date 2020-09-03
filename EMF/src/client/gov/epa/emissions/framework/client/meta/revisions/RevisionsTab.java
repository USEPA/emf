package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.swingworker.RefreshSwingWorkerTasks;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class RevisionsTab extends JPanel implements RevisionsTabView, RefreshObserver{

    private EmfConsole parentConsole;

    private SelectableSortFilterWrapper table;

    private DesktopManager desktopManager;
    
    private MessagePanel messagePanel;

    private RevisionsTabPresenter presenter;

    private boolean editable;
    
    public RevisionsTab(EmfConsole parentConsole, DesktopManager desktopManager, MessagePanel messagePanel, boolean editable) {

        super.setName("revisionsTab");
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;
        this.messagePanel =messagePanel;
        
        this.editable = editable;
        
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(Revision[] revisions, RevisionsTabPresenter presenter) {
        this.presenter = presenter;
        super.removeAll();
        super.add(createLayout(revisions));
    }

    private JPanel createLayout(Revision[] revisions) {
        JPanel layout = new JPanel(new BorderLayout(5, 10));

        layout.add(tablePanel(revisions, parentConsole), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);

        return layout;
    }

    private JPanel tablePanel(Revision[] revisions, EmfConsole parentConsole) {
        //EmfTableModel model = new EmfTableModel(new RevisionsTableData(revisions));
        JPanel tablePanel = new JPanel(new BorderLayout());
        if ( table==null ) 
            table = new SelectableSortFilterWrapper(parentConsole, new RevisionsTableData(revisions), null);
        else
            table.refresh(new RevisionsTableData(revisions));
        tablePanel.add(table);
        return tablePanel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        Insets insets = new Insets(1, 2, 1, 2);

        JPanel buttonPanel = new JPanel();
        JButton viewButton = new JButton("View");
        viewButton.setMargin(insets);
        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                viewRevisions();
            }
        });
        viewButton.setMnemonic('V');
        buttonPanel.add(viewButton);

        JButton editButton = new JButton(new AbstractAction("Edit"){
            public void actionPerformed(ActionEvent event) {
                editRevisions();
            }
        });
        editButton.setMnemonic('E');
        editButton.setMargin(insets);
        editButton.setEnabled(this.editable);
        
        buttonPanel.add(editButton);

        panel.add(buttonPanel, BorderLayout.LINE_START);

        return panel;
    }

    private void viewRevisions() {
        List selected = table.selected();
        if (selected == null ||selected.size() == 0) {
            messagePanel.setMessage("Please select a revision.");
            return;
        }
            
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ViewRevisionWindow view = new ViewRevisionWindow(desktopManager);
            presenter.doViewRevision((Revision) iter.next(), view);
        }
    }

    private void editRevisions() {

        List<Revision> selected = (List<Revision>) table.selected();
        if (selected == null || selected.size() == 0) {
            messagePanel.setMessage("Please select a revision.");
        }
        else {
            for (Revision revision : selected) {
                try {
                    presenter.doEditRevision(revision, new RevisionEditorViewImpl(this.parentConsole), this);
                } catch (EmfException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void doRefresh() throws EmfException {
        try {
            new RefreshSwingWorkerTasks(this, messagePanel, presenter).execute();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    
    public void doRefresh(Revision[] revisions){
        super.removeAll();
        super.add(createLayout(revisions));
        super.validate();
        messagePanel.setMessage("Finished loading dataset revisions.");
    }

    @Override
    public void refreshMSG() {
        messagePanel.clear();
        messagePanel.setMessage("Refresh to see the changes in dataset revisions.");
    }
}
