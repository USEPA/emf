package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

import java.io.File;

public class CMImportPresenter {

    private CMImportView view;

    private EmfSession session;

    private CMImportInputRules importRules;

    public CMImportPresenter(EmfSession session) {
        this.session = session;
        this.importRules = new CMImportInputRules();
    }

    public void doImport(boolean purge, int [] sectorIDs, String directory, String[] files) throws EmfException {
        
        if ( purge && sectorIDs != null) { // need to purge control measure by sectors
            
            if ( sectorIDs.length == 0) { // TODO: JIZHEN purge all
                //System.out.println("Purge all");
                
            } else { // TODO: JIZHEN purge by sectors
                //System.out.println("Purge by sectors: ");
                for ( int i=0; i<sectorIDs.length; i++) {
                    //System.out.print(sectorIDs[i] + " ");
                }
                //System.out.println("");
            }
            
            // TODO: JIZHEN do validation here
            
            ControlMeasure[] oldCMs = session.controlMeasureService().getControlMeasureBySector(sectorIDs);
            int numOldCMs = oldCMs.length;
            int numNewCMs = session.controlMeasureImportService().getControlMeasureCountInSummaryFile(purge, sectorIDs, directory, files, session.user());
            String msg = "Are you sure you want to purge existing control measures?\n\nYou are " + (numOldCMs != numNewCMs ? "only ": "") + "importing " + numNewCMs + " control measures " + (numOldCMs != numNewCMs ? ", BUT ": "AND ") + "purging " + numOldCMs + " existing control measures.\n\nThe existing control measures to be purged will be backed up via the export process.\n\nContinue with the purge?";
            boolean doPurege = view.confirmToPurge(msg);
            
            if ( !doPurege) {
                return;
            }
            
            importControlMeasures(purge, sectorIDs, directory, files);
            
            return;

        }
        
        importControlMeasures(purge, sectorIDs, directory, files);
    }

    void importControlMeasures(boolean purge, int [] sectorIDs, String directory, String[] files) throws EmfException {
        
        //Maybe validate if user is a CoST SU, if truncate is true
        importRules.validate(directory, files);
        
        startImportMessage(view);
//        importing = true;
//        session.controlMeasureImportService().importControlMeasures(mapToRemote(directory), files, session.user());
        session.controlMeasureImportService().importControlMeasures(purge, sectorIDs, directory, files, session.user());
    }

    private void startImportMessage(CMImportView view) {
        String message = "Started Import. Please use 'Status Window' and 'Import Status' button in the import window to track";
        view.setMessage(message);
    }

    public void doDone() {
//        if (importing) {
//            String message = "Control measures are being imported, you will lose status messages by closing this window." + System.getProperty("line.separator")
//            + " Click the Import Status button to ge the status of the import porcess.  Click no, if you to don't care to see the messages for the import process.";
//            String title = "Ignore import status messages?";
//            YesNoDialog dialog = new YesNoDialog(view, title, message);
//            if (dialog.confirm()) {
//                view.disposeView();
//            }
//        }
        view.disposeView();
    }

    public void display(CMImportView view) {
        try {
            removeImportStatuses();
        } catch (EmfException e) {
            //
        }
        this.view = view;
        view.register(this);
        view.setDefaultBaseFolder(getDefaultBaseFolder());

        view.display();
    }

    private String getDefaultBaseFolder() {
        String folder = session.preferences().inputFolder();
        if (folder == null) folder = "";
        else if (!new File(folder).isDirectory())
            folder = "";// default, if unspecified

        return folder;
    }

    // TODO: move the getFileNamesFromPattern () to a common service
    public String[] getFilesFromPatten(String folder, String pattern) throws EmfException {
        return session.eximService().getFilenamesFromPattern(folder, pattern);
    }

    public Status[] getImportStatus() throws EmfException {
        return session.controlMeasureImportService().getImportStatus(session.user());
    }

    public void removeImportStatuses() throws EmfException {
        session.controlMeasureImportService().removeImportStatuses(session.user());
    }
    
    public Sector[] getDistinctControlMeasureSectors() throws EmfException {
        return session.controlMeasureService().getDistinctControlMeasureSectors();
    }

    public String getCoSTSUs() throws EmfException {
        return session.controlStrategyService().getCoSTSUs();
    }
}
