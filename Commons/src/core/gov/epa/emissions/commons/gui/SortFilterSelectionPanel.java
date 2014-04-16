package gov.epa.emissions.commons.gui;

import gov.epa.mims.analysisengine.table.MultiRowHeaderTableModel;
import gov.epa.mims.analysisengine.table.SortFilterTablePanel;
import gov.epa.mims.analysisengine.table.filter.FilterCriteria;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

/**
 * <p>
 * Description: This table can both sort and filter data based on criteria entred by the user.
 * </p>
 */
public class SortFilterSelectionPanel extends SortFilterTablePanel {

    public final static String SELECT_COL_NAME = "Select";

    protected JButton selectAllButton;

    protected JButton clearAllButton;

    private SortFilterSelectModel selectModel;

    public SortFilterSelectionPanel(Component parent, SortFilterSelectModel model) {
        super(parent, model);
        this.selectModel = model;
    }
    
    public SortCriteria getSortCriteria()
    {
        return overallModel.getSortCriteria();
    }

    public FilterCriteria getFilterCriteria()
    {
        return overallModel.getFilterCriteria();
    }
    
    /**
     * Adding/Removing different actions are implemented in createPopupMenuAndToolBar This is only implemented in the
     * gov.epa.emissions.emisview.gui.SortFilterSelectionPanel So if you want to use this constructor from some other
     * class please refactor the createPopupMenuAndToolBar()
     * 
     */
    public SortFilterSelectionPanel(Component parent, MultiRowHeaderTableModel model, boolean popupMenu, boolean sort,
            boolean filter, boolean format, boolean showHide, boolean reset) {
        super(parent, model, popupMenu, sort, filter, format, showHide, reset);
    }

