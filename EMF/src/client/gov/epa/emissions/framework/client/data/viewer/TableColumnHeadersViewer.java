package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.client.data.DataHeaderPref;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class TableColumnHeadersViewer implements MouseListener, ActionListener {
    
    //private static final String HIDDEN_COLUMN = "HiddenColumn:";
    private static final String COLUMN_ORDER  = "ColumnOrder:";
    private static final String COLUMN_HIDE  = "ColumnHide:";
    private JTable table;
    private TableMetadata tableMetadata;
    private int sColumn;
    private DataHeaderPref headerPref;
    private ArrayList<TableColumn>  allColumns;
    private ArrayList<TableColumn>  orderList;
    private ArrayList<TableColumn>  hideList;
    private Boolean resetView;
    private Boolean firstTime = true;

    public TableColumnHeadersViewer(JTable table, TableMetadata tableMetadata, DataHeaderPref headerPref){
        this.table = table;
        this.tableMetadata = tableMetadata;
        this.headerPref = headerPref;
        this.allColumns = getModelColumns();
        this.resetView = headerPref.getResetView();
    }
    
    private ArrayList<TableColumn> getModelColumns(){
        TableColumnModel columnModel = table.getColumnModel();
        ArrayList<TableColumn> columns = new ArrayList<TableColumn>();
        int count = columnModel.getColumnCount();
        for (int i =0; i < count; i++){ 
            columns.add(columnModel.getColumn(i));
        }       
        return columns;
//        List<GeoRegion> regions = new ArrayList<GeoRegion>();
//        regions.addAll(Arrays.asList(regionsWidget.getRegions()));
        
//        values.toArray(new Case[0]);
    }
    
    private ArrayList<TableColumn> getColumns(String colType){      
        return (ArrayList)table.getClientProperty(colType);
    }
    
    public void  renderHeader() {
        JTableHeader tableHeader = new JTableHeader(table.getColumnModel());
        tableHeader.setBackground(UIManager.getDefaults().getColor("TableHeader.background"));
        tableHeader.setDefaultRenderer(new TableHeaderRenderer(tableHeader, tableMetadata));
        table.setTableHeader(tableHeader);
        table.removeMouseListener(this);
        table.getTableHeader().addMouseListener( this );
        initiateColumns();
    }

    public class TableHeaderRenderer extends JPanel implements TableCellRenderer {

        private JTextPane textPane;

        private TableMetadata metadata;

        public TableHeaderRenderer(JTableHeader tableHeader, TableMetadata tableMetadata) {
            this.textPane = new JTextPane();
            this.metadata = tableMetadata;
            textPaneSettings(tableHeader);
            setLayout(new BorderLayout());
            add(textPane);
        }

        private void textPaneSettings(JTableHeader tableHeader) {
            textPane.setForeground(tableHeader.getForeground());
            textPane.setBackground(tableHeader.getBackground());
            textPane.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            StyledDocument doc = textPane.getStyledDocument();
            MutableAttributeSet standard = new SimpleAttributeSet();
            Font font = tableHeader.getFont();
            StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
            StyleConstants.setFontFamily(standard, font.getFamily());
            StyleConstants.setFontSize(standard,font.getSize());
            doc.setParagraphAttributes(0, 0, standard, true);
            
            textPane.setFont(font);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            textPane.setText(headerText(value));
            return this;
        }

        private String headerText(Object value) {
            String header = ((value == null) ? "" : value.toString());
            return header.toUpperCase() + type(header);
        }

        private String type(String header) {
            ColumnMetaData data = metadata.columnMetadata(header);
            if(data==null){
                return "\n";
            }
            String type = parse(data.getType());
            if(!type.equalsIgnoreCase("String")){
                return "\n"+type;
            }
            int length = data.getSize();
            String size = (length==-1 || length > 500)?"*":""+length;
            return "\n" + type + "(" + size + ")";
        }

        private String parse(String type) {
            int index = type.lastIndexOf('.');
            return type.substring(index + 1);
        }
    }
    private void initiateColumns(){
        String[] hideCols = headerPref.getHCols();
        String[] orderCols = headerPref.getOCols();
        hideList = new ArrayList();
        table.putClientProperty(COLUMN_HIDE, hideList);
        orderList = new ArrayList();
        table.putClientProperty(COLUMN_ORDER, orderList);
        if ( hideCols != null && hideCols.length > 0){
            for ( int i=0; i< hideCols.length; i++){
                TableColumn col1 = containColumn(allColumns, hideCols[i]); //save it when exist
                TableColumn col2 = containColumn(hideList, orderCols[i]);
                if (col1 !=null && col2 == null ) {
                    hideList.add(col1);
                }
            }
        }
        if ( orderCols != null  && orderCols.length > 0){
            for ( int i=0; i< orderCols.length; i++){
                TableColumn col1 = containColumn(allColumns, orderCols[i]);
                TableColumn col2 = containColumn(orderList, orderCols[i]);
                TableColumn col3 = containColumn(hideList, orderCols[i]);
                if (col1 !=null && col2 == null && col3 == null) {
                    orderList.add(col1);
                }
            }
        }
        displayClumns();
    }
    
    public void displayClumns(){
        this.resetView = headerPref.getResetView();
         
     // reset orderList from table model if not first time
        if ( firstTime ) 
            firstTime = false;
        else
            resetList();
        
        removeAllColumns();
        
        // reset table columns from orderList
        if ( ! resetView ){
            if ( (orderList == null || orderList.size() ==0 ) 
                    && (hideList == null || hideList.size() ==0 ) ) {
                showAllColumns();
                resetList();
                return;
            }
            // Add columns in column order first, then add columns missed
            ListIterator<TableColumn> it = orderList.listIterator();
            while (it.hasNext())
            {
                TableColumn column = it.next();
                table.getColumnModel().addColumn( column );
            } 
            
            int columnCount = allColumns.size();
            
            for (int i = 0; i < columnCount; i++)
            {    
                TableColumn column = allColumns.get(i);
                if ( (! hideList.contains(column)) && (! orderList.contains(column)) )
                table.getColumnModel().addColumn( column );
            } 
        }
        // Reset all table columns
        else{
             showAllColumns();
        }
        resetList();
    }
    
    private void removeAllColumns(){
        TableColumnModel columnModel = table.getColumnModel();
        
        while (columnModel.getColumnCount() > 0) {
            columnModel.removeColumn(columnModel.getColumn(0));
        }
    }
    
    private void showAllColumns(){
         
        int columnCount = allColumns.size();
        
        for (int i = 0; i < columnCount; i++)
        {   // remove all columns
            TableColumn column = allColumns.get(i);
            table.addColumn( column );
        } 
//        table.repaint();
    }
     
    private TableColumn containColumn(ArrayList<TableColumn> cols, String colName){
        //TableColumnModel columnModel = table.getColumnModel();
        int count = cols.size();
        for (int i =0; i < count; i++){ 
            if (cols.get(i).getHeaderValue().equals(colName))
                return cols.get(i);
        }
        return null;
    }
    
    private void resetList() {
        TableColumnModel columnModel = table.getColumnModel();
        int count = columnModel.getColumnCount();
        orderList.clear();
        hideList.clear();
        for (int i =0; i < count; i++){ 
            orderList.add(columnModel.getColumn(i));   
        }
        
        count = allColumns.size();         
        for (int i =0; i < count; i++){
            if ( ! orderList.contains(allColumns.get(i)) ){
                hideList.add(allColumns.get(i));
            }
        }
    }
    /**
     *  Hide a column from view in the table.
     *
     *  @param  table        the table from which the column is removed
     *  @param  modelColumn  the column index from the TableModel
     *                       of the column to be removed
     */
    private void hideColumn(int modelColumn)
    {    
        //int viewColumn = table.convertColumnIndexToView( modelColumn );
        //int viewColumn = modelColumn;
        
        TableColumn column = table.getColumnModel().getColumn(modelColumn);
        table.getColumnModel().removeColumn( column );       
        resetList();
    }
  
 
    //  Implement the MouseListener
 
    public void mousePressed(MouseEvent e)
    {
        checkForPopup( e );
    }
  
    public void mouseReleased(MouseEvent e)
    {
        checkForPopup( e );
    }
  
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
  
    private void checkForPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JTableHeader header = (JTableHeader)e.getComponent();
            sColumn = header.columnAtPoint( e.getPoint() );
            showPopup();
        }
    }
  
    /**
     *  Show a hidden column in the table.
     *
     *  @param  table        the table to which the column is added
     *  @param  columnName   the column name from the TableModel
     *                       of the column to be added
     */
    public void showPopup()
    {
        //int columns = table.getModel().getColumnCount();
        JMenuItem[] items = new JMenuItem[1];
        JMenuItem item = new JMenuItem( "Hide");
        item.addActionListener(this);
        items[0] = item;

        JPopupMenu popup = new JPopupMenu()
        {
            public void setSelected(Component sel)
            {
                int index = getComponentIndex( sel );
                getSelectionModel().setSelectedIndex(index);
                final MenuElement me[] = new MenuElement[2];
                me[0]=(MenuElement)this;
                me[1]=getSubElements()[index];
 
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        MenuSelectionManager.defaultManager()
                            .setSelectedPath(me);
                    }
                });
            }
        };
 
        for (int i = 0; i < items.length; i++)
        {
            if (items[i] != null)
            {
                popup.add( items[i] );
            }
        }
 
        //  Display the popup below the TableHeader
 
        JTableHeader header = table.getTableHeader();
        Rectangle r = header.getHeaderRect( sColumn );
        popup.show(header, r.x, r.height);
        //int modelColumn = table.convertColumnIndexToModel( tableColumn );
        popup.setSelected(item);
    }
 
    /**
     *  Responsible for processing the ActionEvent. A column will either be
     *  added to the table or removed from the table depending on the state
     *  of the menu item that was clicked.
     *
     * @param event the ActionEvent.
     */
    public void actionPerformed(ActionEvent event)
    {
//        JMenuItem item = (JMenuItem)event.getSource();
//        JPopupMenu popup = (JPopupMenu)item.getParent();
//        JTableHeader header = (JTableHeader)popup.getInvoker();
//        JTable table = header.getTable();
        hideColumn(sColumn);      
    }
    
    public void saveColPref() {
        resetList();      
        headerPref.setOrderCols(convertColNames(orderList));
        headerPref.setHCols(convertColNames(hideList));       
    }
    
    private String[] convertColNames(ArrayList<TableColumn>  colList){
        if ( colList == null || colList.size() ==0 )
            return null;
        
        ArrayList<String> nameList = new ArrayList<String>();
        int count= colList.size();
        for ( int i=0 ; i< count; i++ )
            nameList.add((String)colList.get(i).getHeaderValue());
        return nameList.toArray(new String[0]);
    }
    
}
