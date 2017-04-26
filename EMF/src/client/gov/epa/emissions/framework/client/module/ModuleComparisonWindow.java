package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.ViewMode;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.LiteModule;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ModuleComparisonWindow extends ReusableInteralFrame implements ModuleComparisonView, RefreshObserver {

    private ModuleComparisonPresenter presenter;

    private SelectableSortFilterWrapper table;

    private JPanel layout;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;
    private EmfSession session;

    private Button swapButton;
    
    private Module firstModule;
    private Module secondModule;
    
    private TreeMap<String, String[]> comp = new TreeMap<String, String[]>();

    public ModuleComparisonWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager, Module firstModule, Module secondModule) {
        super(getWindowTitle(firstModule, secondModule), new Dimension(700, 350), desktopManager);
        super.setName(getWindowTitle(firstModule, secondModule));

        this.session = session;
        this.parentConsole = parentConsole;

        this.firstModule = firstModule;
        this.secondModule = secondModule;
        
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    private static String getWindowTitle(Module firstModule, Module secondModule) {
        return "Module Comparison  -  [ 1 ] " + firstModule.getName() + "  vs  [ 2 ] " + secondModule.getName();
    }

    public void observe(ModuleComparisonPresenter presenter) {
        this.presenter = presenter;
    }

    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.refreshLayout();
    }

    public void display() {
        createLayout();
        super.display();
        populate();
    }

    private void createLayout() {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(createTablePanel(), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Modules", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private void computeComp() {
        Map<String, String> firstCompData = firstModule.getCompData(session.moduleService(), session.dataService());
        Map<String, String> secondCompData = secondModule.getCompData(session.moduleService(), session.dataService());
        comp = new TreeMap<String, String[]>();
        for(String key : firstCompData.keySet()) {
            String firstData = firstCompData.get(key);
            if (firstData == null)
                firstData = "";
            if (secondCompData.containsKey(key)) {
                String secondData = secondCompData.get(key);
                if (secondData == null)
                    secondData = "";
                comp.put(key, new String[] { firstData.equals(secondData) ? "MATCH" : "DIFFERENT", firstData, secondData });
            } else {
                comp.put(key, new String[] { "FIRST ONLY", firstData, "" });
            }
        }
        for(String key : secondCompData.keySet()) {
            String secondData = secondCompData.get(key);
            if (firstCompData.containsKey(key))
                continue;
            comp.put(key, new String[] { "SECOND ONLY", "", secondData });
        }
    }

    private JPanel createTablePanel() {
        computeComp();
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, new ModuleComparisonTableData(firstModule, secondModule, comp), null);
        tablePanel.add(table);
        return tablePanel;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        Button closeButton = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        closePanel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        Action swapAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                swapModules();
            }
        };
        swapButton = new Button("Swap Modules", swapAction);
        swapButton.setMnemonic(KeyEvent.VK_S);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(swapButton);

        return crudPanel;
    }

    private void swapModules() {
        Module tempModule = firstModule;
        firstModule = secondModule;
        secondModule = tempModule;
        super.setTitle(getWindowTitle(firstModule, secondModule));
        super.setName(getWindowTitle(firstModule, secondModule));
        doRefresh();
    }

    public EmfConsole getParentConsole() {
        return this.parentConsole;
    }

    @Override
    public void doRefresh() {
        // FIXME these settings are reverted somewhere else
        swapButton.setEnabled(true);

        // TODO fetch the two modules from the server
        computeComp();
        table.refresh(new ModuleComparisonTableData(firstModule, secondModule, comp));
        panelRefresh();
    }

    @Override
    public void populate() {
        doRefresh();
    }
}
