package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.CoSTConstants;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class ControlMeasureDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private ReferencesDAO referencesDAO;
    
//    private DbServer dbServer;
    
    public ControlMeasureDAO() {

        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
        this.referencesDAO = new ReferencesDAO();
    }

//    public ControlMeasureDAO(DbServer dbServer) {
//        lockingScheme = new LockingScheme();
//        hibernateFacade = new HibernateFacade();
//        this.dbServer = dbServer;
//    }

    public boolean exists(int id, EntityManager entityManager) {
        return hibernateFacade.exists(id, ControlMeasure.class, entityManager);
    }

    public <C> boolean exists(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate[] predicates, EntityManager entityManager) {
        return hibernateFacade.exists(criteriaBuilderQueryRoot, predicates, entityManager);
    }

    public ControlMeasure current(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, ControlMeasure.class, entityManager);
    }

    public boolean canUpdate(ControlMeasure measure, EntityManager entityManager) {
        if (!exists(measure.getId(), entityManager)) {
            return false;
        }

        ControlMeasure current = current(measure.getId(), entityManager);
        entityManager.clear();// clear to flush current
        if (current.getName().equals(measure.getName()))
            return true;

        return !nameUsed(measure.getName(), entityManager);
    }

    /*
     * Return true if the name is already used
     */
    private boolean nameUsed(String name, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, ControlMeasure.class, entityManager);
    }

    public boolean exists(String name, EntityManager entityManager) {
        return hibernateFacade.exists(name, ControlMeasure.class, entityManager);
    }

    public List all(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ControlMeasure> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ControlMeasure.class, entityManager);
        return hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);
    }

    public List getControlMeasures(Pollutant poll, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ControlMeasure> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ControlMeasure.class, entityManager);
        return hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("majorPollutant"), poll));
    }

    // NOTE: it't not happening in one transaction. modify?
    //No, there is situations where there is too much data and transaction would cause to much overhead
    public int add(ControlMeasure measure, Scc[] sccs, EntityManager entityManager) throws EmfException {
        //Validate control measure
        //make sure abbreviation is not 
        if (measure.getAbbreviation().trim().length() == 0) {
            throw new EmfException("An abbreviation must be specified");
        }
        //make sure its not longer than 10 characters
        if (measure.getAbbreviation().trim().length() > CoSTConstants.CM_ABBREV_LEN) { //10) { // JIZHEN20110727
            throw new EmfException("An abbreviation must not be longer than " + CoSTConstants.CM_ABBREV_LEN + " characters");
        }
        //make sure its does not contain a space
        if (measure.getAbbreviation().trim().indexOf(" ") > 0) {
            throw new EmfException("An abbreviation can not contain a space");
        }

        checkForConstraints(measure, entityManager);
        updateReferenceIds(measure, entityManager);
        int cmId = hibernateFacade.add(measure, entityManager);
//        int cmId = controlMeasureIds(measure, sccs, entityManager);
        for (int i = 0; i < sccs.length; i++) {
            sccs[i].setControlMeasureId(cmId);
        }
        hibernateFacade.add(sccs, entityManager);
        return cmId;
    }

    private int controlMeasureIds(ControlMeasure measure, Scc[] sccs, EntityManager entityManager) {
        ControlMeasure cm = hibernateFacade.load(ControlMeasure.class, "name", measure.getName(), entityManager);
        int cmId = cm.getId();
        for (int i = 0; i < sccs.length; i++) {
            sccs[i].setControlMeasureId(cmId);
        }
        return cmId;
    }

    public void remove(int controlMeasureId, EntityManager entityManager) {
        removeSccs(controlMeasureId, entityManager);
        hibernateFacade.remove(current(controlMeasureId, entityManager), entityManager);
    }

    public void remove(int[] sectorIds, EntityManagerFactory entityManagerFactory, DbServer dbServer) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        final EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            removeSccs(sectorIds, entityManager);
            
            AggregateEfficiencyRecordDAO aerDAO = new AggregateEfficiencyRecordDAO();
            aerDAO.removeAggregateEfficiencyRecords(sectorIds, dbServer);
            
            removeEfficiencyRecords(sectorIds, entityManager);
            
            List<ControlMeasure> lstCM = getControlMeasureBySectors(sectorIds, true, entityManager);
            int [] cmIDs = new int[lstCM.size()];
            for ( int i=0; i<lstCM.size(); i++) {
                cmIDs[i] = lstCM.get(i).getId();
            }
            removeStrategyMeasures(cmIDs, dbServer);
            removeProgramMeasures(cmIDs, dbServer);
            
            String idList = "";
            for (int i = 0; i < sectorIds.length; ++i) {
                idList += (i > 0 ? ","  : "") + sectorIds[i];
            }
            
            String hqlSelect = "select icm "
                    + "FROM ControlMeasure AS icm "
                    + (sectorIds != null && sectorIds.length > 0 
                            ? "inner join icm.sectors AS s "
                              + "WHERE s.id in (" + idList + ") " 
                            : "");
            List<ControlMeasure> controlMeasures  = entityManager.createQuery( hqlSelect ).getResultList();
            for (ControlMeasure controlMeasure : controlMeasures) {
                entityManager.remove(controlMeasure);
            }
            
