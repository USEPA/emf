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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

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

    public List getKeywords(Session session) {
        return keywordsDAO.getKeywords(session);
    }

    public void add(Region region, Session session) {
        addObject(region, session);
    }
    
    public void add(DatasetNote note, Session session) {
        addObject(note, session);
    }

    public void add(Project project, Session session) {
        addObject(project, session);
    }

    public List<Region> getRegions(Session session) {
        CriteriaBuilderQueryRoot<Region> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Region.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Region> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }
    
    public List<UserFeature> getUserFeatures(Session session) {
        CriteriaBuilderQueryRoot<UserFeature> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(UserFeature.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<UserFeature> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List<Project> getProjects(Session session) {
        CriteriaBuilderQueryRoot<Project> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Project.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Project> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List<Country> getCountries(Session session) {
        CriteriaBuilderQueryRoot<Country> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Country.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Country> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List getSectors(Session session) {
        return sectorsDao.getAll(session);
    }

    public List getDatasetTypes(Session session) {
        return datasetTypesDAO.getAll(session);
    }

    public List<DatasetType> getDatasetTypes(Session session, BasicSearchFilter searchFilter) {
        return datasetTypesDAO.getDatasetTypes(session, searchFilter);
    }

    public List<DatasetType> getDatasetTypes(int userId, Session session) {
        return session
            .createQuery(
                    "select DT from DatasetType as DT " 
                    + "where "
                    + " DT.id not in (select EDT.id from User as U "
                    + " inner join U.excludedDatasetTypes as EDT where U.id = "
                    + userId + ")" 
                    + " order by DT.name", DatasetType.class)
            .list();
    }

    public List<DatasetType> getLightDatasetTypes(int userId, Session session) {
        return session
            .createQuery(
                    "select new DatasetType(DT.id, DT.name) from DatasetType as DT " 
                    + "where "
                    + " DT.id not in (select EDT.id from User as U "
                    + " inner join U.excludedDatasetTypes as EDT where U.id = "
                    + userId + ")" 
                    + " order by DT.name", DatasetType.class) 
            .list();
        
//        "select new DatasetType(dT.id, dT.name) " +
//        "from DatasetType dT order by dT.name"
    }

    public List<DatasetType> getLightDatasetTypes(Session session) {
        return datasetTypesDAO.getLightAll(session);
    }

    public DatasetType getLightDatasetType(String name, Session session) {
        return session
                .createQuery(
                        "select new DatasetType(DT.id, DT.name) from DatasetType as DT " 
                        + "where DT.name = :datasetTypeName", DatasetType.class) 
                .setParameter("datasetTypeName", name)
                .uniqueResult();
    }

    public DatasetType getDatasetType(String name, Session session) {
        return datasetTypesDAO.get(name, session);
    }

    public DatasetType getDatasetType(int id, Session session) {
        return datasetTypesDAO.current(id, session);
    }

    public Sector obtainLockedSector(User user, Sector sector, Session session) {
        return sectorsDao.obtainLocked(user, sector, session);
    }

    public DatasetType obtainLockedDatasetType(User user, DatasetType type, Session session) {
        return datasetTypesDAO.obtainLocked(user, type, session);
    }

    public Sector updateSector(Sector sector, Session session) throws EmfException {
        return sectorsDao.update(sector, session);
    }
    
    public GeoRegion obtainLockedRegion(User user, GeoRegion region, Session session) {
        return (GeoRegion)lockingScheme.getLocked(user, current(region, session), session);
    }
    
    public GeoRegion updateGeoregion(GeoRegion region, User user, Session session) throws EmfException {
        return (GeoRegion) lockingScheme.releaseLockOnUpdate(region, current(region, session), session);
    }
    
    private GeoRegion current(GeoRegion region, Session session) {
        return current(region.getId(), GeoRegion.class, session);
    }

    public Revision obtainLockedRevision(User user, Revision revision, Session session) {
        return datasetDAO.obtainLocked(user, revision, session);
    }

    public Revision releaseLockedRevision (User user, Revision locked, Session session) {
        return datasetDAO.releaseLocked(user, locked, session);
    }

    public Revision updateRevision(Revision revision, Session session) throws EmfException {
        return datasetDAO.update(revision, session);
    }

    public Version getVersion(int datasetId, int version, Session session) throws EmfException {
        return datasetDAO.getVersion(session, datasetId, version);
    }

    public DatasetType updateDatasetType(DatasetType type, Session session) throws EmfException {
        return datasetTypesDAO.update(type, session);
    }

    public Sector releaseLockedSector(User user, Sector locked, Session session) {
        return sectorsDao.releaseLocked(user, locked, session);
    }

    public DatasetType releaseLockedDatasetType(User user, DatasetType locked, Session session) {
        return datasetTypesDAO.releaseLocked(user, locked, session);
    }

    public List getPollutants(Session session) {
        return pollutantsDAO.getAll(session);
    }

    public List getSourceGroups(Session session) {
        return sourceGroupsDAO.getAll(session);
    }

    public List getControlMeasureImportStatuses(String username, Session session) {
        return getStatus(username, session);
    }

    public void removeDatasetTypes(DatasetType type, Session session) {
        hibernateFacade.remove(type, session);
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
   
    public void removeXFileFormat(XFileFormat fileFormat, Session session) {
        hibernateFacade.remove(fileFormat, session);
    }
    
    public void removeStatuses(String username, String type, Session session) {
        String hqlDelete = "delete Status s where s.username = :username and s.type = :type";
        Transaction tx = null;
        try {
            tx = session.
                    beginTransaction();
            session.createQuery(hqlDelete)
                .setParameter("username", username)
                .setParameter("type", type)
                .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }

    public List<Status> getStatuses(String username, Session session) {
        return getStatus(username, session);
    }

    private List<Status> getStatus(String username, Session session) {
        removeReadStatus(username, session);

        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            CriteriaBuilderQueryRoot<Status> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Status.class, session);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<Status> root = criteriaBuilderQueryRoot.getRoot();

            Predicate criterion1 = builder.equal(root.get("username"), username);
            Predicate criterion2 = builder.notEqual(root.get("type"), "CMImportDetailMsg");

            List<Status> all = hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { criterion1, criterion2 }, builder.desc(root.get("timestamp")), session);

            // mark read
            for (Iterator<Status> iter = all.iterator(); iter.hasNext();) {
                Status element = iter.next();
                element.markRead();
                session.save(element);

            }
            tx.commit();

            return all;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void add(Status status, Session session) {
        addObject(status, session);
    }

    private void removeReadStatus(String username, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            CriteriaBuilderQueryRoot<Status> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Status.class, session);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<Status> root = criteriaBuilderQueryRoot.getRoot();

            Predicate criterion1 = builder.equal(root.get("username"), username);
            Predicate criterion2 = builder.equal(root.get("read"), Boolean.TRUE);

            List<Status> read = hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { criterion1, criterion2 }, builder.desc(root.get("timestamp")), session);

            for (Iterator<Status> iter = read.iterator(); iter.hasNext();) {
                Status element = iter.next();
                session.delete(element);
            }

            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List getIntendedUses(Session session) {
        CriteriaBuilderQueryRoot<IntendedUse> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(IntendedUse.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<IntendedUse> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public void add(IntendedUse intendedUse, Session session) {
        addObject(intendedUse, session);
    }

    public void add(Country country, Session session) {
        addObject(country, session);
    }

    public void add(DatasetType datasetType, Session session) {
        datasetTypesDAO.add(datasetType, session);
    }

    public void add(XFileFormat format, Session session) {
        addObject(format, session);
    }

    public void update(XFileFormat format, Session session) {
        hibernateFacade.updateOnly(format, session);
    }

    public void add(Sector sector, Session session) {
        addObject(sector, session);
    }
    
    public void add(GeoRegion grid, Session session) {
        addObject(grid, session);
    }

    private void addObject(Object obj, Session session) {
        hibernateFacade.add(obj, session);
    }

    public List<NoteType> getNoteTypes(Session session) {
        CriteriaBuilderQueryRoot<NoteType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(NoteType.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<NoteType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, session);
    }

    public void add(Revision revision, Session session) {
        addObject(revision, session);
    }

    public void add1(DatasetNote note, Session session) {
        addObject(note, session);
    }

    public void add(Pollutant pollutant, Session session) {
        addObject(pollutant, session);
    }

    public void add(SourceGroup sourcegrp, Session session) {
        addObject(sourcegrp, session);
    }

    public List<GeoRegion> getGeoRegions(Session session) {
        CriteriaBuilderQueryRoot<GeoRegion> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(GeoRegion.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<GeoRegion> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }
    
    public List<RegionType> getRegionTypes(Session session) {
        CriteriaBuilderQueryRoot<RegionType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(RegionType.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<RegionType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }
    
    public List<Revision> getRevisions(int datasetId, Session session) {
        CriteriaBuilderQueryRoot<Revision> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Revision.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Revision> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("datasetId"), Integer.valueOf(datasetId)) }, session);
    }

    public List<DatasetNote> getDatasetNotes(int datasetId, Session session) {
        CriteriaBuilderQueryRoot<DatasetNote> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(DatasetNote.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<DatasetNote> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("datasetId"), Integer.valueOf(datasetId)) }, session);
    }
    
    public List<Note> getNotes(Session session, String nameContains) {
        if (nameContains.trim().equals(""))
            return session
            .createQuery(
                    "select new Note( NT.id, NT.name) from Note as NT ", Note.class).list();
        return session
        .createQuery(
                "select new Note( NT.id, NT.name) from Note as NT " + "where "
                        + " lower(NT.name) like "
                        + "'%"
                        + nameContains.toLowerCase().trim() + "%'", Note.class) 
        .list();
 }

    
    /*
     * Return true if the name is already used
     */
    public <C> boolean nameUsed(String name, Class<C> clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    public <C> C current(int id, Class<C> clazz, Session session) {
        return hibernateFacade.current(id, clazz, session);
    }

    public <C> boolean exists(int id, Class<C> clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public boolean canUpdate(Sector sector, Session session) {
        return sectorsDao.canUpdate(sector, session);
    }

    public boolean canUpdate(DatasetType datasetType, Session session) {
        return datasetTypesDAO.canUpdate(datasetType, session);
    }
    
    public void validateDatasetTypeIndicesKeyword(DatasetType datasetType, Column[] cols) throws EmfException {
        datasetTypesDAO.validateDatasetTypeIndicesKeyword(datasetType, cols);
    }
    
    public <T> T load(Class<T> clazz, String name, Session session) {
        return hibernateFacade.load(clazz, "name", name, session);
    }
    
    public <C,K> List<C> get(Class<C> clazz, String keyField, K keyValue, Session session){
        return hibernateFacade.get(clazz, keyField, keyValue, session);
    }
    
    public void updateProject(Project project, Session session) {
        hibernateFacade.updateOnly(project, session);
    }
}