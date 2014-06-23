package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.ui.RowSource;

public class InputsRowSource implements RowSource {

    private CaseInput input;
    
    private EmfSession session;

    public InputsRowSource(CaseInput source, EmfSession session) {
        this.input = source;
        this.session = session;
    }

    public Object[] values() {
        return new Object[] { getInputName(input), getEnvtVarName(input), getRegionName(input), 
                getSectorName(input), getJob(input), getProgramName(input), 
                getDatasetName(input), getVersion(input),
                getQAStatus(input), getDSType(input), isRequired(input), isLocal(input), 
                getSubDir(input), getLastModifiedDate(input)};
    }
    
    private String getInputName(CaseInput input) {
        return (input.getInputName() == null) ? "" : input.getInputName().getName();
    }
    
    private String getRegionName(CaseInput input) {
        return (input.getRegion() == null) ? "" : input.getRegion().toString();
    }
    
    private String getSectorName(CaseInput input) {
        return (input.getSector() == null) ? "All sectors" : input.getSector().getName();
    }
    
    private String getProgramName(CaseInput input) {
        return (input.getProgram() == null) ? "" : input.getProgram().getName();
    }
    
    private String getEnvtVarName(CaseInput input) {
        return (input.getEnvtVars() == null) ? "" : input.getEnvtVars().getName();
    }
    
    private String getDatasetName(CaseInput input) {
        return (input.getDataset() == null) ? "" : input.getDataset().getName();
    }
    
    private String getVersion(CaseInput input) {
        return (input.getVersion() == null) ? "" : input.getVersion().getVersion() + "";
    }
    
    private String getQAStatus(CaseInput input) {
        return "";
    }
    
    private String getDSType(CaseInput input) {
        return (input.getDatasetType() == null) ? "" : input.getDatasetType().getName();
    }
    
    private String isRequired(CaseInput input) {
        return (input == null) ? "" : input.isRequired() + "";
    }
    
    private String isLocal(CaseInput input) {
        return (input == null) ? "" : input.isLocal() + "";
    }

    private String getSubDir(CaseInput input) {
        return (input.getSubdirObj() == null || 
                (input.getDatasetType() != null && input.getDatasetType().isExternal())) ? 
                        "" : input.getSubdirObj().toString();
    }
    
    private String getLastModifiedDate(CaseInput input) {
        return (input.getLastModifiedDate() == null) ? "" : CustomDateFormat.format_YYYY_MM_DD_HH_MM(input.getLastModifiedDate());
    }
     
    private String getJob(CaseInput input) {
        try {
            CaseJob job = session.caseService().getCaseJob(input.getCaseJobID());
            return (job == null) ? InputFieldsPanelPresenter.ALL_FOR_SECTOR : job.toString();
        } catch (EmfException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Object source() {
        return input;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}