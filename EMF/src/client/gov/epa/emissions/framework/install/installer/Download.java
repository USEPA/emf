package gov.epa.emissions.framework.install.installer;

import java.awt.Cursor;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.FileChannel;
import java.net.URL;

public class Download extends Thread {
    private String urlbase;

    private String installhome;

    private String filelist;

    private InstallPresenter presenter;

    private volatile Thread blinker;

    private int numFiles2Download;

    private File2Download[] todownload;

    public void initialize(String url, String filelist, String installhome) throws InstallException {
        this.urlbase = url;
        this.installhome = installhome;
        this.filelist = filelist;
        this.blinker = new Thread(this);
        try {
            downloadFileList();
        } catch (IOException e) {
            setErrMsg("Downloading files list failed.");
            throw new InstallException("Downloading files list failed.");
        }
        this.todownload = getFiles2Download();
    }

    public void start() {
        blinker.start();
    }

    public void stopDownload() {
        presenter.setCursor(Cursor.getDefaultCursor());
        blinker = null;
    }

    public void run() {
        Thread thisThread = Thread.currentThread();
        presenter.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        try {
            for (int x = 0; x < numFiles2Download; x++) {
                if (blinker == thisThread) {
                    String temp = todownload[x].getPath();
                    File file2save = getSingleDownloadFile(temp);
                    HttpURLConnection conn = getConnection(temp);
                    writeStatus(x, temp);
                    saveFile(file2save, conn);
                }
            }

            if (blinker == thisThread) {
                Tools.updateFileModTime(installhome, todownload);
                saveCurrentFilesInfo();
                presenter.setStatus("Downloads Complete.");
                presenter.setFinish();
            }
        } catch (Exception e) {
            setErrMsg("Downloading files failed.");
        } finally {
            stopDownload();
        }
    }

    public void setFile2Download(File2Download[] todownload) {
        this.numFiles2Download = todownload.length;
        this.todownload = todownload;
    }

    public void createShortcut() {
        File bat = new File(installhome, "shortcut.bat");
        File inf = new File(installhome, "shortcut.inf");
        createShortcutBatchFile(bat, inf);

        try {

            String[] cmd = getCommands();

            /*
             * Only creates a shortcut on Windows start menu.
             */
            if (System.getProperty("os.name").indexOf("Windows") >= 0) {
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();
            }

            bat.delete();
            inf.delete();
        } catch (IOException e) {
            setErrMsg("Creating shortcut failed.");
        } catch (InterruptedException e) {
            setErrMsg("Windows runtime error while creating EMF client shortcut.");
        }
    }
    
    public String getInstallHome() {
        return this.installhome;
    }

    private void createShortcutBatchFile(File bat, File inf) {
        String separator = Constants.SEPARATOR;

        String battext = "\n@echo off & setlocal" + separator
                + "\nset inf=rundll32 setupapi,InstallHinfSection DefaultInstall" + separator + "\nstart/w %inf% 132 "
                + installhome.replace('\\', '/') + "/shortcut.inf" + separator + "\nendlocal" + separator;

        String inftext = "[version]" + separator + "signature=$chicago$" + separator + "[DefaultInstall]" + separator
                + "UpdateInis=Addlink" + separator + "[Addlink]" + separator
                + "setup.ini, progman.groups,, \"group200=\"\"EMF\"\"\"" + separator
                + "setup.ini, group200,, \"\"EMF Client\",\"\"\"\"\"\"" + installhome.replace('\\', '/') + "/"
                + Constants.EMF_BATCH_FILE + "\"\"\"\"\"\",\""
                + installhome.replace('\\', '/') + Constants.EMF_ICON + "\",0\"" + separator;

        try {
            FileWriter fw1 = new FileWriter(bat);
            FileWriter fw2 = new FileWriter(inf);
            fw1.write(battext);
            fw2.write(inftext);
            fw1.close();
            fw2.close();
        } catch (IOException e) {
            setErrMsg("Creating shortcut files failed.");
        }
    }

    private String[] getCommands() {
        String[] cmd = new String[3];
        String os = System.getProperty("os.name");

        if (os.equalsIgnoreCase("Windows 98") || os.equalsIgnoreCase("Windows 95")) {
            cmd[0] = "command.com";
        } else {
            cmd[0] = "cmd.exe";
        }

        cmd[1] = "/C";
        cmd[2] = installhome.replace('\\', '/') + "/shortcut.bat";

        return cmd;
    }

    public void downloadFileList() throws IOException {
        File dir = new File(installhome);
        dir.mkdirs();
        File download = new File(dir, filelist);

        HttpURLConnection conn = getConnection(filelist);
        String s = "";
        String out = "";

        InputStream is = conn.getInputStream();
        BufferedReader content = new BufferedReader(new InputStreamReader(is));
        FileWriter fw = new FileWriter(download);
        while ((s = content.readLine()) != null) {
            out += s + Constants.SEPARATOR;
        }
        is.close();
        fw.write(out);
        fw.close();
    }

    public File2Download[] getFiles2Download() {
        File list = new File(installhome, filelist);
        if (list.exists()) {
            TextParser parser = new TextParser(list, ";");
            parser.parse();
            numFiles2Download = parser.getNumDownloadFiles();
            return parser.getDownloadFiles();
        }

        return null;
    }

    public File getSingleDownloadFile(String name) {
        if (name.endsWith("/")) {
            File f = new File(installhome, name);
            f.mkdirs();
        } else {
            int index = name.lastIndexOf("/");
            
            // Get the subdir
            String sub = name.substring(0, index);
            File subdir = new File(installhome, sub);

            if (!subdir.exists())
                subdir.mkdirs();
        }

        return new File(installhome, name);
    }

    private HttpURLConnection getConnection(String name) throws IOException {
        URL url = new URL(urlbase + "/" + name);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("user-agent", "Mozilla/5.0");
        conn.connect();

        return conn;
    }

    public void writeStatus(int n, String name) {
        String status = "Status: Downloading " + (n + 1) + " out of " + numFiles2Download + " files:   " + name;
        presenter.setStatus(status);
    }

    public void saveFile(File file, HttpURLConnection conn) throws IOException {
        if (!file.isDirectory()) {
            InputStream is = conn.getInputStream();
            ReadableByteChannel readableByteChannel = Channels.newChannel(is);
            FileOutputStream fos = new FileOutputStream(file);
            FileChannel fileChannel = fos.getChannel();
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            is.close();
            fos.close();
        }
        conn.disconnect();
    }

    private void saveCurrentFilesInfo() {
        try {
            FileOutputStream fos = new FileOutputStream(new File(installhome, Constants.UPDATE_FILE));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getFiles2Download());
            oos.close();
        } catch (Exception e) {
            setErrMsg("Saving files info failed.");
        }
    }

    private void setErrMsg(String msg) {
        presenter.displayErr(msg);
    }

    public void addObserver(InstallPresenter presenter) {
        this.presenter = presenter;
    }

}
