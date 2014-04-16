package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.ControlStrategyServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class ControlStrategyTransportServiceTestCase extends WebServicesTestCase {


    private ControlStrategyService css = null;

    private ControlStrategyService service;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());
        service = new ControlStrategyServiceImpl(sessionFactory, dbServerFactory());

        ServiceLocator rl = serviceLocator();
        css = rl.controlStrategyService();
    }
    
    public void testShouldGetNoControlStrategies() throws EmfException {
        ControlStrategy[] controlStrategies = service.getControlStrategies();
        assertEquals("fetch 0 strategies",controlStrategies.length, 0);
    }

    public void testShouldAddControlStrategy() throws EmfException {
        ControlStrategy controlStrategy = controlStrategy();
        css.addControlStrategy(controlStrategy);
        ControlStrategy[] controlStrategies= null;
        try {
            controlStrategies = service.getControlStrategies();
            assertEquals(1,controlStrategies.length);
        } finally {
          //remove(controlStrategies[0]);
        }
    }
    
    private ControlStrategy controlStrategy() {
        return new ControlStrategy("test"+Math.random());
    }

    protected void doTearDown() throws Exception {// no op
    }

}
