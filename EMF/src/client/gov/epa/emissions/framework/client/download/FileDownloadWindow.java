package gov.epa.emissions.framework.client.download;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.AnalysisEngineTableApp;
import gov.epa.emissions.framework.client.status.MultiLineCellRenderer;
import gov.epa.emissions.framework.client.status.MultiLineTable;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.FileDownload;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.client.methods.ZeroCopyConsumer;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.params.CoreConnectionPNames;

public class FileDownloadWindow 
  extends ReusableInteralFrame 
  implements FileDownloadView, RefreshObserver {

    private MessagePanel messagePanel;

    private FileDownloadTableModel fileDownloadTableModel;

    private FileDownloadPresenter presenter;

    private EmfConsole parent;
    private JTable table = null;
    private Task task;

    private CountDownLatch latch = new CountDownLatch(2);
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    
    

    
    
    class Task extends SwingWorker<Void, Void> {
        private CountDownLatch latch;
        private FileDownload fileDownload;

        public Task(FileDownload fileDownload, CountDownLatch latch) {
            this.latch = latch;
            this.fileDownload = fileDownload;
        }

        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
            System.out.println("check to see if file download is already there");
            System.out.println("if overwrite flag is true then overwrite the file...");
            
            
            int progress = 0;
//            fileDownload.setProgress(0);

            String downloadURL = fileDownload.getUrl();
            String destinationFolder = "";
            HttpAsyncClient httpclient = null;
            File downloadedFile = null;
            try {
                destinationFolder = presenter.getDownloadFolder();
                downloadedFile = new File(destinationFolder + "//" + fileDownload.getFileName());
                
                //check to see if file download is already there
                if (downloadedFile.exists() && !fileDownload.getOverwrite()) {
                    fileDownload.setMessage("File has already been downloaded.");
                    return null;
                }
                
                httpclient = new DefaultHttpAsyncClient();
                //Initialize progress property.
                setProgress(0);
            } catch (IOReactorException e2) {
                // NOTE Auto-generated catch block
                e2.printStackTrace();
            } catch (EmfException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }

            System.out.println("start file download...");
            
            httpclient.getParams()
                .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 10000)
                .setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setIntParameter(CoreConnectionPNames.MIN_CHUNK_LIMIT, 8 * 1024);
            httpclient.start();
            try {
                  
                // Create a writable file channel
                final FileChannel wChannel = new FileOutputStream(downloadedFile, false).getChannel();

                ZeroCopyConsumer<File> consumer = new ZeroCopyConsumer<File>(downloadedFile) {

                    long position = 0;
//                        @Override
//                        protected void onResponseReceived(HttpResponse httpResponse) {
//                            
//                        }
                    
                    long downloadedBytes = 0;
                    int previousProgress = 0;
                    int progress = 0;

                    @Override
                    protected void onContentReceived(org.apache.http.nio.ContentDecoder decoder, org.apache.http.nio.IOControl ioctrl)  {
                        boolean allRead = false;
                        ByteBuffer t = ByteBuffer.allocate(2048);

                        while(!allRead) {
                          int count = 0;
                        try {
                            count = decoder.read(t);
                        } catch (IOException e1) {
                            // NOTE Auto-generated catch block
                            e1.printStackTrace();
                        }
                          if(count <= 0) {
                            allRead = true;
//                                System.out.println("Buffer reading is : " + decoder.isCompleted());
                          } else {
                              downloadedBytes += count;
//                                  System.out.println("onContentReceived downloadedBytes = " + downloadedBytes + ", count = " + count);
//                                  System.out.println("****** Number of Bytes read is : " + count);
                             t.flip();
                             try {
                                wChannel.write(t);
                            } catch (IOException e) {
                                // NOTE Auto-generated catch block
                                e.printStackTrace();
                            }
                            t.clear();
                          }
                          
                        }
                        if (fileDownload.getSize() != 0) 
                            progress = Math.min((int)((downloadedBytes * 100.0) / fileDownload.getSize()), 100);
                        if (previousProgress != progress) {
                            setProgress(progress);
//                                fileDownload.setProgress(progress);
//                                table.repaint();
                        }
                        previousProgress = progress;
                    }
                    
                    @Override
                    protected File process(
                            final HttpResponse response, 
                            final File file,
                            final ContentType contentType) throws Exception {
//                            System.out.println("process");
                        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                            throw new ClientProtocolException("Upload failed: " + response.getStatusLine());
                        }
                        //finalize progress bar download status
                        setProgress(100);
//                            fileDownload.setProgress(100);
//                            table.repaint();
                        return file;
                    }

                };
                  
                Future<File> future = httpclient.execute(HttpAsyncMethods.createGet(downloadURL), 
                        consumer, 
                        new FutureCallback<File>() {

                            public void cancelled() {
                                // NOTE Auto-generated method stub
                                System.out.println("cancelled");
                            }

                            public void completed(File arg0) {
                                // NOTE Auto-generated method stub
                                System.out.println("completed");
                            }

                            public void failed(Exception arg0) {
                                // NOTE Auto-generated method stub
                                System.out.println("failed");
                            }
                        });
                File result = future.get();
                System.out.println("Response file length: " + result.length());
                System.out.println("Response file length: " + result.getAbsolutePath());
                
//                    System.out.println("consumer.isDone() = " + consumer.isDone());
                System.out.println("Shutting down");
                wChannel.close();
            } catch (MalformedURLException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    httpclient.shutdown();
                } catch (InterruptedException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
            System.out.println("Done");
            return null;
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
//            Toolkit.getDefaultToolkit().beep();
//            startButton.setEnabled(true);
            setCursor(null); //turn off the wait cursor
            latch.countDown();
//            taskOutput.append("Done!\n");
        }
    }

    public FileDownloadWindow(EmfConsole parent, DesktopManager desktopManager) {
        super("Downloads", desktopManager);
        super.setName("Downloads");
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
        button.setToolTipText("Removes downloads from the list");

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClear();
            }
        });

        return button;
    }

    private Button createRefreshButton() {
        return new RefreshButton(this, "Refresh the Downloaded Files", messagePanel);
    }

    private ImageIcon trashIcon() {
        return new ImageResources().trash("Clear Downloaded Files");
    }

    private JScrollPane createTable() {

        fileDownloadTableModel = new FileDownloadTableModel();
        table = new JTable(fileDownloadTableModel);
//            new MultiLineTable(fileDownloadTableModel);
        table.setName("fileDownloads");
        // FIXME: code put in for the demo
//        table.setRowHeight(50);
        //table.setDefaultRenderer(Object.class, new TextAreaTableCellRenderer());
        
        table.setCellSelectionEnabled(true);
//        MultiLineCellRenderer multiLineCR = new MultiLineCellRenderer();
        FileDownloadTableCellRenderer progressBarTableCellRenderer = new FileDownloadTableCellRenderer();
//        table.getColumnModel().getColumn(0).setCellRenderer(multiLineCR);
//        table.getColumnModel().getColumn(1).setCellRenderer(multiLineCR);
//        table.getColumnModel().getColumn(2).setCellRenderer(multiLineCR);
        table.getColumnModel().getColumn(0).setCellRenderer(progressBarTableCellRenderer);
        
        table.addMouseListener(new MouseListener() {
            
            public void mouseReleased(MouseEvent e) {
                int r = table.rowAtPoint(e.getPoint());
                int c = table.columnAtPoint(e.getPoint());
                if (r >= 0 && r < table.getRowCount()
                        && c >= 0 && c < table.getColumnCount()) {
                    table.setRowSelectionInterval(r, r);
                    table.setColumnSelectionInterval(c, c);
                } else {
                    table.clearSelection();
                }
                checkForPopup( e );
            }
            
            public void mousePressed(MouseEvent e) {
                // NOTE Auto-generated method stub
                
            }
            
            public void mouseExited(MouseEvent e) {
                // NOTE Auto-generated method stub
                
            }
            
            public void mouseEntered(MouseEvent e) {
                // NOTE Auto-generated method stub
                
            }
            
            public void mouseClicked(MouseEvent e) {
                // NOTE Auto-generated method stub
                
            }
        });
        
//        setColumnWidths(table.getColumnModel());
//        table.setPreferredScrollableViewportSize(this.getSize());

        return new JScrollPane(table);
    }

    private void checkForPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            showPopup(e.getPoint());//table.columnAtPoint(e.getPoint()), table.rowAtPoint(e.getPoint()));
        }
    }
  
    /**
     *  Show a hidden column in the table.
     *
     *  @param  table        the table to which the column is added
     *  @param  columnName   the column name from the TableModel
     *                       of the column to be added
     */
    public void showPopup(final Point point) //int column, int row)
    {

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
 
        //int columns = table.getModel().getColumnCount();
//        JMenuItem[] items = new JMenuItem[3];
//        JMenuItem item = new JMenuItem( "Download");
//        item.addActionListener(new ActionListener() {
//            
//            public void actionPerformed(ActionEvent e) {
//                final int selectedRow = table.getSelectedRow();
//                
//                System.out.println("selectedRow = " + selectedRow);
////                downloadFile(e.getSource());
//                // NOTE Auto-generated method stub
////                System.out.println("Popup Download click"  + e.getSource());
////                System.out.println(table.getModel().getValueAt(table.getSelectedRow(), 1));
////                System.out.println(((FileDownloadTableModel)(table.getModel())).getSource(table.getSelectedRow()));
//                final FileDownload fileDownload = (FileDownload) ((FileDownloadTableModel)(table.getModel())).getSource(selectedRow);
////                System.out.println(((FileDownloadTableModel)(table.getModel())).getSource(table.getSelectedRow()) instanceof FileDownload);
//                
//                //see if file is already downloaded...
//                if (fileDownload.getProgress() == 100) {
//                    messagePanel.setError("File is already downloaded.");
//                    return;
//                }
//                    
//                
//                fileDownload.setProgress(0);
//                
//                fileDownloadTableModel.setValueAt(fileDownload, selectedRow, 0);
////                presenter.doRefresh();
////                ((FileDownloadTableCellRenderer)table.getCellRenderer(selectedRow, 0)).setValue(fileDownload);
//
//                //Instances of javax.swing.SwingWorker are not reusuable, so
//                //we create new instances as needed.
//                task = new Task(fileDownload, latch);
//                task.addPropertyChangeListener(new PropertyChangeListener() {
//                    
//                    public void propertyChange(PropertyChangeEvent evt) {
////                        System.out.println(evt.getPropertyName() + " " + evt.getNewValue());
//                        if ("progress" == evt.getPropertyName()) {
//                            int progress = (Integer) evt.getNewValue();
//                            fileDownload.setProgress(progress);
//                            table.repaint();
////                            fileDownloadTableModel.setValueAt(fileDownload, selectedRow, 0);
////                            ((FileDownloadTableCellRenderer)table.getCellRenderer(selectedRow, 0)).setValue(fileDownload);
////                            progressBar.setValue(progress);
////                            taskOutput.append(String.format(
////                                    "Completed %d%% of task.\n", task.getProgress()));
//                        } 
//                    }
//                });
////                task.execute();
//                executor.execute(task);
//                fileDownload.setProgress(0);
//                
//                System.out.println("start new swingworkertask");  
//            }
//        });
//        popup.add( item );
//        //a group of radio button menu items
//        popup.addSeparator();
        JMenuItem item = new JMenuItem( "View in Analysis Engine");
        item.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        final int selectedRow = table.getSelectedRow();
                        
                        System.out.println("selectedRow = " + selectedRow);
//                        downloadFile(e.getSource());
                        // NOTE Auto-generated method stub
//                        System.out.println("Popup Download click"  + e.getSource());
//                        System.out.println(table.getModel().getValueAt(table.getSelectedRow(), 1));
//                        System.out.println(((FileDownloadTableModel)(table.getModel())).getSource(table.getSelectedRow()));
                        FileDownload fileDownload = (FileDownload) ((FileDownloadTableModel)(table.getModel())).getSource(selectedRow);
                        if (fileDownload.getProgress() == 100) {
                            AnalysisEngineTableApp app = new AnalysisEngineTableApp("View Exported File: " + fileDownload.getFileName(), new Dimension(500, 500), desktopManager, parent);
                            try {
                                app.display(new String[] { presenter.getDownloadFolder() + "/" + fileDownload.getFileName() });
                            } catch (EmfException ex) {
                                // NOTE Auto-generated catch block
                                messagePanel.setError(ex.getMessage());
                            }
                        } else {
                            // NOTE Auto-generated catch block
                            messagePanel.setError("File has not been fully downloaded.");
                        }
                    }
                });
            }
        });
        popup.add( item );
        item = new JMenuItem( "Open Containing Folder");
        item.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        // NOTE Auto-generated method stub
                        System.out.println("Popup Open Containing Folder click");
                        Desktop desktop = null;
                        if (Desktop.isDesktopSupported()) {
                            desktop = Desktop.getDesktop();
                        }

                        try {
                            desktop.open(new File(presenter.getDownloadFolder()));
                        } catch (IOException ex) {
                            messagePanel.setError(ex.getMessage());
                        }
                    }
                });
                
            }
        });
        popup.add( item );

        //        for (int i = 0; i < items.length; i++)
