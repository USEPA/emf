package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertyDao;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.Task;

import java.io.File;

public interface IExportTask extends Runnable, Comparable<Object> {

    public abstract boolean isEquivalent(Task task);

    public abstract void init(User user, File file, EmfDataset dataset, Services services, AccessLog accesslog,
            String rowFilters, String colOrders, DbServerFactory dbFactory, HibernateSessionFactory sessionFactory,
            Version version, EmfDataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition,
            boolean download);

    public abstract void run();

    public abstract String[] quickRunExternalExport();

    public abstract boolean fileExists();

    public abstract boolean isExternal();

    public abstract File getFile();

    public abstract EmfDataset getDataset();

    public abstract Version getVersion();

    public abstract String createId();

    public abstract String getTaskId();

    public abstract void setTaskId(String taskId);

    public abstract String getSubmitterId();

    public abstract void setSubmitterId(String submitterId);

    public abstract User getUser();

    public abstract boolean isReady();

    public abstract void setReady(boolean isReady);

    public abstract void setReadyTrue();

    public abstract void setReadyFalse();

    public abstract void setEmfPropertyDao(EmfPropertyDao emfPropertyDao);

    String getPropertyValue(String name);

    String getDownloadExportFolder();

    String getDownloadExportRootURL();
}