package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlmeasure.ControlMeasureExportService;

import java.io.File;

public class CMExportPresenter {

    private CMExportView view;

    private EmfSession session;

    private static String lastFolder = null;

    public CMExportPresenter(EmfSession session) {
        this.session = session;
    }

    public void notifyDone() {
        view.disposeView();
    }

    public void display(CMExportView view) {
        this.view = view;
        view.observe(this);
        view.display();
        view.setMostRecentUsedFolder(getFolder());
        
    }

    private String getFolder() {
        return (lastFolder != null) ? lastFolder : getDefaultFolder();
    }

    public void doExportWithOverwrite(int[] controlMeasureIds, String folder, String prefix, boolean download) throws EmfException {
        doExport(controlMeasureIds, folder, true, prefix, download);
    }

    public void doExportWithoutOverwrite(int[] controlMeasureIds, String folder, String prefix, boolean download) throws EmfException {
        doExport(controlMeasureIds, folder, false, prefix, download);
    }

    private void doExport(int[] controlMeasureIds, String folder, boolean overwrite, String prefix, boolean download) throws EmfException {
        ControlMeasureExportService service = session.controlMeasureExportService();
        lastFolder=folder;
        
        if (overwrite)
            service.exportControlMeasuresWithOverwrite(folder, prefix, controlMeasureIds, session.user(), download);
        else
            service.exportControlMeasures(folder, prefix, controlMeasureIds, session.user(), download);

        session.setMostRecentExportFolder(folder);
        
    }

//    private String mapToRemote(String dir) {
//        return session.preferences().mapLocalOutputPathToRemote(dir);
//    }

    private String getDefaultFolder() {
        String folder = session.preferences().outputFolder();
        if (folder == null) return "";
        
        if (!new File(folder).isDirectory())
            folder = "";// default, if unspecified

        return folder;
    }
    
    public ControlMeasure[] getControlMeasureBySector(int[] sectorIds, boolean allClasses) throws EmfException {
        if (!allClasses) {
            return session.controlMeasureService().getControlMeasureBySectorExcludeClasses(sectorIds);
        }
        return session.controlMeasureService().getControlMeasureBySector(sectorIds);
    }
    
    public Sector[] getDistinctControlMeasureSectors() throws EmfException {
        return session.controlMeasureService().getDistinctControlMeasureSectors();
    }
}
