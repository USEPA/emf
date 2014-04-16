package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class ControlStrategyServiceTest extends ServiceTestCase {

    private ControlStrategyService service;

    private UserServiceImpl userService;

    private ControlMeasureService cmService;
    
    private HibernateSessionFactory sessionFactory;

    protected void doSetUp() throws Exception {
        sessionFactory = sessionFactory(configFile());
        service = new ControlStrategyServiceImpl(sessionFactory, dbServerFactory());
        userService = new UserServiceImpl(sessionFactory);
        cmService = new ControlMeasureServiceImpl(sessionFactory, dbServerFactory());
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetControlStrategies() throws Exception {
        int totalBeforeAdd = service.getControlStrategies().length;
        ControlStrategy element = controlStrategy();
        try {
            List list = Arrays.asList(service.getControlStrategies());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    private ControlStrategy controlStrategy() {
        ControlStrategy element = new ControlStrategy("test" + Math.random());
        add(element);
        return element;
    }

    public void testShouldAddControlStrategy() throws Exception {
        int totalBeforeAdd = service.getControlStrategies().length;
        ControlStrategy element = new ControlStrategy("test" + Math.random());
        service.addControlStrategy(element);

        try {
            List list = Arrays.asList(service.getControlStrategies());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldRemoveControlStrategy() throws Exception {
        User owner = userService.getUser("emf");
        int totalBeforeAdd = service.getControlStrategies().length;
        ControlStrategy element = new ControlStrategy("test" + Math.random());
        element.setCreator(owner);
        element.setControlMeasureClasses(cmService.getMeasureClasses());
        service.addControlStrategy(element);
        
        try {
            List list = Arrays.asList(service.getControlStrategies());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));

//            service.removeControlStrategies(new ControlStrategy[]{element}, owner);
            service.removeControlStrategies(new int[]{element.getId()}, owner);
            List newlist = Arrays.asList(service.getControlStrategies());
            assertEquals(totalBeforeAdd, newlist.size());
            assertFalse(newlist.contains(element));
        } catch (Exception e) {
            throw new Exception("Cann't remove control strategy from database.");
        }
    }

    public void testShouldFailToRemoveControlStrategy() throws Exception {
        User owner = userService.getUser("emf");
        User user = userService.getUser("admin");
        int totalBeforeAdd = service.getControlStrategies().length;
        ControlStrategy element = new ControlStrategy("test" + Math.random());
        element.setCreator(owner);
        service.addControlStrategy(element);
        
        try {
            List list = Arrays.asList(service.getControlStrategies());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
            
//            service.removeControlStrategies(new ControlStrategy[]{element}, user);
            service.removeControlStrategies(new int[]{element.getId()}, user);
        } catch (Exception e) {
            remove(element);
            return;
        }
        
        fail("Only creator can delete the control strategy");
    }

    public void testShouldObtainLockedControlStrategy() throws EmfException {
        User owner = userService.getUser("emf");
        ControlStrategy element = controlStrategy();

        try {
            ControlStrategy locked = service.obtainLocked(owner, element.getId());
            assertTrue("Should be locked by owner", locked.isLocked(owner));

            ControlStrategy loadedFromDb = load(element);// object returned directly from the table
            assertEquals(locked.getLockOwner(), loadedFromDb.getLockOwner());
        } finally {
            remove(element);
        }
    }

    public void testShouldReleaseLockedOnControlStrategy() throws EmfException {
        User owner = userService.getUser("emf");
        ControlStrategy element = controlStrategy();

        try {
            ControlStrategy locked = service.obtainLocked(owner, element.getId());
            service.releaseLocked(owner, locked.getId());
            ControlStrategy released = (ControlStrategy) load(ControlStrategy.class,locked.getName());
            assertFalse("Should have released lock", released.isLocked());

            ControlStrategy loadedFromDb = load(element);
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(element);
        }
    }

    public void testShouldUpdateControlStrategy() throws Exception {
        User owner = userService.getUser("emf");
        ControlStrategy element = controlStrategy();
        ControlStrategy released = null;
        try {
            ControlStrategy locked = service.obtainLocked(owner, element.getId());

            session.clear();
            ControlMeasureClass[] cmcs = cmService.getMeasureClasses();
            int cmcLength = cmcs.length;

            //add control measures to include to strategy, check no of stratgies for test...
            LightControlMeasure[] cms = cmService.getLightControlMeasures();
            int cmLength = cms.length;

            //add control measures to include to strategy, check no of stratgies for test...
            ControlStrategyMeasure[] csms = new ControlStrategyMeasure[cms.length];

            for (int i = 0; i < cms.length; i++) {
                csms[i].setControlMeasure(cms[i]);
            }

            locked.setName("TEST");
            locked.setDescription("TEST control strategy");
            locked.setControlMeasureClasses(cmcs);
            locked.setControlMeasures(csms);

            released = service.updateControlStrategy(locked);
            assertEquals("TEST", released.getName());
            assertEquals("TEST control strategy", released.getDescription());
            assertEquals(released.getLockOwner(), null);
            assertEquals(cmcLength, released.getControlMeasureClasses().length);
            assertEquals(cmLength, released.getControlMeasures().length);
            assertFalse("Lock should be released on update", released.isLocked());
        } finally {
            remove(element);
        }
    }

    // Need to populate the test database table emf.strategy_type to pass this test.
    public void testShouldGetStrategyTypes() throws Exception {
        StrategyType[] types = service.getStrategyTypes();
        session.clear();

        assertEquals(3, types.length);
        assertEquals("Max Emissions Reduction", types[2].getName());
        assertEquals("Least Cost", types[1].getName());
    }

    public void testShouldUpdateControlStrategyWithLock() throws Exception {

        User owner = userService.getUser("emf");
        ControlStrategy element = controlStrategy();
        ControlStrategy csWithLock = null;
        try {
            ControlStrategy locked = service.obtainLocked(owner, element.getId());

            locked.setName("TEST");
            locked.setDescription("TEST control strategy");

            csWithLock = service.updateControlStrategyWithLock(locked);
            assertEquals("TEST", csWithLock.getName());
            assertEquals("TEST control strategy", csWithLock.getDescription());
            assertEquals(csWithLock.getLockOwner(), owner.getUsername());
            assertTrue("Lock should be kept on update", csWithLock.isLocked());
        } finally {
            remove(element);
        }
    }

    public void testShouldStopRunControlStrategyWithoutError() throws Exception {
        try {
            service.stopRunStrategy(0);
        } catch (Exception e) {
            throw e;
        }
    }

    public void testShouldCopyControlStrategy() throws Exception {
        User owner = userService.getUser("emf");
        ControlStrategy element = controlStrategy();
        ControlStrategy released = null;
        ControlStrategy copied = null;
        try {
            ControlStrategy locked = service.obtainLocked(owner, element.getId());

            session.clear();
            ControlMeasureClass[] cmcs = cmService.getMeasureClasses();

            //add control measures to include to strategy, check no of stratgies for test...
            LightControlMeasure[] cms = cmService.getLightControlMeasures();
            ControlStrategyMeasure[] csms = new ControlStrategyMeasure[cms.length];

            for (int i = 0; i < cms.length; i++) {
                csms[i].setControlMeasure(cms[i]);
            }
            
            locked.setControlMeasureClasses(cmcs);
            locked.setControlMeasures(csms);

            released = service.updateControlStrategy(locked);

            //now create a copy of this cs...
            int newId = service.copyControlStrategy(released.getId(), owner);
            copied =  service.getById(newId);
            assertEquals(copied.getName(), "Copy of " + released.getName());
            assertEquals(copied.getDescription(), released.getDescription());
            assertEquals(copied.getControlMeasureClasses().length, released.getControlMeasureClasses().length);
            assertEquals(copied.getControlMeasures().length, released.getControlMeasures().length);
        } finally {
            remove(element);
            remove(copied);
        }
    }

    private ControlStrategy load(ControlStrategy controlStrategy) {
        Transaction tx = null;
        session.clear();
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(ControlStrategy.class).add(
                    Restrictions.eq("name", controlStrategy.getName()));
            tx.commit();

            return (ControlStrategy) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}
