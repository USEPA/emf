package gov.epa.emissions.framework.client.status;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.swingworker.GenericSwingWorker;
import gov.epa.emissions.framework.services.EmfException;
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
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class StatusWindow 
  extends DisposableInteralFrame 
  implements StatusView, RefreshObserver {

    private MessagePanel messagePanel;

    private StatusTableModel statusTableModel;

    private StatusPresenter presenter;

    private EmfConsole parent;
    
    private User user;

    public StatusWindow(EmfConsole parent, DesktopManager desktopManager, User user) {
        super("Status", desktopManager);
        super.setName("status");
        this.user = user;
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
        messagePanel = new SingleLineMessagePanel(false);
        container.add(messagePanel);

        JButton clearButton = createClearButton();
        getRootPane().setDefaultButton(clearButton);
        container.add(clearButton);

        container.add(createRefreshButton());

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private JButton createClearButton() {
        JButton button = new JButton("Clear Messages");
        button.setIcon(trashIcon());
        button.setBorderPainted(false);
        button.setToolTipText("Clear the Status messages");
        button.setMnemonic(KeyEvent.VK_C);

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
        table.getAccessibleContext().setAccessibleName("List of status messages");
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
        update();
    }

    public void update() {
        new GenericSwingWorker<Status[]>(getContentPane(), messagePanel) {

            @Override
            public Status[] doInBackground() throws EmfException {
                return presenter.getStatuses(user.getUsername());
            }

            @Override
            public void done() {
                try {
                    //make sure something didn't happen
                    Status[] statuses = get();
                    
                    messagePanel.setMessage("Last Update : " + CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(new Date()), Color.GRAY);
                    statusTableModel.refresh(statuses);

                    parentContainer.invalidate();
                    parentContainer.validate();

                } catch (InterruptedException e1) {
                    if (e1.getMessage().length() > 100)
                        messagePanel.setError(e1.getMessage().substring(0, 100) + "...");
                    else
                        messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
                    if (e1.getMessage().length() > 100)
                        messagePanel.setError(e1.getMessage().substring(0, 100) + "...");
                    else
                        messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } finally {
                    super.finalize();            }
            }
        }.execute();
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

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }
}
