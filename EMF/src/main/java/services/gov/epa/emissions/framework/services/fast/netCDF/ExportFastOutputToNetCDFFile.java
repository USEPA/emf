package gov.epa.emissions.framework.services.fast.netCDF;

import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExportFastOutputToNetCDFFile {

    private PooledExecutor threadPool;

    private DbServerFactory dbServerFactory;

    private HibernateSessionFactory sessionFactory;

    private static Log LOG = LogFactory.getLog(ExportFastOutputToNetCDFFile.class);

    private String pollutant;

    private String userName;

    private String dirName;

    private int datasetId;

    private int gridId;

    private int datasetVersion;

    public ExportFastOutputToNetCDFFile(int datasetId, int datasetVersion, int gridId, String userName, String dirName, String pollutant,
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory,
            PooledExecutor threadPool) {
//        this(step, dbServerFactory,
//            user, sessionFactory,
//            threadPool);
        this.datasetId = datasetId;
        this.datasetVersion = datasetVersion;
        this.gridId = gridId;
        this.userName = userName;
        this.dirName = dirName;
        this.pollutant = pollutant;
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
        this.threadPool = threadPool;
    }

    public void export() throws EmfException {
        ExportFastOutputToNetCDFFileTask task = new ExportFastOutputToNetCDFFileTask(datasetId, datasetVersion, gridId, userName, dirName, pollutant,
                dbServerFactory, sessionFactory);
        try {
            threadPool.execute(new GCEnforcerTask("Export Dataset Id : " + datasetId, task));
        } catch (InterruptedException e) {
            LOG.error("Error while exporting a Dataset Id : " + datasetId, e);
            throw new EmfException(e.getMessage());
        }
    }
}
