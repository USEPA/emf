package gov.epa.emissions.framework.services.tempalloc;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.DataServiceImpl.DeleteType;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class TemporalAllocationDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private EntityManagerFactory entityManagerFactory;

    private DbServerFactory dbServerFactory;
    
    private DatasetDAO datasetDao;
    
    public TemporalAllocationDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
        datasetDao = new DatasetDAO();
    }
    
    public TemporalAllocationDAO(DbServerFactory dbServerFactory, EntityManagerFactory entityManagerFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    public int add(TemporalAllocation element, EntityManager entityManager) {
        return addObject(element, entityManager);
    }

    private int addObject(Object obj, EntityManager entityManager) {
        return (Integer)hibernateFacade.add(obj, entityManager);
    }

    public List<TemporalAllocation> all(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<TemporalAllocation> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(TemporalAllocation.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<TemporalAllocation> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public TemporalAllocation obtainLocked(User owner, int id, EntityManager entityManager) {
        return (TemporalAllocation) lockingScheme.getLocked(owner, current(id, entityManager), entityManager);
    }

    public void releaseLocked(User user, int id, EntityManager entityManager) {
        TemporalAllocation current = getTemporalAllocation(id, entityManager);
        lockingScheme.releaseLock(user, current, entityManager);
    }

    public TemporalAllocation updateWithLock(TemporalAllocation locked, EntityManager entityManager) throws EmfException {
        return (TemporalAllocation) lockingScheme.renewLockOnUpdate(locked, current(locked, entityManager), entityManager);
    }

    private TemporalAllocation current(TemporalAllocation temporalAllocation, EntityManager entityManager) {
        return current(temporalAllocation.getId(), entityManager);
    }

    public boolean canUpdate(TemporalAllocation temporalAllocation, EntityManager entityManager) {
        if (!exists(temporalAllocation.getId(), entityManager)) {
            return false;
        }

        TemporalAllocation current = current(temporalAllocation.getId(), entityManager);

        entityManager.clear(); // clear to flush current

        if (current.getName().equals(temporalAllocation.getName()))
            return true;

        return !nameUsed(temporalAllocation.getName(), entityManager);
    }

    public boolean nameUsed(String name, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, TemporalAllocation.class, entityManager);
    }

    private TemporalAllocation current(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, TemporalAllocation.class, entityManager);
    }

    public boolean exists(int id, EntityManager entityManager) {
        return hibernateFacade.exists(id, TemporalAllocation.class, entityManager);
    }

    public TemporalAllocation getById(int id, EntityManager entityManager) {
        TemporalAllocation element = hibernateFacade.load(TemporalAllocation.class, "id", Integer.valueOf(id), entityManager);
        return element;
    }

    public TemporalAllocation getByName(String name, EntityManager entityManager) {
        TemporalAllocation element = hibernateFacade.load(TemporalAllocation.class, "name", new String(name), entityManager);
        return element;
    }

    public TemporalAllocation getTemporalAllocation(int id, EntityManager entityManager) {
        return hibernateFacade.load(TemporalAllocation.class, "id", Integer.valueOf(id), entityManager);
    }
    
    public List<TemporalAllocationResolution> allResolutions(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<TemporalAllocationResolution> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(TemporalAllocationResolution.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<TemporalAllocationResolution> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public Long getTemporalAllocationRunningCount(EntityManager entityManager) {
        return (Long)entityManager.createQuery("SELECT COUNT(*) FROM TemporalAllocation WHERE runStatus = 'Running'").getSingleResult();
    }
    
    public List<TemporalAllocation> getTemporalAllocationsByRunStatus(String runStatus, EntityManager entityManager) {
      return entityManager.createQuery("SELECT new TemporalAllocation(ta.id, ta.name) FROM TemporalAllocation ta WHERE ta.runStatus = :runStatus order by ta.lastModifiedDate").setParameter("runStatus", runStatus).getResultList();
    }
    
    public String getTemporalAllocationRunStatus(int id, EntityManager entityManager) {
        return (String)entityManager.createQuery("SELECT runStatus FROM TemporalAllocation WHERE id = " + id).getSingleResult();
    }
    
    public void updateTemporalAllocationOutput(TemporalAllocationOutput output, EntityManager entityManager) {
        hibernateFacade.saveOrUpdate(output, entityManager);
    }
    
    public TemporalAllocationOutputType getTemporalAllocationOutputType(String name, EntityManager entityManager) {
        return hibernateFacade.load(TemporalAllocationOutputType.class, "name", name, entityManager);
    }
    
    public List<TemporalAllocationOutput> getTemporalAllocationOutputs(int temporalAllocationId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<TemporalAllocationOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(TemporalAllocationOutput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<TemporalAllocationOutput> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade
                .get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("temporalAllocationId"), Integer.valueOf(temporalAllocationId)) }, entityManager);
    }
    
    public EmfDataset[] getTemporalAllocationOutputDatasets(int temporalAllocationId, EntityManager entityManager) {
        List<TemporalAllocationOutput> outputs = getTemporalAllocationOutputs(temporalAllocationId, entityManager);
        List<EmfDataset> datasets = new ArrayList<EmfDataset>();
        if (outputs != null) {
            for (TemporalAllocationOutput output : outputs) {
                if (output.getOutputDataset() != null) {
                    datasets.add(output.getOutputDataset());
                }
            }
        }
        if (datasets.size() > 0) {
            return datasets.toArray(new EmfDataset[0]);
        }
        return null;
    }
    
    public void removeOutputs(int temporalAllocationId, EntityManager entityManager) {
        List<TemporalAllocationOutput> outputs = getTemporalAllocationOutputs(temporalAllocationId, entityManager);
        hibernateFacade.remove(outputs.toArray(), entityManager);
    }

    public void removeOutputDatasets(EmfDataset[] datasets, User user, EntityManager entityManager, DbServer dbServer) throws EmfException {
        if (datasets != null) {
            try {
                deleteDatasets(datasets, user, entityManager);
                datasetDao.deleteDatasets(datasets, dbServer, entityManager);
            } catch (EmfException e) {
                throw new EmfException(e.getMessage());
            }
        }
    }
    
    public void deleteDatasets(EmfDataset[] datasets, User user, EntityManager entityManager) throws EmfException {
        EmfDataset[] lockedDatasets = getLockedDatasets(datasets, user, entityManager);
        
        if (lockedDatasets == null)
            return;
        
        try {
            new DataServiceImpl(dbServerFactory, entityManagerFactory).deleteDatasets(user, lockedDatasets, DeleteType.TEMPORAL_ALLOCATION);
        } catch (EmfException e) {
            if (!e.getType().equals(EmfException.MSG_TYPE))
                throw new EmfException(e.getMessage());
        } finally {
            releaseLocked(lockedDatasets, user, entityManager);
        }
    }
    
    private EmfDataset[] getLockedDatasets(EmfDataset[] datasets, User user, EntityManager entityManager) {
        List lockedList = new ArrayList();
        
        for (int i = 0; i < datasets.length; i++) {
            EmfDataset locked = obtainLockedDataset(datasets[i], user, entityManager);
            if (locked == null) {
                releaseLocked((EmfDataset[])lockedList.toArray(new EmfDataset[0]), user, entityManager);
                return null;
            }
            
            lockedList.add(locked);
        }
        
        return (EmfDataset[])lockedList.toArray(new EmfDataset[0]);
    }

    private EmfDataset obtainLockedDataset(EmfDataset dataset, User user, EntityManager entityManager) {
        EmfDataset locked = datasetDao.obtainLocked(user, dataset, entityManager);
        return locked;
    }
    
    private void releaseLocked(EmfDataset[] lockedDatasets, User user, EntityManager entityManager) {
        if (lockedDatasets.length == 0)
            return;
        
        for(int i = 0; i < lockedDatasets.length; i++)
            datasetDao.releaseLocked(user, lockedDatasets[i], entityManager);
    }
    
    public void remove(TemporalAllocation element, EntityManager entityManager) {
        hibernateFacade.remove(element, entityManager);
    }
    
    public void remove(TemporalAllocationOutput output, EntityManager entityManager) {
        hibernateFacade.remove(output, entityManager);
    }

    public void setRunStatusAndCompletionDate(TemporalAllocation element, String runStatus, Date completionDate, EntityManager entityManager) {
        hibernateFacade.executeInsideTransaction(em -> {
            entityManager.createQuery("update TemporalAllocation set runStatus = :status, lastModifiedDate = :date, completionDate = :completionDate where id = :id")
                .setParameter("status", runStatus)
                .setParameter("date", new Date())
                .setParameter("completionDate", completionDate)
                .setParameter("id", element.getId())
                .executeUpdate();
        }, entityManager);
    }
}
