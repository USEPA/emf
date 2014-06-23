package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastAnalysisOutput;
import gov.epa.emissions.framework.services.fast.FastAnalysisOutputType;
import gov.epa.emissions.framework.services.fast.FastDataset;
import gov.epa.emissions.framework.services.fast.FastNonPointDataset;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.services.fast.FastRunOutput;
import gov.epa.emissions.framework.services.fast.FastRunOutputType;
import gov.epa.emissions.framework.services.fast.FastService;
import gov.epa.emissions.framework.services.fast.Grid;

import java.util.Date;
import java.util.List;

public class FastServiceTransport implements FastService {
    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfCall call;

    public FastServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }

    private EmfCall call() throws EmfException {
        if (call == null)
            call = callFactory.createSessionEnabledCall("Fast Service");
        
        return call;
    }

    public synchronized FastRun[] getFastRuns() throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastRuns");
        call.setReturnType(mappings.fastRuns());

        return (FastRun[]) call.requestResponse(new Object[] {});
    }

    public synchronized FastRun[] getFastRuns(int gridId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastRuns");
        call.addIntegerParam("gridId");
        call.setReturnType(mappings.fastRuns());

        return (FastRun[]) call.requestResponse(new Object[] { new Integer(gridId) });
    }

    public synchronized int addFastRun(FastRun fastRun) throws EmfException {
        EmfCall call = call();

        call.setOperation("addFastRun");
        call.addParam("fastRun", mappings.fastRun());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { fastRun });
    }

    public synchronized FastRun obtainLockedFastRun(User owner, int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedFastRun");
        call.addParam("owner", mappings.user());
        call.addIntegerParam("id");
        call.setReturnType(mappings.fastRun());

        return (FastRun) call.requestResponse(new Object[] { owner, new Integer(id) });

    }

//    public void releaseLocked(FastRun locked) throws EmfException {
//        EmfCall call = call();
//
//        call.setOperation("releaseLocked");
//        call.addParam("fastRun", mappings.fastRun());
//        call.setReturnType(mappings.fastRun());
//
//        call.request(new Object[] { locked });
//    }

    public synchronized void releaseLockedFastRun(User user, int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedFastRun");
        call.addParam("user", mappings.user());
        call.addIntegerParam("id");
        call.setReturnType(mappings.fastRun());

        call.request(new Object[] { user, new Integer(id) });
    }

    public synchronized FastRun updateFastRun(FastRun fastRun) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateFastRun");
        call.addParam("fastRun", mappings.fastRun());
        call.setReturnType(mappings.fastRun());

        return (FastRun) call.requestResponse(new Object[] { fastRun });
    }

    public synchronized FastRun updateFastRunWithLock(FastRun fastRun) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateFastRunWithLock");
        call.addParam("fastRun", mappings.fastRun());
        call.setReturnType(mappings.fastRun());

        return (FastRun) call.requestResponse(new Object[] { fastRun });
    }

