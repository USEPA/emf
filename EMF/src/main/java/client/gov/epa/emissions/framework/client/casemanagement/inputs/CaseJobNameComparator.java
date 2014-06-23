//New file created by RVA to sort the Case Jobs

package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

import java.util.Comparator;

public class CaseJobNameComparator implements Comparator {

    public int compare(Object job1, Object job2) {
        String name1 = ((CaseJob) job1).getName();
        String name2 = ((CaseJob) job2).getName();

        //Note: this comparator imposes orderings that are inconsistent with equals
        return name1.compareToIgnoreCase(name2);
    }

}
