package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.gui.*;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlStrategyProgramFilter;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.TrackableSortFilterSelectModel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.*;

public class ControlProgramSelectionDialog extends JDialog implements ControlProgramSelectionView {

    private ControlProgramTableData tableData;

    private TrackableSortFilterSelectModel selectModel;

    private JPanel tablePanel;

    private SelectableSortFilterWrapper table;

    private EmfConsole parent;

    private ControlProgramSelectionPresenter presenter;
    
    private ManageChangeables changeables;

    private TextField simpleTextFilter;

    private ComboBox filterFieldsComboBox;

    public ControlProgramSelectionDialog(EmfConsole parent, ManageChangeables changeables) {
        super(parent);
        setModal(true);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        this.parent = parent;
        this.changeables = changeables;
    }

    public void display(ControlProgram[] controlPrograms) throws EmfException {
        tableData = new ControlProgramTableData(controlPrograms);

        EmfTableModel tableModel = new EmfTableModel(tableData);
        selectModel = new TrackableSortFilterSelectModel(tableModel);
        changeables.addChangeable(selectModel);
//        table = new SelectableSortFilterWrapper(parent, tableData, sortCriteria());
//        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parent, selectModel);


        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(createTopPanel(), BorderLayout.NORTH);
        contentPane.add(tablePanel(), BorderLayout.CENTER);
        contentPane.add(buttonPanel(), BorderLayout.SOUTH);

        setTitle("Select Control Programs");
        this.pack();
        this.setSize(600,400);
        this.setLocation(ScreenUtils.getPointToCenter(parent));
        this.setVisible(true);
    }

    private JPanel tablePanel() {

        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parent, tableData, sortCriteria());

        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }


    private SortCriteria sortCriteria() {
        String[] columnNames = { "Name" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true });
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        simpleTextFilter = new TextField("textfilter", 25);
        simpleTextFilter.setPreferredSize(new Dimension(360, 25));
        simpleTextFilter.setEditable(true);
        simpleTextFilter.addActionListener(simpleFilterTypeAction());


        JPanel advPanel = new JPanel(new BorderLayout(5, 2));

        //get table column names
//        String[] columns = new String[] {"Module Name", "Composite?", "Final?", "Tags", "Project", "Module Type", "Version", "Creator", "Date", "Lock Owner", "Lock Date", "Description" };//(new ModulesTableData(new ConcurrentSkipListMap<Integer, LiteModule>())).columns();

        filterFieldsComboBox = new ComboBox("Select one", (new ControlStrategyProgramFilter()).getFilterFieldNames());
        filterFieldsComboBox.setSelectedIndex(1);
        filterFieldsComboBox.setPreferredSize(new Dimension(180, 25));
        filterFieldsComboBox.addActionListener(simpleFilterTypeAction());

        advPanel.add(getFilterFieldsComboBoxPanel("Filter Fields:", filterFieldsComboBox), BorderLayout.LINE_START);
        advPanel.add(simpleTextFilter, BorderLayout.EAST);
//        advPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

        topPanel.add(advPanel, BorderLayout.EAST);

        JPanel mainPanel = new JPanel(new GridLayout(2, 1));
        mainPanel.add(panel);
        mainPanel.add(topPanel);

        return mainPanel;
    }

    private Action simpleFilterTypeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
//                DatasetType type = getSelectedDSType();
//                try {
//                    // count the number of datasets and do refresh
//                    if (dsTypesBox.getSelectedIndex() > 0)
                try {
                    doRefresh();
                } catch (EmfException e1) {
                    e1.printStackTrace();
                }
//                } catch (EmfException e1) {
////                    messagePanel.setError("Could not retrieve all modules " /*+ type.getName()*/);
//                }
            }
        };
    }

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }

    private JPanel getFilterFieldsComboBoxPanel(String label, JComboBox box) {
        JPanel panel = new JPanel(new BorderLayout(5, 2));
        JLabel jlabel = new JLabel(label);
        jlabel.setHorizontalAlignment(JLabel.RIGHT);
        panel.add(jlabel, BorderLayout.WEST);
        panel.add(box, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 10));

        return panel;
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                add();
                setVisible(false);
                dispose();
            }
        };
    }

    private void add() {
        List selected = table.selected();
        ControlProgram[] sccs = (ControlProgram[]) selected.toArray(new ControlProgram[0]);
        presenter.doAdd(sccs);

    }

    public void observe(Object presenter) {
        this.presenter = (ControlProgramSelectionPresenter)presenter;
    }

    public void refresh(ControlProgram[] controlPrograms) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        tableData = new ControlProgramTableData(controlPrograms);
        table.refresh(tableData);
        panelRefresh();
        setCursor(Cursor.getDefaultCursor());
    }

    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
//        super.refreshLayout();
    }

    public BasicSearchFilter getSearchFilter() {

        BasicSearchFilter searchFilter = new BasicSearchFilter();
        String fieldName = (String)filterFieldsComboBox.getSelectedItem();
        if (fieldName != null) {
            searchFilter.setFieldName(fieldName);
            searchFilter.setFieldValue(simpleTextFilter.getText());
        }
        return searchFilter;
    }
}
