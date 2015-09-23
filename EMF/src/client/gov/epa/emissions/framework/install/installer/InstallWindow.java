package gov.epa.emissions.framework.install.installer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class InstallWindow extends JFrame implements InstallView {
    final static String DIRECTORY_PAGE = "EMF Directories";

    final static String DOWNLOAD_PAGE = "Messages";

    final static String UPDATE_PAGE = "Update Files";

    final static int INSTALL = 0;

    final static int RE_INSTALL = 1;

    final static int UPDATE = 2;

    private boolean windowsOS = true;

    private int INSTALL_MODE = INSTALL;

    private InstallPresenter presenter;

    private JFrame installFrame;

    private JPanel directoryPage, downloadPage, statusPanel, cards;

    private Box updatePage;

    private JTextField url, javaHomeDirField, rHomeField, inputDirField, outputDirField, installDirField, serverField,
            tmpDirField, preferencesText;

    private JLabel urlLabel, javaHomeLabel, rHomeLabel, inputLabel, outputLabel, installHomeLabel, tmpDirLabel;

    private JLabel statusLabel, load, serverLabel, holderLabel, inputDirBrowser, outputDirBrowser, prefFileLabel, serverHolderLabel;

    private JButton installButton, exitInstallButton, cancel;

    private JButton installDirBrowser, javaHomeBrowser, rHomeBrowser, tmpDirBrowser;

    static boolean installFinished = false;

    public InstallWindow() {
        super("EMF Client Installer -- " + Constants.VERSION);
        super.setIconImage(Toolkit.getDefaultToolkit().getImage(Object.class.getClass().getResource("/logo.JPG")));
    }

    public void initialize() {
        windowsOS = presenter.windowsOS();
        installFrame = this;

        // Create widgets.
        url = new JTextField(30);
        url.setToolTipText("URL for downloading and updating EMF client software");

        javaHomeDirField = new JTextField(Constants.JAVA_HOME);
        javaHomeDirField.setToolTipText("Java Runtime Environment (JRE) install home directory");

        rHomeField = new JTextField(30);
        rHomeField.setToolTipText("R bin directory. R is not required but is used to make plots");

        inputDirField = new JTextField(30);
        inputDirField.setToolTipText("EMF server side data file import directory");

        outputDirField = new JTextField(30);
        outputDirField.setToolTipText("EMF server side data file export directory");

        installDirField = new JTextField(30);
        installDirField.setToolTipText("Home directory user chooses to install EMF client software");

        tmpDirField = new JTextField(30);
        tmpDirField.setToolTipText("QA reports will be downloaded into this directory");

        serverField = new JTextField(30);
        serverField.setToolTipText("EMF service site URL");
        
        preferencesText = new JTextField(30);
        preferencesText.setText(Constants.USER_HOME + File.separatorChar + Constants.EMF_PREFERENCES_FILE);
        preferencesText.setEditable(false);

        urlLabel = new JLabel("EMF Download URL", SwingConstants.RIGHT);
        javaHomeLabel = new JLabel("Java Home Directory", SwingConstants.RIGHT);
        rHomeLabel = new JLabel("R Bin Directory", SwingConstants.RIGHT);
        inputLabel = new JLabel("Server Import Directory", SwingConstants.RIGHT);
        outputLabel = new JLabel("Server Export Directory", SwingConstants.RIGHT);
        installHomeLabel = new JLabel("Client Home Directory", SwingConstants.RIGHT);
        tmpDirLabel = new JLabel("Local Temp Directory", SwingConstants.RIGHT);
        serverLabel = new JLabel("Server Address", SwingConstants.RIGHT);
        serverHolderLabel = new JLabel();
        prefFileLabel = new JLabel("Output Preferences File", SwingConstants.RIGHT);
        statusLabel = new JLabel();
        holderLabel = new JLabel();
        new JLabel();
        load = new JLabel();

        javaHomeBrowser = new JButton("Browse...");
        rHomeBrowser = new JButton("Browse...");
        inputDirBrowser = new JLabel();
        outputDirBrowser = new JLabel();
        installDirBrowser = new JButton("Browse...");
        tmpDirBrowser = new JButton("Browse...");
        installButton = new JButton("Install");
        exitInstallButton = new JButton(" Exit  ");
        cancel = new JButton("Cancel");

        // Create and set up the panel.
        directoryPage = new JPanel();
        directoryPage.setLayout((new BorderLayout()));
        downloadPage = new JPanel();
        downloadPage.setLayout((new BorderLayout()));

        statusPanel = new JPanel();
        statusPanel.setLayout(new GridLayout(1, 1));

        installButton.addActionListener(new InstallButtonListener());
        exitInstallButton.addActionListener(new ExitInstallButtonListener());
        cancel.addActionListener(new CancelButtonListener());
        javaHomeBrowser.addActionListener(new Browse1Listener());
        rHomeBrowser.addActionListener(new BrowseRHomeListener());
        installDirBrowser.addActionListener(new InstallDirBrowserListener());
        tmpDirBrowser.addActionListener(new TmpDirBrowseListener());

        // first look in the user home directory for the preferences file
        getUserPreferences();

        // Set the default button.
        getRootPane().setDefaultButton(installButton);

        cards = new JPanel(new CardLayout());
        directoryPage = createFirstPage();
        downloadPage = createSecondPage();
        updatePage = new UpdateFilesPage(cards);
        ((UpdateFilesPage) updatePage).observe(presenter);

        // Create the panel that contains the "cards".
        cards.add(directoryPage, DIRECTORY_PAGE);
        cards.add(downloadPage, DOWNLOAD_PAGE);
        cards.add(updatePage, UPDATE_PAGE);

        getContentPane().add(cards, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(700, 400));
        setResizable(false);
    }

    private void getUserPreferences() {
        try {
            InstallPreferences up = presenter.getUserPreference();

            if (up == null) {
                url.setText(Constants.EMF_URL);
                serverField.setText(Constants.SERVER_ADDRESS);
                return;
            }

            String urlString = up.emfWebSite();
            String serverString = up.emfServer();
            String inputString = up.inputFolder();
            String outputString = up.outputFolder();
            String installString = up.emfInstallFolder();
            String localTmpDir = up.localTempDir();
            String rhome = up.rHome();

            url.setText(urlString);
            inputDirField.setText(inputString);
            outputDirField.setText(outputString);
            installDirField.setText(windowsOS ? installString.replace('/', '\\') : installString);
            rHomeField.setText(windowsOS ? rhome.replace('/', '\\') : rhome);
            tmpDirField.setText(windowsOS ? localTmpDir.replace('/', '\\') : localTmpDir);
            serverField.setText(serverString);
        } catch (Exception e) {
            presenter.displayErr(e.getMessage());
        }
    }

    private void setLookAndFeel() {
        String lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();

        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLocationRelativeTo(null);
        setResizable(false);
    }

    public void display() {
        initialize();
        setLookAndFeel();
        setVisible(true);
    }

    public void observe(InstallPresenter presenter) {
        this.presenter = presenter;
    }

    public void close() {
        super.dispose();
    }

    public void showMsg(String msg1, String msg2) {
        JOptionPane.showMessageDialog(this, msg1, msg2, JOptionPane.WARNING_MESSAGE);
    }

    private void browse(String name, JTextField text, boolean defaultTempDir) {
        JFileChooser chooser;
        File file = new File(text.getText());

        if (defaultTempDir) {
            chooser = new JFileChooser(new File(System.getProperty("java.io.tmpdir")));
        } else if (file.isDirectory()) {
            chooser = new JFileChooser(file);
        } else {
            chooser = new JFileChooser("C:\\");
        }

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Please select the " + name);

        int option = chooser.showDialog(this, "Select");
        if (option == JFileChooser.APPROVE_OPTION) {
            text.setText("" + chooser.getSelectedFile());
        }
    }

    private JPanel createFirstPage() {
        // Create GridBagLayout.
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel installPanel = new JPanel(gridbag);
        JPanel installPanelNorth = new JPanel();
        JPanel installPanelEast = new JPanel();
        JPanel installPanelWest = new JPanel();
        Box installPanelSouth = new Box(BoxLayout.X_AXIS);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(1, 1, 7, 5);
        gridbag.setConstraints(urlLabel, c);
        gridbag.setConstraints(url, c);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(holderLabel, c);
        installPanel.add(urlLabel);
        installPanel.add(url);
        installPanel.add(holderLabel);

        c.gridwidth = 1; // next-to-last in row
        gridbag.setConstraints(inputLabel, c);
        gridbag.setConstraints(inputDirField, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(inputDirBrowser, c);
        installPanel.add(inputLabel);
        installPanel.add(inputDirField);
        installPanel.add(inputDirBrowser);

        c.gridwidth = 1; // next-to-last in row
        gridbag.setConstraints(outputLabel, c);
        gridbag.setConstraints(outputDirField, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(outputDirBrowser, c);
        installPanel.add(outputLabel);
        installPanel.add(outputDirField);
        installPanel.add(outputDirBrowser);

        c.gridwidth = 1; // next-to-last in row
        // c.insets = new Insets(7, 1, 7, 5);
        gridbag.setConstraints(javaHomeLabel, c);
        gridbag.setConstraints(javaHomeDirField, c);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(javaHomeBrowser, c);
        installPanel.add(javaHomeLabel);
        installPanel.add(javaHomeDirField);
        installPanel.add(javaHomeBrowser);

        c.gridwidth = 1; // next-to-last in row
        // c.insets = new Insets(7, 1, 7, 5);
        gridbag.setConstraints(rHomeLabel, c);
        gridbag.setConstraints(rHomeField, c);
        c.gridwidth = GridBagConstraints.REMAINDER; // end row
        gridbag.setConstraints(rHomeBrowser, c);
        installPanel.add(rHomeLabel);
        installPanel.add(rHomeField);
        installPanel.add(rHomeBrowser);

        c.gridwidth = 1; // next-to-last in row
        gridbag.setConstraints(installHomeLabel, c);
        gridbag.setConstraints(installDirField, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(installDirBrowser, c);
        installPanel.add(installHomeLabel);
        installPanel.add(installDirField);
        installPanel.add(installDirBrowser);

        c.gridwidth = 1; // next-to-last in row
        gridbag.setConstraints(tmpDirLabel, c);
        gridbag.setConstraints(tmpDirField, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(tmpDirBrowser, c);
        installPanel.add(tmpDirLabel);
        installPanel.add(tmpDirField);
        installPanel.add(tmpDirBrowser);

        c.gridwidth = 1; // next-to-last in row
        gridbag.setConstraints(serverLabel, c);
        gridbag.setConstraints(serverField, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(serverHolderLabel, c);
        installPanel.add(serverLabel);
        installPanel.add(serverField);
        installPanel.add(serverHolderLabel);
        
        c.gridwidth = 1; // next-to-last in row
        gridbag.setConstraints(prefFileLabel, c);
        gridbag.setConstraints(preferencesText, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        installPanel.add(prefFileLabel);
        installPanel.add(preferencesText);

        installPanelSouth.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 80));
        installPanelSouth.add(Box.createHorizontalGlue());
        installPanelSouth.add(installButton);
        installPanelSouth.add(Box.createRigidArea(new Dimension(20, 0)));
        installPanelSouth.add(exitInstallButton);

        // Assemble the panels
        directoryPage.add(installPanel, BorderLayout.CENTER);
        directoryPage.add(installPanelNorth, BorderLayout.NORTH);
        directoryPage.add(installPanelSouth, BorderLayout.SOUTH);
        directoryPage.add(installPanelEast, BorderLayout.EAST);
        directoryPage.add(installPanelWest, BorderLayout.WEST);

        return directoryPage;
    }

    private JPanel createSecondPage() {
        JPanel upper = new JPanel();
        load.setFont(new Font("default", Font.BOLD, 12));
        upper.add(load);

        Box buttons = new Box(BoxLayout.X_AXIS);
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 80));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(cancel);
        buttons.setLocation(0, 100);

        statusPanel.setBorder(BorderFactory.createEmptyBorder(1, 10, 2, 10));
        statusPanel.add(statusLabel);

        Box southPanel = new Box(BoxLayout.Y_AXIS);
        southPanel.add(buttons);
        southPanel.add(statusPanel);

        downloadPage.add(upper, BorderLayout.CENTER);
        downloadPage.add(southPanel, BorderLayout.SOUTH);

        return downloadPage;
    }

    private class ExitInstallButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String installhome = installDirField.getText();

            writePreferences();
            
            if (new File(installhome).exists())
                rewriteBatchFile();
            
            System.exit(0);
        }
    }

    private class InstallButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int option = checkDirs();
            if (option == JOptionPane.OK_OPTION) {
                try {
                    presenter.initModels(url.getText(), Constants.FILE_LIST, installDirField.getText());
                } catch (InstallException e1) {
                    return;
                }

                if (new File(installDirField.getText(), Constants.UPDATE_FILE).exists())
                    checkUpdates();
                else {
                    load.setText(Constants.EMF_INSTALL_MESSAGE);
                    writePreferences();
                    presenter.startDownload();
                }
            }
        }
    }

    private class Browse1Listener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            browse("Java Home Directory", javaHomeDirField, false);
        }
    }

    private class BrowseRHomeListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            browse("R Bin Directory", rHomeField, false);
        }
    }

    private class InstallDirBrowserListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            browse("Install Directory", installDirField, false);
        }
    }

    private class TmpDirBrowseListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            browse("Local Temporary Directory", tmpDirField, true);
        }
    }

    private class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equalsIgnoreCase("Cancel")) {
                presenter.stopDownload();
                CardLayout cl = (CardLayout) (cards.getLayout());
                cl.show(cards, DIRECTORY_PAGE);
            }

            if (e.getActionCommand().equalsIgnoreCase("Done")) {
                System.exit(0);
            }
        }
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void displayErr(String err) {
        presenter.stopDownload();
        JOptionPane.showMessageDialog(this, err, "EMF Client Installation Error", JOptionPane.WARNING_MESSAGE);
    }

    public void setFinish() {
        
        presenter.deleteOutOfDateFiles();
        rewriteBatchFile();

        if (INSTALL_MODE == INSTALL) {
            presenter.createShortcut();
            load.setText(Constants.INSTALL_CLOSE_MESSAGE);
        } else if (INSTALL_MODE == RE_INSTALL) {
            load.setText(Constants.REINSTALL_CLOSE_MESSAGE);
        } else if (INSTALL_MODE == UPDATE) {
            load.setText(Constants.UPDATE_CLOSE_MESSAGE);
        }

        cancel.setText("Done");
    }

    private void rewriteBatchFile() {
        String javahome = javaHomeDirField.getText();
        String installhome = installDirField.getText();
        String server = serverField.getText();
        String rhome = rHomeField.getText();
        presenter.createBatchFile(installhome + File.separatorChar + Constants.EMF_BATCH_FILE, Constants.EMF_PREFERENCES_FILE,
                javahome, rhome, server);
    }

    public int checkDirs() {
        String serverAddr = serverField.getText();
        boolean localhost = serverAddr == null ? false : serverAddr.contains("localhost");
        String[] names = { inputDirField.getText(), outputDirField.getText(), javaHomeDirField.getText(),
                installDirField.getText() };
        String[] labels = { "Server Import Directory", "Server Export Directory", "Java Home Directory",
                "Client Home Directory" };
        String message1 = "";
        String message2 = "";

        if ((!(new File(inputDirField.getText()).exists()) || !(new File(outputDirField.getText()).exists()))
                && !localhost)
            message1 += "Please make sure the server import/export directory exists on the EMF server."
                    + Constants.SEPARATOR;

        for (int i = 0; i < names.length; i++) {
            if (!(new File(names[i]).exists())) {
                if (i < 2 && !localhost)
                    continue;

                message2 += labels[i] + ", ";
            }
        }

        if (!message2.equals(""))
            message2 += " does not exist." + Constants.SEPARATOR;

        String message3 = "Do you want to proceed with installing the EMF client?";

        if (!message1.isEmpty() || !message2.isEmpty())
            return JOptionPane.showConfirmDialog(this, message1 + message2 + message3);

        return JOptionPane.OK_OPTION;
    }

    private void checkUpdates() {
        Object[] possibleValues = { "Update", "Reinstall" };
        Object selectedValue = JOptionPane.showInputDialog(installFrame,
                "The Client Home Directory already exists. Would you like to reinstall "
                        + "or update?\n Please choose one:", "Input", JOptionPane.INFORMATION_MESSAGE, null,
                possibleValues, possibleValues[0]);
        if (selectedValue == "Reinstall") {
            INSTALL_MODE = RE_INSTALL;
            load.setText(Constants.EMF_REINSTALL_MESSAGE);
            writePreferences();
            presenter.startDownload();
        } else if (selectedValue == "Update") {
            INSTALL_MODE = UPDATE;
            load.setText(Constants.EMF_UPDATE_MESSAGE);
            writePreferences();
            startUpdates();
        }
        
    }

    private void writePreferences() {
        String javahome = javaHomeDirField.getText();
        String rhome = rHomeField.getText();
        String outputdir = outputDirField.getText();
        String inputdir = inputDirField.getText();
        String installhome = installDirField.getText();
        String tmpDir = tmpDirField.getText();
        String website = url.getText();
        String server = serverField.getText();
        CardLayout cl = (CardLayout) (cards.getLayout());
        cl.show(cards, DOWNLOAD_PAGE);
        presenter.writePreference(website, inputdir, outputdir, javahome, rhome, installhome, tmpDir, server);
    }

    private void startUpdates() {
        CardLayout cl = (CardLayout) (cards.getLayout());
        ((UpdateFilesPage) updatePage).setDownloadPage(DOWNLOAD_PAGE);
        ((UpdateFilesPage) updatePage).setDirsPage(DIRECTORY_PAGE);
        ((UpdateFilesPage) updatePage).display(getUpdateText());
        cl.show(cards, UPDATE_PAGE);
    }

    private String getUpdateText() {
        String separator = Constants.SEPARATOR;
        String text = "Files to update:" + separator + separator;
        String[] files = presenter.checkUpdates();
        for (int i = 0; i < files.length; i++)
            text += files[i] + separator;
        text += separator;
        
        int numOfOutOfDateFiles = presenter.getNumOfOutDateFiles();
        List<String> filesToBeDeleted = presenter.getOutDateFiles();
        text += "Files to delete:" + separator + separator;
        for ( String file : filesToBeDeleted) {
            text += file + separator;
        }

        text += separator + "Total: " + (files.length + numOfOutOfDateFiles) + " files.";
        if (files.length + numOfOutOfDateFiles > 0)
            ((UpdateFilesPage) updatePage).enableUpdate();

        return text;
    }

}
