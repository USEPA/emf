package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.QAStep;
import junit.framework.TestCase;

public class QAStepsTest extends TestCase {

    public void testSize() {
        QAStep[] list = { new QAStep(), new QAStep() };
        QASteps steps = new QASteps(list);

        assertEquals(2, steps.size());
    }
    
    public void testAdd() {
        QAStep[] list = { new QAStep(), new QAStep() };
        QASteps steps = new QASteps(list);
        
        steps.add(new QAStep());
        assertEquals(3, steps.size());
    }
    
    public void testGet() {
        QAStep[] list = { new QAStep(), new QAStep() };
        QASteps steps = new QASteps(list);
        
        QAStep step = new QAStep();
        steps.add(step);
        assertSame(step, steps.get(2));
    }
}
