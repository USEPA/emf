package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

public class PollutantTest extends ServiceTestCase {

    HibernateFacade hibernateFacade;
    
    public PollutantTest(){
        hibernateFacade = new HibernateFacade();
    }
    protected void doSetUp() throws Exception {
        // NOTE Auto-generated method stub

    }

    protected void doTearDown() throws Exception {
        // NOTE Auto-generated method stub

    }

    public void testShouldReturnExistingPollutant() throws Exception {
        Pollutants pollutants = new Pollutants(sessionFactory());
        Pollutant pm10Pollutant = pollutant("PM10");
        assertEquals(pm10Pollutant, pollutants.getPollutant("PM10"));
    }

    public void testShouldAddNewPollutant() throws Exception {
        Pollutant newPollutant = null;
        try {
            Pollutant pollutant = pollutant("New");
            assertNull(pollutant);
            Pollutants pollutants = new Pollutants(sessionFactory());
            newPollutant = pollutants.getPollutant("NEW");
            assertEquals("NEW", newPollutant.getName());
        } finally {
            drop(newPollutant);
        }
    }
    private void drop(Pollutant pollutant) {
        hibernateFacade.delete(pollutant, session);
    }
    private Pollutant pollutant(String name) {
        List list = hibernateFacade.get(Pollutant.class, Restrictions.eq("name", name), session);
        if (list.isEmpty())
            return null;
        return (Pollutant) list.get(0);
    }

}