//    public void removeFastRuns(FastRun[] elements, User user) throws EmfException {
//        EmfCall call = call();
//
//        call.setOperation("removeFastRuns");
//        call.addParam("elements", mappings.controlStrategies());
//        call.addParam("user", mappings.user());
//        call.setVoidReturnType();
//
//        call.request(new Object[] { elements, user });
//    }

    public synchronized void removeFastRuns(int[] ids, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeFastRuns");
        call.addIntArrayParam();
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { ids, user });
    }

    public synchronized void runFastRun(User user, int fastRunId) throws EmfException {
        EmfCall call = call();

        call.setOperation("runFastRun");
        call.addParam("user", mappings.user());
        call.addIntegerParam("fastRunId");
        call.setVoidReturnType();

        call.request(new Object[] { user, new Integer(fastRunId) });
    }

    public synchronized void stopFastRun(int fastRunId) throws EmfException {
        EmfCall call = call();

        call.setOperation("stopFastRun");
        call.addIntegerParam("fastRunId");
        call.setVoidReturnType();

        call.request(new Object[] { new Integer(fastRunId) });
    }

    public synchronized int copyFastRun(int id, User creator) throws EmfException {
        EmfCall call = call();

        call.setOperation("copyFastRun");
        call.addIntegerParam("id");
        call.addParam("creator", mappings.user());
        call.setIntegerReturnType();
        return (Integer) call.requestResponse(new Object[] { new Integer(id), creator });
    }

    public synchronized FastRun getFastRun(int fastRunId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastRun");
        call.addIntegerParam("fastRunId");
        call.setReturnType(mappings.fastRun());
        return (FastRun) call.requestResponse(new Object[] { new Integer(fastRunId) });
    }

    public synchronized FastRunOutput[] getFastRunOutputs(int fastRunId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastRunOutputs");
        call.addIntegerParam("fastRunId");
        call.setReturnType(mappings.fastRunOutputs());

        return (FastRunOutput[]) call.requestResponse(new Object[] { new Integer(fastRunId) });
    }

    public List<FastRun> getFastRunsByRunStatus(String runStatus) {
        // NOTE Auto-generated method stub
        return null;
    }

    public Long getFastRunRunningCount() throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastRunRunningCount");
        call.setLongReturnType();

        return (Long) call.requestResponse(new Object[] { });
    }

    public void setFastRunRunStatusAndCompletionDate(int id, String runStatus, Date completionDate)
            {
        // NOTE Auto-generated method stub
        
    }

    public synchronized String getDefaultExportDirectory() throws EmfException {
        EmfCall call = call();

        call.setOperation("getDefaultExportDirectory");
        call.setReturnType(mappings.string());

        return (String) call.requestResponse(new Object[] {  });
    }

    public synchronized String getFastRunStatus(int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastRunStatus");
        call.addIntegerParam("id");
        call.setReturnType(mappings.string());

        return (String) call.requestResponse(new Object[] { new Integer(id) });
    }

    public synchronized FastDataset[] getFastDatasets() throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastDatasets");
        call.setReturnType(mappings.fastDatasets());

        return (FastDataset[]) call.requestResponse(new Object[] {});
    }

    public synchronized FastDataset getFastDataset(int fastDatasetId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastDataset");
        call.addIntegerParam("fastDatasetId");
        call.setReturnType(mappings.fastDataset());

        return (FastDataset) call.requestResponse(new Object[] { new Integer(fastDatasetId) });
    }

    public int getFastDatasetCount() throws EmfException {

        EmfCall call = call();

        call.setOperation("getFastDatasetCount");
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] {});
    }

    public synchronized int addFastDataset(FastDataset fastDataset) throws EmfException {
        EmfCall call = call();

        call.setOperation("addFastDataset");
        call.addParam("fastDataset", mappings.fastDataset());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { fastDataset });
    }

    public synchronized void removeFastDataset(int fastDatasetId, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeFastDataset");
        call.addIntegerParam("fastDatasetId");
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { new Integer(fastDatasetId), user });
    }

    public synchronized FastNonPointDataset[] getFastNonPointDatasets() throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastNonPointDatasets");
        call.setReturnType(mappings.fastNonPointDatasets());

        return (FastNonPointDataset[]) call.requestResponse(new Object[] {});
    }

    public synchronized FastNonPointDataset getFastNonPointDataset(int fastNonPointDatasetId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastNonPointDataset");
        call.addIntegerParam("fastNonPointDatasetId");
        call.setReturnType(mappings.fastNonPointDataset());

        return (FastNonPointDataset) call.requestResponse(new Object[] { new Integer(fastNonPointDatasetId) });
    }

    public int getFastNonPointDatasetCount() throws EmfException {

        EmfCall call = call();

        call.setOperation("getFastNonPointDatasetCount");
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] {});
    }

    public int addFastNonPointDataset(FastNonPointDataset fastNonPointDataset, User user) throws EmfException {

        EmfCall call = call();

        call.setOperation("addFastNonPointDataset");
        call.addParam("fastNonPointDataset", mappings.fastNonPointDataset());
        call.addParam("user", mappings.user());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { fastNonPointDataset, user });
    }

    public synchronized void removeFastNonPointDataset(int fastNonPointDatasetId, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeFastNonPointDataset");
        call.addIntegerParam("fastNonPointDatasetId");
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { new Integer(fastNonPointDatasetId), user });
    }

    public synchronized Grid[] getGrids() throws EmfException {
        EmfCall call = call();

        call.setOperation("getGrids");
        call.setReturnType(mappings.grids());

        return (Grid[]) call.requestResponse(new Object[] {});
    }

    public synchronized Grid getGrid(String name) throws EmfException {
        EmfCall call = call();

        call.setOperation("getGrid");
        call.addStringParam("name");
        call.setReturnType(mappings.grid());

        return (Grid) call.requestResponse(new Object[] { name });
    }









    public synchronized FastAnalysis[] getFastAnalyses() throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastAnalyses");
        call.setReturnType(mappings.fastAnalyses());

        return (FastAnalysis[]) call.requestResponse(new Object[] {});
    }

    public synchronized int addFastAnalysis(FastAnalysis fastAnalysis) throws EmfException {
        EmfCall call = call();

        call.setOperation("addFastAnalysis");
        call.addParam("fastAnalysis", mappings.fastAnalysis());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { fastAnalysis });
    }

    public synchronized FastAnalysis obtainLockedFastAnalysis(User owner, int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLockedFastAnalysis");
        call.addParam("owner", mappings.user());
        call.addIntegerParam("id");
        call.setReturnType(mappings.fastAnalysis());

        return (FastAnalysis) call.requestResponse(new Object[] { owner, new Integer(id) });

    }

