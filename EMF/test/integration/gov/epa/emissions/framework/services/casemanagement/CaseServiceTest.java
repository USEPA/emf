package gov.epa.emissions.framework.services.casemanagement;

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

public class CaseServiceTest extends ServiceTestCase {

    private CaseServiceImpl service;

    private UserServiceImpl userService;

    private HibernateSessionFactory sessionFactory;
    
    private User user;

    protected void doSetUp() throws Exception {
        sessionFactory = sessionFactory(configFile());
        service = new CaseServiceImpl(sessionFactory, dbServerFactory);
        userService = new UserServiceImpl(sessionFactory);
        user = userService.getUser("emf");
    }

    protected void doTearDown() throws Exception {
        service = null;
        userService = null;
        sessionFactory = null;
        System.gc();
    }

    public void testShouldGetAbbreviations() throws Exception {
        int totalBeforeAdd = service.getAbbreviations().length;
        Abbreviation element = new Abbreviation("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getAbbreviations());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetAirQualityModels() throws Exception {
        int totalBeforeAdd = service.getAirQualityModels().length;
        AirQualityModel element = new AirQualityModel("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getAirQualityModels());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetCaseCategories() throws Exception {
        int totalBeforeAdd = service.getCaseCategories().length;
        CaseCategory element = new CaseCategory("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getCaseCategories());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetEmissionsYears() throws Exception {
        int totalBeforeAdd = service.getEmissionsYears().length;
        EmissionsYear element = new EmissionsYear("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getEmissionsYears());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetMeteorlogicalYears() throws Exception {
        int totalBeforeAdd = service.getMeteorlogicalYears().length;
        MeteorlogicalYear element = new MeteorlogicalYear("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getMeteorlogicalYears());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetModelToRuns() throws Exception {
        int totalBeforeAdd = service.getModelToRuns().length;
        ModelToRun element = new ModelToRun("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getModelToRuns());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetSpeciations() throws Exception {
        int totalBeforeAdd = service.getSpeciations().length;
        Speciation element = new Speciation("test" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getSpeciations());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetInputNames() throws Exception {
        int totalBeforeAdd = service.getInputNames().length;

        InputName element = new InputName("input name one" + Math.random());
        add(element);

        try {
            List list = Arrays.asList(service.getInputNames());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldGetInputEnvtVars() throws Exception {
        int totalBeforeAdd = service.getInputEnvtVars().length;
        InputEnvtVar envtVar = new InputEnvtVar("envt var one" + Math.random());
        add(envtVar);

        try {
            List list = Arrays.asList(service.getInputEnvtVars());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(envtVar));
        } finally {
            remove(envtVar);
        }
    }

    public void testShouldGetPrograms() throws Exception {
        int totalBeforeAdd = service.getPrograms().length;
        CaseProgram program = new CaseProgram("input name one" + Math.random());
        add(program);

        try {
            List list = Arrays.asList(service.getPrograms());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(program));
        } finally {
            remove(program);
        }
    }

    public void testShouldGetSubdirs() throws Exception {
        int totalBeforeAdd = service.getSubDirs().length;
        SubDir subdir = new SubDir("subdir name one" + Math.random());
        add(subdir);

        try {
            List list = Arrays.asList(service.getSubDirs());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(subdir));
        } finally {
            remove(subdir);
        }
    }

    public void testShouldGetCases() throws Exception {
        int totalBeforeAdd = service.getCases().length;
        Case element = newCase();

        try {
            List list = Arrays.asList(service.getCases());
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    private Case newCase() {
        // create a new case and set the name and export top dir
        Case element = new Case("test" + Math.random());
        element.setInputFileDir("/home/azubrow/smoke_emf_training/2002/smoke");

        // adds the element to the db and then reloads it from the db
        // ensures that it has an id
        add(element);
        return (Case) load(Case.class, element.getName());
    }

    public void testShouldAddCase() throws Exception {
        int totalBeforeAdd = service.getCases().length;
        Case element = new Case("test" + Math.random());

        service.addCase(user, element);

        try {
            List list = Arrays.asList(service.getCases());

            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldRemoveCase() throws Exception {
        int totalBeforeAdd = service.getCases().length;
        Case element = new Case("test" + Math.random());
        element.setLastModifiedBy(user);
        service.addCase(user, element);

        service.removeCase(element);

        List list = Arrays.asList(service.getCases());
        assertEquals(totalBeforeAdd, list.size());
        assertFalse(list.contains(element));
    }

    public void testShouldObtainLockedCase() throws EmfException {
        User owner = userService.getUser("emf");
        Case element = newCase();

        try {
            Case locked = service.obtainLocked(owner, element);
            assertTrue("Should be locked by owner", locked.isLocked(owner));

            Case loadedFromDb = (Case) load(Case.class, element.getName());// object returned directly from the table
            assertEquals(locked.getLockOwner(), loadedFromDb.getLockOwner());
        } finally {
            remove(element);
        }
    }

    public void testShouldUpdateCase() throws Exception {
        User owner = userService.getUser("emf");
        Case element = newCase();

        try {
            Case locked = service.obtainLocked(owner, element);
            locked.setName("TEST");
            locked.setDescription("TEST case");

            Case released = service.updateCase(locked);
            assertEquals("TEST", released.getName());
            assertEquals("TEST case", released.getDescription());
            assertEquals(released.getLockOwner(), null);
            assertFalse("Lock should be released on update", released.isLocked());
        } finally {
            remove(element);
        }
    }

    public Object load(Class clazz, String className) {
        /**
         * loads the abstract class through hibernate from db need to pass class name (type) and recast the return
         */

        Transaction tx = null;

        session.clear();
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("name", className));
            tx.commit();

            return crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}
