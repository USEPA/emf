package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.basic.SearchDAOUtility;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class ControlProgramDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;
    
    public ControlProgramDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public ControlProgramDAO(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
    }

    public int add(ControlProgram element, Session session) {
        return addObject(element, session);
    }

    private int addObject(Object obj, Session session) {
        return (Integer)hibernateFacade.add(obj, session);
    }

    public List<ControlProgram> all(Session session) {
        CriteriaBuilderQueryRoot<ControlProgram> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ControlProgram.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ControlProgram> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List getControlPrograms(Session session, BasicSearchFilter searchFilter) {
        String hql = "select distinct cp " +
                "from ControlProgram as cp " +
                "left join cp.dataset as dataset " +
                "left join cp.controlProgramType as cpt ";
        //
        if (StringUtils.isNotBlank(searchFilter.getFieldName())
                && StringUtils.isNotBlank(searchFilter.getFieldValue())) {
            String whereClause = SearchDAOUtility.buildSearchCriterion(new ControlStrategyProgramFilter(), searchFilter);
            if (StringUtils.isNotBlank(whereClause))
                hql += " where " + whereClause;
        }
        return session.createQuery(hql).list();
    }

    public List<ControlProgramType> getControlProgramTypes(Session session) {
        CriteriaBuilderQueryRoot<ControlProgramType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ControlProgramType.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ControlProgramType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public ControlProgram obtainLocked(User owner, int id, Session session) {
        return (ControlProgram) lockingScheme.getLocked(owner, current(id, session), session);
    }

    public void releaseLocked(User user, int id, Session session) {
        ControlProgram current = getControlProgram(id, session);
        lockingScheme.releaseLock(user, current, session);
    }

    public ControlProgram update(ControlProgram locked, Session session) throws EmfException {
        return (ControlProgram) lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
    }

    public ControlProgram updateWithLock(ControlProgram locked, Session session) throws EmfException {
        return (ControlProgram) lockingScheme.renewLockOnUpdate(locked, current(locked, session), session);
    }

    private ControlProgram current(ControlProgram controlProgram, Session session) {
        return current(controlProgram.getId(), session);
    }

    public boolean canUpdate(ControlProgram controlProgram, Session session) {
        if (!exists(controlProgram.getId(), session)) {
            return false;
        }

        ControlProgram current = current(controlProgram.getId(), session);

        session.clear();// clear to flush current

        if (current.getName().equals(controlProgram.getName()))
            return true;

        return !nameUsed(controlProgram.getName(), session);
    }

    public boolean nameUsed(String name, Session session) {
        return hibernateFacade.nameUsed(name, ControlProgram.class, session);
    }

    private ControlProgram current(int id, Session session) {
        return hibernateFacade.current(id, ControlProgram.class, session);
    }

    public boolean exists(int id, Session session) {
        return hibernateFacade.exists(id, ControlProgram.class, session);
    }

    public void remove(ControlProgram controlProgram, Session session) throws EmfException, SQLException {
        //see if control strategy is using the program, if so throw an error...
        DbServer dbServer = this.dbServerFactory.getDbServer();
        Datasource datasource = dbServer.getEmissionsDatasource();
        Connection connection = datasource.getConnection();
        
        String sqlString =  "select cS.name from emf.control_strategies as cS,  " +
        		"emf.control_strategy_programs as csP where csP.control_strategy_id = cS.id " +
        		"and csP.control_program_id = " + controlProgram.getId() + ";";
        //        List list = session.createQuery("select cS.name " +
        //                "from ControlStrategy as cS where cS.id in " +
        //                "(select EDT.id from ControlProgram as cP " +
        //                "inner join cS.controlPrograms as EDT where cP.id = "+controlProgram.getId() + ")").list();
        Statement statement;
        ResultSet resultSet = null;

        statement = connection.createStatement();
        resultSet = statement.executeQuery(sqlString);

        if ( resultSet != null && resultSet.next() )
            throw new EmfException("Error: dataset used by control strategy: " + resultSet.getString("name"));
        //        resultSet.close();
        hibernateFacade.remove(controlProgram, session);        
        resultSet.close();
}

    public ControlProgram getByName(String name, Session session) {
        ControlProgram cs = hibernateFacade.load(ControlProgram.class, "name", new String(name), session);
        return cs;
    }

    public ControlProgram getControlProgram(int id, Session session) {
        ControlProgram cs = hibernateFacade.load(ControlProgram.class, "id", Integer.valueOf(id), session);
        return cs;
    }

    public List<ControlProgram> getControlProgramsByControlMeasures(int[] cmIds, Session session) {
        List<ControlProgram> list = new ArrayList<ControlProgram>();
        String idList = "";
        for (int i = 0; i < cmIds.length; ++i) {
            idList += (i > 0 ? ","  : "") + cmIds[i];
        }
        try {
            Query query = session.createQuery("select distinct cp "
                    + "FROM ControlProgram AS cp "
                    + (cmIds != null && cmIds.length > 0 
                            ? "inner join cp.controlMeasures AS cpm "
                               + "WHERE cpm.id in (" + idList + ") " 
                            : "")
                    + "order by cp.name");
            query.setCacheable(true);
            list = query.list();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }
    
    public void updateControlProgram(int controlProgramId, String msg, Session session, int[] measureIdsToDelete) throws EmfException {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.createQuery("update ControlProgram set description =  '' || "
                        + "description || '\n------\n' || :msg, lastModifiedDate = :date where id = :id")
            .setParameter("msg", msg)
            .setParameter("date", new Date())
            .setParameter("id", Integer.valueOf(controlProgramId))
            .executeUpdate();
            tx.commit();
            session.clear();
            
            //also need to purge measures that are being deleted...this is needed to keep hibernate list_index in synch...
            ControlProgram cs = getControlProgram(controlProgramId, session);
            List<ControlMeasure> measures = new ArrayList<ControlMeasure>();
            measures.addAll(Arrays.asList(cs.getControlMeasures()));
            for (ControlMeasure m : cs.getControlMeasures()) {
                for (int id : measureIdsToDelete) {
                    if (id == m.getId()) {
                        measures.remove(m);
                    }
                }
            }
            cs.setControlMeasures(measures.toArray(new ControlMeasure[0]));
            updateWithLock(cs, session);

            
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            throw e;
        }
    }
}