//    public void releaseLocked(FastAnalysis locked) throws EmfException {
//        EmfCall call = call();
//
//        call.setOperation("releaseLocked");
//        call.addParam("fastAnalysis", mappings.fastAnalysis());
//        call.setReturnType(mappings.fastAnalysis());
//
//        call.request(new Object[] { locked });
//    }

    public synchronized void releaseLockedFastAnalysis(User user, int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLockedFastAnalysis");
        call.addParam("user", mappings.user());
        call.addIntegerParam("id");
        call.setReturnType(mappings.fastAnalysis());

        call.request(new Object[] { user, new Integer(id) });
    }

    public synchronized FastAnalysis updateFastAnalysis(FastAnalysis fastAnalysis) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateFastAnalysis");
        call.addParam("fastAnalysis", mappings.fastAnalysis());
        call.setReturnType(mappings.fastAnalysis());

        return (FastAnalysis) call.requestResponse(new Object[] { fastAnalysis });
    }

    public synchronized FastAnalysis updateFastAnalysisWithLock(FastAnalysis fastAnalysis) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateFastAnalysisWithLock");
        call.addParam("fastAnalysis", mappings.fastAnalysis());
        call.setReturnType(mappings.fastAnalysis());

        return (FastAnalysis) call.requestResponse(new Object[] { fastAnalysis });
    }

