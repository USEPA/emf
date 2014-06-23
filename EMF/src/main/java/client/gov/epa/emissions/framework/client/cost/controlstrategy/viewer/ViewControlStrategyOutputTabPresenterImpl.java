package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditorView;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenterImpl;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

public class ViewControlStrategyOutputTabPresenterImpl implements ViewControlStrategyOutputTabPresenter {

    private EmfSession session;

    private ViewControlStrategyOutputTabView view;

    private static String lastFolder = null;

    public ViewControlStrategyOutputTabPresenterImpl(EmfSession session, ViewControlStrategyOutputTabView view) {
        this.session = session;
        this.view = view;
    }

    public void doRun(ControlStrategy controlStrategy) throws EmfException {
        view.run(controlStrategy);
    }

    public void doExport(EmfDataset[] datasets, String folder) throws EmfException {
        view.clearMessage();

        if (datasets.length == 0) {
            throw new EmfException("Please select one or more result datasets");
        }

        session.setMostRecentExportFolder(folder);
        ExImService service = session.eximService();

        Version[] versions = new Version[datasets.length];
        for (int i = 0; i < datasets.length; i++) {
            versions[i] = service.getVersion(datasets[i], datasets[i].getDefaultVersion());
        }

        service.exportDatasets(session.user(), datasets, versions, folder, 
                null, true, "", null, null, null, "", "Exporting datasets");
    }

    public void doAnalyze(String controlStrategyName, EmfDataset[] datasets) throws EmfException {

        if (datasets.length == 0) {
            throw new EmfException("Please select one or more result datasets. ");
        }

        String[] fileNames = new String[datasets.length];
        for (int i = 0; i < datasets.length; i++) {

            File localFile = new File(tempResultFilePath(datasets[i]));
            try {

                if (!localFile.exists() || localFile.lastModified() != datasets[i].getModifiedDateTime().getTime()) {

                    Writer output = new BufferedWriter(new FileWriter(localFile));
                    try {

                        output.write(writeHeader(datasets[i]));
                        output.write(getTableAsString(datasets[i]));
                    } finally {

                        output.close();
                        localFile.setLastModified(datasets[i].getModifiedDateTime().getTime());
                        fileNames[i] = localFile.getAbsolutePath();
                    }
                } else {
                    fileNames[i] = localFile.getAbsolutePath();
                }
            } catch (Exception e) {
                throw new EmfException(e.getMessage());
            }
        }

        view.displayAnalyzeTable(controlStrategyName, fileNames);
    }

    private String tempResultFilePath(EmfDataset dataset) throws EmfException {

        String separator = File.separator;
        UserPreference preferences = new DefaultUserPreferences();
        String tempDir = preferences.localTempDir();

        if (tempDir == null || tempDir.isEmpty()) {
            tempDir = System.getProperty("java.io.tmpdir");
        }

        File tempDirFile = new File(tempDir);

        if (!tempDirFile.exists() || !tempDirFile.isDirectory() || !tempDirFile.canWrite() || !tempDirFile.canRead()) {
            throw new EmfException("Import-export temporary folder does not exist or lacks write permissions: "
                    + tempDir);
        }

        return tempDir + separator + dataset.getName() + ".csv"; // this is how exported file name was
    }

    private String writeHeader(EmfDataset dataset) {

        String lineFeeder = System.getProperty("line.separator");
        String header = "#DATASET_NAME=" + dataset.getName() + lineFeeder;
        header += "#DATASET_VERSION_NUM= " + "dataset.getVersion()" + lineFeeder;
        header += "#CREATION_DATE=" + CustomDateFormat.format_YYYY_MM_DD_HH_MM(dataset.getCreatedDateTime())
                + lineFeeder;

        return header;
    }

    public String getTableAsString(EmfDataset dataset) throws EmfException {
        return session.dataService().getTableAsString("emissions." + getTableName(dataset));
    }

    private String getTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable().toLowerCase();
    }

    public void doDisplay(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults)
            throws EmfException {

        view.observe(this);
        view.display(controlStrategy, controlStrategyResults);
    }

    public void setLastFolder(String folder) {
        lastFolder = folder;
    }

    public String folder() {
        return (lastFolder != null) ? lastFolder : defaultFolder();
    }

    private String defaultFolder() {

        String folder = session.preferences().outputFolder();
        if (folder == null || !new File(folder).isDirectory())
            folder = "";// default, if unspecified

        return folder;
    }

    public void doInventory(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults,
            String namePrefix)
            throws EmfException {

        view.clearMessage();
        session.controlStrategyService().createInventories(session.user(), controlStrategy, 
                controlStrategyResults, namePrefix);
    }

    public void doRefresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        view.refresh(controlStrategy, controlStrategyResults);
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();
        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public void doDisplayPropertiesEditor(DatasetPropertiesEditorView editor, EmfDataset dataset) throws EmfException {

        view.clearMessage();
        // make sure the dataset still exists, it could have been removed and the client might not of/
        // refreshed their view, so lets check for the existence of the dataset
        PropertiesEditorPresenter presenter = new PropertiesEditorPresenterImpl(dataset, editor, session);
        presenter.doDisplay();
    }

    public void doChangeStrategyType(StrategyType strategyType) {
        // no-op
    }

    public long getTableRecordCount(EmfDataset dataset) throws EmfException {
        return session.dataService().getTableRecordCount("emissions." + getTableName(dataset));
    }

    public Version[] getVersions(int datasetId) throws EmfException {
        return session.dataEditorService().getVersions(datasetId);
    }

    public Version getVersion(int datasetId, int version) throws EmfException {

        Version[] versions = session.dataEditorService().getVersions(datasetId);
        for (Version v : versions) {
            if (v.getVersion() == version) {
                return v;
            }
        }

        return null;
    }
}