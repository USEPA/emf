package gov.epa.emissions.framework.services.fast.shapefile;

import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;

import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ExportFastOutputToShapeFile {

    private PooledExecutor threadPool;

    private DbServerFactory dbServerFactory;

    private EntityManagerFactory entityManagerFactory;

    private static Log LOG = LogFactory.getLog(ExportFastOutputToShapeFile.class);

    private String pollutant;

    private String userName;

    private String dirName;

    private int datasetId;

    private int gridId;

    private int datasetVersion;

    public ExportFastOutputToShapeFile(int datasetId, int datasetVersion, int gridId, String userName, String dirName, String pollutant,
            DbServerFactory dbServerFactory, EntityManagerFactory entityManagerFactory,
            PooledExecutor threadPool) {
//        this(step, dbServerFactory,
//            user, entityManagerFactory,
//            threadPool);
        this.datasetId = datasetId;
        this.datasetVersion = datasetVersion;
        this.gridId = gridId;
        this.userName = userName;
        this.dirName = dirName;
        this.pollutant = pollutant;
        this.dbServerFactory = dbServerFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.threadPool = threadPool;
    }

    public void export() throws EmfException {
        ExportFastOutputToShapeFileTask task = new ExportFastOutputToShapeFileTask(datasetId, datasetVersion, gridId, userName, dirName, pollutant,
                dbServerFactory, entityManagerFactory);
        try {
            threadPool.execute(new GCEnforcerTask("Export Dataset Id : " + datasetId, task));
        } catch (InterruptedException e) {
            LOG.error("Error while exporting a Dataset Id : " + datasetId, e);
            throw new EmfException(e.getMessage());
        }
    }
}
