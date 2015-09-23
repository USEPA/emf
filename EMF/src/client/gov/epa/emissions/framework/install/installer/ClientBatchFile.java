package gov.epa.emissions.framework.install.installer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ClientBatchFile {
    
    private PrintWriter writer;
    
    private final String sep = Constants.SEPARATOR;
    
    private File batchFile;

    public ClientBatchFile(String fileName) throws Exception{
        this.batchFile = new File(fileName);
        writer = new PrintWriter(new BufferedWriter( new FileWriter(fileName)));
    }
    
    public void create(String preference, String javahome, String rhome, String server) throws Exception{
        writer.println("@echo off" + sep);
        writer.println("::  Batch file to start the EMF Client" + sep  + sep);
        writer.println("set EMF_HOME=\"" + batchFile.getParent() + "\""+ sep);
        writer.println("set R_HOME=" + rhome + sep);
        writer.println("set JAVA_EXE=\"" + javahome + "\\bin\\java\"" + sep);
        writer.println("::  add bin directory to search path" + sep); 
        writer.println("set PATH=%PATH%;%R_HOME%" + sep);
        writer.println(":: set needed jar files in CLASSPATH" + sep);
        classPath();
        writer.println("set CLASSPATH=%CLASSPATH%;%EMF_HOME%\\emf-client.jar");
        writer.println(sep + sep + "@echo on" + sep + sep);
        writer.println("%JAVA_EXE% -Xmx400M -DUSER_PREFERENCES=" + 
                "\"" + System.getProperty("user.home") + "\\" + preference + "\" " +
                "-DEMF_HOME=%EMF_HOME% " + "-DR_HOME=\"%R_HOME%\" " +
                "-classpath %CLASSPATH% gov.epa.emissions.framework.client.EMFClient " +
                server + sep);
        writer.close();
         
    }

    private void classPath() throws Exception {
        String [] jarFiles = getJarFiles();
        writer.println();
        writer.println("set CLASSPATH=%EMF_HOME%\\lib\\"+jarFiles[0]);
        for (int i = 1; i < jarFiles.length; i++) {
            writer.println("set CLASSPATH=%CLASSPATH%;%EMF_HOME%\\lib\\"+jarFiles[i]);
        }
    }

    private String[] getJarFiles() throws Exception {
        File libDir = new File(batchFile.getParent() + File.separator + "lib");
        if (!libDir.exists())
            libDir = new File(System.getProperty("user.dir") + File.separator + "lib");
        String[] fileNames = libDir.list();
        return new FilePatternMatcher(libDir, "*.jar").matchingNames(fileNames);
    }
    
}
