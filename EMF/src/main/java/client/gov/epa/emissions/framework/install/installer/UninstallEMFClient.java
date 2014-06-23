package gov.epa.emissions.framework.install.installer;

import java.io.File;

/***
 * NOTE: DESTRUCTIVE!!!
 * NOTE: Never run this program within Eclipse!!!
 * NOTE:YOU WILL DETELETE EVERYTHING!!!
 *
 ***/

public class UninstallEMFClient {
    public static void main(String[] args) throws Exception {
        File desktopLink = new File(Constants.UNINSTALL_DESKTOP);
        File startMenuDir = new File(Constants.UNINSTALL_START);
        File installDir = new File(Constants.WORK_DIR);

        System.out.println("Work Dir: " + installDir.getAbsolutePath());

        boolean deleteDesktop = true;
        boolean deleteStartMenu = true;
        boolean deleteInstallDir = true;
        int exitValue = 0;

        try {
            deleteDesktop = desktopLink.delete();

            File[] files = startMenuDir.listFiles();

            for (File file : files) {
                deleteStartMenu = file.delete();
            }
            
            deleteStartMenu = startMenuDir.delete();
            
            File[] installFiles = installDir.listFiles();
            
            for (File file : installFiles)
                deleteInstallDir = file.delete();
        } catch (Exception e) {
            exitValue = 1;
            StringBuffer msg = new StringBuffer();

            if (!deleteDesktop)
                msg.append("Cannot delete file: " + Constants.UNINSTALL_DESKTOP + Constants.SEPARATOR);

            if (!deleteStartMenu)
                msg.append("Cannot delete directory: " + Constants.UNINSTALL_START + Constants.SEPARATOR);

            if (!deleteInstallDir)
                msg.append("Cannot delete directory: " + Constants.WORK_DIR + Constants.SEPARATOR);

            Throwable error = e.getCause();

            if (error != null)
                msg.append("Error: " + error.getMessage() + Constants.SEPARATOR);

            msg.append("Error: " + e.getMessage());

            throw new Exception(msg.toString());
        } finally {
            System.exit(exitValue);
        }
    }
}
