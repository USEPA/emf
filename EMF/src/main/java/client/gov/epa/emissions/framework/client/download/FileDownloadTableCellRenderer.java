package gov.epa.emissions.framework.client.download;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.basic.FileDownload;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;


public class FileDownloadTableCellRenderer extends JPanel implements TableCellRenderer {
    
    protected static Border noFocusBorder; 

    private Color unselectedForeground; 
    private Color unselectedBackground; 
    private JProgressBar progressBar;
    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    private JLabel nameLabel;
    private FileDownload fileDownload;

    private JLabel sizeDateLabel;

    private DecimalFormat formatter;

    private JLabel messageLabel;
   
    public FileDownload getFileDownload() {
        return fileDownload;
    }

    public void setFileDownload(FileDownload fileDownload) {
        this.fileDownload = fileDownload;
    }

    public FileDownloadTableCellRenderer(FileDownload fileDownload){
        this();
        nameLabel.setText(fileDownload.getUrl());
        this.fileDownload = fileDownload;
    }
    
    public FileDownloadTableCellRenderer(){
        super();
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(true);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(true);
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(true);
        
//        this.fileDownload = fileDownload;
        this.setLayout(new BorderLayout());
        
        nameLabel = new JLabel();
        leftPanel.add(nameLabel, BorderLayout.NORTH);
        sizeDateLabel = new JLabel();
        leftPanel.add(sizeDateLabel, BorderLayout.SOUTH);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setSize(new Dimension(50, 40));
        rightPanel.add(progressBar, BorderLayout.SOUTH);
        messageLabel = new JLabel();
        rightPanel.add(messageLabel, BorderLayout.NORTH);
        noFocusBorder = new EmptyBorder(1, 2, 1, 2);
//        setLineWrap(true);
//        setWrapStyleWord(true);
        setOpaque(true);
        setBorder(noFocusBorder);
//        setPreferredSize(new Dimension(250, 80));
        this.add(leftPanel, BorderLayout.WEST);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(rightPanel, BorderLayout.EAST);
        this.formatter = new DecimalFormat("#,###");
    }
    
    public void setForeground(Color c) {
        super.setForeground(c); 
        unselectedForeground = c; 
    }

    public void setBackground(Color c) {
        super.setBackground(c); 
        unselectedBackground = c; 
    }

    public void updateUI() {
        super.updateUI(); 
        setForeground(null);
        setBackground(null);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, 
            int row, int column) {

        if (isSelected) {
            super.setForeground(table.getSelectionForeground());
            super.setBackground(table.getSelectionBackground());
        }
        else {
            super.setForeground((unselectedForeground != null) ? unselectedForeground 
                    : table.getForeground());
            super.setBackground((unselectedBackground != null) ? unselectedBackground 
                    : table.getBackground());
        }

        setFont(table.getFont());

        if (hasFocus) {
            setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
            if (table.isCellEditable(row, column)) {
                super.setForeground( UIManager.getColor("Table.focusCellForeground") );
                super.setBackground( UIManager.getColor("Table.focusCellBackground") );
            }
        } else {
            setBorder(noFocusBorder);
        }
        FileDownload fileDownload = (FileDownload)value;
        nameLabel.setText(fileDownload.getFileName());
        messageLabel.setText(fileDownload.getMessage());
        //adding +1 gives a number closer to what windows explorer shows????
        sizeDateLabel.setText(formatter.format((Math.ceil((fileDownload.getSize() / 1024))) + 1) + " kB -- " + CustomDateFormat.format_h_m_a_MMM_DD(fileDownload.getTimestamp()));
        //show if hidden
//        for (int i = 0; i < table.getModel().getRowCount(); i++) {
//            FileDownload fileDownload2 = (FileDownload)table.getModel().getValueAt(i, 0);
//            System.out.println("table.getModel(): " + fileDownload2.getId() +  " " + fileDownload2.getUrl() +  " " + fileDownload2.getProgress());
//        }
//        System.out.println(fileDownload.getId() +  " " + fileDownload.getUrl() +  " " + fileDownload.getProgress() +  " " + progressBar.isVisible());
//        if (fileDownload.getProgress() > 0 && !progressBar.isVisible())
        if (fileDownload.getProgress() != null && fileDownload.getProgress() > 0)
            progressBar.setVisible(true);
        else
            progressBar.setVisible(false);
        progressBar.setValue(fileDownload.getProgress());
        setValue(value); 
//        table.setRowHeight(row, 200);
        return this;
    }

    protected void setValue(Object value) {
//        System.out.println(value);
//        setValue((value == null || value.equals("")) ? 0 : Integer.parseInt(value + ""));
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getLabel() {
        return nameLabel;
    }

}
