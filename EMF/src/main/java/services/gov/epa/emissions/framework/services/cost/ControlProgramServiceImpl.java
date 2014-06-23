package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ControlProgramServiceImpl implements ControlProgramService {

    private static Log LOG = LogFactory.getLog(ControlProgramServiceImpl.class);

    private PooledExecutor threadPool;

    private HibernateSessionFactory sessionFactory;

//    private DbServerFactory dbServerFactory;

    private ControlProgramDAO dao;

    public ControlProgramServiceImpl() throws Exception {
        init(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public ControlProgramServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws Exception {
        init(sessionFactory, dbServerFactory);
    }

    private synchronized void init(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.sessionFactory = sessionFactory;
//        this.dbServerFactory = dbServerFactory;
        dao = new ControlProgramDAO(dbServerFactory, sessionFactory);
        threadPool = createThreadPool();

    }

    protected synchronized void finalize() throws Throwable {
        threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
        threadPool.awaitTerminationAfterShutdown();
        super.finalize();
    }

    private synchronized PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    public synchronized ControlProgram[] getControlPrograms() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List cs = dao.all(session);
            System.err.println(cs.size());
            return (ControlProgram[]) cs.toArray(new ControlProgram[0]);
        } catch (HibernateException e) {
            LOG.error("Could not retrieve all control strategies.");
            throw new EmfException("Could not retrieve all control strategies.");
        } finally {
            session.close();
        }
    }

    public synchronized int addControlProgram(ControlProgram element) throws EmfException {
        Session session = sessionFactory.getSession();
        int csId;
        try {
            csId = dao.add(element, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add Control Program: " + element, e);
            throw new EmfException("Could not add Control Program: " + element);
        } finally {
            session.close();
        }
        return csId;
    }

    public synchronized ControlProgram obtainLocked(User owner, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlProgram locked = dao.obtainLocked(owner, id, session);

            return locked;
        } catch (RuntimeException e) {
            LOG
                    .error("Could not obtain lock for Control Program: id = " + id + " by owner: "
                            + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Control Program: id = " + id + " by owner: "
                    + owner.getUsername());
        } finally {
            session.close();
        }
    }

    public synchronized void releaseLocked(User user, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.releaseLocked(user, id, session);
        } catch (RuntimeException e) {
            LOG.error(
                    "Could not release lock for Control Program id: " + id,
                    e);
            throw new EmfException("Could not release lock for Control Program id: " + id);
        } finally {
            session.close();
        }
    }

    public synchronized ControlProgram updateControlProgram(ControlProgram element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdate(element, session))
                throw new EmfException("The Control Program name is already in use");

            ControlProgram released = dao.update(element, session);

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Program: " + element, e);
            throw new EmfException("Could not update ControlProgram: " + element);
        } finally {
            session.close();
        }
    }

    public synchronized ControlProgram updateControlProgramWithLock(ControlProgram element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdate(element, session))
                throw new EmfException("Control Program name already in use");

            ControlProgram csWithLock = dao.updateWithLock(element, session);

            return csWithLock;
//            return dao.getById(csWithLock.getId(), session);
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Program: " + element, e);
            throw new EmfException("Could not update ControlProgram: " + element);
        } finally {
            session.close();
        }
    }

