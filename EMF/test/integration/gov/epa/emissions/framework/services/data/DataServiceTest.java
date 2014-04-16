package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class DataServiceTest extends ServiceTestCase {

    private DataServiceImpl service;

    private UserService userService;

    private DataCommonsServiceImpl dataCommonsService;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());
        service = new DataServiceImpl(dbServerFactory, sessionFactory);
        userService = new UserServiceImpl(sessionFactory);
        dataCommonsService = new DataCommonsServiceImpl(sessionFactory);
        deleteAllDatasets();
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetAllDatasets() throws EmfException {
        EmfDataset[] datasets = service.getDatasets("", 1);
        assertEquals(0, datasets.length);

        EmfDataset dataset = newDataset();

        try {
            EmfDataset[] postInsert = service.getDatasets("", 1);
            assertEquals(1, postInsert.length);
        } finally {
            remove(dataset);
        }
    }

    public void testShouldObtainLockedDataset() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            assertTrue("Should be locked by owner", locked.isLocked(owner));

            EmfDataset loadedFromDb = load(dataset);// object returned directly from the table
            assertEquals(locked.getLockOwner(), loadedFromDb.getLockOwner());
        } finally {
            remove(dataset);
        }
    }

    public void testShouldReleaseLockedDataset() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            EmfDataset released = service.releaseLockedDataset(owner, locked);
            assertFalse("Should have released lock", released.isLocked());

            EmfDataset loadedFromDb = load(dataset);
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(dataset);
        }
    }

    public void FIXME_testShouldUpdateDataset() throws Exception {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            locked.setName("TEST");
            locked.setDescription("TEST dataset");

            EmfDataset released = service.updateDataset(locked);
            assertEquals("TEST", released.getName());
            assertEquals("TEST dataset", released.getDescription());
            assertEquals(released.getLockOwner(), null);
            assertFalse("Lock should be released on update", released.isLocked());
        } finally {
            remove(dataset);
        }
    }

    private EmfDataset newDataset() throws EmfException {
        EmfDataset dataset = new EmfDataset();

        User owner = userService.getUser("emf");

        dataset.setName("data-service-test" + Math.abs(new Random().nextInt()));
        dataset.setCreator(owner.getUsername());

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(dataset);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

        return load(dataset);
    }

    private EmfDataset load(EmfDataset dataset) {
        session.clear();// flush cached objects

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(EmfDataset.class).add(Restrictions.eq("name", dataset.getName()));
            tx.commit();

            return (EmfDataset) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void testShouldAddMultipleSectorsFromWithinDataset() throws EmfException {
        User owner = userService.getUser("emf");
        Sector[] allSectors = dataCommonsService.getSectors();

        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            locked.addSector(allSectors[0]);
            locked.addSector(allSectors[1]);
            EmfDataset released = service.updateDataset(locked);
            Sector[] sectorsFromDataset = released.getSectors();

            assertEquals(2, sectorsFromDataset.length);
        } finally {
            remove(dataset);
        }
    }

    public void testShouldAddCountryFromWithinDataset() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        Country country = new Country("FOOBAR");
        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            dataCommonsService.addCountry(country);
            locked.setCountry(country);

            EmfDataset released = service.updateDataset(locked);
            assertEquals("FOOBAR", released.getCountry().getName());
        } finally {
            remove(dataset);
            remove(country);
        }
    }

    public void testShouldAddProjectFromWithinDataset() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        Project project = new Project("FOOBAR");
        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            dataCommonsService.addProject(project);
            locked.setProject(project);

            EmfDataset released = service.updateDataset(locked);
            assertEquals("FOOBAR", released.getProject().getName());
        } finally {
            remove(dataset);
            remove(project);
        }
    }

    public void testShouldAddRegionFromWithinDataset() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();
        Region region = new Region("FOOBAR");
        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            locked.setRegion(region);
            dataCommonsService.addRegion(region);
            EmfDataset released = service.updateDataset(locked);
            assertEquals("FOOBAR", released.getRegion().getName());
        } finally {
            remove(dataset);
            remove(region);
        }
    }

    public void FIXME_testShouldUpdateDatsetName() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset = newDataset();

        try {
            EmfDataset locked = service.obtainLockedDataset(owner, dataset);
            locked.setName("TEST dataset");

            EmfDataset released = service.updateDataset(locked);
            assertEquals("TEST dataset", released.getName());
        } finally {
            remove(dataset);
        }
    }

    public void FIXME_testShouldFailOnAttemptToUpdateDatasetWithDuplicateName() throws EmfException {
        EmfDataset dataset1 = newDataset();

        EmfDataset dataset2 = newDataset();
        try {
            dataset2.setName(dataset1.getName());
            service.updateDataset(dataset2);
        } catch (EmfException e) {
            assertEquals("The Dataset name is already in use", e.getMessage());
            return;
        } finally {
            remove(dataset1);
        }
    }

    public void FIXME_testShouldDeleteDatasets() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset1 = newDataset();
        EmfDataset dataset2 = newDataset();
        dataset1.setCreator("emf");
        dataset2.setCreator("emf");

        try {
            EmfDataset locked1 = service.obtainLockedDataset(owner, dataset1);
            EmfDataset locked2 = service.obtainLockedDataset(owner, dataset2);
            service.deleteDatasets(owner, new EmfDataset[] { locked1, locked2 });
            EmfDataset[] datasets = service.getDatasets("", owner.getId());

            assertEquals("0 datasets", 0, datasets.length);
        } catch (EmfException e) {
            return;
        } finally {
            remove(dataset1);
            remove(dataset2);
        }
    }

    public void testShouldFailOnDeletingDatasetsWithADifferentCreator() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset1 = newDataset();
        EmfDataset dataset2 = newDataset();
        dataset1.setCreator("emf1");
        dataset2.setCreator("emf2");

        try {
            service.deleteDatasets(owner, new EmfDataset[] { dataset1, dataset2 });
        } catch (EmfException e) {
            assertTrue("Should give error msg.", e.getMessage().startsWith("Cannot delete"));
            return;
        } finally {
            remove(dataset1);
            remove(dataset2);
        }
    }

    public void testShouldFailOnDeletingDatasetsUsedByAControlStrategy() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset1 = newDataset();
        EmfDataset dataset2 = newDataset();
        ControlStrategy strategy = newControlStrategy(new ControlStrategyInputDataset(dataset1));

        try {
            assertEquals("data-service-test-strategy", strategy.getName());
            service.deleteDatasets(owner, new EmfDataset[] { dataset1, dataset2 });
        } catch (EmfException e) {
            assertTrue("Should give error msg.", e.getMessage().startsWith("Cannot delete"));
            return;
        } finally {
            remove(strategy);
            remove(dataset1);
            remove(dataset2);
        }
    }

    public void testShouldFailOnDeletingDatasetsUsedByACase() throws EmfException {
        User owner = userService.getUser("emf");
        EmfDataset dataset1 = newDataset();
        EmfDataset dataset2 = newDataset();
        Case caseObj = newCase(dataset2);
        CaseInput input = new CaseInput();
        input.setDataset(dataset2);
        input.setCaseID(caseObj.getId());
        save(input);
        
        try {
            assertEquals("data-service-test-case", caseObj.getName());
            service.deleteDatasets(owner, new EmfDataset[] { dataset1, dataset2 });
        } catch (EmfException e) {
            assertTrue("Should give error msg.", e.getMessage().startsWith("Cannot delete"));
            return;
        } finally {
            remove(input);
            remove(caseObj);
            remove(dataset1);
            remove(dataset2);
        }
    }

    private ControlStrategy newControlStrategy(ControlStrategyInputDataset dataset) {
        ControlStrategy strategy = new ControlStrategy("data-service-test-strategy");
        strategy.setControlStrategyInputDatasets(new ControlStrategyInputDataset[] { dataset });

        session.clear();// flush cached objects
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(strategy);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

        return loadControlStrategy(strategy);
    }

    private ControlStrategy loadControlStrategy(ControlStrategy strategy) {
        session.clear();// flush cached objects

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(ControlStrategy.class).add(
                    Restrictions.eq("name", strategy.getName()));
            tx.commit();

            return (ControlStrategy) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private Case newCase(EmfDataset dataset) {
        //CaseInput input = new CaseInput();
        //input.setDataset(dataset);
        Case caseObj = new Case("data-service-test-case");
        //caseObj.setCaseInputs(new CaseInput[] { input });

        session.clear();// flush cached objects
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(caseObj);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

        return loadCase(caseObj);
    }

    private Case loadCase(Case caseObj) {
        session.clear();// flush cached objects

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Case.class).add(Restrictions.eq("name", caseObj.getName()));
            tx.commit();

            return (Case) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

}