//    public void removeFastAnalyses(FastAnalysis[] elements, User user) throws EmfException {
//        EmfCall call = call();
//
//        call.setOperation("removeFastAnalyses");
//        call.addParam("elements", mappings.controlStrategies());
//        call.addParam("user", mappings.user());
//        call.setVoidReturnType();
//
//        call.request(new Object[] { elements, user });
//    }

    public synchronized void removeFastAnalyses(int[] ids, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeFastAnalyses");
        call.addIntArrayParam();
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { ids, user });
    }

    public synchronized void runFastAnalysis(User user, int fastAnalysisId) throws EmfException {
        EmfCall call = call();

        call.setOperation("runFastAnalysis");
        call.addParam("user", mappings.user());
        call.addIntegerParam("fastAnalysisId");
        call.setVoidReturnType();

        call.request(new Object[] { user, new Integer(fastAnalysisId) });
    }

    public synchronized void stopFastAnalysis(int fastAnalysisId) throws EmfException {
        EmfCall call = call();

        call.setOperation("stopFastAnalysis");
        call.addIntegerParam("fastAnalysisId");
        call.setVoidReturnType();

        call.request(new Object[] { new Integer(fastAnalysisId) });
    }

    public synchronized int copyFastAnalysis(int id, User creator) throws EmfException {
        EmfCall call = call();

        call.setOperation("copyFastAnalysis");
        call.addIntegerParam("id");
        call.addParam("creator", mappings.user());
        call.setIntegerReturnType();
        return (Integer) call.requestResponse(new Object[] { new Integer(id), creator });
    }

    public synchronized FastAnalysis getFastAnalysis(int fastAnalysisId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastAnalysis");
        call.addIntegerParam("fastAnalysisId");
        call.setReturnType(mappings.fastAnalysis());
        return (FastAnalysis) call.requestResponse(new Object[] { new Integer(fastAnalysisId) });
    }

    public synchronized FastAnalysisOutput[] getFastAnalysisOutputs(int fastAnalysisId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastAnalysisOutputs");
        call.addIntegerParam("fastAnalysisId");
        call.setReturnType(mappings.fastAnalysisOutputs());

        return (FastAnalysisOutput[]) call.requestResponse(new Object[] { new Integer(fastAnalysisId) });
    }

    public List<FastAnalysis> getFastAnalysesByRunStatus(String runStatus) {
        // NOTE Auto-generated method stub
        return null;
    }

    public Long getFastAnalysisRunningCount() throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastAnalysisRunningCount");
        call.setLongReturnType();

        return (Long) call.requestResponse(new Object[] { });
    }

    public void setFastAnalysisRunStatusAndCompletionDate(int id, String runStatus, Date completionDate)
            {
        // NOTE Auto-generated method stub
        
    }

    public synchronized String getFastAnalysisStatus(int id) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastAnalysisStatus");
        call.addIntegerParam("id");
        call.setReturnType(mappings.string());

        return (String) call.requestResponse(new Object[] { new Integer(id) });
    }

    public FastAnalysisOutputType getFastAnalysisOutputType(String name) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastAnalysisOutputType");
        call.addStringParam("name");
        call.setReturnType(mappings.fastAnalysisOutputType());

        return (FastAnalysisOutputType)call.requestResponse(new Object[] { name });
    }

    public FastAnalysisOutputType[] getFastAnalysisOutputTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastAnalysisOutputTypes");
        call.setReturnType(mappings.fastAnalysisOutputTypes());

        return (FastAnalysisOutputType[]) call.requestResponse(new Object[] {  });
    }

    public FastRunOutputType getFastRunOutputType(String name) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastRunOutputType");
        call.addStringParam("name");
        call.setReturnType(mappings.fastRunOutputType());

        return (FastRunOutputType)call.requestResponse(new Object[] { name });
    }

    public FastRunOutputType[] getFastRunOutputTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastRunOutputTypes");
        call.setReturnType(mappings.fastRunOutputTypes());

        return (FastRunOutputType[]) call.requestResponse(new Object[] {  });
    }

    public void exportFastOutputToShapeFile(int datasetId, int datasetVersion, int gridId, String userName, String dirName,
            String pollutant) throws EmfException {
        EmfCall call = call();

        call.setOperation("exportFastOutputToShapeFile");
        call.addIntegerParam("datasetId");
        call.addIntegerParam("datasetVersion");
        call.addIntegerParam("gridId");
        call.addStringParam("userName");
        call.addStringParam("dirName");
        call.addStringParam("pollutant");
        call.setVoidReturnType();

        call.request(new Object[] { new Integer(datasetId), new Integer(datasetVersion), new Integer(gridId), userName, dirName, pollutant });
    }

    public void exportFastOutputToNetCDFFile(int datasetId, int datasetVersion, int gridId, String userName, String dirName,
            String pollutant) throws EmfException {
        EmfCall call = call();

        call.setOperation("exportFastOutputToNetCDFFile");
        call.addIntegerParam("datasetId");
        call.addIntegerParam("datasetVersion");
        call.addIntegerParam("gridId");
        call.addStringParam("userName");
        call.addStringParam("dirName");
        call.addStringParam("pollutant");
        call.setVoidReturnType();

        call.request(new Object[] { new Integer(datasetId), new Integer(datasetVersion), new Integer(gridId), userName, dirName, pollutant });
    }

    public String[] getFastRunSpeciesMappingDatasetPollutants(int datasetId, int datasetVersion) throws EmfException {
        EmfCall call = call();

        call.setOperation("getFastRunSpeciesMappingDatasetPollutants");
        call.addIntegerParam("datasetId");
        call.addIntegerParam("datasetVersion");
        call.setStringArrayReturnType();

        return (String[])call.requestResponse(new Object[] { new Integer(datasetId), new Integer(datasetVersion) });
    }
}
