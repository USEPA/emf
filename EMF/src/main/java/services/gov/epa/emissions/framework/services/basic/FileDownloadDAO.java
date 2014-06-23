package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.spring.AppConfig;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Repository;

@Repository
//@Scope("prototype")
public class FileDownloadDAO {

    private HibernateSessionFactory sessionFactory;

    public FileDownloadDAO() {
        this(HibernateSessionFactory.get());
    }

    public FileDownloadDAO(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

//    public void setSessionFactory(SessionFactory sessionFactory) {
//        this.sessionFactory = sessionFactory;
//      }

    private String getPropertyValue(String name) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty(name, session);
            return property != null ? property.getValue() : null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Could not get EMF property.");
        } finally {
            session.close();
        }
    }

    public String getDownloadExportFolder() throws EmfException {
        return getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_FOLDER);
    }

    public String getDownloadExportRootURL() throws EmfException {
        return getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_ROOT_URL);
    }

    public int getDownloadExportFileHoursToExpire() throws EmfException {
        return Integer.parseInt(getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_FILE_HOURS_TO_EXPIRE));
    }

    public void add(FileDownload fileDownload) throws EmfException  {
        //determine the size of the file
        File file = new File(fileDownload.getAbsolutePath());
        if (!file.exists()) 
            throw new EmfException("File to download doesn't exist on the server.");
        fileDownload.setSize(file.length());

        StatelessSession session = sessionFactory.getStatelessSession();
        Transaction tx = null;
        try {
            
            
            tx = session.beginTransaction();
            session.insert(fileDownload);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
         
        try {
            ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
            ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) context.getBean("poolTaskExecutor");

//                PrintTask2 printTask1 = (PrintTask2) context.getBean("printTask2");
//                printTask1.setName("Thread 1");
            taskExecutor.execute(new RemoveDownloadFilesTask(getDownloadExportFolder(), getDownloadExportFileHoursToExpire()));

        } catch (Exception e) {
            //suppress all errors this shouldn't stop the process from working....
            //throw new EmfException(e.getMessage());
        }
   }
    
    public void add(User user, Date dateAdded, String fileName, String type, Boolean overwrite) throws EmfException {
        
        String username = user.getUsername();
        
        FileDownload fileDownload = new FileDownload();
        fileDownload.setUserId(user.getId());
        fileDownload.setType(type);
        fileDownload.setTimestamp(dateAdded);
        fileDownload.setAbsolutePath(getDownloadExportFolder() + "/" + username + "/" + fileName);
        fileDownload.setUrl(getDownloadExportRootURL() + "/" + username + "/" + fileName);
        fileDownload.setOverwrite(overwrite);

        //persist record
        add(fileDownload);
    }
    
    public List getFileDownloads(Integer userId, Session session) {
//        return this.getHibernateTemplate().find(
//                "from FileDownload as FD where "
//                        + " FD.userId=?", userId);
        return session
        .createQuery(
                "Select FD from FileDownload as FD where "
                        + " FD.userId = "
                        + userId) 
        .list();
    }

    public List getUnreadFileDownloads(Integer userId, Session session) {
//        return this.getHibernateTemplate().find(
//                "from FileDownload as FD where FD.read = false "
//                        + " and FD.userId=?", userId);
        return session
        .createQuery(
                "Select FD from FileDownload as FD where "
                + " FD.userId = " + userId
                + " and FD.read = false") 
        .list();
    }

    //mark filedownloads as read state
    public void markFileDownloadsRead(Integer[] fileDownloadIds, Session session) {
        String fileDownloadIdList = "";//Arrays.toString(fileDownloadIds);
        for (int i = 0; i < fileDownloadIds.length; i++)
            fileDownloadIdList += (fileDownloadIdList.length() > 0 ? "," : "") + fileDownloadIds[i];
        if (fileDownloadIdList.length() > 0) {
            String hqlUpdate = "update emf.file_downloads set is_read = true where id in (" + fileDownloadIdList + ")";

            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.createSQLQuery(hqlUpdate).executeUpdate();
                tx.commit();
            } catch (HibernateException e) {
                tx.rollback();
                throw e;
            }
        
        
        }
    }

    //removes all filedownloads
    public void removeFileDownloads(Integer userId, Session session) {
        String hqlDelete = "delete FileDownload FD where FD.userId = :userId";
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.createQuery(hqlDelete).setInteger("userId", userId).executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
}
