package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.util.ClipBoardCopy;
import gov.epa.emissions.framework.client.data.DataHeaderPref;
import gov.epa.emissions.framework.client.data.DateRenderer;
import gov.epa.emissions.framework.client.data.DoubleRenderer;
import gov.epa.emissions.framework.client.data.PaginationPanel;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.ScrollableTable;
import gov.epa.emissions.framework.ui.TableColumnWidth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public class ViewerPanel extends JPanel implements ViewerPanelView {

    private EmfTableModel tableModel;

    private JPanel pageContainer;
    
    private JTable viewTable;
    
    private ViewablePage tableData;

    private PaginationPanel paginationPanel;

    private ScrollableTable table;

    private DataSortFilterPanelViewer sortFilterPanel;

    private EmfDataset dataset;

    private TableMetadata tableMetadata;
    
    private String rowFilter;

    private DoubleRenderer doubleRenderer; 
    
    private DataHeaderPref headerPref;
    
    private TableColumnHeadersViewer tableColumnHeaders;
    
    private Page page;

    public ViewerPanel(MessagePanel messagePanel, EmfDataset dataset, 
            TableMetadata tableMetadata, String filter) {

        super(new BorderLayout());

        this.doubleRenderer = new DoubleRenderer();
        this.doubleRenderer.setGroup(true);
        this.doubleRenderer.setDecimalPlaces(4);
        this.headerPref = new DataHeaderPref(false, dataset.getDatasetTypeName());

        this.tableMetadata = tableMetadata;
        this.dataset = dataset;
        this.rowFilter =filter;
        doLayout(messagePanel);
    }

    private void setBorder() {
        Border outer = BorderFactory.createEmptyBorder(10, 5, 10, 5);
        Border inner = BorderFactory.createLineBorder(Color.GRAY);
        CompoundBorder border = BorderFactory.createCompoundBorder(outer, inner);

        super.setBorder(border);
    }

    private void doLayout(MessagePanel messagePanel) {
        super.add(topPanel(messagePanel), BorderLayout.PAGE_START);

        pageContainer = new JPanel(new BorderLayout());
        super.add(pageContainer, BorderLayout.CENTER);
        setBorder();
    }

    private JPanel topPanel(MessagePanel messagePanel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(sortFilterPanel(messagePanel), BorderLayout.CENTER);
        panel.add(paginationPanel(messagePanel), BorderLayout.EAST);

        return panel;
    }

    private JPanel paginationPanel(MessagePanel messagePanel) {
        JPanel panel = new JPanel();
        paginationPanel = new PaginationPanel(messagePanel);
        panel.add(paginationPanel);

        return panel;
    }

    private DataSortFilterPanelViewer sortFilterPanel(MessagePanel messagePanel) {
        sortFilterPanel = new DataSortFilterPanelViewer(messagePanel, dataset, rowFilter, 
                this.doubleRenderer, this.headerPref);
        sortFilterPanel.setForEditor(false);
        return sortFilterPanel;
    }

    public void observe(TablePresenter presenter) {
        paginationPanel.init(presenter);
        sortFilterPanel.init(presenter);
    }

    public void display(Page page) {

        if (page != null) {
            this.page = page;
        }
        
        pageContainer.removeAll();
        paginationPanel.updateStatus(this.page);
        pageContainer.add(table(this.page), BorderLayout.CENTER);
        pageContainer.validate();
    }

    private ScrollableTable table(Page page) {
        if ( tableModel == null ){ 
            Font monospacedFont = new Font("Monospaced", Font.LAYOUT_NO_LIMIT_CONTEXT, 12);
            tableData = new ViewablePage(tableMetadata, page);
            tableModel = new EmfTableModel(tableData);
            viewTable = new JTable(tableModel);
            viewTableConfig(viewTable);
            table = new ScrollableTable(viewTable, monospacedFont);
            addCopyPasteClipBoard(viewTable);
            }
        else {
            tableData = new ViewablePage(tableMetadata, page);
            tableModel.refresh(tableData);
            viewTable.setModel(tableModel);
            tableColumnHeaders.displayClumns();
            table.repaint();
        }
        return table;
    }

    private void addCopyPasteClipBoard(JTable viewTable) {
        ClipBoardCopy clipBoardCopy = new ClipBoardCopy(viewTable);
        clipBoardCopy.registerCopyKeyStroke();
    }

    private void viewTableConfig(JTable viewTable) {
        this.tableColumnHeaders =new TableColumnHeadersViewer(viewTable, tableMetadata, headerPref);
        tableColumnHeaders.renderHeader();
        new TableColumnWidth(viewTable, tableMetadata).columnWidths();

        viewTable.setDefaultRenderer(Double.class, doubleRenderer);
        viewTable.setDefaultRenderer(Float.class, doubleRenderer);
        
        DateRenderer fcr = new DateRenderer();
        viewTable.setDefaultRenderer(java.sql.Timestamp.class, fcr);
        viewTable.setDefaultRenderer(java.sql.Date.class, fcr);
        viewTable.setDefaultRenderer(java.sql.Time.class, fcr);
        viewTable.setDefaultRenderer(java.util.Date.class, fcr);
        viewTable.setDefaultRenderer(java.util.Calendar.class, fcr);
        viewTable.setDefaultRenderer(java.util.GregorianCalendar.class, fcr);
        
        viewTable.repaint();
    }

    public void updateFilteredRecordsCount(int filtered) {
        paginationPanel.updateFilteredRecordsCount(filtered);
    }

    public void scrollToPageEnd() {
        table.selectLastRow();
    }

    public TableMetadata tableMetadata() {
        return tableMetadata;
    }

    public String getRowFilter(){
        return rowFilter;
    }

    public void saveColPref() {
        tableColumnHeaders.saveColPref();     
    }

}
