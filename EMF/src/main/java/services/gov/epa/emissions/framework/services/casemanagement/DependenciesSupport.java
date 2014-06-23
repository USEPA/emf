package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DependenciesSupport {
    public static List<CaseJob> path = new ArrayList<CaseJob>();

    public static boolean dependsSet = false;

    private CaseDAO dao = null;
    
    public DependenciesSupport(CaseDAO caseDAO) {
        // NOTE Auto-generated constructor stub
        this.dao = caseDAO;
    }

    public void recurse(CaseJob job) throws Exception {
        System.out.println("In job= " + job.getName());
        path.add(job);
        List<DependentJob> dependents = Arrays.asList(job.getDependentJobs());
        
        if (dependents.size() == 0) {
            System.out.println("No dependencies for job= " + job.getName());
        } else {
            System.out.println("job: " + job.getName()
                    + " has dependents num= " + dependents.size());
            
            Iterator<DependentJob> iter = dependents.iterator();
            while (iter.hasNext()) {
                DependentJob djb = iter.next();
                CaseJob depCaseJob = dao.getCaseJob(djb.getJobId());
                
                if (path.contains(depCaseJob)) {
                    System.out
                            .println("######################### LOOP DETECTED for job= "
                                    + job.getName()
                                    + " to dependent job= "
                                    + depCaseJob.getName());
                    throw new Exception();

                }
                System.out.println("this job= " + job.getName()
                        + " Picked up Dependent Job= " + depCaseJob.getName());
                recurse(depCaseJob);
            }

        }
        path.remove(job);
    }

    
}
