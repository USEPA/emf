package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastOutputExportWrapper;

import java.util.List;

public interface ExportPresenter {

    void notifyDone();

    void display(ExportView view);

    void doExportWithOverwrite(List<FastOutputExportWrapper> fastOutputExportWrappers, String folder)
            throws EmfException;

    void doExport(List<FastOutputExportWrapper> fastOutputExportWrappers, String folder) throws EmfException;

    void setLastFolder(String lastfolder);
}