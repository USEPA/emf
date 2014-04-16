package gov.epa.emissions.framework.client.cost.controlstrategy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.UUID;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyPresenter;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyPresenterImpl;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyView;
import gov.epa.emissions.framework.client.cost.controlstrategy.viewer.ViewControlStrategyPresenter;
import gov.epa.emissions.framework.client.cost.controlstrategy.viewer.ViewControlStrategyPresenterImpl;
import gov.epa.emissions.framework.client.cost.controlstrategy.viewer.ViewControlStrategyView;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class ControlStrategiesManagerPresenterImpl implements RefreshObserver, ControlStrategiesManagerPresenter {
    private ControlStrategyManagerView view;

    private EmfSession session;

    private LightControlMeasure[] controlMeasures = {};

    public ControlStrategiesManagerPresenterImpl(EmfSession session, ControlStrategyManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void display() throws EmfException {
        view.display(service().getControlStrategies());
        view.observe(this);
    }

    private ControlStrategyService service() {
        return session.controlStrategyService();
    }

    public void doRefresh() throws EmfException {
        // loadControlMeasures();
        view.refresh(service().getControlStrategies());
    }

    public void doClose() {
        view.disposeView();
    }

    public void doNew(ControlStrategyView view) {
        ControlStrategyPresenter presenter = new ControlStrategyPresenterImpl(session, view, this);
        presenter.doDisplay();

    }

    public void doEdit(EditControlStrategyView view, ControlStrategy controlStrategy) throws EmfException {
        EditControlStrategyPresenter presenter = new EditControlStrategyPresenterImpl(controlStrategy, session, view,
                this);
        displayEditor(presenter);
    }

    public void doView(ViewControlStrategyView view, ControlStrategy controlStrategy) throws EmfException {

        ViewControlStrategyPresenter presenter = new ViewControlStrategyPresenterImpl(controlStrategy, session, view,
                this);
        displayViewer(presenter);
    }

    void displayEditor(EditControlStrategyPresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    void displayViewer(ViewControlStrategyPresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    public void doRemove(int[] ids) throws EmfException {
        service().removeControlStrategies(ids, session.user());
    }

    // public void doSaveCopiedStrategies(ControlStrategy coppied, String name) throws EmfException {
    // if (isDuplicate(coppied))
    // throw new EmfException("A control strategy named '" + coppied.getName() + "' already exists.");
    //
    // coppied.setCreator(session.user());
    // coppied.setLastModifiedDate(new Date());
    // service().addControlStrategy(coppied);
    // }

    public void doSaveCopiedStrategies(int id, User creator) throws EmfException {
        service().copyControlStrategy(id, session.user());
    }

    // private boolean isDuplicate(ControlStrategy newStrategy) throws EmfException {
    // return (service().isDuplicateName(newStrategy.getName()) != 0);
    // // ControlStrategy[] strategies = service().getControlStrategies();
    // // for (int i = 0; i < strategies.length; i++) {
    // // if (strategies[i].getName().equals(newStrategy.getName()))
    // // return true;
    // // }
    // //
    // // return false;
    // }

    public LightControlMeasure[] getControlMeasures() {
        return controlMeasures;
    }

    public void loadControlMeasures() throws EmfException {
        controlMeasures = session.controlMeasureService().getLightControlMeasures();
    }

    public void viewControlStrategyComparisonResult(int[] controlStrategyIds, String exportDir) throws EmfException {
        if (controlStrategyIds == null || controlStrategyIds.length == 0)
            throw new EmfException("No cases to compare.");
        
        File localFile = new File(tempQAStepFilePath(exportDir));
        try {
            if (!localFile.exists()) {
                Writer output = new BufferedWriter(new FileWriter(localFile));
                try {
                    output.write(  
//                            writerHeader(qaStep, qaResult, dataset.getName())
                            ""+ getControlStrategyComparisonResult(controlStrategyIds) 
                            );
                }
                finally {
                    output.close();
                }
            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
        
        view.displayControlStrategyComparisonResult("Control Strategy Comparison", localFile.getAbsolutePath());
    }
    
    private String tempQAStepFilePath(String exportDir) throws EmfException {
        String separator = File.separator; 
        UserPreference preferences = new DefaultUserPreferences();
        String tempDir = preferences.localTempDir();
//        String separator = exportDir.length() > 0 ? (exportDir.charAt(0) == '/') ? "/" : "\\" : "\\";
//        String tempDir = System.getProperty("IMPORT_EXPORT_TEMP_DIR"); 

        if (tempDir == null || tempDir.isEmpty())
            tempDir = System.getProperty("java.io.tmpdir");

        File tempDirFile = new File(tempDir);

        if (!(tempDirFile.exists() && tempDirFile.isDirectory() && tempDirFile.canWrite() && tempDirFile.canRead()))
            throw new EmfException("Import-export temporary folder does not exist or lacks write permissions: "
                    + tempDir);


        return tempDir + separator + (UUID.randomUUID()).toString().replaceAll("-", "") + ".csv"; // this is how exported file name was
    }

    private String getControlStrategyComparisonResult(int[] caseIds) throws EmfException {
        return service().getControlStrategyComparisonResult(caseIds);
    }

}
