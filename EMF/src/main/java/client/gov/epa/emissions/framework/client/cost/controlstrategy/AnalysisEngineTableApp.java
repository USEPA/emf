package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.csv.CSVFileReader;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.googleearth.kml.gui.PointSourceGeneratorFrame;
import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.SpecialTableModel;
import gov.epa.mims.analysisengine.table.TablePanel;
import gov.epa.mims.analysisengine.table.TextDialog;
import gov.epa.mims.analysisengine.table.io.FileImportGUI;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

public class AnalysisEngineTableApp extends DisposableInteralFrame
// implements google earth data file producer
{

    private JTabbedPane mainTabbedPane;
    
    private JMenuItem googleEarth;

    private JMenuItem closeMenuItem;
    
    private EmfConsole parentConsole;

    private Dimension dimension;
    
    private String title; 

    private static int counter = 0;

    public AnalysisEngineTableApp(String controlStrategyName, Dimension dimension, DesktopManager desktopManager,
            EmfConsole parentConsole) {
        super(controlStrategyName + counter++, dimension, desktopManager);
        this.dimension = dimension;
        this.parentConsole = parentConsole;
        this.title = controlStrategyName.substring(controlStrategyName.indexOf(":")==-1? 0: controlStrategyName.indexOf(":")+1);
    }

    public void display(String[] fileNames) {
        try {
            setLayout(fileNames);
            super.display();
        } catch (Exception e) {
            for (int i = 0; i< fileNames.length; i++)
            {
                System.out.println("File "+i+"= "+fileNames[i]);
            }
            if (fileNames.length==0) System.out.println("No files provided");
            e.printStackTrace();
            System.out.println("Error displaying analysis engine table.");
        }
    }

    private void setLayout(String[] fileNames) throws Exception {
        JPanel menuPanel = createMenuPanel(fileNames);
        mainTabbedPane = new JTabbedPane();
        String[] tabNames = createTabNames(1, 40, fileNames);
        importFiles(fileNames, tabNames, FileImportGUI.GENERIC_FILE, ",");
//        Container contentPane = getContentPane();
//        contentPane.setLayout(new BorderLayout());
//        contentPane.add(mainTabbedPane);
        JPanel componentsPanel = new JPanel();

        componentsPanel.setBorder(BorderFactory.createEtchedBorder());
        componentsPanel.setLayout(new BorderLayout());
        componentsPanel.add(mainTabbedPane);
        
        
        JPanel mainPanel = new JPanel();

        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setPreferredSize(new Dimension(700, 600));
        menuPanel.setMaximumSize(new Dimension(4000, 25));
        mainPanel.add(menuPanel);
        mainPanel.add(componentsPanel);
        Container container = getContentPane();
        container.add(mainPanel);
        pack();
    }

    private String[] createTabNames(int startIndex, int endIndex, String[] fileNames) {
        String[] tabNames = null;
        if (fileNames != null) {
            // switch the indices
            if (startIndex > endIndex) {
                int tempStartIndex = startIndex;
                startIndex = endIndex;
                endIndex = tempStartIndex;
            }// if

            startIndex--; // deducting 1 since user values starts from 1
            // but index starts from 0;
            tabNames = new String[fileNames.length];
            for (int i = 0; i < fileNames.length; i++) {
                File file = new File(fileNames[i]);
                String fileNameWithExt = file.getName();
                int wholeLength = fileNameWithExt.length();
                String fileNameWOExt = fileNameWithExt.substring(0, wholeLength - 4);
                int correctedEndIndex = -1;

                if (startIndex > fileNameWOExt.length()) {
                    startIndex = FileImportGUI.START_TAB_NAME_INDEX - 1; // set to 0
                }// if

                if (endIndex > fileNameWOExt.length()) {
                    correctedEndIndex = fileNameWOExt.length();
                } // if
                else {
                    correctedEndIndex = endIndex;
                }// else
                String tabName = fileNameWOExt.substring(startIndex, correctedEndIndex);
                tabNames[i] = tabName;
            }// for(i)
        }// if(selectedFiles != null)
        return tabNames;
    }// createTabNames()

    private void importFiles(String[] fileNames, String[] tabNames, String fileType, String delimiter) throws Exception {
        boolean allSuccessful = true; // a flag to check whether all the file imported sucessfuly
        ArrayList<String> importFileStatus = new ArrayList<String>();
        ArrayList<String> warningWindow = new ArrayList<String>();

        for (int i = 0; i < fileNames.length; i++) {
            SpecialTableModel model = null;
            try {
                model = createSpecialTableModel(fileNames[i], delimiter, warningWindow);
                insertIntoTabbedPane(model, fileNames[i], tabNames[i], fileType);
                importFileStatus.add("Success: " + fileNames[i]);
            } // try
            catch (Exception e) {
                e.printStackTrace();
                allSuccessful = false;
                if (e.getMessage() == null) {
                    importFileStatus.add("FAILURE: " + fileNames[i] + "\n");
                    importFileStatus.add(get50Lines(fileNames[i]));
                } else {
                    importFileStatus.add("FAILURE: " + fileNames[i] + ":\n" + e.getMessage());
                    importFileStatus.add(get50Lines(fileNames[i]));
                }
                throw e;
            }// catch
        }
        importStatus(allSuccessful, importFileStatus, warningWindow);
    }

    private SpecialTableModel createSpecialTableModel(String file, String delimiter, ArrayList<String> warningWindow)
            throws Exception {
        CSVFileReader reader = null;
        File csvFile = new File(file);

        if (csvFile.length() > 200000000)
            throw new EmfException("File is too big to open (> 200MB).");

        if (delimiter.equals(","))
            reader = new CSVFileReader(csvFile);
        else{
            throw new EmfException("File " + csvFile + " is not csv format. Sorry, I cannot read it.");       
        }
            
        String[] rowHeader = new String[0];
        String fileHeader = ((reader.getHeader() == null) ? "" : reader.headerToString());
        String[][] colHeader = new String[1][];
        colHeader[0] = reader.getCols();

        
        Class<?>[] colClasses = getColumnClass(reader.comments(), reader.getCols().length);

        ArrayList<ArrayList<?>> tableData = getTableData(reader, colClasses);
        reader.close();
        return new SpecialTableModel(fileHeader, rowHeader, colHeader, tableData, "", colClasses);
    }

    
    private ArrayList<ArrayList<?>> getTableData(CSVFileReader reader, Class<?>[] colClasses) throws Exception {
        ArrayList<ArrayList<?>> tableData = new ArrayList<ArrayList<?>>();
        int numOfCols = colClasses.length;

        Record record = reader.read();

        while (!record.isEnd()) {
            String[] tokens = record.getTokens();
            int numOfTokens = tokens.length;
            ArrayList rowData = new ArrayList();

            for (int j = 0; j < numOfCols; j++) {
                String temp = null; 
                
                if ( j < numOfTokens && tokens[j] != null)
                    temp = tokens[j].trim();
                
                if (colClasses[j].equals(String.class))
                    rowData.add(temp);
                else if (colClasses[j].equals(Double.class))
                    rowData.add((temp == null || temp.isEmpty())? Double.NaN : new Double(temp));
                else if (colClasses[j].equals(Boolean.class)) {
                    Boolean b = null;
                    if (temp != null && !temp.isEmpty()) {
                        if ( temp.equalsIgnoreCase("true") || temp.equalsIgnoreCase("t"))
                            b= new Boolean(true);
                        else if (temp.equalsIgnoreCase("false") || temp.equalsIgnoreCase("f"))
                            b = new Boolean(false);
                    }
                    rowData.add(b); //(temp == null || temp.isEmpty())? null : new Boolean(temp));
                }
                else if (colClasses[j].equals(Date.class))
                    rowData.add((temp == null || temp.isEmpty())? null : CustomDateFormat.format_yyyy_MM_dd_HHmmss(temp));
                else if (colClasses[j].equals(Integer.class))
                    rowData.add((temp == null || temp.isEmpty())? null : new Integer(temp));
                else
                    rowData.add(tokens[j]);
            }

            tableData.add(rowData);
            record = reader.read();
        }

        return tableData;
    }

    private Class<?>[] getColumnClass(List<String> comments, int numOfCols) throws EmfException {
        List<Class<?>> colClasses = new ArrayList<Class<?>>();
        List<String> colTypes = new ArrayList<String>();

        for (Iterator<String> iter = comments.iterator(); iter.hasNext();) {
            String line = iter.next().toUpperCase();

            if (line.contains("#COLUMN_TYPES=")) {
                int index = line.indexOf("=");

                if (index < 0)
                    throw new EmfException(
                            "Column types line format is not correct. The correct format is: #TYPES=[type_1] [type_2] ... [type_n]");

                StringTokenizer st = new StringTokenizer(line.substring(++index), "|");

                while (st.hasMoreTokens())
                    colTypes.add(st.nextToken());

                break;
            }
        }
        
        if (colTypes.size() == 0) {
            for (int i = 0; i < numOfCols; i++)
                colClasses.add(String.class);
        } else if (colTypes.size() != numOfCols)
            throw new EmfException("Number of column types don't match number of columns.");

        for (Iterator<String> iter = colTypes.iterator(); iter.hasNext();) {
            String type = iter.next().trim();
            //using postgresql data types.
            if (type.toUpperCase().startsWith("VARCHAR"))
                colClasses.add(String.class);
            else if (type.toUpperCase().startsWith("TEXT"))
                colClasses.add(String.class);
            else if (type.toUpperCase().startsWith("INT"))
                colClasses.add(Integer.class);
            else if (type.toUpperCase().startsWith("REAL"))
                colClasses.add(Double.class);
            else if (type.toUpperCase().startsWith("FLOAT"))
                colClasses.add(Double.class);
            else if (type.toUpperCase().startsWith("BOOL"))
                colClasses.add(Boolean.class);
            else if (type.toUpperCase().startsWith("TIME"))
                colClasses.add(Date.class);
            else
                colClasses.add(String.class);
        }

        return colClasses.toArray(new Class[0]);
    }

    private void importStatus(boolean allSuccessful, ArrayList importFileStatus, ArrayList warningWindow) {
        try {
            if (warningWindow.size() != 0) {
                TextDialog dialog = new TextDialog(this, "WARNING", "", false);
                dialog.setTextFromList("Warnings from the Table Loader", warningWindow);
                dialog.setIconImage(EmfImageTool.createImage("/logo.JPG"));
                dialog.setModal(true);
                dialog.setVisible(true);
                warningWindow.clear();
            }
            if (!allSuccessful) // ie don't show the dialog if all the files are imported sucessfuly.
            {
                TextDialog dialog = new TextDialog(this, "Import File Status", "", false);
                dialog.setTextFromList("The status of the import process is as follows:", importFileStatus);
                dialog.setIconImage(EmfImageTool.createImage("/logo.JPG"));

                dialog.setSize(800, 400);

                dialog.setModal(true);
                dialog.setVisible(true);
            }// if(!allSuccessful)
        } // try
        catch (Exception e) {
            DefaultUserInteractor.get().notifyOfException(this, "Failed to show import status dialog", e,
                    UserInteractor.ERROR);
        }// catch
    }

    public void insertIntoTabbedPane(SpecialTableModel model, String fileName, String tabName, String fileType) {
        if (model == null)
            return;

        if (fileName == null)
            fileName = tabName;
        TablePanel panel = new TablePanel(parentConsole, model, fileName, tabName, fileType, mainTabbedPane);

        mainTabbedPane.addTab(tabName, null, panel, fileName);
        mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);
        mainTabbedPane.setPreferredSize(dimension);
    }

    public static String get50Lines(String fileName) {
        StringBuffer lines = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String str = new String("The first 50 Lines of the file " + fileName);
            lines.append(str);
            lines.append("\n");
            for (int i = 0; i < str.length(); i++)
                lines.append("=");
            lines.append("\n\n");
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < 50) {
                lines.append(line);
                lines.append("\n");
                count++;
            }
            reader.close();
        } catch (IOException ie) {
            ie.printStackTrace();
            lines.append("Error reading file " + ie.getMessage());
        }
        return lines.toString();
    }
    
    private JPanel createMenuPanel(String[] fileNames) {
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBorder(BorderFactory.createEtchedBorder());
        JMenuBar menuBar = new JMenuBar();
        menuBar.setLayout(new BoxLayout(menuBar, BoxLayout.X_AXIS));

        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);

        googleEarth = createGoogleMenuItem(fileNames);
        googleEarth.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
        fileMenu.add(googleEarth);
        //exportMenuItem.setEnabled(false);
        
        // Add call to Google earth generator
        // button should be called 'Google Earth'
        // 

        closeMenuItem = createCloseMenuItem();
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
        fileMenu.add(closeMenuItem);
        closeMenuItem.setEnabled(false);


        menuPanel.add(menuBar, BorderLayout.WEST);
        return menuPanel;
    }
    
    private JMenuItem createGoogleMenuItem(final String[] fileNames) {
        JMenuItem exportMenu = new JMenuItem("Google Earth");
        exportMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    //TablePanel table = (TablePanel) mainTabbedPane.getComponentAt(0);
                  int index = mainTabbedPane.getSelectedIndex();
                  String[] tabNames = createTabNames(1, 40, fileNames);
                  String subtitle = "Create Google Earth file: " + tabNames[index]+ "/" + title ;
                  PointSourceGeneratorFrame frame = new PointSourceGeneratorFrame(new File(fileNames[index]), subtitle);
                          //"C:\\DOCUME~1\\CEPUSER\\LOCALS~1\\Temp\\QA_DSID243_V0_20080826140114Summarize_by_Plant.csv"));
                  //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                  frame.pack();
                  frame.centerFrame();
                  frame.setVisible(true);
              } catch (RuntimeException ex) {
                  JOptionPane.showMessageDialog(parentConsole, ex.getLocalizedMessage(), "Google Earth Exception", JOptionPane.ERROR_MESSAGE);
              }
            }// actionPerformed()
        });
        // create a file chooser
        return exportMenu;
    }// createExportMenuItem()
    
    private JMenuItem createCloseMenuItem() {
        JMenuItem closeMenu = new JMenuItem("Close");
        return closeMenu;
    }
}