//            entityManager.createSQLQuery("").executeUpdate();
            tx.commit(); 
        }
        catch (RuntimeException e) {
            tx.rollback();
            throw new EmfException(e.getMessage());
        } finally {
            entityManager.close();
        }

        //      LOG.error("remove EfficiencyRecords");
      //removeEfficiencyRecords(cmId, entityManager);
        
//        AggregateEfficiencyRecordDAO aerDAO = new AggregateEfficiencyRecordDAO();
//        aerDAO.removeAggregateEfficiencyRecords(sectorIds, dbServer);

//        entityManager = entityManagerFactory.createEntityManager();
//        try {
//            Transaction tx = entityManager.beginTransaction();
//            removeEfficiencyRecords(sectorIds, entityManager);
//            tx.commit();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            entityManager.close();
//        }
        
        
//        String idList = "";
//        for (int i = 0; i < sectorIds.length; ++i) {
//            idList += (i > 0 ? ","  : "") + sectorIds[i];
//        }
//        
//        String hqlSelect = "select icm "
//                + "FROM ControlMeasure AS icm "
//                + (sectorIds != null && sectorIds.length > 0 
//                        ? "inner join icm.sectors AS s "
//                          + "WHERE s.id in (" + idList + ") " 
//                        : "");
//        entityManager = entityManagerFactory.createEntityManager();
//        try {
//            List<ControlMeasure> controlMeasures  = entityManager.createQuery( hqlSelect ).list();
//            Transaction tx = entityManager.beginTransaction();
//            for (ControlMeasure controlMeasure : controlMeasures) {
//                entityManager.delete(controlMeasure);
//            }
//            tx.commit();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            entityManager.close();
//        }

//doesn;t work, need to load full object then call entityManager.delete(ControlMeasure);
//      String hqlDelete = "delete ControlMeasure cm where cm.id IN (select icm.id "
//          + "FROM ControlMeasure AS icm "
//          + (sectorIds != null && sectorIds.length > 0 
//                  ? "inner join icm.sectors AS s "
//                    + "WHERE s.id in (" + idList + ") " 
//                  : "") + ")";
//        entityManager.createQuery( hqlDelete ).executeUpdate();

        
//        entityManager.flush();
    }

    public int copy(int controlMeasureId, User creator, EntityManager entityManager, DbServer dbServer) throws EmfException {
        ControlMeasure cm = current(controlMeasureId, entityManager);
        entityManager.clear();//must do this
        
        //set the name and give a random abbrev...
        cm.setName("Copy of " + cm.getName() + " " + creator.getName() + " " + CustomDateFormat.format_HHMM(new Date()));
        cm.setAbbreviation(CustomDateFormat.format_YYDDHHMMSS(new Date()));
        
        CriteriaBuilderQueryRoot<ControlMeasure> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ControlMeasure.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ControlMeasure> root = criteriaBuilderQueryRoot.getRoot();

        //make sure the name and abbrev are unique
//        Criterion name = Restrictions.eq("name", cm.getName());
        Predicate abbrev = builder.equal(root.get("abbreviation"), cm.getAbbreviation());
        boolean abbrExist = abbrExist(criteriaBuilderQueryRoot, new Predicate[] { abbrev }, entityManager);
        boolean nameExist = nameExist(cm.getName(), entityManager);

        if (abbrExist || nameExist)
            throw new EmfException("A control measure with the same name or abbreviation already exists.");

        //default appropriate values
        cm.setCreator(creator);
        cm.setLastModifiedTime(new Date());
        cm.setLastModifiedBy(creator.getName());

        //create new measure, get its id
        int cmId = hibernateFacade.add(cm, entityManager);

        //copy measure SCCs
        Scc[] sccs = getSccs(controlMeasureId, entityManager);
        entityManager.clear();//must do this
        updateSccsControlMeasureIds(sccs, cmId);
        hibernateFacade.add(sccs, entityManager);
        
        //copy measure Efficiecny Records
        EfficiencyRecord[] records = (EfficiencyRecord[]) getEfficiencyRecords(controlMeasureId, entityManager).toArray(new EfficiencyRecord[0]);
        entityManager.clear();//must do this
        updateEfficiencyRecordControlMeasureIds(records, cmId);
        hibernateFacade.add(records, entityManager);
        
        //populate aggregate Efficiecny Records
        updateAggregateEfficiencyRecords(cmId, dbServer);

        return cmId;
    }

    private void removeSccs(int controlMeasureId, EntityManager entityManager) {
        String hqlDelete = "delete Scc scc where scc.controlMeasureId = :controlMeasureId";

        final EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            entityManager.createQuery( hqlDelete )
                .setParameter("controlMeasureId", controlMeasureId)
                .executeUpdate();
            tx.commit(); 
        }
        catch (RuntimeException e) {
            tx.rollback();
            throw e;
        } finally {
            //
        }
