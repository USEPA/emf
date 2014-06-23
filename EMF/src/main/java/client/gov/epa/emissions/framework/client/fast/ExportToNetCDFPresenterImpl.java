package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastOutputExportWrapper;
import gov.epa.emissions.framework.services.fast.FastService;

import java.io.File;
import java.util.List;

public class ExportToNetCDFPresenterImpl extends ExportPresenterImpl {

    public ExportToNetCDFPresenterImpl(EmfSession session) {
        super(session);
    }

    public void doExportWithOverwrite(List<FastOutputExportWrapper> outputs, String folder) throws EmfException {
        doExportInvoke(outputs, folder, true);
    }

    public void doExport(List<FastOutputExportWrapper> outputs, String folder) throws EmfException {
        doExportInvoke(outputs, folder, false);
    }

    /**
     * This method was modified on 07/13/2007 to convert the calls to use datasetIds instead of sending the selected
     * outputs back as a collection for export. Original code is preserved as comments in the method.
     */
    private void doExportInvoke(List<FastOutputExportWrapper> outputs, String folder, boolean overwrite)
            throws EmfException {

        try {
            FastService service = session.fastService();

            session.setMostRecentExportFolder(folder);

            if (new File(folder).isDirectory()) {
                lastFolder = folder;
            }

            for (FastOutputExportWrapper fastOutputExportWrapper : outputs) {

                int datasetId = fastOutputExportWrapper.getOutputDatasetId();
                int datasetVersion = fastOutputExportWrapper.getOutputDatasetVersion();
                int gridId = fastOutputExportWrapper.getGridId();
                String username = this.session.user().getUsername();
                String pollutant = "";

                if (overwrite) {
                    service.exportFastOutputToNetCDFFile(datasetId, datasetVersion, gridId, username, folder, pollutant);
                } else {
                    service.exportFastOutputToNetCDFFile(datasetId, datasetVersion, gridId, username, folder, pollutant);
                }
            }

        } catch (Exception e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }

    }
}
