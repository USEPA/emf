package gov.epa.emissions.framework.install.installer;

import gov.epa.emissions.commons.io.importer.FilePatternMatcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CreateDownloadFilesList {

    private char delimiter;

    private String libDir;

    private String outputDir;

    private PrintWriter printer;

    private int counter = 0;
    
    private File clientJarFile;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mmaaa");

    public CreateDownloadFilesList(String libDir, String outputDir, char delimiter) {

        this.libDir = libDir;
        this.outputDir = outputDir;
        this.delimiter = delimiter;
        this.clientJarFile = new File(Constants.CLIENT_JAR_FILE);
    }

    protected void createFilesList() throws Exception {

        if (!isDirectory(this.libDir)) {
            throw new Exception("The '" + this.libDir + "' is not a directory");
        }

        if (!isDirectory(this.outputDir)) {
            throw new Exception("The '" + this.outputDir + "' is not a directory");
        }

        System.out.println("Generating file '" + Constants.FILE_LIST + "' from lib dir '" + this.libDir + "'");
        System.out.println("Generating file '" + Constants.FILE_LIST + "' in dir '" + this.outputDir + "'");
        
        List<File> jarRefFiles = new ArrayList<File>();
        File[] jarFiles = getFiles(this.libDir);
        
        System.out.println("Getting ref files from dir '" + Constants.REFERENCE_PATH + "'");
        File[] refFiles = getRefFiles(Constants.REFERENCE_PATH);
 
        System.out.println("Getting pref files from dir '" + Constants.PREFERENCE_PATH + "'");
        File[] prefFiles = getRefFiles(Constants.PREFERENCE_PATH);
        
        System.out.println("Adding jar files:");
        if (jarFiles != null) {
            for (File file : jarFiles) {
                System.out.println(" " + file.getCanonicalPath());
            }
        }
        else {
            System.out.println("Warning: jar files array is null!");
        }
        jarRefFiles.addAll(Arrays.asList(jarFiles));

        System.out.println("Adding ref files:");
        if (refFiles != null) {
            for (File file : refFiles) {
                System.out.println(" " + file.getCanonicalPath());
            }
        }
        else {
            System.out.println("Warning: ref files array is null!");
        }
        jarRefFiles.addAll(Arrays.asList(refFiles));

        System.out.println("Adding pref files:");
        if (prefFiles != null) {
            for (File file : prefFiles) {
                System.out.println(" " + file.getCanonicalPath());
            }
        }
        else {
            System.out.println("Warning: pref files array is null!");
        }
        jarRefFiles.addAll(Arrays.asList(prefFiles));
        
        printer = new PrintWriter(new BufferedWriter(new FileWriter(this.outputDir + File.separatorChar
                + Constants.FILE_LIST)));
        printHeader();
        createFilesList(jarRefFiles.toArray(new File[0]));
        print(clientJarFile);
        printer.close();
    }

    private File[] getFiles(String dir) {
        File path = new File(dir);
        String[] fileNames = path.list();
        try {
            String[] jarFiles = new FilePatternMatcher(path, "*.jar").matchingNames(fileNames);
            File[] jars = new File[jarFiles.length];
            for (int i = 0; i < jarFiles.length; i++)
                jars[i] = new File(dir, jarFiles[i]);

            return jars;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private File[] getRefFiles(String dir) {
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        };
        File path = new File(dir);
        
        return path.listFiles(fileFilter);
    }

    private void createFilesList(File[] files) {
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                print(files[i]);
            } else {
                createFilesList(files[i].listFiles());
            }
        }
    }

    private void print(File file) {
        counter++;
        printer.print(counter);
        printer.print(delimiter);

        String relativePath = getRelativePath(file);
        relativePath = relativePath.replace('\\', '/');
        printer.print(relativePath);
        printer.print(delimiter);

        printer.print("all");
        printer.print(delimiter);

        long lastModified = file.lastModified();
        Date date = new Date(lastModified);
        printer.print(dateFormat.format(date));
        printer.print(delimiter);

        printer.print("1.0");
        printer.print(delimiter);

        if (file.length() == 0) {
            printer.print(0);
        } else if (file.length() % 1024 == 0) {
            printer.print((file.length() / 1024));// in KB
        } else {
            printer.print(((file.length() / 1024) + 1));// in KB
        }

        printer.println();
        printer.flush();
    }

    private String getRelativePath(File file) {
        String absFilePath = file.getAbsolutePath();
        String relativePath = "/lib/" + file.getName();
        String parentPath = file.getParent();
        System.out.println("Parent path: " + parentPath);
        System.out.println(Constants.REFERENCE_PATH);
        
        if (absFilePath.indexOf("epa-commons") >= 0) {
            relativePath = "/lib/epa-commons.jar";
        } else if (absFilePath.indexOf("analysis-engine") >= 0) {
            relativePath = "/lib/analysis-engine.jar";
        } else if (parentPath.equalsIgnoreCase(Constants.REFERENCE_PATH)) {
            relativePath = "/config/ref/delimited/" + file.getName();
        } else if (parentPath.equalsIgnoreCase(Constants.PREFERENCE_PATH)) {
            relativePath = "/config/preferences/" + file.getName();
        } else if (absFilePath.equalsIgnoreCase(Constants.CLIENT_JAR_FILE)) {
            relativePath = File.separatorChar + file.getName();
        }
        else {
            System.out.println("Using default relative path for file '" + file.getAbsoluteFile() + "'");
        }

        System.out.println("For file '" + file.getAbsoluteFile() + "' using relative path '" + relativePath + "'");
        
        return relativePath;
    }

    private void printHeader() {
        printer.println("number" + delimiter + "path" + delimiter + "groups" + delimiter + "date" + delimiter
                + "version" + delimiter + "size");
        printer.flush();
    }

    private boolean isDirectory(String dirName) {
        File file = new File(dirName);
        return file.exists() && file.isDirectory();
    }
    
    public static void main(String[] args) {
        try {
            CreateDownloadFilesList filelist = new CreateDownloadFilesList(args[0], args[1], ';');
            filelist.createFilesList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
