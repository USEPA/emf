package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.AirQualityModel;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.EmissionsYear;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;
import gov.epa.emissions.framework.services.casemanagement.ModelToRun;
import gov.epa.emissions.framework.services.casemanagement.Speciation;
import gov.epa.emissions.framework.services.casemanagement.SubDir;
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

public class CaseServiceTransport implements CaseService {

    private CallFactory callFactory;

    private DataMappings dataMappings;

    private CaseMappings caseMappings;

    private EmfCall emfCall;

    public CaseServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        dataMappings = new DataMappings();
        caseMappings = new CaseMappings();
    }

    private EmfCall call() throws EmfException {
        if (emfCall == null)
            emfCall = callFactory.createSessionEnabledCall("Case Service");

        return this.emfCall;
    }

    public synchronized Case[] getCases() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCases");
        call.setReturnType(caseMappings.cases());

        return (Case[]) call.requestResponse(new Object[] {});
    }
    
    public synchronized Case[] getCases(String nameContains) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCases");
        call.addStringParam("nameContains");
        call.setReturnType(caseMappings.cases());

        return (Case[]) call.requestResponse(new Object[] {nameContains});
    }

    public synchronized Case reloadCase(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("reloadCase");
        call.addIntegerParam("caseId");
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public synchronized Abbreviation[] getAbbreviations() throws EmfException {
        EmfCall call = call();

        call.setOperation("getAbbreviations");
        call.setReturnType(caseMappings.abbreviations());

        return (Abbreviation[]) call.requestResponse(new Object[] {});
    }

    public synchronized Abbreviation addAbbreviation(Abbreviation abbr) throws EmfException {
        EmfCall call = call();

        call.setOperation("addAbbreviation");
        call.addParam("abbr", caseMappings.abbreviation());
        call.setReturnType(caseMappings.abbreviation());

        return (Abbreviation) call.requestResponse(new Object[] { abbr });
    }

    public synchronized AirQualityModel[] getAirQualityModels() throws EmfException {
        EmfCall call = call();

        call.setOperation("getAirQualityModels");
        call.setReturnType(caseMappings.airQualityModels());

        return (AirQualityModel[]) call.requestResponse(new Object[] {});
    }

    public synchronized CaseCategory[] getCaseCategories() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseCategories");
        call.setReturnType(caseMappings.caseCategories());

        return (CaseCategory[]) call.requestResponse(new Object[] {});
    }

    public synchronized CaseCategory addCaseCategory(CaseCategory element) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseCategory");
        call.addParam("element", caseMappings.caseCategory());
        call.setReturnType(caseMappings.caseCategory());

        return (CaseCategory) call.requestResponse(new Object[] { element });
    }

    public synchronized EmissionsYear[] getEmissionsYears() throws EmfException {
        EmfCall call = call();

        call.setOperation("getEmissionsYears");
        call.setReturnType(caseMappings.emissionsYears());

        return (EmissionsYear[]) call.requestResponse(new Object[] {});
    }

    public synchronized MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        EmfCall call = call();

        call.setOperation("getMeteorlogicalYears");
        call.setReturnType(caseMappings.meteorlogicalYears());

        return (MeteorlogicalYear[]) call.requestResponse(new Object[] {});
    }

    public synchronized Speciation[] getSpeciations() throws EmfException {
        EmfCall call = call();

        call.setOperation("getSpeciations");
        call.setReturnType(caseMappings.speciations());

        return (Speciation[]) call.requestResponse(new Object[] {});
    }

    public synchronized Case addCase(User user, Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCase");
        call.addParam("user", dataMappings.user());
        call.addParam("element", caseMappings.caseObject());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { user, element });
    }

    public synchronized void removeCase(Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeCase");
        call.addParam("element", caseMappings.caseObject());
        call.setVoidReturnType();

        call.request(new Object[] { element });
    }

    public synchronized Case obtainLocked(User owner, Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLocked");
        call.addParam("owner", dataMappings.user());
        call.addParam("element", caseMappings.caseObject());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { owner, element });
    }

    public synchronized Case releaseLocked(User owner, Case locked) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLocked");
        call.addParam("owner", dataMappings.user());
        call.addParam("locked", caseMappings.caseObject());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { owner, locked });
    }

    public synchronized Case updateCase(Case element) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCase");
        call.addParam("element", caseMappings.caseObject());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { element });
    }

    public synchronized CaseInput[] getCaseInputs() throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseInputs");
        call.setReturnType(caseMappings.caseinputs());

        return (CaseInput[]) call.requestResponse(new Object[] {});
    }

    public synchronized InputName[] getInputNames() throws EmfException {
        EmfCall call = call();

        call.setOperation("getInputNames");
        call.setReturnType(caseMappings.inputnames());

        return (InputName[]) call.requestResponse(new Object[] {});
    }

    public synchronized InputEnvtVar[] getInputEnvtVars() throws EmfException {
        EmfCall call = call();

        call.setOperation("getInputEnvtVars");
        call.setReturnType(caseMappings.inputEnvtVars());

        return (InputEnvtVar[]) call.requestResponse(new Object[] {});
    }

    public synchronized CaseProgram[] getPrograms() throws EmfException {
        EmfCall call = call();

        call.setOperation("getPrograms");
        call.setReturnType(caseMappings.programs());

        return (CaseProgram[]) call.requestResponse(new Object[] {});
    }

    public synchronized void export(User user, String dirName, String purpose, boolean overWrite, int caseId)
            throws EmfException {
        EmfCall call = call();

        call.setOperation("export");
        call.addParam("user", dataMappings.user());
        call.addStringParam("dirName");
        call.addStringParam("purpose");
        call.addBooleanParameter("overWrite");
        call.addParam("caseId", caseMappings.integer());
        call.setVoidReturnType();

        call.request(new Object[] { user, dirName, purpose, Boolean.valueOf(overWrite), Integer.valueOf(caseId) });
    }

    public synchronized InputName addCaseInputName(InputName name) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseInputName");
        call.addParam("name", caseMappings.inputname());
        call.setReturnType(caseMappings.inputname());

        return (InputName) call.requestResponse(new Object[] { name });
    }

    public synchronized CaseProgram addProgram(CaseProgram program) throws EmfException {
        EmfCall call = call();

        call.setOperation("addProgram");
        call.addParam("program", caseMappings.program());
        call.setReturnType(caseMappings.program());

        return (CaseProgram) call.requestResponse(new Object[] { program });
    }

    public synchronized InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException {
        EmfCall call = call();

        call.setOperation("addInputEnvtVar");
        call.addParam("inputEnvtVar", caseMappings.inputEnvtVar());
        call.setReturnType(caseMappings.inputEnvtVar());

        return (InputEnvtVar) call.requestResponse(new Object[] { inputEnvtVar });
    }

    public synchronized ModelToRun[] getModelToRuns() throws EmfException {
        EmfCall call = call();

        call.setOperation("getModelToRuns");
        call.setReturnType(caseMappings.modelToRuns());

        return (ModelToRun[]) call.requestResponse(new Object[] {});
    }

    public synchronized ModelToRun addModelToRun(ModelToRun model) throws EmfException {
        EmfCall call = call();

        call.setOperation("addModelToRun");
        call.addParam("model", caseMappings.modelToRun());
        call.setReturnType(caseMappings.modelToRun());

        return (ModelToRun) call.requestResponse(new Object[] { model });
    }

    public synchronized SubDir[] getSubDirs() throws EmfException {
        EmfCall call = call();

        call.setOperation("getSubDirs");
        call.setReturnType(caseMappings.subdirs());

        return (SubDir[]) call.requestResponse(new Object[] {});
    }

    public synchronized SubDir addSubDir(SubDir subdir) throws EmfException {
        EmfCall call = call();

        call.setOperation("addSubDir");
        call.addParam("subdir", caseMappings.subdir());
        call.setReturnType(caseMappings.subdir());

        return (SubDir) call.requestResponse(new Object[] { subdir });
    }

    public synchronized void addCaseInputs(User user, int caseId, CaseInput[] inputs) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseInputs");
        call.addParam("user", dataMappings.user());
        call.addIntegerParam("caseId");
        call.addParam("inputs", caseMappings.caseinputs());
        call.setVoidReturnType();

        call.request(new Object[] { user, new Integer(caseId), inputs });
    }

    public synchronized CaseInput addCaseInput(User user, CaseInput input) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseInput");
        call.addParam("user", dataMappings.user());
        call.addParam("input", caseMappings.caseinput());
        call.setReturnType(caseMappings.caseinput());

        return (CaseInput) call.requestResponse(new Object[] { user, input });
    }

    public synchronized void updateCaseInput(User user, CaseInput input) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCaseInput");
        call.addParam("user", dataMappings.user());
        call.addParam("input", caseMappings.caseinput());
        call.setVoidReturnType();

        call.request(new Object[] { user, input });
    }

    public synchronized void removeCaseInputs(User user, CaseInput[] inputs) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeCaseInputs");
        call.addParam("user", dataMappings.user());
        call.addParam("inputs", caseMappings.caseinputs());
        call.setVoidReturnType();

        call.request(new Object[] { user, inputs });
    }

    public synchronized void removeCaseOutputs(User user, CaseOutput[] outputs, boolean deleteDataset)
            throws EmfException {
        EmfCall call = call();

        call.setOperation("removeCaseOutputs");
        call.addParam("user", dataMappings.user());
        call.addParam("outputs", caseMappings.caseOutputs());
        call.addBooleanParameter("deleteDataset");
        call.setVoidReturnType();

        call.request(new Object[] { user, outputs, deleteDataset });
    }

    public synchronized CaseInput[] getCaseInputs(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseInputs");
        call.addParam("caseId", dataMappings.integer());
        call.setReturnType(caseMappings.caseinputs());

        return (CaseInput[]) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public synchronized CaseInput[] getCaseInputs(int caseId, int[] jobIds) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseInputs");
        call.addParam("caseId", dataMappings.integer());
        call.addIntArrayParam();
        call.setReturnType(caseMappings.caseinputs());

        return (CaseInput[]) call.requestResponse(new Object[] { new Integer(caseId), jobIds });
    }

    public synchronized CaseInput[] getCaseInputs(int pageSize, int caseId, Sector sector, 
            String envNameContains, boolean showAll) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseInputs");
        call.addParam("pageSize", dataMappings.integer());
        call.addParam("caseId", dataMappings.integer());
        call.addParam("sector", dataMappings.sector());
        call.addStringParam("envNameContains");
        call.addBooleanParameter("showAll");
        call.setReturnType(caseMappings.caseinputs());

        return (CaseInput[]) call.requestResponse(new Object[] { new Integer(pageSize), new Integer(caseId), sector,
                envNameContains, showAll });
    }

    public void copyCaseObject(int[] toCopy, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("copyCaseObject");
        call.addIntArrayParam();
        call.addParam("user", dataMappings.user());
        call.setReturnType(caseMappings.cases());

        call.requestResponse(new Object[] { toCopy, user });
    }

    public synchronized CaseJob addCaseJob(User user, CaseJob job) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseJob");
        call.addParam("user", dataMappings.user());
        call.addParam("job", caseMappings.casejob());
        call.setReturnType(caseMappings.casejob());

        return (CaseJob) call.requestResponse(new Object[] { user, job });
    }

    public synchronized void addCaseJobs(User user, int caseId, CaseJob[] jobs) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseJobs");
        call.addParam("user", dataMappings.user());
        call.addIntegerParam("caseId");
        call.addParam("jobs", caseMappings.casejobs());
        call.setVoidReturnType();

        call.request(new Object[] { user, new Integer(caseId), jobs });
    }

    public synchronized CaseJob[] getCaseJobs(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseJobs");
        call.addIntegerParam("caseId");
        call.setReturnType(caseMappings.casejobs());

        return (CaseJob[]) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public synchronized CaseJob getCaseJob(int jobId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseJob");
        call.addIntegerParam("jobId");
        call.setReturnType(caseMappings.casejob());

        return (CaseJob) call.requestResponse(new Object[] { new Integer(jobId) });
    }

    public synchronized JobRunStatus[] getJobRunStatuses() throws EmfException {
        EmfCall call = call();

        call.setOperation("getJobRunStatuses");
        call.setReturnType(caseMappings.jobRunStatuses());

        return (JobRunStatus[]) call.requestResponse(new Object[] {});
    }

    public synchronized Executable[] getExecutables(int casejobId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getExecutables");
        call.setReturnType(caseMappings.executables());

        return (Executable[]) call.requestResponse(new Object[] {});
    }

    public synchronized void removeCaseJobs(User user, CaseJob[] jobs) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeCaseJobs");
        call.addParam("user", dataMappings.user());
        call.addParam("jobs", caseMappings.casejobs());
        call.setVoidReturnType();

        call.request(new Object[] { user, jobs });
    }

    public synchronized void updateCaseJob(User user, CaseJob job) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCaseJob");
        call.addParam("user", dataMappings.user());
        call.addParam("job", caseMappings.casejob());
        call.setVoidReturnType();

        call.request(new Object[] { user, job });
    }

    public synchronized void updateCaseJobStatus(CaseJob job) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCaseJobStatus");
        call.addParam("job", caseMappings.casejob());
        call.setVoidReturnType();

        call.request(new Object[] { job });
    }

    public synchronized void saveCaseJobFromClient(User user, CaseJob job) throws EmfException {
        EmfCall call = call();

        call.setOperation("saveCaseJobFromClient");
        call.addParam("user", dataMappings.user());
        call.addParam("job", caseMappings.casejob());
        call.setVoidReturnType();

        call.request(new Object[] { user, job });
    }

    public synchronized Host[] getHosts() throws EmfException {
        EmfCall call = call();

        call.setOperation("getHosts");
        call.setReturnType(caseMappings.hosts());

        return (Host[]) call.requestResponse(new Object[] {});
    }

    public synchronized Host addHost(Host host) throws EmfException {
        EmfCall call = call();

        call.setOperation("addHost");
        call.addParam("host", caseMappings.host());
        call.setReturnType(caseMappings.host());

        return (Host) call.requestResponse(new Object[] { host });
    }

    public synchronized Executable addExecutable(Executable exe) throws EmfException {
        EmfCall call = call();

        call.setOperation("addExecutable");
        call.addParam("exe", caseMappings.executable());
        call.setReturnType(caseMappings.executable());

        return (Executable) call.requestResponse(new Object[] { exe });
    }

    public synchronized ParameterEnvVar addParameterEnvVar(ParameterEnvVar envVar) throws EmfException {
        EmfCall call = call();

        call.setOperation("addParameterEnvVar");
        call.addParam("envVar", caseMappings.parameterEnvVar());
        call.setReturnType(caseMappings.parameterEnvVar());

        return (ParameterEnvVar) call.requestResponse(new Object[] { envVar });
    }

    public synchronized ParameterEnvVar[] getParameterEnvVars() throws EmfException {
        EmfCall call = call();

        call.setOperation("getParameterEnvVars");
        call.setReturnType(caseMappings.parameterEnvVars());

        return (ParameterEnvVar[]) call.requestResponse(new Object[] {});
    }

    public synchronized ValueType addValueType(ValueType type) throws EmfException {
        EmfCall call = call();

        call.setOperation("addValueType");
        call.addParam("type", caseMappings.valueType());
        call.setReturnType(caseMappings.valueType());

        return (ValueType) call.requestResponse(new Object[] { type });
    }

    public synchronized ValueType[] getValueTypes() throws EmfException {
        EmfCall call = call();

        call.setOperation("getValueTypes");
        call.setReturnType(caseMappings.valueTypes());

        return (ValueType[]) call.requestResponse(new Object[] {});
    }

    public synchronized ParameterName addParameterName(ParameterName name) throws EmfException {
        EmfCall call = call();

        call.setOperation("addParameterName");
        call.addParam("name", caseMappings.parameterName());
        call.setReturnType(caseMappings.parameterName());

        return (ParameterName) call.requestResponse(new Object[] { name });
    }

    public synchronized ParameterName[] getParameterNames() throws EmfException {
        EmfCall call = call();

        call.setOperation("getParameterNames");
        call.setReturnType(caseMappings.parameterNames());

        return (ParameterName[]) call.requestResponse(new Object[] {});
    }

    public synchronized CaseParameter addCaseParameter(User user, CaseParameter param) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseParameter");
        call.addParam("user", dataMappings.user());
        call.addParam("param", caseMappings.caseParameter());
        call.setReturnType(caseMappings.caseParameter());

        return (CaseParameter) call.requestResponse(new Object[] { user, param });
    }

    public synchronized void addCaseParameters(User user, int caseID, CaseParameter[] params) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseParameters");
        call.addParam("user", dataMappings.user());
        call.addIntegerParam("caseId");
        call.addParam("params", caseMappings.caseParameters());
        call.setVoidReturnType();

        call.request(new Object[] { user, new Integer(caseID), params });
    }

    public synchronized CaseParameter[] getCaseParameters(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseParameters");
        call.addIntegerParam("caseId");
        call.setReturnType(caseMappings.caseParameters());

        return (CaseParameter[]) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public CaseParameter[] getCaseParameters(int caseId, int[] jobIds) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseParameters");
        call.addIntegerParam("caseId");
        call.addIntArrayParam();
        call.setReturnType(caseMappings.caseParameters());

        return (CaseParameter[]) call.requestResponse(new Object[] { new Integer(caseId), jobIds });
    }

    public synchronized CaseParameter[] getCaseParameters(int pageSize, int caseId, Sector sector, 
            String envNameContains, boolean showAll) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseParameters");
        call.addIntegerParam("pageSize");
        call.addIntegerParam("caseId");
        call.addParam("sector", dataMappings.sector());
        call.addStringParam("envNameContains");
        call.addBooleanParameter("showAll");
        call.setReturnType(caseMappings.caseParameters());

        return (CaseParameter[]) call.requestResponse(new Object[] { new Integer(pageSize), new Integer(caseId),
                sector, envNameContains, showAll });
    }

    public synchronized void removeCaseParameters(User user, CaseParameter[] params) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeCaseParameters");
        call.addParam("user", dataMappings.user());
        call.addParam("params", caseMappings.caseParameters());
        call.setVoidReturnType();

        call.request(new Object[] { user, params });
    }

    public synchronized void updateCaseParameter(User user, CaseParameter parameter) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCaseParameter");
        call.addParam("user", dataMappings.user());
        call.addParam("parameter", caseMappings.caseParameter());
        call.setVoidReturnType();

        call.request(new Object[] { user, parameter });
    }

    public synchronized void runJobs(CaseJob[] jobs, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("runJobs");
        call.addParam("jobs", caseMappings.casejobs());
        call.addParam("user", dataMappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { jobs, user });
    }

    public synchronized String runJobs(Integer[] jobIds, int caseId, User user) throws EmfException {
        EmfCall call = call();
        call.setOperation("runJobs");
        call.addParam("jobids", caseMappings.casejobIds());
        call.addParam("caseId", caseMappings.integer());
        call.addParam("user", dataMappings.user());
        call.setStringReturnType();
        return (String) call.requestResponse(new Object[] { jobIds, Integer.valueOf(caseId), user });
    }

    public synchronized void exportCaseInputs(User user, Integer[] caseInputIds, String purpose) throws EmfException {
        EmfCall call = call();
        call.setOperation("exportCaseInputs");
        call.addParam("user", dataMappings.user());
        call.addParam("caseInputIds", caseMappings.caseInputIds());
        call.addStringParam("purpose");

        call.setVoidReturnType();
        call.request(new Object[] { user, caseInputIds, purpose });

    }

    public synchronized void exportCaseInputsWithOverwrite(User user, Integer[] caseInputIds, String purpose)
            throws EmfException {
        EmfCall call = call();
        call.setOperation("exportCaseInputsWithOverwrite");
        call.addParam("user", dataMappings.user());
        call.addParam("caseInputIds", caseMappings.caseInputIds());
        call.addStringParam("purpose");

        call.setVoidReturnType();
        call.request(new Object[] { user, caseInputIds, purpose });

    }

    public synchronized int recordJobMessage(JobMessage message, String jobKey) throws EmfException {
        EmfCall call = call();

        call.setOperation("recordJobMessage");
        call.addParam("message", caseMappings.jobMessage());
        call.addStringParam("jobKey");
        call.setIntegerReturnType();
        call.setTimeOut(40000); // set time out in milliseconds to terminate if service doesn't response

        return (Integer) call.requestResponse(new Object[] { message, jobKey });
    }

    public synchronized int recordJobMessage(JobMessage[] msgs, String[] keys) throws EmfException {
        EmfCall call = call();

        call.setOperation("recordJobMessage");
        call.addParam("msgs", caseMappings.jobMessages());
        call.addParam("keys", caseMappings.strings());
        call.setIntegerReturnType();
        call.setTimeOut(40000); // set time out in milliseconds to terminate if service doesn't response

        return (Integer) call.requestResponse(new Object[] { msgs, keys });
    }

    public synchronized JobMessage[] getJobMessages(int caseId, int jobId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getJobMessages");
        call.addIntegerParam("caseId");
        call.addIntegerParam("jobId");
        call.setReturnType(caseMappings.jobMessages());

        return (JobMessage[]) call.requestResponse(new Object[] { new Integer(caseId), new Integer(jobId) });
    }

    public synchronized CaseJob[] getAllValidJobs(int jobId, int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getAllValidJobs");
        call.addIntegerParam("jobId");
        call.addIntegerParam("caseId");
        call.setReturnType(caseMappings.casejobIds());

        return (CaseJob[]) call.requestResponse(new Object[] { new Integer(jobId), new Integer(caseId) });
    }

    public synchronized CaseJob[] getDependentJobs(int jobId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getDependentJobs");
        call.addIntegerParam("jobId");
        call.setReturnType(caseMappings.casejobIds());

        return (CaseJob[]) call.requestResponse(new Object[] { new Integer(jobId) });
    }

    public synchronized int[] getJobIds(int caseId, String[] names) throws EmfException {
        EmfCall call = call();

        call.setOperation("getJobIds");
        call.addIntegerParam("caseId");
        call.addParam("names", caseMappings.strings());
        call.setIntArrayReturnType();

        return (int[]) call.requestResponse(new Object[] { new Integer(caseId), names });
    }

    public synchronized String restoreTaskManagers() throws EmfException {
        EmfCall call = call();
        call.setOperation("restoreTaskManagers");
        call.setStringReturnType();
        return (String) call.requestResponse(new Object[] {});
    }

    public synchronized String printStatusCaseJobTaskManager() throws EmfException {
        EmfCall call = call();
        call.setOperation("printStatusCaseJobTaskManager");
        call.setStringReturnType();
        return (String) call.requestResponse(new Object[] {});
    }

    public synchronized Case[] getCases(CaseCategory category) throws EmfException {
        EmfCall call = call();
        call.setOperation("getCases");
        call.addParam("category", caseMappings.caseCategory());
        call.setReturnType(caseMappings.cases());

        return (Case[]) call.requestResponse(new Object[] { category });
    }
    
    public synchronized Case[] getCases(CaseCategory category, String nameContains) throws EmfException {
        EmfCall call = call();
        call.setOperation("getCases");
        call.addParam("category", caseMappings.caseCategory());
        call.addStringParam("nameContains");
        call.setReturnType(caseMappings.cases());

        return (Case[]) call.requestResponse(new Object[] { category, nameContains });
    }

    public String validateJobs(Integer[] jobIDs) throws EmfException {
        EmfCall call = call();
        call.setOperation("validateJobs");
        call.addParam("jobIDs", caseMappings.casejobIds());
        call.setStringReturnType();
        call.setTimeOut(40000);

        return (String) call.requestResponse(new Object[] { jobIDs });
    }

    public synchronized CaseOutput[] getCaseOutputs(int caseId, int jobId) throws EmfException {
        EmfCall call = call();
        call.setOperation("getCaseOutputs");
        call.addIntegerParam("caseId");
        call.addIntegerParam("jobId");
        call.setReturnType(caseMappings.caseOutputs());

        return (CaseOutput[]) call.requestResponse(new Object[] { new Integer(caseId), new Integer(jobId) });
    }

    public synchronized void registerOutput(CaseOutput output, String jobKey) throws EmfException {
        EmfCall call = call();
        call.setOperation("registerOutput");
        call.addParam("output", caseMappings.caseOutput());
        call.addStringParam("jobKey");
        call.setVoidReturnType();
        call.setTimeOut(40000); // set time out in milliseconds to terminate if service doesn't response

        call.request(new Object[] { output, jobKey });
    }

    public synchronized void registerOutputs(CaseOutput[] outputs, String[] jobKeys) throws EmfException {
        EmfCall call = call();

        call.setOperation("registerOutputs");
        call.addParam("outputs", caseMappings.caseOutputs());
        call.addParam("jobKeys", caseMappings.strings());
        call.setIntegerReturnType();
        call.setTimeOut(40000); // set time out in milliseconds to terminate if service doesn't response

        call.request(new Object[] { outputs, jobKeys });
    }

    public synchronized Case updateCaseWithLock(Case caseObj) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCaseWithLock");
        call.addParam("caseObj", caseMappings.caseObject());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { caseObj });
    }

    public synchronized void updateCaseOutput(User user, CaseOutput output) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateCaseOutput");
        call.addParam("user", dataMappings.user());
        call.addParam("output", caseMappings.caseOutput());
        call.setVoidReturnType();

        call.request(new Object[] { user, output });

    }

    public synchronized void removeMessages(User user, JobMessage[] msgs) throws EmfException {
        EmfCall call = call();

        call.setOperation("removeMessages");
        call.addParam("user", dataMappings.user());
        call.addParam("msgs", caseMappings.jobMessages());
        call.setVoidReturnType();

        call.request(new Object[] { user, msgs });
    }

    public synchronized CaseOutput addCaseOutput(User user, CaseOutput output) throws EmfException {
        EmfCall call = call();

        call.setOperation("addCaseOutput");
        call.addParam("user", dataMappings.user());
        call.addParam("output", caseMappings.caseOutput());
        call.setReturnType(caseMappings.caseOutput());

        return (CaseOutput) call.requestResponse(new Object[] { user, output });

    }

    public synchronized AirQualityModel addAirQualityModel(AirQualityModel airQModel) throws EmfException {
        EmfCall call = call();

        call.setOperation("addAirQualityModel");
        call.addParam("airQModel", caseMappings.airQualityModel());
        call.setReturnType(caseMappings.airQualityModel());

        return (AirQualityModel) call.requestResponse(new Object[] { airQModel });
    }

    public synchronized EmissionsYear addEmissionsYear(EmissionsYear emissYear) throws EmfException {
        EmfCall call = call();

        call.setOperation("addEmissionsYear");
        call.addParam("emissYear", caseMappings.emissionsYear());
        call.setReturnType(caseMappings.emissionsYear());

        return (EmissionsYear) call.requestResponse(new Object[] { emissYear });
    }

    public synchronized MeteorlogicalYear addMeteorologicalYear(MeteorlogicalYear metYear) throws EmfException {
        EmfCall call = call();

        call.setOperation("addMeteorologicalYear");
        call.addParam("metYear", caseMappings.meteorlogicalYear());
        call.setReturnType(caseMappings.meteorlogicalYear());

        return (MeteorlogicalYear) call.requestResponse(new Object[] { metYear });
    }

    public synchronized Speciation addSpeciation(Speciation speciation) throws EmfException {
        EmfCall call = call();

        call.setOperation("addSpeciation");
        call.addParam("speciation", caseMappings.speciation());
        call.setReturnType(caseMappings.speciation());

        return (Speciation) call.requestResponse(new Object[] { speciation });
    }

    public synchronized String getJobStatusMessage(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getJobStatusMessage");
        call.addIntegerParam("caseId");
        call.setStringReturnType();

        return (String) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public synchronized String[] getAllCaseNameIDs() throws EmfException {
        EmfCall call = call();

        call.setOperation("getAllCaseNameIDs");
        call.setStringArrayReturnType();

        return (String[]) call.requestResponse(new Object[] {});
    }

    public synchronized Case mergeCases(User user, int parentCaseId, int templateCaseId, int[] jobIds, String jobGroup,
            Case sensitivityCase) throws EmfException {
        EmfCall call = call();

        call.setOperation("mergeCases");
        call.addParam("user", dataMappings.user());
        call.addIntegerParam("parentCaseId");
        call.addIntegerParam("templateCaseId");
        call.addIntArrayParam();
        call.addStringParam("jobGroup");
        call.addParam("sensitivityCase", caseMappings.caseObject());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { user, new Integer(parentCaseId), new Integer(templateCaseId),
                jobIds, jobGroup, sensitivityCase });
    }

    public synchronized String checkParentCase(Case caseObj) throws EmfException {
        EmfCall call = call();

        call.setOperation("checkParentCase");
        call.addParam("caseObj", caseMappings.caseObject());
        call.setStringReturnType();

        return (String) call.requestResponse(new Object[] { caseObj });
    }

    public synchronized String validateNLInputs(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("validateNLInputs");
        call.addIntegerParam("caseId");
        call.setStringReturnType();

        return (String) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public synchronized String validateNLParameters(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("validateNLParameters");
        call.addIntegerParam("caseId");
        call.setStringReturnType();

        return (String) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public synchronized Case addSensitivity2Case(User user, int parentCaseId, int templateCaseId, int[] jobIds,
            String jobGroup, Case sensitivityCase, GeoRegion region) throws EmfException {
        EmfCall call = call();

        call.setOperation("addSensitivity2Case");
        call.addParam("user", dataMappings.user());
        call.addIntegerParam("parentCaseId");
        call.addIntegerParam("templateCaseId");
        call.addIntArrayParam();
        call.addStringParam("jobGroup");
        call.addParam("sensitivityCase", caseMappings.caseObject());
        call.addParam("region", dataMappings.geoRegion());
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { user, new Integer(parentCaseId), new Integer(templateCaseId),
                jobIds, jobGroup, sensitivityCase, region });
    }

    public synchronized Case[] getSensitivityCases(int parentCaseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getSensitivityCases");
        call.addIntegerParam("parentCaseId");
        call.setReturnType(caseMappings.cases());

        return (Case[]) call.requestResponse(new Object[] { new Integer(parentCaseId) });
    }

    public synchronized String[] getJobGroups(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getJobGroups");
        call.addIntegerParam("caseId");
        call.setStringArrayReturnType();

        return (String[]) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public Case getCaseFromName(String name) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCaseFromName");
        call.addStringParam("name");
        call.setReturnType(caseMappings.caseObject());

        return (Case) call.requestResponse(new Object[] { name });

    }

    public synchronized void printCase(String folder, int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("printCase");
        call.addStringParam("folder");
        call.addIntegerParam("caseId");
        call.setVoidReturnType();

        call.request(new Object[] { folder, new Integer(caseId) });
    }
    
    public synchronized String[] printLocalCase(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("printLocalCase");
        call.addIntegerParam("caseId");
        call.setStringArrayReturnType();

        return (String[]) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public Case[] getCasesThatInputToOtherCases(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCasesThatInputToOtherCases");
        call.addIntegerParam("caseId");
        call.setReturnType(caseMappings.cases());

        return (Case[]) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public Case[] getCasesThatOutputToOtherCases(int caseId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCasesThatOutputToOtherCases");
        call.addIntegerParam("caseId");
        call.setReturnType(caseMappings.cases());

        return (Case[]) call.requestResponse(new Object[] { new Integer(caseId) });
    }

    public Case[] getCasesByOutputDatasets(int[] datasetIds) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCasesByOutputDatasets");
        call.addIntArrayParam();
        call.setReturnType(caseMappings.cases());

        return (Case[]) call.requestResponse(new Object[] { datasetIds });
    }

    public Case[] getCasesByInputDataset(int datasetId) throws EmfException {
        EmfCall call = call();

        call.setOperation("getCasesByInputDataset");
        call.addIntegerParam("datasetId");
        call.setReturnType(caseMappings.cases());

        return (Case[]) call.requestResponse(new Object[] { new Integer(datasetId) });
    }

    public synchronized void importCase(String folder, String[] files, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("importCase");
        call.addStringParam("folder");
        call.addParam("files", caseMappings.strings());
        call.addParam("user", dataMappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { folder, files, user });
    }

    public synchronized String loadCMAQCase(String path, int jobId, int caseId, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("loadCMAQCase");
        call.addStringParam("path");
        call.addIntegerParam("jobId");
        call.addIntegerParam("caseId");
        call.addParam("user", dataMappings.user());
        call.setStringReturnType();

        return (String) call.requestResponse(new Object[] { path, new Integer(jobId), new Integer(caseId), user });
    }

    public CaseParameter getCaseParameter(int caseId, ParameterEnvVar var) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getCaseParameter");
        call.addIntegerParam("caseId");
        call.addParam("var", caseMappings.parameterEnvVar());
        call.setReturnType(caseMappings.caseParameter());
        
        return (CaseParameter)call.requestResponse(new Object[]{new Integer(caseId), var});
    }

    public int cancelJobs(int[] cancelJobs, User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("cancelJobs");
        call.addIntArrayParam();
        call.addParam("user", dataMappings.user());
        call.setIntegerReturnType();

        return (Integer) call.requestResponse(new Object[] { cancelJobs, user });
    }
    
    public String[] isGeoRegionUsed(int caseId, GeoRegion[] grids) throws EmfException {
        EmfCall call = call();
        call.setOperation("isGeoRegionUsed");
        call.addIntParam();
        call.addParam("grids", dataMappings.geoRegions());
        call.setStringArrayReturnType();
        
        return (String[]) call.requestResponse(new Object[] { caseId, grids });
    }
    
    public String isGeoRegionInSummary(int caseId, GeoRegion[] grids) throws EmfException{
        EmfCall call = call();
        call.setOperation("isGeoRegionInSummary");
        call.addIntParam();
        call.addParam("grids", dataMappings.geoRegions());
        call.setStringReturnType();
        
        return (String) call.requestResponse(new Object[] { caseId, grids });
    }
    
    public String getCaseComparisonResult(int[] caseIds) throws EmfException {
        EmfCall call = call();
        call.setOperation("getCaseComparisonResult");
        call.addParam("caseIds", dataMappings.integers());
        call.setStringReturnType();
        
        return (String) call.requestResponse(new Object[] { caseIds });
    }
    
    public String getCaseQaReports(User user, int[] caseIds, String gridName, Sector[] sectors, 
            String[] repDims, String whereClause, String serverDir) throws EmfException {
        EmfCall call = call();
        call.addParam("user", dataMappings.user());
        call.setOperation("getCaseQaReports"); 
        call.addParam("caseIds", dataMappings.integers());
        call.addStringParam("gridName");
        call.addParam("sectors", dataMappings.sectors());
        call.addParam("repDims", dataMappings.strings());
        call.addStringParam("whereClause");
        call.addStringParam("serverDir");
        call.setStringReturnType();
        
        return (String) call.requestResponse(new Object[] { user, caseIds, gridName, sectors, 
                repDims, whereClause, serverDir});
    }


}
