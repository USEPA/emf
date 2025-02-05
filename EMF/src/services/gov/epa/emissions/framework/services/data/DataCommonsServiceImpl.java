package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.data.UserFeature;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.io.csv.CSVFileReader;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFilePatternMatcher;
import gov.epa.emissions.framework.services.basic.EmfFileSerializer;
import gov.epa.emissions.framework.services.basic.EmfServerFileSystemView;
import gov.epa.emissions.framework.services.basic.FileDownload;
import gov.epa.emissions.framework.services.basic.FileDownloadDAO;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.cost.controlStrategy.FileFormatFactory;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

public class DataCommonsServiceImpl implements DataCommonsService {

    private static Log LOG = LogFactory.getLog(DataCommonsServiceImpl.class);

    private EntityManagerFactory entityManagerFactory;

    private DbServerFactory dbServerFactory;

    private DataCommonsDAO dao;

    private FileDownloadDAO fileDownloadDAO;

    private EmfFileInfo[] files;

    private EmfFileInfo[] subdirs;

    private EmfFileInfo currentDirectory;

    public DataCommonsServiceImpl() {
        this(JpaEntityManagerFactory.get());
        this.dbServerFactory = DbServerFactory.get();
    }

    public DataCommonsServiceImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.fileDownloadDAO = new FileDownloadDAO();
        dao = new DataCommonsDAO();
    }

    public synchronized Keyword[] getKeywords() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        try {
            List keywords = dao.getKeywords(entityManager);
            
            return (Keyword[]) keywords.toArray(new Keyword[keywords.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Keywords", e);
            throw new EmfException("Could not get all Keywords");
        } finally {
            entityManager.close();
        }
    }

    public synchronized Country[] getCountries() throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List countries = dao.getCountries(entityManager);
            entityManager.close();

            return (Country[]) countries.toArray(new Country[countries.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Countries", e);
            throw new EmfException("Could not get all Countries");
        }
    }

    public synchronized Sector[] getSectors() throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List sectors = dao.getSectors(entityManager);
            entityManager.close();

            return (Sector[]) sectors.toArray(new Sector[sectors.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Sectors", e);
            throw new EmfException("Could not get all Sectors");
        }
    }

    public synchronized Sector obtainLockedSector(User owner, Sector sector) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Sector lockedSector = dao.obtainLockedSector(owner, sector, entityManager);
            entityManager.close();

            return lockedSector;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for sector: " + sector.getName() + " by owner: " + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for sector: " + sector.getName() + " by owner: "
                    + owner.getUsername());
        }
    }

    public synchronized Sector updateSector(Sector sector) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            if (!dao.canUpdate(sector, entityManager))
                throw new EmfException("The Sector name is already in use");

            Sector released = dao.updateSector(sector, entityManager);
            entityManager.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update sector: " + sector.getName(), e);
            throw new EmfException("The Sector name is already in use");
        }
    }

    public synchronized Sector releaseLockedSector(User user, Sector sector) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Sector released = dao.releaseLockedSector(user, sector, entityManager);
            entityManager.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for sector: " + sector.getName() + " by owner: " + sector.getLockOwner(),
                    e);
            throw new EmfException("Could not release lock for sector: " + sector.getName() + " by owner: "
                    + sector.getLockOwner());
        }
    }

    public synchronized DatasetType[] getDatasetTypes() throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List list = dao.getDatasetTypes(entityManager);
            entityManager.close();

            return (DatasetType[]) list.toArray(new DatasetType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all DatasetTypes", e);
            throw new EmfException("Could not get all DatasetTypes ");
        }
    }

    public synchronized DatasetType[] getDatasetTypes(BasicSearchFilter searchFilter) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List list = dao.getDatasetTypes(entityManager, searchFilter);
            entityManager.close();

            return (DatasetType[]) list.toArray(new DatasetType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all DatasetTypes", e);
            throw new EmfException("Could not get all DatasetTypes ");
        }
    }

    //searchFilter
    // DatasetType (limit to viewable dataset types)
    public synchronized DatasetType[] getDatasetTypes(int userId) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List list = dao.getDatasetTypes(userId, entityManager);

            entityManager.close();

            return (DatasetType[]) list.toArray(new DatasetType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all DatasetTypes", e);
            throw new EmfException("Could not get all DatasetTypes ");
        }
    }

    // DatasetType (limit to viewable dataset types)
    public synchronized DatasetType[] getLightDatasetTypes(int userId) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List list = dao.getLightDatasetTypes(userId, entityManager);

            entityManager.close();

            return (DatasetType[]) list.toArray(new DatasetType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all DatasetTypes", e);
            throw new EmfException("Could not get all DatasetTypes ");
        }
    }

    public synchronized DatasetType[] getLightDatasetTypes() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List<DatasetType> list = dao.getLightDatasetTypes(entityManager);

            return list.toArray(new DatasetType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all DatasetTypes", e);
            throw new EmfException("Could not get all DatasetTypes ");
        } finally {
            if (entityManager != null) entityManager.close();
        }
    }

    public synchronized DatasetType getLightDatasetType(String name) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getLightDatasetType(name, entityManager);
        } catch (HibernateException e) {
            LOG.error("Could not get DatasetType", e);
            throw new EmfException("Could not get DatasetType");
        } finally {
            entityManager.close();
        }
    }

    public synchronized DatasetType getDatasetType(String name) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getDatasetType(name, entityManager);
        } catch (HibernateException e) {
            LOG.error("Could not get DatasetType", e);
            throw new EmfException("Could not get DatasetType");
        } finally {
            entityManager.close();
        }
    }

    public synchronized DatasetType obtainLockedDatasetType(User user, DatasetType type) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        try {
            DatasetType locked = dao.obtainLockedDatasetType(user, type, entityManager);

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for DatasetType: " + type.getName(), e);
            throw new EmfException("Could not obtain lock for DatasetType: " + type.getName());
        } finally {
            entityManager.close();
        }
    }

    public synchronized DatasetType updateDatasetType(DatasetType type) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {

            if (!dao.canUpdate(type, entityManager))
                throw new EmfException("DatasetType name already in use");

            //validate INDICES keyword...
            XFileFormat xFileFormat = type.getFileFormat();
            Column[] cols = new Column[] { };
            if (xFileFormat != null) {
                cols = xFileFormat.cols();
            } else {
                TableFormat tableFormat = new FileFormatFactory(dbServer).tableFormat(type, true);
                if (tableFormat != null) cols = tableFormat.cols();
            }
            
            //validate indexes specified in keyword actually exist.
            if (cols != null) 
                dao.validateDatasetTypeIndicesKeyword(type, cols);
            
            //validate inline_comment_char keyword doesn't use a typical delimiter character -- comma or semi-colon
            for (KeyVal keyVal : type.getKeyVals()) {
                if (keyVal.getName().equals(Dataset.inline_comment_char))
                    if (keyVal.getValue() != null && !keyVal.getValue().trim().isEmpty() 
                            && (keyVal.getValue().trim().equals(",") || keyVal.getValue().trim().equals(";"))
                ) 
                            throw new EmfException("DatasetType keyword, " + Dataset.inline_comment_char + ", contains an invalid inline comment delimiter, comma (,) or semi-colon (;).");
            }

            //update FileFormat
            if (xFileFormat != null) {
                dao.update(xFileFormat, entityManager);
            }

            DatasetType locked = dao.updateDatasetType(type, entityManager);

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not update DatasetType. Name is already in use: " + type.getName(), e);
            throw new EmfException("DatasetType name already in use");
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            entityManager.close();
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
//                e.printStackTrace();
            }
        }
    }

    public synchronized DatasetType releaseLockedDatasetType(User user, DatasetType type) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            DatasetType locked = dao.releaseLockedDatasetType(user, type, entityManager);
            entityManager.close();

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock on DatasetType: " + type.getName(), e);
            throw new EmfException("Could not release lock on DatasetType: " + type.getName());
        }
    }

    public void deleteDatasetTypes(User owner, DatasetType[] types) throws EmfException {
        EntityManager entityManager = this.entityManagerFactory.createEntityManager();
        DbServer dbServer = dbServerFactory.getDbServer();
        
        try {
            if (owner.isAdmin()){
                for (int i =0; i<types.length; i++) {
                    checkIfUsedByCases(types[i], entityManager); 
                    checkIfUsedByDeletedDS(types[i], entityManager);
                    checkIfUsedByDatasets(types[i], entityManager);
                    checkIfUsedByModuleTypes(types[i], entityManager);
                    dao.removeUserExcludedDatasetType(types[i], dbServer);
                    dao.removeTableConsolidation(types[i], dbServer);
                    dao.removeDatasetTypes(types[i], entityManager);
                    
                    if (types[i].getFileFormat()!= null )
                        dao.removeXFileFormat(types[i].getFileFormat(), entityManager);
                }
            }
        } catch (Exception e) {          
            LOG.error("Error deleting dataset types. " , e);
            throw new EmfException("Error deleting dataset types. \n" + e.getMessage());
        } finally {
            entityManager.close(); 
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
//                e.printStackTrace();
            }
        }
    }
    
    private void checkIfUsedByCases(DatasetType type, EntityManager entityManager) throws EmfException {
        List list = null;

        // check if dataset is an input dataset for some cases (via the cases.cases_caseinputs table)
        list = entityManager.createQuery(
                "select CI.caseID from CaseInput as CI " + "where (CI.datasetType.id = "
                        + type.getId()+ ")").getResultList();

        if (list != null && list.size() > 0) {
            Case usedCase = dao.get(Case.class, "id", list.get(0), entityManager).get(0);
            throw new EmfException("Dataset type \" " + type.getName()+ "\" is used by case " + usedCase.getName() + ".");
        }
    }
    
    private void checkIfUsedByDeletedDS(DatasetType type, EntityManager entityManager) throws EmfException {
        List list = null;

        // check if dataset is an input dataset for some cases (via the cases.cases_caseinputs table)
        list = entityManager.createQuery(
                "select DS.id from EmfDataset as DS " + "where (lower(DS.status) like '%deleted%')"
                 + "and DS.datasetType.id = "
                        + type.getId()).getResultList();

        if (list != null && list.size() > 0) {
            EmfDataset deletedDS = dao.get(EmfDataset.class, "id", list.get(0), entityManager).get(0);
            throw new EmfException(" Cannot delete dataset type <"+ type.getName()
                    + ">. \n The following user have removed but not purged datasets of this type. \n" 
                    + "<" + deletedDS.getCreator() + ", " + deletedDS.getCreatorFullName() +">");
        }
    }
    
    private void checkIfUsedByDatasets(DatasetType type, EntityManager entityManager) throws EmfException {
        List list = null;

        // check if dataset is an input dataset for some cases (via the cases.cases_caseinputs table)
        list = entityManager.createQuery("select DS.name from EmfDataset as DS where (DS.datasetType.id = " + type.getId() + ")").getResultList();

        if (list != null && list.size() > 0) {
            StringBuilder message = new StringBuilder();
            message.append("Dataset type \"" + type.getName()+ "\" is used by:\n");
            int count = (list.size() <= 6) ? list.size() : 5;
            for(int i = 0; i < count; i++) {
                message.append("- dataset \"" + list.get(i) + "\"\n");
            }
            if (list.size() > count) message.append(String.format("(and %d more)\n", list.size() - count));
            throw new EmfException(message.toString());
        }
    }

    private void checkIfUsedByModuleTypes(DatasetType type, EntityManager entityManager) throws EmfException {
        List list = null;

        // check if dataset type is is used by any module type
        list = entityManager.createQuery("FROM ModuleTypeVersionDataset AS mtvd WHERE (mtvd.datasetType.id = " + type.getId() + ")").getResultList();

        if (list != null && list.size() > 0) {
            StringBuilder message = new StringBuilder();
            message.append("Dataset type \"" + type.getName() + "\" is used by:\n");
            int count = (list.size() <= 6) ? list.size() : 5;
            for(int i = 0; i < count; i++) {
                ModuleTypeVersionDataset mtvt = (ModuleTypeVersionDataset) list.get(i);
                message.append(String.format("- module type \"%s\" version %d \"%s\" placeholder \"%s\"\n",
                                             mtvt.getModuleTypeVersion().getModuleType().getName(),
                                             mtvt.getModuleTypeVersion().getVersion(),
                                             mtvt.getModuleTypeVersion().getName(),
                                             mtvt.getPlaceholderName()));
            }
            if (list.size() > count) message.append(String.format("(and %d more)\n", list.size() - count));
            throw new EmfException(message.toString());
        }
    }

    public synchronized Status[] getStatuses(String username) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List statuses = dao.getStatuses(username, entityManager);
            entityManager.close();

            return (Status[]) statuses.toArray(new Status[statuses.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Status messages", e);
            throw new EmfException("Could not get all Status messages");
        }
    }

    public synchronized FileDownload[] getFileDownloads(Integer userId) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List fileDownloads = fileDownloadDAO.getFileDownloads(userId, entityManager);
            entityManager.close();

            return (FileDownload[]) fileDownloads.toArray(new FileDownload[fileDownloads.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all FileDownloads", e);
            throw new EmfException("Could not get all FileDownloads");
        }
    }

    public synchronized FileDownload[] getUnreadFileDownloads(Integer userId) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List fileDownloads = fileDownloadDAO.getUnreadFileDownloads(userId, entityManager);
            entityManager.close();

            return (FileDownload[]) fileDownloads.toArray(new FileDownload[fileDownloads.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all FileDownloads", e);
            throw new EmfException("Could not get all FileDownloads");
        }
    }

    public void addFileDownload(FileDownload fileDownload) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            fileDownloadDAO.add(fileDownload, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not add FileDownload", e);
            throw new EmfException("Could not add FileDownload");
        } finally {
            entityManager.close();
        }
    }

    public void markFileDownloadsRead(Integer[] fileDownloadIds) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            fileDownloadDAO.markFileDownloadsRead(fileDownloadIds, entityManager);
