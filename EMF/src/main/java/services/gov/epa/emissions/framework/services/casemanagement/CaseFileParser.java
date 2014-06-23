package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.CommaDelimitedTokenizer;
import gov.epa.emissions.commons.io.importer.DelimitedFileReader;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.commons.util.StringTools;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CaseFileParser {
    private static Log log = LogFactory.getLog(CaseFileParser.class);
    
    private File sum_file;

    private File inputs_file;

    private File jobs_file;
    
    private String sysNewLine = System.getProperty("line.separator");

    private String[] paramColNames = new String[0];

    private String[] inputColNames = new String[0];

    private String[] jobColNames = new String[0];

    private List<CaseParameter> params = new ArrayList<CaseParameter>();

    private List<ParameterEnvVar> pEnvVars = new ArrayList<ParameterEnvVar>();

    private List<CaseInput> inputs = new ArrayList<CaseInput>();

    private List<InputEnvtVar> inEnvVars = new ArrayList<InputEnvtVar>();

    private List<CaseJob> jobs = new ArrayList<CaseJob>();

    private Case caseObj = new Case();

    public CaseFileParser(String sumFile, String inputsFile, String jobsFile) throws Exception {
        sum_file = new File(sumFile);
        inputs_file = new File(inputsFile);
        jobs_file = new File(jobsFile);
        readProcessAllSummaryLines(sum_file);
        readProcessAllInputsLines(inputs_file);
        readProcessAllJobsLines(jobs_file);
    }

    public Case getCase() {
        return this.caseObj;
    }

    public Abbreviation getCaseAbbreviation() {
        return this.caseObj.getAbbreviation();
    }

    public String[] getParamColNames() {
        return this.paramColNames;
    }

    public String[] getJobColNames() {
        return this.jobColNames;
    }

    public String[] getInputColNames() {
        return this.inputColNames;
    }

    public List<CaseJob> getJobs() {
        return this.jobs;
    }
    
    public List<CaseParameter> getParameters() {
        return this.params;
    }

    public List<CaseInput> getInputs() {
        return this.inputs;
    }

    public List<ParameterEnvVar> getParamEnvVars() {
        return this.pEnvVars;
    }

    public String[] getRecordFields(String line, String delimiter) {
        List<String> fields = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(line, delimiter);

        while (st.hasMoreTokens())
            fields.add(st.nextToken().trim());

        return fields.toArray(new String[0]);
    }

    public String[] getNonEmptyFeilds(String[] fields) {
        List<String> realFields = new ArrayList<String>();

        for (int i = 0; i < fields.length; i++)
            if (fields[i].length() > 0)
                realFields.add(fields[i]);

        return realFields.toArray(new String[0]);
    }

    private void readProcessAllSummaryLines(File file) throws Exception {
        DelimitedFileReader reader = new DelimitedFileReader(file, new CommaDelimitedTokenizer());
        
        try {
            Record record = null;

            for (record = reader.read(); !record.isEnd(); record = reader.read())
                processSummary(record.getTokens());
        } catch (ParseException e) {
            log.error("Summary field not in correct format.", e);
            throw new Exception("Summary field not in correct format: " + e.getMessage());
        } catch (Exception e) {
            log.error("Could not read summary info.", e);
            throw new Exception("Could not read summary info: " + e.getMessage());
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    private void readProcessAllInputsLines(File file) throws Exception {
        DelimitedFileReader reader = new DelimitedFileReader(file, new CommaDelimitedTokenizer());

        try {
            Record record = null;

            for (record = reader.read(); !record.isEnd(); record = reader.read())
                processInputs(record.getTokens());
        } catch (ParseException e) {
            log.error("Inputs field not in correct format.", e);
            throw new Exception("Inputs field not in correct format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Could not read inputs info: " + e.getMessage());
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    private void readProcessAllJobsLines(File file) throws Exception {
        DelimitedFileReader reader = new DelimitedFileReader(file, new CommaDelimitedTokenizer());
        
        try {
            Record record = null;

            for (record = reader.read(); !record.isEnd(); record = reader.read())
                processJobs(record.getTokens());
        } catch (ParseException e) {
            log.error("Jobs field not in correct format.", e);
            throw new Exception("Jobs field not in correct format: " + e.getMessage());
        } catch (Exception e) {
            log.error("Could not read jobs info.", e);
            throw new Exception("Could not read jobs info: " + e.getMessage());
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    private void processSummary(String[] data) throws Exception {
        String line = data[0];

        if (line.startsWith("\""))
            line = line.substring(1);

        if (line.endsWith("\""))
            line = line.substring(0, line.length() - 1);
        
        if (line.startsWith("#")) {
            populateCaseMainFields(line);
            return;
        }

        if (line.startsWith("Tab")) {
            paramColNames = data;
            return;
        }

        if (line.startsWith("Summary")) {
            populateCase(data);
            return;
        }

        if (line.startsWith("Parameters")) {
            addParameter(data);
            return;
        }
    }

    private void populateCaseMainFields(String line) {
        int index = line.indexOf('=') + 1;

        if (line.startsWith("#EMF_CASE_NAME")) {
            caseObj.setName(restoreChars(line.substring(index)));
            return;
        }

        if (line.startsWith("#EMF_CASE_ABBREVIATION")) {
            caseObj.setAbbreviation(new Abbreviation(restoreChars(line.substring(index))));
            return;
        }

        if (line.startsWith("#EMF_CASE_DESCRIPTION")) {
            caseObj.setDescription(recoverNewLines(restoreChars(line.substring(index))));
            return;
        }

        if (line.startsWith("#EMF_CASE_CATEGORY")) {
            caseObj.setCaseCategory(new CaseCategory(restoreChars(line.substring(index))));
            return;
        }

        if (line.startsWith("#EMF_PROJECT")) {
            caseObj.setProject(new Project(restoreChars(line.substring(index))));
            return;
        }

        if (line.startsWith("#EMF_CASE_COPIED_FROM")) {
            caseObj.setTemplateUsed(restoreChars(line.substring(index)));
            return;
        }

        if (line.startsWith("#EMF_IS_FINAL")) {
            caseObj.setIsFinal(line.substring(index).equalsIgnoreCase("true"));
            return;
        }

        if (line.startsWith("#EMF_IS_TEMPLATE")) {
            caseObj.setCaseTemplate(line.substring(index).equalsIgnoreCase("true"));
            return;
        }
        
        if (line.startsWith("#EMF_OUTPUT_JOB_SCRIPTS_FOLDER")) {
            caseObj.setOutputFileDir(restoreChars(line.substring(index)));
            return;
        }
        
        if (line.startsWith("#EMF_INPUT_FOLDER")) {
            caseObj.setInputFileDir(restoreChars(line.substring(index)));
            return;
        }
        
        if (line.startsWith("#EMF_SECTORS=")) {
            String[] sectors = line.substring(index).split("&");
            Sector[] sList = new Sector[sectors.length];
            
            for(int i = 0; i < sList.length; i++)
                sList[i] = new Sector(restoreChars(sectors[i]), restoreChars(sectors[i]));
            
            caseObj.setSectors(sList);
            return;
        }
        
        if (line.startsWith("#EMF_REGION")) {
            GeoRegion[] regions = caseObj.getRegions();
            
            if (regions == null)
                regions = new GeoRegion[0];
            
            List<GeoRegion> rList = new ArrayList<GeoRegion>();
            rList.addAll(Arrays.asList(regions));
            
            GeoRegion region = new GeoRegion();
            String[] rFields = line.substring(index).split("&");
            region.setName(restoreChars(rFields[0]));
            region.setAbbreviation(restoreChars(rFields[1]));
            region.setIoapiName(restoreChars(rFields[2]));
            
            rList.add(region);
            caseObj.setRegions(rList.toArray(new GeoRegion[0]));
        }
    }

    private String recoverNewLines(String text) {
        if (text == null || text.trim().isEmpty())
            return "";
        
        return text.replaceAll(StringTools.EMF_NEW_LINE, sysNewLine);
    }

    private void populateCase(String[] values) throws ParseException {
        // NOTE: the order of fields:
        // Tab,Parameter,Order,Envt. Var.,Region,Sector,Job,Program,Value,Type,Reqd?,Local?,Last Modified,Notes,Purpose
        // pEnvVars.add(new ParameterEnvVar(values[3]));
        String value = restoreChars(values[8]);
        
        if (values[1].equalsIgnoreCase("Model to Run")) {
            caseObj.setModel(new ModelToRun(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Model Version")) {
            caseObj.setModelVersion(value);
            return;
        }

        if (values[1].equalsIgnoreCase("Modeling Region")) {
            caseObj.setModelingRegion(new Region(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Met Layers")) {
            caseObj.setNumMetLayers(value == null || value.equalsIgnoreCase("null") || value.trim().isEmpty() ? null : new Integer(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Emission Layers")) {
            caseObj.setNumEmissionsLayers(value == null || value.equalsIgnoreCase("null") || value.trim().isEmpty() ? null : new Integer(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Downstream Model")) {
            caseObj.setAirQualityModel(new AirQualityModel(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Speciation")) {
            caseObj.setSpeciation(new Speciation(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Meteorological Year")) {
            caseObj.setMeteorlogicalYear(new MeteorlogicalYear(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Base Year")) {
            if (value != null && !value.trim().isEmpty() && !value.trim().toLowerCase().equals("null"))
                caseObj.setEmissionsYear(new EmissionsYear(value.trim()));

            return;
        }

        if (values[1].equalsIgnoreCase("Future Year")) {
            if (value != null && !value.trim().isEmpty())
                caseObj.setFutureYear(Integer.parseInt(value));
            
            return;
        }

        if (values[1].equalsIgnoreCase("Start Date & Time")) {
            caseObj.setStartDate(CustomDateFormat.parse_MM_DD_YYYY_HH_mm(value));
            return;
        }

        if (values[1].equalsIgnoreCase("End Date & Time")) {
            caseObj.setEndDate(CustomDateFormat.parse_MM_DD_YYYY_HH_mm(value));
            return;
        }

    }

    private void addParameter(String[] fields) throws Exception {
        CaseParameter newParam = new CaseParameter();

        // NOTE: the order of fields:
        // Tab,Parameter,Order,Envt. Var.,Region,Sector,Job,Program,Value,Type,Reqd?,Local?,Last Modified,Notes,Purpose

        newParam.setParameterName(new ParameterName(restoreChars(fields[1])));
        newParam.setOrder(Float.valueOf(fields[2]));
        ParameterEnvVar envar = new ParameterEnvVar(restoreChars(fields[3]));
        pEnvVars.add(envar);
        newParam.setEnvVar(envar);
        newParam.setRegion(new GeoRegion(restoreChars(fields[4]), restoreChars(fields[4])));
        newParam.setSector(new Sector(restoreChars(fields[5]), restoreChars(fields[5])));
        newParam.setJobName(restoreChars(fields[6]));
        newParam.setProgram(new CaseProgram(restoreChars(fields[7])));
        newParam.setValue(restoreChars(fields[8]));
        newParam.setType(new ValueType(fields[9]));
        newParam.setRequired(fields[10].equalsIgnoreCase("TRUE"));
        newParam.setLocal(fields[11].equalsIgnoreCase("TRUE"));
        newParam.setLastModifiedDate(CustomDateFormat.parse_MM_DD_YYYY_HH_mm(fields[12]));
        newParam.setNotes(recoverNewLines(restoreChars(fields[13])));
        newParam.setPurpose(recoverNewLines(restoreChars(fields[14])));

        this.params.add(newParam);
    }

    private void processInputs(String[] data) throws ParseException {
        if (data[0].startsWith("Tab")) {
            inputColNames = data;
            return;
        }

        CaseInput input = new CaseInput();

        // NOTE: the order of fields:
        // Tab,Inputname,Envt Variable,Sector,Job,Program,Dataset,Version,QA status,DS Type,Reqd?,Local?,Subdir,Last
        // Modified,Parentcase

        input.setInputName(new InputName(restoreChars(data[1])));
        InputEnvtVar envVar = new InputEnvtVar(restoreChars(data[2]));
        this.inEnvVars.add(envVar);
        input.setEnvtVars(envVar);
        input.setRegion(new GeoRegion(restoreChars(data[3]), restoreChars(data[3])));
        input.setSector(new Sector(restoreChars(data[4]), data[4].equalsIgnoreCase("All sectors") ? "" : restoreChars(data[4])));
        input.setJobName(data[5].equalsIgnoreCase("All jobs for sector") ? "" : restoreChars(data[5]));
        input.setProgram(new CaseProgram(restoreChars(data[6])));
        input.setDataset(new EmfDataset(0, restoreChars(data[7]).trim(), 0, 0, restoreChars(data[10])));
        Version version = (data[8] == null || data[8].equalsIgnoreCase("null") || data[8].trim().isEmpty()) ? null
                : new Version(Integer.parseInt(data[8]));
        input.setVersion(version);
        input.setDatasetType(new DatasetType(restoreChars(data[10])));
        input.setRequired(data[11].equalsIgnoreCase("TRUE"));
        input.setLocal(data[12].equalsIgnoreCase("TRUE"));
        input.setSubdirObj(new SubDir(restoreChars(data[13])));
        input.setLastModifiedDate(data[14].trim().isEmpty() ? null : CustomDateFormat.parse_MM_DD_YYYY_HH_mm(data[14]));
        input.setParentCase(restoreChars(data[15]));

        this.inputs.add(input);
    }

    private void processJobs(String[] data) throws ParseException {
        if (data[0].startsWith("Tab")) {
            jobColNames = data;
            return;
        }

        CaseJob job = new CaseJob();

        // NOTE: the order of fields:
        // Tab,JobName,Order,Sector,RunStatus,StartDate,CompletionDate,Executable,Arguments,Path,QueueOptions,JobGroup,Local,QueueID,User,Host,Notes,Purpose,DependsOn

        job.setName(restoreChars(data[1]));
        job.setJobNo(Float.parseFloat(data[2]));
        job.setRegion(new GeoRegion(restoreChars(data[3]), restoreChars(data[3])));
        job.setSector(new Sector(restoreChars(data[4]), data[4].equalsIgnoreCase("All sectors") ? "" : restoreChars(data[4])));
        job.setRunstatus(new JobRunStatus(restoreChars(data[5])));
        job.setRunStartDate(data[6].trim().isEmpty() ? null : CustomDateFormat.parse_MM_DD_YYYY_HH_mm(data[6]));
        job.setRunCompletionDate(data[7].trim().isEmpty() ? null : CustomDateFormat.parse_MM_DD_YYYY_HH_mm(data[7]));
        job.setExecutable(new Executable(restoreChars(data[8])));
        job.setArgs(restoreChars(data[9]));
        job.setPath(restoreChars(data[10]));
        job.setQueOptions(restoreChars(data[11]));
        job.setJobGroup(restoreChars(data[12]));
        job.setLocal(data[13].equalsIgnoreCase("TRUE"));
        job.setIdInQueue(data[14]);
        job.setUser(new User(restoreChars(data[15])));
        job.setHost(new Host(restoreChars(data[16])));
        job.setRunNotes(recoverNewLines(restoreChars(data[17])));
        job.setPurpose(recoverNewLines(restoreChars(data[18])));

        this.jobs.add(job);
    }
    
    private String restoreChars(String parsed) {
        return parsed.replaceAll(StringTools.EMF_DOUBLE_QUOTE, "\"");
    }

}
