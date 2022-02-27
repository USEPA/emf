package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

//FIXME: look at the common design b/w this and UserManagerWindow. Refactor ?
public class SectorsManagerWindow extends DisposableInteralFrame implements SectorsManagerView, RefreshObserver {

    private SectorsManagerPresenter presenter;

    private SelectableSortFilterWrapper table;

    private JPanel layout;
    
    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    public SectorsManagerWindow(EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Sector Manager", new Dimension(475, 300), desktopManager);
        super.setName("sectorManager");

        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(SectorsManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Sector[] sectors) {
        doLayout(sectors);
        super.display();
    }

    public void refresh(Sector[] sectors) {
        table.refresh(new SectorsTableData(sectors));
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.refreshLayout();
    }

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }

    private void doLayout(Sector[] sectors) {
        //model = new EmfTableModel(new SectorsTableData(sectors));
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, new SectorsTableData(sectors), null);
        table.getTable().getAccessibleContext().setAccessibleName("List of sectors");
        tablePanel.add(table);
        createLayout();
    }

    private void createLayout() {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel, BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Sectors", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        closeButton.setMnemonic(KeyEvent.VK_L);
        closePanel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewSectors();
            }

        };

        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);
        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);
        crudPanel.add(viewButton);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editSectors();
            }

        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);
        crudPanel.add(editButton);

        JButton newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                createNewSector();
            }
        });
        newButton.setMnemonic(KeyEvent.VK_N);
        crudPanel.add(newButton);

        return crudPanel;
    }

    private void viewSectors() {
        List sectors = selected();
        // TODO: move it into Presenter - look at UserManagerWindow
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            Sector sector = (Sector) iter.next();
            presenter.doView(sector, displaySectorView());
        }
    }

    private void editSectors() {
        List sectors = selected();
        // TODO: move it into Presenter - look at UserManagerWindow
        for (Iterator iter = sectors.iterator(); iter.hasNext();) {
            Sector sector = (Sector) iter.next();
            try {
                presenter.doEdit(sector, editSectorView(), displaySectorView());
            } catch (EmfException e) {
                setError("Could not edit Sector: " + sector.getName() + "." + e.getMessage());
            }
        }
    }

    private void createNewSector() {
        Sector sector = new Sector("New Sector", "New Sector");
        presenter.displayNewSector(sector, newSectorView());
    }

    private void setError(String message) {
        messagePanel.setError(message);
        super.refreshLayout();
    }

    private List selected() {
        return table.selected();
    }

    // FIXME: this table refresh sequence applies to every CRUD panel. Refactor
    private ViewSectorWindow displaySectorView() {
        return new ViewSectorWindow(desktopManager);
    }

    private EditableSectorView editSectorView() {
        return new EditSectorWindow(desktopManager, parentConsole);
    }

    private NewSectorView newSectorView() {
        return new NewSectorWindow(desktopManager, parentConsole);
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }

}
