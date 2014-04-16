package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class InfoTab extends JPanel implements InfoTabView, RefreshObserver {

    private EmfConsole parentConsole;

    private JPanel sourcesPanel;
    
    private JPanel filter;

    private JTextField nameFilter;

    private boolean forViewer;

    private ManageChangeables changeablesList;

    private InfoTabPresenter sourceTabPresenter;
    
    private SelectableSortFilterWrapper table;

    private MessagePanel msgPanel;

    private int sourceLimit = -1;

    public InfoTab(MessagePanel messagePanel, ManageChangeables changeablesList, EmfConsole parentConsole,
            boolean forViewer) {
        setName("infoTab");
        this.parentConsole = parentConsole;
        this.forViewer = forViewer;
        this.changeablesList = changeablesList;
        this.msgPanel = messagePanel;

        super.setLayout(new BorderLayout());

        add(createLayout(), BorderLayout.CENTER);
    }

    private JPanel createLayout() {
        JPanel container = new JPanel(new BorderLayout());

        filter = new JPanel();
        filter.add(new JLabel("External Source Name Contains: "));
        nameFilter = new JTextField();
        nameFilter.setPreferredSize(new Dimension(120, 20));
        nameFilter.setToolTipText("An external name filter. Press enter to refresh.");

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    doRefresh();
                } catch (EmfException e) {
                    msgPanel.setError("Cannot retrieve sources.");
                }
            }
        };

        KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        nameFilter.registerKeyboardAction(actionListener, keystroke, JComponent.WHEN_FOCUSED);
        filter.add(nameFilter);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(filter, BorderLayout.LINE_END);
        container.add(panel, BorderLayout.NORTH);

        sourcesPanel = new JPanel(new BorderLayout());
        container.add(sourcesPanel, BorderLayout.CENTER);

        return container;
    }

    public void displayInternalSources(InternalSource[] sources) throws EmfException {
        this.filter.setVisible(false);
        displaySources("Data Tables", new InternalSourcesTableData(sources), false);
    }

    public void displayExternalSources(int numOfSrcs) throws EmfException {
        if (numOfSrcs > 30) {
            SourcesInfoDialog dialog = new SourcesInfoDialog("Limit the Number of Sources to View", numOfSrcs, this,
                    parentConsole);
            sourceLimit = dialog.showDialog();
        }

        int dsId = sourceTabPresenter.getCurrentDatasetId();
        ExternalSource[] sources = sourceTabPresenter.getExternalSrcs(dsId, sourceLimit, nameFilter.getText());
        displaySources("External Files", new ExternalSourcesTableData(sources), true);
    }

    private void displaySources(String title, TableData tableData, boolean external) throws EmfException {
        sourcesPanel.removeAll();
        sourcesPanel.setBorder(new Border(title));
        sourcesPanel.add(createSortFilterPane(tableData, parentConsole, external));
        sourcesPanel.validate();
    }

    private JPanel createSortFilterPane(TableData tableData, EmfConsole parentConsole, boolean external)
            throws EmfException {
        JPanel tablePanel = new JPanel(new BorderLayout());
        if ( table == null )
            table = new SelectableSortFilterWrapper(parentConsole, tableData, null);
        else
            table.refresh(tableData);
        
        tablePanel.add(table, BorderLayout.CENTER);

        EmfDataset dataset = sourceTabPresenter.getDataset();

        if (external && !forViewer) {
            ExternalSource[] extSrcs = sourceTabPresenter
                    .getExternalSrcs(dataset.getId(), sourceLimit, getNameFilter());

            if (extSrcs != null && extSrcs.length > 0)
                tablePanel.add(controlPanel(), BorderLayout.PAGE_END);
        }

        return tablePanel;
    }

    private JPanel controlPanel() {
        JPanel container = new JPanel();

        Button update = new Button("Update", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                updateSources();
            }
        });
        container.add(update);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    private void updateSources() {
        EmfDataset dataset = sourceTabPresenter.getDataset();
        EmfSession session = sourceTabPresenter.getSession();
        String title = "Update Dataset External Source for Dataset: " + dataset.getName();
        ExternalSourceUpdateWindow view = new ExternalSourceUpdateWindow(title, parentConsole, changeablesList, session);
        ExternalSourceUpdatePresenter updatePresenter = new ExternalSourceUpdatePresenter(sourceTabPresenter);
        updatePresenter.display(view);
    }

    public void observe(InfoTabPresenter presenter) {
        sourceTabPresenter = presenter;
    }

    public void doRefresh() throws EmfException {
        try {
            kickPopulateThread();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    private void kickPopulateThread() {
        Thread populateThread = new Thread(new Runnable() {
            public void run() {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    msgPanel.setMessage("Please wait while loading dataset sources...");
                    sourceTabPresenter.doDisplay(nameFilter.getText());
                    msgPanel.setMessage("Finished loading dataset sources.");
                } catch (Exception e) {
                    msgPanel.setError(e.getMessage());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        populateThread.start();
    }

    public String getNameFilter() {
        return nameFilter == null ? null : nameFilter.getText();
    }

    public int getSourceSize() {
        return sourceLimit;
    }

}
