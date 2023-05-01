package gov.epa.emissions.framework.services.dao;

import java.util.Date;
import java.util.List;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.FileDownload;
import gov.epa.emissions.framework.services.persistence.IGenericDao;

public interface FileDownloadDao extends IGenericDao<FileDownload> {

    String getDownloadExportFolder();

    String getDownloadExportRootURL();

    int getDownloadExportFileHoursToExpire();

    public void add(FileDownload fileDownload) throws EmfException;
    
    void add(User user, Date dateAdded, String fileName, String type, Boolean overwrite) throws EmfException;

    List<FileDownload> getFileDownloads(Integer userId);

    List<FileDownload> getUnreadFileDownloads(Integer userId);

    //mark filedownloads as read state
    void markFileDownloadsRead(Integer[] fileDownloadIds);

    void removeFileDownloads(Integer userId);
}
