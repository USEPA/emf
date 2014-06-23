package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface CaseService {

    Abbreviation[] getAbbreviations() throws EmfException;

    AirQualityModel[] getAirQualityModels() throws EmfException;

    AirQualityModel addAirQualityModel(AirQualityModel airQModel) throws EmfException;

    CaseCategory[] getCaseCategories() throws EmfException;

    EmissionsYear[] getEmissionsYears() throws EmfException;

    EmissionsYear addEmissionsYear(EmissionsYear emissYear) throws EmfException;
    
    MeteorlogicalYear addMeteorologicalYear(MeteorlogicalYear metYear) throws EmfException;

    MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException;

    Speciation[] getSpeciations() throws EmfException;

    Speciation addSpeciation(Speciation speciation) throws EmfException;
    
    InputName[] getInputNames() throws EmfException;

    InputEnvtVar[] getInputEnvtVars() throws EmfException;
 
    CaseProgram[] getPrograms() throws EmfException;

    ModelToRun[] getModelToRuns() throws EmfException;

    SubDir[] getSubDirs() throws EmfException;

    Case[] getCases() throws EmfException;
    
    Case[] getCases(String nameContains) throws EmfException;

    Case addCase(User owner, Case element) throws EmfException;

    CaseCategory addCaseCategory(CaseCategory element) throws EmfException;
    
    Abbreviation addAbbreviation(Abbreviation abbr) throws EmfException;

    void removeCase(Case element) throws EmfException;

    Case obtainLocked(User owner, Case element) throws EmfException;

    Case releaseLocked(User owner, Case locked) throws EmfException;

    Case updateCase(Case element) throws EmfException;
    
    Case reloadCase(int caseId) throws EmfException;
    
    Case getCaseFromName(String name) throws EmfException;
    
    InputName addCaseInputName(InputName name) throws EmfException;

    CaseProgram addProgram(CaseProgram program) throws EmfException;
    
    ModelToRun addModelToRun(ModelToRun model) throws EmfException;

    InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException;

    SubDir addSubDir(SubDir subdir) throws EmfException;

    CaseInput addCaseInput(User user, CaseInput input) throws EmfException;
    
    void addCaseInputs(User user, int caseId, CaseInput[] inputs) throws EmfException;
    
    void updateCaseInput(User user, CaseInput input) throws EmfException;
    
    void removeCaseInputs(User user, CaseInput[] inputs) throws EmfException;
    
    void removeCaseOutputs(User user, CaseOutput[] outputs, boolean deleteDataset) throws EmfException;
    
    CaseInput[] getCaseInputs(int caseId) throws EmfException;
    
    CaseInput[] getCaseInputs(int caseId, int[] jobIds) throws EmfException;
    
    CaseInput[] getCaseInputs(int pagesize, int caseId, Sector sector, String envNameContains, boolean showAll) throws EmfException;
    
    String validateNLInputs(int caseId) throws EmfException;
    
    String validateNLParameters(int caseId) throws EmfException;
    
    void copyCaseObject(int[] toCopy, User user) throws EmfException;
    
    CaseJob addCaseJob(User user, CaseJob job) throws EmfException;
    
    CaseJob getCaseJob(int jobId) throws EmfException;
    
    CaseJob[] getCaseJobs(int caseId) throws EmfException;
    
    //Sector[] getSectorsUsedbyJobs(int caseId) throws EmfException;
    
    int[] getJobIds(int caseId, String[] jobNames) throws EmfException;
    
    CaseJob[] getDependentJobs(int jobId) throws EmfException;
    
    CaseJob[] getAllValidJobs(int jobId, int caseId) throws EmfException;

    JobRunStatus[] getJobRunStatuses() throws EmfException;
    
    void removeCaseJobs(User user, CaseJob[] jobs) throws EmfException;

    void updateCaseJob(User user, CaseJob job) throws EmfException;
    
    void saveCaseJobFromClient(User user, CaseJob job) throws EmfException;

    Host[] getHosts() throws EmfException;

    Host addHost(Host host) throws EmfException;
        
    Executable addExecutable(Executable exe) throws EmfException;
    
    ParameterEnvVar addParameterEnvVar(ParameterEnvVar envVar) throws EmfException;
    
    ParameterEnvVar[] getParameterEnvVars() throws EmfException;

    ValueType addValueType(ValueType type) throws EmfException;
    
    ValueType[] getValueTypes() throws EmfException;

    ParameterName addParameterName(ParameterName name) throws EmfException;
    
    ParameterName[] getParameterNames() throws EmfException;
    
    CaseParameter addCaseParameter(User user, CaseParameter param) throws EmfException;
    
    void removeCaseParameters(User user, CaseParameter[] params) throws EmfException;
    
    CaseParameter[] getCaseParameters(int caseId) throws EmfException;
    
    CaseParameter[] getCaseParameters(int caseId, int[] jobIds) throws EmfException;
    
    CaseParameter[] getCaseParameters(int pageSize, int caseId, Sector sector, String envNameContains, boolean showAll) throws EmfException;
    
    CaseParameter getCaseParameter(int caseId, ParameterEnvVar var) throws EmfException;
    
    void updateCaseParameter(User user, CaseParameter parameter) throws EmfException;

    void runJobs(CaseJob[] jobs, User user) throws EmfException;
    String runJobs(Integer[] jobIds, int caseId, User user) throws EmfException;
    String getJobStatusMessage(int caseId) throws EmfException;
    void updateCaseJobStatus(CaseJob job) throws EmfException;

    // Used for CaseService ExportInputs
    void exportCaseInputs(User user, Integer[] caseInputIds, String purpose) throws EmfException;
    void exportCaseInputsWithOverwrite(User user, Integer[] caseInputIds, String purpose) throws EmfException;
    
    // For command line client
    int recordJobMessage(JobMessage message, String jobKey) throws EmfException;
    int recordJobMessage(JobMessage[] msgs, String[] keys) throws EmfException;
    JobMessage[] getJobMessages(int caseId, int jobId) throws EmfException;
    
//    void registerOutput(CaseOutput output, String jobKey) throws EmfException;
    void registerOutputs(CaseOutput[] outputs, String[] jobKeys) throws EmfException;
    CaseOutput[] getCaseOutputs(int caseId, int jobId) throws EmfException;
    CaseOutput addCaseOutput(User user, CaseOutput output) throws EmfException;
    void updateCaseOutput(User user, CaseOutput output) throws EmfException;
    
    String restoreTaskManagers() throws EmfException;

    String printStatusCaseJobTaskManager() throws EmfException;
    
    Case[] getCases(CaseCategory category) throws EmfException;

    Case[] getCases(CaseCategory category, String nameContains) throws EmfException;
    
    String validateJobs(Integer[] jobIDs) throws EmfException;
    
    Case updateCaseWithLock(Case caseObj) throws EmfException;

    void removeMessages(User user, JobMessage[] msgs) throws EmfException;

    String[] getAllCaseNameIDs() throws EmfException;

    void addCaseJobs(User user, int caseId, CaseJob[] jobs) throws EmfException;

    void addCaseParameters(User user, int caseID, CaseParameter[] params) throws EmfException;
    
    Case mergeCases(User user, int parentCaseId, int templateCaseId, int[] jobIds, String jobGroup, Case sensitivityCase) throws EmfException;
    
    Case addSensitivity2Case(User user, int parentCaseId, int templateCaseId, int[] jobIds, String jobGroup, Case sensitivityCase, GeoRegion geoRegion) throws EmfException;
    
    String checkParentCase(Case caseObj) throws EmfException;

    Case[] getSensitivityCases(int parentCaseId) throws EmfException;
    
    String[] getJobGroups(int caseId) throws EmfException;

    void printCase(String folder, int caseId) throws EmfException;
    
    String[] printLocalCase(int caseId) throws EmfException;
    
    Case[] getCasesThatInputToOtherCases(int caseId) throws EmfException;
    
    Case[] getCasesThatOutputToOtherCases(int caseId) throws EmfException;
    
    Case[] getCasesByOutputDatasets(int[] datasetIds) throws EmfException;
    
    Case[] getCasesByInputDataset(int datasetId) throws EmfException;
    
    void importCase(String folder, String[] files, User user) throws EmfException;
    
    String loadCMAQCase(String path, int jobId, int caseId, User user) throws EmfException;

    int cancelJobs(int[] jobIds, User user) throws EmfException;
    
    String[] isGeoRegionUsed(int caseId, GeoRegion[] grids) throws EmfException;
    
    String isGeoRegionInSummary(int caseId, GeoRegion[] grids) throws EmfException;

    String getCaseComparisonResult(int[] caseIds) throws EmfException;
    
    String getCaseQaReports(User user, int[] caseIds, String gridName, Sector[] sectors, 
            String[] repDims, String whereClause, String serverDir) throws EmfException;

 }