//            entityManager.clear();
//            entityManager.flush();
                       
            entityManager.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add FileDownload", e);
            throw new EmfException("Could not add FileDownload");
        }
    }
    
    public void removeFileDownloads(Integer userId) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            fileDownloadDAO.removeFileDownloads(userId, entityManager);
            entityManager.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add FileDownload", e);
            throw new EmfException("Could not add FileDownload");
        }
    }
                
    public synchronized Project[] getProjects() throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List projects = dao.getProjects(entityManager);
            entityManager.close();

            return (Project[]) projects.toArray(new Project[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Projects", e);
            throw new EmfException("Could not get all Projects");
        }
    }

    public synchronized Project addProject(Project project) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            if (dao.nameUsed(project.getName(), Project.class, entityManager))
                throw new EmfException("Project name already in use");

            dao.add(project, entityManager);
            return (Project) dao.load(Project.class, project.getName(), entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not add new Project", e);
            throw new EmfException("Project name already in use");
        } finally {
            entityManager.close();
        }
    }

    public synchronized Region[] getRegions() throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List regions = dao.getRegions(entityManager);
            entityManager.close();

            return (Region[]) regions.toArray(new Region[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Regions", e);
            throw new EmfException("Could not get all Regions");
        }
    }
    
    public synchronized UserFeature[] getUserFeatures() throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List userFeatures = dao.getUserFeatures(entityManager);
            entityManager.close();

            return (UserFeature[]) userFeatures.toArray(new UserFeature[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all user features", e);
            throw new EmfException("Could not get all user features");
        }
    }

    public synchronized void addRegion(Region region) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            if (dao.nameUsed(region.getName(), Region.class, entityManager))
                throw new EmfException("Region name already in use");

            dao.add(region, entityManager);
            entityManager.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new Region", e);
            throw new EmfException("Region name already in use");
        }
    }

    public synchronized IntendedUse[] getIntendedUses() throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List regions = dao.getIntendedUses(entityManager);
            entityManager.close();

            return (IntendedUse[]) regions.toArray(new IntendedUse[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Intended Use", e);
            throw new EmfException("Could not get all Intended Use");
        }
    }

    public synchronized void addIntendedUse(IntendedUse intendedUse) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            if (dao.nameUsed(intendedUse.getName(), IntendedUse.class, entityManager))
                throw new EmfException("Intended use name already in use");

            dao.add(intendedUse, entityManager);
            entityManager.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new intended use", e);
            throw new EmfException("Intended use name already in use");
        }
    }

    public synchronized void addCountry(Country country) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            if (dao.nameUsed(country.getName(), Country.class, entityManager))
                throw new EmfException("The Country name is already in use");

            dao.add(country, entityManager);
            entityManager.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new country", e);
            throw new EmfException("Country name already in use");
        }
    }

    public synchronized void addDatasetType(DatasetType type) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {

            if (dao.nameUsed(type.getName(), DatasetType.class, entityManager))
                throw new EmfException("The DatasetType name is already in use");

            //validate INDICES keyword...
            XFileFormat xFileFormat = type.getFileFormat();
            Column[] cols = new Column[] { };
            if (xFileFormat != null) {
                if (xFileFormat.getId() == 0) {
                    xFileFormat = addFileFormat(xFileFormat);
                    type.setFileFormat(xFileFormat);
                }
                cols = xFileFormat.cols();
            } else {
                TableFormat tableFormat = new FileFormatFactory(dbServer).tableFormat(type, true);
                if (tableFormat != null) cols = tableFormat.cols();
            }
            if (cols != null) 
                dao.validateDatasetTypeIndicesKeyword(type, cols);
            
            // create keywords if needed
            if (type.getKeyVals().length > 0) {
                KeywordsDAO keywordsDAO = new KeywordsDAO();
                for (KeyVal keyVal : type.getKeyVals()) {
                    Keyword added = keywordsDAO.add(keyVal.getKeyword(), entityManager);
                    keyVal.setKeyword(added);
                }
            }
            
            dao.add(type, entityManager);
        } catch (Exception e) {
            LOG.error("Could not add DatasetType", e);
            throw new EmfException("Could not add DatasetType: " + e.getMessage());
        } finally {
            entityManager.close();
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
//                e.printStackTrace();
            }
        }
    }

    public synchronized XFileFormat addFileFormat(XFileFormat format) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        try {
            dao.add(format, entityManager);
            return (XFileFormat)dao.load(XFileFormat.class, format.getName(), entityManager);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Could not add new XFileFormat " + format.getName(), e);
            throw new EmfException("Could not add new XFileFormat " + format.getName() + ". " + e.getLocalizedMessage());
        } finally {
            if (entityManager != null)
                entityManager.close();
        }
    }

    public synchronized XFileFormat updateFileFormat(XFileFormat format) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            dao.update(format, entityManager);
            return (XFileFormat)dao.load(XFileFormat.class, format.getName(), entityManager);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Could not add new XFileFormat " + format.getName(), e);
            throw new EmfException("Could not add new XFileFormat " + format.getName() + ". " + e.getLocalizedMessage());
        } finally {
            if (entityManager != null)
                entityManager.close();
        }
    }

    // under construction
    public synchronized XFileFormat deleteFileFormat(XFileFormat format) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        try {
            dao.add(format, entityManager);
            return (XFileFormat)dao.load(XFileFormat.class, format.getName(), entityManager);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Could not add new XFileFormat " + format.getName(), e);
            throw new EmfException("Could not add new XFileFormat " + format.getName() + ". " + e.getLocalizedMessage());
        } finally {
            if (entityManager != null)
                entityManager.close();
        }
    }

    public synchronized GeoRegion addGeoRegion(GeoRegion newRegion) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        try {
            String name = newRegion.getName();
            String abbr = newRegion.getAbbreviation();
            
            if (name == null || name.trim().isEmpty())
                throw new EmfException("Region name cannot be null or empty.");
            
            List<?> num = entityManager.createQuery("SELECT COUNT(gr.id) from GeoRegion as gr where " +
                    "lower(gr.name) = '" + name.toLowerCase() + "'").getResultList();
            
            if (Integer.parseInt(num.get(0).toString()) > 0)
                throw new EmfException("Region name '" + name + "' has been used already.");
            
            if (abbr != null && abbr.trim().isEmpty()) 
                abbr = null;
            
            if (abbr != null) {
                num = entityManager.createQuery("SELECT COUNT(gr.id) from GeoRegion as gr where " +
                        "lower(gr.abbreviation) = '" + abbr.toLowerCase() + "'").getResultList();
                
                if (Integer.parseInt(num.get(0).toString()) > 0)
                    throw new EmfException("Abbreviation '" + abbr + "' has been used already.");
            }
            
            newRegion.setAbbreviation(abbr);
            dao.add(newRegion, entityManager);
            return (GeoRegion)dao.load(GeoRegion.class, newRegion.getName(), entityManager);
        } catch (Exception e) {
            LOG.error("Could not add new GeoRegion", e);
            String err = e.getMessage() == null ? "." : ": " + e.getMessage();
            err = err.length() > 50 ? err.substring(0, 49) : err;
            throw new EmfException("Could not add new GeoRegion" + err);
        } finally {
            if (entityManager != null)
                entityManager.close();
        }
    }
    
    public synchronized GeoRegion obtainLockedRegion(User owner, GeoRegion region) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        try {
            return dao.obtainLockedRegion(owner, region, entityManager);
        } catch (Exception e) {
            LOG.error("Could not obtain lock for region: " + region.getName() + " by owner: " + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for region: " + region.getName() + " by owner: "
                    + owner.getUsername());
        } finally {
            if (entityManager != null)
                entityManager.close();
        }
    }
    
    public GeoRegion updateGeoRegion(GeoRegion region, User user) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        try {
            return dao.updateGeoregion(region, user, entityManager);
        } catch (Exception e) {
            LOG.error("ERROR: updating GeoRegion (" + region.getName() + ").", e);
            throw new EmfException("Cannot update region object. " + e.getMessage() + ".");
        } finally {
            if (entityManager != null)
                entityManager.close();
        }
    }
    
    public synchronized void addSector(Sector sector) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            if (dao.nameUsed(sector.getName(), Sector.class, entityManager))
                throw new EmfException("Sector name already in use");

            dao.add(sector, entityManager);
            entityManager.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new Sector.", e);
            throw new EmfException("Sector name already in use");
        }
    }

    public synchronized DatasetNote[] getDatasetNotes(int datasetId) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List datasetNotes = dao.getDatasetNotes(datasetId, entityManager);
            entityManager.close();

            return (DatasetNote[]) datasetNotes.toArray(new DatasetNote[datasetNotes.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Notes of dataset" + datasetId , e);
            throw new EmfException("Could not get all Notes of dataset " + datasetId);
        }
    }
    
    public synchronized Note[] getNameContainNotes(String nameContains) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List notes = dao.getNotes(entityManager, nameContains);
            entityManager.close();

            return (Note[]) notes.toArray(new Note[notes.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Notes", e);
            throw new EmfException("Could not get all Notes");
        }
    }
    
    public synchronized Note[] getNotes(int[] noteIds) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List<Note> notes = new ArrayList<Note>();
            for (int id : noteIds){
               notes.add((Note) dao.current(Integer.valueOf(id), Note.class, entityManager)); 
            }
            entityManager.close();

            return notes.toArray(new Note[notes.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Notes", e);
            throw new EmfException("Could not get all Notes");
        }
    }

    public synchronized void addDatasetNote(DatasetNote datasetNote) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            //check has been done on the client side
