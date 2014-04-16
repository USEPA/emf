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
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

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

    public List getRegions(Session session) {
        return session.createCriteria(Region.class).addOrder(Order.asc("name")).list();
    }
    
    public List getUserFeatures(Session session) {
        return session.createCriteria(UserFeature.class).addOrder(Order.asc("name")).list();
    }

    public List getProjects(Session session) {
        return session.createCriteria(Project.class).addOrder(Order.asc("name")).list();
    }

    public List getCountries(Session session) {
        return session.createCriteria(Country.class).addOrder(Order.asc("name")).list();
    }

    public List getSectors(Session session) {
        return sectorsDao.getAll(session);
    }

    public List getDatasetTypes(Session session) {
        return datasetTypesDAO.getAll(session);
    }

    public List<DatasetType> getDatasetTypes(int userId, Session session) {
        return session
            .createQuery(
                    "select DT from DatasetType as DT " 
                    + "where "
                    + " DT.id not in (select EDT.id from User as U "
                    + " inner join U.excludedDatasetTypes as EDT where U.id = "
                    + userId + ")" 
                    + " order by DT.name") 
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
                    + " order by DT.name") 
            .list();
        
//        "select new DatasetType(dT.id, dT.name) " +
//        "from DatasetType dT order by dT.name"
    }

    public List<DatasetType> getLightDatasetTypes(Session session) {
        return datasetTypesDAO.getLightAll(session);
    }

    public DatasetType getLightDatasetType(String name, Session session) {
        return (DatasetType) session
                .createQuery(
                        "select new DatasetType(DT.id, DT.name) from DatasetType as DT " 
                        + "where DT.name = :datasetTypeName") 
                .setString("datasetTypeName", name)
                .uniqueResult();
    }

    public DatasetType getDatasetType(String name, Session session) {
        return datasetTypesDAO.get(name, session);
    }

    public DatasetType getDatasetType(int id, Session session) {
        return datasetTypesDAO.current(id, DatasetType.class, session);
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
        return (GeoRegion)current(region.getId(), GeoRegion.class, session);
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
        Criterion criterion1 = Restrictions.eq("username", username);
        Criterion criterion2 = Restrictions.eq("type", "CMImportDetailMsg");
        return getStatus(username, new Criterion[] { criterion1, criterion2 }, session);
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
        session.createQuery(hqlDelete).setString("username", username).setString("type", type).executeUpdate();
    }

    public List getStatuses(String username, Session session) {
        Criterion criterion1 = Restrictions.eq("username", username);
        Criterion criterion2 = Restrictions.ne("type", "CMImportDetailMsg");
        return getStatus(username, new Criterion[] { criterion1, criterion2 }, session);
    }

    private List getStatus(String username, Criterion[] criterions, Session session) {
        removeReadStatus(username, session);

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            // Criteria crit = session.createCriteria(Status.class).add();
            Criteria crit = session.createCriteria(Status.class).addOrder(Order.desc("timestamp"));

            for (int i = 0; i < criterions.length; i++) {// add restrictions
                crit = crit.add(criterions[i]);
            }
            List all = crit.list();

            // mark read
            for (Iterator iter = all.iterator(); iter.hasNext();) {
                Status element = (Status) iter.next();
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
            Criteria crit = session.createCriteria(Status.class).add(Restrictions.eq("username", username)).add(
                    Restrictions.eq("read", Boolean.TRUE));

            List read = crit.list();
            for (Iterator iter = read.iterator(); iter.hasNext();) {
                Status element = (Status) iter.next();
                session.delete(element);
            }

            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List getIntendedUses(Session session) {
        return session.createCriteria(IntendedUse.class).addOrder(Order.asc("name")).list();
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

    public void add(Sector sector, Session session) {
        addObject(sector, session);
    }
    
    public void add(GeoRegion grid, Session session) {
        addObject(grid, session);
    }

    private void addObject(Object obj, Session session) {
        hibernateFacade.add(obj, session);
    }

    public List getNoteTypes(Session session) {
        return session.createCriteria(NoteType.class).list();
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
        return hibernateFacade.getAll(GeoRegion.class, Order.asc("name"), session);
    }
    
    public List<RegionType> getRegionTypes(Session session) {
        return hibernateFacade.getAll(RegionType.class, Order.asc("name"), session);
    }
    
    public List getRevisions(int datasetId, Session session) {
        return session.createCriteria(Revision.class).add(Restrictions.eq("datasetId", new Integer(datasetId))).list();
    }

    public List getDatasetNotes(int datasetId, Session session) {
        return session.createCriteria(DatasetNote.class).add(Restrictions.eq("datasetId", new Integer(datasetId))).list();
    }
    
    public List getNotes(Session session, String nameContains) {
        if (nameContains.trim().equals(""))
            return session
            .createQuery(
                    "select new Note( NT.id, NT.name) from Note as NT ").list();
        return session
        .createQuery(
                "select new Note( NT.id, NT.name) from Note as NT " + "where "
                        + " lower(NT.name) like "
                        + "'%"
                        + nameContains.toLowerCase().trim() + "%'") 
        .list();
 }

    
    /*
     * Return true if the name is already used
     */
    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    public Object current(int id, Class clazz, Session session) {
        return hibernateFacade.current(id, clazz, session);
    }

    public boolean exists(int id, Class clazz, Session session) {
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
    
    public Object load(Class clazz, String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        return hibernateFacade.load(clazz, criterion, session);
    }
    
    public List get(Class clazz, Criterion criterion, Session session){
        return hibernateFacade.get(clazz, criterion, session);
    }

}