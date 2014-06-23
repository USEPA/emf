package gov.epa.emissions.framework.install.installer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class Tools {

    public static String resetWinSeprator(String s) {
        int i = s.length();
        char[] array = s.toCharArray();
        for (int j = 0; j < i; j++) {
            if (array[j] == '/') {
                array[j] = '\\';
            }
        }
        return String.valueOf(array);
    }

    public static boolean removeDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = removeDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static String getCurrentDate(String format) {
        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String datenewformat = formatter.format(today);
        return datenewformat;
    }

    public static void updateFileModTime(String installhome, File2Download[] files) throws Exception {
        for (int i = 0; i < files.length; i++) {
            String fullpath = installhome + "/" + files[i].getPath();
            File file = new File(fullpath);

            StringTokenizer st = new StringTokenizer(files[i].getDate(), " ");
            String time1 = st.nextToken().trim();
            String time2 = st.nextToken().trim();

            SimpleDateFormat sdf = new SimpleDateFormat(Constants.TIME_FORMAT);
            Date date = sdf.parse(time1 + " " + time2);
            file.setLastModified(date.getTime());
        }
    }

    public static void writePreference(String website, String input, String output, String javahome, String rhome,
            String emfhome, String tmpDir, String server) throws Exception {
        String separator = Constants.SEPARATOR;
        InstallPreferences up = Tools.getUserPreference();
        int sigDigits = 0;
        int deciPlace = 2;
        String doubleOption = up.doubleOption();

        try {
            sigDigits = Integer.parseInt(up.significantDigits());
            deciPlace = Integer.parseInt(up.decimalPlaces());
        } catch (Exception e) {
            // NOTE cat parse exception, but do nothing
        }

        String header = "#EMF Client/Installer - Preferences" + separator + "#comments '#'" + separator
                + "#preference specified by key,value pair separted by '='" + separator + "#case sensitive" + separator
                + "#white spaces and line terminators can be escaped by '\'" + separator
                + "#If the value aren't specified then default value will be empty string" + separator
                + "#Use '/' for path separator for file names" + separator + separator;

        String emfPrefString = "web.site=" + website + separator + "emf.install.folder=" + emfhome.replace('\\', '/')
                + separator + "server.import.folder=" + input.replace('\\', '/') + separator + "server.export.folder="
                + output.replace('\\', '/') + separator + "server.address=" + server + separator + "java.home="
                + javahome.replace('\\', '/') + separator + "local.temp.dir=" + tmpDir.replace('\\', '/') + separator
                + "r.home=" + rhome.replace('\\', '/') + separator;

        String analysisEnginePrefString = separator + separator + "#Analysis Engine Preferences" + separator
                + "format.double.decimal_places=" + deciPlace + separator + "format.double.option=" + doubleOption + separator
                + "#legal options: Standard_Notation,Scientific_Notation, Dollars, Percentage, Custom" + separator
                + "format.double.significant_digits=" + sigDigits;

        PrintWriter userPrefWriter = new PrintWriter(new BufferedWriter(new FileWriter(System.getProperty("user.home")
                + "\\" + Constants.EMF_PREFERENCES_FILE)));
        userPrefWriter.write(header + emfPrefString + analysisEnginePrefString);
        userPrefWriter.close();
    }

    public static void createShortcut(String installhome) throws IOException, InterruptedException {
        File bat = new File(installhome, "shortcut.bat");
        File inf = new File(installhome, "shortcut.inf");
        Tools.createShortcutBatchFile(installhome, bat, inf);

        try {

            String[] cmd = Tools.getCommands(installhome);

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
            throw e;
        } catch (InterruptedException e) {
            throw e;
        }
    }

    private static void createShortcutBatchFile(String installhome, File bat, File inf) throws IOException {
        String separator = Constants.SEPARATOR;

        String battext = "\n@echo off & setlocal" + separator
                + "\nset inf=rundll32 setupapi,InstallHinfSection DefaultInstall" + separator + "\nstart/w %inf% 132 "
                + installhome.replace('\\', '/') + "/shortcut.inf" + separator + "\nendlocal" + separator;

        String inftext = "[version]" + separator + "signature=$chicago$" + separator + "[DefaultInstall]" + separator
                + "UpdateInis=Addlink" + separator + "[Addlink]" + separator
                + "setup.ini, progman.groups,, \"group200=\"\"EMF\"\"\"" + separator
                + "setup.ini, group200,, \"\"\"EMF Client\"\",\"\"" + installhome.replace('\\', '/') + "/"
                + Constants.EMF_BATCH_FILE + "\"\",\"\"" + installhome.replace('\\', '/') + Constants.EMF_ICON
                + "\"\",0\"" + separator;

        FileWriter fw1 = new FileWriter(bat);
        FileWriter fw2 = new FileWriter(inf);

        try {
            fw1.write(battext);
            fw2.write(inftext);
        } catch (IOException e) {
            throw e;
        } finally {
            fw1.close();
            fw2.close();
        }
    }

    private static String[] getCommands(String installhome) {
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

    public static InstallPreferences getUserPreference() throws Exception {
        File userPreference = new File(Constants.USER_HOME, Constants.EMF_PREFERENCES_FILE);

        if (userPreference.exists())
            return new InstallPreferences(userPreference);

        InputStream templateInputStream = Object.class.getClass().getResource(
                "/" + Constants.INSTALLER_PREFERENCES_FILE).openStream();

        return new InstallPreferences(templateInputStream);
    }

}
