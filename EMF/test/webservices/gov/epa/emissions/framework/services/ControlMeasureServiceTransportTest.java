package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.services.cost.ControlMeasureServiceImpl;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.rmi.RemoteException;

public class ControlMeasureServiceTransportTest extends ServiceTestCase {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private ControlMeasureService service = null;
    
    private DataCommonsService dataService = null;

    private UserService userService;
    
    private ControlMeasureService cmService;
    
    private HibernateSessionFactory sessionFactory;
    
    protected void doSetUp() throws Exception {
        sessionFactory = sessionFactory();

        RemoteServiceLocator rl = new RemoteServiceLocator(DEFAULT_URL);
        service = rl.controlMeasureService();
        dataService = rl.dataCommonsService();
        userService = rl.userService();
        cmService = new ControlMeasureServiceImpl(sessionFactory, dbServerFactory());
   }

    private String getRandomString() {
        return Math.round(Math.random() * 100) % 100 + "";
    }

    public void testServiceActive() throws Exception {
        assertEquals("Service works", cmService.getMeasures().length, 0);
    }

    public void testShouldGetAllControlMeasures() throws EmfException {
        ControlMeasure[] all = service.getMeasures();
        assertEquals("0 types", all.length, 0);
    }
    
    public void testShouldGetControlMeasureByMajorPollutant() throws RemoteException {
        Pollutant[] pollutants = dataService.getPollutants();
        
        ControlMeasure cm = new ControlMeasure();
        String name = "cm test added" + getRandomString();
        cm.setName(name);
        cm.setEquipmentLife(new Float(12));
        cm.setAbbreviation("12345678" + getRandomString());
        cm.setMajorPollutant(pollutants[0]);
        service.addMeasure(cm, new Scc[]{});

        ControlMeasure[] target = service.getMeasures(pollutants[0]);
        assertEquals(target.length, 1);
        assertEquals(name, target[0].getName());
        
        service.removeMeasure(target[0].getId());
    }


    public void testShouldAddOneControlMeasure() throws RemoteException {
        ControlMeasure cm = new ControlMeasure();
        cm.setName("cm test added" + getRandomString());
        cm.setEquipmentLife(new Float(12));
        cm.setAbbreviation("12345678" + getRandomString());
        service.addMeasure(cm, new Scc[]{});

        ControlMeasure[] all = service.getMeasures();
        assertEquals(all.length, 1);
        assertEquals("cm test added", all[0].getName());
        
        service.removeMeasure(all[0].getId());
        assertEquals(all.length - 1, service.getMeasures().length);
    }

    public void testShouldUpdateControlMeasure() throws RemoteException {
        User owner = userService.getUser("emf");
        ControlMeasure cm = new ControlMeasure();
        cm.setEquipmentLife(new Float(12));
        cm.setName("cm test added");
        cm.setAbbreviation("12345678");
        service.addMeasure(cm, new Scc[]{});
        
        ControlMeasure cmModified = service.obtainLockedMeasure(owner, service.getMeasures()[0].getId());
        cmModified.setEquipmentLife(new Float(120));
        cmModified.setName("cm updated");
        ControlMeasure cm2 = service.updateMeasure(cmModified, new Scc[]{});
        
        try {
            assertEquals("cm updated", cm2.getName());
            assertEquals(new Float(120), new Float(cm2.getEquipmentLife()));
        } finally {
            service.removeMeasure(cmModified.getId());
        }
    }
    
    public void testShouldLockUnlockControlMeasure() throws EmfException {
        User owner = userService.getUser("emf");
        ControlMeasure cm = new ControlMeasure();
        cm.setName("xxxx");
        cm.setAbbreviation("yyyyyyyy");
        service.addMeasure(cm, new Scc[]{});
        
        ControlMeasure released = null;

        try {
            ControlMeasure locked = service.obtainLockedMeasure(owner, service.getMeasures()[0].getId());
            assertTrue("Should have lock cm", locked.isLocked());

//FIXME
//            released = service.releaseLockedControlMeasure(locked);
//            assertFalse("Should have released lock", released.isLocked());

        } finally {
            service.removeMeasure(released.getId());
        }
    }

    public void testShouldGetCorrectSCCs() throws EmfException {
        ControlMeasure cm = new ControlMeasure();
        cm.setEquipmentLife(new Float(12));
        cm.setName("cm test added" + Math.random());
        cm.setAbbreviation("12345678");
        
        //These scc numbers have to exist in the reference.scc table
        cm.setSccs(new Scc[] {new Scc("10100224", ""), new Scc("10100225", ""), new Scc("10100226", "")} ); 
        service.addMeasure(cm, new Scc[]{});
        int measuresAfterAddOne = service.getMeasures().length;
        
        Scc[] sccs = service.getSccsWithDescriptions(cm.getId());
        service.removeMeasure(cm.getId());
        

        assertEquals(3, sccs.length);
        assertEquals("10100224", sccs[0].getCode());
        assertEquals("10100225", sccs[1].getCode());
        assertEquals("10100226", sccs[2].getCode());
        
        assertEquals(measuresAfterAddOne, service.getMeasures().length + 1);
    }

    protected void doTearDown() throws Exception {// no op
    }

}
