package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class CaseDAO_CaseTest extends ServiceTestCase {

    private CaseDAO dao;

    protected void doSetUp() throws Exception {
        dao = new CaseDAO();
    }

    protected void doTearDown() {// no op
    }

    public void testShouldPersistEmptyCaseOnAdd() {
        int totalBeforeAdd = dao.getCases(session).size();

        Case element = new Case("test" + Math.random());
        add(element);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

    public void testShouldRemoveCaseOnRemove() {
        int totalBeforeAdd = dao.getCases(session).size();
        Case element = new Case("test" + Math.random());
        add(element);

        session.clear();
        dao.remove(element, session);
        List list = dao.getCases(session);
        assertEquals(totalBeforeAdd, list.size());
    }

    public void testShouldGetAllCases() {
        int totalBeforeAdd = dao.getCases(session).size();
        Case element = newCase();

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(totalBeforeAdd + 1, list.size());
            assertTrue(list.contains(element));
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithPrimitiveAttributesOnAdd() {
        int totalBeforeAdd = dao.getCases(session).size();

        Case element = new Case("test" + Math.random());
        element.setDescription("desc");
        element.setRunStatus("started");
        element.setLastModifiedDate(new Date());
        element.setTemplateUsed("another dataset");

        add(element);

        session.clear();
        try {
            List list = dao.getCases(session);

            Case added = (Case) list.get(totalBeforeAdd);
            assertEquals(element.getDescription(), added.getDescription());
            assertEquals(element.getRunStatus(), added.getRunStatus());
            assertEquals(element.getLastModifiedDate(), added.getLastModifiedDate());
            assertEquals(element.getTemplateUsed(), added.getTemplateUsed());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithAnAbbreviationOnAdd() {
        Case element = new Case("test" + Math.random());
        Abbreviation abbreviation = new Abbreviation("test" + Math.random());
        add(abbreviation);
        List abbrs = dao.getAbbreviations(session);
        element.setAbbreviation((Abbreviation) abbrs.get(abbrs.size() - 1));

        add(element);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(abbreviation, ((Case) list.get(list.size()-1)).getAbbreviation());
        } finally {
            remove(element);
            remove(abbreviation);
        }
    }

    public void testShouldPersistCaseWithAnAirQualityModelOnAdd() {
        Case element = new Case("test" + Math.random());
        AirQualityModel aqm = new AirQualityModel("test" + Math.random());
        add(aqm);
        element.setAirQualityModel(aqm);

        add(element);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(aqm, ((Case) list.get(0)).getAirQualityModel());
        } finally {
            remove(element);
            remove(aqm);
        }
    }

    public void testShouldPersistCaseWithAnCaseInputAndASubdirOnAdd() {
        Case element = new Case("test" + Math.random());
        SubDir subdir = new SubDir("sub/dir" + Math.random());
        add(subdir);
        CaseInput input = new CaseInput();
        input.setSubdirObj(subdir);
        add(element);
        session.clear();

        try {
            List list = dao.getCases(session);
            Case loadedCase = (Case) list.get(list.size() - 1);
            input.setCaseID(loadedCase.getId());
            add(input);
            session.clear();

            List inputs = dao.getCaseInputs(loadedCase.getId(), session);
            assertEquals(input, inputs.get(inputs.size() - 1));
            assertEquals(subdir, ((CaseInput) inputs.get(inputs.size() - 1)).getSubdirObj());
            assertEquals(1, inputs.size());
        } finally {
            remove(input);
            remove(subdir);
            remove(element);
        }
    }

    public void testShouldThrowExceptionWhenAnCaseInputHasSameSectorProgramInputname() {
        Case element = new Case("test" + Math.random());
        InputName inputname = new InputName("test input name");
        CaseProgram program = new CaseProgram("test case program");
        Sector sector = new Sector("", "test sector");
        CaseInput inputOne = new CaseInput();
        CaseInput inputTwo = new CaseInput();

        add(inputname);
        add(program);
        add(sector);
        add(element);
        inputOne.setInputName(inputname);
        inputOne.setProgram(program);
        inputOne.setSector(sector);
        inputTwo.setInputName(inputname);
        inputTwo.setProgram(program);
        inputTwo.setSector(sector);

        session.clear();

        try {
            List list = dao.getCases(session);
            Case loadedCase = (Case) list.get(0);
            inputOne.setCaseID(loadedCase.getId());
            inputTwo.setCaseID(loadedCase.getId());
            add(inputOne);
            add(inputTwo);
        } catch (Exception e) {
            String exceptionMsg = "could not insert:";
            assertTrue("Should throw exception", e.getMessage().startsWith(exceptionMsg));
        } finally {
            remove(inputOne);
            remove(inputname);
            remove(program);
            remove(sector);
            remove(element);
        }
    }

    public void testShouldPersistCaseWithCaseCategoryOnAdd() {
        Case element = new Case("test" + Math.random());
        CaseCategory attrib = new CaseCategory("test" + Math.random());
        add(attrib);
        element.setCaseCategory(attrib);

        add(element);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getCaseCategory());
        } finally {
            remove(element);
            remove(attrib);
        }
    }

    public void testShouldPersistCaseWithEmissionsYearOnAdd() {
        Case element = new Case("test" + Math.random());
        EmissionsYear attrib = new EmissionsYear("test" + Math.random());
        add(attrib);
        element.setEmissionsYear(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getEmissionsYear());
        } finally {
            remove(element);
            remove(attrib);
        }
    }

    public void testShouldPersistCaseWithMeteorlogicalYearOnAdd() {
        Case element = new Case("test" + Math.random());
        MeteorlogicalYear attrib = new MeteorlogicalYear("test" + Math.random());
        add(attrib);
        element.setMeteorlogicalYear(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getMeteorlogicalYear());
        } finally {
            remove(element);
            remove(attrib);
        }
    }

    public void testShouldPersistCaseWithSpeciationOnAdd() {
        Case element = new Case("test" + Math.random());
        Speciation attrib = new Speciation("test" + Math.random());
        add(attrib);
        element.setSpeciation(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getSpeciation());
        } finally {
            remove(element);
            remove(attrib);
        }
    }

    public void testShouldPersistCaseWithCreatorOnAdd() {
        Case element = new Case("test" + Math.random());
        UserDAO userDAO = new UserDAO();
        User creator = userDAO.get("emf", session);
        element.setLastModifiedBy(creator);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(creator, ((Case) list.get(0)).getLastModifiedBy());
        } finally {
            remove(element);
        }
    }

    public void testShouldPersistCaseWithProjectOnAdd() {
        Case element = new Case("test" + Math.random());
        Project attrib = new Project("test" + Math.random());
        add(attrib);
        element.setProject(attrib);

        add(element);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getProject());
        } finally {
            remove(element);
            remove(attrib);
        }
    }

    public void testShouldPersistCaseWithRegionOnAdd() {
        Case element = new Case("test" + Math.random());
        Region attrib = new Region("test" + Math.random());
        add(attrib);
        element.setControlRegion(attrib);

        dao.add(element, session);

        session.clear();
        try {
            List list = dao.getCases(session);
            assertEquals(attrib, ((Case) list.get(0)).getControlRegion());
        } finally {
            remove(element);
            remove(attrib);
        }
    }

    public void testShouldObtainLockedCaseForUpdate() {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);

        Case element = new Case("test" + Math.random());
        add(element);

        try {
            Case locked = dao.obtainLocked(owner, element, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());

            Case loadedFromDb = load(element);
            assertEquals(owner.getUsername(), loadedFromDb.getLockOwner());
        } finally {
            remove(element);
        }
    }

    public void testShouldReleaseLock() {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);

        Case element = new Case("test" + Math.random());
        add(element);

        try {
            Case locked = dao.obtainLocked(owner, element, session);
            Case released = dao.releaseLocked(owner, locked, session);
            assertFalse("Should have released lock", released.isLocked());

            Case loadedFromDb = load(element);
            assertFalse("Should have released lock", loadedFromDb.isLocked());
        } finally {
            remove(element);
        }
    }

    public void testShouldFailToGetLockWhenAlreadyLockedByAnotherUser() {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);

        Case element = new Case("test" + Math.random());
        add(element);

        try {
            dao.obtainLocked(owner, element, session);

            User user = userDAO.get("admin", session);
            Case result = dao.obtainLocked(user, element, session);

            assertFalse("Should have failed to obtain lock as it's already locked by another user", result
                    .isLocked(user));// failed to obtain lock for another user
        } finally {
            remove(element);
        }
    }

    public void testShouldUpdateCaseOnUpdate() throws EmfException {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);

        Case element = newCase();

        session.clear();
        try {
            Case locked = dao.obtainLocked(owner, element, session);
            assertEquals(locked.getLockOwner(), owner.getUsername());
            locked.setName("TEST");

            Case modified = dao.update(locked, session);
            assertEquals("TEST", locked.getName());
            assertEquals(modified.getLockOwner(), null);
        } finally {
            remove(element);
        }
    }

    
    public void testGetCasesThatInputToOtherCases() {
        try {
            Case[] cases = dao.getCasesThatInputToOtherCases(1, session).toArray(new Case[0]);

            assertEquals(0, cases.length);
        } finally {
            //
        }
    }

    public void testGetCasesThatOutputToOtherCases() {
        try {
            Case[] cases = dao.getCasesThatOutputToOtherCases(1, session).toArray(new Case[0]);

            assertEquals(0, cases.length);
        } finally {
            //
        }
    }

    public void testGetCasesByOutputDatasets() {
        try {
            Case[] cases = dao.getCasesByOutputDatasets(new int[] {1,2,3}, session).toArray(new Case[0]);

            assertEquals(0, cases.length);
        } finally {
            //
        }
    }

    public void testGetCasesByInputDataset() {
        try {
            Case[] cases = dao.getCasesByInputDataset(1, session).toArray(new Case[0]);

            assertEquals(0, cases.length);
        } finally {
            //
        }
    }

    private Case newCase() {
        Case element = new Case("test" + Math.random());
        add(element);

        return element;
    }

    private Case load(Case caseObj) {
        Transaction tx = null;

        session.clear();
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
