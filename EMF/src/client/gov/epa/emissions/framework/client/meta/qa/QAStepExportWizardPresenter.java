package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStepResult;

public class QAStepExportWizardPresenter {

    private EmfSession session;

    private QAStepExportWizardView view;

    public QAStepExportWizardPresenter(EmfSession session) {
        this.session = session;
    }

    public void display(QAStepExportWizardView view, QAStepResult qaStepResult) throws EmfException {
        this.view = view;
        this.view.observe(this);
        this.view.display(qaStepResult);
    }

    public void close() {
        //
    }
    
    public boolean isShapeFileCapable(QAStepResult stepResult) throws EmfException {
        return session.qaService().isShapefileCapable(stepResult);
    }
    
    public boolean ignoreShapeFileFunctionality() throws EmfException {
        try {
            String value = session.userService().getPropertyValue("IGNORE_SHAPEFILE_FUNCTIONALITY");
            return (value != null && value.equalsIgnoreCase("true") ? true : false);
        } finally {
            //
        }
    }

    public String[] getTableColumns(String tableName) throws EmfException {
        return session.dataService().getTableColumns(tableName);
    }
    
    public ProjectionShapeFile[] getProjectionShapeFiles() throws EmfException {
        return session.qaService().getProjectionShapeFiles();       
    }

    public boolean isCanceled() {
        // NOTE Auto-generated method stub
        return view.isCanceled();
    }
    
}
