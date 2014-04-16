package gov.epa.emissions.framework.client.status;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class StatusWindow 
  extends ReusableInteralFrame 
  implements StatusView, RefreshObserver {

    private MessagePanel messagePanel;

    private StatusTableModel statusTableModel;

    private StatusPresenter presenter;

    private EmfConsole parent;
    
    public StatusWindow(EmfConsole parent, DesktopManager desktopManager) {
        super("Status", desktopManager);
        super.setName("status");
        this.parent = parent;

        position(parent);
        super.setContentPane(createLayout());

        super.setClosable(false);
        super.setMaximizable(false);
    }

    private JPanel createLayout() {
        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(createTable(), BorderLayout.CENTER);

        return layout;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel(new FlowLayout());
        messagePanel = new SingleLineMessagePanel();
        container.add(messagePanel);

        JButton clearButton = createClearButton();
        getRootPane().setDefaultButton(clearButton);
        container.add(clearButton);

        container.add(createRefreshButton());

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private JButton createClearButton() {
        JButton button = new JButton(trashIcon());
        button.setName("clear");
        button.setBorderPainted(false);
        button.setToolTipText("Clear the Status messages");

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClear();
            }
        });

        return button;
    }

    private Button createRefreshButton() {
        return new RefreshButton(this, "Refresh the Status messages", messagePanel);
    }

    private ImageIcon trashIcon() {
        return new ImageResources().trash("Clear Messages");
    }

    private JScrollPane createTable() {
        statusTableModel = new StatusTableModel();
        JTable 
        table = //new JTable(statusTableModel);
                new MultiLineTable(statusTableModel);
        table.setName("statusMessages");
        // FIXME: code put in for the demo
        //table.setRowHeight(50);
        //table.setDefaultRenderer(Object.class, new TextAreaTableCellRenderer());
        
        table.setCellSelectionEnabled(true);
        MultiLineCellRenderer multiLineCR = new MultiLineCellRenderer();
        table.getColumnModel().getColumn(0).setCellRenderer(multiLineCR);
        table.getColumnModel().getColumn(1).setCellRenderer(multiLineCR);
        table.getColumnModel().getColumn(2).setCellRenderer(multiLineCR);
        
        setColumnWidths(table.getColumnModel());
        table.setPreferredScrollableViewportSize(this.getSize());

        return new JScrollPane(table);
    }

    private void setColumnWidths(TableColumnModel model) {
        TableColumn message = model.getColumn(1);
        message.setPreferredWidth((int) (getWidth() * 0.75));
    }

    private void position(Container parent) {
        Dimension parentSize = parent.getSize();

        int width = (int) parentSize.getWidth() - 20;
        int height = 150;
        super.dimensions(width, height);
        super.setMinimumSize(new Dimension(width / 15, height));

        int x = 0;
        int y = (int) parentSize.getHeight() - height - 90;
        setLocation(x, y);
    }

    public void disposeView() {
        super.dispose();
        // don't try to unregister, since we didn't register with the desktopManager
    }

    public void display() {
        setVisible(true);
        // don't register through desktopmanager, since we don't want to close this window
    }

    public void update(Status[] statuses) {
        messagePanel.setMessage("Last Update : " + CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(new Date()), Color.GRAY);
        statusTableModel.refresh(statuses);

        super.revalidate();
    }

    public void notifyError(String message) {
        messagePanel.setError(message);
    }

    public void observe(StatusPresenter presenter) {
        this.presenter = presenter;
    }

    public void clear() {
        parent.clearMesagePanel();
        statusTableModel.clear();
    }

    public void doRefresh() {
        presenter.doRefresh();
    }
}
