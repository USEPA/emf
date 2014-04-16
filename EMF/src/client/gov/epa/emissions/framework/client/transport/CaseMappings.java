package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.AirQualityModel;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseProgram;
import gov.epa.emissions.framework.services.casemanagement.CasesSens;
import gov.epa.emissions.framework.services.casemanagement.EmissionsYear;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;
import gov.epa.emissions.framework.services.casemanagement.ModelToRun;
import gov.epa.emissions.framework.services.casemanagement.Speciation;
import gov.epa.emissions.framework.services.casemanagement.SubDir;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;

public class CaseMappings extends Mappings {

    public void register(Call call) {
        beans(call);
        arrays(call);
    }

    private void beans(Call call) {
        bean(call, Case.class, caseObject());
        bean(call, Abbreviation.class, abbreviation());
        bean(call, AirQualityModel.class, airQualityModel());
        bean(call, CaseCategory.class, caseCategory());
        bean(call, EmissionsYear.class, emissionsYear());
        bean(call, MeteorlogicalYear.class, meteorlogicalYear());
        bean(call, Speciation.class, speciation());
        bean(call, InputName.class, inputname());
        bean(call, InputEnvtVar.class, inputEnvtVar());
        bean(call, CaseInput.class, caseinput());
        bean(call, ModelToRun.class, modelToRun());
        bean(call, CaseProgram.class, program());
        bean(call, SubDir.class, subdir());
        bean(call, CaseJob.class, casejob());
        bean(call, JobRunStatus.class, jobRunStatus());
        bean(call, Executable.class, executable());
        bean(call, Host.class, host());
        bean(call, ParameterEnvVar.class, parameterEnvVar());
        bean(call, ValueType.class, valueType());
        bean(call, ParameterName.class, parameterName());
        bean(call, CaseParameter.class, caseParameter());
        bean(call, JobMessage.class, jobMessage());
        bean(call, CaseOutput.class, caseOutput());
        bean(call, DependentJob.class, dependentJob());
        bean(call, CasesSens.class, casesSens());
        
        bean(call, Mutex.class, mutex());
    }

    private void arrays(Call call) {
        array(call, Case[].class, cases());
        array(call, Abbreviation[].class, abbreviations());
        array(call, AirQualityModel[].class, airQualityModels());
        array(call, CaseCategory[].class, caseCategories());
        array(call, EmissionsYear[].class, emissionsYears());
        array(call, MeteorlogicalYear[].class, meteorlogicalYears());
        array(call, Speciation[].class, speciations());
        array(call, InputName[].class, inputnames());
        array(call, InputEnvtVar[].class, inputEnvtVars());
        array(call, CaseInput[].class, caseinputs());
        array(call, ModelToRun[].class, modelToRuns());
        array(call, CaseProgram[].class, programs());
        array(call, SubDir[].class, subdirs());
        array(call, CaseJob[].class, casejobs());
        array(call, JobRunStatus[].class, jobRunStatuses());
        array(call, Executable[].class, executables());
        array(call, Host[].class, hosts());
        array(call, ParameterEnvVar[].class, parameterEnvVars());
        array(call, ValueType[].class, valueTypes());
        array(call, ParameterName[].class, parameterNames());
        array(call, CaseParameter[].class, caseParameters());
        array(call, JobMessage[].class, jobMessages());
        array(call, CaseOutput[].class, caseOutputs());
        array(call, DependentJob[].class, dependentJobs());
        array(call, CasesSens[].class, casesSenses());
    }

    public QName caseObject() {
        return qname("Case");
    }

    public QName cases() {
        return qname("Cases");
    }

    public QName abbreviation() {
        return qname("Abbreviation");
    }

    public QName abbreviations() {
        return qname("Abbreviations");
    }

    public QName airQualityModel() {
        return qname("AirQualityModel");
    }

    public QName airQualityModels() {
        return qname("AirQualityModels");
    }

    public QName caseCategory() {
        return qname("CaseCategory");
    }

    public QName caseCategories() {
        return qname("CaseCategories");
    }

    public QName emissionsYear() {
        return qname("EmissionsYear");
    }

    public QName emissionsYears() {
        return qname("EmissionsYears");
    }

    public QName meteorlogicalYear() {
        return qname("MeteorlogicalYear");
    }

    public QName meteorlogicalYears() {
        return qname("MeteorlogicalYears");
    }

    public QName speciation() {
        return qname("Speciation");
    }

    public QName speciations() {
        return qname("Speciations");
    }

    public QName caseinput() {
        return qname("CaseInput");
    }

    public QName caseinputs() {
        return qname("CaseInputs");
    }

    public QName inputEnvtVar() {
        return qname("InputEnvtVar");
    }

    public QName inputEnvtVars() {
        return qname("InputEnvtVars");
    }

    public QName inputname() {
        return qname("InputName");
    }
 
    public QName inputnames() {
        return qname("InputNames");
    }
 
    public QName mutex() {
        return qname("Mutex");
    }

    public QName modelToRun() {
        return qname("ModelToRun");
    }
    
    public QName modelToRuns() {
        return qname("ModelToRuns");
    }
    
    public QName program() {
        return qname("CaseProgram");
    }

    public QName programs() {
        return qname("CasePrograms");
    }
    
    public QName subdir() {
        return qname("SubDir");
    }

    public QName subdirs() {
        return qname("SubDirs");
    }
    
    public QName casejob() {
        return qname("CaseJob");
    }
    
    public QName jobRunStatus(){
        return qname("JobRunStatus");
    }
    
    public QName executable(){
        return qname("Executable");
    }

    public QName casejobs() {
        return qname("CaseJobs");
    }
    
    public QName jobRunStatuses(){
        return qname("JobRunStatuses");
    }
    
    public QName executables(){
        return qname("Executables");
    }
    
    public QName host(){
        return qname("Host");
    }
    
    public QName hosts(){
        return qname("Hosts");
    }

    public QName parameterEnvVar() {
        return qname("ParameterEnvVar");
    }

    public QName parameterEnvVars() {
        return qname("ParameterEnvVars");
    }
    
    public QName valueType() {
        return qname("ValueType");
    }
    
    public QName valueTypes() {
        return qname("ValueTypes");
    }

    public QName parameterName() {
        return qname("ParameterName");
    }

    public QName parameterNames() {
        return qname("ParameterNames");
    }
    
    public QName caseParameter() {
        return qname("CaseParameter");
    }
    
    public QName caseParameters() {
        return qname("CaseParameters");
    }

    public QName casejobIds() {
        return qname("IntegerArray");
    }

    public QName caseInputIds() {
        return qname("IntegerArray");
    }
    
    public QName jobMessage() {
        return qname("JobMessage");
    }
    
    public QName jobMessages() {
        return qname("JobMessages");
    }
    
    public QName dependentJob() {
        return qname("DependentJob");
    }

    public QName dependentJobs() {
        return qname("DependentJobs");
    }
    
    
    public QName caseOutput() {
        return qname("CaseOutput");
    }
    
    public QName caseOutputs() {
        return qname("CaseOutputs");
    }
    
    public QName casesSens() {
        return qname("CaseSens");
    }

    public QName casesSenses() {
        return qname("CaseSenses");
    }
    
}
