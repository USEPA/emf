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
    
    public void create(String preference, String javahome, String rhome, String server, String cmdArguments) throws Exception{
        writer.println("@echo off" + sep);
        writer.println("::  Batch file to start the EMF Client" + sep  + sep);
        writer.println("set EMF_HOME=\"" + batchFile.getParent() + "\""+ sep);
        writer.println("set R_HOME=" + rhome + sep);
        writer.println("::  add bin directory to search path" + sep); 
        writer.println("set PATH=%PATH%;%R_HOME%" + sep);
        writer.println(":: set needed jar files in CLASSPATH" + sep);
        writer.println("set CLASSPATH=%EMF_HOME%\\lib*;%EMF_HOME%\\emf-client.jar");
        writer.println(sep + sep + "@echo on" + sep + sep);
        writer.println("java -Xmx1024M -DUSER_PREFERENCES=" +
                "\"" + System.getProperty("user.home") + "\\" + preference + "\" " +
                "-DEMF_HOME=%EMF_HOME% " + "-DR_HOME=\"%R_HOME%\" " +
                cmdArguments + " " +
                "-classpath %CLASSPATH% gov.epa.emissions.framework.client.EMFClient " +
                server + sep);
        writer.close();
         
    }
}