//            if (dao.nameUsed(note.getName(), Note.class, entityManager))
//                throw new EmfException("Note name already in use");

            dao.add(datasetNote, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not add new note", e);
            throw new EmfException("Note name already in use");
        } finally {
            entityManager.close();
        }
    }

//    public synchronized void addNotesB(DatasetNote[] notes) throws EmfException {
//        for (int i = 0; i < notes.length; i++) {
//            this.addDatasetNote(notes[i]);
//        }
//    }

    public synchronized void addDatasetNotes(DatasetNote[] dsNotes) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        try {
            for (int i = 0; i < dsNotes.length; i++) {
                dao.add(dsNotes[i], entityManager);
                entityManager.clear();
            }
        } catch (Exception e) {
            LOG.error("Could not add new note", e);
            throw new EmfException("Adding new DatasetNote failed: " + e.getMessage());
        } finally {
            entityManager.close();
        }

    }

    public synchronized NoteType[] getNoteTypes() throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List notetypes = dao.getNoteTypes(entityManager);
            entityManager.close();

            return (NoteType[]) notetypes.toArray(new NoteType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Note Types", e);
            throw new EmfException("Could not get all Note Types");
        }
    }

    public synchronized Revision obtainLockedRevision(User owner, Revision revision) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Revision lockedRevision = dao.obtainLockedRevision(owner, revision, entityManager);
            entityManager.close();

            return lockedRevision;
        } catch (RuntimeException e) {
            String message = "Could not obtain lock for revision with id: " + revision.getId() + " by owner: " + owner.getUsername();
            LOG.error(message, e);
            throw new EmfException(message);
        }
    }

    public synchronized Revision releaseLockedRevision(User user, Revision revision) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Revision released = dao.releaseLockedRevision(user, revision, entityManager);
            entityManager.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for revision: " + revision.getId() + " by owner: " + revision.getLockOwner(),
                    e);
            throw new EmfException("Could not release lock for revision: " + revision.getId() + " by owner: "
                    + revision.getLockOwner());
        }
    }

    public synchronized Revision updateRevision(Revision revision) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

                Revision released = dao.updateRevision(revision, entityManager);

            entityManager.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update revisions", e);
            throw new EmfException("One of the revisions is already in use");
        }
    }

    public synchronized Revision[] getRevisions(int datasetId) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List revisions = dao.getRevisions(datasetId, entityManager);
            entityManager.close();

            return (Revision[]) revisions.toArray(new Revision[revisions.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Revisions", e);
            throw new EmfException("Could not get all Revisions");
        }
    }

    public synchronized void addRevision(Revision revision) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            dao.add(revision, entityManager);
            entityManager.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add revision", e);
            throw new EmfException("Could not add revision");
        }
    }

    public synchronized Pollutant[] getPollutants() throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List pollutants = dao.getPollutants(entityManager);
            entityManager.close();

            return (Pollutant[]) pollutants.toArray(new Pollutant[pollutants.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all pollutants", e);
            throw new EmfException("Could not get all pollutants");
        }
    }

    public synchronized void addPollutant(Pollutant pollutant) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            if (dao.nameUsed(pollutant.getName(), Pollutant.class, entityManager))
                throw new EmfException("Pollutant name already in use");

            dao.add(pollutant, entityManager);
            entityManager.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new pollutant.", e);
            throw new EmfException("Pollutant name already in use");
        }
    }

    public synchronized SourceGroup[] getSourceGroups() throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            List sourcegrp = dao.getSourceGroups(entityManager);
            entityManager.close();

            return (SourceGroup[]) sourcegrp.toArray(new SourceGroup[sourcegrp.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all source groups", e);
            throw new EmfException("Could not get all source groups");
        }
    }

    public synchronized void addSourceGroup(SourceGroup sourcegrp) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            if (dao.nameUsed(sourcegrp.getName(), SourceGroup.class, entityManager))
                throw new EmfException("Source group name already in use");

            dao.add(sourcegrp, entityManager);
            entityManager.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add new source group.", e);
            throw new EmfException("Source group name already in use");
        }
    }

    public synchronized File[] getFiles(File[] dir) throws EmfException {
        try {
            if (dir[0] == null) {
                EmfServerFileSystemView fsv = new EmfServerFileSystemView();
                return fsv.getDefaultDirectory().listFiles();
            }

            return dir[0].listFiles();
        } catch (RuntimeException e) {
            LOG.error("Could not list files.", e);
            throw new EmfException("Could not list files. " + e.getMessage());
        }
    }

    public synchronized GeoRegion[] getGeoRegions() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List<GeoRegion> results = dao.getGeoRegions(entityManager);
            return results.toArray(new GeoRegion[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Grids", e);
            throw new EmfException("Could not get all Grids");
        } finally {
            entityManager.close();
        }
    }
    
    public synchronized RegionType[] getRegionTypes() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
       
        try {
            List<RegionType> results = dao.getRegionTypes(entityManager);
            return results.toArray(new RegionType[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all RegionTypes", e);
            throw new EmfException("Could not get all RegionTypes");
        } finally {
            if (entityManager != null)
                entityManager.close();
        }
    }
    
    public synchronized EmfFileInfo createNewFolder(String folder, String subfolder) throws EmfException {
        try {
            if (folder == null || folder.trim().isEmpty())
                return null;

            if (subfolder == null || subfolder.trim().isEmpty())
                return null;

            File subdir = new File(folder, subfolder);
            
            if (subdir.exists())
                throw new EmfException("Subfolder " + subfolder + " already exists.");
            
            if (subdir.mkdirs()) {
                setDirsWritable(new File(folder), subdir);
                return EmfFileSerializer.convert(subdir);
            }

            return null;
        } catch (Exception e) {
            LOG.error("Could not create new folder " + folder + File.separator + subfolder + ". ", e);
            throw new EmfException(e.getMessage());
        }
    }
    
    private void setDirsWritable(File base, File dir) {
        while (dir != null) {
            try {
                dir.setWritable(true, false);
            } catch (Exception e) {
                return;
            }
            
            dir = dir.getParentFile();
            
            if (dir.compareTo(base) == 0)
                return;
        }
    }

    public synchronized EmfFileInfo getDefaultDir() throws EmfException {
        try {
            //NOTE: FileSystemView doesn't work well on Linux platform
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            return EmfFileSerializer.convert(fsv.getDefaultDirectory());
        } catch (IOException e) {
            LOG.error("Could not get default directory.", e);
            throw new EmfException("Could not get get default directory.");
        }
    }

    public synchronized EmfFileInfo getHomeDir() throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            return EmfFileSerializer.convert(fsv.getHomeDirectory());
        } catch (IOException e) {
            LOG.error("Could not get home directory.", e);
            throw new EmfException("Could not get home directory.");
        }
    }

    public synchronized EmfFileInfo[] getRoots() throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            File[] roots = fsv.getRoots();
            return getFileInfos(roots);
        } catch (IOException e) {
            LOG.error("Could not get file system roots.", e);
            throw new EmfException("Could not file system roots.");
        }
    }

    private synchronized EmfFileInfo[] getFileInfos(File[] roots) throws IOException {
        EmfFileInfo[] infos = new EmfFileInfo[roots.length];

        for (int i = 0; i < infos.length; i++)
            infos[i] = EmfFileSerializer.convert(roots[i]);

        return infos;
    }

    public synchronized boolean isRoot(EmfFileInfo fileInfo) throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            return fsv.isRoot(EmfFileSerializer.convert(fileInfo));
        } catch (Exception e) {
            throw new EmfException("Could not determine roots.");
        }
    }

    public synchronized boolean isFileSystemRoot(EmfFileInfo fileInfo) throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            return fsv.isFileSystemRoot(EmfFileSerializer.convert(fileInfo));
        } catch (Exception e) {
            throw new EmfException("Could not determine roots.");
        }
    }

    public synchronized EmfFileInfo getChild(EmfFileInfo file, String child) throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            File childFile = fsv.getChild(EmfFileSerializer.convert(file), child);
            checkDirPermission(childFile);
            return EmfFileSerializer.convert(childFile);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public synchronized EmfFileInfo getParentDirectory(EmfFileInfo file) throws EmfException {
        try {
            EmfServerFileSystemView fsv = new EmfServerFileSystemView();
            File parentFile = fsv.getParentDirectory(EmfFileSerializer.convert(file));
            checkDirPermission(parentFile);
            return EmfFileSerializer.convert(parentFile);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public synchronized EmfFileInfo[] getSubdirs(EmfFileInfo dir) throws EmfException {
        try {
            EmfFileInfo gooddir = correctEmptyDir(dir);
            //if (currentDirectory != null && gooddir.getAbsolutePath().equals(currentDirectory.getAbsolutePath())) {
            //    return this.subdirs != null ? this.subdirs : new EmfFileInfo[0];
            //}
            
            currentDirectory = gooddir;
            File currentdirFile = new File(currentDirectory.getAbsolutePath());
            checkDirPermission(currentdirFile);
            listDirsAndFiles(currentdirFile.listFiles(), currentdirFile, "*");

            return this.subdirs != null ? this.subdirs : new EmfFileInfo[0];
        } catch (Exception e) {
            LOG.warn("Could not get subdirectories of  " + dir.getAbsolutePath() + ": " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    private void checkDirPermission(File dir) throws EmfException {
        if (dir.exists() && dir.isDirectory() && dir.listFiles() == null)
            throw new EmfException("Tomcat doesn't have read permission.");
    }

    private synchronized EmfFileInfo correctEmptyDir(EmfFileInfo dir) throws IOException {
        boolean resetPath = false;

        if (dir == null || dir.getAbsolutePath() == null || dir.getAbsolutePath().trim().equals(""))
            resetPath = true;
        else {
            File f = new File(dir.getAbsolutePath());
            
            if (f.isFile())
                return EmfFileSerializer.convert(f.getParentFile());
            
            if (!f.exists())
                resetPath = true;
        }

        if (resetPath) {
            if (File.separatorChar == '/') {
                dir.setAbsolutePath("/");
                dir.setName("/");
            } else {
                dir.setAbsolutePath("C:\\");
                dir.setName("C:\\");
            }
        }

        return dir;
    }

    private synchronized void listDirsAndFiles(File[] files, File cur, String filter) throws IOException {
        List<EmfFileInfo> subdirsOfCurDir = new ArrayList<EmfFileInfo>();
        List<EmfFileInfo> filesOfCurDir = new ArrayList<EmfFileInfo>();
        EmfFileInfo curInfo = EmfFileSerializer.convert(cur);
        curInfo.setName(".");
        subdirsOfCurDir.add(0, curInfo);

        EmfFileInfo parentdir = EmfFileSerializer.convert(cur);
        parentdir.setName("..");
        boolean isRoot = false;

        // if it's the root, you don't have ..
        if (File.separatorChar == '/') {
            if (cur.getAbsolutePath().equals("/"))
                isRoot = true;
        } else { // Windows
            if (cur.getAbsolutePath().length() == 3)
                isRoot = true;
        }

        if (!isRoot) {
            parentdir = EmfFileSerializer.convert(cur.getParentFile());
            parentdir.setName("..");
            subdirsOfCurDir.add(1, parentdir);
        }

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                subdirsOfCurDir.add(EmfFileSerializer.convert(files[i]));
            } else {
                filesOfCurDir.add(EmfFileSerializer.convert(files[i]));
            }
        }

        this.files = getFileinfosFromPattern(filesOfCurDir.toArray(new EmfFileInfo[0]), filter);
        this.subdirs = subdirsOfCurDir.toArray(new EmfFileInfo[0]);
    }

    public synchronized EmfFileInfo[] getEmfFileInfos(EmfFileInfo dir, String filter) throws EmfException {
        try {
            EmfFileInfo gooddir = correctEmptyDir(dir);
            //if (currentDirectory != null && gooddir.getAbsolutePath().equals(currentDirectory.getAbsolutePath())) {
            //    return this.files == null ? new EmfFileInfo[0] : this.files;
            //}

            currentDirectory = gooddir;
            File currentdirFile = new File(currentDirectory.getAbsolutePath());
            listDirsAndFiles(currentdirFile.listFiles(), currentdirFile, filter);

            return this.files != null ? this.files : new EmfFileInfo[0];
        } catch (Exception e) {
            LOG.error("Could not list files.", e);
            throw new EmfException("Could not list files. " + e.getMessage());
        }
    }
    
    private synchronized EmfFileInfo[] getFileinfosFromPattern(EmfFileInfo[] fileInfos, String pattern) throws EmfException {
        try {
            if (pattern == null || fileInfos.length == 0)
                return fileInfos;
                
            String pat = pattern.trim();
            
            if (pat.length() == 0)
                return fileInfos;
            
            File directory = new File(fileInfos[0].getParent());
            EmfFilePatternMatcher fpm = new EmfFilePatternMatcher(directory, pat);
            EmfFileInfo[] matchedNames = fpm.getMatched(fileInfos);
            if (matchedNames.length == 0)
                throw new EmfException("No files found for pattern '" + pattern + "'");

            return matchedNames;
        } catch (Exception e) {
            throw new EmfException("Cannot apply pattern.");
        }
    }

    public void copyQAStepTemplates(User user, QAStepTemplate[] templates, int[] datasetTypeIds, boolean replace) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        String datasetTypeNameList = "";
        try {
            DatasetType[] datasetTypes = new DatasetType[datasetTypeIds.length];
            //get lock first, if you can't then throw an error
            for (int i = 0; i < datasetTypeIds.length; i++) {
                int datasetTypeId = datasetTypeIds[i];
                //get lock on dataset type so we can update it...
                DatasetType datasetType = dao.obtainLockedDatasetType(user, dao.getDatasetType(datasetTypeId, entityManager), entityManager);
                if (!datasetType.isLocked(user)) throw new EmfException("Could not copy QA Step Templates to " + datasetType.getName() + " its locked by " + datasetType.getLockOwner() + ".");
                datasetTypes[i] = datasetType;
            }
            int i = 0;
            for (DatasetType datasetType : datasetTypes) {
                ++i;
                QAStepTemplate[] existingQaStepTemplates = datasetType.getQaStepTemplates();
                boolean exists = false;
                //add qa templates to dataset type
                for (QAStepTemplate template : templates) {
                    exists = false;
                    //check if one with the same name already exists
                    for (QAStepTemplate existingTemplate : existingQaStepTemplates) {
                        if (existingTemplate.getName().equals(template.getName())) {
                            exists = true;
                            //if replacing, then remove existing template
                            if (replace) datasetType.removeQaStepTemplate(existingTemplate);
                        }
                    }
                    //if not replacing, then add "Copy of " in front of the name
                    if (exists && !replace) {
                        String newName = "Copy of " + template.getName();
                        //check if one with the same name already exists
                        for (QAStepTemplate existingQAStepTemplate : existingQaStepTemplates) {
                            if (existingQAStepTemplate.getName().equals(newName)) {
                                newName = "Copy of " + newName;
                            }
                        }
                        template.setName(newName);
                    }
                    datasetType.addQaStepTemplate(template);
                }
                //update the dataset type
                dao.updateDatasetType(datasetType, entityManager);
                datasetTypeNameList += (i > 1 ? ", " : "") + datasetType.getName();
            }

            Status endStatus = new Status();
            endStatus.setUsername(user.getUsername());
            endStatus.setType("CopyQAStepTemplate");
            endStatus.setMessage("Copied " + templates.length + " QA Step Templates to Dataset Types: " + datasetTypeNameList + ".");
            endStatus.setTimestamp(new Date());

            new StatusDAO(entityManagerFactory).add(endStatus);

        } catch (RuntimeException e) {
            LOG.error("Could not copy QAStepTemplates to Dataset Types.", e);
            throw new EmfException("Could not copy QA Step Templates to Dataset Types. " + e.getMessage());
        } finally {
            //release lock on dataset type
            for (int datasetTypeId : datasetTypeIds) {
                dao.releaseLockedDatasetType(user, dao.getDatasetType(datasetTypeId, entityManager), entityManager);
            }
            entityManager.close();
        }
    }

    public void addDatasetTypeWithFileFormat(DatasetType dstype, XFileFormat format, String formatFile)
            throws EmfException {
        File file = new File(formatFile);
        
        if (file == null || !file.exists())
            throw new EmfException("Format definition file " + formatFile + " doesn't exist.");
        CSVFileReader reader = null;
        try {
            reader = new CSVFileReader(file);
            List<Column> colObjs = new ArrayList<Column>();
            SqlDataTypes types = DbServerFactory.get().getDbServer().getSqlDataTypes();
            
            //NOTE: currently the format definition file has to follow this column sequence:
            // name,type,default value,mandatory,description,formatter,constraints,width,spaces,fixformat start,fixformat end
            int nRequiredFormatCols = 11;
            int line = 1;
            for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
                String[] data = record.getTokens();
                if (data.length != nRequiredFormatCols ) {
                    throw new EmfException("Format definition file must have " + nRequiredFormatCols + " cols");     
                }
                String type = getType(data[1], types, data[7]); //get sql data type
                if (type.contains("error:")){
                    int index = type.indexOf(":");
                    throw new EmfException("Line " + line + " of "+ type.substring(index+1));
                }
                // mandatory value has to be true or false
                if ( !data[3].equalsIgnoreCase("true") && !data[3].equalsIgnoreCase("false") ) 
                    throw new EmfException("Line " + line + " of the Format Definition File has an unrecognized boolean value: "+data[3]);
                colObjs.add(new Column(data[0], type, data[2], data[3], data[4],
                        data[5], data[6], data[7], data[8], data[9], data[10]));
                line++;
            }
            
            reader.close();
            
            format.setColumns(colObjs.toArray(new Column[0]));
            
            XFileFormat loadedFormat = addFileFormat(format);
            dstype.setFileFormat(loadedFormat);
            addDatasetType(dstype);
           
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }finally{
            try {
                reader.close();
            } catch (IOException e) {
                throw new EmfException(e.getMessage());
            } 
        }
        
    }

    private String getType(String type, SqlDataTypes types, String width) {
        if (type == null || type.trim().isEmpty())
            return " error: the Format Definition File is missing a data type";
        
        int widthValue = 0;
        
        try {
            widthValue = Integer.parseInt(width);
        } catch (Exception e) {
            int index1 = type.trim().indexOf('(');
            int index2 = type.trim().indexOf(')');
            
            if (index1 > 0 && index2 > 0 && widthValue == 0)
                widthValue = Integer.parseInt(type.substring(++index1, index2));
        }
        
        if (type.toLowerCase().startsWith("varchar") && widthValue > 0)
            return types.stringType(widthValue);
        
        if (type.toLowerCase().startsWith("text"))
            return types.text();
        
        if (type.toLowerCase().startsWith("real")
                || type.toLowerCase().startsWith("double precision"))
            return types.realType();
        
        if (type.toLowerCase().startsWith("date"))
            return types.timestamp();
        
        if (type.toLowerCase().startsWith("timestamp without time zone"))
            return types.timestamp();
        
        if (type.toLowerCase().trim().equals("timestamp"))
            return types.timestamp();
        
        // timestamp with time zone - not include here
        
        if (type.toLowerCase().startsWith("char"))
            return types.charType();
        
        if (type.toLowerCase().startsWith("bool"))
            return types.booleanType();
        
        if (type.toLowerCase().startsWith("int4"))
            return types.longType();
        
        if (type.toLowerCase().startsWith("int2"))
            return types.smallInt();
        
        if (type.toLowerCase().startsWith("int"))
            return types.intType();
        
        if (type.toLowerCase().startsWith("serial"))
            return types.autoIncrement();
            
        return " error: the Format Definition File has an unrecognized data type: "+ type;
    }

    public synchronized void updateProject(Project project) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        dao.updateProject(project, entityManager);
        entityManager.close();
    }

}
