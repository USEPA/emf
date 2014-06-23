package gov.epa.emissions.framework.client.casemanagement.outputs;

//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseObjectManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

import javax.swing.JComponent;

public class OutputFieldsPanelPresenter{

    private EmfSession session;

    private OutputFieldsPanelView view;

    private int caseId;
    
    public static final String ALL_FOR_SECTOR = "All jobs for sector";
    
    private CaseObjectManager caseObjectManager = null;
    
    public OutputFieldsPanelPresenter(int caseId, OutputFieldsPanelView outputFields, EmfSession session) //throws EmfException 
    {
        this.session = session;
        this.view = outputFields;
        this.caseId = caseId;
        this.caseObjectManager = CaseObjectManager.getCaseObjectManager(session);
    }

    public void display(CaseOutput output, JComponent container) throws EmfException {
        view.observe(this);
        view.display(output, container, session);
    }

    public Sector[] getSectors() throws EmfException {
        return caseObjectManager.getSectorsWithAll();
    }

    public CaseJob[] getCaseJobs() throws EmfException 
    {
        return caseObjectManager.getCaseJobsWithAll(caseId);
   }
    
    public DatasetType[] getDSTypes() {
       return caseObjectManager.getDatasetTypes();
    }
    
    public DatasetType getDatasetType(String datasetType)
    {
            return session.getLightDatasetType(datasetType);
    }
    
    public EmfDataset[] getDatasets(DatasetType type) throws EmfException
{
        if (type == null)
            return new EmfDataset[0];

        return dataService().getDatasets(type);
    }

    private DataService dataService() {
        return session.dataService();
    }
    
//    public String[] getDatasetValues(Integer id) {
// //       CaseOutput output=view.getOutput();
//        String[] values = null;
//        
//        try {
//            values = dataService().getDatasetValues(id);
//        } catch (Exception e) {
//            return null;
//        }
//        return values;
//    }
    
    public String[] getDatasetValues() {
               CaseOutput output=view.getOutput();
               String[] values = null;
               
               try {
                   values = dataService().getDatasetValues(new Integer(output.getDatasetId()));
               } catch (Exception e) {
                   return null;
               }
               return values;
           }

    public void doSave() throws EmfException {
         session.caseService().updateCaseOutput(session.user(), view.setFields());
    }

    public int getJobIndex(int caseJobId, CaseJob [] jobs) //throws EmfException 
    {
        //CaseJob[] jobs = session.caseService().getCaseJobs(caseId);
        // AME: don't go get the jobs from the server again!
        if (caseJobId == 0) return 0;
        for (int i = 0; i < jobs.length; i++)
            if (jobs[i].getId() == caseJobId)
                return i; // because of the default "All jobs" job is not in db
        return 0;
    }
    
//    public int getDatasetTypeIndex(String dsType, DatasetType[] datasetTypes) //throws EmfException 
//    {
//        if (dsType.trim()=="") return 0;
//        for (int i = 0; i < datasetTypes.length; i++)
//            if (datasetTypes[i].getName().trim().equalsIgnoreCase(dsType.trim()))
//                return i+1; // because of the default "All jobs" job is not in db
//        return 0;
//    } 
    
//    public int getDatasetIndex(String ds, EmfDataset[] datasets) //throws EmfException 
//    {
//        if (ds.trim()=="") return 0;
//        for (int i = 0; i < datasets.length; i++)
//            if (datasets[i].getName().trim().equalsIgnoreCase(ds.trim()))
//                return i+1; // because of the default "not selected" job is in db
//        return 0;
//    }

    public Sector getJobSector(Integer jobId){
        Sector sec=null; 
        try {
            sec=session.caseService().getCaseJob(jobId).getSector();
        } catch (EmfException e) {
            return null; 
        }
        return sec; 
    }    
}