//    public void removeControlStrategies(ControlProgram[] elements, User user) throws EmfException {
//        try {
//            for (int i = 0; i < elements.length; i++) {
//                if (!user.equals(elements[i].getCreator()))
//                    throw new EmfException("Only the creator of " + elements[i].getName()
//                            + " can remove it from the database.");
//                remove(elements[i]);
//            }
//
//        } catch (RuntimeException e) {
//            LOG.error("Could not update Control Program: " + elements, e);
//            throw new EmfException("Could not update ControlProgram: " + elements);
//        }
//    }

    public synchronized void removeControlPrograms(int[] ids, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        String exception = "";
        try {
            for (int i = 0; i < ids.length; i++) {
                ControlProgram cs = dao.getControlProgram(ids[i], session);
                session.clear();
                
                //check if admin user, then allow it to be removed.
                if (user.equals(cs.getCreator())||user.isAdmin()){
                    if (cs.isLocked())
                        exception += "The control Program, " + cs.getName() + ", is in edit mode and can not be removed. ";
                    else
                        remove(cs);
                }
                else{
                    exception += "Permission denied to the Program: " + cs.getName() + ". ";
                }
            }
            
            if (exception.length() > 0) throw new EmfException(exception);
        } catch (RuntimeException e) {
            LOG.error("Could not remove Control Program", e);
            throw new EmfException("Could not remove ControlProgram");
        } finally {
            session.close();
        }
    }

    private synchronized void remove(ControlProgram element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {

            if (!dao.canUpdate(element, session))
                throw new EmfException("Control Program name already in use");

            dao.remove(element, session);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control program: " + element, e);
            throw new EmfException("Could not remove control program: " + element.getName());
        } catch (SQLException e) {
            LOG.error("Could not remove control program: " + element, e);
            throw new EmfException("Could not remove control program: " + element.getName());
        } finally {
            session.close();
        }
    }
    
    public synchronized void removeResultDatasets(Integer[] ids, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        DatasetDAO dsDao = new DatasetDAO();
        try {
            for (Integer id : ids ) {
                EmfDataset dataset = dsDao.getDataset(session, id);

                if (dataset != null) {
                    try {
                        dsDao.remove(user, dataset, session);
                    } catch (EmfException e) {
                        if (DebugLevels.DEBUG_12())
                            System.out.println(e.getMessage());
                        
                        throw new EmfException(e.getMessage());
                    }
                }
            }
        } finally {
            session.close();
        }
    }
    
    public synchronized ControlProgramType[] getControlProgramTypes() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List st = dao.getControlProgramTypes(session);
            return (ControlProgramType[]) st.toArray(new ControlProgramType[0]);
        } catch (HibernateException e) {
            LOG.error("could not retrieve all control program types. " + e.getMessage());
            throw new EmfException("could not retrieve all control program types. " + e.getMessage());
        } finally {
            session.close();
        }
    }

    //returns control Program Id for the given name
    public synchronized int isDuplicateName(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlProgram cs = dao.getByName(name, session);
            return cs == null ? 0 : cs.getId();
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve if ControlProgram name is already used", e);
            throw new EmfException("Could not retrieve if ControlProgram name is already used");
        } finally {
            session.close();
        }
    }

    public synchronized int copyControlProgram(int id, User creator) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            //get cs to copy
            ControlProgram cs = dao.getControlProgram(id, session);
            
            session.clear();// clear to flush current

            String name = "Copy of " + cs.getName();
            //make sure this won't cause duplicate issues...
            if (isDuplicate(name))
                throw new EmfException("A control Program named '" + name + "' already exists.");

            //do a deep copy
            ControlProgram coppied = (ControlProgram)DeepCopy.copy(cs);
            //change to applicable values
            coppied.setName(name);
            coppied.setCreator(creator);
            coppied.setLastModifiedDate(new Date());
            if (coppied.isLocked()){
                coppied.setLockDate(null);
                coppied.setLockOwner(null);
            }
                       
            dao.add(coppied, session);
            int csId = coppied.getId();
            return csId;
        } catch (EmfException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            LOG.error("Could not copy control Program", e);
            throw new EmfException("Could not copy control Program");
        } catch (Exception e) {
            LOG.error("Could not copy control Program", e);
            throw new EmfException("Could not copy control Program");
        } finally {
            session.close();
        }
    }

    private synchronized boolean isDuplicate(String name) throws EmfException {
        return (isDuplicateName(name) != 0);
    }

    public synchronized ControlProgram getControlProgram(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getControlProgram(id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not get control Program", e);
            throw new EmfException("Could not get control Program");
        } finally {
            session.close();
        }
    }
}
