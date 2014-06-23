package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAStepTemplate;

import java.util.Comparator;

public class QAStepOrderComparator implements Comparator {
    public int compare(Object template1, Object template2) {
        float order1 = ((QAStepTemplate) template1).getOrder();
        float order2 = ((QAStepTemplate) template2).getOrder();

        //Note: this comparator imposes orderings that are inconsistent with equals
        if (order1 > order2)
            return 1;
        
        if(order1 == order2)
            return 0;
        
        return -1;
      }

}
