package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.List;

public class ControlStrategyDAOTest extends ServiceTestCase {

    private ControlStrategyDAO dao;

    protected void doSetUp() throws Exception {
        dao = new ControlStrategyDAO();
    }

    protected void doTearDown() throws Exception {
        // no op
    }

    public void testShouldPersistEmptyControlStrategyOnAdd() {
        int totalBeforeAdd = dao.all(session).size();

        ControlStrategy element = newControlStrategy();
        session.clear();
        try {
            List list = dao.all(session);
            assertEquals(totalBeforeAdd + 1, list.size());
        } finally {
            remove(element);
        }
    }

//    public void testTest() {
//        List list = dao.test(session);
//        System.err.println(list.size());
//    }
//
    private ControlStrategy newControlStrategy() {
        ControlStrategy element = new ControlStrategy("test" + Math.random());
        element.setRunStatus("Running");
        int id = dao.add(element, session);
        element.setId(id);
        return element;
    }

    public void testShouldUpdateControlStrategyOnUpdate() throws EmfException {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);

        ControlStrategy element = newControlStrategy();
        session.clear();
        
        EmfDataset dataset = dataset("detailed dataset");
        try {
            ControlStrategy locked = dao.obtainLocked(owner, element.getId(), session);
            
            assertEquals(locked.getLockOwner(), owner.getUsername());
            locked.setName("TEST");
            locked.setFilter("WHERE SCC=20210000");
            
            session.clear();
            ControlStrategy modified = dao.update(locked, session);
            
            assertEquals("TEST", locked.getName());
            assertEquals("WHERE SCC=20210000",locked.getFilter());
            assertEquals(modified.getLockOwner(), null);
            
        } finally {
            remove(element);
            remove(dataset);
        }
    }    private EmfDataset dataset(String name) throws EmfException {
        User owner = new UserDAO().get("emf", session);

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

    public void testShouldRemoveControlStrategy() {
        int totalBeforeAdd = dao.all(session).size();
        ControlStrategy element = newControlStrategy();
        session.clear();
        int totalAfterAdd = dao.all(session).size();

        assertEquals(totalAfterAdd, totalBeforeAdd + 1);

        try {
            dao.remove(element, session);
            int totalAfterRemove = dao.all(session).size();
            assertEquals(totalBeforeAdd, totalAfterRemove);
        } catch (Exception e) {
            remove(element);
        }
    }


    public void testShouldGetControlStrategyRunStatus() {
        ControlStrategy element = newControlStrategy();
        session.clear();
        try {
            String runStatus = dao.getControlStrategyRunStatus(element.getId(), session);
            assertEquals("Running", runStatus);
        } finally {
            remove(element);
        }
    }

    public void testShouldGetControlStrategyRunStatus3() {
        try {
            List awasd = new DatasetDAO().getDatasets(session, 1);
            assertEquals(0, awasd.size());
        } finally {
            //
        }
    }

    public void testShouldGetControlStrategiesByRunStatus() {
        ControlStrategy element = newControlStrategy();
        session.flush();
        session.clear();
        ControlStrategy element2 = newControlStrategy();
        session.flush();
        session.clear();
        ControlStrategy element3 = newControlStrategy();
        session.flush();
        session.clear();
        List all = dao.getControlStrategiesByRunStatus("Running", session);
        dao.setControlStrategyRunStatus(element2.getId(), "Waiting", null, session);
        session.flush();
        session.clear();
       try {
            all = dao.getControlStrategiesByRunStatus("Running", session);
            
            assertEquals(all.size(), 2);
        } finally {
            remove(element);
            remove(element2);
            remove(element3);
        }
    }


    public void testShouldGetControlStrategiesRunningCount() {
        long count = dao.getControlStrategyRunningCount(session);
       try {
            
            assertEquals(count, 0);
        } finally {
            //
        }
    }
}
