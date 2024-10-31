package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.data.UserFeature;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;

public class DataCommonsDAO {

    private SectorsDAO sectorsDao;

    private HibernateFacade hibernateFacade;

    private KeywordsDAO keywordsDAO;

    private DatasetTypesDAO datasetTypesDAO;

    private PollutantsDAO pollutantsDAO;

    private SourceGroupsDAO sourceGroupsDAO;

    private DatasetDAO datasetDAO;
    
    private LockingScheme lockingScheme;
    
    public DataCommonsDAO() {
        sectorsDao = new SectorsDAO();
        hibernateFacade = new HibernateFacade();
        lockingScheme = new LockingScheme();
        keywordsDAO = new KeywordsDAO();
        datasetTypesDAO = new DatasetTypesDAO();
        pollutantsDAO = new PollutantsDAO();
        sourceGroupsDAO = new SourceGroupsDAO();
        datasetDAO = new DatasetDAO();
    }

    public List getKeywords(EntityManager entityManager) {
        return keywordsDAO.getKeywords(entityManager);
    }

    public void add(Region region, EntityManager entityManager) {
        hibernateFacade.add(region, entityManager);
    }
    
    public void add(DatasetNote note, EntityManager entityManager) {
        hibernateFacade.add(note, entityManager);
    }

    public void add(Project project, EntityManager entityManager) {
        hibernateFacade.add(project, entityManager);
    }

    public List<Region> getRegions(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Region> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Region.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Region> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }
    
