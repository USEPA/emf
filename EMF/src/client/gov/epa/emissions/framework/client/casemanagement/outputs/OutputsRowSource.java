package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.ui.RowSource;

public class OutputsRowSource implements RowSource {
    private CaseOutput source;

    private String[] datasetValues;
    
    private CaseJob job; 

    public OutputsRowSource(CaseOutput source, EmfSession session) {
        this.source = source;
        this.datasetValues = getDatasetValues(source, session);
        this.job = getCaseJob(source, session);
    }


    public Object[] values() {
        if (datasetValues == null)
            return new Object[] { getOutputName(source), getJobName(), getSector(), "",
                "", "", "", "", getExecName(source), getMessage(source) };
        
        return new Object[] { getOutputName(source), getJobName(), getSector(), getDatasetProperty("name"),
                getDatasetProperty("datasetType"), getStatus(source), getDatasetProperty("creator"),
                getDatasetCreatedDate("createdDateTime"), getExecName(source), getMessage(source) };
    }

    private String getMessage(CaseOutput source) {
        String message = source.getMessage();
        
        if (source.getDatasetId() > 0)
        {
           if ((source.getStatus() != null) && source.getStatus().contains("completed")
              && datasetValues == null) 
           {
            message = "Dataset has been deleted.";
            source.setStatus("");
            source.setMessage(message);
           }
        }
        
        if(message != null && message.length() > 50)
            return message.substring(0,49);
        
        return message;
    }


    private Object getExecName(CaseOutput output) {
        return output.getExecName() != null ? output.getExecName() : "";
    }

    private Object getStatus(CaseOutput output) {
        return output.getStatus() != null ? output.getStatus() : "";
    }

    private String getDatasetProperty(String property) {
        String value = null;

        for (String values : datasetValues) {
            if (values.startsWith(property))
                value = values.substring(values.indexOf(",") + 1);
        }
        return value; 
    }
    
    private String getDatasetCreatedDate(String property) {
        String value = null;

        for (String values : datasetValues) {
            if (values.startsWith(property))
                value = values.substring(values.indexOf(",") + 1, values.indexOf(",") + 17);
                //value = ((value !=null) && value.length() >16) ? value.substring(0, 16): value;
        }
        return value; 
    }

    private String getOutputName(CaseOutput output) {
        return output.getName() != null ? output.getName() : "";
    }

    private String getJobName() {
        String jobName = null;
        if (job != null)
            jobName = job.getName();
        return jobName==null? "": jobName;
    }

    private String getSector() {
        String sectorName=null; 
        if (job != null){
            Sector sec=job.getSector();
            sectorName= (sec==null? "":sec.getName());
        }
        return sectorName;
    }

    public void setValueAt(int column, Object val) {
//      No Op
    }

    public Object source() {
        return source;
    }

    public void validate(int rowNumber) {
//      No Op
    }

    private String[] getDatasetValues(CaseOutput output, EmfSession session) {
        String[] values = null;
        
        if (output.getDatasetId() == 0)
            return null; 
        try {
            values = session.dataService().getDatasetValues(new Integer(output.getDatasetId()));
        } catch (Exception e) {
            return null;
        }
        
        return values;
    }
    
   private CaseJob getCaseJob(CaseOutput output, EmfSession session) {
        try {
            job =session.caseService().getCaseJob(output.getJobId());
            
        } catch (EmfException e) {
            return null; 
        }
        return job;
    }
}