//        Scc[] sccs = getSccs(controlMeasureId, entityManager);
//        for (int i = 0; i < sccs.length; i++) {
//            hibernateFacade.remove(sccs[i], entityManager);
//        }
    }

    private void removeSccs(int[] sectorIds, EntityManager entityManager) {
        
        String idList = "";
        for (int i = 0; i < sectorIds.length; ++i) {
            idList += (i > 0 ? ","  : "") + sectorIds[i];
        }

        String hqlDelete = "delete Scc scc where scc.controlMeasureId  IN (select cm.id "
                + "FROM ControlMeasure AS cm "
                + (sectorIds != null && sectorIds.length > 0 
                        ? "inner join cm.sectors AS s "
                          + "WHERE s.id in (" + idList + ") " 
                        : "") + ")";
        try {
            entityManager.createQuery( hqlDelete ).executeUpdate();
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public ControlMeasure grabLocked(User user, int controlMeasureId, EntityManager entityManager) {
        ControlMeasure controlMeasure = current(controlMeasureId, entityManager);
        lockingScheme.grabLock(user, controlMeasure, entityManager);
        return controlMeasure;
    }

    public ControlMeasure obtainLocked(User user, int controlMeasureId, EntityManager entityManager) {
        return (ControlMeasure) lockingScheme.getLocked(user, current(controlMeasureId, entityManager), entityManager);
    }

//    private ControlMeasure current(ControlMeasure measure, EntityManager entityManager) {
//        return current(measure.getId(), ControlMeasure.class, entityManager);
//    }

    public void releaseLocked(User user, int controlMeasureId, EntityManager entityManager) {
        ControlMeasure cm = current(controlMeasureId, entityManager);
        lockingScheme.releaseLock(user, cm, entityManager);
    }
    
    public ControlMeasure update(ControlMeasure locked, Scc[] sccs, EntityManager entityManager) throws EmfException {
        return update(locked, sccs, entityManager, true);
    }

    public ControlMeasure update(ControlMeasure locked, Scc[] sccs, EntityManager entityManager, boolean releaseLock) throws EmfException {
        // Validate control measure
        // make sure abbreviation is not
        if (locked.getAbbreviation().trim().length() == 0) {
            throw new EmfException("An abbreviation must be specified");
        }
        // make sure its not longer than 10 characters
        if (locked.getAbbreviation().trim().length() > CoSTConstants.CM_ABBREV_LEN) { //10) {
            throw new EmfException("An abbreviation must not be longer than " + CoSTConstants.CM_ABBREV_LEN + " characters");
        }
        // make sure its does not contain a space
        if (locked.getAbbreviation().trim().indexOf(" ") > 0) {
            throw new EmfException("An abbreviation can not contain a space");
        }

        checkForConstraints(locked, entityManager);
        updateReferenceIds(locked, entityManager);
        
        ControlMeasure updatedControlMeasure = null;
        if (releaseLock) {
            updatedControlMeasure = (ControlMeasure) lockingScheme.releaseLockOnUpdate(locked, current(locked.getId(),
                    entityManager), entityManager);
        } else {
            updatedControlMeasure = (ControlMeasure) lockingScheme.renewLockOnUpdate(locked, current(locked.getId(),
                    entityManager), entityManager);
        }
        
        updateSccs(sccs, locked.getId(), entityManager);
        return updatedControlMeasure;
    }

    /**
     * Checks for references with the same description in the db. If one is found, the object reference is updated to
     * the existing one.
     */
    private void updateReferenceIds(ControlMeasure controlMeasure, EntityManager entityManager) {

        Reference[] newReferences = controlMeasure.getReferences();

        List<Reference> existingReferences = this.referencesDAO.getReferences(entityManager);

        for (int i = 0; i < newReferences.length; i++) {

            Reference newReference = newReferences[i];
            if (newReference != null/* && newReference.isUpdated()*/) {

                for (Reference existingReference : existingReferences) {

                    if (existingReference.getDescription().equalsIgnoreCase(newReference.getDescription())) {

                        newReferences[i] = existingReference;
                        break;
                    }
                }
            }
        }
    }

    private void updateSccs(Scc[] sccs, int controlMeasureId, EntityManager entityManager) {
        updateSccsControlMeasureIds(sccs, controlMeasureId);
        Scc[] existingSccs = getSccs(controlMeasureId, entityManager);
        List removeList = new ArrayList(Arrays.asList(existingSccs));
        List newSccList = new ArrayList();
        processSccsList(sccs, newSccList, removeList);

        hibernateFacade.remove(removeList.toArray(new Scc[0]), entityManager);
        hibernateFacade.add(newSccList.toArray(new Scc[0]), entityManager);

    }

    // initally all existing sccs in the removeScc list
    private void processSccsList(Scc[] sccsFromClient, List newSccsList, List removeSccs) {
        for (int i = 0; i < sccsFromClient.length; i++) {
            int index = removeSccs.indexOf(sccsFromClient[i]);
            if (index != -1) {
                removeSccs.remove(index);
            } else {
                newSccsList.add(sccsFromClient[i]);
            }
        }
    }

    private void updateSccsControlMeasureIds(Scc[] sccs, int controlMeasureId) {
        for (int i = 0; i < sccs.length; i++) {
            sccs[i].setControlMeasureId(controlMeasureId);
        }
    }

    private void updateEfficiencyRecordControlMeasureIds(EfficiencyRecord[] records, int controlMeasureId) {
        for (int i = 0; i < records.length; i++) {
            records[i].setControlMeasureId(controlMeasureId);
        }
    }

    public Scc[] getSccsWithDescriptions(int controlMeasureId, DbServer dbServer) throws EmfException {
        try {
            RetrieveSCC retrieveSCC = new RetrieveSCC(controlMeasureId, dbServer);
            return retrieveSCC.sccs();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public Scc[] getSccs(int controlMeasureId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Scc> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Scc.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Scc> root = criteriaBuilderQueryRoot.getRoot();

        Predicate id = builder.equal(root.get("controlMeasureId"), Integer.valueOf(controlMeasureId));
        List<Scc> list = hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { id }, entityManager);
        return list.toArray(new Scc[0]);
    }

    public String[] getCMAbbrevAndSccs(int measureId, DbServer dbServer) throws EmfException {
        try {
            RetrieveSCC retrieveSCC = new RetrieveSCC(measureId, dbServer);
            return retrieveSCC.cmAbbrevAndSccs();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public void checkForConstraints(ControlMeasure controlMeasure, EntityManager entityManager) throws EmfException {
        CriteriaBuilderQueryRoot<ControlMeasure> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ControlMeasure.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ControlMeasure> root = criteriaBuilderQueryRoot.getRoot();

        Predicate id = builder.notEqual(root.get("id"), controlMeasure.getId());
        Predicate name = builder.equal(root.get("name"), controlMeasure.getName());
        Predicate abbrev = builder.equal(root.get("abbreviation"), controlMeasure.getAbbreviation());

//        if (nameExist(new Criterion[] { id, name }, entityManager))
//            throw new EmfException("The Control Measure name is already in use: " + controlMeasure.getName());

        if (abbrExist(criteriaBuilderQueryRoot, new Predicate[] { id, abbrev }, entityManager))
            throw new EmfException("This control measure abbreviation is already in use: "
                    + controlMeasure.getAbbreviation());
    }

    private boolean nameExist(String name, EntityManager entityManager) {
        Long count = (Long)entityManager.createQuery("select count(cM) from ControlMeasure cM where trim(cM.name) =  '" + name.replaceAll("'", "''") + "'").getSingleResult();
        
        return count > 0 ? true : false;
    }

    private boolean abbrExist(CriteriaBuilderQueryRoot<ControlMeasure> criteriaBuilderQueryRoot, Predicate[] predicates, EntityManager entityManager) {
        return exists(criteriaBuilderQueryRoot, predicates, entityManager);
    }

    public ControlMeasure load(ControlMeasure measure, EntityManager entityManager) {
        return hibernateFacade.load(ControlMeasure.class, "name", measure.getName(), entityManager);
    }

    public int addFromImporter(ControlMeasure measure, Scc[] sccs, User user, EntityManager entityManager, DbServer dbServer) throws EmfException {
        int cmId = 0;
//        Criterion name = Restrictions.eq("name", measure.getName());
        CriteriaBuilderQueryRoot<ControlMeasure> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ControlMeasure.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ControlMeasure> root = criteriaBuilderQueryRoot.getRoot();
        Predicate abbrev = builder.equal(root.get("abbreviation"), measure.getAbbreviation());
        boolean abbrExist = abbrExist(criteriaBuilderQueryRoot, new Predicate[] { abbrev }, entityManager);
        boolean nameExist = nameExist(measure.getName(), entityManager);

        if (abbrExist) {// overwrite based on the UNIQUE control measure abbreviation
            cmId = getControlMeasureIdByAbbreviation(measure.getAbbreviation(), entityManager);
            measure.setId(cmId);
            ControlMeasure obtainLocked = grabLocked(user, cmId, entityManager);
            if (obtainLocked == null)
                throw new EmfException("Could not obtain the lock to update: " + measure.getName());
            measure.setLockDate(obtainLocked.getLockDate());
            measure.setLockOwner(obtainLocked.getLockOwner());
//            LOG.error("remove Sccs");
            removeSccs(cmId, entityManager);
//            LOG.error("remove EfficiencyRecords");
            //removeEfficiencyRecords(cmId, entityManager);
            removeEfficiencyRecords(cmId, dbServer);
//            LOG.error("remove AggregateEfficiencyRecords");
            AggregateEfficiencyRecordDAO aerDAO = new AggregateEfficiencyRecordDAO();
            aerDAO.removeAggregateEfficiencyRecords(cmId, dbServer);
//            LOG.error("update measure and sccs");
            update(measure, sccs, entityManager);
//        } else if (abbrExist) {
//            throw new EmfException("This control measure abbreviation is already in use: " + measure.getAbbreviation());
        } else {
            cmId = add(measure, sccs, entityManager);
        }
        return cmId;
    }

    
    // use only after confirming measure is exist
    private int getControlMeasureIdByAbbreviation(String abbreviation, EntityManager entityManager) {
        int id = entityManager
                    .createQuery("select cM.id from ControlMeasure cM where cM.abbreviation =  :abbreviation", Integer.class)
                    .setParameter("abbreviation", abbreviation)
                    .getSingleResult();
        
        return id;
    }

    public void removeMeasureEquationType(int controlMeasureEquationTypeId, EntityManager entityManager) {
        String hqlDelete = "delete ControlMeasureEquationType et where et.id = :controlMeasureEquationTypeId";

        final EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            entityManager
                .createQuery( hqlDelete )
                .setParameter("controlMeasureEquationTypeId", controlMeasureEquationTypeId)
                .executeUpdate();
            tx.commit(); 
        }
        catch (RuntimeException e) {
            tx.rollback();
            throw e;
        } finally {
            //
        }
    }

    public void removeEfficiencyRecords(int controlMeasureId, EntityManager entityManager) {
        String hqlDelete = "delete EfficiencyRecord er where er.controlMeasureId = :controlMeasureId";

        final EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            entityManager
                .createQuery( hqlDelete )
                .setParameter("controlMeasureId", controlMeasureId)
                .executeUpdate();
            tx.commit(); 
        }
        catch (RuntimeException e) {
            tx.rollback();
            throw e;
        } finally {
            //
        }
    }
    
    public void removeProgramMeasures(int[] cmIds, DbServer dbServer) throws EmfException {
        
        String cmIdList = "";
        for (int i = 0; i < cmIds.length; ++i) {
            cmIdList += (i > 0 ? ","  : "") + cmIds[i];
        }
        
        try {
            
            String queryStr = "delete from emf.control_program_measures " +
            "where control_measure_id in (" + cmIdList + ");";
            
            if ( DebugLevels.DEBUG_23()) {
                System.out.println( queryStr);
            }
            
            dbServer.getEmfDatasource().query().execute( queryStr);
            } catch (SQLException e) {
                
                if ( DebugLevels.DEBUG_23()) {
                    System.out.println( e.getMessage());
                }
                
                throw new EmfException(e.getMessage());
            } 
    }

    public void removeStrategyMeasures(int[] cmIds, DbServer dbServer) throws EmfException {
        
        String cmIdList = "";
        for (int i = 0; i < cmIds.length; ++i) {
            cmIdList += (i > 0 ? ","  : "") + cmIds[i];
        }
        
        try {
            
            String queryStr = "delete from emf.control_strategy_measures " +
            "where control_measure_id in (" + cmIdList + ");";
            
            if ( DebugLevels.DEBUG_23()) {
                System.out.println( queryStr);
            }
            
            dbServer.getEmfDatasource().query().execute( queryStr);
        } catch (SQLException e) {
            
            if ( DebugLevels.DEBUG_23()) {
                System.out.println( e.getMessage());
            }
            
            throw new EmfException(e.getMessage());
        }        

    }

    public void removeEfficiencyRecords(int[] sectorIds, EntityManager entityManager) {
        
        String idList = "";
        for (int i = 0; i < sectorIds.length; ++i) {
            idList += (i > 0 ? ","  : "") + sectorIds[i];
        }
        
        String hqlDelete = "delete EfficiencyRecord er where er.controlMeasureId IN (select cm.id "
                + "FROM ControlMeasure AS cm "
                + (sectorIds != null && sectorIds.length > 0 
                        ? "inner join cm.sectors AS s "
                          + "WHERE s.id in (" + idList + ") " 
                        : "") + ")";
        try {
            entityManager.createQuery( hqlDelete ).executeUpdate();
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public void removeEfficiencyRecords(int controlMeasureId, DbServer dbServer) throws EmfException {
        try {
            dbServer.getEmfDatasource().query().execute("delete from emf.control_measure_efficiencyrecords " +
                    "where control_measures_id = " + controlMeasureId + ";");
            } catch (SQLException e) {
                throw new EmfException(e.getMessage());
            }
    }

    public List allCMClasses(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ControlMeasureClass> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ControlMeasureClass.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ControlMeasureClass> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public ControlMeasureClass getCMClass(EntityManager entityManager, String name) {
        return hibernateFacade.load(ControlMeasureClass.class, "name", name, entityManager);
    }

    public List<LightControlMeasure> getLightControlMeasures(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<LightControlMeasure> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(LightControlMeasure.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<LightControlMeasure> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<EfficiencyRecord> getEfficiencyRecords(int controlMeasureId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<EfficiencyRecord> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(EfficiencyRecord.class, entityManager);
        return hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("controlMeasureId"), Integer.valueOf(controlMeasureId)));
    }

    public long getEfficiencyRecordCount(int controlMeasureId, EntityManager entityManager) {
        return entityManager
                    .createQuery("select count(EF) from EfficiencyRecord as EF where EF.controlMeasureId =  :controlMeasureId", Long.class)
                    .setParameter("controlMeasureId", controlMeasureId)
                    .getSingleResult();
    }

    public List<EfficiencyRecord> getEfficiencyRecords(int controlMeasureId, int inventoryYear, 
            String[] pollutants, EntityManager entityManager) {
        String sql = "from EfficiencyRecord as e where e.controlMeasureId = :controlMeasureId and coalesce(date_part('year', e.effectiveDate), :inventoryYear) <= :inventoryYear";
        if (pollutants.length> 0) {
            sql += " and e.pollutant.name in (";
            for (int i = 0; i < pollutants.length; i++) {
                sql += (i > 0 ? "," : "") + "'" + pollutants[i] + "'";
            }
            sql += ")";
        }
//        System.out.println(sql);
        TypedQuery<EfficiencyRecord> query = 
            entityManager
                .createQuery(sql, EfficiencyRecord.class)
                .setParameter("controlMeasureId", Integer.valueOf(controlMeasureId))
                .setParameter("inventoryYear", Integer.valueOf(inventoryYear));
        return query.getResultList();
    }

    public EfficiencyRecord[] getEfficiencyRecords(int controlMeasureId, int recordLimit, String filter, DbServer dbServer) throws EmfException {
        try {
            RetrieveEfficiencyRecord retrieveEfficiencyRecord = new RetrieveEfficiencyRecord(controlMeasureId, dbServer);
            return retrieveEfficiencyRecord.getEfficiencyRecords(recordLimit, filter);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public int addEfficiencyRecord(EfficiencyRecord efficiencyRecord, EntityManager entityManager, DbServer dbServer) throws EmfException {
        checkForDuplicateEfficiencyRecord(efficiencyRecord, entityManager);
        hibernateFacade.add(efficiencyRecord, entityManager);
        updateAggregateEfficiencyRecords(efficiencyRecord.getControlMeasureId(), dbServer);
        return efficiencyRecord.getId();
    }

//    public int addEfficiencyRecord(EfficiencyRecord efficiencyRecord, StatelessSession entityManager) throws EmfException {
//        checkForDuplicateEfficiencyRecord(efficiencyRecord, entityManager);
//        entityManager.insert(efficiencyRecord);
////        hibernateFacade.add(efficiencyRecord, entityManager);
//        return efficiencyRecord.getId();
//    }

    public void checkForDuplicateEfficiencyRecord(EfficiencyRecord record, EntityManager entityManager) throws EmfException {
        CriteriaBuilderQueryRoot<EfficiencyRecord> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(EfficiencyRecord.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<EfficiencyRecord> root = criteriaBuilderQueryRoot.getRoot();

        Predicate id = builder.notEqual(root.get("id"), Integer.valueOf(record.getId()));
        Predicate measureId = builder.equal(root.get("controlMeasureId"), Integer.valueOf(record.getControlMeasureId()));
        Predicate locale = builder.equal(root.get("locale"), record.getLocale());
        Predicate pollutant = builder.equal(root.get("pollutant"), record.getPollutant());
        Predicate existingMeasureAbbr = record.getExistingMeasureAbbr() == null ? builder.isNull(root.get("existingMeasureAbbr")) : builder.equal(root.get("existingMeasureAbbr"), record.getExistingMeasureAbbr());
        Predicate existingDevCode = builder.equal(root.get("existingDevCode"), record.getExistingDevCode());
        Predicate effectiveDate = record.getEffectiveDate() == null ? builder.isNull(root.get("effectiveDate")) : builder.equal(root.get("effectiveDate"), record.getEffectiveDate());

        Predicate minEmis = record.getMinEmis() == null ? builder.isNull(root.get("minEmis")) : builder.equal(root.get("minEmis"), record.getMinEmis());
        Predicate maxEmis = record.getMaxEmis() == null ? builder.isNull(root.get("maxEmis")) : builder.equal(root.get("maxEmis"), record.getMaxEmis());

        if (exists(criteriaBuilderQueryRoot, new Predicate[] {id, measureId, locale, pollutant, existingMeasureAbbr, existingDevCode, effectiveDate, minEmis, maxEmis}, entityManager)) {
            throw new EmfException("Duplicate Record: The combination of 'Pollutant', 'Locale', 'Effective Date', 'Existing Measure', 'Existing Dev Code', 'Minimum Emission' and 'Maximum Emission' should be unique - Locale = " + record.getLocale()
                + " Pollutant = " + record.getPollutant().getName()
                + " ExistingMeasureAbbr = " + record.getExistingMeasureAbbr()
                + " ExistingDevCode = " + record.getExistingDevCode()
                + " EffectiveDate = " + (record.getEffectiveDate() == null ? "" : record.getEffectiveDate())
                + " MinEmis = " + (record.getMinEmis() == null ? "" : record.getMinEmis())
                + " MaxEmis = " + (record.getMaxEmis() == null ? "" : record.getMaxEmis()));
        }
    }

//    public void checkForDuplicateEfficiencyRecord(EfficiencyRecord record, StatelessSession entityManager) throws EmfException {
//        CriteriaBuilderQueryRoot<EfficiencyRecord> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(EfficiencyRecord.class, entityManager);
//        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
//        Root<EfficiencyRecord> root = criteriaBuilderQueryRoot.getRoot();
//
//        Criterion id = Restrictions.ne("id", Integer.valueOf(record.getId()));
//        Criterion measureId = Restrictions.eq("controlMeasureId", Integer.valueOf(record.getControlMeasureId()));
//        Criterion locale = Restrictions.eq("locale", record.getLocale());
//        Criterion pollutant = Restrictions.eq("pollutant", record.getPollutant());
//        Criterion existingMeasureAbbr = record.getExistingMeasureAbbr() == null ? Restrictions.isNull("existingMeasureAbbr") : Restrictions.eq("existingMeasureAbbr", record.getExistingMeasureAbbr());
//        Criterion effectiveDate = record.getEffectiveDate() == null ? Restrictions.isNull("effectiveDate") : Restrictions.eq("effectiveDate", record.getEffectiveDate());
//
//        if (exists(EfficiencyRecord.class, new Criterion[] {id, measureId, locale, pollutant, existingMeasureAbbr, effectiveDate}, entityManager)) {
//            throw new EmfException("Duplicate Record: The combination of 'Pollutant', 'Locale', 'Effective Date' and 'Existing Measure' should be unique - Locale = " + record.getLocale()
//                + " Pollutant = " + record.getPollutant().getName()
//                + " ExistingMeasureAbbr = " + record.getExistingMeasureAbbr()
//                + " EffectiveDate = " + (record.getEffectiveDate() == null ? "" : record.getEffectiveDate()));
//        }
//    }
//
    public void removeEfficiencyRecord(int efficiencyRecordId, EntityManager entityManager, DbServer dbServer) throws EmfException {
        EfficiencyRecord er = hibernateFacade.current(efficiencyRecordId, EfficiencyRecord.class, entityManager);
        int cmId = er.getControlMeasureId();
        hibernateFacade.remove(hibernateFacade.current(efficiencyRecordId, EfficiencyRecord.class, entityManager), entityManager);
        updateAggregateEfficiencyRecords(cmId, dbServer);
    }

    public void updateEfficiencyRecord(EfficiencyRecord efficiencyRecord, EntityManager entityManager, DbServer dbServer) throws EmfException {
        checkForDuplicateEfficiencyRecord(efficiencyRecord, entityManager);
        hibernateFacade.saveOrUpdate(efficiencyRecord, entityManager);
        updateAggregateEfficiencyRecords(efficiencyRecord.getControlMeasureId(), dbServer);
    }

    public ControlMeasure[] getSummaryControlMeasures(DbServer dbServer, String whereFilter) throws EmfException {
        try {
            RetrieveControlMeasure retrieveControlMeasure = new RetrieveControlMeasure(dbServer);
            return retrieveControlMeasure.getControlMeasures(whereFilter);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public ControlMeasure[] getSummaryControlMeasures(int majorPollutantId, DbServer dbServer, String whereFilter) throws EmfException {
        try {
            RetrieveControlMeasure retrieveControlMeasure = new RetrieveControlMeasure(dbServer);
            return retrieveControlMeasure.getControlMeasures(majorPollutantId, whereFilter);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public List<ControlMeasure> getLightControlMeasures(String whereFilter, DbServer dbServer) throws EmfException {

        try {
            RetrieveControlMeasure retrieveControlMeasure = new RetrieveControlMeasure(dbServer);
            return retrieveControlMeasure.getLightControlMeasures(whereFilter);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public List<ControlMeasure> getLightControlMeasures(int majorPollutantId, String whereFilter, DbServer dbServer) throws EmfException {

        try {
            RetrieveControlMeasure retrieveControlMeasure = new RetrieveControlMeasure(dbServer);
            return retrieveControlMeasure.getLightControlMeasures(majorPollutantId, whereFilter);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    private void updateAggregateEfficiencyRecords(int controlMeasureId, DbServer dbServer) throws EmfException {
        try {
            AggregateEfficiencyRecordDAO aerDAO = new AggregateEfficiencyRecordDAO();
            aerDAO.updateAggregateEfficiencyRecords(controlMeasureId, dbServer);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public void updateAggregateEfficiencyRecords(ControlMeasure[] measures, DbServer dbServer) throws EmfException {
        try {
            AggregateEfficiencyRecordDAO aerDAO = new AggregateEfficiencyRecordDAO();
            for (int i = 0; i < measures.length; i++) {
                aerDAO.updateAggregateEfficiencyRecords(measures[i].getId(), dbServer);
            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public List<EquationType> getEquationTypes(EntityManager entityManager) {
        TypedQuery<EquationType> query = 
            entityManager
                .createQuery("from EquationType as e order by e.name", EquationType.class);
        return query.getResultList();
    }

    public List<ControlMeasurePropertyCategory> getPropertyCategories(EntityManager entityManager) {
        TypedQuery<ControlMeasurePropertyCategory> query = 
            entityManager
                .createQuery("from ControlMeasurePropertyCategory as e order by e.name", ControlMeasurePropertyCategory.class);
        return query.getResultList();
    }

    public List<Sector> getDistinctControlMeasureSectors(EntityManager entityManager) {
        TypedQuery<Sector> query = 
            entityManager
                .createQuery("select distinct s FROM ControlMeasure AS cm inner join cm.sectors AS s order by s.name", Sector.class);
        return query.getResultList();
    }

    public List<ControlMeasure> getControlMeasureBySectors(int[] sectorIds, boolean allClasses, EntityManager entityManager) {
        String idList = "";
        for (int i = 0; i < sectorIds.length; ++i) {
            idList += (i > 0 ? ","  : "") + sectorIds[i];
        }
        
        String join = (sectorIds.length > 0 ? "inner join cm.sectors AS s " : "") +
                      (!allClasses ? "inner join cm.cmClass AS cl " : "");
        TypedQuery<ControlMeasure> query = 
            entityManager
                .createQuery("select new ControlMeasure(cm.id, cm.name, cm.abbreviation) "
                    + "FROM ControlMeasure AS cm "
                    + join
                    + "WHERE 1 = 1 "
                    + (sectorIds.length > 0 ? "AND s.id IN (" + idList + ") " : "")
                    + (!allClasses ? "AND cl.name NOT IN ('Obsolete', 'Temporary') " : "")
                    + "order by cm.name", ControlMeasure.class);
        return query.getResultList();
    }
}