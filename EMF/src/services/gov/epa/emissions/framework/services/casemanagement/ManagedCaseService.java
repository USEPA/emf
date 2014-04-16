package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.io.generic.GenericExporterToString;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.commons.util.StringTools;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.RemoteCommand;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJobKey;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.casemanagement.outputs.QueueCaseOutput;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.exim.ManagedCopyService;
import gov.epa.emissions.framework.services.exim.ManagedExportService;
import gov.epa.emissions.framework.services.exim.ManagedImportService;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.CaseJobSubmitter;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.tasks.TaskManagerFactory;
import gov.epa.emissions.framework.tasks.TaskSubmitter;
import gov.epa.emissions.framework.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class ManagedCaseService {
    private static Log log = LogFactory.getLog(ManagedCaseService.class);

    private static int svcCount = 0;
    
    private String svcLabel = null;
    
    private String locNewLine = StringTools.EMF_NEW_LINE;

    public synchronized String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }
        log.info(svcLabel);

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    private CaseDAO dao;

    private HibernateSessionFactory sessionFactory = null;

    private DbServerFactory dbFactory;

    private User user;

    private ManagedImportService importService;
    
    private ManagedCopyService copyService;

    private ManagedExportService exportService;

    private String eolString = System.getProperty("line.separator");

    // these run fields are used to create the run script
    private String runShell = "#!/bin/csh"; // shell to run under

    private String runSet = "setenv"; // how to set a variable

    private String runEq = " "; // equate a variable (could be a space)

    private String runTerminator = ""; // line terminator

    private String runComment = "##"; // line comment

    private String runSuffix = ".csh"; // job run file suffix

    // all sectors and all jobs id in the case inputs tb
    private final Sector ALL_SECTORS = null;

    private final GeoRegion ALL_REGIONS = null;

    private final String AAA = "all regions, all sectors, all jobs";

    private final String AAJ = "all regions, all sectors, specific job";

    private final String ASA = "all regions, specific sector, all jobs";

    private final String ASJ = "all regions, specific sector, specific job";

    private final String RAA = "specific region, all sectors, all jobs";

    private final String RSA = "specific region, specific sector, all jobs";

    private final String RAJ = "specific region, all sectors, specific job";

    private final String RSJ = "specific region, specific sector, specific job";

    private final int ALL_JOB_ID = 0;

    // protected Session session = null;

    // private Session getSession() {
    // if (session == null) {
    // session = sessionFactory.getSession();
    // }
    // return session;
    // }

    public ManagedCaseService(DbServerFactory dbFactory, HibernateSessionFactory sessionFactory) {
        this.dbFactory = dbFactory;
        this.sessionFactory = sessionFactory;
        this.dao = new CaseDAO(sessionFactory);

        if (DebugLevels.DEBUG_9())
            System.out.println("In ManagedCaseService constructor: Is the session Factory null? "
                    + (sessionFactory == null));

        myTag();

        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> " + myTag());

        log.info("ManagedCaseService");
        // log.info("exportTaskSubmitter: " + caseJobSubmitter);
        log.info("Session factory null? " + (sessionFactory == null));
        log.info("User null? : " + (user == null));
    }

    /**
     * Generate the unique job key
     * 
     */
    private synchronized String createJobKey(int jobId) {
        return jobId + "_" + new Date().getTime();
    }

    private synchronized ManagedExportService getExportService() {
        log.info("ManagedCaseService::getExportService");

        if (exportService == null) {
            try {
                exportService = new ManagedExportService(dbFactory, sessionFactory);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return exportService;
    }

    private synchronized ManagedImportService getImportService() throws EmfException {
        log.info("ManagedCaseService::getImportService");

        if (importService == null) {
            try {
                importService = new ManagedImportService(dbFactory, sessionFactory);
            } catch (Exception e) {
                e.printStackTrace();
                throw new EmfException(e.getMessage());
            }
        }

        return importService;
    }
    
    private synchronized ManagedCopyService getCopyService() throws EmfException {
        log.info("ManagedCaseService::getCopyService");

    //    if (copyService == null) {
            try {
                copyService = new ManagedCopyService(dbFactory, sessionFactory);
            } catch (Exception e) {
                e.printStackTrace();
                throw new EmfException(e.getMessage());
            }
    //    }

        return copyService;
    }

    // ***************************************************************************

    public synchronized Case[] getCases() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List cases = dao.getCases(session);

            return (Case[]) cases.toArray(new Case[0]);
        } catch (RuntimeException e) {
            log.error("Could not get all Cases", e);
            throw new EmfException("Could not get all Cases");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }
    
    public synchronized Case[] getCases(String nameContains) throws EmfException {
        
        Session session = sessionFactory.getSession();
        try {
            return dao.getCases(session, nameContains);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not retrieve all case", e);
            throw new EmfException("Could not retrieve all case " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized Case getCase(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Case caseObj = dao.getCase(caseId, session);
            return caseObj;
        } catch (RuntimeException e) {
            log.error("Could not get all Cases", e);
            throw new EmfException("Could not get all Cases");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized Case getCaseFromName(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Case caseObj = dao.getCaseFromName(name, session);
            return caseObj;
        } catch (RuntimeException e) {
            log.error("Could not get case", e);
            throw new EmfException("Could not get case");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private Case getCase(int caseId, Session session) throws EmfException {
        try {
            Case caseObj = dao.getCase(caseId, session);
            return caseObj;
        } catch (RuntimeException e) {
            log.error("Could not get case from case id: " + caseId + ".", e);
            throw new EmfException("Could not get case from case id: " + caseId + ".");
        }
    }

    public synchronized Case[] getCases(CaseCategory category) {
        return dao.getCases(category);
    }
    
    public synchronized Case[] getCases(CaseCategory category, String nameContains) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getCases(session, category,nameContains);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not retrieve all case", e);
            throw new EmfException("Could not retrieve all case " + e.getMessage());
        } finally {
            session.close();
        }

    }


    public synchronized Version[] getLaterVersions(EmfDataset dataset, Version version) throws EmfException {
        Versions versions = new Versions();
        Session session = sessionFactory.getSession();

        try {
            int id = dataset.getId();
            Version[] vers = versions.getLaterVersions(id, version, session);

            if (DebugLevels.DEBUG_14() && vers.length > 0)
                System.out.println("There are " + vers.length + " later versions for dataset: " + dataset.getName());

            return vers;
        } catch (Exception e) {
            log.error("Could not get versions for dataset: " + dataset.getName() + ".\n" , e);
            throw new EmfException("Could not get versions for dataset: " + dataset.getName() + ".\n");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseJob getCaseJob(int jobId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return dao.getCaseJob(jobId, session);
        } catch (Exception e) {
            log.error("Could not get job for job id " + jobId + ".\n", e);
            throw new EmfException("Could not get job for job id " + jobId + ".\n");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private CaseJob getCaseJob(int jobId, Session session) throws EmfException {
        try {
            return dao.getCaseJob(jobId, session);
        } catch (Exception e) {
            log.error("Could not get job for job id " + jobId + ".\n", e);
            throw new EmfException("Could not get job for job id " + jobId + ".\n");
        } 
    }

    public synchronized Sector[] getSectorsUsedbyJobs(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List<Sector> sectors = dao.getSectorsUsedbyJobs(caseId, session);
            return sectors.toArray(new Sector[0]);
        } catch (Exception e) {
            log.error("Could not get sectors for case id " + caseId + ".\n" + e.getMessage());
            throw new EmfException("Could not get sectors for case id " + caseId + ".\n");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized Abbreviation[] getAbbreviations() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List abbreviations = dao.getAbbreviations(session);

            return (Abbreviation[]) abbreviations.toArray(new Abbreviation[0]);
        } catch (RuntimeException e) {
            log.error("Could not get all Abbreviations", e);
            throw new EmfException("Could not get all Abbreviations");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized Abbreviation addAbbreviation(Abbreviation abbr) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.add(abbr, session);
            return (Abbreviation) dao.load(Abbreviation.class, abbr.getName(), session);
        } catch (Exception e) {
            log.error("Cannot add case abbreviation " + abbr.getName(), e);
            throw new EmfException("Cannot add case abbreviation " + abbr.getName() + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized AirQualityModel[] getAirQualityModels() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List airQualityModels = dao.getAirQualityModels(session);
            return (AirQualityModel[]) airQualityModels.toArray(new AirQualityModel[0]);
        } catch (RuntimeException e) {
            log.error("Could not get all Air Quality Models", e);
            throw new EmfException("Could not get all Air Quality Models");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseCategory[] getCaseCategories() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getCaseCategories(session);
            return (CaseCategory[]) results.toArray(new CaseCategory[0]);
        } catch (RuntimeException e) {
            log.error("Could not get all Case Categories", e);
            throw new EmfException("Could not get all Case Categories");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseCategory addCaseCategory(CaseCategory element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(element, session);
            return (CaseCategory) dao.load(CaseCategory.class, element.getName(), session);
        } catch (RuntimeException e) {
            log.error("Could not add CaseCategory: " + element, e);
            throw new EmfException("Could not add CaseCategory: " + element);
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized EmissionsYear[] getEmissionsYears() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getEmissionsYears(session);
            return (EmissionsYear[]) results.toArray(new EmissionsYear[0]);
        } catch (RuntimeException e) {
            log.error("Could not get all Emissions Years", e);
            throw new EmfException("Could not get all Emissions Years");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getMeteorlogicalYears(session);
            return (MeteorlogicalYear[]) results.toArray(new MeteorlogicalYear[0]);
        } catch (RuntimeException e) {
            log.error("Could not get all Meteorological Years", e);
            throw new EmfException("Could not get all Meteorological Years");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized Speciation[] getSpeciations() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getSpeciations(session);
            return (Speciation[]) results.toArray(new Speciation[0]);
        } catch (RuntimeException e) {
            log.error("Could not get all Speciations", e);
            throw new EmfException("Could not get all Speciations");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized Case addCase(User user, Case element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(element, session);
            Case loaded = (Case) dao.load(Case.class, element.getName(), session);
            Case locked = dao.obtainLocked(user, loaded, session);
            locked.setAbbreviation(new Abbreviation(loaded.getId() + ""));
            return dao.update(locked, session);
        } catch (RuntimeException e) {
            log.error("Could not add Case: " + element, e);
            throw new EmfException("Could not add Case: " + element);
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized String checkParentCase(Case caseObj) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.checkParentChildRelationship(caseObj, session);

            List<?> list1 = session.createQuery(
                    "SELECT obj.caseId FROM CaseJob as obj WHERE obj.parentCaseId = " + caseObj.getId()).list();
            List<?> list2 = session.createQuery(
                    "SELECT obj.caseID FROM CaseInput as obj WHERE obj.parentCaseId = " + caseObj.getId()).list();
            List<?> list3 = session.createQuery(
                    "SELECT obj.caseID FROM CaseParameter as obj WHERE obj.parentCaseId = " + caseObj.getId()).list();

            if (list1 != null && list1.size() > 0) {
                int childId = Integer.parseInt(list1.get(0).toString());

                if (childId != caseObj.getId()) {
                    Case childCase = dao.getCase(childId, session);
                    return "\"" + caseObj.getName() + "\" is a parent case for at least one job from case \""
                            + childCase.getName() + "\".";
                }
            }

            if (list2 != null && list2.size() > 0) {
                int childId = Integer.parseInt(list2.get(0).toString());

                if (childId != caseObj.getId()) {
                    Case childCase = dao.getCase(childId, session);
                    return "\"" + caseObj.getName() + "\" is a parent case for at least one input from case \""
                            + childCase.getName() + "\".";
                }
            }

            if (list3 != null && list3.size() > 0) {
                int childId = Integer.parseInt(list3.get(0).toString());

                if (childId != caseObj.getId()) {
                    Case childCase = dao.getCase(childId, session);
                    return "\"" + caseObj.getName() + "\" is a parent case for at least one parameter from case \""
                            + childCase.getName() + "\".";
                }
            }

            return "";
        } catch (Exception e) {
            log.error("Checking case " + caseObj.getName() + ".\n", e);
            throw new EmfException(e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized void removeCase(Case caseObj) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            if (DebugLevels.DEBUG_18())
                log.warn("Started removing case: " + new Date());

            setStatus(caseObj.getLastModifiedBy(), "Started removing case " + caseObj.getName() + ".", "Remove Case");

            List<CaseJob> jobs = dao.getCaseJobs(caseObj.getId(), session);
            checkJobsStatuses(jobs, caseObj);

            if (DebugLevels.DEBUG_18())
                log.warn("Removing case: finished checking job statuses: " + new Date());

            List<CaseInput> inputs = dao.getCaseInputs(caseObj.getId(), session);
            dao.removeCaseInputs(inputs.toArray(new CaseInput[0]), session);

            if (DebugLevels.DEBUG_18())
                log.warn("Removing case: finished removing case inputs: " + new Date());

            PersistedWaitTask[] persistedJobs = getPersistedJobs(jobs, session);
            QueueCaseOutput[] outputQs = getQedOutputs(jobs, session);

            if (outputQs.length > 0)
                throw new EmfException("Selected case: " + caseObj.getName() + " has " + outputQs.length
                        + " pending outputs to register.");

            CaseJobKey[] keys = getJobsKeys(jobs, session);
            JobMessage[] msgs = getJobsMessages(jobs, session);
            CaseOutput[] outputs = getJobsOutputs(jobs, session);

            try {
                dao.removeObjects(persistedJobs, session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove persisted jobs from db table.");
            }

            if (DebugLevels.DEBUG_18())
                log.warn("Removing case: finished removing persisted jobs: " + new Date());

            try {
                dao.removeObjects(keys, session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove job keys from db table.");
            }

            if (DebugLevels.DEBUG_18())
                log.warn("Removing case: finished removing job keys from job-key table: " + new Date());

            try {
                dao.removeObjects(msgs, session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove job messages from db table.");
            }

            if (DebugLevels.DEBUG_18())
                log.warn("Removing case: finished removing job messages: " + new Date());

            try {
                dao.removeObjects(outputs, session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove case outputs from db table.");
            }

            if (DebugLevels.DEBUG_18())
                log.warn("Removing case: finished removing outputs: " + new Date());

            try {
                CaseJob[] toRemove = jobs.toArray(new CaseJob[0]);

                // NOTE: cannot remove jobs without first reset the job dependencies
                for (CaseJob job : toRemove) {
                    job.setDependentJobs(null);
                    dao.updateCaseJob(job);
                }

                dao.removeCaseJobs(toRemove, session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove jobs from db table.");
            }

            if (DebugLevels.DEBUG_18())
                log.warn("Removing case: finished removing jobs: " + new Date());

            List<CaseParameter> parameters = dao.getCaseParameters(caseObj.getId(), session);
            try {
                dao.removeCaseParameters(parameters.toArray(new CaseParameter[0]), session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove case parameters from db table.");
            }

            if (DebugLevels.DEBUG_18())
                log.warn("Removing case: finished removing case parameters: " + new Date());

            try {
                dao.removeChildCase(caseObj.getId(), session);
            } catch (Exception e) {
                throw new EmfException("Cannot remove case: " + e.getMessage());
            }

            if (DebugLevels.DEBUG_18())
                log.warn("Removing case: finished removing items from parent-sensitivity cases table: " + new Date());

            try {
                dao.remove(caseObj, session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove case objects: " + caseObj.getName() + " from db table.");
            }

            if (DebugLevels.DEBUG_18())
                log.warn("Removing case: finished removing case objects: " + new Date());

            try {
                dao.removeObject(caseObj.getAbbreviation(), session);
            } catch (RuntimeException e) {
                throw new EmfException("Cannot remove case abbreviation: " + caseObj.getAbbreviation().getName()
                        + " from db table.");
            }

            setStatus(caseObj.getLastModifiedBy(), "Finished removing case " + caseObj.getName() + ".", "Remove Case");

            if (DebugLevels.DEBUG_18())
                log.warn("Removing case: finished removing case abbreviation: " + new Date());
        } catch (Exception e) {
            log.error("Could not remove Case: " + caseObj, e);
            throw new EmfException(e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private PersistedWaitTask[] getPersistedJobs(List<CaseJob> jobs, Session session2) {
        List<PersistedWaitTask> persistedJobs = new ArrayList<PersistedWaitTask>();

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            persistedJobs.addAll(dao.getPersistedWaitTasks(job.getCaseId(), job.getId(), session2));
        }

        return persistedJobs.toArray(new PersistedWaitTask[0]);
    }

    private QueueCaseOutput[] getQedOutputs(List<CaseJob> jobs, Session session) {
        List<QueueCaseOutput> outputs = new ArrayList<QueueCaseOutput>();

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            outputs.addAll(dao.getQueueCaseOutputs(job.getCaseId(), job.getId(), session));
        }

        return outputs.toArray(new QueueCaseOutput[0]);
    }

    private CaseJobKey[] getJobsKeys(List<CaseJob> jobs, Session session2) {
        List<CaseJobKey> keys = new ArrayList<CaseJobKey>();

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            keys.addAll(dao.getCaseJobKey(job.getId(), session2));
        }

        return keys.toArray(new CaseJobKey[0]);
    }

    private void checkJobsStatuses(List<CaseJob> jobs, Case caseObj) throws EmfException {
        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            JobRunStatus status = job.getRunstatus();
            boolean active = false;

            if (status != null && status.getName().toUpperCase().equals("RUNNING"))
                active = true;

            if (status != null && status.getName().toUpperCase().equals("SUBMITTED"))
                active = true;

            if (status != null && status.getName().toUpperCase().equals("EXPORTING"))
                active = true;

            if (status != null && status.getName().toUpperCase().equals("WAITING"))
                active = true;

            if (active)
                throw new EmfException("Job: " + job.getName() + " in case: " + caseObj.getName()
                        + " has an active status.");
        }

    }

    private CaseOutput[] getJobsOutputs(List<CaseJob> jobs, Session session) {
        List<CaseOutput> allOutputs = new ArrayList<CaseOutput>();

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            allOutputs.addAll(dao.getCaseOutputs(job.getCaseId(), job.getId(), session));
        }

        return allOutputs.toArray(new CaseOutput[0]);
    }

    private JobMessage[] getJobsMessages(List<CaseJob> jobs, Session session) {
        List<JobMessage> allMsgs = new ArrayList<JobMessage>();

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            allMsgs.addAll(dao.getJobMessages(job.getCaseId(), job.getId(), session));
        }

        return allMsgs.toArray(new JobMessage[0]);
    }

    private synchronized void setStatus(User user, String message, String type) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType(type);
        status.setMessage(message);
        status.setTimestamp(new Date());
        StatusDAO statusDao = new StatusDAO(sessionFactory);
        statusDao.add(status);
    }

    public synchronized Case obtainLocked(User owner, Case element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Case locked = dao.obtainLocked(owner, element, session);
            return locked;
        } catch (RuntimeException e) {
            log.error("Could not obtain lock for Case: " + element + " by owner: " + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Case: " + element + " by owner: " + owner.getUsername());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized Case releaseLocked(User owner, Case locked) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Case released = dao.releaseLocked(owner, locked, session);
            return released;
        } catch (RuntimeException e) {
            log.error("Could not release lock by " + locked.getLockOwner(), e);
            throw new EmfException("Could not release lock by " + locked.getLockOwner() + " for Case: " + locked);
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized Case updateCase(Case element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {

            Case released = dao.update(element, session);
            return released;
        } catch (RuntimeException e) {
            log.error("Could not update Case", e);
            throw new EmfException("Could not update Case: " + element + "; " + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseInput getCaseInput(int inputId) throws EmfException {

        Session session = sessionFactory.getSession();
        try {

            CaseInput input = dao.getCaseInput(inputId, session);
            return input;
        } catch (RuntimeException e) {
            log.error("Could not get all Input Names", e);
            throw new EmfException("Could not get all Input Names");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }

    }

    public synchronized InputName[] getInputNames() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getInputNames(session);
            return (InputName[]) results.toArray(new InputName[0]);
        } catch (RuntimeException e) {
            log.error("Could not get all Input Names", e);
            throw new EmfException("Could not get all Input Names");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized InputEnvtVar[] getInputEnvtVars() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getInputEnvtVars(session);
            return (InputEnvtVar[]) results.toArray(new InputEnvtVar[0]);
        } catch (RuntimeException e) {
            log.error("Could not get all Input Environment Variables", e);
            throw new EmfException("Could not get all Input Environment Variables");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseProgram[] getPrograms() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getPrograms(session);
            return (CaseProgram[]) results.toArray(new CaseProgram[0]);
        } catch (RuntimeException e) {
            log.error("Could not get all Programs", e);
            throw new EmfException("Could not get all Programs");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized ModelToRun[] getModelToRuns() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getModelToRuns(session);
            return (ModelToRun[]) results.toArray(new ModelToRun[0]);
        } catch (RuntimeException e) {
            log.error("Could not get all Models To Run", e);
            throw new EmfException("Could not get all Models To Run");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private Version[] getInputDatasetVersions(int caseId) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseId);
        List<Version> list = new ArrayList<Version>();

        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].getDataset() != null && !checkExternalDSType(inputs[i].getDatasetType()))
                list.add(inputs[i].getVersion());

        return list.toArray(new Version[0]);
    }

    private EmfDataset[] getInputDatasets(int caseId) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseId);
        List<EmfDataset> list = new ArrayList<EmfDataset>();

        for (int i = 0; i < inputs.length; i++)
            if (inputs[i].getDataset() != null && !checkExternalDSType(inputs[i].getDatasetType()))
                list.add(inputs[i].getDataset());

        return list.toArray(new EmfDataset[0]);
    }

    private boolean checkExternalDSType(DatasetType type) {
        String name = type.getName();

        return name.indexOf("External") >= 0;
    }

    private SubDir[] getSubdirs(int caseId) throws EmfException {
        CaseInput[] inputs = getCaseInputs(caseId);
        SubDir[] subdirs = new SubDir[inputs.length];

        for (int i = 0; i < inputs.length; i++)
            subdirs[i] = inputs[i].getSubdirObj();

        return subdirs;
    }

    public synchronized InputName addCaseInputName(InputName name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(name, session);
            Criterion crit1 = Restrictions.eq("name", name.getName());
            Criterion crit2 = Restrictions.eq("modelToRunId", new Integer(name.getModelToRunId()));

            return (InputName) dao.load(InputName.class, new Criterion[] { crit1, crit2 }, session);
        } catch (Exception e) {
            log.error("Could not add new case input name '" + name.getName() + "'\n", e);
            throw new EmfException("Could not add new case input name '" + name.getName() + "'");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseProgram addProgram(CaseProgram program) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(program, session);
            Criterion crit1 = Restrictions.eq("name", program.getName());
            Criterion crit2 = Restrictions.eq("modelToRunId", new Integer(program.getModelToRunId()));

            return (CaseProgram) dao.load(CaseProgram.class, new Criterion[] { crit1, crit2 }, session);
        } catch (Exception e) {
            log.error("Could not add new program '" + program.getName() + "'\n", e);
            throw new EmfException("Could not add new program '" + program.getName() + "'");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized InputEnvtVar addInputEnvtVar(InputEnvtVar inputEnvtVar) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(inputEnvtVar, session);
            Criterion crit1 = Restrictions.eq("name", inputEnvtVar.getName());
            Criterion crit2 = Restrictions.eq("modelToRunId", new Integer(inputEnvtVar.getModelToRunId()));
            return (InputEnvtVar) dao.load(InputEnvtVar.class, new Criterion[] { crit1, crit2 }, session);
        } catch (Exception e) {
            log.error("Could not add new input environment variable '" + inputEnvtVar.getName() + "'\n", e);
            throw new EmfException("Could not add new input environment variable '" + inputEnvtVar.getName() + "'");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized ModelToRun addModelToRun(ModelToRun model) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ModelToRun temp = dao.loadModelTorun(model.getName(), session);

            if (temp != null)
                return temp;

            dao.add(model, session);

            return (ModelToRun) dao.load(ModelToRun.class, model.getName(), session);
        } catch (Exception e) {
            log.error("Could not add new model to run '" + model.getName() + "'\n", e);
            throw new EmfException("Could not add new model to run '" + model.getName() + "'");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized SubDir[] getSubDirs() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List results = dao.getSubDirs(session);
            return (SubDir[]) results.toArray(new SubDir[0]);
        } catch (RuntimeException e) {
            log.error("Could not get all subdirectories", e);
            throw new EmfException("Could not get all subdirectories");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized SubDir addSubDir(SubDir subdir) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Criterion crit1 = Restrictions.eq("name", subdir.getName());
            Criterion crit2 = Restrictions.eq("modelToRunId", new Integer(subdir.getModelToRunId()));
            SubDir existed = (SubDir) dao.load(SubDir.class, new Criterion[] { crit1, crit2 }, session);

            if (existed != null)
                return existed;

            dao.add(subdir, session);

            return (SubDir) dao.load(SubDir.class, new Criterion[] { crit1, crit2 }, session);
        } catch (Exception e) {
            log.error("Could not add new subdirectory '" + subdir.getName() + "'\n", e);
            throw new EmfException("Could not add new subdirectory '" + subdir.getName() + "'");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized void addCaseInputs(User user, int caseId, CaseInput[] inputs) throws EmfException {
        Session session = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();
        List<GeoRegion> regions = new ArrayList<GeoRegion>();

        try {
            for (int i = 0; i < inputs.length; i++) {
                inputs[i].setCaseID(caseId);
                CaseJob job = dao.getCaseJob(inputs[i].getCaseJobID());

                if (job != null) {
                    CaseJob targetJob = dao.getCaseJob(caseId, job, session);

                    if (targetJob == null)
                        throw new EmfException("Required case job: " + job.getName() + " doesn't exist in target case.");

                    inputs[i].setCaseJobID(targetJob.getId());
                }

                if (dao.caseInputExists(inputs[i], session))
                    throw new EmfException("Case input: " + inputs[i].getName() + " already exists in target case.");
            }

            for (CaseInput input : inputs) {
                dao.add(input, session);
                Sector sector = input.getSector();
                GeoRegion region = input.getRegion();

                if (sector != null && !sectors.contains(sector))
                    sectors.add(sector);
                if (region != null && !regions.contains(region))
                    regions.add(region);
            }
        } catch (Exception e) {
            log.error("Could not add new case input '" + inputs[0].getName() + "' etc.\n", e);
            throw new EmfException(e.getMessage());
        } finally {
            // NOTE: if any inputs get copied, we need to update target case sector list
            try {
                updateCase(user, caseId, session, sectors, regions);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + inputs.length + " inputs copied.");
            } finally {
                if (session != null && session.isConnected())
                    session.close();
            }
        }
    }

    private synchronized void addCaseInputs(int sensitivityCaseId, CaseInput[] inputs, String jobPrefix, GeoRegion region)
            throws EmfException {
        Session session = sessionFactory.getSession();
        
        try {
            for (int i = 0; i < inputs.length; i++) {
                inputs[i].setCaseID(sensitivityCaseId);
                CaseJob job = dao.getCaseJob(inputs[i].getCaseJobID());

                if (job != null) {
                    if (job.getRegion() != null && region != null) job.setRegion(region);
                    job.setName(jobPrefix + job.getName());
                    
                    CaseJob targetJob = dao.getCaseJob(sensitivityCaseId, job, session);

                    if (targetJob == null)
                        throw new EmfException("Required case job: " + jobPrefix + job.getName()
                                + " doesn't exist in target case.");

                    inputs[i].setCaseJobID(targetJob.getId());
                }

                if (dao.caseInputExists(inputs[i], session))
                    throw new EmfException("Case input: " + inputs[i].getName() + " already exists in target case.");
            }

            for (CaseInput input : inputs) {
                dao.add(input, session);
            }
        } catch (Exception e) {
            log.error("Could not add new case input '" + inputs[0].getName() + "' etc.\n", e);
            throw new EmfException(e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseInput addCaseInput(User user, CaseInput input, boolean copyingCase) throws EmfException {
        Session session = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();
        List<GeoRegion> regions = new ArrayList<GeoRegion>();
        Sector sector = input.getSector();
        GeoRegion region = input.getRegion();

        if (sector != null && !sectors.contains(sector))
            sectors.add(sector);
        if (region != null && !regions.contains(region))
            regions.add(region);
        
        try {
            if (!copyingCase)
                checkNExtendCaseLock(user, getCase(input.getCaseID(), session));
        } catch (EmfException e) {
            throw e;
        }

        try {
            if (dao.caseInputExists(input, session))
                throw new EmfException(
                        "The combination of 'Input Name', 'Region', 'Sector', 'Program', and 'Job' should be unique.");

            dao.add(input, session);
            
            return (CaseInput) dao.loadCaseInput(input, session);
        } catch (Exception e) {
            log.error("Could not add new case input '" + input.getName() + "'\n", e);
            throw new EmfException(e.getMessage() == null ? "Could not add new case input." : e.getMessage());
        } finally {
            try {
                updateCase(user, input.getCaseID(), session, sectors, regions);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + "Case can not be updated. ");
            } finally {
                if (session != null && session.isConnected())
                    session.close();
            }
        }
        
    }

    public synchronized void updateCaseInput(User user, CaseInput input) throws EmfException {
        Session localSession = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();
        List<GeoRegion> regions = new ArrayList<GeoRegion>();
        Sector sector = input.getSector();
        GeoRegion region = input.getRegion();

        if (sector != null && !sectors.contains(sector))
            sectors.add(sector);
        if (region != null && !regions.contains(region))
            regions.add(region);

        try {
            checkNExtendCaseLock(user, getCase(input.getCaseID(), localSession));
        } catch (EmfException e) {
            throw e;
        }

        try {
            CaseInput loaded = (CaseInput) dao.loadCaseInput(input, localSession);

            if (loaded != null && loaded.getId() != input.getId())
                throw new EmfException("Case input uniqueness check failed (" + loaded.getId() + "," + input.getId()
                        + ")");

            // Clear the cached information. To update a case
            // FIXME: Verify the session.clear()
            localSession.clear();
            dao.updateCaseInput(input, localSession);
            // setStatus(user, "Saved input " + input.getName() + " to database.", "Save Input");
        } catch (RuntimeException e) {
            log.error("Could not update case input: " + input.getName() + ".\n", e);
            throw new EmfException("Could not update case input: " + input.getName() + ".");
        } finally {
            try {
                updateCase(user, input.getCaseID(),localSession, sectors, regions);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + "Case can not be updated. ");
            } finally {
                if (localSession != null && localSession.isConnected())
                    localSession.close();
            } 
        }
    }

    public synchronized void removeCaseInputs(User user, CaseInput[] inputs) throws EmfException {
        Session session = sessionFactory.getSession();       
        int caseId = inputs[0].getCaseID();        
        List<Sector> sectors = new ArrayList<Sector>();
        List<GeoRegion> regions = new ArrayList<GeoRegion>();
        try {
            dao.removeCaseInputs(inputs, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not remove case input " + inputs[0].getName() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove case input " + inputs[0].getName() + " etc.");
        } finally {
            try {
                updateCase(user, caseId, session, sectors, regions);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + "Case can not be updated. ");
            } finally {
                if (session != null && session.isConnected())
                    session.close();
            }
        }
    }

    public synchronized CaseInput[] getCaseInputs(int caseId, int[] jobIds) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseInput> inputs = dao.getCaseInputsByJobIds(caseId, jobIds, session);

            return inputs.toArray(new CaseInput[0]);
        } catch (Exception e) {
            throw new EmfException("Error retrieving case inputs: " + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseInput[] getCaseInputs(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseInput> inputs = dao.getCaseInputs(caseId, session);

            Collections.sort(inputs, new Comparator<CaseInput>() {
                public int compare(CaseInput o1, CaseInput o2) {
                    int sectorId1 = (o1.getSector() == null) ? 0 : o1.getSector().getId();
                    int sectorId2 = (o2.getSector() == null) ? 0 : o2.getSector().getId();

                    int envId1 = (o1.getEnvtVars() == null) ? 0 : o1.getEnvtVars().getId();
                    int envId2 = (o2.getEnvtVars() == null) ? 0 : o2.getEnvtVars().getId();

                    int jobId1 = o1.getCaseJobID();
                    int jobId2 = o2.getCaseJobID();

                    if (sectorId1 > sectorId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 > envId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 > jobId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 == jobId2)
                        return 0;

                    return -1;
                }
            });

            return inputs.toArray(new CaseInput[0]);
        } catch (Exception e) {
            log.error("Could not get all inputs for case (id=" + caseId + ").\n", e);
            throw new EmfException("Could not get all inputs for case (id=" + caseId + ").\n");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseInput[] getCaseInputs(int pageSize, int caseId, Sector sector, String envNameContains,
            boolean showAll) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseInput> inputs = dao.getCaseInputs(pageSize, caseId, sector, envNameContains, showAll, session);

            Collections.sort(inputs, new Comparator<CaseInput>() {
                public int compare(CaseInput o1, CaseInput o2) {
                    int sectorId1 = (o1.getSector() == null) ? 0 : o1.getSector().getId();
                    int sectorId2 = (o2.getSector() == null) ? 0 : o2.getSector().getId();

                    int envId1 = (o1.getEnvtVars() == null) ? 0 : o1.getEnvtVars().getId();
                    int envId2 = (o2.getEnvtVars() == null) ? 0 : o2.getEnvtVars().getId();

                    int jobId1 = o1.getCaseJobID();
                    int jobId2 = o2.getCaseJobID();

                    if (sectorId1 > sectorId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 > envId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 > jobId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 == jobId2)
                        return 0;

                    return -1;
                }
            });
            return inputs.toArray(new CaseInput[0]);
        } catch (Exception e) {
            log.error("Could not get all inputs for case (id=" + caseId + ").\n", e);
            throw new EmfException("Could not get all inputs for case (id=" + caseId + ").\n");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    /**
     * Gets all the inputs for this job, selects based on: case ID, job ID, and sector
     */
    private List<CaseInput> getJobInputs(int caseId, int jobId, Sector sector, GeoRegion region, Session session)
            throws EmfException {
        List<CaseInput> outInputs = new ArrayList<CaseInput>();
        EmfDataset cipDataset = null;
        String badCipName = null;

        // select the inputs based on 3 criteria
        try {
            List<CaseInput> inputs = dao.getJobInputs(caseId, jobId, sector, region, session);
            if (DebugLevels.DEBUG_9())
                System.out.println("Are inputs null?" + (inputs == null));
            Iterator<CaseInput> iter = inputs.iterator();

            while (iter.hasNext()) {

                CaseInput cip = iter.next();
                badCipName = cip.getName();
                cipDataset = cip.getDataset();

                if (cipDataset == null) {

                    if (cip.isRequired()) {
                        if (DebugLevels.DEBUG_9())
                            System.out.println("CIP DATASET IS NULL AND IS REQD FOR CIP INPUT " + cip.getName());
                        badCipName = cip.getName();
                        // emf exception
                        throw new EmfException("Required dataset not set for CaseInput= " + badCipName);
                    }
                } else {
                    outInputs.add(cip);
                }
            }

            return outInputs;
        } catch (Exception e) {
            log.error("Could not get all inputs for case (id=" + caseId + "), job (id=" + jobId + ").\n", e);
            // throw new EmfException("Required dataset not set for Case Input name = " + badCipName);
            throw new EmfException(e.getMessage());
        }
    }

    private List<CaseInput> getAllJobInputs(CaseJob job, Session session) throws EmfException {
        Map<String, List<CaseInput>> map = getInputHierarchy(job, session);
        List<CaseInput> inputsAll = new ArrayList<CaseInput>(); // all inputs
        List<CaseInput> inputsAAA = map.get(AAA);
        List<CaseInput> inputsASA = map.get(ASA);
        List<CaseInput> inputsAAJ = map.get(AAJ);
        List<CaseInput> inputsASJ = map.get(ASJ);
        List<CaseInput> inputsRAA = map.get(RAA);
        List<CaseInput> inputsRSA = map.get(RSA);
        List<CaseInput> inputsRAJ = map.get(RAJ);
        List<CaseInput> inputsRSJ = map.get(RSJ);

        // append all the job inputs to the inputsAll list
        if ((inputsAAA != null) && (inputsAAA.size() > 0)) {
            if (DebugLevels.DEBUG_0())
                System.out.println("Number of AAA inputs = " + inputsAAA.size());
            inputsAll.addAll(inputsAAA);
        }

        if ((inputsASA != null) && (inputsASA.size() > 0)) {
            if (DebugLevels.DEBUG_0())
                System.out.println("Number of ASA inputs = " + inputsASA.size());
            inputsAll.addAll(inputsASA);
        }

        if ((inputsAAJ != null) && (inputsAAJ.size() > 0)) {
            if (DebugLevels.DEBUG_0())
                System.out.println("Number of AAJ inputs = " + inputsAAJ.size());
            inputsAll.addAll(inputsAAJ);
        }

        if ((inputsASJ != null) && (inputsASJ.size() > 0)) {
            if (DebugLevels.DEBUG_0())
                System.out.println("Number of ASJ inputs = " + inputsASJ.size());
            inputsAll.addAll(inputsASJ);
        }

        if ((inputsRSA != null) && (inputsRSA.size() > 0)) {
            if (DebugLevels.DEBUG_0())
                System.out.println("Number of RSA inputs = " + inputsRSA.size());
            inputsAll.addAll(inputsRSA);
        }

        if ((inputsRAJ != null) && (inputsRAJ.size() > 0)) {
            if (DebugLevels.DEBUG_0())
                System.out.println("Number of RAJ inputs = " + inputsRAJ.size());
            inputsAll.addAll(inputsRAJ);
        }

        if ((inputsRAA != null) && (inputsRAA.size() > 0)) {
            if (DebugLevels.DEBUG_0())
                System.out.println("Number of RAA inputs = " + inputsRAA.size());
            inputsAll.addAll(inputsRAA);
        }

        if ((inputsRSJ != null) && (inputsRSJ.size() > 0)) {
            if (DebugLevels.DEBUG_0())
                System.out.println("Number of RSJ inputs = " + inputsRSJ.size());
            inputsAll.addAll(inputsRSJ);
        }

        if (DebugLevels.DEBUG_0())
            System.out.println("Total number of inputs = " + inputsAll.size());

        return (inputsAll);
    }

    private Map<String, List<CaseInput>> getInputHierarchy(CaseJob job, Session session) throws EmfException {
        Map<String, List<CaseInput>> map = new HashMap<String, List<CaseInput>>();

        /**
         * Gets all the inputs for a specific job
         */
        int caseId = job.getCaseId();
        int jobId = job.getId();

        /*
         * Need to get the inputs for 8 different scenarios: All combinations of Region, Sector, and CaseJob.
         */
        List<CaseInput> inputsAAA = null; // inputs for all regions, all sectors and all jobs
        List<CaseInput> inputsASA = null; // inputs for all regions, specific sector and all jobs
        List<CaseInput> inputsAAJ = null; // inputs for all regions, all sectors specific jobs
        List<CaseInput> inputsASJ = null; // inputs for all regions, specific sectors specific jobs
        List<CaseInput> inputsRAA = null; // inputs for specific region, all sectors and all jobs
        List<CaseInput> inputsRSA = null; // inputs for specific region, specific sector and all jobs
        List<CaseInput> inputsRAJ = null; // inputs for specific region, all sectors specific jobs
        List<CaseInput> inputsRSJ = null; // inputs for specific region, specific sectors specific jobs

        try {
            Sector sector = job.getSector();
            GeoRegion region = job.getRegion();

            // Get case inputs (the datasets associated w/ the case)
            // All regions, all sectors, all jobs
            inputsAAA = this.getJobInputs(caseId, this.ALL_JOB_ID, this.ALL_SECTORS, this.ALL_REGIONS, session);

            // All regions, sector specific, all jobs
            if (sector != this.ALL_SECTORS) {
                inputsASA = this.getJobInputs(caseId, this.ALL_JOB_ID, sector, this.ALL_REGIONS, session);
            }

            // All regions, all sectors, job specific
            inputsAAJ = this.getJobInputs(caseId, jobId, this.ALL_SECTORS, this.ALL_REGIONS, session);

            // All regions, specific sector and specific job
            if (sector != this.ALL_SECTORS) {
                inputsASJ = this.getJobInputs(caseId, jobId, sector, this.ALL_REGIONS, session);
            }

            // Specific region, all sectors, all jobs
            if (region != this.ALL_REGIONS)
                inputsRAA = this.getJobInputs(caseId, this.ALL_JOB_ID, this.ALL_SECTORS, region, session);

            // Region specific, sector specific, all jobs
            if (sector != this.ALL_SECTORS && region != this.ALL_REGIONS) {
                inputsRSA = this.getJobInputs(caseId, this.ALL_JOB_ID, sector, region, session);
            }

            // Region specific, all sectors, job specific
            if (region != this.ALL_REGIONS)
                inputsRAJ = this.getJobInputs(caseId, jobId, this.ALL_SECTORS, region, session);

            // Region specific, specific sector and specific job
            if (sector != this.ALL_SECTORS && region != this.ALL_REGIONS) {
                inputsRSJ = this.getJobInputs(caseId, jobId, sector, region, session);
            }
        } catch (Exception e) {
            log.error("Could not get all inputs for case (id=" + caseId + "), job (id=" + jobId + ").\n", e);
            // throw new EmfException("Could not get all inputs for case (id=" + caseId + "), job (id=" + jobId +
            // ").\n");
            throw new EmfException(e.getMessage());
        }

        map.put(AAA, inputsAAA);
        map.put(ASA, inputsASA);
        map.put(ASJ, inputsASJ);
        map.put(AAJ, inputsAAJ);
        map.put(RAA, inputsRAA);
        map.put(RSA, inputsRSA);
        map.put(RAJ, inputsRAJ);
        map.put(RSJ, inputsRSJ);

        return map;
    }

    public void copyCaseObject(int[] toCopy, User user) throws EmfException {
        
//        for (int i = 0; i < toCopy.length; i++) {
//            Case caseToCopy = getCase(toCopy[i]);
  
            String submitterID = getCopyService().copyCases(user, toCopy, this);
            if (DebugLevels.DEBUG_4())
                System.out.println("In ExImServiceImpl:CopyCase() SUBMITTERID = " + submitterID);
        
        
//            try {
              //  copySingleCaseObj(caseToCopy, user);
//            } catch (Exception e) {
//                log.error("Could not copy case " + caseToCopy.getName() + ".", e);
//                throw new EmfException("Could not copy case " + caseToCopy.getName() + ". " + e.getMessage());
//            }
//        }

        
    }

    public synchronized ParameterName[] getParameterNames() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<ParameterName> type = dao.getParameterNames(session);

            return type.toArray(new ParameterName[0]);
        } catch (Exception e) {
            log.error("Could not get all parameter names.\n", e);
            throw new EmfException("Could not get all parameter names.\n");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized void addCaseParameters(User user, int caseId, CaseParameter[] params) throws EmfException {
        Session session = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();
        List<GeoRegion> regions = new ArrayList<GeoRegion>();
        try {
            for (int i = 0; i < params.length; i++) {
                params[i].setCaseID(caseId);

                CaseJob job = dao.getCaseJob(params[i].getJobId());

                if (job != null) {
                    CaseJob targetJob = dao.getCaseJob(caseId, job, session);

                    if (targetJob == null)
                        throw new EmfException("Required case job: " + job.getName() + " doesn't exist in target case.");

                    params[i].setJobId(targetJob.getId());
                }

                if (dao.caseParameterExists(params[i], session))
                    throw new EmfException("Case parameter: " + params[i].getName() + " already exists in target case.");
            }

            for (CaseParameter param : params) {
                dao.addParameter(param, session);
                Sector sector = param.getSector();
                GeoRegion region = param.getRegion();
                
                if (sector != null && !sectors.contains(sector))
                    sectors.add(sector);
                if (region != null && !regions.contains(region))
                    regions.add(region);
            }
        } catch (Exception e) {
            log.error("Could not add new case parameter '" + params[0].getName() + "' etc.\n", e);
            throw new EmfException(e.getMessage());
        } finally {
            // NOTE: if there is any parameter get copied, need to add new sectors to the copied case
            // sectors list
            try {
                updateCase(user, caseId, session, sectors, regions);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + params.length + " parameters copied.");
            } finally {
                if (session != null && session.isConnected())
                    session.close();
            }
        }
    }

    private void updateCase(User user, int caseId, Session session, List<Sector> sectors, List<GeoRegion> regions) throws EmfException {
        
        boolean lockObtained = false;
        Case target = dao.getCase(caseId, session);

        if (target.isLocked() && !target.isLocked(user))
            throw new EmfException("Cannot obtain lock on target case to update sectors list.");

        Case locked = target;

        if (!target.isLocked()) {
            locked = dao.obtainLocked(user, target, session);
            lockObtained = true;
        }
        if (sectors.size() > 0) {
            Sector[] sectrs = locked.getSectors();
            List<Sector> existed = new ArrayList<Sector>();
            existed.addAll(Arrays.asList(sectrs));

            for (int i = 0; i < sectors.size(); i++)
                if (!existed.contains(sectors.get(i)))
                    existed.add(sectors.get(i));

            locked.setSectors(existed.toArray(new Sector[0]));
        }
        
        if (regions.size() > 0) {
            GeoRegion[] regs = locked.getRegions();
            List<GeoRegion> existed = new ArrayList<GeoRegion>();
            existed.addAll(Arrays.asList(regs));

            for (int i = 0; i < regions.size(); i++)
                if (!existed.contains(regions.get(i)))
                    existed.add(regions.get(i));

            locked.setRegions(existed.toArray(new GeoRegion[0]));
        }
        
        locked.setLastModifiedBy(user);
        locked.setLastModifiedDate(new Date());

        if (lockObtained) {
            dao.update(locked, session);
            return;
        }
        dao.updateWithLock(locked, session);
    } 

    private synchronized void addCaseParameters(int caseId, CaseParameter[] params, String jobPrefix, GeoRegion region)
            throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            for (int i = 0; i < params.length; i++) {
                params[i].setCaseID(caseId);

                CaseJob job = dao.getCaseJob(params[i].getJobId());
                //this is where we need to change the region in job object to the user selected region if 
                //region value is not null inside the job

                if (job != null) {
                    if (job.getRegion() != null && region != null) job.setRegion(region);
                    job.setName(jobPrefix + job.getName());
                    
                    CaseJob targetJob = dao.getCaseJob(caseId, job, session);

                    if (targetJob == null)
                        throw new EmfException("Required case job: " + jobPrefix + job.getName()
                                + " doesn't exist in target case.");

                    params[i].setJobId(targetJob.getId());
                }

                if (dao.caseParameterExists(params[i], session))
                    throw new EmfException("Case parameter: " + params[i].getName() + " already exists in target case.");
            }

            for (CaseParameter param : params)
                dao.addParameter(param, session);
        } catch (Exception e) {
            log.error("Could not add new case parameter '" + params[0].getName() + "' etc.\n", e);
            throw new EmfException(e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseParameter addCaseParameter(User user, CaseParameter param, boolean copyingCase)
            throws EmfException {
        Session session = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();
        List<GeoRegion> regions = new ArrayList<GeoRegion>();
        try {
            if (!copyingCase)
                checkNExtendCaseLock(user, getCase(param.getCaseID(), session));
        } catch (EmfException e) {
            throw e;
        }

        try {
            if (dao.caseParameterExists(param, session))
                throw new EmfException(
                        "The combination of 'Parameter Name', 'Region', 'Sector', 'Program', and 'Job' should be unique.");

            dao.addParameter(param, session);
            
            Sector sector = param.getSector();
            GeoRegion region = param.getRegion();

            if (sector != null && !sectors.contains(sector))
                sectors.add(sector);
            if (region != null && !regions.contains(region))
                regions.add(region);
            
            return dao.loadCaseParameter(param, session);
        } catch (Exception e) {
            log.error("Could not add new case parameter '" + param.getName() + "'\n", e);
            throw new EmfException(e.getMessage() == null ? "Could not add new case parameter." : e.getMessage());
        } finally {
            try {
                updateCase(user, param.getCaseID(), session, sectors, regions);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + "Case can not be updated. ");
            } finally {      
                if (session != null && session.isConnected())
                    session.close();
            }
        }
    }

    public synchronized void removeCaseParameters(User user, CaseParameter[] params) throws EmfException {
        Session session = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();
        List<GeoRegion> regions = new ArrayList<GeoRegion>();
        try {
            dao.removeCaseParameters(params, session);
        } catch (Exception e) {
            log.error("Could not remove case parameter " + params[0].getName() + " etc.\n", e);
            throw new EmfException("Could not remove case parameter " + params[0].getName() + " etc.");
        } finally {
            try {
                updateCase(user, params[0].getCaseID(),session, sectors, regions);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + "Case can not be updated. ");
            } finally {      
                if (session != null && session.isConnected())
                    session.close();
            }
        }
        
        
    }

    public CaseParameter getCaseParameter(int caseId, ParameterEnvVar var) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return dao.getCaseParameter(caseId, var, session);
        } catch (Exception e) {
            log.error("Could not get parameter for case (id=" + caseId + ") and environment variable: " + var.getName()
                    + ".\n", e);
            throw new EmfException("Could not get parameter for case (id=" + caseId + ") and environment variable: "
                    + var.getName() + ".\n");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseParameter[] getCaseParameters(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseParameter> params = dao.getCaseParameters(caseId, session);

            Collections.sort(params, new Comparator<CaseParameter>() {
                public int compare(CaseParameter o1, CaseParameter o2) {
                    int sectorId1 = (o1.getSector() == null) ? 0 : o1.getSector().getId();
                    int sectorId2 = (o2.getSector() == null) ? 0 : o2.getSector().getId();

                    int envId1 = (o1.getEnvVar() == null) ? 0 : o1.getEnvVar().getId();
                    int envId2 = (o2.getEnvVar() == null) ? 0 : o2.getEnvVar().getId();

                    int jobId1 = o1.getJobId();
                    int jobId2 = o2.getJobId();

                    if (sectorId1 > sectorId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 > envId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 > jobId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 == jobId2)
                        return 0;

                    return -1;
                }
            });

            return params.toArray(new CaseParameter[0]);
        } catch (Exception e) {
            log.error("Could not get all parameters for case (id=" + caseId + ").\n", e);
            throw new EmfException("Could not get all parameters for case (id=" + caseId + ").\n");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseParameter[] getCaseParameters(int caseId, int[] jobIds) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseParameter> params = dao.getCaseParametersByJobIds(caseId, jobIds, session);

            return params.toArray(new CaseParameter[0]);
        } catch (Exception e) {
            log.error("Could not get all parameters for case (id=" + caseId + ").\n", e);
            throw new EmfException("Could not get all parameters for case (id=" + caseId + ").\n");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized CaseParameter[] getCaseParameters(int pageSize, int caseId, Sector sector,
            String envNameContains, boolean showAll) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<CaseParameter> params = dao.getCaseParameters(pageSize, caseId, sector, envNameContains, showAll,
                    session);

            Collections.sort(params, new Comparator<CaseParameter>() {
                public int compare(CaseParameter o1, CaseParameter o2) {
                    int sectorId1 = (o1.getSector() == null) ? 0 : o1.getSector().getId();
                    int sectorId2 = (o2.getSector() == null) ? 0 : o2.getSector().getId();

                    int envId1 = (o1.getEnvVar() == null) ? 0 : o1.getEnvVar().getId();
                    int envId2 = (o2.getEnvVar() == null) ? 0 : o2.getEnvVar().getId();

                    int jobId1 = o1.getJobId();
                    int jobId2 = o2.getJobId();

                    if (sectorId1 > sectorId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 > envId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 > jobId2)
                        return 1;
                    else if (sectorId1 == sectorId2 && envId1 == envId2 && jobId1 == jobId2)
                        return 0;

                    return -1;
                }
            });

            return params.toArray(new CaseParameter[0]);
        } catch (Exception e) {
            log.error("Could not get all parameters for case (id=" + caseId + ").\n", e);
            throw new EmfException("Could not get all parameters for case (id=" + caseId + ").\n");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized Executable addExecutable(Executable exe) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.exeutableExists(session, exe))
                dao.add(exe, session);

            return (Executable) dao.load(Executable.class, exe.getName(), session);
        } catch (Exception e) {
            log.error("Could not add new executable '" + exe.getName() + "'\n", e);
            throw new EmfException("Could not add new executable '" + exe.getName() + "'");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized ParameterName addParameterName(ParameterName name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.addParameterName(name, session);
            Criterion crit1 = Restrictions.eq("name", name.getName());
            Criterion crit2 = Restrictions.eq("modelToRunId", new Integer(name.getModelToRunId()));
            return (ParameterName) dao.load(ParameterName.class, new Criterion[] { crit1, crit2 }, session);
        } catch (Exception e) {
            log.error("Could not add new parameter name '" + name.getName() + "'\n", e);
            throw new EmfException("Could not add new parameter name '" + name.getName() + "'");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized ValueType[] getValueTypes() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<ValueType> type = dao.getValueTypes(session);

            return type.toArray(new ValueType[0]);
        } catch (Exception e) {
            log.error("Could not get all value types.\n", e);
            throw new EmfException("Could not get all value types.\n");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized ValueType addValueType(ValueType type) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.addValueType(type, session);
            return (ValueType) dao.load(ValueType.class, type.getName(), session);
        } catch (Exception e) {
            log.error("Could not add new value type '" + type.getName() + "'\n", e);
            throw new EmfException("Could not add new value type '" + type.getName() + "'");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized ParameterEnvVar[] getParameterEnvVars() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<ParameterEnvVar> envvar = dao.getParameterEnvVars(session);

            return envvar.toArray(new ParameterEnvVar[0]);
        } catch (Exception e) {
            log.error("Could not get all parameter env variables.\n", e);
            throw new EmfException("Could not get all parameter env variables.\n");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized ParameterEnvVar addParameterEnvVar(ParameterEnvVar envVar) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(envVar, session);
            Criterion crit1 = Restrictions.eq("name", envVar.getName());
            Criterion crit2 = Restrictions.eq("modelToRunId", new Integer(envVar.getModelToRunId()));
            return (ParameterEnvVar) dao.load(ParameterEnvVar.class, new Criterion[] { crit1, crit2 }, session);
        } catch (Exception e) {
            log.error("Could not add new parameter env variable '" + envVar.getName() + "'\n", e);
            throw new EmfException("Could not add new parameter env variable '" + envVar.getName() + "'");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized void addCaseJobs(User user, int caseId, CaseJob[] jobs) throws EmfException {
        Session session = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();
        List<GeoRegion> regions = new ArrayList<GeoRegion>();
        
        try {
            for (CaseJob job : jobs)
                if (dao.getCaseJob(caseId, job, session) != null)
                    throw new EmfException("Case job: " + job.getName() + " already exists in the target case.");

            for (CaseJob job : jobs) {
                job.setCaseId(caseId);
                job.setJobkey(null); // jobkey supposedly generated when it is run
                job.setRunLog(null);
                job.setRunStartDate(null);
                job.setRunCompletionDate(null);
                job.setIdInQueue(null);
                job.setRunstatus(dao.getJobRunStatuse("Not Started"));
                dao.add(job, session);
                Sector sector = job.getSector();
                GeoRegion region = job.getRegion();
                
                if (sector != null && !sectors.contains(sector))
                    sectors.add(job.getSector());
                if (region != null && !regions.contains(region))
                    regions.add(region);
            }
        } catch (EmfException e) {
            log.error("Could not add new case jobs '" + jobs[0].getName() + "' etc.\n" + e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            // NOTE: if any jobs get copied, we need to update target case sector list
            try {
                updateCase(user, caseId, session, sectors, regions);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + jobs.length + " jobs copied.");
            } finally {
                session.close();
            }
        }
    }

    public synchronized void addCaseJobs4Sensitivity(User user, int caseId, CaseJob[] jobs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            for (CaseJob job : jobs)
                if (dao.getCaseJob(caseId, job, session) != null)
                    throw new EmfException("Case job: " + job.getName() + " already exists in the target case.");

            for (CaseJob job : jobs) {
                job.setCaseId(caseId);
                job.setJobkey(null); // jobkey supposedly generated when it is run
                job.setRunLog(null);
                job.setRunStartDate(null);
                job.setRunCompletionDate(null);
                job.setRunstatus(dao.getJobRunStatuse("Not Started"));
                dao.add(job, session);
            }
        } catch (EmfException e) {
            log.error("Could not add new case jobs '" + jobs[0].getName() + "' etc.\n" + e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized CaseJob addCaseJob(User user, CaseJob job, boolean copyingCase) throws EmfException {
        Session session = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();
        List<GeoRegion> regions = new ArrayList<GeoRegion>();
        try {
            if (!copyingCase) // if not copying case, automatically extend the lock on case object
                checkNExtendCaseLock(user, getCase(job.getCaseId(), session));
        } catch (EmfException e) {
            throw e;
        }

        try {
            if (job.getRunstatus() == null)
                job.setRunstatus(dao.getJobRunStatuse("Not Started"));
            
            if (dao.caseJobExists(job, session))
                throw new EmfException(
                        "The combination of 'Name', 'Region', 'Sector' should be unique.");

            job.setIdInQueue(null);
            dao.add(job, session);
            
            Sector sector = job.getSector();
            GeoRegion region = job.getRegion();

            if (sector != null && !sectors.contains(sector))
                sectors.add(sector);
            if (region != null && !regions.contains(region))
                regions.add(region);
            
            session.clear();
            
            return dao.loadCaseJob(job, session);
        } catch (Exception e) {
            log.error("Adding new job " + job.getName() + " failed.", e);
            throw new EmfException(e.getMessage());
        } finally {
            try {
                updateCase(user, job.getCaseId(), session, sectors, regions);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + "Case can not be updated. ");
            } finally {      
                if (session != null && session.isConnected())
                    session.close();
            }
        }
    }

    public synchronized CaseJob[] getCaseJobs(int caseId) throws EmfException {

        Session session = sessionFactory.getSession();

        try {
            List<CaseJob> jobs = dao.getCaseJobs(caseId, session);

            if (jobs == null || jobs.size() == 0)
                return new CaseJob[0];

            Collections.sort(jobs);
            return jobs.toArray(new CaseJob[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all jobs for case (id=" + caseId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all jobs for case (id=" + caseId + ").\n");
        } finally {
            session.close();
        }
    }

    public synchronized JobRunStatus[] getJobRunStatuses() throws EmfException {

        Session session = sessionFactory.getSession();

        try {
            List<JobRunStatus> runstatuses = dao.getJobRunStatuses(session);

            return runstatuses.toArray(new JobRunStatus[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all job run statuses.\n" + e.getMessage());
            throw new EmfException("Could not get all job run statuses.\n");
        } finally {
            session.close();
        }
    }

    public synchronized Executable[] getExecutables(int casejobId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<Executable> runstatuses = dao.getExecutables(session);

            return runstatuses.toArray(new Executable[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all executables.\n" + e.getMessage());
            throw new EmfException("Could not get all executables.\n");
        } finally {
            session.close();
        }
    }

    private void resetRelatedJobsField(CaseJob[] jobs) throws EmfException {
        int jobslen = jobs.length;

        if (jobslen == 0)
            return;

        Session session = sessionFactory.getSession();

        try {
            int caseId = jobs[0].getCaseId();
            CaseInput[] inputs = dao.getCaseInputs(caseId, session).toArray(new CaseInput[0]);
            CaseParameter[] params = dao.getCaseParameters(caseId, session).toArray(new CaseParameter[0]);

            for (int i = 0; i < jobslen; i++) {
                for (int j = 0; j < inputs.length; j++)
                    if (inputs[j].getCaseJobID() == jobs[i].getId())
                        inputs[j].setCaseJobID(0);

                for (int k = 0; k < params.length; k++)
                    if (params[k].getJobId() == jobs[i].getId())
                        params[k].setJobId(0);

                jobs[i].setDependentJobs(null);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not reset case job.\n" + e.getMessage());
            throw new EmfException("Could not reset case job ");
        } finally {
            session.close();
        }

    }

    public synchronized void removeCaseJobs(User user, CaseJob[] jobs) throws EmfException {
        Session session = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();
        List<GeoRegion> regions = new ArrayList<GeoRegion>();
        
        checkJobsStatuses(Arrays.asList(jobs), dao.getCase(jobs[0].getCaseId(), session));
        checkTaskPersistItems(jobs, session);
        checkJobOutputItems(jobs, session);
        checkJobHistoryItems(jobs, session);
        resetRelatedJobsField(jobs);
        deleteCaseJobKeyObjects(jobs, session);

        try {
            dao.checkJobDependency(jobs, session);
        } catch (EmfException e) {
            throw new EmfException("Cannot remove " + e.getMessage());
        }

        try {
            session.clear();
            dao.removeCaseJobs(jobs, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not remove case job " + jobs[0].getName() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove selected job(s): " + e.getLocalizedMessage());
        } finally {
            try {
                updateCase(user, jobs[0].getCaseId(),session, sectors, regions);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + "Case can not be updated. ");
            } finally {    
                if (session != null && session.isConnected())
                    session.close();
            }
        }
        
    }

    private void checkJobOutputItems(CaseJob[] jobs, Session session) throws EmfException {
        String query = "SELECT obj.name from " + CaseOutput.class.getSimpleName() + " obj WHERE obj.jobId = "
                + getAndOrClause(jobs, "obj.jobId");
        List<?> list = session.createQuery(query).list();

        if (list != null && list.size() > 0)
            throw new EmfException("Please remove case outputs for the selected jobs.");
    }

    private void checkJobHistoryItems(CaseJob[] jobs, Session session) throws EmfException {
        String query = "SELECT obj.message from " + JobMessage.class.getSimpleName() + " obj WHERE obj.jobId = "
                + getAndOrClause(jobs, "obj.jobId");
        List<?> list = session.createQuery(query).list();

        if (list != null && list.size() > 0)
            throw new EmfException("Please remove job messages for the selected jobs.");
    }

    private void checkTaskPersistItems(CaseJob[] jobs, Session session) throws EmfException {
        String query = "SELECT obj.id from " + PersistedWaitTask.class.getSimpleName() + " obj WHERE obj.jobId = "
                + getAndOrClause(jobs, "obj.jobId");
        List<?> list = session.createQuery(query).list();

        if (list != null && list.size() > 0)
            throw new EmfException("Cannot delete job(s) -- selected job(s) is still active.");
    }

    private void deleteCaseJobKeyObjects(CaseJob[] jobs, Session session) throws EmfException {
        int updatedItems = 0;

        try {
            Transaction tx = session.beginTransaction();

            String query = "DELETE " + CaseJobKey.class.getSimpleName() + " obj WHERE obj.jobId = "
                    + getAndOrClause(jobs, "obj.jobId");

            if (DebugLevels.DEBUG_16())
                System.out.println("hql delete string: " + query);

            updatedItems = session.createQuery(query).executeUpdate();
            tx.commit();

            if (DebugLevels.DEBUG_16())
                System.out.println(updatedItems + " items updated.");
        } catch (HibernateException e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (DebugLevels.DEBUG_16())
                log.warn(updatedItems + " items updated from " + CaseJobKey.class.getName() + " table.");
        }
    }

    private String getAndOrClause(CaseJob[] jobs, String attrName) {
        StringBuffer sb = new StringBuffer();
        int numIDs = jobs.length;

        if (numIDs == 1)
            return "" + jobs[0].getId();

        for (int i = 0; i < numIDs - 1; i++)
            sb.append(jobs[i].getId() + " OR " + attrName + " = ");

        sb.append(jobs[numIDs - 1].getId());

        return sb.toString();
    }


    public synchronized void exportInputsForCase(User user, String dirName, String purpose, boolean overWrite,
            int caseId) throws EmfException {
        EmfDataset[] datasets = getInputDatasets(caseId);
        Version[] versions = getInputDatasetVersions(caseId);
        SubDir[] subdirs = getSubdirs(caseId);

        if (datasets.length == 0)
            return;

        File inputsDir = new File(dirName);

        if (!inputsDir.exists()) {
            inputsDir.mkdirs();
            inputsDir.setWritable(true, false);
        }

        for (int i = 0; i < datasets.length; i++) {
            String subdir = (subdirs[i] == null) ? "" : subdirs[i].getName();
            String exportDir = dirName + System.getProperty("file.separator") + subdir;
            File dir = new File(exportDir);

            if (!dir.exists()) {
                dir.mkdirs();
                setDirsWritable(new File(dirName), dir);
            }

            getExportService().exportForClient(user, new EmfDataset[] { datasets[i] }, new Version[] { versions[i] },
                    exportDir, purpose, overWrite);
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

    public synchronized void updateCaseParameter(User user, CaseParameter parameter) throws EmfException {
        Session localSession = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();
        List<GeoRegion> regions = new ArrayList<GeoRegion>();
        
        try {
            checkNExtendCaseLock(user, getCase(parameter.getCaseID(), localSession));
        } catch (EmfException e) {
            throw e;
        }

        try {
            CaseParameter loaded = dao.loadCaseParameter(parameter, localSession);

            if (loaded != null && loaded.getId() != parameter.getId())
                throw new EmfException("Case parameter uniqueness check failed (" + loaded.getId() + ","
                        + parameter.getId() + ")");

            // FIXME: why session.clear()?
            localSession.clear();
            dao.updateCaseParameter(parameter, localSession);
            
            Sector sector = parameter.getSector();
            GeoRegion region = parameter.getRegion();

            if (sector != null && !sectors.contains(sector))
                sectors.add(sector);
            if (region != null && !regions.contains(region))
                regions.add(region);

            // setStatus(user, "Saved parameter " + parameter.getName() + " to database.", "Save Parameter");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case parameter: " + parameter.getName() + ".\n" + e);
            throw new EmfException("Could not update case parameter: " + parameter.getName() + ".");
        } finally {
            try {
                updateCase(user, parameter.getCaseID(), localSession, sectors, regions);
            } catch (Exception e) {
                throw new EmfException(e.getMessage() + " " + "Case can not be updated. ");
            } finally {      
                if (localSession != null && localSession.isConnected())
                    localSession.close();
            }
         
        }
    }

    /**
     * SubmitJobs(...) is called form the CaseServiceImpl.
     * 
     * 
     */
    public synchronized String submitJobs(Integer[] jobIds, int caseId, User user) throws EmfException {
        if (DebugLevels.DEBUG_0())
            System.out.println("ManagedCaseService::submitJobs size: " + jobIds.length + " for caseId= " + caseId);

        // create a new caseJobSubmitter for each client call in a session
        TaskSubmitter caseJobSubmitter = new CaseJobSubmitter(sessionFactory);

        Hashtable<String, CaseJob> caseJobsTable = new Hashtable<String, CaseJob>();
        ManagedExportService expSvc = null;

        CaseJobTask[] caseJobsTasksInSubmission = null;
        ArrayList<CaseJobTask> caseJobsTasksList = new ArrayList<CaseJobTask>();

        Case jobCase = this.getCase(caseId);

        if (DebugLevels.DEBUG_15()) {
            // logNumDBConn("beginning of job submitter");
        }

        Session session = sessionFactory.getSession();

        try {
            String caseJobExportSubmitterId = null;
            String caseJobSubmitterId = caseJobSubmitter.getSubmitterId();

            // Test input directory is not empty

            if ((jobCase.getInputFileDir() == null) || (jobCase.getInputFileDir().equals(""))) {
                throw new EmfException("Input directory must be set to run job(s).");
            }
            if (DebugLevels.DEBUG_0())
                System.out.println("Is CaseJobSubmitterId null? " + (caseJobSubmitterId == null));
            // FIXME: Does this need to be done in a new DAO method???
            // Get the CaseJobs for each jobId
            // create a CaseJobTask per CaseJob.
            // save the CaseJobTask in an array and the CaseJob in the hashTable with
            // the casejjobtask unique id as the key to find the casejob
            for (Integer jobId : jobIds) {
                int jid = jobId.intValue();

                if (DebugLevels.DEBUG_15()) {
                    // logNumDBConn("beginning of job submitter loop (jobID: " + jid + ")");
                }

                String jobKey = null;

                if (DebugLevels.DEBUG_0())
                    System.out.println("The jobId= " + jid);

                CaseJob caseJob = this.getCaseJob(jid, session);

                // NOTE: This is where the jobkey is generated and set in the CaseJob
                jobKey = this.createJobKey(jid);

                // set the job key in the case job
                caseJob.setJobkey(jobKey);

                // set the run user for the case job
                caseJob.setRunJobUser(user);

                // reset job run log
                caseJob.setRunLog("");

                // clear previous job queue id
                caseJob.setIdInQueue(null);

                // NOTE: does this affect job run???
                this.updateCaseJob(user, caseJob);

                // FIXME: Is this still needed?????
                // caseJob.setRunStartDate(new Date());
                if (DebugLevels.DEBUG_15()) {
                    logNumDBConn("beginning of job task creation (jobID: " + jid + ")");
                }

                CaseJobTask cjt = new CaseJobTask(jid, caseId, user);
                cjt.setJobkey(jobKey);
                cjt.setNumDepends(caseJob.getDependentJobs().length);

                // get or create the reference to the Managed Export Service for this casejobtask
                expSvc = this.getExportService();

                if (DebugLevels.DEBUG_6())
                    System.out.println("set the casejobsubmitter id = " + caseJobSubmitterId);

                cjt.setSubmitterId(caseJobSubmitterId);

                if (DebugLevels.DEBUG_9())
                    System.out.println("before getJobFileName");

                String jobFileName = this.getJobFileName(caseJob, session);
                String jobLogFile = this.getLog(jobFileName);

                if (DebugLevels.DEBUG_6())
                    System.out.println("setJobFileContent");

                if (DebugLevels.DEBUG_9())
                    System.out.println("before setJobFileContent");

                cjt.setJobFileContent(this.createJobFileContent(caseJob, user, jobFileName, jobLogFile, expSvc, session));

                if (DebugLevels.DEBUG_15()) {
                    logNumDBConn("after creation of job file (jobID: " + jid + ")");
                }

                cjt.setJobFile(jobFileName);
                cjt.setLogFile(jobLogFile);
                cjt.setJobName(caseJob.getName());
                if (DebugLevels.DEBUG_6())
                    System.out.println("set Host Name");
                cjt.setHostName(caseJob.getHost().getName());
                if (DebugLevels.DEBUG_6())
                    System.out.println("getQueOptions");

                String queueOptions = caseJob.getQueOptions();

                // replace joblog in queue options
                queueOptions = queueOptions.replace("$EMF_JOBLOG", jobLogFile);
                // If other parameters are in the queue options, translate them to their
                // string values in the queue
                try {
                    // Queue options could be job specific, therefore pass job, sector, and region
                    queueOptions = dao.replaceEnvVarsCase(queueOptions, " ", jobCase, jobId, caseJob.getSector(),
                            caseJob.getRegion());
                } catch (Exception e) {
                    System.out.println(e);
                    throw new EmfException("Job (" + cjt.getJobName() + "): " + e.getMessage());
                }
                if (DebugLevels.DEBUG_6())
                    System.out.println("Queue options: " + queueOptions);
                cjt.setQueueOptions(queueOptions);

                if (DebugLevels.DEBUG_6())
                    System.out.println("Completed setting the CaseJobTask");

                // Now add the CaseJobTask to the caseJobsTasksList
                caseJobsTasksList.add(cjt);
                // Add the caseJob to the Hashtable caseJobsTable with the cjt taskid as the key
                caseJobsTable.put(cjt.getTaskId(), caseJob);

            }// for jobIds

            // convert the caseJobsTasksList to an array caseJobsTasksInSubmission
            caseJobsTasksInSubmission = caseJobsTasksList.toArray(new CaseJobTask[0]);

            // Now sort the Array using the built in comparator
            Arrays.sort(caseJobsTasksInSubmission);

            for (CaseJobTask cjt : caseJobsTasksInSubmission) {

                if (DebugLevels.DEBUG_15()) {
                    logNumDBConn("beginning of job task loop (jobID: " + cjt.getJobId() + ")");
                }

                // get the caseJob out of the hashtable
                CaseJob caseJob = caseJobsTable.get(cjt.getTaskId());

                if (DebugLevels.DEBUG_0())
                    System.out.println("Is the caseJob for this jobId null? " + (caseJob == null));

                // now get the Case (called jobCase since case is a reserved word in Java) using
                // the caseId sent in from the GUI
                // Case jobCase = this.getCase(caseId, session);
                cjt.setCaseName(jobCase.getName());

                String purpose = "Used by job: " + caseJob.getName() + " of Case: " + jobCase.getName();
                if (DebugLevels.DEBUG_6())
                    System.out.println("Purpose= " + purpose);

                if (DebugLevels.DEBUG_0())
                    System.out.println("caseId= " + caseId + " Is the Case for this job null? " + (jobCase == null));

                List<CaseInput> inputs = getAllJobInputs(caseJob, session);

                // test inputs for which need to be exported
                List<CaseInput> inputs2Export = getInputs2Export(expSvc, inputs, caseJob, jobCase, user, purpose); // BUG3589

                // if no inputs need to be exported, set job tasks exports to success and set status to waiting
                String runStatusValue = "Exporting";
                if (inputs2Export == null || inputs2Export.size() == 0) {
                    cjt.setExportsSuccess(true);
                    runStatusValue = "Waiting";
                }

                if (DebugLevels.DEBUG_9())
                    System.out.println("Number of inputs for this job: " + inputs.size());

                if (DebugLevels.DEBUG_9())
                    System.out.println("Number of inputs to be exported for this job: " + inputs2Export.size());

                if (DebugLevels.DEBUG_15()) {
                    logNumDBConn("beginning of adding job task (jobID: " + cjt.getJobId() + ")");
                }

                // send the casejobtask to the CJTM priority queue and then to wait queue
                TaskManagerFactory.getCaseJobTaskManager(sessionFactory).addTask(cjt);

                if (DebugLevels.DEBUG_15()) {
                    logNumDBConn("before submitting to export (jobID: " + cjt.getJobId() + ")");
                }

                // Now update the casejob in the database
                caseJob.setRunstatus(getJobRunStatus(runStatusValue, session));
                caseJob.setRunStartDate(new Date());
                updateJob(caseJob, session);

                // pass the inputs to the exportService which uses an exportJobSubmitter to work with exportTaskManager
                if (!cjt.isExportsSuccess() && !this.doNotExportJobs(session)) {
                    caseJobExportSubmitterId = expSvc.exportForJob(user, inputs2Export, cjt.getTaskId(), purpose,
                            caseJob, jobCase); // BUG3589
                } else {
                    log.warn("ManagedCaseService: case jobs related datasets are not exported.");
                }

                if (DebugLevels.DEBUG_15()) {
                    logNumDBConn("after submitted to export (jobID: " + cjt.getJobId() + ")");
                }

                if (DebugLevels.DEBUG_6())
                    System.out.println("Added caseJobTask to collection");

                if (DebugLevels.DEBUG_0())
                    System.out.println("Case Job Export Submitter Id for case job:" + caseJobExportSubmitterId);

            }// for cjt

            // Process the case job task manager wait queue
            // if a job has no new exports, this signals the task queue to
            // process it
            TaskManagerFactory.getCaseJobTaskManager(sessionFactory).processTaskQueue();

            if (DebugLevels.DEBUG_0())
                System.out.println("Case Job Submitter Id for case job:" + caseJobSubmitterId);

            return caseJobSubmitterId;
        } catch (EmfException ex) {
            ex.printStackTrace();
            throw new EmfException(ex.getMessage());
        } finally {
            if (DebugLevels.DEBUG_15()) {
                logNumDBConn("finished job submission");
            }

            if (session != null && session.isConnected())
                session.close();
        }
    }

    private List<CaseInput> getInputs2Export(ManagedExportService exptSrv, List<CaseInput> inputs, CaseJob job,
            Case caseObj, User user, String purpose) throws EmfException {
        // Determining which inputs already have been exported
        // return a list of those inputs which still need to be exported
        if (inputs == null || inputs.size() == 0)
            return inputs;

        List<CaseInput> toExport = new ArrayList<CaseInput>();
        String delimeter = System.getProperty("file.separator");
        LoggingServiceImpl logSvr = exptSrv.services().getLoggingService();

        Session session = this.sessionFactory.getSession();
        DatasetDAO dsdao = new DatasetDAO();

        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            boolean needExport = false;
            EmfDataset dataset = input.getDataset();
            SubDir subdir = input.getSubdirObj();
            String fullPath = null;

            // check if dataset is null, if so exception
            if (dataset == null) {
                throw new EmfException("Input (" + input.getName() + ") must have a dataset");
            }

            // check for external dataset
            if (dataset.isExternal()) {
                ExternalSource[] extSrcs = null;

                try {
                    extSrcs = dsdao.getExternalSrcs(dataset.getId(), -1, null, session);
                } catch (Exception e) {
                    log.error("Could not get external sources for dataset " + dataset.getName(), e);
                    throw new EmfException("Could not get external sources for dataset " + dataset.getName() + ".");
                }

                // test that all the external files in the dataset exist
                if (extSrcs == null || extSrcs.length == 0) {
                    throw new EmfException("Input (" + input.getName() + ") must have at least 1 external dataset");
                }

                // loop of external ds, if all there add to toExport list
                for (int i = 0; i < extSrcs.length; i++) {
                    fullPath = extSrcs[i].getDatasource();

                    if (!new File(fullPath).exists()) {
                        needExport = true;
                        break;
                    }
                }
            } else {
                // internal dataset
                // Create a full path to the input file
                fullPath = exptSrv.getCleanDatasetName(input.getDataset(), input.getVersion());

                if ((subdir != null) && !(subdir.toString()).equals("")) {
                    fullPath = caseObj.getInputFileDir() + delimeter + input.getSubdirObj() + delimeter + fullPath;
                } else {
                    fullPath = caseObj.getInputFileDir() + delimeter + fullPath;
                }

                // Expand input director, ie. remove env variables
                try {
                    // full path based on input dir, which is case general, therefore don't pass job, sector, or region
                    fullPath = dao.replaceEnvVarsCase(fullPath, delimeter, caseObj, this.ALL_JOB_ID, this.ALL_SECTORS,
                            this.ALL_REGIONS);
                } catch (Exception e) {
                    throw new EmfException("Input folder: " + e.getMessage());
                }

                if (!new File(fullPath).exists())
                    needExport = true;
            }

            if (needExport)
                toExport.add(input);

            // ATTENTION!
            // The following is what we want! Do not disable that! We want to log the access EVEN if the datset not get exported,
            // since it is used by cases - but we 0 the time required when do the access logging.
            //if (!DebugLevels.BUG3589Fixed) { // should NOT log access from here! log the access in ExportTask
            if (!needExport)
                exptSrv.logExportedTask(logSvr, user, purpose, fullPath, input); // BUG3589
            //}
        }

        return toExport;
    }

    private JobRunStatus getJobRunStatus(String runStatus, Session session) throws EmfException {
        JobRunStatus jrStat = null;

        try {
            jrStat = dao.getJobRunStatuse(runStatus);

            return jrStat;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get job run status.\n" + e.getMessage());
            throw new EmfException("Could not get job run status.\n");
        }
    }

    private synchronized void updateJob(CaseJob caseJob, Session session) throws EmfException {
        try {
            dao.updateCaseJob(caseJob, session);
            dao.updateCaseJobKey(caseJob.getId(), caseJob.getJobkey(), session);
            session.flush();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public synchronized void updateCaseJob(User user, CaseJob job) throws EmfException {
        try {
            CaseJob loaded = dao.loadUniqueCaseJob(job);
            
            if (user == null)
                throw new EmfException("Running Case Job requires a valid user");

            if (loaded != null && loaded.getId() != job.getId())
                throw new EmfException("Case job uniqueness check failed (" + loaded.getId() + "," + job.getId() + ")");

            dao.updateCaseJob(job);

            // This is a manual update of the waiting tasks in CaseJobTask manager
            // If CaseJobTaskManager is not null. check the dependencies in the WaitTable
            TaskManagerFactory.getCaseJobTaskManager(sessionFactory).callBackFromJobRunServer();

        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case job: " + job.getName() + ".\n" + e);
            throw new EmfException("Could not update case job: " + job.getName() + ".");
        }
    }

    public synchronized void updateCaseJobStatus(CaseJob job) throws EmfException {
        try {
            dao.updateCaseJob(job);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case job: " + job.getName() + ".\n" + e);
            throw new EmfException("Could not update case job: " + job.getName() + ".");
        }
    }

    public synchronized void saveCaseJobFromClient(User user, CaseJob job) throws EmfException {
        Session localSession = sessionFactory.getSession();
        List<Sector> sectors = new ArrayList<Sector>();
        List<GeoRegion> regions = new ArrayList<GeoRegion>();
       
        CaseJob loaded = dao.loadUniqueCaseJob(job);

        if (loaded != null && loaded.getId() != job.getId())
            throw new EmfException("The combination of 'Name', 'Region', 'Sector' should be unique.");

        Sector sector = job.getSector();
        GeoRegion region = job.getRegion();

        if (sector != null && !sectors.contains(sector))
            sectors.add(sector);
        if (region != null && !regions.contains(region))
            regions.add(region);

        try {
            updateCase(user, job.getCaseId(), localSession, sectors, regions );
        } catch (Exception e) {
            throw new EmfException(e.getMessage() + " " + "Case can not be updated. ");        
        }
        
        try {
            checkNExtendCaseLock(user, getCase(job.getCaseId(), localSession));
        } catch (EmfException e) {
            throw e;
        } finally {
            localSession.close();
        }

        try {
            // maintain current running user from server
            // do NOT update the running user from the client
            job.setRunJobUser(dao.getCaseJob(job.getId()).getRunJobUser());

            dao.updateCaseJob(job);
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case job: " + job.getName() + ".\n", e);
            throw new EmfException("Could not update case job: " + job.getName() + ".");
        }
    }

    public synchronized Host[] getHosts() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<Host> hosts = dao.getHosts(session);

            return hosts.toArray(new Host[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all hosts.\n" + e.getMessage());
            throw new EmfException("Could not get all hosts.\n");
        } finally {
            session.close();
        }
    }

    public synchronized Host addHost(Host host) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(host, session);
            return (Host) dao.load(Host.class, host.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new host '" + host.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new host '" + host.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized void exportCaseInputs(User user, Integer[] caseInputIds, String purpose) throws EmfException {

        for (Integer caseInputId : caseInputIds) {
            CaseInput caseInput = this.getCaseInput(caseInputId.intValue());
            Case caseObj = this.getCase(caseInput.getCaseID());

            EmfDataset ds = caseInput.getDataset();
            Version version = caseInput.getVersion();
            SubDir subdirObj = caseInput.getSubdirObj();

            String subDir = "";

            if (subdirObj != null) {
                if (subdirObj.getName() != null) {
                    subDir = subdirObj.getName();
                }
            }

            String delimeter = System.getProperty("file.separator");
            String exportDir = caseObj.getInputFileDir() + delimeter + subDir;
            // export Dir is case general, therefore don't pass job, sector, or region
            String exportDirExpanded = dao.replaceEnvVarsCase(exportDir, delimeter, caseObj, this.ALL_JOB_ID,
                    this.ALL_SECTORS, this.ALL_REGIONS);
            getExportService().exportForClient(user, new EmfDataset[] { ds }, new Version[] { version },
                    exportDirExpanded, purpose, false);

        }

    }

    public synchronized void exportCaseInputsWithOverwrite(User user, Integer[] caseInputIds, String purpose)
            throws EmfException {
        for (Integer caseInputId : caseInputIds) {
            CaseInput caseInput = this.getCaseInput(caseInputId.intValue());
            Case caseObj = this.getCase(caseInput.getCaseID());

            EmfDataset ds = caseInput.getDataset();
            Version version = caseInput.getVersion();
            SubDir subdirObj = caseInput.getSubdirObj();
            String subDir = "";

            if (subdirObj != null) {
                if (subdirObj.getName() != null) {
                    subDir = subdirObj.getName();
                }
            }

            String delimeter = System.getProperty("file.separator");
            String exportDir = caseObj.getInputFileDir() + delimeter + subDir;
            // export Dir is case general, therefore don't pass job, sector, or region
            String exportDirExpanded = dao.replaceEnvVarsCase(exportDir, delimeter, caseObj, this.ALL_JOB_ID,
                    this.ALL_SECTORS, this.ALL_REGIONS);

            getExportService().exportForClient(user, new EmfDataset[] { ds }, new Version[] { version },
                    exportDirExpanded, purpose, true);
        }

    }

    public synchronized void export(User user, String dirName, String purpose, boolean overWrite, int caseId)
            throws EmfException {
        if (DebugLevels.DEBUG_0())
            System.out.println("ManagedCaseService::export for caseId: " + caseId);

        EmfDataset[] datasets = getInputDatasets(caseId);
        Version[] versions = getInputDatasetVersions(caseId);
        SubDir[] subdirs = getSubdirs(caseId);

        if (datasets.length == 0)
            return;

        File inputsDir = new File(dirName);

        if (!inputsDir.exists()) {
            inputsDir.mkdirs();
            inputsDir.setWritable(true, false);
        }

        for (int i = 0; i < datasets.length; i++) {
            String subdir = (subdirs[i] == null) ? "" : subdirs[i].getName();

            String exportDir = dirName + System.getProperty("file.separator") + subdir;
            File dir = new File(exportDir);

            if (!dir.exists()) {
                dir.mkdirs();
                setDirsWritable(new File(dirName), dir);
            }

            getExportService().exportForClient(user, new EmfDataset[] { datasets[i] }, new Version[] { versions[i] },
                    exportDir, purpose, overWrite);

        }
    }

    private Map<String, List<CaseParameter>> getParameterHierarchy(CaseJob job, Session session) throws EmfException {
        Map<String, List<CaseParameter>> map = new HashMap<String, List<CaseParameter>>();

        /**
         * Gets all the inputs for a specific job
         */
        int caseId = job.getCaseId();
        int jobId = job.getId();

        /*
         * Need to get the inputs for 8 different scenarios: All combinations of Region, Sector, and CaseJob.
         */
        List<CaseParameter> paramsAAA = null; // parameters for all regions, all sectors and all jobs
        List<CaseParameter> paramsASA = null; // parameters for all regions, specific sector and all jobs
        List<CaseParameter> paramsAAJ = null; // parameters for all regions, all sectors specific jobs
        List<CaseParameter> paramsASJ = null; // parameters for all regions, specific sectors specific jobs
        List<CaseParameter> paramsRAA = null; // parameters for specific region, all sectors and all jobs
        List<CaseParameter> paramsRSA = null; // parameters for specific region, specific sector and all jobs
        List<CaseParameter> paramsRAJ = null; // parameters for specific region, all sectors specific jobs
        List<CaseParameter> paramsRSJ = null; // parameters for specific region, specific sectors specific jobs

        try {
            Sector sector = job.getSector();
            GeoRegion region = job.getRegion();

            // Get case inputs (the datasets associated w/ the case)
            // All regions, all sectors, all jobs
            paramsAAA = this.getJobParameters(caseId, this.ALL_JOB_ID, this.ALL_SECTORS, this.ALL_REGIONS, session);

            // All regions, sector specific, all jobs
            if (sector != this.ALL_SECTORS) {
                paramsASA = this.getJobParameters(caseId, this.ALL_JOB_ID, sector, this.ALL_REGIONS, session);
            }

            // All regions, all sectors, job specific
            paramsAAJ = this.getJobParameters(caseId, jobId, this.ALL_SECTORS, this.ALL_REGIONS, session);

            // All regions, specific sector and specific job
            if (sector != this.ALL_SECTORS) {
                paramsASJ = this.getJobParameters(caseId, jobId, sector, this.ALL_REGIONS, session);
            }

            // Specific region, all sectors, all jobs
            if (region != this.ALL_REGIONS)
                paramsRAA = this.getJobParameters(caseId, this.ALL_JOB_ID, this.ALL_SECTORS, region, session);

            // Region specific, sector specific, all jobs
            if (sector != this.ALL_SECTORS && region != this.ALL_REGIONS) {
                paramsRSA = this.getJobParameters(caseId, this.ALL_JOB_ID, sector, region, session);
            }

            // Region specific, all sectors, job specific
            if (region != this.ALL_REGIONS)
                paramsRAJ = this.getJobParameters(caseId, jobId, this.ALL_SECTORS, region, session);

            // Region specific, specific sector and specific job
            if (sector != this.ALL_SECTORS && region != this.ALL_REGIONS) {
                paramsRSJ = this.getJobParameters(caseId, jobId, sector, region, session);
            }
        } catch (Exception e) {
            log.error("Could not get all inputs for case (id=" + caseId + "), job (id=" + jobId + ").\n", e);
            // throw new EmfException("Could not get all inputs for case (id=" + caseId + "), job (id=" + jobId +
            // ").\n");
            throw new EmfException(e.getMessage());
        }

        map.put(AAA, paramsAAA);
        map.put(ASA, paramsASA);
        map.put(ASJ, paramsASJ);
        map.put(AAJ, paramsAAJ);
        map.put(RAA, paramsRAA);
        map.put(RSA, paramsRSA);
        map.put(RAJ, paramsRAJ);
        map.put(RSJ, paramsRSJ);

        return map;
    }

    private List<CaseParameter> getJobParameters(int caseId, int jobId, Sector sector, GeoRegion region, Session session)
            throws EmfException {
        /**
         * Gets all the parameters for this job, selects based on: case ID, job ID, and sector
         */
        // select the inputs based on 3 criteria
        try {
            List<CaseParameter> parameters = dao.getJobParameters(caseId, jobId, sector, region, session);
            // return an array of all type CaseParameter
            Collections.sort(parameters);
            return parameters;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all parameters for case (id=" + caseId + "), job (id=" + jobId + ").\n"
                    + e.getMessage());
            throw new EmfException("Could not get all parameters for case (id=" + caseId + "), job (id=" + jobId
                    + ").\n");
        }
    }

    private synchronized List<CaseInput> excludeInputsEnv(List<CaseInput> inputs, String envname) {
        /**
         * Excludes input elements from the inputs list based on environmental variable name.
         * 
         * NOTE: the # of elements of inputs is modified in the calling routine also, if an input has no environmental
         * variable, it will be treated as having the name ""
         */
        List<CaseInput> exclInputs = new ArrayList<CaseInput>();
        String inputEnvName = "";

        if (inputs == null || inputs.size() == 0)
            return exclInputs;

        // loop of inputs (using an iterator) and test for this env name
        Iterator<CaseInput> iter = inputs.iterator();
        while (iter.hasNext()) {
            CaseInput input = iter.next();

            inputEnvName = (input.getEnvtVars() == null) ? "" : input.getEnvtVars().getName();
            // input has an environmental variable w/ this name
            if (inputEnvName.equals(envname)) {
                // add the input to exclude
                exclInputs.add(input);
            }
        }

        // Now remove the excluded elements from inputs
        Iterator<CaseInput> iter2 = exclInputs.iterator();
        while (iter2.hasNext()) {
            CaseInput exclInput = iter2.next();
            // remove this element from the input list
            inputs.remove(exclInput);
        }

        // return the exclude list
        return exclInputs;
    }

    private String setenvInput(CaseInput input, Case caseObj, CaseJob job, ManagedExportService expSvc)
            throws EmfException {
        /**
         * Creates a line of the run job file. Sets the env variable to the value input file.
         * 
         * For eg. If the env variable is GRIDDESC, and the shell type is csh, this will return "setenv GRIDDESC
         * /home/azubrow/smoke/ge_dat/griddesc_12Apr2007_vo.txt"
         * 
         * Will replace any environmental variables in the input director with their value, i.e. will expand the path.
         * 
         * Note: this could be bash or tcsh format, could easily modify for other languages, for example python, perl,
         * etc.
         */

        EmfDataset dataset = input.getDataset();
        InputEnvtVar envvar = input.getEnvtVars();
        SubDir subdir = input.getSubdirObj();
        String fullPath = null;
        String setenvLine = null;
        String delimeter = System.getProperty("file.separator");

        // check if dataset is null, if so exception
        if (dataset == null) {
            throw new EmfException("Input (" + input.getName() + ") must have a dataset");
        }

        // check for external dataset
        if (dataset.isExternal()) {
            Session session = this.sessionFactory.getSession();
            ExternalSource[] externalDatasets = null;
            DatasetDAO dsdao = new DatasetDAO();

            try {
                externalDatasets = dsdao.getExternalSrcs(dataset.getId(), -1, null, session);
            } catch (Exception e) {
                log.error("Could not get external sources for dataset " + dataset.getName(), e);
                throw new EmfException("Could not get external sources for dataset " + dataset.getName() + ".");
            } finally {
                session.close();
            }

            // set the full path to the first external file in the dataset
            if (externalDatasets == null || externalDatasets.length == 0) {
                throw new EmfException("Input (" + input.getName() + ") must have at least 1 external dataset");
            }
            fullPath = externalDatasets[0].getDatasource();
        } else {
            // internal dataset
            // Create a full path to the input file
            fullPath = expSvc.getCleanDatasetName(input.getDataset(), input.getVersion());
            if ((subdir != null) && !(subdir.toString()).equals("")) {
                fullPath = caseObj.getInputFileDir() + delimeter + input.getSubdirObj() + delimeter + fullPath;
            } else {
                fullPath = caseObj.getInputFileDir() + delimeter + fullPath;
            }
        }
        if (envvar == null) {
            // if no environmental variable, just created a commented
            // line w/ input name = fullPath
            setenvLine = this.runComment + " " + input.getName() + " = " + fullPath + eolString;
        } else {
            setenvLine = shellSetenv(envvar.getName(), fullPath);
        }

        // Expand input director, ie. remove env variables
        try {
            // input Dir is case general, therefore don't pass job, sector, or region
            setenvLine = dao.replaceEnvVarsCase(setenvLine, delimeter, caseObj, this.ALL_JOB_ID, this.ALL_SECTORS,
                    this.ALL_REGIONS);

            return setenvLine;
        } catch (Exception e) {
            throw new EmfException("Input folder: " + e.getMessage());
        }
    }

    private String shellSetenv(String envvariable, String envvalue) {
        /**
         * Simply creates a setenv line from an environmental variable and a value.
         * 
         * For eg. If the env variable is IOAPI_GRIDNAME, and the shell type is csh, this will return "setenv GRIDDESC
         * US36_148x112"
         * 
         * Note: this could be bash or tcsh format, could easily modify for other languages, for example python, perl,
         * etc.
         */
        String setenvLine;

        // add quotes to value, if they are not already there
        if (envvalue.indexOf('"') >= 0)
            setenvLine = this.runSet + " " + envvariable + this.runEq + envvalue + this.runTerminator;
        else
            setenvLine = this.runSet + " " + envvariable + this.runEq + addQuotes(envvalue) + this.runTerminator;

        return setenvLine + eolString;

    }

    /**
     * Creates a line of the run job file. Sets the env variable to the value of the parameter.
     * 
     * For eg. If the env variable is IOAPI_GRIDNAME, and the shell type is csh, this will return
     * 
     * "setenv GRIDDESC US36_148x112"
     * 
     * Note: this could be bash or tcsh format, could easily modify for other languages, for example python, perl, etc.
     */
    private String setenvParameter(CaseParameter parameter) throws EmfException {

        if (false)
            throw new EmfException();
        String setenvLine = null;
        if (parameter.getEnvVar() == null) {
            // no environmental variable, therefore create commented line
            // parameter name = value
            setenvLine = this.runComment + " " + parameter.getName() + " = " + parameter.getValue() + eolString;
        } else {
            setenvLine = shellSetenv(parameter.getEnvVar().getName(), parameter.getValue());

        }
        return setenvLine;
    }

    public synchronized String createJobFileContent(CaseJob job, User user, String jobFileName,
            String jobLogFile, ManagedExportService expSvc, Session session) throws EmfException {
        /**
         * Creates the content string for a job run file w/ all necessary inputs and parameters set.
         * 
         * Input: job - the Case Job user - the user
         * 
         * Output: String - the job content
         */

        // String jobContent="";
        String jobFileHeader = "";

        StringBuffer sbuf = new StringBuffer();

        // Some objects needed for accessing data
        Case caseObj = this.getCase(job.getCaseId(), session);
        int jobId = job.getId();

        CaseInput headerInput = null; // input for the EMF Job header

        // Make sure no spaces or strange characters in the job name
        String jobName = job.getName().replace(" ", "_");
        jobName = replaceNonDigitNonLetterChars(jobName);
        /*
         * Get the inputs in the based on hierarchy between region, sector, and job.
         * 
         * Present hierarchy from general to specific AAA - all regions, all sectors, all jobs RAA - specific region,
         * all sectors, all jobs ASA - all regions, specific sector, all jobs RSA - specific region, specific sector,
         * all jobs AAJ - all regions, all sectors, specific job RAJ - specific region, all sectors, specific job ASJ -
         * all regions, specific sector, specific job RSJ - specific region, specific sector, specific job
         */

        // inputs based on all (A), specific region (R), specific sector (S), and/or specific job (J)
        Map<String, List<CaseInput>> map = getInputHierarchy(job, session);
        List<CaseInput> inputsAAA = map.get(AAA);
        List<CaseInput> inputsASA = map.get(ASA);
        List<CaseInput> inputsAAJ = map.get(AAJ);
        List<CaseInput> inputsASJ = map.get(ASJ);
        List<CaseInput> inputsRAA = map.get(RAA);
        List<CaseInput> inputsRSA = map.get(RSA);
        List<CaseInput> inputsRAJ = map.get(RAJ);
        List<CaseInput> inputsRSJ = map.get(RSJ);

        // parameters based on all (A), specific region (R), specific sector (S), and/or specific job (J)
        Map<String, List<CaseParameter>> paramsmap = getParameterHierarchy(job, session);
        List<CaseParameter> paramsAAA = paramsmap.get(AAA);
        List<CaseParameter> paramsASA = paramsmap.get(ASA);
        List<CaseParameter> paramsAAJ = paramsmap.get(AAJ);
        List<CaseParameter> paramsASJ = paramsmap.get(ASJ);
        List<CaseParameter> paramsRAA = paramsmap.get(RAA);
        List<CaseParameter> paramsRSA = paramsmap.get(RSA);
        List<CaseParameter> paramsRAJ = paramsmap.get(RAJ);
        List<CaseParameter> paramsRSJ = paramsmap.get(RSJ);

        // Create an export service to get names of the datasets as inputs to Smoke script
        // ExportService exports = new ExportService(dbServerlocal, this.threadPool, this.sessionFactory);
        // Get case inputs (the datasets associated w/ the case)

        // Exclude any inputs w/ environmental variable EMF_JOBHEADER and see if the EMF_JOBHEADER exists at each level
        // / AAA
        List<CaseInput> exclInputs = this.excludeInputsEnv(inputsAAA, "EMF_JOBHEADER");

        if (exclInputs.size() != 0) {
            headerInput = exclInputs.get(0); // get the first element as header
        }

        // / RAA
        exclInputs = this.excludeInputsEnv(inputsRAA, "EMF_JOBHEADER");

        if (exclInputs.size() != 0) {
            headerInput = exclInputs.get(0); // get the first element as header
        }

        // / ASA
        exclInputs = this.excludeInputsEnv(inputsASA, "EMF_JOBHEADER");

        if (exclInputs.size() != 0) {
            headerInput = exclInputs.get(0); // get the first element as header
        }

        // / RSA
        exclInputs = this.excludeInputsEnv(inputsRSA, "EMF_JOBHEADER");

        if (exclInputs.size() != 0) {
            headerInput = exclInputs.get(0); // get the first element as header
        }

        // / AAJ
        exclInputs = this.excludeInputsEnv(inputsAAJ, "EMF_JOBHEADER");

        if (exclInputs.size() != 0) {
            headerInput = exclInputs.get(0); // get the first element as header
        }

        // / RAJ
        exclInputs = this.excludeInputsEnv(inputsRAJ, "EMF_JOBHEADER");

        if (exclInputs.size() != 0) {
            headerInput = exclInputs.get(0); // get the first element as header
        }

        // / ASJ
        exclInputs = this.excludeInputsEnv(inputsASJ, "EMF_JOBHEADER");

        if (exclInputs.size() != 0) {
            headerInput = exclInputs.get(0); // get the first element as header
        }

        // / RSJ
        exclInputs = this.excludeInputsEnv(inputsRSJ, "EMF_JOBHEADER");

        if (exclInputs.size() != 0) {
            headerInput = exclInputs.get(0); // get the first element as header
        }

        // Get the sector and region
        Sector sector = job.getSector();
        GeoRegion region = job.getRegion();

        // Get header String:
        if (headerInput != null) {
            // Header string starts the job file content string

            try {
                // get the string of the EMF_JOBHEADER from the input
                jobFileHeader = getJobFileHeader(headerInput);
                sbuf.append(jobFileHeader);
            } catch (Exception e) {
                log.error("Could not write EMF header to job script file. ", e);
                throw new EmfException("Could not write EMF header to job script file");
            }
        } else {
            // Start the job file content string and append the end of line characters for this OS
            sbuf.append(this.runShell + this.eolString);
        }

        // print job name to file
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Job run file for job: " + jobName + eolString);

        /*
         * Define some EMF specific variables
         */
        sbuf.append(eolString);

        sbuf.append(this.runComment + " EMF specific variables" + eolString);
        sbuf.append(shellSetenv("EMF_JOBID", String.valueOf(jobId)));
        sbuf.append(shellSetenv("EMF_JOBNAME", jobName));
        sbuf.append(shellSetenv("EMF_USER", user.getUsername()));

        // name of this job script and script directory

        // Expand output director, ie. remove env variables
        String delimeter = System.getProperty("file.separator");
        String outputFileDir = caseObj.getOutputFileDir();
        if (outputFileDir == null || (outputFileDir.length() == 0))
            throw new EmfException("The Output Job Scripts folder has not been specified");
        try {
            // output Dir is case general, therefore don't pass job, sector, or region
            String outputDirExpanded = dao.replaceEnvVarsCase(outputFileDir, delimeter, caseObj, this.ALL_JOB_ID,
                    this.ALL_SECTORS, this.ALL_REGIONS);
            sbuf.append(shellSetenv("EMF_SCRIPTDIR", outputDirExpanded));
        } catch (Exception e) {
            throw new EmfException("Output folder: " + e.getMessage());
        }
        sbuf.append(shellSetenv("EMF_SCRIPTNAME", jobFileName));
        sbuf.append(shellSetenv("EMF_LOGNAME", jobLogFile));

        // Generate and get a unique job key, add it to the job,
        // update the db, and write it to the script
        String jobKey = createJobKey(jobId);
        job.setJobkey(jobKey);
        updateCaseJob(user, job);
        sbuf.append(shellSetenv("EMF_JOBKEY", job.getJobkey()));

        // Print the inputs to the file

        /*
         * loop over inputs and write Env variables and input (full name and path) to job run file, print comments order
         * of inputs based on above hierarchy
         */

        // AAA
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Inputs -- for all regions, all sectors and all jobs" + eolString);
        if (inputsAAA != null) {
            for (CaseInput input : inputsAAA) {
                sbuf.append(setenvInput(input, caseObj, job, expSvc));
            }
        }

        // RAA
        sbuf.append(eolString);
        if (inputsRAA != null) {
            sbuf
                    .append(this.runComment + " Inputs -- region (" + region + ") and all sectors and all jobs"
                            + eolString);
            for (CaseInput input : inputsRAA) {
                sbuf.append(setenvInput(input, caseObj, job, expSvc));
            }
        }

        // ASA
        sbuf.append(eolString);
        if (inputsASA != null) {
            sbuf.append(this.runComment + " Inputs -- all regions and  sector (" + sector + ") and all jobs"
                    + eolString);
            for (CaseInput input : inputsASA) {
                sbuf.append(setenvInput(input, caseObj, job, expSvc));
            }
        }

        // RSA
        sbuf.append(eolString);
        if (inputsRSA != null) {
            sbuf.append(this.runComment + " Inputs -- region (" + region + ") and  sector (" + sector
                    + ") and all jobs" + eolString);
            for (CaseInput input : inputsRSA) {
                sbuf.append(setenvInput(input, caseObj, job, expSvc));
            }
        }

        // AAJ
        sbuf.append(eolString);
        if (inputsAAJ != null) {
            sbuf.append(this.runComment + " Inputs -- all regions and  all sector and job (" + job + ")" + eolString);
            for (CaseInput input : inputsAAJ) {
                sbuf.append(setenvInput(input, caseObj, job, expSvc));
            }
        }

        // RAJ
        sbuf.append(eolString);
        if (inputsRAJ != null) {
            sbuf.append(this.runComment + " Inputs -- region (" + region + ") and  all sectors and job (" + job + ")"
                    + eolString);
            for (CaseInput input : inputsRAJ) {
                sbuf.append(setenvInput(input, caseObj, job, expSvc));
            }
        }

        // ASJ
        sbuf.append(eolString);
        if (inputsASJ != null) {
            sbuf.append(this.runComment + " Inputs -- all regions and  sector (" + sector + ") and job (" + job.getName() + ")"
                    + eolString);
            for (CaseInput input : inputsASJ) {
                sbuf.append(setenvInput(input, caseObj, job, expSvc));
            }
        }

        // RSJ
        sbuf.append(eolString);
        if (inputsRSJ != null) {
            sbuf.append(this.runComment + " Inputs -- region (" + region + ") and  sector (" + sector + ") and job ("
                    + job.getName() + ")" + eolString);
            for (CaseInput input : inputsRSJ) {
                sbuf.append(setenvInput(input, caseObj, job, expSvc));
            }
        }

        /*
         * Get the parameters for this job in following order: from summary tab, from job tab, then in the same order as
         * the inputs, ie.: Present hierarchy from general to specific AAA - all regions, all sectors, all jobs RAA -
         * specific region, all sectors, all jobs ASA - all regions, specific sector, all jobs RSA - specific region,
         * specific sector, all jobs AAJ - all regions, all sectors, specific job RAJ - specific region, all sectors,
         * specific job ASJ - all regions, specific sector, specific job RSJ - specific region, specific sector,
         * specific job
         */

        // Parameters from the summary tab
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Parameters -- from Case summary " + eolString);
        if (caseObj.getAbbreviation() != null) {
            sbuf.append(shellSetenv("CASE", caseObj.getAbbreviation().getName()));
        }
        // Need to have quotes around model name b/c could be more than one word, include version if available
        if (caseObj.getModel() != null) {
            String modelName = caseObj.getModel().getName();
            if (caseObj.getModelVersion() != null) {
                modelName += caseObj.getModelVersion();
            }
            sbuf.append(shellSetenv("MODEL_LABEL", modelName));
        }
        if (caseObj.getAirQualityModel() != null) {
            sbuf.append(shellSetenv("EMF_AQM", caseObj.getAirQualityModel().getName()));
        }
        if (caseObj.getSpeciation() != null) {
            sbuf.append(shellSetenv("EMF_SPC", caseObj.getSpeciation().getName()));
        }
        if (caseObj.getEmissionsYear() != null) {
            sbuf.append(shellSetenv("BASE_YEAR", caseObj.getEmissionsYear().getName())); // Should base year ==
            // emissions year ????
        }
        // sbuf.append(shellSetenv("BASE_YEAR", String.valueOf(caseObj.getBaseYear())));
        if (caseObj.getFutureYear() != 0) { // CHECK: should it be included if == 0 ???
            sbuf.append(shellSetenv("FUTURE_YEAR", String.valueOf(caseObj.getFutureYear())));
        }
        // Need to have quotes around start and end date b/c could be more than one word 'DD/MM/YYYY HH:MM'
        if (caseObj.getStartDate() != null) {
            String startString = caseObj.getStartDate().toString();
            sbuf.append(shellSetenv("EPI_STDATE_TIME", startString));
        }
        if (caseObj.getEndDate() != null) {
            String endString = caseObj.getEndDate().toString();
            sbuf.append(shellSetenv("EPI_ENDATE_TIME", endString));
        }
        // Parent case
        String parentName = caseObj.getTemplateUsed();
        if (parentName != null && parentName != "") {
            try {
                Case parentCase = dao.getCaseFromName(parentName, session);
                sbuf.append(shellSetenv("PARENT_CASE", parentCase.getAbbreviation().getName()));
            } catch (Exception e) {
                log.error("Parent case (" + parentName + ") does not exist. Will not set PARENT_CASE parameter");
                log.error(e.getMessage());
            }
        }

        // Add Parameters from job tab
        sbuf.append(eolString);
        sbuf.append(this.runComment + " Parameters -- from job tab" + eolString);
        if (job.getSector() != null) {
            sbuf.append(shellSetenv("SECTOR", job.getSector().getName()));
        }
        if (job.getJobGroup() != null && !job.getJobGroup().isEmpty()) {
            sbuf.append(shellSetenv("JOB_GROUP", job.getJobGroup()));
        }
        if (region != null) {
            sbuf.append(shellSetenv("REGION", region.getName()));
            if (region.getAbbreviation() != null && !region.getAbbreviation().isEmpty()) {
                sbuf.append(shellSetenv("REGION_ABBREV", region.getAbbreviation()));
            }
            if (region.getIoapiName() != null && !region.getIoapiName().isEmpty()) {
                sbuf.append(shellSetenv("REGION_IOAPI_GRIDNAME", region.getIoapiName()));
            }
        }

        // AAA
        sbuf.append(eolString);
        if (paramsAAA != null) {
            sbuf.append(this.runComment + " Parameters -- all regions, all sectors, all jobs " + eolString);
            for (CaseParameter param : paramsAAA) {
                sbuf.append(setenvParameter(param));
            }
        }

        // RAA
        sbuf.append(eolString);
        if (paramsRAA != null) {
            sbuf.append(this.runComment + " Parameters -- region (" + region + "), all sectors, all jobs " + eolString);
            for (CaseParameter param : paramsRAA) {
                sbuf.append(setenvParameter(param));
            }
        }

        // ASA
        sbuf.append(eolString);
        if (paramsASA != null) {
            sbuf.append(this.runComment + " Parameters -- all regions, sector (" + sector + "), all jobs " + eolString);
            for (CaseParameter param : paramsASA) {
                sbuf.append(setenvParameter(param));
            }
        }

        // RSA
        sbuf.append(eolString);
        if (paramsRSA != null) {
            sbuf.append(this.runComment + " Parameters -- region (" + region + "), sector (" + sector + "), all jobs "
                    + eolString);
            for (CaseParameter param : paramsRSA) {
                sbuf.append(setenvParameter(param));
            }
        }

        // AAJ
        sbuf.append(eolString);
        if (paramsAAJ != null) {
            sbuf.append(this.runComment + " Parameters -- all regions, all sectors, job (" + job.getName() + ")" + eolString);
            for (CaseParameter param : paramsAAJ) {
                sbuf.append(setenvParameter(param));
            }
        }

        // RAJ
        sbuf.append(eolString);
        if (paramsRAJ != null) {
            sbuf.append(this.runComment + " Parameters -- region (" + region + "), all sectors, job (" + job.getName() + ")"
                    + eolString);
            for (CaseParameter param : paramsRAJ) {
                sbuf.append(setenvParameter(param));
            }
        }

        // ASJ
        sbuf.append(eolString);
        if (paramsASJ != null) {
            sbuf.append(this.runComment + " Parameters -- all regions, sector (" + sector + "), all job (" + job.getName() + ")"
                    + eolString);
            for (CaseParameter param : paramsASJ) {
                sbuf.append(setenvParameter(param));
            }
        }

        // RSJ
        sbuf.append(eolString);
        if (paramsRSJ != null) {
            sbuf.append(this.runComment + " Parameters -- region (" + region + "), sector (" + sector + "), job ("
                    + job.getName() + ")" + eolString);
            for (CaseParameter param : paramsRSJ) {
                sbuf.append(setenvParameter(param));
            }
        }

        // Getting the executable object from the job
        Executable execVal = job.getExecutable();

        // path to executable
        String execPath = job.getPath();

        // executable string
        String execName = execVal.getName();

        // executable full name and arguments
        String execFull = execPath + System.getProperty("file.separator") + execName;
        String execFullArgs = execFull + " " + job.getArgs() + eolString;
        // print executable
        sbuf.append(eolString);
        sbuf.append(this.runComment + " job executable" + eolString);
        sbuf.append("$EMF_CLIENT -k $EMF_JOBKEY -x " + execFull + " -m \"Running top level script for job: " + jobName
                + "\"" + eolString);
        sbuf.append(execFullArgs);

        // add a test of the status and send info through the
        // command client -- should generalize so not csh specific
        sbuf.append("if ( $status != 0 ) then" + eolString);
        sbuf.append("\t $EMF_CLIENT -k $EMF_JOBKEY -s 'Failed' -m \"ERROR running Job: $EMF_JOBNAME\" -t 'e' "
                + eolString);
        sbuf.append("\t exit(1)" + eolString);
        sbuf.append("else" + eolString);
        sbuf.append("\t $EMF_CLIENT -k $EMF_JOBKEY -s 'Completed' -m \"Completed job: $EMF_JOBNAME\"" + eolString);
        sbuf.append("endif" + eolString);

        // Send back the contents of jobContent string for job file
        return sbuf.toString();

    }// /end of createJobFileContent()

    private String addQuotes(String evName) {
        return '"' + evName + '"';
    }

    private String getJobFileHeader(CaseInput headerInput) throws EmfException {
        /**
         * gets the EMF JOBHEADER as a string
         */

        // get some info from the header input
        EmfDataset dataset = headerInput.getDataset();
        Version version = headerInput.getVersion();

        // create an exporter to get the string
        DbServer dbServer = this.dbFactory.getDbServer();
        GenericExporterToString exporter = new GenericExporterToString(dataset, "", dbServer,
                new VersionedDataFormatFactory(version, dataset), null);

        // Get the string from the exporter
        try {
            exporter.export(null);
            String fileHeader = exporter.getOutputString();
            return fileHeader;

        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            try {
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                throw new EmfException("ManagedCaseService: error closing db server. " + e.getMessage());
            }
        }

    }

    private String getJobFileName(CaseJob job, Session session) throws EmfException {
        /*
         * Creates the File name that corresponds to the job script file name.
         * 
         * Format: <jobname>_<case_abbrev>_<datestamp>.csh
         */
        String dateStamp = CustomDateFormat.format_YYYYMMDDHHMMSS(new Date());
        String jobName = job.getName().replace(" ", "_");
        Case caseObj = this.getCase(job.getCaseId(), session);
        
        //Get sector name and region abbreviation
        String secName = job.getSector() == null ? "" : job.getSector().getName();
        String regionAbbr = job.getRegion() == null ? "" : job.getRegion().getAbbreviation();
        
        if (!secName.isEmpty() && jobName.toLowerCase().contains(secName.toLowerCase()))
            secName = "";
        
        if (!regionAbbr.isEmpty() && jobName.toLowerCase().contains(regionAbbr.toLowerCase()))
            regionAbbr = "";
        
        if (!secName.isEmpty())
            secName = "_" + secName;
        
        if (!regionAbbr.isEmpty())
            regionAbbr = "_" + regionAbbr;
        
        // Get case abbreviation, if no case abbreviation construct one from id
        String defaultAbbrev = "case" + job.getCaseId();
        String caseAbbrev = (caseObj.getAbbreviation() == null) ? defaultAbbrev : caseObj.getAbbreviation().getName();

        // Expand output director, ie. remove env variables
        String delimeter = System.getProperty("file.separator");
        String outputFileDir = caseObj.getOutputFileDir();
        try {
            // output file Dir is case general, therefore don't pass job, sector, or region
            String outputDirExpanded = dao.replaceEnvVarsCase(outputFileDir, delimeter, caseObj, this.ALL_JOB_ID,
                    this.ALL_SECTORS, this.ALL_REGIONS);

            // Test output directory to place job script
            if ((outputDirExpanded == null) || (outputDirExpanded.equals(""))) {
                throw new EmfException(
                        "The output job scripts directory must be set on the Jobs tab prior to running any jobs");
            }

            String fileName = replaceNonDigitNonLetterChars(jobName + secName + regionAbbr + "_" + caseAbbrev + "_" + dateStamp
                    + this.runSuffix);
            fileName = outputDirExpanded + delimeter + fileName;
            return fileName;
        } catch (Exception e) {
            throw new EmfException("Output folder: " + e.getMessage());
        }
    }

    private String getLog(String jobFileName) throws EmfException {
        /*
         * From the job script name, it creates the name of corresponding log file. It also checks that an appropriate
         * log directory exists.
         */

        File file = new File(jobFileName);
        String fileShort = file.getName(); // file w/o path
        // Create a log file name by replacing the suffix of the job
        // of the job file w/ .log
        String logFileName = fileShort.replaceFirst(this.runSuffix, ".log");

        // Check if logs dir (jobpath/logs) exists, if not create
        File logDir = new File(file.getParent() + System.getProperty("file.separator") + "logs");

        //first find root parent directory that don't exist
        List<File> missingDirectoryList = new ArrayList<File>();
        File missingParentDirectory = logDir.getParentFile();
        while (!missingParentDirectory.exists()) {
            missingDirectoryList.add(missingParentDirectory);
            missingParentDirectory = missingParentDirectory.getParentFile();
        }
        
        //now create the log directory and make it readable, writable, and executable by everyone
        if (!(logDir.isDirectory())) {
            // Need to create the directory
            if (!(logDir.mkdirs())) {
                throw new EmfException("Error creating job log directory: " + logDir);
            }

            // Make directory writable by everyone
            if (!logDir.setWritable(true, false)) {
                throw new EmfException("Error changing job log directory's write permissions: " + logDir);
            }

            // Make directory writable by everyone
            if (!logDir.setExecutable(true, false)) {
                throw new EmfException("Error changing job log directory's executable permissions: " + logDir);
            }

            // Make directory writable by everyone
            if (!logDir.setReadable(true, false)) {
                throw new EmfException("Error changing job log directory's readable permissions: " + logDir);
            }
        }

        //next give each parent directory the appropriate permission
        for (File missingParentDir : missingDirectoryList) {
            missingParentDir.setWritable(true, false);
            missingParentDir.setExecutable(true, false);
            missingParentDir.setReadable(true, false);
        }

        // Create the logFile full name
        logFileName = logDir + System.getProperty("file.separator") + logFileName;

        return logFileName;

    }

    // for command line client
    public synchronized int recordJobMessage(JobMessage message, String jobKey) throws EmfException {
        try {
            CaseJob job = getJobFromKey(jobKey);

            User user = job.getUser();
            message.setCaseId(job.getCaseId());
            message.setJobId(job.getId());
            String status = message.getStatus();
            String jobStatus = job.getRunstatus().getName();
            String lastMsg = message.getMessage();

            if (lastMsg != null && !lastMsg.trim().isEmpty())
                job.setRunLog(lastMsg);

            if (!status.isEmpty() && !jobStatus.equalsIgnoreCase(status)) {
                job.setRunstatus(dao.getJobRunStatuse(status));

                if (!(status.equalsIgnoreCase("Running"))) {
                    // status is Completed or Failed - set completion date
                    job.setRunCompletionDate(new Date());
                } else {
                    // status is running - set running date
                    job.setRunStartDate(new Date());
                }

            }

            dao.updateCaseJob(job);

            if (!user.getUsername().equalsIgnoreCase(message.getRemoteUser()))
                throw new EmfException(
                        "Error recording job messages: Remote user doesn't match the user who runs the job.");

            dao.add(message);

            if (!status.isEmpty() && !jobStatus.equalsIgnoreCase(status)) {
                if (!(status.equalsIgnoreCase("Running"))) {
                    // Notify CaseJobTaskManager that the job status has changed to Completed or Failed
                    TaskManagerFactory.getCaseJobTaskManager(sessionFactory).callBackFromJobRunServer();
                }

            }

            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new EmfException("Error recording job messages: " + e.getMessage());
        }
    }

    private CaseJob getJobFromKey(String jobKey) throws EmfException {
        CaseJob job = dao.getCaseJob(jobKey);

        if (job == null)
            throw new EmfException("Error recording job messages: No jobs found associated with job key: " + jobKey);
        return job;
    }

    public synchronized JobMessage[] getJobMessages(int caseId, int jobId) throws EmfException {
        Session session = sessionFactory.getSession();
        List<JobMessage> msgs = null;

        try {
            if (jobId == 0)
                msgs = dao.getJobMessages(caseId, session);
            else
                msgs = dao.getJobMessages(caseId, jobId, session);

            return msgs.toArray(new JobMessage[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized CaseJob[] getAllValidJobs(int jobId, int caseId) throws EmfException {
        try {
            return dao.getAllValidJobs(jobId, caseId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all valid jobs for job (id=" + jobId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all valid jobs for job (id=" + jobId + ").\n");
        }
    }

    public synchronized CaseJob[] getDependentJobs(int jobId) throws EmfException {
        try {
            return dao.getDependentJobs(jobId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all dependent jobs for job (id=" + jobId + ").\n" + e.getMessage());
            throw new EmfException("Could not get all dependent jobs for job (id=" + jobId + ").\n");
        }
    }

    public synchronized int[] getJobIds(int caseId, String[] jobNames) throws EmfException {
        try {
            return dao.getJobIds(caseId, jobNames);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all job ids for job (" + jobNames[0] + ", etc.).\n" + e.getMessage());
            throw new EmfException("Could not get all job ids for job (" + jobNames[0] + ", etc.).\n");
        }
    }

    public synchronized void finalize() throws Throwable {
        // this.session = null;
        super.finalize();
    }

    public synchronized String restoreTaskManagers() throws EmfException {
        if (DebugLevels.DEBUG_9())
            System.out.println("ManagedCaseService::restoreTaskManagers");

        String mesg;
        List distinctUserIds = null;

        try {
            ArrayList userIds = (ArrayList) dao.getDistinctUsersOfPersistedWaitTasks();
            mesg = "Total number of persisted wait tasks retored: " + userIds.size();
            distinctUserIds = getDistinctUserIds(userIds);

            Iterator iter = distinctUserIds.iterator();
            while (iter.hasNext()) {

                // get the user id
                int uid = ((IntegerHolder) iter.next()).getUserId();

                // acquire all the CaseJobTasks for this uid
                // List allCJTsForUser = getCaseJobTasksForUser(uid);

                resubmitPersistedTasksForUser(uid);

            }

            return mesg;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("System problems: Database access failed");
        }

    }

    private synchronized void resubmitPersistedTasksForUser(int uid) throws EmfException {
        if (DebugLevels.DEBUG_9())
            System.out.println("Start ManagedCaseService::resubmitPersistedTasksForUser uid= " + uid);
        Integer[] jobIds = null;
        int caseId = -9;
        User user = getUser(uid);

        if (DebugLevels.DEBUG_9())
            System.out.println("Incoming userid= " + uid + " acquired userName= " + user.getName());

        // Get All persisted wait jobs for this user
        List<PersistedWaitTask> allPersistedTasks = getPersistedTasksForUser(uid);
        if (allPersistedTasks == null) {
            if (DebugLevels.DEBUG_9())
                System.out.println("allPersistedTasks is null WHY?");

        } else {
            if (DebugLevels.DEBUG_9())
                System.out.println("Size of list returned from Persist wait table= " + allPersistedTasks.size());
            jobIds = new Integer[allPersistedTasks.size()];

            for (int i = 0; i < allPersistedTasks.size(); i++) {
                PersistedWaitTask pwTask = allPersistedTasks.get(i);
                if (caseId == -9) {
                    caseId = pwTask.getCaseId();
                }
                jobIds[i] = new Integer(pwTask.getJobId());
            }

            // Task has been acquired so delete from persisted wait task table
            dao.removePersistedTasks(allPersistedTasks.toArray(new PersistedWaitTask[0]));

            if (DebugLevels.DEBUG_9())
                System.out.println("After the loop jobId array of ints size= " + jobIds.length);
            if (DebugLevels.DEBUG_9())
                System.out.println("After the loop CaseId= " + caseId);

        }

        if (allGood(user, jobIds, caseId)) {
            if (DebugLevels.DEBUG_9())
                System.out.println("ManagedCaseService::resubmitPersistedTasksForUser Everything is good so resubmit");
            this.submitJobs(jobIds, caseId, user);
        } else {
            throw new EmfException("Failed to restore persisted wait tasks for user= " + user.getName());
        }

        if (DebugLevels.DEBUG_9())
            System.out.println("End ManagedCaseService::resubmitPersistedTasksForUser uid= " + uid);
    }

    private boolean allGood(User user, Integer[] jobIds, int caseId) {
        boolean allGewd = false;

        if ((caseId != -9) && (user != null) && (jobIds != null) && (jobIds.length > 0)) {
            allGewd = true;
        }
        if (DebugLevels.DEBUG_9())
            System.out.println("END ManagedCaseService::allGood status= " + allGewd);

        return allGewd;
    }

    private User getUser(int uid) throws EmfException {
        Session session = this.sessionFactory.getSession();
        User user = null;
        try {
            UserDAO userDAO = new UserDAO();
            user = userDAO.get(uid, session);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("System Problems: Database access error");

        } finally {
            session.clear();
            session.close();
        }

        return user;
    }

    private List<PersistedWaitTask> getPersistedTasksForUser(int uid) throws EmfException {
        Session session = this.sessionFactory.getSession();
        List<PersistedWaitTask> allPersistTasks = null;

        try {
            allPersistTasks = dao.getPersistedWaitTasksByUser(uid);
            String statMsg;

            if (allPersistTasks != null) {
                statMsg = allPersistTasks.size() + " elements List returned of persisted wait tasks for user= " + uid;
            } else {
                statMsg = "Empty list of elements for user= " + uid;

            }

            if (DebugLevels.DEBUG_9())
                System.out.println(statMsg);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("System Problems: Database access error");

        } finally {
            session.clear();
            session.close();
        }

        return allPersistTasks;
    }

    private ArrayList getDistinctUserIds(List userIds) {
        ArrayList distUid = new ArrayList();

        Iterator iter = userIds.iterator();
        while (iter.hasNext()) {
            IntegerHolder intHold = (IntegerHolder) iter.next();
            intHold.setId(0);
            if (!distUid.contains(intHold)) {
                distUid.add(intHold);
            }
        }

        return distUid;
    }

    public synchronized String printStatusCaseJobTaskManager() throws EmfException {
        return TaskManagerFactory.getCaseJobTaskManager(sessionFactory).getStatusOfWaitAndRunTable();
    }

    public synchronized String validateJobs(Integer[] jobIDs) throws EmfException {
        if (DebugLevels.DEBUG_14())
            System.out.println("Start validating jobs on server side. " + new Date());

        String ls = System.getProperty("line.separator");
        Session session = sessionFactory.getSession();

        List<CaseInput> allInputs = new ArrayList<CaseInput>();

        try {
            for (Integer id : jobIDs) {
                CaseJob job = this.getCaseJob(id.intValue(), session);
                allInputs.addAll(this.getAllJobInputs(job, session));
            }
        } catch (RuntimeException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        } finally {
            session.close();
        }

        if (DebugLevels.DEBUG_14())
            System.out.println("Finished validating jobs on server side. " + new Date());

        TreeSet<CaseInput> set = new TreeSet<CaseInput>(allInputs);
        List<CaseInput> uniqueInputs = new ArrayList<CaseInput>(set);

        String finalVersionMsg = listNonFinalInputs(uniqueInputs);
        String laterVersionMsg = listInputsMsg(uniqueInputs);
        String returnMsg = "";

        if (finalVersionMsg == null || finalVersionMsg.isEmpty())
            returnMsg = laterVersionMsg;
        else
            returnMsg = border(100, "*") + ls + finalVersionMsg + ls + border(100, "*") + ls + laterVersionMsg
                    + border(100, "*");

        return returnMsg;
    }

    private String listNonFinalInputs(List<CaseInput> inputs) {
        String inputsList = "";
        String ls = System.getProperty("line.separator");

        if (DebugLevels.DEBUG_14())
            System.out.println("Start listing non-final inputs. " + new Date());

        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            String dataset = input.getDataset().getName();
            Version version = input.getVersion();

            if (!version.isFinalVersion())
                inputsList += "Input: " + input.getName() + ";  Dataset: " + dataset + ls;
        }

        if (DebugLevels.DEBUG_14())
            System.out.println("Finished listing non-final inputs. " + new Date());

        if (inputsList.isEmpty())
            return inputsList;

        return "The selected jobs have non-final dataset versions:" + ls + ls + inputsList;
    }

    private String listInputsMsg(List<CaseInput> inputs) throws EmfException {
        StringBuffer inputsList = new StringBuffer("");

        // NOTE: if change headers msg here, please also change the client side
        String header = "Inputs using datasets that have later versions available: ";
        String header2 = "No new versions exist for selected inputs.";
        boolean laterVersionExists = false;

        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            String inputName = input.getName();
            EmfDataset dataset = input.getDataset();

            if (dataset == null)
                inputsList.append("Input: " + inputName + " has no dataset\n");
            else {
                String datasetName = input.getDataset().getName();
                Version version = input.getVersion();

                if (version == null) {
                    inputsList.append("Input: " + inputName + " hasn't set dataset version\n");
                    continue;
                }

                Version[] laterVersions = getLaterVersions(dataset, version);

                if (laterVersions.length > 0)
                    inputsList.append("Input name: " + inputName + ";    " + "Dataset name: " + datasetName + "\n");

                for (Version ver : laterVersions) {
                    inputsList.append("    Version " + ver.getVersion() + ":   " + ver.getName() + ", "
                            + (dataset.getCreator() == null ? "" : dataset.getCreator()) + ", "
                            + (ver.isFinalVersion() ? "Final" : "Non-final") + "\n");
                    laterVersionExists = true;
                }
            }
        }

        return (laterVersionExists) ? header + "\n\n" + inputsList.toString() : header2 + "\n\n"
                + inputsList.toString();
    }

    private String border(int num, String symbl) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < num; i++)
            sb.append(symbl);

        return sb.toString();
    }

    public synchronized CaseOutput[] getCaseOutputs(int caseId, int jobId) throws EmfException {
        Session session = sessionFactory.getSession();
        List<CaseOutput> outputs = null;

        try {
            if (jobId == 0)
                outputs = dao.getCaseOutputs(caseId, session);
            else
                outputs = dao.getCaseOutputs(caseId, jobId, session);

            return outputs.toArray(new CaseOutput[0]);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized void registerOutputs(CaseOutput[] outputs, String[] jobKeys) throws EmfException {
        try {
            CaseJob job = null;

            for (int i = 0; i < outputs.length; i++) {
                job = getJobFromKey(jobKeys[i]);
                outputs[i].setCaseId(job.getCaseId());
                outputs[i].setJobId(job.getId());
            }

            getImportService().importDatasetsForCaseOutput(job.getUser(), outputs);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().startsWith("Error registering output"))
                throw new EmfException(e.getMessage());
            throw new EmfException("Error registering output: " + e.getMessage());
        }
    }

    public synchronized void removeCaseOutputs(User user, CaseOutput[] outputs, boolean deleteDataset)
            throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.removeCaseOutputs(user, outputs, deleteDataset, session);
        } catch (Exception e) {
            e.printStackTrace();
            // log.error("Could not remove case output " + outputs[0].getName() + " etc.\n" + e.getMessage());
            // throw new EmfException("Could not remove case output " + outputs[0].getName() + " etc.");
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized void updateCaseOutput(User user, CaseOutput output) throws EmfException {
        Session localSession = sessionFactory.getSession();

        try {
            checkNExtendCaseLock(user, getCase(output.getCaseId(), localSession));
        } catch (EmfException e) {
            throw e;
        }

        try {
            CaseOutput loaded = (CaseOutput) dao.loadCaseOutput(output, localSession);

            if (loaded != null && loaded.getId() != output.getId())
                throw new EmfException("Case output uniqueness check failed (" + loaded.getId() + "," + output.getId()
                        + ")");

            // Clear the cached information. To update a case
            // FIXME: Verify the session.clear()
            localSession.clear();
            dao.updateCaseOutput(output, localSession);
            // setStatus(user, "Saved input " + input.getName() + " to database.", "Save Input");
        } catch (RuntimeException e) {
            e.printStackTrace();
            log.error("Could not update case output: " + output.getName() + ".\n" + e);
            throw new EmfException("Could not update case output: " + output.getName() + ".");
        } finally {
            localSession.close();
        }

    }

    public synchronized void removeMessages(User user, JobMessage[] msgs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.removeJobMessages(msgs, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not remove case output " + msgs[0].getMessage() + " etc.\n" + e.getMessage());
            throw new EmfException("Could not remove case messages " + msgs[0].getMessage());
        } finally {
            session.close();
        }

    }

    public synchronized CaseOutput addCaseOutput(User user, CaseOutput output) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            checkNExtendCaseLock(user, getCase(output.getCaseId(), session));
        } catch (EmfException e) {
            throw e;
        }

        try {
            if (dao.caseOutputExists(output, session))
                throw new EmfException("The combination of 'Output Name'and 'Job' should be unique.");

            dao.add(output, session);
            return (CaseOutput) dao.loadCaseOutput(output, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new case output '" + output.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new case output '" + output.getName() + "'");
        } finally {
            session.close();
        }
    }

    private synchronized void checkNExtendCaseLock(User user, Case currentCase) throws EmfException {
        Case locked = obtainLocked(user, currentCase);

        if (!locked.isLocked(user))
            throw new EmfException("Lock on the current case object expired. User " + locked.getLockOwner()
                    + " has it now.");
    }

    private boolean doNotExportJobs(Session session) {
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("DO_NOT_EXPORT_JOBS", session);
            String value = property.getValue();
            return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
        } catch (Exception e) {
            return false;// Default value for maxpool and poolsize
        }
    }

    private void logNumDBConn(String prefix) throws EmfException {
        String logNumDBConnCmd = "ps aux | grep postgres | wc -l";
        InputStream inStream = RemoteCommand.executeLocal(logNumDBConnCmd);

        RemoteCommand.logRemoteStdout("Logged DB connections: " + prefix, inStream);
    }

    public synchronized AirQualityModel addAirQualityModel(AirQualityModel airQModel) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            AirQualityModel temp = (AirQualityModel) dao.load(AirQualityModel.class, airQModel.getName(), session);

            if (temp != null)
                return temp;
            dao.add(airQModel, session);
            return (AirQualityModel) dao.load(AirQualityModel.class, airQModel.getName(), session);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new AirQualityModel '" + airQModel.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new AirQualityModel '" + airQModel.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized EmissionsYear addEmissionYear(EmissionsYear emissYear) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(emissYear, session);
            return (EmissionsYear) dao.load(EmissionsYear.class, emissYear.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new EmissionsYear '" + emissYear.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new EmissionsYear '" + emissYear.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized MeteorlogicalYear addMeteorologicalYear(MeteorlogicalYear metYear) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(metYear, session);
            return (MeteorlogicalYear) dao.load(MeteorlogicalYear.class, metYear.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new MeteorlogicalYear '" + metYear.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new MeteorlogicalYear '" + metYear.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized Speciation addSpeciation(Speciation speciation) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Speciation temp = (Speciation) dao.load(Speciation.class, speciation.getName(), session);

            if (temp != null)
                return temp;

            dao.add(speciation, session);
            return (Speciation) dao.load(Speciation.class, speciation.getName(), session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add new speciation '" + speciation.getName() + "'\n" + e.getMessage());
            throw new EmfException("Could not add new speciation '" + speciation.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized String getJobStatusMessage(int caseId) {
        Session session = sessionFactory.getSession();

        int failedCount = 0;
        int waitingCount = 0;
        int runningCount = 0;

        try {
            CaseJob[] jobs = getCaseJobs(caseId);

            if (jobs.length == 0)
                return "";

            for (CaseJob job : jobs) {
                JobRunStatus status = job.getRunstatus();

                if (status == null)
                    continue;

                if (status.getName().equalsIgnoreCase("Failed"))
                    failedCount++;

                if (status.getName().equalsIgnoreCase("Waiting"))
                    waitingCount++;

                if (status.getName().equalsIgnoreCase("Running"))
                    runningCount++;
            }

            if (failedCount == 0 && waitingCount == 0 && runningCount == 0)
                return "";

            return "Current case has " + runningCount + " running, " + waitingCount + " waiting, and " + failedCount
                    + " failed jobs.";
        } catch (Exception e) {
            return "";
        } finally {
            session.close();
        }
    }

    private String replaceNonDigitNonLetterChars(String name) {
        String filename = name.trim();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < filename.length(); i++) {
            if (filename.charAt(i) == '.')
                sb.append(".");
            else if (!Character.isLetterOrDigit(filename.charAt(i)))
                sb.append("_");
            else
                sb.append(filename.charAt(i));
        }

        return sb.toString();
    }

    public synchronized String[] getAllCaseNameIDs() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getAllCaseNameIDs(session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not retrieve all case names and ids.", e);
            throw new EmfException("Could not retrieve all case names and ids. " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized Case addSensitivity2Case(User user, int parentCaseId, int templateCaseId, int[] jobIds,
            String jobGroup, Case sensitivityCase, GeoRegion region) throws EmfException {
        Session session = sessionFactory.getSession();
        Case lockedSC = null;
        Case lockedPC = null;
        Case lockedTC = null;
        String jobPrefix = (jobGroup == null || jobGroup.trim().isEmpty()) ? "" : jobGroup + " ";

        try {
            lockedSC = dao.obtainLocked(user, sensitivityCase, session);
            int targetId = sensitivityCase.getId();

            Case parent = dao.getCase(parentCaseId, session);
            lockedPC = dao.obtainLocked(user, parent, session);

            Case template = dao.getCase(templateCaseId, session);
            lockedTC = dao.obtainLocked(user, template, session);

            if (lockedSC == null)
                throw new EmfException("Cannot obtain lock for adding sensitivity to happen: User "
                        + sensitivityCase.getLockOwner() + " has the lock for case '" + sensitivityCase.getName() + "'");

            if (lockedPC == null)
                throw new EmfException("Cannot obtain lock for adding sensitivity to happen: User "
                        + parent.getLockOwner() + " has the lock for case '" + parent.getName() + "'");

            if (lockedTC == null)
                throw new EmfException("Cannot obtain lock for adding sensitivity to happen: User "
                        + template.getLockOwner() + " has the lock for case '" + template.getName() + "'");

            List<CaseJob> existingJobs = dao.getCaseJobs(targetId, session);
            List<CaseInput> existingInputs = dao.getCaseInputs(targetId, session);
            List<CaseParameter> existingParams = dao.getCaseParameters(targetId, session);
            CaseJob[] jobs2copy = getJobs2Copy(jobIds);

            checkJobsDuplication(existingJobs, jobs2copy, jobPrefix);

            CaseJob[] jobs = cloneCaseJobs(lockedSC.getId(), lockedTC.getId(), jobGroup, jobPrefix, jobs2copy, region, user);
            CaseInput[] inputs = cloneCaseInputs(parentCaseId, lockedSC.getId(), getValidCaseInputs4SensitivityCase(
                    template.getId(), jobIds, jobs2copy, session), region, session);
            CaseParameter[] params = cloneCaseParameters(parentCaseId, lockedSC.getId(),
                    getValidCaseParameters4SensitivityCase(template.getId(), jobIds, jobs2copy, session), region, session);

            addCaseJobs4Sensitivity(user, targetId, jobs);
            addCaseInputs(targetId, removeRedundantInputs(inputs, existingInputs, jobPrefix), jobPrefix, region);
            addCaseParameters(targetId, removeRedundantParams(params, existingParams, jobPrefix), jobPrefix, region);

            // NOTE: add sectors to sensitivity case from the selected jobs
            existingJobs.addAll(Arrays.asList(jobs));
            lockedSC.setSectors(getSectorsFromJobs(existingJobs.toArray(new CaseJob[0])));

            updateCase(lockedSC);

            return lockedSC;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not add sensitivity to sensitivity case.", e);
            throw new EmfException(e.getMessage());
        } finally {
            try {
                if (lockedSC != null)
                    dao.releaseLocked(user, lockedSC, session);

                session.clear();
                if (lockedPC != null)
                    dao.releaseLocked(user, lockedPC, session);

                session.clear();
                if (lockedTC != null)
                    dao.releaseLocked(user, lockedTC, session);

                session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkJobsDuplication(List<CaseJob> existingJobs, CaseJob[] jobs2copy, String jobPrefix)
            throws EmfException {
        for (Iterator<CaseJob> iter = existingJobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            String jobName = job.getName();
            Sector sector = job.getSector();
            GeoRegion region = job.getRegion();

            for (CaseJob temp : jobs2copy)
                if (jobName.equalsIgnoreCase(jobPrefix + temp.getName()) &&
                        objectsEqual(sector, temp.getSector()) &&
                        objectsEqual(region, temp.getRegion()))
                    throw new EmfException("Job (" + temp.getName() + ") has an equivalent one existing.");
        }
    }

    private boolean objectsEqual(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null)
            return true;
        
        if (obj1 == null && obj2 != null)
            return false;
        
        if (obj1 != null && obj2 == null)
            return false;
        
        if (obj1 != null && obj2 != null)
            return obj1.equals(obj2);
        
        return false;
    }

    private CaseInput[] removeRedundantInputs(CaseInput[] inputs, List<CaseInput> existingInputs, String jobPrefix)
            throws EmfException {
        List<CaseInput> uniqueInputs = new ArrayList<CaseInput>();
        uniqueInputs.addAll(Arrays.asList(inputs));
        Session session = this.sessionFactory.getSession();
        CaseInput[] existingInputsArray = existingInputs.toArray(new CaseInput[0]);
        CaseJob[] selectedJobs = new CaseJob[inputs.length];
        CaseJob[] existingJobs = new CaseJob[existingInputs.size()];

        try {
            for (int i = 0; i < inputs.length; i++)
                selectedJobs[i] = dao.getCaseJob(inputs[i].getCaseJobID(), session);

            for (int j = 0; j < existingInputs.size(); j++)
                existingJobs[j] = dao.getCaseJob(existingInputsArray[j].getCaseJobID(), session);
        } catch (Exception e) {
            throw new EmfException("Cannot get jobs for comparisons in removing dedundant case inputs. "
                    + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }

        for (int m = 0; m < inputs.length; m++) {
            for (int n = 0; n < existingInputsArray.length; n++) {
                String inputJobName = "";
                String nextJobName = "";

                if (selectedJobs[m] != null)
                    inputJobName = jobPrefix + selectedJobs[m].getName();

                if (existingJobs[n] != null)
                    nextJobName = existingJobs[n].getName();

                Sector inputSector = inputs[m].getSector();
                Sector nextSector = existingInputsArray[n].getSector();
                boolean sectorExists = (inputSector == null && nextSector == null)
                        || (inputSector != null && inputSector.equals(nextSector));

                CaseProgram inputProgram = inputs[m].getProgram();
                CaseProgram nextProgram = existingInputsArray[n].getProgram();
                boolean programExists = (inputProgram == null && nextProgram == null)
                        || (inputProgram != null && inputProgram.equals(nextProgram));

                if (inputs[m].getInputName().equals(existingInputsArray[n].getInputName()) && sectorExists
                        && programExists && inputJobName.equals(nextJobName))
                    uniqueInputs.remove(inputs[m]);
            }
        }

        return uniqueInputs.toArray(new CaseInput[0]);
    }

    private CaseParameter[] removeRedundantParams(CaseParameter[] params, List<CaseParameter> existingParams,
            String jobPrefix) throws EmfException {
        List<CaseParameter> uniqueParams = new ArrayList<CaseParameter>();
        uniqueParams.addAll(Arrays.asList(params));
        Session session = this.sessionFactory.getSession();
        CaseParameter[] existingParamsArray = existingParams.toArray(new CaseParameter[0]);
        CaseJob[] selectedJobs = new CaseJob[params.length];
        CaseJob[] existingJobs = new CaseJob[existingParams.size()];

        try {
            for (int i = 0; i < params.length; i++)
                selectedJobs[i] = dao.getCaseJob(params[i].getJobId(), session);

            for (int j = 0; j < existingParams.size(); j++)
                existingJobs[j] = dao.getCaseJob(existingParamsArray[j].getJobId(), session);
        } catch (Exception e) {
            throw new EmfException("Cannot get jobs for comparisons in removing dedundant case parameters. "
                    + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }

        for (int m = 0; m < params.length; m++) {
            for (int n = 0; n < existingParams.size(); n++) {
                String paramJobName = "";
                String nextJobName = "";

                if (selectedJobs[m] != null)
                    paramJobName = jobPrefix + selectedJobs[m].getName();

                if (existingJobs[n] != null)
                    nextJobName = existingJobs[n].getName();

                Sector paramSector = params[m].getSector();
                Sector nextSector = existingParamsArray[n].getSector();
                boolean sectorExists = (paramSector == null && nextSector == null)
                        || (paramSector != null && paramSector.equals(nextSector));

                CaseProgram paramProgram = params[m].getProgram();
                CaseProgram nextProgram = existingParamsArray[n].getProgram();
                boolean programExists = (paramProgram == null && nextProgram == null)
                        || (paramProgram != null && paramProgram.equals(nextProgram));

                if (params[m].getParameterName().equals(existingParamsArray[n].getParameterName()) && sectorExists
                        && programExists && (paramJobName.equals(nextJobName)))
                    uniqueParams.remove(params[m]);
            }
        }

        return uniqueParams.toArray(new CaseParameter[0]);
    }

    public synchronized Case mergeCases(User user, int parentCaseId, int templateCaseId, int[] jobIds, String jobGroup,
            Case sensitivityCase) throws EmfException {
        Session session = sessionFactory.getSession();
        Case lockedSC = null;
        Case lockedPC = null;
        Case lockedTC = null;
        String jobPrefix = (jobGroup == null || jobGroup.trim().isEmpty()) ? "" : jobGroup + " ";

        try {
            Abbreviation abbr = sensitivityCase.getAbbreviation();

            if (abbr != null) {
                try {
                    dao.add(abbr, session);
                } catch (RuntimeException e) {
                    throw new EmfException("Please check if the specified abbreviation already exists.");
                }
            }

            dao.add(sensitivityCase, session);
            Case loaded = (Case) dao.load(Case.class, sensitivityCase.getName(), session);
            lockedSC = dao.obtainLocked(user, loaded, session);
            int targetId = loaded.getId();
            GeoRegion senRegion = (loaded.getRegions() == null ? null : loaded.getRegions()[0]);

            Case parent = dao.getCase(parentCaseId, session);
            lockedPC = dao.obtainLocked(user, parent, session);
            Case template = dao.getCase(templateCaseId, session);
            lockedTC = dao.obtainLocked(user, template, session);

            if (lockedSC == null)
                throw new EmfException("Cannot obtain lock for merging case to happen: User " + loaded.getLockOwner()
                        + " has the lock for case '" + loaded.getName() + "'");

            if (lockedPC == null)
                throw new EmfException("Cannot obtain lock for merging case to happen: User " + parent.getLockOwner()
                        + " has the lock for case '" + parent.getName() + "'");

            if (lockedTC == null)
                throw new EmfException("Cannot obtain lock for merging case to happen: User " + template.getLockOwner()
                        + " has the lock for case '" + template.getName() + "'");

            CaseJob[] jobs2copy = getJobs2Copy(jobIds);
            CaseJob[] jobs = cloneCaseJobs(lockedSC.getId(), lockedTC.getId(), jobGroup, jobPrefix, jobs2copy, senRegion, user);
            CaseInput[] inputs = cloneCaseInputs(parentCaseId, lockedSC.getId(), getValidCaseInputs4SensitivityCase(
                    template.getId(), jobIds, jobs2copy, session), senRegion, session);
            CaseParameter[] params = cloneCaseParameters(parentCaseId, lockedSC.getId(),
                    getValidCaseParameters4SensitivityCase(template.getId(), jobIds, jobs2copy, session), senRegion, session);

            addCaseJobs4Sensitivity(user, targetId, jobs);
            addCaseInputs(targetId, inputs, jobPrefix, senRegion);
            addCaseParameters(targetId, params, jobPrefix, senRegion);
            copySummaryInfo(lockedPC, lockedSC);

            // NOTE: copy input/output folder from template case
            lockedSC.setInputFileDir(lockedTC.getInputFileDir());
            lockedSC.setOutputFileDir(lockedTC.getOutputFileDir());

            // NOTE: set sectors from the selected jobs
            lockedSC.setSectors(getSectorsFromJobs(jobs));

            if (abbr == null)
                lockedSC.setAbbreviation(new Abbreviation(lockedSC.getId() + ""));

            updateCase(lockedSC);
            dao.add(new CasesSens(parentCaseId, lockedSC.getId()));

            return lockedSC;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not merge case.", e);
            throw new EmfException("Could not merge case. " + e.getMessage());
        } finally {
            try {
                if (lockedSC != null)
                    dao.releaseLocked(user, lockedSC, session);

                session.clear();
                if (lockedPC != null)
                    dao.releaseLocked(user, lockedPC, session);

                session.clear();
                if (lockedTC != null)
                    dao.releaseLocked(user, lockedTC, session);

                session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Sector[] getSectorsFromJobs(CaseJob[] jobs) {
        List<Sector> sectors = new ArrayList<Sector>();

        for (CaseJob job : jobs) {
            Sector sctr = job.getSector();

            if (sctr != null)
                sectors.add(sctr);
        }

        TreeSet<Sector> set = new TreeSet<Sector>(sectors);
        List<Sector> uniqueSectors = new ArrayList<Sector>(set);

        return uniqueSectors.toArray(new Sector[0]);
    }

    private void copySummaryInfo(Case parent, Case sensitivityCase) {
        sensitivityCase.setAirQualityModel(parent.getAirQualityModel());
        sensitivityCase.setBaseYear(parent.getBaseYear());
        sensitivityCase.setControlRegion(parent.getControlRegion());
        sensitivityCase.setDescription("Sensitivity on " + parent.getName() + ": " + parent.getDescription());
        sensitivityCase.setEmissionsYear(parent.getEmissionsYear());
        sensitivityCase.setFutureYear(parent.getFutureYear());
        // sensitivityCase.setGrid(parent.getGrid());
        sensitivityCase.setGridDescription(parent.getGridDescription());
        // sensitivityCase.setGridResolution(parent.getGridResolution());
        sensitivityCase.setMeteorlogicalYear(parent.getMeteorlogicalYear());
        sensitivityCase.setModel(parent.getModel());
        sensitivityCase.setModelVersion(parent.getModelVersion());
        sensitivityCase.setModelingRegion(parent.getModelingRegion());
        sensitivityCase.setProject(parent.getProject());
        sensitivityCase.setSpeciation(parent.getSpeciation());
        sensitivityCase.setStartDate(parent.getStartDate());
        sensitivityCase.setEndDate(parent.getEndDate());
        sensitivityCase.setTemplateUsed(parent.getName());
        sensitivityCase.setNumEmissionsLayers(parent.getNumEmissionsLayers());
        sensitivityCase.setNumMetLayers(parent.getNumMetLayers());
    }

    private CaseJob[] getJobs2Copy(int[] jobIds) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            CaseJob[] jobs = new CaseJob[jobIds.length];

            for (int i = 0; i < jobs.length; i++)
                jobs[i] = dao.getCaseJob(jobIds[i], session);

            return jobs;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all jobs.", e);
            throw new EmfException("Could not get all jobs. " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private CaseJob[] cloneCaseJobs(int targetCaseId, int parentCaseId, String jobGroup, String jobPrefix,
            CaseJob[] jobs, GeoRegion senRegion, User user) throws Exception {
        List<CaseJob> copied = new ArrayList<CaseJob>();

        for (int i = 0; i < jobs.length; i++) {
            CaseJob job = (CaseJob) DeepCopy.copy(jobs[i]);
            job.setName(jobPrefix + job.getName());
            job.setCaseId(targetCaseId);
            job.setParentCaseId(parentCaseId);
            job.setJobGroup(jobGroup);
            job.setUser(user);
            job.setRunJobUser(null);
            
            if (job.getRegion() != null)
                job.setRegion(senRegion);
            
            copied.add(job);
        }

        return copied.toArray(new CaseJob[0]);
    }

    private CaseInput[] cloneCaseInputs(int parentCaseId, int targetCaseId, CaseInput[] inputs, GeoRegion region, Session session)
            throws Exception {
        List<CaseInput> inputList2Target = new ArrayList<CaseInput>();

        for (int i = 0; i < inputs.length; i++) {
            CaseInput tempInput = (CaseInput) DeepCopy.copy(inputs[i]);
            
            if (tempInput.getRegion() != null)
                tempInput.setRegion(region);
            
            CaseInput inputFromParent = getParentCaseInputs4SensitivityCase(parentCaseId, tempInput, region, session);
            boolean modifiedFromParent = false;

            if (inputFromParent != null) {
                tempInput.setParentCaseId(parentCaseId);

                if (tempInput.getDataset() == null) {
                    tempInput.setDataset(inputFromParent.getDataset());
                    tempInput.setVersion(inputFromParent.getVersion());
                    tempInput.setDatasetType(inputFromParent.getDatasetType());
                    modifiedFromParent = true;
                }

                if (tempInput.getEnvtVars() == null) {
                    tempInput.setEnvtVars(inputFromParent.getEnvtVars());
                    modifiedFromParent = true;
                }

                if (tempInput.getSubdirObj() == null) {
                    tempInput.setSubdirObj(inputFromParent.getSubdirObj());
                    modifiedFromParent = true;
                }

                if (modifiedFromParent)
                    tempInput.setLastModifiedDate(inputFromParent.getLastModifiedDate());
            } else {
                tempInput.setParentCaseId(inputs[i].getCaseID());
            }

            tempInput.setCaseID(targetCaseId);
            inputList2Target.add(tempInput);
        }

        /*
         * NOTE: If two inputs are very similar in the template, they potentially could be identical after they are
         * updated from the parent. This makes sure that we do not try to add 2 identical inputs. GOTCHA: If we need
         * both inputs, maybe we need to check for this and copy the original inputs without updating from parent.
         */
        // TreeSet<CaseInput> set = new TreeSet<CaseInput>(inputList2Target);
        // List<CaseInput> uniqueInputs = new ArrayList<CaseInput>(set);
        //
        // return uniqueInputs.toArray(new CaseInput[0]);
        return inputList2Target.toArray(new CaseInput[0]);
    }

    private CaseParameter[] cloneCaseParameters(int parentCaseId, int targetCaseId, CaseParameter[] params,
            GeoRegion senRegion, Session session) throws Exception {
        List<CaseParameter> params2Target = new ArrayList<CaseParameter>();

        for (int i = 0; i < params.length; i++) {
            CaseParameter tempParam = (CaseParameter) DeepCopy.copy(params[i]);
            
            if (tempParam.getRegion() != null)
                tempParam.setRegion(senRegion);
            
            CaseParameter parentParameter = getParentCaseParameters4SensitivityCase(parentCaseId, tempParam, senRegion, session);
            boolean modifiedFromParent = false;

            if (parentParameter != null) {
                tempParam.setParentCaseId(parentCaseId);

                if (tempParam.getEnvVar() == null) {
                    tempParam.setEnvVar(parentParameter.getEnvVar());
                    modifiedFromParent = true;
                }

                if (tempParam.getType() == null) {
                    tempParam.setType(parentParameter.getType());
                    modifiedFromParent = true;
                }

                if (tempParam.getValue() == null || tempParam.getValue().trim().isEmpty()) {
                    tempParam.setValue(parentParameter.getValue());
                    modifiedFromParent = true;
                }

                if (tempParam.getPurpose() == null || tempParam.getPurpose().trim().isEmpty()) {
                    tempParam.setPurpose(parentParameter.getPurpose());
                    modifiedFromParent = true;
                }

                if (modifiedFromParent)
                    tempParam.setLastModifiedDate(parentParameter.getLastModifiedDate());
            } else {
                tempParam.setParentCaseId(params[i].getCaseID());
            }

            tempParam.setCaseID(targetCaseId);
            params2Target.add(tempParam);
        }

        /*
         * NOTE: If two parameters are very similar in the template, they potentially could be identical after they are
         * updated from the parent. This makes sure that we do not try to add 2 identical parameters. GOTCHA: If we need
         * both parameters, maybe we need to check for this and copy the original parameters without updating from
         * parent.
         */
        // TreeSet<CaseParameter> set = new TreeSet<CaseParameter>(params2Target);
        // List<CaseParameter> uniqueParameters = new ArrayList<CaseParameter>(set);
        //
        // return uniqueParameters.toArray(new CaseParameter[0]);
        return params2Target.toArray(new CaseParameter[0]);
    }

    public synchronized String validateNLInputs(int caseId) throws EmfException {
        String noLocalValues = "";
        try {
            CaseInput[] inputList = getCaseInputs(caseId);
            if (inputList == null)
                return noLocalValues;

            for (CaseInput input : inputList) {
                if (!input.isLocal() && input.getDataset() == null) {
                    noLocalValues += getInputValues(input) + "\n";
                }
            }
        } catch (RuntimeException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return noLocalValues;
    }

    public synchronized String validateNLParameters(int caseId) throws EmfException {
        String noLocalValues = "";
        CaseParameter[] paraList = getCaseParameters(caseId);
        for (CaseParameter par : paraList) {
            if (!par.isLocal() && par.getValue().trim().isEmpty()) {
                noLocalValues += getParamValues(par) + "\n";
            }
        }
        return noLocalValues;
    }

    private String getInputValues(CaseInput input) {
        String Value = (input.getEnvtVars() == null ? "" : input.getEnvtVars().getName()) + "; "
                + (input.getSector() == null ? "All sectors" : input.getSector().getName()) + "; "
                + getJobName(input.getCaseJobID()) + "; " + input.getName();
        return Value;
    }

    private String getParamValues(CaseParameter parameter) {
        String Value = (parameter.getEnvVar() == null ? "" : parameter.getEnvVar().getName()) + "; "
                + (parameter.getSector() == null ? "All sectors" : parameter.getSector().getName()) + "; "
                + getJobName(parameter.getJobId()) + "; " + parameter.getName();
        return Value;
    }

    private String getJobName(int jobId) {
        CaseJob job = dao.getCaseJob(jobId);
        if (job == null)
            return "";

        return job.getName();
    }

    public synchronized Case[] getSensitivityCases(int parentCaseId) throws EmfException {
        Session session = sessionFactory.getSession();
        Exception exc = null;
        Case parent = null;

        try {
            List<Case> cases = dao.getSensitivityCases(parentCaseId, session);
            return cases.toArray(new Case[0]);
        } catch (Exception e) {
            exc = e;
            parent = dao.getCase(parentCaseId, session);
        } finally {
            session.close();

            if (exc != null) {
                exc.printStackTrace();
                log.error("Could not get all sensitivity cases.", exc);
                throw new EmfException("Could not get all sensitivity cases for parent case"
                        + (parent == null ? "." : " (" + parent.getName() + ")."));
            }
        }

        return null;
    }

    public synchronized String[] getJobGroups(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            return dao.getJobGroups(caseId, session);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Could not get all job groups info.", e);
            throw new EmfException("Could not get all job groups info.");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private CaseInput[] getValidCaseInputs4SensitivityCase(int caseId, int[] jobIds, CaseJob[] jobs, Session session) {
        String query = "SELECT obj.id from "
                + CaseInput.class.getSimpleName()
                + " as obj WHERE obj.caseID = "
                + caseId
                + " AND ((obj.caseJobID = 0 AND obj.sector.id is null AND obj.region.id is null) "
                + getAndOrClause(jobIds, "obj.caseJobID", getSectorIds(jobs), "obj.sector.id", getRegionIds(jobs),
                        "obj.region.id") + ")";

        if (DebugLevels.DEBUG_9())
            log.warn(query);

        List<?> ids = session.createQuery(query).list();
        List<CaseInput> inputs = new ArrayList<CaseInput>();

        for (Iterator<?> iter = ids.iterator(); iter.hasNext();) {
            Integer id = (Integer) iter.next();
            inputs.add(dao.getCaseInput(id, session));
        }

        return inputs.toArray(new CaseInput[0]);
    }

    private CaseParameter[] getValidCaseParameters4SensitivityCase(int caseId, int[] jobIds, CaseJob[] jobs,
            Session session) {
        String query = "SELECT obj.id from "
                + CaseParameter.class.getSimpleName()
                + " as obj WHERE obj.caseID = "
                + caseId
                + " AND ((obj.jobId = 0 AND obj.sector.id is null AND obj.region.id is null) "
                + getAndOrClause(jobIds, "obj.jobId", getSectorIds(jobs), "obj.sector.id", getRegionIds(jobs),
                        "obj.region.id") + ")";

        if (DebugLevels.DEBUG_9())
            log.warn(query);

        List<?> ids = session.createQuery(query).list();

        List<CaseParameter> params = new ArrayList<CaseParameter>();

        for (Iterator<?> iter = ids.iterator(); iter.hasNext();)
            params.add(dao.getCaseParameter((Integer) iter.next(), session));

        return params.toArray(new CaseParameter[0]);
    }

    private int[] getSectorIds(CaseJob[] jobs) {
        int[] sectorIds = new int[jobs.length];

        for (int i = 0; i < jobs.length; i++) {
            Sector sector = jobs[i].getSector();

            if (sector == null)
                sectorIds[i] = -1;
            else
                sectorIds[i] = sector.getId();
        }

        return sectorIds;
    }

    private int[] getRegionIds(CaseJob[] jobs) {
        int[] regionIds = new int[jobs.length];

        for (int i = 0; i < jobs.length; i++) {
            GeoRegion region = jobs[i].getRegion();

            if (region == null)
                regionIds[i] = -1;
            else
                regionIds[i] = region.getId();
        }

        return regionIds;
    }

    private String getAndOrClause(int[] jIds, String jStr, int[] sIds, String sStr, int[] rIds, String rStr) {
        int numIDs = jIds.length;

        if (numIDs < 1)
            return "";

        // NOTE: the following implementation reflects this logic:
        // If you select job1 which is region1 and sector1, the logic for selecting the appropriate parameters from the
        // template is:
        // AAA - (all regions, all sectors AND all jobs) OR
        // RAA - (region=region1, all sectors AND all jobs) OR
        // ASA - (all regions, sector=sector1 AND all jobs) OR
        // RSA - (region=region1, sector=sector1 AND all jobs) OR
        // AAJ - (all regions, all sectors AND job=job1) OR
        // RAJ - (region=region1, all sectors AND job=job1) OR
        // ASJ - (all regions, sector=sector1 AND job=job1) OR
        // RSJ - (region=region1, sector=sector1 AND job=job1)
        // You have to also make sure this doesn't fail if the region/sector of the job is All regions/sectors.

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < numIDs; i++) {
            if (sIds[i] != -1 && rIds[i] != -1)
                sb.append(" OR (" + jStr + " = 0 AND " + sStr + " is null AND " + rStr + " = " + rIds[i] + ") OR ("
                        + jStr + " = 0 AND " + sStr + " = " + sIds[i] + " AND " + rStr + " is null) OR (" + jStr
                        + " = 0 AND " + sStr + " = " + sIds[i] + " AND " + rStr + " = " + rIds[i] + ") OR (" + jStr
                        + " = " + jIds[i] + " AND " + sStr + " is null AND " + rStr + " is null) OR (" + jStr + " = "
                        + jIds[i] + " AND " + sStr + " is null AND " + rStr + " = " + rIds[i] + ") OR (" + jStr + " = "
                        + jIds[i] + " AND " + sStr + " = " + sIds[i] + " AND " + rStr + " is null) OR (" + jStr + " = "
                        + jIds[i] + " AND " + sStr + " = " + sIds[i] + " AND " + rStr + " = " + rIds[i] + ")");
            else if (sIds[i] != -1 && rIds[i] == -1) {
                sb.append(" OR (" + jStr + " = 0 AND " + sStr + " = " + sIds[i] + " AND " + rStr + " is null) OR ("
                        + jStr + " = " + jIds[i] + " AND " + sStr + " is null AND " + rStr + " is null) OR (" + jStr
                        + " = " + jIds[i] + " AND " + sStr + " = " + sIds[i] + " AND " + rStr + " is null" + ")");
            } else if (sIds[i] == -1 && rIds[i] != -1) {
                sb.append(" OR (" + jStr + " = 0 AND " + sStr + " is null AND " + rStr + " = " + rIds[i] + ") OR ("
                        + jStr + " = " + jIds[i] + " AND " + sStr + " is null AND " + rStr + " is null) OR (" + jStr
                        + " = " + jIds[i] + " AND " + sStr + " is null AND " + rStr + " = " + rIds[i] + ")");
            } else
                sb.append(" OR (" + jStr + " = " + jIds[i] + " AND " + sStr + " is null AND " + rStr + " is null)");
        }

        return sb.toString();
    }

    // NOTE: the following implementation reflects this logic:
    //        
    // same env variable as param1 AND ((all jobs and all sectors) OR (all jobs and same sector as param1))
    //
    // If you get multiple parameters from that query, then you match with same sector.
    //
    // example:
    // template (name, env variable, sector, job) parent (name, env variable, sector, job)
    // PARAM1,EV1,all sectors,all jobs PARAM1,EV1,all sectors,all jobs
    // PARAM2,EV1,sector1,all jobs PARAM2,EV1,sector1,all jobs
    // PARAM3,EV1,all sectors,job1 PARAM3,EV1,sector2,all jobs
    // PARAM4,EV1,sector2,all jobs
    // PARAM5,EV1,sector3,all jobs
    //
    // So, the matching would be:
    // template, parent
    // 1,1
    // 2,2
    // 3,1
    // 4,3
    // 5,1
    // On 7/23/2009, since we introduced the GeoRegion in parameters (same as in inputs), the matching logic
    // reasonably expand into this combinations:
    // same env variable AND (
    // (all jobs and all sectors and all regions) OR
    // (all jobs and same sector and all regions) OR
    // (all jobs and all sectors and same region) OR
    // (all jobs and same sector and same region) )

    @SuppressWarnings("unchecked")
    private CaseParameter getParentCaseParameters4SensitivityCase(int parentcaseId, CaseParameter templateparam, GeoRegion selectedRegion, Session session) {

        if (DebugLevels.DEBUG_9()) {
            System.out.println("Trying to find match for " + templateparam.getName());
        }
        
        ParameterEnvVar env = templateparam.getEnvVar();
        
        if (env == null)
            return null; // NOTE: parameter's got to have an environment variable.
        
        Sector sector = templateparam.getSector();
        GeoRegion region = templateparam.getRegion();
        
        CaseParameter matchedParameter = null;
        CaseJob refJob = new CaseJob();
        
        //NOTE: if templateJob is NOT null it means that this template parameter is job specific
        CaseJob templateJob = dao.getCaseJob(templateparam.getJobId(), session);
        
        if (templateJob != null) {
            refJob.setName(templateJob.getName());
            refJob.setSector(templateJob.getSector());
        }
        
        //NOTE: if template input is job specific, this job's region (usually a generic region)
        // has to be reset to the selected region before it is used to search the relavant job
        // from the parent case
        if (templateJob != null && templateJob.getRegion() != null) refJob.setRegion(selectedRegion);
        
        CaseJob parentJob = (templateJob == null) ? null : dao.getCaseJob(parentcaseId, refJob, session);
        
        //NOTE: if template parameter is job specific and this job doesn't exist in parent case, then there is definitely no matches
        if (templateJob != null && parentJob == null) return null;
        
        //NOTE: parentJob is NOT null, implies that this template parameter is job specific
        if (parentJob != null) {
            //NOTE: get all job specific params by this specific job
            List<CaseParameter> params = dao.getCaseParametersByJobId(parentcaseId, parentJob.getId(), session);
            
            //NOTE: if there is no job specific params in parent case, then no matches
            if (params == null || params.size() == 0) return null;
            
            for (CaseParameter temp : params) {
                ParameterEnvVar tempEnv = temp.getEnvVar();
                
                //NOTE: if env var doesn't match, then it is not a match
                if (tempEnv == null || !env.equals(tempEnv)) continue;
                
                matchedParameter = temp;
                break; //NOTE: The first match would be it.
            }
            
            //NOTE: got to return even if matchedParameter is null
            return matchedParameter;
        }

        String query = "SELECT obj.id from " + CaseParameter.class.getSimpleName() + " as obj WHERE obj.caseID = "
                + parentcaseId + " AND obj.envVar.id = " + env.getId();

        String suffix = " AND ((obj.jobId = 0 AND obj.sector.id is null AND obj.region.id is null) ";

        if (sector != null && region != null) {
            suffix += "OR (obj.jobId = 0 AND obj.sector.id = " + sector.getId() + " AND obj.region.id = "
                    + region.getId() + ") " + "OR (obj.jobId = 0 AND obj.sector.id is null AND obj.region.id = "
                    + region.getId() + ") " + "OR (obj.jobId = 0 AND obj.sector.id = " + sector.getId()
                    + " AND obj.region.id is null))";
        } else if (sector != null && region == null) {
            suffix += "OR (obj.jobId = 0 AND obj.sector.id = " + sector.getId() + " AND obj.region.id is null))";
        } else if (sector == null && region != null) {
            suffix += "OR (obj.jobId = 0 AND obj.sector.id is null AND obj.region.id = " + region.getId() + "))";
        } else if (sector == null && region == null) {
            suffix += ")";
        }

        query += suffix;

        if (DebugLevels.DEBUG_9())
            log.warn(query);

        List<Integer> ids = session.createQuery(query).list();

        if (DebugLevels.DEBUG_9()) {
            System.out.println("#IDs returned by query: " + ids == null ? 0 : ids.size());
        }        

        /*
         * convert ids to parameters
         */
        List<CaseParameter> parameters = new ArrayList<CaseParameter>();
        for (Integer id : ids) {
            parameters.add(dao.getCaseParameter(id, session));
        }
        
        if (DebugLevels.DEBUG_9()) {
            System.out.println("Attempting to match: " + Utils.stringify(templateparam));
        }
        
        /*
         * make sure list is not empty
         */
        if (!parameters.isEmpty()) {

            /*
             * sort parameters and get the first one
             */
            Utils.sortParameters(parameters);
            matchedParameter = parameters.get(0);

            if (DebugLevels.DEBUG_9()) {
                System.out.println("Matched with: " + Utils.stringify(matchedParameter));
            }
        } else {
            if (DebugLevels.DEBUG_9()) {
                System.out.println("No match found: list empty");
            }
        }
        
        return matchedParameter;
    }

    @SuppressWarnings("unchecked")
    private CaseInput getParentCaseInputs4SensitivityCase(int parentcaseId, CaseInput templateinput, GeoRegion selectedRegion, Session session) {

        if (DebugLevels.DEBUG_9()) {
            System.out.println("Trying to find match for " + templateinput.getName());
        }
        
        InputEnvtVar env = templateinput.getEnvtVars();

        if (env == null)
            return null; // NOTE: input's got to have an environment variable.
        
        CaseInput matchedInput = null;
        CaseJob refJob = new CaseJob();
        GeoRegion region = templateinput.getRegion();
        Sector sector = templateinput.getSector();
        
        //NOTE: if templateJob is NOT null it means that this template input is job specific
        CaseJob templateJob = dao.getCaseJob(templateinput.getCaseJobID(), session);
        
        if (templateJob != null) {
            refJob.setName(templateJob.getName());
            refJob.setSector(templateJob.getSector());
        }
        
        //NOTE: if template input is job specific, this job's region (usually a generic region)
        // has to be reset to the selected region before it is used to search the relavant job
        // from the parent case
        if (templateJob != null && templateJob.getRegion() != null) refJob.setRegion(selectedRegion);
        
        CaseJob parentJob = (templateJob == null) ? null : dao.getCaseJob(parentcaseId, refJob, session);
        
        //NOTE: if template input is job specific and this job doesn't exist in parent case, then there is definitely no matches
        if (templateJob != null && parentJob == null) return null;
        
        //NOTE: parentJob is NOT null, implies that this template input is job specific
        if (parentJob != null) {
            //NOTE: get all job specific inputs by this specific job
            List<CaseInput> inputs = dao.getCaseInputsByJobIds(parentcaseId, new int[] {parentJob.getId()}, session);
            
            //NOTE: if there is no job specific inputs in parent case, then no matches
            if (inputs == null || inputs.size() == 0) return null;
            
            for (CaseInput temp : inputs) {
                InputEnvtVar tempEnv = temp.getEnvtVars();
                
                //NOTE: if env var doesn't match, then it is not a match
                if (tempEnv == null || !env.equals(tempEnv)) continue;
                
                matchedInput = temp;
                break; //NOTE: The first match would be it.
            }
            
            //NOTE: got to return even if matchedInput is null
            return matchedInput;
        }
        
        String query = "SELECT obj.id from " + CaseInput.class.getSimpleName() + " as obj WHERE obj.caseID = " + parentcaseId
                + " AND obj.envtVars.id = " + env.getId();

        String suffix = " AND ((obj.caseJobID = 0 AND obj.sector.id is null AND obj.region.id is null) ";

        if (sector != null && region != null) {
            suffix += "OR (obj.caseJobID = 0 AND obj.sector.id = " + sector.getId() + " AND obj.region.id = "
                    + region.getId() + ") " + "OR (obj.caseJobID = 0 AND obj.sector.id is null AND obj.region.id = "
                    + region.getId() + ") " + "OR (obj.caseJobID = 0 AND obj.sector.id = " + sector.getId()
                    + " AND obj.region.id is null))";
        } else if (sector != null && region == null) {
            suffix += "OR (obj.caseJobID = 0 AND obj.sector.id = " + sector.getId() + " AND obj.region.id is null))";
        } else if (sector == null && region != null) {
            suffix += "OR (obj.caseJobID = 0 AND obj.sector.id is null AND obj.region.id = " + region.getId() + "))";
        } else if (sector == null && region == null) {
            suffix += ")";
        }

        query += suffix;
        if (DebugLevels.DEBUG_9())
            log.warn(query);

        List<Integer> ids = session.createQuery(query).list();

        if (DebugLevels.DEBUG_9()) {
            System.out.println("#IDs returned by query: " + ids == null ? 0 : ids.size());
        }
        
        /*
         * convert ids to inputs
         */
        List<CaseInput> inputs = new ArrayList<CaseInput>();
        for (Integer id : ids) {
            inputs.add(dao.getCaseInput(id, session));
        }
        
        if (DebugLevels.DEBUG_9()) {
            System.out.println("Attempting to match: " + Utils.stringify(templateinput));
        }
        
        /*
         * make sure list is not empty
         */
        if (!inputs.isEmpty()) {

            /*
             * sort parameters and get the first one
             */
            Utils.sortInputs(inputs);
            matchedInput = inputs.get(0);

            if (DebugLevels.DEBUG_9()) {
                System.out.println("Matched with: " + Utils.stringify(matchedInput));
            }
        } else {
            if (DebugLevels.DEBUG_9()) {
                System.out.println("No match found: list empty");
            }
        }
        
        return matchedInput;
    }
    
    public synchronized void printCase(String folder, int caseId) throws EmfException {
        Session session = sessionFactory.getSession();
        Case currentCase = null;

        try {
            currentCase = dao.getCase(caseId, session);
            if (currentCase == null)
                throw new EmfException("Cannot retrieve current case.");

            File exportDir = new File(folder);
            if (!exportDir.canWrite()) {
                throw new EmfException("EMF cannot write to folder " + folder);
            }
            List<CaseJob> jobs = dao.getCaseJobs(caseId);
            List<CaseInput> inputs = dao.getCaseInputs(caseId, session);
            List<CaseParameter> parameters = dao.getCaseParameters(caseId, session);

            String prefix = currentCase.getName() + "_" + currentCase.getAbbreviation().getName() + "_";
            prefix = StringTools.replaceNoneLetterDigit(prefix, '_');
            String sumParamFile = prefix + "Summary_Parameters.csv";
            String inputsFile = prefix + "Inputs.csv";
            String jobsFile = prefix + "Jobs.csv";

            // First buffer: parameter
            // Second buffer: inputs
            // third buffer: jobs
            String[] caseExportString = getCaseExportString(currentCase, parameters, jobs, inputs, session);

            printCaseSumParams(caseExportString[0], folder, sumParamFile);
            printCaseInputs(caseExportString[1], folder, inputsFile);
            printCaseJobs(caseExportString[2], folder, jobsFile);

            // return caseExportString;
        } catch (Exception e) {
            log.error("Could not export case "
                    + (currentCase == null ? " (id = " + caseId + ")." : currentCase.getName() + "."), e);
            throw new EmfException("Could not export case: "
            // AME: this info makes the message too long to see it all in the window
                    // + (currentCase == null ? " (id = " + caseId + "). " : currentCase.getName() + ". ")
                    + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public String[] printLocalCase(int caseId) throws EmfException {
        Session session = sessionFactory.getSession();
        Case currentCase = null;

        try {
            currentCase = dao.getCase(caseId, session);
            if (currentCase == null)
                throw new EmfException("Cannot retrieve current case.");

            List<CaseJob> jobs = dao.getCaseJobs(caseId);
            List<CaseInput> inputs = dao.getCaseInputs(caseId, session);
            List<CaseParameter> parameters = dao.getCaseParameters(caseId, session);

            // First buffer: parameter
            // Second buffer: inputs
            // third buffer: jobs
            String[] caseExportString = getCaseExportString(currentCase, parameters, jobs, inputs, session);

            return caseExportString;
        } catch (Exception e) {
            log.error("Could not export case "
                    + (currentCase == null ? " (id = " + caseId + ")." : currentCase.getName() + "."), e);
            throw new EmfException("Could not export case: "
            // AME: this info makes the message too long to see it all in the window
                    // + (currentCase == null ? " (id = " + caseId + "). " : currentCase.getName() + ". ")
                    + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private synchronized String[] getCaseExportString(Case currentCase, List<CaseParameter> parameters,
            List<CaseJob> jobs, List<CaseInput> inputs, Session session) {

        List<String> caseExportList = new ArrayList<String>();
        caseExportList.add(bufferCaseSumParams(currentCase, parameters, jobs));
        caseExportList.add(bufferCaseInputs(inputs, jobs, session));
        caseExportList.add(bufferCaseJobs(jobs, session));
        return caseExportList.toArray(new String[0]);
    }

    private synchronized void printCaseSumParams(String sb, String folder, String sumParamFile) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(folder, sumParamFile))));
        writer.println(sb.toString());
        writer.close();
    }

    private synchronized void printCaseInputs(String sb, String folder, String inputsFile) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(folder, inputsFile))));
        writer.println(sb.toString());
        writer.close();
    }

    private synchronized void printCaseJobs(String sb, String folder, String jobsFile) throws IOException {
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(folder, jobsFile))));
        writer.println(sb.toString());
        writer.close();
    }

    private synchronized String bufferCaseSumParams(Case currentCase, List<CaseParameter> parameters, List<CaseJob> jobs) {
        String ls = System.getProperty("line.separator");
        String model = (currentCase.getModel() == null) ? "" : currentCase.getModel().getName();
        String modelRegion = (currentCase.getModelingRegion() == null) ? "" : currentCase.getModelingRegion().getName();
        // String gridName = (currentCase.getGrid() == null) ? "" : currentCase.getGrid().getName();
        // String gridResolution = (currentCase.getGridResolution() == null) ? "" : currentCase.getGridResolution()
        // .getName();
        String dstrModel = (currentCase.getAirQualityModel() == null) ? "" : currentCase.getAirQualityModel().getName();
        String speciation = (currentCase.getSpeciation() == null) ? "" : currentCase.getSpeciation().getName();
        String metYear = (currentCase.getMeteorlogicalYear() == null) ? "" : currentCase.getMeteorlogicalYear()
                .getName();
        String startDate = (currentCase.getStartDate() == null) ? "" : CustomDateFormat
                .format_MM_DD_YYYY_HH_mm(currentCase.getStartDate());
        String endDate = (currentCase.getEndDate() == null) ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(currentCase
                .getEndDate());
        EmissionsYear emisyr = currentCase.getEmissionsYear();
        String emisYear = (emisyr == null || emisyr.getName().trim().isEmpty() ? "" : emisyr.getName().trim());

        String summary = "\"#EMF_CASE_NAME="
                + clean(currentCase.getName())
                + "\""
                + ls
                + "\"#EMF_CASE_ABBREVIATION="
                + clean(currentCase.getAbbreviation() == null ? "" : currentCase.getAbbreviation().getName())
                + "\""
                + ls
                + "\"#EMF_CASE_DESCRIPTION="
                + clean(currentCase.getDescription() == null ? "" : processNewLines(currentCase.getDescription()))
                + "\""
                + ls
                + "\"#EMF_CASE_CATEGORY="
                + clean(currentCase.getCaseCategory() == null ? "" : currentCase.getCaseCategory().getName())
                + "\""
                + ls
                + "\"#EMF_PROJECT="
                + clean(currentCase.getProject() == null ? "" : currentCase.getProject().getName())
                + "\""
                + ls
                + "\"#EMF_SECTORS="
                + clean(getSectors(currentCase.getSectors()))
                + "\""
                + ls
                + getRegions(currentCase.getRegions(), ls)
                + "\"#EMF_CASE_COPIED_FROM="
                + clean(currentCase.getTemplateUsed())
                + "\""
                + ls
                + "\"#EMF_LAST_MODIFIED="
                + clean(currentCase.getLastModifiedBy() == null ? "" : currentCase.getLastModifiedBy().getName())
                + " on "
                + CustomDateFormat.format_MM_DD_YYYY_HH_mm(currentCase.getLastModifiedDate())
                + "\""
                + ls
                + "\"#EMF_IS_FINAL="
                + currentCase.getIsFinal()
                + " \""
                + ls
                + "\"#EMF_IS_TEMPLATE="
                + currentCase.isCaseTemplate()
                + "\""
                + ls
                + "\"#EMF_OUTPUT_JOB_SCRIPTS_FOLDER="
                + currentCase.getOutputFileDir()
                + "\""
                + ls
                + "\"#EMF_INPUT_FOLDER="
                + currentCase.getInputFileDir()
                + "\""
                + ls
                + "Tab,Parameter,Order,Envt. Var.,Region,Sector,Job,Program,Value,Type,Reqd?,Local?,Last Modified,Notes,Purpose"
                + ls + "Summary,Model to Run,0,MODEL_LABEL,,All sectors,All jobs for sector,All programs,\""
                + clean(model) + "\",String,TRUE,TRUE,,," + ls
                + "Summary,Model Version,0,MODEL_LABEL,,All sectors,All jobs for sector,All programs,\""
                + clean(currentCase.getModelVersion()) + "\",String,TRUE,TRUE,,," + ls
                + "Summary,Modeling Region,0,,,All sectors,All jobs for sector,All programs,\"" + clean(modelRegion)
                + "\",String,TRUE,TRUE,,," + ls
                + "Summary,Met Layers,0,,,All sectors,All jobs for sector,All programs,"
                + currentCase.getNumMetLayers() + ",Integer,TRUE,TRUE,,," + ls
                + "Summary,Emission Layers,0,,,All sectors,All jobs for sector,All programs,"
                + currentCase.getNumEmissionsLayers() + ",Integer,TRUE,TRUE,,," + ls
                + "Summary,Downstream Model,0,EMF_AQM,,All sectors,All jobs for sector,All programs,\""
                + clean(dstrModel) + "\",String,TRUE,TRUE,,," + ls
                + "Summary,Speciation,0,EMF_SPC,,All sectors,All jobs for sector,All programs,\"" + clean(speciation)
                + "\",String,TRUE,TRUE,,," + ls
                + "Summary,Meteorological Year,0,,,All sectors,All jobs for sector,All programs," + metYear
                + ",String,TRUE,TRUE,,," + ls
                + "Summary,Base Year,0,BASE_YEAR,,All sectors,All jobs for sector,All programs,"
                + emisYear + ",String,TRUE,TRUE,,," + ls
                + "Summary,Future Year,0,FUTURE_YEAR,,All sectors,All jobs for sector,All programs,"
                + currentCase.getFutureYear() + ",String,TRUE,TRUE,,," + ls
                + "Summary,Start Date & Time,0,EPI_STDATE_TIME,,All sectors,All jobs for sector,All programs,"
                + startDate + ",Date,TRUE,TRUE,,," + ls
                + "Summary,End Date & Time,0,EPI_ENDATE_TIME,,All sectors,All jobs for sector,All programs," + endDate
                + ",Date,TRUE,TRUE,,," + ls;

        StringBuffer sb = new StringBuffer(summary);

        for (Iterator<CaseParameter> iter = parameters.iterator(); iter.hasNext();) {
            CaseParameter param = iter.next();
            String name = param.getName();
            String order = param.getOrder() + "";
            String envVar = param.getEnvVar() == null ? "" : param.getEnvVar() + "";
            String region = (param.getRegion() == null) ? "" : param.getRegion().getName();
            String sector = (param.getSector() == null) ? "All sectors" : param.getSector() + "";
            String job = getJobName(param.getJobId(), jobs);
            String prog = param.getProgram() == null ? "" : param.getProgram() + "";
            String value = param.getValue() == null ? "" : param.getValue();
            String type = param.getType() == null ? "" : param.getType() + "";
            String reqrd = param.isRequired() + "";
            String local = param.isLocal() + "";
            String lstMod = param.getLastModifiedDate() == null ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(param
                    .getLastModifiedDate());
            String notes = param.getNotes() == null ? "" : processNewLines(param.getNotes());
            String purpose = param.getPurpose() == null ? "" : processNewLines(param.getPurpose());

            sb.append("Parameters,\"" + clean(name) + "\"," + order + ",\"" + clean(envVar) + "\",\"" + clean(region)
                    + "\",\"" + clean(sector) + "\",\"" + clean(job) + "\",\"" + clean(prog) + "\",\"" + clean(value)
                    + "\"," + clean(type) + "," + reqrd + "," + local + "," + lstMod + ",\"" + clean(notes) + "\",\""
                    + clean(purpose) + "\"" + ls);
        }

        return sb.toString();
    }

    private String processNewLines(String text) {
        int i = 0;
        
        while (i < text.length()) {
            if (text.charAt(i) == '\n') {
                text = text.substring(0, i) + locNewLine + text.substring(i+1);
                i += locNewLine.length() - 1;
            } else if (text.charAt(i) == '\r' && text.charAt(i+1) == '\n') {
                text = text.substring(0, i) + locNewLine + text.substring(i+2);
                i += locNewLine.length();
            } else if (text.charAt(i) == '\r') {
                text = text.substring(0, i) + locNewLine + text.substring(i+1);
                i += locNewLine.length() - 1;
            } else
                i++;
        }

        return text;
    }

    private String getRegions(GeoRegion[] regions, String ls) {
        StringBuffer sb = new StringBuffer();

        for (GeoRegion region : regions)
            sb.append("\"#EMF_REGION=" + region.getName() + "&" + region.getAbbreviation() + "&"
                    + region.getIoapiName() + "\"" + ls);

        return sb.toString();
    }

    private String getSectors(Sector[] sectors) {
        StringBuffer sb = new StringBuffer();

        for (Sector sector : sectors)
            sb.append(sector.getName() + "&");

        int lastAmp = sb.lastIndexOf("&");

        return lastAmp < 0 ? sb.toString() : sb.toString().substring(0, lastAmp);
    }

    private String getJobName(int jobId, List<CaseJob> jobs) {
        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();

            if (jobId == 0)
                return "All jobs for sector";

            if (jobId == job.getId())
                return job.getName();
        }

        return "";
    }

    private String getDependsOnJobsString(DependentJob[] dependentJobs, Session session) {
        StringBuffer sb = new StringBuffer();

        for (DependentJob job : dependentJobs) {
            CaseJob depJob = dao.getCaseJob(job.getJobId(), session);
            sb.append(depJob != null ? depJob.getName() + "&" : "");
        }

        int lastAmp = sb.lastIndexOf("&");

        return lastAmp < 0 ? sb.toString() : sb.toString().substring(0, lastAmp);
    }

    private synchronized String bufferCaseInputs(List<CaseInput> inputs, List<CaseJob> jobs, Session session) {
        String ls = System.getProperty("line.separator");
        String columns = "Tab,Inputname,Envt Variable,Region,Sector,Job,Program,Dataset,Version,QA status,DS Type,Reqd?,Local?,Subdir,Last Modified,Parentcase"
                + ls;

        StringBuffer sb = new StringBuffer(columns);

        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            String name = input.getName();
            String envVar = input.getEnvtVars() == null ? "" : input.getEnvtVars() + "";
            String sector = (input.getSector() == null) ? "All sectors" : input.getSector() + "";
            String region = (input.getRegion() == null) ? "" : input.getRegion().getName();
            String job = getJobName(input.getCaseJobID(), jobs);
            String prog = input.getProgram() == null ? "" : input.getProgram() + "";
            String dsName = input.getDataset() == null ? "" : input.getDataset().getName();
            String dsVersion = input.getVersion() == null ? "" : input.getVersion().getVersion() + "";
            String qaStatus = "";
            String dsType = input.getDatasetType() == null ? "" : input.getDatasetType().getName();
            String reqrd = input.isRequired() + "";
            String local = input.isLocal() + "";
            String subdir = input.getSubdirObj() == null ? "" : input.getSubdirObj() + "";
            String lstMod = input.getLastModifiedDate() == null ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(input
                    .getLastModifiedDate());
            Case parent = (input.getParentCaseId() > 0) ? dao.getCase(input.getParentCaseId(), session) : null;
            String parentName = parent != null ? parent.getName() : "";

            sb.append("Inputs,\"" + clean(name) + "\",\"" + clean(envVar) + "\",\"" + clean(region) + "\",\""
                    + clean(sector) + "\",\"" + clean(job) + "\",\"" + clean(prog) + "\",\"" + clean(dsName) + "\","
                    + dsVersion + "," + clean(qaStatus) + ",\"" + clean(dsType) + "\"," + reqrd + "," + local + ","
                    + clean(subdir) + "," + lstMod + ",\"" + clean(parentName) + "\"" + ls);
        }
        return sb.toString();
        // PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(folder, inputsFile))));
        // writer.println(sb.toString());
        // writer.close();
    }

    private String bufferCaseJobs(List<CaseJob> jobs, Session session) {
        String ls = System.getProperty("line.separator");
        String columns = "Tab,JobName,Order,Region,Sector,RunStatus,StartDate,CompletionDate,Executable,Arguments,Path,QueueOptions,JobGroup,Local,QueueID,User,Host,Notes,Purpose,DependsOn"
                + ls;

        StringBuffer sb = new StringBuffer(columns);

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            String name = job.getName();
            String order = job.getJobNo() + "";
            String region = (job.getRegion() == null) ? "" : job.getRegion().getName();
            String sector = (job.getSector() == null) ? "All sectors" : job.getSector() + "";
            String status = job.getRunstatus() == null ? "" : job.getRunstatus() + "";
            String start = job.getRunStartDate() == null ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(job
                    .getRunStartDate());
            String end = job.getRunCompletionDate() == null ? "" : CustomDateFormat.format_MM_DD_YYYY_HH_mm(job
                    .getRunCompletionDate());
            String exec = job.getExecutable() == null ? "" : job.getExecutable() + "";
            String args = job.getArgs() == null ? "" : job.getArgs();
            String path = job.getPath() == null ? "" : job.getPath();
            String qOptns = job.getQueOptions() == null ? "" : job.getQueOptions();
            String jobGrp = job.getJobGroup();
            String local = job.isLocal() + "";
            String qId = job.getIdInQueue() == null ? "" : job.getIdInQueue();
            String user = job.getUser() == null ? "" : job.getUser().getName();
            String host = job.getHost() == null ? "" : job.getHost() + "";
            String notes = job.getRunNotes() == null ? "" : processNewLines(job.getRunNotes());
            String purpose = job.getPurpose() == null ? "" : processNewLines(job.getPurpose());
            String dependsOn = getDependsOnJobsString(job.getDependentJobs(), session);

            sb.append("Jobs,\"" + clean(name) + "\"," + order + ",\"" + clean(region) + "\",\"" + clean(sector) + "\","
                    + clean(status) + "," + start + "," + end + "," + clean(exec) + ",\"" + clean(args) + "\","
                    + clean(path) + ",\"" + clean(qOptns) + "\",\"" + clean(jobGrp) + "\"," + local + ",\""
                    + clean(qId) + "\",\"" + clean(user) + "\", " + clean(host) + ",\"" + clean(notes) + "\",\""
                    + clean(purpose) + "\",\"" + clean(dependsOn) + "\"" + ls);
        }
        return sb.toString();
    }

    private String clean(String toClean) {
        if (toClean == null || toClean.trim().isEmpty())
            return "";

        String temp = toClean.replace("\"", StringTools.EMF_DOUBLE_QUOTE);

        return temp.replaceAll("\\\\", "/");
    }

    public synchronized int cancelJobs(int[] jobIds, User user) throws EmfException {
        if (jobIds == null || jobIds.length == 0)
            return 0;

        int jobCanceled = 0;

        for (int id : jobIds) {
            CaseJob job = dao.getCaseJob(id);

            if (job == null)
                continue;

            User runUser = job.getRunJobUser();

            if (!user.equals(runUser) && !user.isAdmin())
                throw new EmfException("only the user who is running the job '" + job.getName()
                        + "' or an administrator can cancel the job");

            TaskManagerFactory.getCaseJobTaskManager(sessionFactory).cancelJob(id, user);
            jobCanceled++;
        }

        return jobCanceled;
    }
    
    public String isGeoRegionInSummary(int caseId, GeoRegion[] grids) throws EmfException{
        String message = "";
        
        Case caseObj = getCase(caseId);
        List<GeoRegion> regions= Arrays.asList(caseObj.getRegions());
        
        for (GeoRegion region : grids) {           
            
            if ((region != null) && !(regions.contains(region)))
                message += "  ("+ region.getName() +") ";
        }  
        return message;
        
    }
    
    
    public String[] isGeoRegionUsed(int caseId, GeoRegion[] grids) throws EmfException{
        String[] message = new String[] {"", "",""};
        
        List<GeoRegion> regions = Arrays.asList(grids);
        
        CaseJob[] jobs = getCaseJobs(caseId);
        for (CaseJob job : jobs) {           
            GeoRegion region = job.getRegion();
            if ((region != null) && regions.contains(region)){
                if (!message[0].contains(region.getName())) 
                    message[0] += " ("+ region.getName() + ") ";
            }
        }
        
        CaseInput[] inputs = getCaseInputs(caseId);
        for (CaseInput input : inputs) {           
            GeoRegion region = input.getRegion();
            if ((region != null) && regions.contains(region)){
                if (!message[1].contains(region.getName())) 
                    message[1] += " ("+ region.getName()+ ") ";
            }
        }
        
        CaseParameter[] params = getCaseParameters(caseId);
        for (CaseParameter param : params) {           
            GeoRegion region = param.getRegion();
            if ((region != null) && (regions.contains(region))){
                if (!message[2].contains(region.getName())) 
                    message[2] += " ("+ region.getName()+ ") ";
            }
        }  
       
        return  message;
        
    }

}
