//New file created by RVA (with help from Alison), to sort the QA Step Templates

package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAStepTemplate;

import java.util.Comparator;

public class QAStepNameComparator implements Comparator {

    public int compare(Object template1, Object template2) {
            String name1 = ((QAStepTemplate) template1).getName();
            String name2 = ((QAStepTemplate) template2).getName();

            //Note: this comparator imposes orderings that are inconsistent with equals
            return name1.compareToIgnoreCase(name2);
    }

}
