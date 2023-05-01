package gov.epa.emissions.framework.services.dao.impl;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.FileDownload;
import gov.epa.emissions.framework.services.basic.RemoveDownloadFilesTask;
import gov.epa.emissions.framework.services.dao.FileDownloadDao;
import gov.epa.emissions.framework.services.persistence.AbstractJpaDao;
import gov.epa.emissions.framework.services.persistence.IGenericDao;
import gov.epa.emissions.framework.services.spring.WebApplicationContextUtils;

@Repository(value = "fileDownloadDao")
//@Scope("prototype")
public class FileDownloadDaoImpl extends AbstractJpaDao<FileDownload> implements FileDownloadDao {

    IGenericDao<EmfProperty> emfPropertyDao;

    public FileDownloadDaoImpl() {
        super();
        setClazz(FileDownload.class);
    }

    @Autowired
    public void setEmfPropertyDao(IGenericDao<EmfProperty> daoToSet) {
        emfPropertyDao = daoToSet;
        emfPropertyDao.setClazz(EmfProperty.class);
    }

    private String getPropertyValue(String name) {
        try {
            EmfProperty property = emfPropertyDao.findByName(name);
            return property != null ? property.getValue() : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDownloadExportFolder() {
        return getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_FOLDER);
    }

    public String getDownloadExportRootURL() {
        return getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_ROOT_URL);
    }

    public int getDownloadExportFileHoursToExpire() {
        return Integer.parseInt(getPropertyValue(EmfProperty.DOWNLOAD_EXPORT_FILE_HOURS_TO_EXPIRE));
    }

    @Transactional("transactionManager")
    @Override
    public void add(FileDownload fileDownload) throws EmfException  {
        //determine the size of the file
        File file = new File(fileDownload.getAbsolutePath());
        if (!file.exists()) 
            throw new EmfException("File to download doesn't exist on the server.");
        fileDownload.setSize(file.length());

        try {
            create(fileDownload);
        } catch (RuntimeException e) {
            //
        } 
         
        try {
            ThreadPoolTaskExecutor taskExecutor = WebApplicationContextUtils.getInstance().getApplicationContext().getBean("poolTaskExecutor", ThreadPoolTaskExecutor.class);
            RemoveDownloadFilesTask removeDownloadFilesTask = WebApplicationContextUtils.getInstance().getApplicationContext().getBean("removeDownloadFilesTask", RemoveDownloadFilesTask.class);

            taskExecutor.execute(removeDownloadFilesTask);

        } catch (Exception e) {
            //suppress all errors this shouldn't stop the process from working....
            //throw new EmfException(e.getMessage());
            e.printStackTrace();
        }
   }
    
    @Transactional("transactionManager")
    @Override
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

    @Override
    public List<FileDownload> getFileDownloads(Integer userId) {
        return entityManager
                .createQuery("from FileDownload as FD where "
                + " FD.userId = :userId", FileDownload.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<FileDownload> getUnreadFileDownloads(Integer userId) {
        return entityManager
                .createQuery("from FileDownload as FD where "
                + " FD.userId = :userId" 
                + " and FD.read = false", FileDownload.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    //mark filedownloads as read state
    @Transactional("transactionManager")
    @Override
    public void markFileDownloadsRead(Integer[] fileDownloadIds) {
        if (fileDownloadIds.length > 0) {
            try {
                CriteriaBuilder builder = entityManager.getCriteriaBuilder();
                CriteriaUpdate<FileDownload> update = builder.createCriteriaUpdate(FileDownload.class);

                Root<FileDownload> root = update.from(FileDownload.class);

                update
                    .set(root.get("read"), Boolean.TRUE)
                    .where(
                            root.get("id").in(fileDownloadIds)
                    );

                entityManager
                        .createQuery(update)
                        .executeUpdate();
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    //removes all filedownloads
    @Transactional("transactionManager")
    @Override
    public void removeFileDownloads(Integer userId) {
        String hqlDelete = "delete FileDownload FD where FD.userId = :userId";
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaDelete<FileDownload> delete = builder.createCriteriaDelete(FileDownload.class);

            Root<FileDownload> root = delete.from(FileDownload.class);

            Expression<Boolean> filterPredicate = builder.equal(root.get("userId"), userId);

            delete
                .where(filterPredicate);

            entityManager
                    .createQuery(delete)
                    .executeUpdate();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
