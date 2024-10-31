package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class FileDownloadDAO {

    private PooledExecutor threadPool;
    
    private HibernateFacade hibernateFacade;

    public FileDownloadDAO() {
        threadPool = createThreadPool();
        this.hibernateFacade = new HibernateFacade();
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

//    public FileDownloadDAO(EntityManagerFactory entityManagerFactory) {
//        this.entityManagerFactory = entityManagerFactory;
//    }

//    public void setSessionFactory(SessionFactory entityManagerFactory) {
//        this.entityManagerFactory = entityManagerFactory;
//      }

    private String getPropertyValue(String name, EntityManager entityManager) throws EmfException {
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//
//        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty(name, entityManager);
            return property != null ? property.getValue() : null;
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new EmfException("Could not get EMF property.");
//        } finally {
//            entityManager.close();
//        }
    }

    public String getDownloadExportFolder(EntityManager entityManager) throws EmfException {
        return getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_FOLDER, entityManager);
    }

    public String getDownloadExportRootURL(EntityManager entityManager) throws EmfException {
        return getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_ROOT_URL, entityManager);
    }

    public int getDownloadExportFileHoursToExpire(EntityManager entityManager) throws EmfException {
        return Integer.parseInt(getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_FILE_HOURS_TO_EXPIRE, entityManager));
    }

    public void add(FileDownload fileDownload, EntityManager entityManager) throws EmfException  {
        //determine the size of the file
        File file = new File(fileDownload.getAbsolutePath());
        if (!file.exists()) 
            throw new EmfException("File to download doesn't exist on the server.");
        fileDownload.setSize(file.length());

        hibernateFacade.executeInsideTransaction(em -> {
            em.persist(fileDownload);
        }, entityManager);
         
        try {
            RemoveDownloadFilesTask task = new RemoveDownloadFilesTask(getDownloadExportFolder(entityManager), getDownloadExportFileHoursToExpire(entityManager));
            threadPool.execute(new GCEnforcerTask("Remove Downloaded Files Task", task));
            try {
                RemoveUploadFilesTask task2 = new RemoveUploadFilesTask();
                threadPool.execute(new GCEnforcerTask("Remove Uploaded Files Task", task2));
            } catch (Exception e) {
                //suppress all errors this shouldn't stop the process from working....
                //throw new EmfException(e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            //suppress all errors this shouldn't stop the process from working....
            //throw new EmfException(e.getMessage());
            e.printStackTrace();
        }
   }
    
    public void add(User user, Date dateAdded, String fileName, String type, Boolean overwrite, EntityManager entityManager) throws EmfException {
        
        String username = user.getUsername();
        
        FileDownload fileDownload = new FileDownload();
        fileDownload.setUserId(user.getId());
        fileDownload.setType(type);
        fileDownload.setTimestamp(dateAdded);
        fileDownload.setAbsolutePath(getDownloadExportFolder(entityManager) + "/" + username + "/" + fileName);
        fileDownload.setUrl(getDownloadExportRootURL(entityManager) + "/" + username + "/" + fileName);
        fileDownload.setOverwrite(overwrite);

        //persist record
        add(fileDownload, entityManager);
    }

    public List<FileDownload> getFileDownloads(Integer userId, EntityManager entityManager) {
//        return this.getHibernateTemplate().find(
//                "from FileDownload as FD where "
//                        + " FD.userId=?", userId);
        return entityManager
            .createQuery(
                    "Select FD from FileDownload as FD where "
                            + " FD.userId = "
                            + userId, FileDownload.class) 
            .getResultList();
    }

    public List<FileDownload> getUnreadFileDownloads(Integer userId, EntityManager entityManager) {
//        return this.getHibernateTemplate().find(
//                "from FileDownload as FD where FD.read = false "
//                        + " and FD.userId=?", userId);
        return entityManager
            .createQuery(
                    "Select FD from FileDownload as FD where "
                    + " FD.userId = " + userId
                    + " and FD.read = false", FileDownload.class) 
            .getResultList();
    }

    //mark filedownloads as read state
    public void markFileDownloadsRead(Integer[] fileDownloadIds, EntityManager entityManager) {
        String fileDownloadIdList = "";//Arrays.toString(fileDownloadIds);
        for (int i = 0; i < fileDownloadIds.length; i++)
            fileDownloadIdList += (fileDownloadIdList.length() > 0 ? "," : "") + fileDownloadIds[i];
        if (fileDownloadIdList.length() > 0) {
            String hqlUpdate = "update emf.file_downloads set is_read = true where id in (" + fileDownloadIdList + ")";

            hibernateFacade.executeInsideTransaction(em -> {
                em.createNativeQuery(hqlUpdate).executeUpdate();
            }, entityManager);
        }
    }

    //removes all filedownloads
    public void removeFileDownloads(Integer userId, EntityManager entityManager) {
        String hqlDelete = "delete FileDownload FD where FD.userId = :userId";
        hibernateFacade.executeInsideTransaction(em -> {
            entityManager.createQuery(hqlDelete).setParameter("userId", userId).executeUpdate();
        }, entityManager);
    }
}
