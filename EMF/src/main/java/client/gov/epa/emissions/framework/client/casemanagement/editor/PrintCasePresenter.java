package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.util.StringTools;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PrintCasePresenter {
    private Case currentCase;

    private EmfSession session;

    private static String lastFolder = null;

    public PrintCasePresenter(EmfSession session, Case caseObj) {
        this.currentCase = caseObj;
        this.session = session;
    }

    public void display(PrintCaseDialog view) {
        view.observe(this);
        view.setMostRecentUsedFolder(getFolder());

        view.display();
    }

    private String getFolder() {
        return (lastFolder != null) ? lastFolder : getDefaultFolder();
    }

    public void printCase(String serverfolder) throws EmfException {
        session.caseService().printCase(serverfolder, currentCase.getId());
    }    
  
    public synchronized void printLocalCase(String localfolder) throws EmfException {
        String[] caseExportString = session.caseService().printLocalCase(currentCase.getId());
             
        File exportDir = new File(localfolder);
        if (!exportDir.canWrite()) {
            throw new EmfException("EMF cannot write to folder " + localfolder);
        }
        try {
            String prefix = currentCase.getName() + "_" + currentCase.getAbbreviation().getName() + "_";
            prefix = StringTools.replaceNoneLetterDigit(prefix, '_');
            String sumParamFile = prefix + "Summary_Parameters.csv";
            String inputsFile = prefix + "Inputs.csv";
            String jobsFile = prefix + "Jobs.csv";
            //System.out.println("jobsFile : " +jobsFile);
            //First buffer: parameter
            //Second buffer: inputs
            //third buffer: jobs

            printCaseSumParams(caseExportString[0], localfolder, sumParamFile);
            printCaseInputs(caseExportString[1], localfolder, inputsFile);
            printCaseJobs(caseExportString[2], localfolder, jobsFile);

            //return caseExportString;
        } catch (Exception e) {
            throw new EmfException("Could not export case: "
                    // AME: this info makes the message too long to see it all in the window
                    // + (currentCase == null ? " (id = " + caseId + "). " : currentCase.getName() + ". ")
                    + e.getMessage());
        }  
    } 

    private synchronized void printCaseSumParams(String sb, String folder, String sumParamFile) 
    throws IOException {        
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(folder, sumParamFile))));
        writer.println(sb.toString());
        writer.close();
    }

    private synchronized void printCaseInputs(String sb, String folder,
            String inputsFile) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(folder, inputsFile))));
        writer.println(sb.toString());
        writer.close();
    }

    private synchronized void printCaseJobs(String sb, String folder, String jobsFile)
    throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(folder, jobsFile))));
        writer.println(sb.toString());
        writer.close();
    }

    private String getDefaultFolder() {
        return "";
    }

}
