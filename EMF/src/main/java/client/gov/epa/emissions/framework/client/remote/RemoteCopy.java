package gov.epa.emissions.framework.client.remote;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class RemoteCopy {

    private String program;

    private String tempDir;

    private String host;

    private String os;

    private int errorLevel;

    private String errorString;

    private String userName;

    private String command = "";

    private String programMsg = "";

    private String localFile;

    public RemoteCopy(UserPreference pref, User user) throws EmfException {
        os = System.getProperty("os.name");
        this.tempDir = pref.localTempDir();
        this.host = System.getProperty("emf.remote.host");
        this.userName = user.getUsername();
        checkParameters();
    }

    public String getProgram() {
        return this.program;
    }

    public String getTempDir() {
        return this.tempDir;
    }

    private void checkParameters() throws EmfException {
        if (this.host == null || this.host.isEmpty())
            host = "localhost";

        if (this.tempDir == null || this.tempDir.isEmpty())
            tempDir = ".";

        if (!tempDir.equals(".") && !new File(tempDir).exists())
            throw new EmfException("User specified temporary directory is invalid.");

        if (!host.equalsIgnoreCase("localhost") && (this.program == null || this.program.isEmpty()))
            throw new EmfException(
                    "A valid remote copy program must be specified in the preference file (EMFPrefs.txt).");
    }

    public String copyToLocal(String remotePath, String localPath) throws EmfException {
        if (remotePath == null || remotePath.isEmpty())
            throw new EmfException("Remote copy: a valid remote path must be specified.");

        String separator = (remotePath.charAt(0) == '/') ? "/" : "\\";
        int lastSeparatorIndex = remotePath.lastIndexOf(separator);
        String remotefile = remotePath.substring(++lastSeparatorIndex);

        localPath = (localPath == null || localPath.isEmpty()) ? (tempDir + "/" + remotefile) : localPath;
        localFile = (localPath.contains(remotefile)) ? localPath : localPath + separator + remotefile;

        if (new File(localPath).exists())
            return localPath;

        if (host.equalsIgnoreCase("localhost")) {
            if (os.toUpperCase().contains("WINDOWS"))
                command = "copy " + "\"" + remotePath + "\" \"" + localPath + "\"";
            if (os.toUpperCase().contains("LINUX") || os.toUpperCase().contains("UNIX"))
                command = "cp " + "\"" + remotePath + "\" \"" + localPath + "\"";
        } else if (this.program.contains("pscp"))
            command = this.program + " -batch " + userName + "@" + this.host + ":\"" + remotePath + "\" \"" + localPath
                    + "\"";
        else if (this.program.contains("scp"))
            command = this.program + " -B -o PasswordAuthentication=no " + userName + "@" + this.host + ":\""
                    + remotePath + "\" \"" + localPath + "\"";
        else
            command = this.program + " " + userName + "@" + this.host + ":\"" + remotePath + "\" \"" + localPath + "\"";

        execute(command);

        return localPath;
    }

    private void execute(String command) throws EmfException {
        String[] cmds = getCommands(command);
        BufferedReader reader = null;
        Thread killCopyProcess = new Thread(new Runnable(){
            public void run() {
                killRemoteCopyProcess();
            }
        });

        try {
            killCopyProcess.start();
            Process process = Runtime.getRuntime().exec(cmds);
            errorLevel = process.waitFor();

            if (errorLevel > 0) {
                reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                errorString = reader.readLine();
                throw new EmfException(programMsg + processError());
            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (reader != null)
                closeReader(reader);
        }
    }

    private String[] getCommands(String command) {
        if (os.equalsIgnoreCase("Linux") || os.equalsIgnoreCase("Unix")) {
            return new String[] { "sh", "-c", command };
        }

        String[] cmd = new String[3];

        if (os.equalsIgnoreCase("Windows 98") || os.equalsIgnoreCase("Windows 95")) {
            cmd[0] = "command.com";
        } else {
            cmd[0] = "cmd.exe";
        }

        cmd[1] = "/C";
        cmd[2] = command;

        return cmd;
    }

    private String processError() {
        if (errorString == null || errorString.isEmpty())
            return "Copy file failed. Please check if you have exported the result.";

        errorString = errorString.toLowerCase();

        if (errorString.contains("no supported authentication"))
            return "Remote copy failed. Local key agent not started, logged in user has no account on EMF server or ssh settings not right.";

        if (errorString.contains("cannot create file"))
            return "Please check temporary folder permission/existance specified in the preference file (EMFPrefs.txt).";

        if (errorString.contains("not recognized as an internal or external command"))
            return "Please check the remote copy program specified in the preference file (EMFPrefs.txt).";

        if (errorString.contains("disconnected") || errorString.contains("connection refused"))
            return "Please check your network connection or ssh connection.";

        if (errorString.contains("no such file or directory"))
            return "Please check if you have exported the result or the export folder was right.";

        return errorString;
    }

    private void closeReader(BufferedReader reader) throws EmfException {
        try {
            reader.close();
        } catch (IOException e) {
            throw new EmfException(e.getMessage());
        }
    }

    public void killRemoteCopyProcess() {
        if (host.equalsIgnoreCase("localhost")) 
            return;
        
        String[] cmd = null;
        String msg = null;

        if (os.equalsIgnoreCase("Linux") || os.equalsIgnoreCase("Unix")) {
            cmd = getCommands("kill -9 `ps aux | grep " + program + " | awk '{print $2}'`"); // kill the secure copy
            // program
        } else if (os.equalsIgnoreCase("Windows 98") || os.equalsIgnoreCase("Windows 95")) {
            msg = "Cannot kill process " + program
                    + " under current Windows system. Please go to Windows task manager to kill EMF application.";
        } else { // kill the secure copy program
            cmd = getCommands("taskkill /F /IM " + (program.endsWith(".exe") ? program : program + ".exe"));
        }

        try {
            Thread.sleep(30000);

            if (new File(localFile).exists())
                return;

            if (msg != null) {
                System.err.println(msg);
                return;
            }

            programMsg = "Remote copy timed out and terminated. Please check if you have a user account on EMF server.";
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
