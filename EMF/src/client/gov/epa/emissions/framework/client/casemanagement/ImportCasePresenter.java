package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;

public class ImportCasePresenter {

    private ImportCaseView view;

    private EmfSession session;

    public ImportCasePresenter(EmfSession session) {
        this.session = session;
    }

    public void finish() {
        view.disposeView();
    }

    public void display(ImportCaseView view) {
        this.view = view;
        view.register(this);
        view.setDefaultBaseFolder(getDefaultBaseFolder());

        view.display();
    }

    private String getDefaultBaseFolder() {
        String folder = session.preferences().inputFolder();

        return folder;
    }

    public String[] getFilesFromPatten(String folder, String pattern) throws EmfException {
        return session.eximService().getFilenamesFromPattern(folder, pattern);
    }

    public void importCase(String folder, String[] files) throws EmfException {
        if (files == null || files.length == 0)
            throw new EmfException("Please select case files to import the case.");
        
        if (files.length % 3 != 0)
            throw new EmfException("Three case files should be selected.");
        
        session.caseService().importCase(folder, files, session.user());
    }

}
