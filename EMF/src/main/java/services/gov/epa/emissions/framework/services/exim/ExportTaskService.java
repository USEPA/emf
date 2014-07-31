package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;

public interface ExportTaskService {

    void init(User user, File file, EmfDataset dataset, Services services, AccessLog accesslog, String rowFilters,
            String colOrders, DbServerFactory dbFactory, HibernateSessionFactory sessionFactory, Version version,
            EmfDataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition, boolean download,
            String taskId, String submitterId);

    void run();

    String[] quickRunExternalExport();

    boolean fileExists();

    boolean isExternal();

    String getDownloadExportRootURL();

    String getDownloadExportFolder();

    String getPropertyValue(String name);

    File getFile();

    EmfDataset getDataset();

    Version getVersion();

}