    /**
     * Create the popup menu for the table. Also add any items that are not column specific to the toolbar.
     */
    protected void createPopupMenuAndToolBar() {
        Action action = null;
        popupMenu = new JPopupMenu("Table Operations");

        action = new SortMultipleAction(this);
        if (sort) {
            popupMenu.add(action);
            JButton sortButton = toolBar.add(action);
            sortButton.setToolTipText("Sort(Ascending/Descending)");
        }

        popupMenu.addSeparator();
        action = new FilterAction(this);
        if (filter) {
            popupMenu.add(action);
            JButton filterButton = toolBar.add(action);
            filterButton.setToolTipText("Filter Rows");
        }

        action = new ShowHideColumnsAction(this);
        if (showHide) {
            popupMenu.add(action);
            JButton showHideButton = toolBar.add(action);
            showHideButton.setToolTipText("Show/Hide Columns");
        }

        popupMenu.addSeparator();

        action = new SingleFormatAction(this);
        if (format) {
            popupMenu.add(action);
        }
        action = new MultipleFormatAction(this);
        if (format) {
            popupMenu.add(action);
            JButton formatButton = toolBar.add(action);
            formatButton.setToolTipText("Format columns");
        }

        // popupMenu.add(new AggregateRowsAction(this));
        // popupMenu.add(new AggregateColumnsAction(this));
        popupMenu.addSeparator();

        action = new ResetAction(this);

        if (reset) {
            popupMenu.add(action);
            JButton resetButton = toolBar.add(action);
            resetButton.setToolTipText("Reset");
        }
        action = new SelectAllAction(this);

        selectAllButton = toolBar.add(action);
        selectAllButton.setToolTipText("Select all");
        action = new ClearAllAction(this);

        clearAllButton = toolBar.add(action);
        clearAllButton.setToolTipText("Clear all the selections");

        TableMouseAdapter tableMouseAdapter = new TableMouseAdapter();
        PopupMouseAdapter popupMouseAdapter = new PopupMouseAdapter(scrollPane);
        table.getTableHeader().addMouseListener(tableMouseAdapter);
        table.addMouseListener(popupMouseAdapter);
        table.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    private void selectAll(boolean select) {
        int colNo = overallModel.findColumn(selectModel.getSelectableColumnName());
        int rowCount = getRowCount();

        for (int i = 0; i < rowCount; i++) {
            overallModel.setValueAt(new Boolean(select), i, colNo);
        }
    }

    /**
     * @return the selected object from the column ASSUMING UNIQUE column names
     */
    public Object[] getSelectedObjects(String colName) {
        int col = overallModel.findColumn(colName);
        if (col < 1) // avoiding col =0 for the first col
        {
            throw new IllegalArgumentException("The column name '" + colName + "' does not exist");
        }
        int numRows = overallModel.getRowCount();
        int boolColNo = overallModel.findColumn(SELECT_COL_NAME);
        ArrayList selList = new ArrayList();
        for (int i = 0; i < numRows; i++) {
            Boolean selected = (Boolean) overallModel.getValueAt(i, boolColNo);
            if (selected.booleanValue()) {
                selList.add(overallModel.getValueAt(i, col));
            }
        }// for(i)
        if (selList.size() > 0) {
            return selList.toArray();
        }

        return null;
    }

    /**
     * @return the selected object from the column ASSUMING UNIQUE column names
     */
    public String[] getSelectedStringObjects(String colName) {
        int col = overallModel.findColumn(colName);
        if (col < 1) // avoiding col =0 for the first col
        {
            throw new IllegalArgumentException("The column name '" + colName + "' does not exist");
        }
        int numRows = overallModel.getRowCount();
        int boolColNo = overallModel.findColumn(SELECT_COL_NAME);
        ArrayList selList = new ArrayList();
        for (int i = 0; i < numRows; i++) {
            Boolean selected = (Boolean) overallModel.getValueAt(i, boolColNo);
            if (selected.booleanValue()) {
                selList.add(overallModel.getValueAt(i, col));
            }
        }
        if (selList.size() > 0) {
            String[] stringObjs = new String[selList.size()];
            return (String[]) selList.toArray(stringObjs);
        }

        return null;
    }

    /**
     * @return the selected object from the column ASSUMING UNIQUE column names
     */
    public int[] getSelectedIndexes() {
        int numRows = overallModel.getRowCount();
        int boolColNo = overallModel.findColumn(SELECT_COL_NAME);
        List<Integer> selList = new ArrayList<Integer>();
        for (int i = 0; i < numRows; i++) {
            Boolean selected = (Boolean) overallModel.getValueAt(i, boolColNo);
            if (selected.booleanValue()) {
                selList.add(new Integer(overallModel.getBaseModelRowIndex(i)));
            }
        }
        int[] selectedIndexes = new int[selList.size()];
        for (int i = 0; i < selectedIndexes.length; i++) {
            selectedIndexes[i] = selList.get(i).intValue();
        }
        return selectedIndexes;
    }

    public void addSelectionListener(MouseListener m) {
        table.addMouseListener(m);
        selectAllButton.addMouseListener(m);
        clearAllButton.addMouseListener(m);
    }

    /**
     * An action to select all checkboxes in the checkbox col
     */
    protected class SelectAllAction extends AbstractAction {
        SortFilterSelectionPanel parentRef = null;

        public SelectAllAction(SortFilterSelectionPanel parent) {
            super("All", selAllIcon);
            this.parentRef = parent;
        }

        public void actionPerformed(ActionEvent e) {
            parentRef.selectAll(true);
        }
    }

    /**
     * An action to clear all checkboxes in the checkbox col
     */
    protected class ClearAllAction extends AbstractAction {
        SortFilterSelectionPanel parentRef = null;

        public ClearAllAction(SortFilterSelectionPanel parent) {
            super("Clear", clearAllIcon);
            this.parentRef = parent;
        }

        public void actionPerformed(ActionEvent e) {
            parentRef.selectAll(false);
        }
    }
    
    public void updateStatusLabel() {
        // Parthee's version:
        // String info = overallModel.getRowCount() + ROWS_STR + overallModel.getDataColumnCount() + COLUMNS_STR
        // + "[ " + overallModel.filterSortInfoString() + " ]";
        // Alison's version:
        String info = " " + overallModel.getRowCount() + ROWS_STR + overallModel.getDataColumnCount() + COLUMNS_STR
                + ": " + getSelectedIndexes().length + " Selected [" + overallModel.filterSortInfoString() + "]";
        statusLabel.setText(info);
        statusLabel.setToolTipText(info);
    } // updateStatusLabel()

    public void setModel(SortFilterSelectModel selectModel){
        this.selectModel = selectModel;
        setTableModel(selectModel);
        updateStatusLabel();            
    }
}
