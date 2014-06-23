package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.PivotConfiguration;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.qa.QAService;

public class QAServiceTransport implements QAService {

    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfCall call;

    public QAServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("QA Service");
        
        return call;
    }

    public synchronized QAStep[] getQASteps(EmfDataset dataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("getQASteps");
        call.addParam("dataset", mappings.dataset());
        call.setReturnType(mappings.qaSteps());
        Object[] params = new Object[] { dataset };

        return (QAStep[]) call.requestResponse(params);
    }
    
    public synchronized QAStepResult[] getQAStepResults(EmfDataset dataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("getQAStepResults");
        call.addParam("dataset", mappings.dataset());
        call.setReturnType(mappings.qaStepResults());
        Object[] params = new Object[] { dataset };

        return (QAStepResult[]) call.requestResponse(params);
    }

    public synchronized QAStep[] updateWitoutCheckingConstraints(QAStep[] steps) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateWitoutCheckingConstraints");
        call.addParam("steps", mappings.qaSteps());
        //call.setVoidReturnType();
        call.setReturnType(mappings.qaSteps());

        return (QAStep[]) call.requestResponse(new Object[] { steps });
    }

    public synchronized QAProgram[] getQAPrograms() throws EmfException {
        EmfCall call = call();

        call.setOperation("getQAPrograms");
        call.setReturnType(mappings.programs());

        return (QAProgram[]) call.requestResponse(new Object[] {});
    }

    public synchronized void runQAStep(QAStep step, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("runQAStep");
        call.addParam("step", mappings.qaStep());
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { step, user });

    }

    public synchronized void exportQAStep(QAStep step, User user, String dirName, String fileName, boolean overide, String rowFilter) throws EmfException {
        EmfCall call = call();

        call.setOperation("exportQAStep");
        call.addParam("step", mappings.qaStep());
        call.addParam("user", mappings.user());
        call.addStringParam("dirName");
        call.addStringParam("fileName");
        call.addBooleanParameter("overide");
        call.addStringParam("rowFilter");
        call.setVoidReturnType();

        call.request(new Object[] { step, user, dirName, fileName, new Boolean(overide), rowFilter });
    }

    public synchronized void downloadQAStep(QAStep step, User user, String fileName, boolean overwrite, String rowFilter) throws EmfException {
        EmfCall call = call();

        call.setOperation("downloadQAStep");
        call.addParam("step", mappings.qaStep());
        call.addParam("user", mappings.user());
        call.addStringParam("fileName");
        call.addBooleanParameter("overwrite");
        call.addStringParam("rowFilter");
        call.setVoidReturnType();

        call.request(new Object[] { step, user, fileName, overwrite, rowFilter });
    }

    public synchronized void exportShapeFileQAStep(QAStep step, User user, String dirName, String fileName, boolean overide, ProjectionShapeFile projectionShapeFile, String rowFilter, PivotConfiguration pivotConfiguration) throws EmfException {
        EmfCall call = call();

        call.setOperation("exportShapeFileQAStep");
        call.addParam("step", mappings.qaStep());
        call.addParam("user", mappings.user());
        call.addStringParam("dirName");
        call.addStringParam("fileName");
        call.addBooleanParameter("overide");
        call.addParam("projectionShapeFile", mappings.projectionShapeFile());
        call.addParam("rowFilter", mappings.string());
        call.addParam("pivotConfiguration", mappings.pivotConfiguration());
        call.setVoidReturnType();

        call.request(new Object[] { step, user, dirName, fileName, new Boolean(overide), projectionShapeFile, rowFilter, pivotConfiguration });
    }

    public synchronized void downloadShapeFileQAStep(QAStep step, User user, String fileName, ProjectionShapeFile projectionShapeFile, String rowFilter, PivotConfiguration pivotConfiguration, boolean overwrite) throws EmfException {
        EmfCall call = call();

        call.setOperation("downloadShapeFileQAStep");
        call.addParam("step", mappings.qaStep());
        call.addParam("user", mappings.user());
        call.addStringParam("fileName");
        call.addParam("projectionShapeFile", mappings.projectionShapeFile());
        call.addParam("rowFilter", mappings.string());
        call.addParam("pivotConfiguration", mappings.pivotConfiguration());
        call.addBooleanParameter("overwrite");
        call.setVoidReturnType();

        call.request(new Object[] { step, user, fileName, projectionShapeFile, rowFilter, pivotConfiguration, overwrite });
    }

    public synchronized QAStepResult getQAStepResult(QAStep step) throws EmfException {
        EmfCall call = call();

        call.setOperation("getQAStepResult");
        call.addParam("step", mappings.qaStep());
        call.setReturnType(mappings.qaStepResult());

        return (QAStepResult) call.requestResponse(new Object[] { step });

    }

    public synchronized QAStep update(QAStep step) throws EmfException {
        EmfCall call = call();

        call.setOperation("update");
        call.addParam("steps", mappings.qaStep());
        //call.setVoidReturnType();
        call.setReturnType(mappings.qaStep());

        return (QAStep) call.requestResponse(new Object[] { step });

    }

    public synchronized QAProgram addQAProgram(QAProgram program) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("addQAProgram");
        call.addParam("program", mappings.program());
        call.setReturnType(mappings.program());
        
        return (QAProgram) call.requestResponse(new Object[] { program });
    }

    public ProjectionShapeFile[] getProjectionShapeFiles() throws EmfException {
        EmfCall call = call();

        call.setOperation("getProjectionShapeFiles");
        call.setReturnType(mappings.projectionShapeFiles());

        return (ProjectionShapeFile[]) call.requestResponse(new Object[] {});
    }

    public void copyQAStepsToDatasets(User user, QAStep[] steps, int[] datasetIds, boolean replace) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("copyQAStepsToDatasets");
        call.addParam("user", mappings.user());
        call.addParam("steps", mappings.qaSteps());
        call.addParam("datasetIds", mappings.integers());
        call.addBooleanParameter("replace");
        call.setVoidReturnType();

        call.request(new Object[] { user, steps, datasetIds, new Boolean(replace) });
    }
    
    public boolean getSameAsTemplate(QAStep step) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getSameAsTemplate");
        call.addParam("steps", mappings.qaSteps());
        call.setBooleanReturnType();
        
        return (Boolean)call.requestResponse(new Object[]{step});
    }

    public void deleteQASteps(User user, QAStep[] steps, int datasetId) throws EmfException { // BUG3615

        EmfCall call = call();
        
        call.setOperation("deleteQASteps");
        call.addParam("user", mappings.user());
        call.addParam("steps", mappings.qaSteps());
        call.addParam("datasetId", mappings.integer());
        call.setVoidReturnType();

        call.request(new Object[] { user, steps, datasetId });
        
    }

    public boolean isShapefileCapable(QAStepResult stepResult) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("isShapefileCapable");
        call.addParam("stepResult", mappings.qaStepResult());
        call.setBooleanReturnType();
        
        return (Boolean)call.requestResponse(new Object[]{ stepResult });
    }

}
