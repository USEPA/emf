package gov.epa.emissions.framework.services.cost;

import java.util.Date;
import java.util.List;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class ControlMeasureServiceTest extends ServiceTestCase {

    private ControlMeasureService service;

    private UserServiceImpl userService;

    private DataCommonsService dataService;
    
    private HibernateSessionFactory sessionFactory;

    public void doSetUp() throws Exception {
        sessionFactory = sessionFactory(configFile());
        userService = new UserServiceImpl(sessionFactory);
        service = new ControlMeasureServiceImpl(sessionFactory, dbServerFactory);
        dataService = new DataCommonsServiceImpl(sessionFactory);
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetControlMeasures() throws Exception {

        ControlMeasure cm = new ControlMeasure();
        String name = "cm test one" + Math.random();
        cm.setEquipmentLife(new Float(12));
        cm.setName(name);
        cm.setAbbreviation("12345678");
        cm.setLastModifiedBy(userService.getUser("emf").getName());
        cm.setLastModifiedTime(new Date());
        add(cm);

        try {
            ControlMeasure[] cms = service.getMeasures();

            assertEquals(1, cms.length);
            assertEquals(name, cms[0].getName());
            assertEquals(new Float(12), new Float(cms[0].getEquipmentLife()));
        } finally {
            remove(cm);
        }
    }
    
    public void testShouldGetControlMeasuresByMajorPollutant() throws Exception {
        Pollutant poll = new Pollutant("newpollutant");
        dataService.addPollutant(poll); 
        ControlMeasure cm = new ControlMeasure();
        String name = "cm test one" + Math.random();
        cm.setEquipmentLife(new Float(12));
        cm.setName(name);
        cm.setAbbreviation("12345678");
        cm.setLastModifiedBy(userService.getUser("emf").getName());
        cm.setLastModifiedTime(new Date());
        cm.setMajorPollutant(dataService.getPollutants()[0]);
        add(cm);
        
        try {
            ControlMeasure[] cms = service.getMeasures(dataService.getPollutants()[0]);
            
            assertEquals(1, cms.length);
            assertEquals(name, cms[0].getName());
            assertEquals(new Float(12), new Float(cms[0].getEquipmentLife()));
        } finally {
            remove(poll);
            remove(cm);
        }
    }

    public void testShouldGetAllControlMeasuresClasses() throws Exception {
        
        ControlMeasureClass[] cmcs = service.getMeasureClasses();
        
        assertTrue(cmcs.length > 0);
    }

    public void testShouldAddOneControlMeasure() throws Exception {

        ControlMeasure cm = new ControlMeasure();
        String name = "cm test one" + Math.random();
        cm.setEquipmentLife(new Float(12));
        cm.setName(name);
        cm.setAbbreviation("12345679");
        cm.setLastModifiedBy(userService.getUser("emf").getName());
        cm.setLastModifiedTime(new Date());
        Scc scc1 = new Scc("1023232", "");
        service.addMeasure(cm, new Scc[] { scc1 });

        try {
            ControlMeasure[] cms = service.getMeasures();

            assertEquals(1, cms.length);
            assertEquals(name, cms[0].getName());
            assertEquals(new Float(12), new Float(cms[0].getEquipmentLife()));
        } finally {
            remove(scc1);
            remove(cm);
        }
    }

    public void testShouldUpdateControlMeasure() throws Exception {
        User owner = userService.getUser("emf");
        ControlMeasure cm = new ControlMeasure();
        ControlMeasureClass cmc = service.getMeasureClass("Known");
        cm.setCmClass(cmc);
        String name = "cm test one" + Math.random();
        cm.setEquipmentLife(new Float(12));
        cm.setName(name);
        cm.setAbbreviation("12345688");
        cm.setLastModifiedBy(owner.getName());
        cm.setLastModifiedTime(new Date());
        Scc scc1 = new Scc("123232", "");


        service.addMeasure(cm, new Scc[] { scc1 });

        ControlMeasure cmModified = service.obtainLockedMeasure(owner, cm.getId());
        cmModified.setEquipmentLife(new Float(120));
        cmModified.setName("cm updated");
        ControlMeasure cm2 = service.updateMeasure(cmModified, new Scc[] {});

        try {
            assertEquals("cm updated", cm2.getName());
//            assertEquals(1, cm2.getSccs().length);
            assertEquals("Known", cm2.getCmClass().getName());
            assertEquals(new Float(120), new Float(cm2.getEquipmentLife()));
        } finally {
            remove(scc1);
            remove(cm);
        }
    }

    public void testShouldLockControlMeasure() throws EmfException {
        User owner = userService.getUser("emf");
        ControlMeasure cm = new ControlMeasure();
        cm.setName("xxxx" + Math.random());
        cm.setAbbreviation("yyyyyyyy");
        cm.setLastModifiedBy(owner.getName());
        cm.setLastModifiedTime(new Date());
        Scc scc1 = new Scc("123232", "");
        ControlMeasureClass cmc = service.getMeasureClass("Emerging");
        cm.setCmClass(cmc);
        service.addMeasure(cm, new Scc[] { scc1 });

        try {
            ControlMeasure locked = service.obtainLockedMeasure(owner, cm.getId());
            assertTrue("Should have released lock", locked.isLocked());

            ControlMeasure loadedFromDb = load(cm);
            assertTrue("Should have released lock", loadedFromDb.isLocked());
            assertEquals("Emerging", loadedFromDb.getCmClass().getName());
        } finally {
            remove(scc1);
            remove(cm);
        }
    }

    public void testShouldReleaseLockedControlMeasure() throws EmfException {
        User owner = userService.getUser("emf");
        ControlMeasure cm = new ControlMeasure();
        cm.setName("xxxx" + Math.random());
        cm.setAbbreviation("yyyyyyyy");
        cm.setLastModifiedBy(owner.getName());
        cm.setLastModifiedTime(new Date());
        Scc scc1 = new Scc("123232", "");
        service.addMeasure(cm, new Scc[] { scc1 });

        try {
            ControlMeasure locked = service.obtainLockedMeasure(owner, cm.getId());
//            ControlMeasure released = service.releaseLockedControlMeasure(locked.getId());
            service.releaseLockedControlMeasure(owner, locked.getId());
//            assertFalse("Should have released lock", released.isLocked());

            ControlMeasure loadedFromDb = load(cm);
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(scc1);
            remove(cm);
        }
    }

    public void testShouldRemoveOneControlMeasure() throws Exception {

        ControlMeasure cm = new ControlMeasure();
        String name = "cm test one" + Math.random();
        ControlMeasureClass cmc = service.getMeasureClass("Emerging");
        cm.setCmClass(cmc);
        cm.setEquipmentLife(new Float(12));
        cm.setName(name);
        cm.setAbbreviation("12345678");
        cm.setLastModifiedBy(userService.getUser("emf").getName());
        cm.setLastModifiedTime(new Date());
        Scc scc1 = new Scc("123232", "");
        service.addMeasure(cm, new Scc[] { scc1 });

        ControlMeasure[] cms = service.getMeasures();

        assertEquals(1, cms.length);
        assertEquals(name, cms[0].getName());
        assertEquals(new Float(12), new Float(cms[0].getEquipmentLife()));

        service.removeMeasure(cm.getId());
        assertEquals(0, service.getMeasures().length);
        assertEquals(0,load(Scc.class).size());
    }

    public void testShouldAddControlMeasureEfficiencyRecords() throws Exception {

        ControlMeasure cm = new ControlMeasure();
        String name = "cm test one" + Math.random();
        cm.setEquipmentLife(new Float(12));
        cm.setName(name);
        cm.setAbbreviation("12345679");
        cm.setLastModifiedBy(userService.getUser("emf").getName());
        cm.setLastModifiedTime(new Date());
        Scc scc1 = new Scc("1023232", "");
        int cmId = service.addMeasure(cm, new Scc[] { scc1 });
        EfficiencyRecord records1 = efficiencyRecord(COPollutant(), "23");
        records1.setControlMeasureId(cmId);
        service.addEfficiencyRecord(records1);
        EfficiencyRecord records2 = efficiencyRecord(pm10Pollutant(), "37");
        records2.setControlMeasureId(cmId);
        service.addEfficiencyRecord(records2);

        try {
            EfficiencyRecord[] ers = service.getEfficiencyRecords(cmId);

            assertEquals(2, ers.length);
            assertEquals("23", ers[0].getLocale());
            assertEquals("37", ers[1].getLocale());
        } finally {
            remove(records1);
            remove(records2);
            remove(scc1);
            remove(cm);
        }
    }

    public void testShouldUpdateControlMeasureEfficiencyRecord() throws Exception {

        ControlMeasure cm = new ControlMeasure();
        String name = "cm test one" + Math.random();
        cm.setEquipmentLife(new Float(12));
        cm.setName(name);
        cm.setAbbreviation("12345679");
        cm.setLastModifiedBy(userService.getUser("emf").getName());
        cm.setLastModifiedTime(new Date());
        Scc scc1 = new Scc("1023232", "");
        int cmId = service.addMeasure(cm, new Scc[] { scc1 });
        EfficiencyRecord records1 = efficiencyRecord(COPollutant(), "23");
        records1.setControlMeasureId(cmId);
        service.addEfficiencyRecord(records1);

        try {
            EfficiencyRecord[] ers = service.getEfficiencyRecords(cmId);

            assertEquals(1, ers.length);
            assertEquals("23", ers[0].getLocale());

            ers[0].setLocale("37");
            ers[0].setPollutant(pm10Pollutant());
            service.updateEfficiencyRecord(ers[0]);
            
            records1 = service.getEfficiencyRecords(cmId)[0];
            assertEquals("37", records1.getLocale());
            assertEquals(pm10Pollutant(), records1.getPollutant());
        } finally {
            remove(records1);
            remove(scc1);
            remove(cm);
        }
    }

    public void testShouldRemoveControlMeasureEfficiencyRecord() throws Exception {

        ControlMeasure cm = new ControlMeasure();
        String name = "cm test one" + Math.random();
        cm.setEquipmentLife(new Float(12));
        cm.setName(name);
        cm.setAbbreviation("12345679");
        cm.setLastModifiedBy(userService.getUser("emf").getName());
        cm.setLastModifiedTime(new Date());
        Scc scc1 = new Scc("1023232", "");
        int cmId = service.addMeasure(cm, new Scc[] { scc1 });
        EfficiencyRecord records1 = efficiencyRecord(COPollutant(), "23");
        records1.setControlMeasureId(cmId);
        int erId = service.addEfficiencyRecord(records1);

        try {
            EfficiencyRecord[] ers = service.getEfficiencyRecords(cmId);

            assertEquals(1, ers.length);
            assertEquals("23", ers[0].getLocale());

            service.removeEfficiencyRecord(erId);
            
            ers = service.getEfficiencyRecords(cmId);

            assertEquals(0, ers.length);
        } finally {
            remove(records1);
            remove(scc1);
            remove(cm);
        }
    }

    private List load(Class clazz) {
        session.clear();// flush cached objects

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz);
            tx.commit();
            return crit.list();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private ControlMeasure load(ControlMeasure cm) {
        session.clear();// flush cached objects

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(ControlMeasure.class).add(Restrictions.eq("name", cm.getName()));
            tx.commit();

            return (ControlMeasure) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private EfficiencyRecord efficiencyRecord(Pollutant pollutant, String locale) {
        EfficiencyRecord record = new EfficiencyRecord();
        record.setPollutant(pollutant);
        record.setLocale(locale);
        record.setLastModifiedBy("emf");
        record.setLastModifiedTime(new Date());
        return record;
    }

    private Pollutant pm10Pollutant() {
        return (Pollutant) load(Pollutant.class, "PM10");
    }

    private Pollutant COPollutant() {
        return (Pollutant) load(Pollutant.class, "CO");
    }

}
