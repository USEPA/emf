package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.netCDF.ExportFastOutputToNetCDFFile;
import gov.epa.emissions.framework.services.fast.shapefile.ExportFastOutputToShapeFile;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class FastServiceImpl implements FastService {

    private static Log LOG = LogFactory.getLog(FastServiceImpl.class);

    private PooledExecutor threadPool;

    private EntityManagerFactory entityManagerFactory;

    protected DbServerFactory dbServerFactory;

    private FastDAO dao;

    public FastServiceImpl() throws Exception {
        init(JpaEntityManagerFactory.get(), DbServerFactory.get());
    }

    public FastServiceImpl(EntityManagerFactory entityManagerFactory, DbServerFactory dbServerFactory) throws Exception {
        init(entityManagerFactory, dbServerFactory);
    }

    private synchronized void init(EntityManagerFactory entityManagerFactory, DbServerFactory dbServerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.dbServerFactory = dbServerFactory;
        dao = new FastDAO(dbServerFactory, entityManagerFactory);
        threadPool = createThreadPool();

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

    private <T> T executeDaoCommand(AbstractDaoCommand<T> daoCommand) throws EmfException {

        daoCommand.setSessionFactory(this.entityManagerFactory);
        daoCommand.setLog(LOG);
        return daoCommand.execute().getReturnValue();
    }

    public synchronized FastRun[] getFastRuns() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRun[]>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                List<FastRun> fastRuns = dao.getFastRuns(entityManager);
                this.setReturnValue(fastRuns.toArray(new FastRun[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve all Fast runs.";
            }
        });
    }

    public synchronized FastRun[] getFastRuns(final int gridId) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRun[]>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                List<FastRun> fastRuns = dao.getFastRuns(gridId, entityManager);
                this.setReturnValue(fastRuns.toArray(new FastRun[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve all Fast runs.";
            }
        });
    }

    public synchronized int addFastRun(final FastRun fastRun) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.add(fastRun, entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not add Fast run: " + fastRun;
            }
        });
    }

    public synchronized void setFastRunRunStatusAndCompletionDate(final int id, final String runStatus,
            final Date completionDate) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                dao.setFastRunRunStatusAndCompletionDate(id, runStatus, completionDate, entityManager);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not set Fast run run status: " + id;
            }
        });
    }

    public synchronized FastRun obtainLockedFastRun(final User owner, final int id) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRun>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                FastRun locked = dao.obtainLockedFastRun(owner, id, entityManager);
                this.setReturnValue(locked);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not obtain lock for Fast run with id: " + id + " by owner: " + owner.getUsername();
            }
        });
    }

    // FIXME
    // public void releaseLocked(FastRun locked) throws EmfException {
    // EntityManager entityManager = entityManagerFactory.createEntityManager();
    // try {
    // dao.releaseLocked(locked, entityManager);
    // } catch (RuntimeException e) {
    // LOG.error(
    // "Could not release lock for Control Strategy : " + locked + " by owner: "
    // + locked.getLockOwner(),
    // e);
    // throw new EmfException("Could not release lock for Control Strategy: " +
    // locked + " by owner: "
    // + locked.getLockOwner());
    // } finally {
    // entityManager.close();
    // }
    // }

    public synchronized void releaseLockedFastRun(final User user, final int id) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                dao.releaseLockedFastRun(user, id, entityManager);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not release lock for Fast run id: " + id;
            }
        });
    }

    public synchronized FastRun updateFastRun(final FastRun fastRun) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRun>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                if (!dao.canUpdateFastRun(fastRun, entityManager)) {
                    throw new EmfException("The Fast run name is already in use");
                }

                FastRun released = dao.updateFastRun(fastRun, entityManager);

                this.setReturnValue(released);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not update Fast run: " + fastRun;
            }
        });
    }

    public synchronized FastRun updateFastRunWithLock(final FastRun fastRun) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRun>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                if (!dao.canUpdateFastRun(fastRun, entityManager)) {
                    throw new EmfException("Fast run name already in use");
                }

                FastRun fastRunWithLock = dao.updateFastRunWithLock(fastRun, entityManager);

                this.setReturnValue(fastRunWithLock);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not update Fast run: " + fastRun;
            }
        });
    }

    // public void removeFastRuns(FastRun[] elements, User user) throws
    // EmfException {
    // try {
    // for (int i = 0; i < elements.length; i++) {
    // if (!user.equals(elements[i].getCreator()))
    // throw new EmfException("Only the creator of " + elements[i].getName()
    // + " can remove it from the database.");
    // remove(elements[i]);
    // }
    //
    // } catch (RuntimeException e) {
    // LOG.error("Could not update Control Strategy: " + elements, e);
    // throw new EmfException("Could not update FastRun: " + elements);
    // }
    // }

    public synchronized void removeFastRuns(final int[] ids, final User user) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                String exception = "";
                for (int i = 0; i < ids.length; i++) {

                    FastRun fastRun = dao.getFastRun(ids[i], entityManager);
                    entityManager.clear();

                    // check if admin user, then allow it to be removed.
                    if (user.equals(fastRun.getCreator()) || user.isAdmin()) {
                        if (fastRun.isLocked()) {
                            exception += "The Fast run, " + fastRun.getName()
                                    + ", is in edit mode and can not be removed. ";
                        } else {
                            removeFastRun(fastRun);
                        }
                    } else {
                        exception += "You do not have permission to remove the strategy: " + fastRun.getName() + ". ";
                    }
                }

                if (exception.length() > 0) {
                    throw new EmfException(exception);
                }
            }

            @Override
            protected String getErrorMessage() {
                return "Could not remove all Fast runs";
            }
        });
    }

    private synchronized void removeFastRun(final FastRun fastRun) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                if (!dao.canUpdateFastRun(fastRun, entityManager)) {
                    throw new EmfException("Fast run " + fastRun + " already in use");
                }

                FastRunOutput[] fastRunOutputs = getFastRunOutputs(fastRun.getId());
                for (int i = 0; i < fastRunOutputs.length; i++) {
                    dao.remove(fastRunOutputs[i], entityManager);
                }

                dao.remove(fastRun, entityManager);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not remove Fast run: " + fastRun;
            }
        });
    }

    public synchronized void removeResultDatasets(final Integer[] ids, final User user) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                DatasetDAO dsDao = new DatasetDAO();
                for (Integer id : ids) {
                    EmfDataset dataset = dsDao.getDataset(entityManager, id);

                    if (dataset != null) {
                        try {
                            dsDao.remove(user, dataset, entityManager);
                        } catch (EmfException e) {

                            if (DebugLevels.DEBUG_12()) {
                                System.out.println(e.getMessage());
                            }

                            throw new EmfException(e.getMessage());
                        }
                    }
                }
            }

            @Override
            protected String getErrorMessage() {
                return "";
            }
        });
    }

    public synchronized void runFastRun(final User user, final int fastRunId) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                try {

                    // first see if the strategy has been canceled, is so don't
                    // run it...
                    String runStatus = dao.getFastRunRunStatus(fastRunId, entityManager);
                    if (runStatus.equals("Cancelled")) {
                        return;
                    }

                    FastRun strategy = getFastRun(fastRunId);
                    // validateSectors(strategy);
                    // get rid of for now, since we don't auto export anything
                    // make sure a valid server-side export path was specified
                    // validateExportPath(strategy.getExportDirectory());

                    // make the runner of the strategy is the owner of the
                    // strategy...
                    // NEED TO TALK TO ALISON ABOUT ISSUES, LOCEKD owner might
                    // not be the creator of resulting datsets,
                    // hence a exception when trying to purge/delete the
                    // resulting datasets
                    // if (control);

                    // queue up the strategy to be run, by setting runStatus to
                    // Waiting
                    dao.setFastRunRunStatusAndCompletionDate(fastRunId, "Waiting", null, entityManager);

                    // validatePath(strategy.getExportDirectory());
                    RunFastRun runStrategy = new RunFastRun(entityManagerFactory, dbServerFactory, threadPool);
                    runStrategy.run(user, strategy, FastServiceImpl.this);
                } catch (EmfException e) {

                    // queue up the strategy to be run, by setting runStatus to
                    // Waiting
                    dao.setFastRunRunStatusAndCompletionDate(fastRunId, "Failed", null, entityManager);

                    throw new EmfException(e.getMessage());
                }
            }

            @Override
            protected String getErrorMessage() {
                return "";
            }
        });
    }

    // private void validateSectors(FastRun strategy) throws EmfException {
    // FastRunInventory[] inputDatasets = strategy.getInventories();
    // if (inputDatasets == null || inputDatasets.length == 0)
    // throw new EmfException("Input Dataset does not exist. ");
    // for (FastRunInventory dataset : inputDatasets) {
    // Sector[] sectors = dataset.getInputDataset().getSectors();
    // if (sectors == null || sectors.length == 0)
    // throw new EmfException("Inventory, " +
    // dataset.getInputDataset().getName() +
    // ", is missing a sector.  Edit dataset to add sector.");
    // }
    // }

    public List<FastRun> getFastRunsByRunStatus(final String runStatus) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<List<FastRun>>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getFastRunsByRunStatus(runStatus, entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not get Fast runs by run status: " + runStatus;
            }
        });
    }

    public Long getFastRunRunningCount() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Long>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getFastRunRunningCount(entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not get Fast run running count";
            }
        });
    }

    // private File validatePath(String folderPath) throws EmfException {
    // File file = new File(folderPath);
    //
    // if (!file.exists() || !file.isDirectory()) {
    // LOG.error("Folder " + folderPath + " does not exist");
    // throw new EmfException("Export folder does not exist: " + folderPath);
    // }
    // return file;
    // }

    public synchronized void stopFastRun(final int fastRunId) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                // look at the current status, if waiting or running, then
                // update to Cancelled.
                String status = dao.getFastRunRunStatus(fastRunId, entityManager);
                if (status.toLowerCase().startsWith("waiting") || status.toLowerCase().startsWith("running")) {
                    dao.setFastRunRunStatusAndCompletionDate(fastRunId, "Cancelled", null, entityManager);
                }
            }

            @Override
            protected String getErrorMessage() {
                return "Could not stop Fast run: " + fastRunId;
            }
        });
    }

    public synchronized int isDuplicateFastRunName(final String name) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                FastRun fastRun = dao.getFastRun(name, entityManager);
                this.setReturnValue(fastRun == null ? 0 : fastRun.getId());
            }

            @Override
            protected String getErrorMessage() {
                return "Could not determine if Fast run name is already used";
            }
        });
    }

    public synchronized int copyFastRun(final int id, final User creator) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                // get fast run to copy
                FastRun fastRun = dao.getFastRun(id, entityManager);

                entityManager.clear();// clear to flush current

                String name = "Copy of " + fastRun.getName();
                // make sure this won't cause duplicate issues...
                if (isDuplicateFastRunName(name) != 0) {
                    throw new EmfException("A Fast run named '" + name + "' already exists.");
                }

                // do a deep copy
                FastRun copied = (FastRun) DeepCopy.copy(fastRun);
                // change to applicable values
                copied.setName(name);
                copied.setAbbreviation(CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date()));
                copied.setCreator(creator);
                copied.setLastModifiedDate(new Date());
                copied.setRunStatus("Not started");
                copied.setCopiedFrom(fastRun.getName());
                copied.setLockDate(null);
                copied.setLockOwner(null);

                dao.add(copied, entityManager);
                this.setReturnValue(copied.getId());
            }

            @Override
            protected String getErrorMessage() {
                return "Could not copy Fast run: " + id;
            }
        });
    }

    // private synchronized boolean isDuplicate(String name) throws EmfException
    // {
    // return (isDuplicateName(name) != 0);
    // }

    public synchronized FastRun getFastRun(final int id) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRun>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getFastRun(id, entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not get Fast run: " + id;
            }
        });
    }

    public synchronized FastRunOutput[] getFastRunOutputs(final int fastRunId) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRunOutput[]>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                List<FastRunOutput> all = dao.getFastRunOutputs(fastRunId, entityManager);
                this.setReturnValue(all.toArray(new FastRunOutput[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast run outputs: " + fastRunId;
            }
        });
    }

    public synchronized String getDefaultExportDirectory() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<String>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getDefaultExportDirectory(entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve default export directory";
            }
        });
    }

    public synchronized String getFastRunStatus(final int id) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<String>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getStrategyRunStatus(entityManager, id));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve strategy run status";
            }
        });
    }

    public FastDataset[] getFastDatasets() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastDataset[]>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                List<FastDataset> all = dao.getFastDatasets(entityManager);
                this.setReturnValue(all.toArray(new FastDataset[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast datasets";
            }
        });
    }

    public FastDataset getFastDataset(final int fastDatasetId) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastDataset>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getFastDataset(entityManager, fastDatasetId));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast dataset: " + fastDatasetId;
            }
        });
    }

    public int getFastDatasetCount() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                List<FastDataset> all = dao.getFastDatasets(entityManager);
                this.setReturnValue(all.size());
            }

            @Override
            protected String getErrorMessage() {
                return "Could not deterimine Fast dataset count";
            }
        });
    }

    public synchronized int addFastDataset(final FastDataset fastDataset) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.addFastDataset(fastDataset, entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not add Fast dataset: " + fastDataset;
            }
        });
    }

    // public synchronized int addFastNonPointDataset(final String
    // newInventoryDatasetName,
    // final String baseNonPointDatasetName, final int
    // baseNonPointDatasetVersion,
    // final String griddedSMKDatasetName, final int griddedSMKDatasetVersion,
    // final String invTableDatasetName,
    // final int invTableDatasetVersion, final String gridName, final String
    // userName) throws EmfException {
    //
    // return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
    // @Override
    // protected void doExecute(EntityManager entityManager) throws Exception {
    //
    // DbServer dbServer = dbServerFactory.getDbServer();
    //
    // int fastDatasetId = dao.addFastNonPointDataset(newInventoryDatasetName,
    // baseNonPointDatasetName,
    // baseNonPointDatasetVersion, griddedSMKDatasetName,
    // griddedSMKDatasetVersion,
    // invTableDatasetName, invTableDatasetVersion, gridName, userName, entityManager,
    // dbServer);
    //
    // populateFastQuasiPointDataset((new UserDAO()).get(userName, entityManager),
    // fastDatasetId);
    //
    // this.setReturnValue(fastDatasetId);
    // }
    //
    // @Override
    // protected String getErrorMessage() {
    // return "Could not add FastDataset: " + newInventoryDatasetName;
    // }
    // });
    // }

    public synchronized void removeFastDataset(final int fastDatasetId, User user) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                dao.removeFastDataset(fastDatasetId, entityManager);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not remove Fast dataset: " + fastDatasetId;
            }
        });
    }

    public FastNonPointDataset[] getFastNonPointDatasets() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastNonPointDataset[]>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                List<FastNonPointDataset> all = dao.getFastNonPointDatasets(entityManager);
                this.setReturnValue(all.toArray(new FastNonPointDataset[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast non-point datasets";
            }
        });
    }

    public FastNonPointDataset getFastNonPointDataset(final int fastNonPointDatasetId) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastNonPointDataset>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getFastNonPointDataset(entityManager, fastNonPointDatasetId));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast non-point dataset: " + fastNonPointDatasetId;
            }
        });
    }

    public int getFastNonPointDatasetCount() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                List<FastNonPointDataset> all = dao.getFastNonPointDatasets(entityManager);
                this.setReturnValue(all.size());
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast non-point datasets";
            }
        });
    }

    public synchronized int addFastNonPointDataset(final FastNonPointDataset fastNonPointDataset, final User user)
            throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                DbServer dbServer = dbServerFactory.getDbServer();

                int fastDatasetId = dao.addFastNonPointDataset(fastNonPointDataset, user, 
                        entityManager, dbServer);

                populateFastQuasiPointDataset(user, fastDatasetId);

                this.setReturnValue(fastDatasetId);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not add Fast non-point dataset: " + fastNonPointDataset;
            }
        });
    }

    public synchronized void removeFastNonPointDataset(final int fastNonPointDatasetId, User user) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                dao.removeFastNonPointDataset(fastNonPointDatasetId, entityManager);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not remove Fast non-point dataset: " + fastNonPointDatasetId;
            }
        });
    }

    private void populateFastQuasiPointDataset(final User user, final int fastNonPointDatasetId) throws EmfException {
        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                FastDataset fastDataset = getFastDataset(fastNonPointDatasetId);
                PopulateFastQuasiPointDatasetTask task = new PopulateFastQuasiPointDatasetTask(user, fastDataset,
                        entityManagerFactory, dbServerFactory);
                if (task.shouldProceed()) {
                    threadPool.execute(new GCEnforcerTask("Populate FAST Quasi Point Inventory: "
                            + fastDataset.getDataset().getName(), task));
                }
            }

            @Override
            protected String getErrorMessage() {
                return "Error creating Fast point dataset: " + fastNonPointDatasetId;
            }
        });
    }

    public Grid[] getGrids() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Grid[]>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                List<Grid> all = dao.getGrids(entityManager);
                this.setReturnValue(all.toArray(new Grid[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast grids";
            }
        });
    }

    public Grid getGrid(final String name) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Grid>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getGrid(entityManager, name));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve grid " + name;
            }
        });
    }

    public synchronized FastAnalysis[] getFastAnalyses() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysis[]>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                List<FastAnalysis> fastAnalyses = dao.getFastAnalyses(entityManager);
                this.setReturnValue(fastAnalyses.toArray(new FastAnalysis[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve all Fast analyses";
            }
        });
    }

    public synchronized int addFastAnalysis(final FastAnalysis fastAnalysis) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.add(fastAnalysis, entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not add Fast analysis: " + fastAnalysis;
            }
        });
    }

    public synchronized void setFastAnalysisRunStatusAndCompletionDate(final int id, final String runStatus,
            final Date completionDate) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                dao.setFastAnalysisRunStatusAndCompletionDate(id, runStatus, completionDate, entityManager);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not set Fast analysis run status: " + id;
            }
        });
    }

    public synchronized FastAnalysis obtainLockedFastAnalysis(final User owner, final int id) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysis>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.obtainLockedFastAnalysis(owner, id, entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not obtain lock for Fast analysis: id = " + id + " by owner: " + owner.getUsername();
            }
        });
    }

    // FIXME
    // public void releaseLocked(FastAnalysis locked) throws EmfException {
    // EntityManager entityManager = entityManagerFactory.createEntityManager();
    // try {
    // dao.releaseLocked(locked, entityManager);
    // } catch (RuntimeException e) {
    // LOG.error(
    // "Could not release lock for Control Strategy : " + locked + " by owner: "
    // + locked.getLockOwner(),
    // e);
    // throw new EmfException("Could not release lock for Control Strategy: " +
    // locked + " by owner: "
    // + locked.getLockOwner());
    // } finally {
    // entityManager.close();
    // }
    // }

    public synchronized void releaseLockedFastAnalysis(final User user, final int id) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                dao.releaseLockedFastAnalysis(user, id, entityManager);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not release lock for Fast analysis: " + id;
            }
        });
    }

    public synchronized FastAnalysis updateFastAnalysis(final FastAnalysis fastAnalysis) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysis>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                if (!dao.canUpdate(fastAnalysis, entityManager)) {
                    throw new EmfException("The Fast analysis " + fastAnalysis + " is already in use");
                }

                this.setReturnValue(dao.updateFastAnalysis(fastAnalysis, entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not update Fast analysis: " + fastAnalysis;
            }
        });
    }

    public synchronized FastAnalysis updateFastAnalysisWithLock(final FastAnalysis fastAnalysis) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysis>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                if (!dao.canUpdate(fastAnalysis, entityManager)) {
                    throw new EmfException("The Fast analysis " + fastAnalysis + " is already in use");
                }

                this.setReturnValue(dao.updateWithLock(fastAnalysis, entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not update Fast analysis: " + fastAnalysis;
            }
        });
    }

    public synchronized void removeFastAnalyses(final int[] ids, final User user) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                String exception = "";
                for (int i = 0; i < ids.length; i++) {
                    FastAnalysis fastAnalysis = dao.getFastAnalysis(ids[i], entityManager);
                    entityManager.clear();

                    // check if admin user, then allow it to be removed.
                    if (user.equals(fastAnalysis.getCreator()) || user.isAdmin()) {
                        if (fastAnalysis.isLocked()) {
                            exception += "The Fast analysis, " + fastAnalysis
                                    + ", is in edit mode and can not be removed. ";
                        } else {
                            removeFastAnalysis(fastAnalysis);
                        }
                    } else {
                        exception += "You do not have permission to remove the Fast analysis: " + fastAnalysis + ". ";
                    }
                }

                if (exception.length() > 0) {
                    throw new EmfException(exception);
                }
            }

            @Override
            protected String getErrorMessage() {
                return "Could not remove all Fast analyses";
            }
        });
    }

    public synchronized void removeFastAnalysis(final FastAnalysis fastAnalysis) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                if (!dao.canUpdate(fastAnalysis, entityManager)) {
                    throw new EmfException("The Fast analysis " + fastAnalysis + " already in use");
                }

                FastAnalysisOutput[] fastAnalysisOutputs = getFastAnalysisOutputs(fastAnalysis.getId());
                for (int i = 0; i < fastAnalysisOutputs.length; i++) {
                    dao.remove(fastAnalysisOutputs[i], entityManager);
                }

                dao.remove(fastAnalysis, entityManager);
            }

            @Override
            protected String getErrorMessage() {
                return "Could not remove Fast analysis: " + fastAnalysis;
            }
        });
    }

    public synchronized void runFastAnalysis(final User user, final int fastAnalysisId) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                try {
                    dao.getFastAnalysisRunStatus(fastAnalysisId, entityManager);

                    FastAnalysis strategy = getFastAnalysis(fastAnalysisId);
                    // validateSectors(strategy);
                    // get rid of for now, since we don't auto export anything
                    // make sure a valid server-side export path was specified
                    // validateExportPath(strategy.getExportDirectory());

                    // make the runner of the strategy is the owner of the
                    // strategy...
                    // NEED TO TALK TO ALISON ABOUT ISSUES, LOCEKD owner might
                    // not be the creator of resulting datsets,
                    // hence a exception when trying to purge/delete the
                    // resulting datasets
                    // if (control);

                    // queue up the strategy to be run, by setting runStatus to
                    // Waiting
                    dao.setFastAnalysisRunStatusAndCompletionDate(fastAnalysisId, "Waiting", null, entityManager);

                    // validatePath(strategy.getExportDirectory());
                    RunFastAnalysis runStrategy = new RunFastAnalysis(entityManagerFactory, dbServerFactory, threadPool);
                    runStrategy.run(user, strategy, FastServiceImpl.this);
                } finally {
                    // queue up the strategy to be run, by setting runStatus to
                    // Waiting
                    dao.setFastAnalysisRunStatusAndCompletionDate(fastAnalysisId, "Failed", null, entityManager);
                }
            }
        });
    }

    public List<FastAnalysis> getFastAnalysesByRunStatus(final String runStatus) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<List<FastAnalysis>>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getFastAnalysesByRunStatus(runStatus, entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast analyses by run status: " + runStatus;
            }
        });
    }

    public Long getFastAnalysisRunningCount() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Long>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getFastAnalysisRunningCount(entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast analyses running count";
            }
        });
    }

    public synchronized void stopFastAnalysis(final int fastAnalysisId) throws EmfException {

        this.executeDaoCommand(new AbstractDaoCommand<Void>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                // look at the current status, if waiting or running, then
                // update to Cancelled.
                String status = dao.getFastAnalysisRunStatus(fastAnalysisId, entityManager);
                if (status.toLowerCase().startsWith("waiting") || status.toLowerCase().startsWith("running")) {
                    dao.setFastAnalysisRunStatusAndCompletionDate(fastAnalysisId, "Cancelled", null, entityManager);
                }
            }

            @Override
            protected String getErrorMessage() {
                return "Could not stop Fast analysis: " + fastAnalysisId;
            }
        });
    }

    // returns control strategy Id for the given name
    public synchronized int isDuplicateFastAnalysisName(final String name) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                FastAnalysis fastAnalysis = dao.getFastAnalysis(name, entityManager);
                this.setReturnValue(fastAnalysis == null ? 0 : fastAnalysis.getId());
            }

            @Override
            protected String getErrorMessage() {
                return "Could not determine if Fast analysis name " + name + " is already used";
            }
        });
    }

    public synchronized int copyFastAnalysis(final int id, final User creator) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<Integer>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {

                // get fast analysis to copy
                FastAnalysis fastAnalysis = dao.getFastAnalysis(id, entityManager);

                entityManager.clear();// clear to flush current

                String name = "Copy of " + fastAnalysis.getName();
                // make sure this won't cause duplicate issues...
                if (isDuplicateFastAnalysisName(name) != 0) {
                    throw new EmfException("A Fast analysis named '" + name + "' already exists.");
                }

                // do a deep copy
                FastAnalysis copied = (FastAnalysis) DeepCopy.copy(fastAnalysis);
                // change to applicable values
                copied.setName(name);
                copied.setAbbreviation(CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date()));
                copied.setCreator(creator);
                copied.setLastModifiedDate(new Date());
                copied.setRunStatus("Not started");
                copied.setCopiedFrom(fastAnalysis.getName());
                if (copied.isLocked()) {
                    copied.setLockDate(null);
                    copied.setLockOwner(null);
                }

                dao.add(copied, entityManager);
                this.setReturnValue(copied.getId());
            }

            @Override
            protected String getErrorMessage() {
                return "Could not copy Fast analysis";
            }
        });
    }

    public synchronized FastAnalysis getFastAnalysis(final int id) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysis>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getFastAnalysis(id, entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not get Fast analysis: " + id;
            }
        });
    }

    public synchronized FastAnalysisOutput[] getFastAnalysisOutputs(final int fastAnalysisId) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysisOutput[]>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                List<FastAnalysisOutput> all = dao.getFastAnalysisOutputs(fastAnalysisId, entityManager);
                this.setReturnValue(all.toArray(new FastAnalysisOutput[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast analysis outputs";
            }
        });
    }

    public synchronized String getFastAnalysisStatus(final int id) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<String>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getFastAnalysisRunStatus(entityManager, id));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast analysis run status: " + id;
            }
        });
    }

    public FastAnalysisOutputType getFastAnalysisOutputType(final String name) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysisOutputType>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getFastAnalysisOutputType(name, entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast analysis output type: " + name;
            }
        });
    }

    public FastAnalysisOutputType[] getFastAnalysisOutputTypes() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastAnalysisOutputType[]>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                List<FastAnalysisOutputType> all = dao.getFastAnalysisOutputTypes(entityManager);
                this.setReturnValue(all.toArray(new FastAnalysisOutputType[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast analysis output type";
            }
        });
    }

    public FastRunOutputType getFastRunOutputType(final String name) throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRunOutputType>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                this.setReturnValue(dao.getFastRunOutputType(name, entityManager));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast analysis output type: " + name;
            }
        });
    }

    public FastRunOutputType[] getFastRunOutputTypes() throws EmfException {

        return this.executeDaoCommand(new AbstractDaoCommand<FastRunOutputType[]>() {
            @Override
            protected void doExecute(EntityManager entityManager) throws Exception {
                List<FastRunOutputType> all = dao.getFastRunOutputTypes(entityManager);
                this.setReturnValue(all.toArray(new FastRunOutputType[0]));
            }

            @Override
            protected String getErrorMessage() {
                return "Could not retrieve Fast analysis output types";
            }
        });
    }

    public synchronized void exportFastOutputToShapeFile(int datasetId, int datasetVersion, int gridId, String userName, 
            String dirName, String pollutant) throws EmfException {
        try {
            ExportFastOutputToShapeFile exportQATask = new ExportFastOutputToShapeFile(datasetId, datasetVersion, gridId, userName, dirName, pollutant, dbServerFactory, entityManagerFactory,
                    threadPool);
            exportQATask.export();
        } catch (Exception e) {
            LOG.error("Could not export dataset", e);
            throw new EmfException("Could not export dataset: " + e.getMessage());
        }
    }

    public synchronized void exportFastOutputToNetCDFFile(int datasetId, int datasetVersion, int gridId, String userName, 
            String dirName, String pollutant) throws EmfException {
        try {
            ExportFastOutputToNetCDFFile exportQATask = new ExportFastOutputToNetCDFFile(datasetId, datasetVersion, gridId, userName, dirName, pollutant, dbServerFactory, entityManagerFactory,
                    threadPool);
            exportQATask.export();
        } catch (Exception e) {
            LOG.error("Could not export dataset", e);
            throw new EmfException("Could not export dataset: " + e.getMessage());
        }
    }

    public String[] getFastRunSpeciesMappingDatasetPollutants(int datasetId, int datasetVersion) throws EmfException {
        return dao.getFastRunSpeciesMappingDatasetPollutants(datasetId, datasetVersion);
    }
}
