package gov.epa.emissions.framework.services.qa;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class QAServiceTest extends ServiceTestCase {
    private QAService service;

    private UserDAO userDAO;

    public void doSetUp() throws Exception {
        service = new QAServiceImpl(sessionFactory(configFile()));
        userDAO = new UserDAO();
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldGetQASteps() throws Exception {
        EmfDataset dataset = newDataset();

        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setName("name");
        step.setVersion(2);
        add(step);

        try {
            QAStep[] steps = service.getQASteps(dataset);

            assertEquals(1, steps.length);
            assertEquals("name", steps[0].getName());
            assertEquals(2, steps[0].getVersion());
        } finally {
            remove(step);
            remove(dataset);
        }
    }

    public void testShouldUpdateQASteps() throws Exception {
        EmfDataset dataset = newDataset();

        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setName("name");
        step.setVersion(2);
        add(step);

        QAProgram program = program();

        try {
            QAStep[] read = service.getQASteps(dataset);
            assertEquals(1, read.length);

            read[0].setName("updated-name");
            read[0].setProgram(program);

            service.updateWitoutCheckingConstraints(read);

            QAStep[] updated = service.getQASteps(dataset);
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

    private EmfDataset newDataset() {
        User owner = userDAO.get("emf", session);

        EmfDataset dataset = new EmfDataset();
        dataset.setName("dataset-dao-test");
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
}