//        {
//            if (items[i] != null)
//            {
//                popup.add( items[i] );
//            }
//        }
 
        //  Display the popup below the click point
        popup.show(table.getComponentAt(point), point.x, point.y);
    }
 
    private void setColumnWidths(TableColumnModel model) {
        TableColumn message = model.getColumn(0);
        message.setPreferredWidth((int) (getWidth() * 0.75));
    }

    private void position(Container parent) {
        Dimension parentSize = parent.getSize();

        int width = (int) parentSize.getWidth() * 7 / 16 - 20;
        int height = 250;
        super.dimensions(width, height);
        super.setMinimumSize(new Dimension(width / 15, height));

        int x = 0;
        x = (int) parentSize.getWidth() - width - 20;
        int y = (int) parentSize.getHeight() - height - 90 - 150;
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

    public void update(FileDownload[] fileDownloads) {
        messagePanel.setMessage("Last Update : " + CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(new Date()), Color.GRAY);
        fileDownloadTableModel.refresh(fileDownloads);
        for (int i = 0; i < fileDownloadTableModel.getRowCount(); i++) {
//            System.out.println(i);
            table.setRowHeight(i, 50);
        }

        for (final FileDownload fileDownload : fileDownloads) {
            if (fileDownload.getProgress() < 100 && !fileDownload.getRead()) {
                fileDownload.setProgress(0);
                
                //Instances of javax.swing.SwingWorker are not reusuable, so
                //we create new instances as needed.
                task = new Task(fileDownload, latch);
                task.addPropertyChangeListener(new PropertyChangeListener() {
                    
                    public void propertyChange(PropertyChangeEvent evt) {
        //                System.out.println(evt.getPropertyName() + " " + evt.getNewValue());
                        if ("progress" == evt.getPropertyName()) {
                            int progress = (Integer) evt.getNewValue();
                            fileDownload.setProgress(progress);
                            table.repaint();
                        } 
                    }
                });
                executor.execute(task);
                fileDownload.setProgress(0);
                try {
                    presenter.markFileDownloadRead(fileDownload);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }        
        }
        
        
        if (fileDownloads.length > 0)
            this.toFront();
        super.revalidate();
    }

    public void notifyError(String message) {
        messagePanel.setError(message);
    }

    public void observe(FileDownloadPresenter presenter) {
        this.presenter = presenter;
    }

    public void clear() {
        parent.clearMesagePanel();
        fileDownloadTableModel.clear();
    }

    public void doRefresh() {
        presenter.doRefresh();
    }
}
