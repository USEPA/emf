package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

import org.hibernate.Session;

public class QADaoTest extends ServiceTestCase {

    private QADAO dao;

    private UserDAO userDAO;

    protected void doSetUp() throws Exception {
        dao = new QADAO();
        userDAO = new UserDAO();
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetQASteps() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");

        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setName("name");
        step.setVersion(2);
        add(step);

        try {
            QAStep[] steps = dao.steps(dataset, session);

            assertEquals(1, steps.length);
            assertEquals("name", steps[0].getName());
            assertEquals(2, steps[0].getVersion());
        } finally {
            remove(step);
            remove(dataset);
        }
    }

    public void testShouldUpdateQASteps() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");

        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setName("name");
        step.setVersion(2);
        add(step);
        QAProgram program = program();

        try {
            QAStep[] read = dao.steps(dataset, session);
            assertEquals(1, read.length);

            read[0].setName("updated-name");
            read[0].setProgram(program);

            dao.update(read, session);
            session.clear();// to ensure Hibernate does not return cached objects

            QAStep[] updated = dao.steps(dataset, session);
            assertEquals(1, updated.length);
            assertEquals("updated-name", updated[0].getName());
            assertEquals("SQL", updated[0].getProgram().getName());
        } finally {
            remove(step);
            remove(dataset);
        }
    }

    private QAProgram program() throws Exception {
        Session session = sessionFactory().getSession();
        try {
            return new QADAO().getQAPrograms(session)[0];
        } finally {
            session.close();
        }
    }

    public void testShouldAddNewStepsOnUpdateQASteps() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");

        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setName("name");
        step.setVersion(2);

        try {
            dao.update(new QAStep[] { step }, session);
            session.clear();// to ensure Hibernate does not return cached objects

            QAStep[] updated = dao.steps(dataset, session);
            assertEquals(1, updated.length);
            assertEquals("name", updated[0].getName());
            assertEquals(2, updated[0].getVersion());
        } finally {
            remove(step);
            remove(dataset);
        }
    }

    public void testShouldSaveNewQASteps() throws Exception {
        EmfDataset dataset = newDataset("dataset-dao-test");

        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setName("name");
        step.setVersion(2);

        try {
            dao.add(new QAStep[] { step }, session);
            session.clear();

            QAStep[] read = dao.steps(dataset, session);
            assertEquals(1, read.length);
            assertEquals("name", read[0].getName());
            assertEquals(2, read[0].getVersion());
            assertEquals(dataset.getId(), read[0].getDatasetId());
        } finally {
            remove(step);
            remove(dataset);
        }
    }

    public void testShouldGetAllDefaultQAPrograms() {
        QAProgram[] programs = dao.getQAPrograms(session);
        assertEquals(4, programs.length);
    }

    public void testShouldCheckExistQAStep() throws EmfException {
        EmfDataset dataset = newDataset("dataset-dao-test");

        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setName("name");
        step.setVersion(2);

        try {
            dao.add(new QAStep[] { step }, session);
            session.clear();

            boolean exist = dao.exists(step, session);
            assertTrue(exist);
        } finally {
            remove(step);
            remove(dataset);
        }

    }

    private EmfDataset newDataset(String name) throws EmfException {
        User owner = userDAO.get("emf", session);

        EmfDataset dataset = new EmfDataset();
        
        String newName = name;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);
            
        dataset.setCreator(owner.getUsername());

        save(dataset);
        return (EmfDataset) load(EmfDataset.class, dataset.getName());
    }

}