    public List<UserFeature> getUserFeatures(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<UserFeature> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(UserFeature.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<UserFeature> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<Project> getProjects(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Project> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Project.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Project> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<Country> getCountries(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Country> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Country.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Country> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List getSectors(EntityManager entityManager) {
        return sectorsDao.getAll(entityManager);
    }

    public List getDatasetTypes(EntityManager entityManager) {
        return datasetTypesDAO.getAll(entityManager);
    }

    public List<DatasetType> getDatasetTypes(EntityManager entityManager, BasicSearchFilter searchFilter) {
        return datasetTypesDAO.getDatasetTypes(entityManager, searchFilter);
    }

    public List<DatasetType> getDatasetTypes(int userId, EntityManager entityManager) {
        return entityManager
            .createQuery(
                    "select DT from DatasetType as DT " 
                    + "where "
                    + " DT.id not in (select EDT.id from User as U "
                    + " inner join U.excludedDatasetTypes as EDT where U.id = "
                    + userId + ")" 
                    + " order by DT.name", DatasetType.class)
            .getResultList();
    }

    public List<DatasetType> getLightDatasetTypes(int userId, EntityManager entityManager) {
        return entityManager
            .createQuery(
                    "select new DatasetType(DT.id, DT.name) from DatasetType as DT " 
                    + "where "
                    + " DT.id not in (select EDT.id from User as U "
                    + " inner join U.excludedDatasetTypes as EDT where U.id = "
                    + userId + ")" 
                    + " order by DT.name", DatasetType.class) 
            .getResultList();
        
//        "select new DatasetType(dT.id, dT.name) " +
//        "from DatasetType dT order by dT.name"
    }

    public List<DatasetType> getLightDatasetTypes(EntityManager entityManager) {
        return datasetTypesDAO.getLightAll(entityManager);
    }

    public DatasetType getLightDatasetType(String name, EntityManager entityManager) {
        return entityManager
                .createQuery(
                        "select new DatasetType(DT.id, DT.name) from DatasetType as DT " 
                        + "where DT.name = :datasetTypeName", DatasetType.class) 
                .setParameter("datasetTypeName", name)
                .getSingleResult();
    }

    public DatasetType getDatasetType(String name, EntityManager entityManager) {
        return datasetTypesDAO.get(name, entityManager);
    }

    public DatasetType getDatasetType(int id, EntityManager entityManager) {
        return datasetTypesDAO.current(id, entityManager);
    }

    public Sector obtainLockedSector(User user, Sector sector, EntityManager entityManager) {
        return sectorsDao.obtainLocked(user, sector, entityManager);
    }

    public DatasetType obtainLockedDatasetType(User user, DatasetType type, EntityManager entityManager) {
        return datasetTypesDAO.obtainLocked(user, type, entityManager);
    }

    public Sector updateSector(Sector sector, EntityManager entityManager) throws EmfException {
        return sectorsDao.update(sector, entityManager);
    }
    
    public GeoRegion obtainLockedRegion(User user, GeoRegion region, EntityManager entityManager) {
        return (GeoRegion)lockingScheme.getLocked(user, current(region, entityManager), entityManager);
    }
    
    public GeoRegion updateGeoregion(GeoRegion region, User user, EntityManager entityManager) throws EmfException {
        return (GeoRegion) lockingScheme.releaseLockOnUpdate(region, current(region, entityManager), entityManager);
    }
    
    private GeoRegion current(GeoRegion region, EntityManager entityManager) {
        return current(region.getId(), GeoRegion.class, entityManager);
    }

    public Revision obtainLockedRevision(User user, Revision revision, EntityManager entityManager) {
        return datasetDAO.obtainLocked(user, revision, entityManager);
    }

    public Revision releaseLockedRevision (User user, Revision locked, EntityManager entityManager) {
        return datasetDAO.releaseLocked(user, locked, entityManager);
    }

    public Revision updateRevision(Revision revision, EntityManager entityManager) throws EmfException {
        return datasetDAO.update(revision, entityManager);
    }

    public Version getVersion(int datasetId, int version, EntityManager entityManager) throws EmfException {
        return datasetDAO.getVersion(entityManager, datasetId, version);
    }

    public DatasetType updateDatasetType(DatasetType type, EntityManager entityManager) throws EmfException {
        return datasetTypesDAO.update(type, entityManager);
    }

    public Sector releaseLockedSector(User user, Sector locked, EntityManager entityManager) {
        return sectorsDao.releaseLocked(user, locked, entityManager);
    }

    public DatasetType releaseLockedDatasetType(User user, DatasetType locked, EntityManager entityManager) {
        return datasetTypesDAO.releaseLocked(user, locked, entityManager);
    }

    public List getPollutants(EntityManager entityManager) {
        return pollutantsDAO.getAll(entityManager);
    }

    public List getSourceGroups(EntityManager entityManager) {
        return sourceGroupsDAO.getAll(entityManager);
    }

    public List getControlMeasureImportStatuses(String username, EntityManager entityManager) {
        return getStatus(username, entityManager);
    }

    public void removeDatasetTypes(DatasetType type, EntityManager entityManager) {
        hibernateFacade.remove(type, entityManager);
    }
    
    public void removeUserExcludedDatasetType(DatasetType type, DbServer dbServer) throws EmfException {
        //remove dataset types from the user_excluded_dataset_types table
        try {
            dbServer.getEmfDatasource().query().execute("delete from emf.user_excluded_dataset_types where dataset_type_id = "
                    + type.getId() + "");
        } catch (SQLException e) {
            
            if ( DebugLevels.DEBUG_23()) {
                System.out.println( e.getMessage());
            }
            
            throw new EmfException(e.getMessage(), e);
        } 
//        dbServer
//        .createSQLQuery(
//                "delete from emf.user_excluded_dataset_types where dataset_type_id = "
//                + type.getId() + "").executeUpdate();
    }
    
    public void removeTableConsolidation(DatasetType type, DbServer dbServer) throws EmfException {
        //remove dataset types from the user_excluded_dataset_types table
        try {
            dbServer.getEmfDatasource().query().execute("delete from emf.table_consolidations where dataset_type_id = "
                    + type.getId() + "");
        } catch (SQLException e) {
            
            if ( DebugLevels.DEBUG_23()) {
                System.out.println( e.getMessage());
            }
            
            throw new EmfException(e.getMessage(), e);
        } 
    }
   
    public void removeXFileFormat(XFileFormat fileFormat, EntityManager entityManager) {
        hibernateFacade.remove(fileFormat, entityManager);
    }
    
    public void removeStatuses(String username, String type, EntityManager entityManager) {
        String hqlDelete = "delete Status s where s.username = :username and s.type = :type";
        try {
            hibernateFacade.executeInsideTransaction(em -> {
                em.createQuery(hqlDelete)
                    .setParameter("username", username)
                    .setParameter("type", type)
                    .executeUpdate();
            }, entityManager);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public List<Status> getStatuses(String username, EntityManager entityManager) {
        return getStatus(username, entityManager);
    }

    private List<Status> getStatus(String username, EntityManager entityManager) {
        removeReadStatus(username, entityManager);

        try {
            CriteriaBuilderQueryRoot<Status> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Status.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<Status> root = criteriaBuilderQueryRoot.getRoot();

            Predicate criterion1 = builder.equal(root.get("username"), username);
            Predicate criterion2 = builder.notEqual(root.get("type"), "CMImportDetailMsg");

            List<Status> all = hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { criterion1, criterion2 }, builder.desc(root.get("timestamp")), entityManager);

            hibernateFacade.executeInsideTransaction(em -> {
                // mark read
                for (Iterator<Status> iter = all.iterator(); iter.hasNext();) {
                    Status element = iter.next();
                    element.markRead();
                    entityManager.merge(element);

                }
            }, entityManager);

            return all;
        } catch (HibernateException e) {
            throw e;
        }
    }

    public void add(Status status, EntityManager entityManager) {
        hibernateFacade.add(status, entityManager);
    }

    private void removeReadStatus(String username, EntityManager entityManager) {
        try {
            CriteriaBuilderQueryRoot<Status> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Status.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<Status> root = criteriaBuilderQueryRoot.getRoot();

            Predicate criterion1 = builder.equal(root.get("username"), username);
            Predicate criterion2 = builder.equal(root.get("read"), Boolean.TRUE);

            List<Status> read = hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { criterion1, criterion2 }, builder.desc(root.get("timestamp")), entityManager);

            hibernateFacade.executeInsideTransaction(em -> {
                for (Iterator<Status> iter = read.iterator(); iter.hasNext();) {
                    Status element = iter.next();
                    entityManager.remove(element);
                }
            }, entityManager);

        } catch (HibernateException e) {
            throw e;
        }
    }

    public List getIntendedUses(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<IntendedUse> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(IntendedUse.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<IntendedUse> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public void add(IntendedUse intendedUse, EntityManager entityManager) {
        hibernateFacade.add(intendedUse, entityManager);
    }

    public void add(Country country, EntityManager entityManager) {
        hibernateFacade.add(country, entityManager);
    }

    public void add(DatasetType datasetType, EntityManager entityManager) {
        hibernateFacade.add(datasetType, entityManager);
    }

    public void add(XFileFormat format, EntityManager entityManager) {
        hibernateFacade.add(format, entityManager);
    }

    public void update(XFileFormat format, EntityManager entityManager) {
        hibernateFacade.updateOnly(format, entityManager);
    }

    public void add(Sector sector, EntityManager entityManager) {
        hibernateFacade.add(sector, entityManager);
    }
    
    public void add(GeoRegion grid, EntityManager entityManager) {
        hibernateFacade.add(grid, entityManager);
    }

    public List<NoteType> getNoteTypes(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<NoteType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(NoteType.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<NoteType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);
    }

    public void add(Revision revision, EntityManager entityManager) {
        hibernateFacade.add(revision, entityManager);
    }

    public void add1(DatasetNote note, EntityManager entityManager) {
        hibernateFacade.add(note, entityManager);
    }

    public void add(Pollutant pollutant, EntityManager entityManager) {
        hibernateFacade.add(pollutant, entityManager);
    }

    public void add(SourceGroup sourcegrp, EntityManager entityManager) {
        hibernateFacade.add(sourcegrp, entityManager);
    }

    public List<GeoRegion> getGeoRegions(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<GeoRegion> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(GeoRegion.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<GeoRegion> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }
    
    public List<RegionType> getRegionTypes(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<RegionType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(RegionType.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<RegionType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }
    
    public List<Revision> getRevisions(int datasetId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Revision> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Revision.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Revision> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("datasetId"), Integer.valueOf(datasetId)) }, entityManager);
    }

    public List<DatasetNote> getDatasetNotes(int datasetId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<DatasetNote> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(DatasetNote.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<DatasetNote> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("datasetId"), Integer.valueOf(datasetId)) }, entityManager);
    }
    
    public List<Note> getNotes(EntityManager entityManager, String nameContains) {
        if (nameContains.trim().equals(""))
            return entityManager
            .createQuery(
                    "select new Note( NT.id, NT.name) from Note as NT ", Note.class).getResultList();
        return entityManager
        .createQuery(
                "select new Note( NT.id, NT.name) from Note as NT " + "where "
                        + " lower(NT.name) like "
                        + "'%"
                        + nameContains.toLowerCase().trim() + "%'", Note.class) 
        .getResultList();
 }

    
    /*
     * Return true if the name is already used
     */
    public <C> boolean nameUsed(String name, Class<C> clazz, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, clazz, entityManager);
    }

    public <C> C current(int id, Class<C> clazz, EntityManager entityManager) {
        return hibernateFacade.current(id, clazz, entityManager);
    }

    public <C> boolean exists(int id, Class<C> clazz, EntityManager entityManager) {
        return hibernateFacade.exists(id, clazz, entityManager);
    }

    public boolean canUpdate(Sector sector, EntityManager entityManager) {
        return sectorsDao.canUpdate(sector, entityManager);
    }

    public boolean canUpdate(DatasetType datasetType, EntityManager entityManager) {
        return datasetTypesDAO.canUpdate(datasetType, entityManager);
    }
    
    public void validateDatasetTypeIndicesKeyword(DatasetType datasetType, Column[] cols) throws EmfException {
        datasetTypesDAO.validateDatasetTypeIndicesKeyword(datasetType, cols);
    }
    
    public <T> T load(Class<T> clazz, String name, EntityManager entityManager) {
        return hibernateFacade.load(clazz, "name", name, entityManager);
    }
    
    public <C,K> List<C> get(Class<C> clazz, String keyField, K keyValue, EntityManager entityManager){
        return hibernateFacade.get(clazz, keyField, keyValue, entityManager);
    }
    
    public void updateProject(Project project, EntityManager entityManager) {
        hibernateFacade.updateOnly(project, entityManager);
    }
}