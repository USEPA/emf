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
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;

public class ControlProgramDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private EntityManagerFactory entityManagerFactory;

    private DbServerFactory dbServerFactory;
    
    public ControlProgramDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public ControlProgramDAO(DbServerFactory dbServerFactory, EntityManagerFactory entityManagerFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    public int add(ControlProgram element, EntityManager entityManager) {
        return hibernateFacade.add(element, entityManager);
    }

    public List<ControlProgram> all(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ControlProgram> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ControlProgram.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ControlProgram> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List getControlPrograms(EntityManager entityManager, BasicSearchFilter searchFilter) {
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
        return entityManager.createQuery(hql).getResultList();
    }

    public List<ControlProgramType> getControlProgramTypes(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ControlProgramType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ControlProgramType.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ControlProgramType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public ControlProgram obtainLocked(User owner, int id, EntityManager entityManager) {
        return (ControlProgram) lockingScheme.getLocked(owner, current(id, entityManager), entityManager);
    }

    public void releaseLocked(User user, int id, EntityManager entityManager) {
        ControlProgram current = getControlProgram(id, entityManager);
        lockingScheme.releaseLock(user, current, entityManager);
    }

    public ControlProgram update(ControlProgram locked, EntityManager entityManager) throws EmfException {
        return (ControlProgram) lockingScheme.releaseLockOnUpdate(locked, current(locked, entityManager), entityManager);
    }

    public ControlProgram updateWithLock(ControlProgram locked, EntityManager entityManager) throws EmfException {
        return (ControlProgram) lockingScheme.renewLockOnUpdate(locked, current(locked, entityManager), entityManager);
    }

    private ControlProgram current(ControlProgram controlProgram, EntityManager entityManager) {
        return current(controlProgram.getId(), entityManager);
    }

    public boolean canUpdate(ControlProgram controlProgram, EntityManager entityManager) {
        if (!exists(controlProgram.getId(), entityManager)) {
            return false;
        }

        ControlProgram current = current(controlProgram.getId(), entityManager);

        entityManager.clear();// clear to flush current

        if (current.getName().equals(controlProgram.getName()))
            return true;

        return !nameUsed(controlProgram.getName(), entityManager);
    }

    public boolean nameUsed(String name, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, ControlProgram.class, entityManager);
    }

    private ControlProgram current(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, ControlProgram.class, entityManager);
    }

    public boolean exists(int id, EntityManager entityManager) {
        return hibernateFacade.exists(id, ControlProgram.class, entityManager);
    }

    public void remove(ControlProgram controlProgram, EntityManager entityManager) throws EmfException, SQLException {
        //see if control strategy is using the program, if so throw an error...
        DbServer dbServer = this.dbServerFactory.getDbServer();
        Datasource datasource = dbServer.getEmissionsDatasource();
        Connection connection = datasource.getConnection();
        
        String sqlString =  "select cS.name from emf.control_strategies as cS,  " +
        		"emf.control_strategy_programs as csP where csP.control_strategy_id = cS.id " +
        		"and csP.control_program_id = " + controlProgram.getId() + ";";
        //        List list = entityManager.createQuery("select cS.name " +
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
        hibernateFacade.remove(controlProgram, entityManager);        
        resultSet.close();
}

    public ControlProgram getByName(String name, EntityManager entityManager) {
        ControlProgram cs = hibernateFacade.load(ControlProgram.class, "name", new String(name), entityManager);
        return cs;
    }

    public ControlProgram getControlProgram(int id, EntityManager entityManager) {
        ControlProgram cs = hibernateFacade.load(ControlProgram.class, "id", Integer.valueOf(id), entityManager);
        return cs;
    }

    public List<ControlProgram> getControlProgramsByControlMeasures(int[] cmIds, EntityManager entityManager) {
        List<ControlProgram> list = new ArrayList<ControlProgram>();
        String idList = "";
        for (int i = 0; i < cmIds.length; ++i) {
            idList += (i > 0 ? ","  : "") + cmIds[i];
        }
        try {
            list = entityManager.createQuery("select distinct cp "
                    + "FROM ControlProgram AS cp "
                    + (cmIds != null && cmIds.length > 0 
                            ? "inner join cp.controlMeasures AS cpm "
                               + "WHERE cpm.id in (" + idList + ") " 
                            : "")
                    + "order by cp.name").getResultList();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }
    
    public void updateControlProgram(int controlProgramId, String msg, EntityManager entityManager, int[] measureIdsToDelete) throws EmfException {
        try {

            hibernateFacade.executeInsideTransaction(em -> {
                entityManager.createQuery("update ControlProgram set description =  '' || "
                        + "description || '\n------\n' || :msg, lastModifiedDate = :date where id = :id")
                    .setParameter("msg", msg)
                    .setParameter("date", new Date())
                    .setParameter("id", Integer.valueOf(controlProgramId))
                    .executeUpdate();
            }, entityManager);

            entityManager.clear();
            
            //also need to purge measures that are being deleted...this is needed to keep hibernate list_index in synch...
            ControlProgram cs = getControlProgram(controlProgramId, entityManager);
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
            updateWithLock(cs, entityManager);

            
        } catch (HibernateException e) {
            throw e;
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            throw e;
        }
    }
}
