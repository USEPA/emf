package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class CaseServiceCaseInputTest extends ServiceTestCase {

    protected void doSetUp() throws Exception {//no-op
    }

    protected void doTearDown() throws Exception {
        System.gc();
    }

    private Case newCase() {
        Case element = new Case("test" + Math.random());
        add(element);
        return element;
    }

    public void testShouldPersistACaseInputWhenAddCaseInputWithSomeFieldsAreNull() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());
        CaseService service = new CaseServiceImpl(sessionFactory, dbServerFactory);
        UserService usersvc = new UserServiceImpl(sessionFactory);
        User user = usersvc.getUser("emf");
        
        Case element = newCase();
        InputName inputname = new InputName("test input name");
        CaseProgram program = new CaseProgram("test case program");
        Sector sector = new Sector("" , "test sector one");
        CaseInput inputOne = new CaseInput();
        CaseInput inputTwo = new CaseInput();
        CaseInput inputThree = new CaseInput();
        SubDir subdir = new SubDir("test subdir name one");
        
        add(inputname);
        add(program);
        add(sector);
        add(subdir);
        inputOne.setInputName(null);
        inputOne.setProgram(program);
        inputOne.setSector(sector);
        inputOne.setSubdirObj(null);
        inputTwo.setInputName(inputname);
        inputTwo.setProgram(null);
        inputTwo.setSector(sector);
        inputTwo.setSubdirObj(subdir);
        inputThree.setInputName(inputname);
        inputThree.setProgram(null);
        inputThree.setSector(null);
        inputThree.setSubdirObj(subdir);
        
        try {
            int caseId = load(element).getId();
            inputOne.setCaseID(caseId);
            inputTwo.setCaseID(caseId);
            inputThree.setCaseID(caseId);
            CaseInput returnedInput1 = service.addCaseInput(user, inputOne);
            CaseInput returnedInput2 = service.addCaseInput(user, inputTwo);
            CaseInput returnedInput3 = service.addCaseInput(user, inputThree);
            
            CaseInput[] loadedInputs = service.getCaseInputs(caseId);
            assertEquals(3, loadedInputs.length);
            assertEquals(returnedInput2, loadedInputs[0]);
            assertEquals(returnedInput3, loadedInputs[1]);
            assertEquals(returnedInput1, loadedInputs[2]);
            assertEquals("test input name", loadedInputs[0].getName());
            assertEquals(null, loadedInputs[0].getProgram());
            assertEquals(null, loadedInputs[0].getEnvtVars());
            assertEquals("test sector one", loadedInputs[0].getSector().getName());
            assertEquals("test subdir name one", loadedInputs[0].getSubdirObj().getName());
            assertEquals("test input name", loadedInputs[1].getInputName().getName());
            assertEquals(null, loadedInputs[1].getProgram());
            assertEquals(null, loadedInputs[1].getEnvtVars());
            assertEquals(null, loadedInputs[1].getSector());
            assertEquals("test subdir name one", loadedInputs[1].getSubdirObj().getName());
            assertEquals(null, loadedInputs[2].getInputName());
            assertEquals("test case program", loadedInputs[2].getProgram().getName());
            assertEquals(null, loadedInputs[2].getEnvtVars());
            assertEquals("test sector one", loadedInputs[2].getSector().getName());
            assertEquals(null, loadedInputs[2].getSubdirObj());
        } finally {
            remove(inputOne);
            remove(inputTwo);
            remove(inputThree);
            remove(inputname);
            remove(program);
            remove(sector);
            remove(subdir);
            remove(element);
        }
    }
    
    public void testShouldUpdateCaseInput() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());
        CaseService service = new CaseServiceImpl(sessionFactory, dbServerFactory);
        UserService usersvc = new UserServiceImpl(sessionFactory);
        User user = usersvc.getUser("emf");
        
        Case element = newCase();
        InputName inputname = new InputName("test input name");
        CaseProgram program = new CaseProgram("test case program");
        Sector sector = new Sector("" , "test sector one");
        InputEnvtVar envtVars = new InputEnvtVar("test envt vars");
        CaseInput inputOne = new CaseInput();
        CaseInput inputTwo = new CaseInput();
        CaseInput inputThree = new CaseInput();
        SubDir subdir = new SubDir("test subdir name one");
        
        add(inputname);
        add(program);
        add(sector);
        add(envtVars);
        add(subdir);
        inputOne.setInputName(null);
        inputOne.setProgram(program);
        inputOne.setSector(sector);
        inputOne.setSubdirObj(null);
        inputTwo.setInputName(inputname);
        inputTwo.setProgram(null);
        inputTwo.setSector(sector);
        inputTwo.setSubdirObj(subdir);
        inputThree.setInputName(inputname);
        inputThree.setProgram(null);
        inputThree.setSector(null);
        inputThree.setSubdirObj(subdir);
        
        try {
            int caseId = load(element).getId();
            inputOne.setCaseID(caseId);
            inputTwo.setCaseID(caseId);
            inputThree.setCaseID(caseId);
            CaseInput returnedInput1 = service.addCaseInput(user, inputOne);
            CaseInput returnedInput2 = service.addCaseInput(user, inputTwo);
            CaseInput returnedInput3 = service.addCaseInput(user, inputThree);
            returnedInput1.setEnvtVars(envtVars);
            returnedInput2.setEnvtVars(envtVars);
            returnedInput3.setEnvtVars(envtVars);
            
            service.updateCaseInput(user, returnedInput1);
            service.updateCaseInput(user, returnedInput2);
            service.updateCaseInput(user, returnedInput3);
            
            CaseInput[] loadedInputs = service.getCaseInputs(caseId);
            assertEquals(3, loadedInputs.length);
            assertEquals("test input name", loadedInputs[0].getName());
            assertEquals(null, loadedInputs[0].getProgram());
            assertEquals("test envt vars", loadedInputs[0].getEnvtVars().getName());
            assertEquals("test sector one", loadedInputs[0].getSector().getName());
            assertEquals("test subdir name one", loadedInputs[0].getSubdirObj().getName());
            assertEquals("test input name", loadedInputs[1].getInputName().getName());
            assertEquals(null, loadedInputs[1].getProgram());
            assertEquals("test envt vars", loadedInputs[1].getEnvtVars().getName());
            assertEquals(null, loadedInputs[1].getSector());
            assertEquals("test subdir name one", loadedInputs[1].getSubdirObj().getName());
            assertEquals(null, loadedInputs[2].getInputName());
            assertEquals("test case program", loadedInputs[2].getProgram().getName());
            assertEquals("test envt vars", loadedInputs[2].getEnvtVars().getName());
            assertEquals("test sector one", loadedInputs[2].getSector().getName());
            assertEquals(null, loadedInputs[2].getSubdirObj());
        } finally {
            remove(inputOne);
            remove(inputTwo);
            remove(inputThree);
            remove(inputname);
            remove(program);
            remove(sector);
            remove(envtVars);
            remove(subdir);
            remove(element);
        }
    }
    
    public void testShouldRemoveCaseinputs() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());
        CaseService service = new CaseServiceImpl(sessionFactory, dbServerFactory);
        UserService usersvc = new UserServiceImpl(sessionFactory);
        User user = usersvc.getUser("emf");
        
        Case element = newCase();
        InputName inputname = new InputName("test input name");
        CaseProgram program = new CaseProgram("test case program");
        Sector sector = new Sector("" , "test sector one");
        InputEnvtVar envtVars = new InputEnvtVar("test envt vars");
        CaseInput inputOne = new CaseInput();
        CaseInput inputTwo = new CaseInput();
        CaseInput inputThree = new CaseInput();
        SubDir subdir = new SubDir("test subdir name one");
        
        add(inputname);
        add(program);
        add(sector);
        add(envtVars);
        add(subdir);
        inputOne.setInputName(null);
        inputOne.setProgram(program);
        inputOne.setSector(sector);
        inputOne.setSubdirObj(null);
        inputTwo.setInputName(inputname);
        inputTwo.setProgram(null);
        inputTwo.setSector(sector);
        inputTwo.setSubdirObj(subdir);
        inputThree.setInputName(inputname);
        inputThree.setProgram(null);
        inputThree.setSector(null);
        inputThree.setSubdirObj(subdir);
        
        try {
            int caseId = load(element).getId();
            inputOne.setCaseID(caseId);
            inputTwo.setCaseID(caseId);
            inputThree.setCaseID(caseId);
            CaseInput returnedInput1 = service.addCaseInput(user, inputOne);
            CaseInput returnedInput2 = service.addCaseInput(user, inputTwo);
            CaseInput returnedInput3 = service.addCaseInput(user, inputThree);
            returnedInput1.setEnvtVars(envtVars);
            returnedInput2.setEnvtVars(envtVars);
            returnedInput3.setEnvtVars(envtVars);
            
            service.updateCaseInput(user, returnedInput1);
            service.updateCaseInput(user, returnedInput2);
            service.updateCaseInput(user, returnedInput3);
            
            CaseInput[] loadedInputs = service.getCaseInputs(caseId);
            service.removeCaseInputs(user,loadedInputs);
            
            CaseInput[] loadedInputsAfterDeletion = service.getCaseInputs(caseId);
            
            assertEquals(3, loadedInputs.length);
            assertEquals(0, loadedInputsAfterDeletion.length);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            remove(inputname);
            remove(program);
            remove(sector);
            remove(envtVars);
            remove(subdir);
            remove(element);
        }
    }
    
//    public void testShouldAddCaseJobCorrectly() throws Exception {
//        Case caseObj = newCase();
//        int caseId = caseObj.getId();
//        CaseJob job = new CaseJob();
//        job.setName("TEST");
//        job.setCaseId(caseId);
//        
//        try {
//            CaseJob loaded = service.addCaseJob(job);
//            assertEquals("TEST", loaded.getName());
//            assertEquals(caseId, loaded.getCaseId());
//        } catch (Exception exc) {
//            exc.printStackTrace();
//        } finally {
//            remove(job);
//            remove(caseObj);
//        }
//        
//    }
    
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
