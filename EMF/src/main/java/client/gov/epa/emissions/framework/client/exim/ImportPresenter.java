package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.exim.ExImService;

public class ImportPresenter {

    private ImportView view;

    private ExImService service;

    private User user;

    private EmfSession session;

    private ImportInputRules importRules;

    public ImportPresenter(EmfSession session, User user, ExImService service) {
        this.user = user;
        this.service = service;
        this.session = session;
        this.importRules = new ImportInputRules();
    }

    public void doImport(String directory, String[] files, DatasetType type) throws EmfException {
        importDatasets(directory, files, type, view);
    }

    void importDatasets(String directory, String[] files, DatasetType type, ImportView view) throws EmfException {
        importRules.validate(directory, files, type);
        startImportMessage(view);
        service.importDatasets(user, directory, files, type);
    }

    private void startImportMessage(ImportView view) {
        String message = "Started import. Please monitor the Status window to track your Import request.";
        view.setMessage(message);
    }

    public void doImport(String directory, String[] files, DatasetType type, String datasetName) throws EmfException {
        importDataset(directory, files, type, datasetName, view);
    }

    public void doDone() {
        view.disposeView();
    }

    public void display(ImportView view) {
        this.view = view;
        view.register(this);
        view.setDefaultBaseFolder(getDefaultBaseFolder());

        view.display();
        view.populate();
    }
    
    private String getDefaultBaseFolder() {
        String folder = session.preferences().inputFolder();
        if (folder == null || folder.trim().isEmpty())
            folder = "";// default, if unspecified

        return folder;
    }

    public void importDataset(String directory, String[] files, DatasetType type, String datasetName, ImportView view)
            throws EmfException {
        importRules.validate(directory, files, type, datasetName);
        startImportMessage(view);
        service.importDataset(user, directory, files, type, datasetName);

    }

    public String[] getFilesFromPatten(String folder, String pattern) throws EmfException {
        return service.getFilenamesFromPattern(folder, pattern);
    }

}
