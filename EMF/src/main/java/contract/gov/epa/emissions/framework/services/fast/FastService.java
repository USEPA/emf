package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;

import java.util.Date;
import java.util.List;

public interface FastService extends EMFService {

    FastRun[] getFastRuns() throws EmfException;

    FastRun[] getFastRuns(int gridId) throws EmfException;

    FastRunOutput[] getFastRunOutputs(int fastRunId) throws EmfException;

    int addFastRun(FastRun fastRun) throws EmfException;

    void removeFastRuns(int[] ids, User user) throws EmfException;

    FastRun obtainLockedFastRun(User owner, int id) throws EmfException;

    void releaseLockedFastRun(User user, int id) throws EmfException;

    FastRun updateFastRun(FastRun fastRun) throws EmfException;

    FastRun updateFastRunWithLock(FastRun fastRun) throws EmfException;

    void runFastRun(User user, int fastRunId) throws EmfException;

    List<FastRun> getFastRunsByRunStatus(String runStatus) throws EmfException;

    void stopFastRun(int fastRunId) throws EmfException;

    int copyFastRun(int id, User creator) throws EmfException;

    FastRun getFastRun(int fastRunId) throws EmfException;

    void setFastRunRunStatusAndCompletionDate(int id, String runStatus, Date completionDate) throws EmfException;

    // StrategyType[] getEquaitonTypes();

    Long getFastRunRunningCount() throws EmfException;

    public String getDefaultExportDirectory() throws EmfException;

    public String getFastRunStatus(int id) throws EmfException;

    public FastDataset[] getFastDatasets() throws EmfException;

    public int getFastDatasetCount() throws EmfException;

    public FastDataset getFastDataset(int fastDatasetId) throws EmfException;

    public int addFastDataset(FastDataset fastDataset) throws EmfException;

    public void removeFastDataset(int fastDatasetId, User user) throws EmfException;

    public FastNonPointDataset[] getFastNonPointDatasets() throws EmfException;

    public FastNonPointDataset getFastNonPointDataset(int fastNonPointDatasetId) throws EmfException;

    public int getFastNonPointDatasetCount() throws EmfException;

    public int addFastNonPointDataset(FastNonPointDataset fastNonPointDataset, User user) throws EmfException;

    // public int addFastNonPointDataset(String newInventoryDatasetName, String baseNonPointDatasetName,
    // int baseNonPointDatasetVersion, String griddedSMKDatasetName,
    // int griddedSMKDatasetVersion, String invTableDatasetName,
    // int invTableDatasetVersion, String gridName,
    // String userName) throws EmfException;

    public void removeFastNonPointDataset(int fastNonPointDatasetId, User user) throws EmfException;

    public Grid[] getGrids() throws EmfException;

    public Grid getGrid(String name) throws EmfException;

    FastAnalysis[] getFastAnalyses() throws EmfException;

    FastAnalysisOutput[] getFastAnalysisOutputs(int fastAnalysisId) throws EmfException;

    int addFastAnalysis(FastAnalysis fastAnalysis) throws EmfException;

    void removeFastAnalyses(int[] ids, User user) throws EmfException;

    FastAnalysis obtainLockedFastAnalysis(User owner, int id) throws EmfException;

    void releaseLockedFastAnalysis(User user, int id) throws EmfException;

    FastAnalysis updateFastAnalysis(FastAnalysis fastRun) throws EmfException;

    FastAnalysis updateFastAnalysisWithLock(FastAnalysis fastRun) throws EmfException;

    void runFastAnalysis(User user, int fastAnalysisId) throws EmfException;

    List<FastAnalysis> getFastAnalysesByRunStatus(String runStatus) throws EmfException;

    void stopFastAnalysis(int fastAnalysisId) throws EmfException;

    int copyFastAnalysis(int id, User creator) throws EmfException;

    FastAnalysis getFastAnalysis(int fastAnalysisId) throws EmfException;

    void setFastAnalysisRunStatusAndCompletionDate(int id, String runStatus, Date completionDate) throws EmfException;

    // StrategyType[] getEquaitonTypes();

    Long getFastAnalysisRunningCount() throws EmfException;

    public String getFastAnalysisStatus(int id) throws EmfException;

    FastAnalysisOutputType[] getFastAnalysisOutputTypes() throws EmfException;

    FastAnalysisOutputType getFastAnalysisOutputType(String name) throws EmfException;

    FastRunOutputType[] getFastRunOutputTypes() throws EmfException;

    FastRunOutputType getFastRunOutputType(String name) throws EmfException;

    void exportFastOutputToNetCDFFile(int datasetId, int datasetVersion, int gridId, String userName, 
            String dirName, String pollutant) throws EmfException;

    void exportFastOutputToShapeFile(int datasetId, int datasetVersion, int gridId, String userName, 
            String dirName, String pollutant) throws EmfException;

    String[] getFastRunSpeciesMappingDatasetPollutants(int datasetId, int datasetVersion) throws EmfException;

}