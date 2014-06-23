package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.commons.util.StringTools;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class CaseAssistanceService {
    private static Log log = LogFactory.getLog(ManagedCaseService.class);

    private static int svcCount = 0;

    private String svcLabel = null;

    public synchronized String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }
        log.info(svcLabel);

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    private CaseDAO caseDao;

    private DataCommonsDAO dataDao;

    private DatasetDAO dsDao;

    private HibernateSessionFactory sessionFactory;

    public CaseAssistanceService(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.caseDao = new CaseDAO();
        this.dataDao = new DataCommonsDAO();
        this.dsDao = new DatasetDAO();

        if (DebugLevels.DEBUG_9())
            System.out.println("In CaseAssistanceService constructor: Is the session Factory null? "
                    + (sessionFactory == null));

        myTag();

        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> " + myTag());

        log.info("CaseAssistanceService");
        log.info("Session factory null? " + (sessionFactory == null));
    }

    public synchronized void importCase(String folder, String[] files, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        CaseDaoHelper helper = new CaseDaoHelper(sessionFactory, caseDao, dataDao, dsDao);
        String[][] cases = sortCaseFiles(getFiles(folder, files));

        Case newCase = null;

        try {
            for (int i = 0; i < cases.length; i++) {
                CaseFileParser caseParser = new CaseFileParser(cases[i][0], cases[i][1], cases[i][2]);
                newCase = caseParser.getCase();

                if (newCase == null)
                    throw new EmfException("Case not properly parsed.");

                if (newCase.getName() == null || newCase.getName().trim().isEmpty())
                    throw new EmfException("Case name not specified.");

                if (newCase.getModel() == null || newCase.getModel().getName().isEmpty())
                    throw new EmfException("Case run model not specified.");

                Case existedCase = caseDao.getCaseFromName(newCase.getName(), session);

                if (existedCase != null)
                    throw new EmfException("Case (" + newCase.getName()
                            + ") to import has a duplicate name in cases table.");

                session.clear();
                resetCaseValues(user, newCase, session);
                session.clear(); // NOTE: to clear up the old object images
                session.flush();
                addNewCaseObject(newCase);

                session.clear(); // NOTE: to clear up the old object images
                Case loadedCase = caseDao.getCaseFromName(newCase.getName(), session);
                int caseId = loadedCase.getId();
                int modId = loadedCase.getModel().getId();
                insertJobs(caseId, user, caseParser.getJobs(), helper); // NOTE: Have to insert jobs before inserting
                                                                        // inputs/parameters
                HashMap<String, Integer> jobIds = getJobIds(caseDao.getCaseJobs(caseId, session));
                insertParameters(caseId, modId, caseParser.getParameters(), helper, jobIds);
                insertInputs(caseId, modId, caseParser.getInputs(), helper, jobIds);
            }
        } catch (Exception e) {
            log.error("Could not import case", e);

            if (e instanceof EmfException)
                throw (EmfException) e;

            Throwable ex = e.getCause();

            if (ex != null)
                throw new EmfException(ex.getMessage());

            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    private HashMap<String, Integer> getJobIds(List<CaseJob> caseJobs) {
        HashMap<String, Integer> jobIds = new HashMap<String, Integer>();

        if (caseJobs != null && caseJobs.size() > 0) {
            for (CaseJob job : caseJobs)
                jobIds.put(job.getName(), new Integer(job.getId()));
        }

        return jobIds;
    }

    private void addNewCaseObject(Case newCase) throws Exception {
        Session session = sessionFactory.getSession();

        try {
            caseDao.add(newCase, session);
        } catch (Exception e) {
            throw e;
        } finally {
            session.close();
        }
    }

    private String[] getFiles(String folder, String[] files) {
        String[] fullPathFiles = new String[files.length];

        for (int i = 0; i < files.length; i++)
            fullPathFiles[i] = folder + File.separator + files[i];

        return fullPathFiles;
    }

    private String[][] sortCaseFiles(String[] files) throws EmfException {
        int len = files.length;
        int numCases = len / 3;

        if (len % 3 != 0)
            throw new EmfException("Incomplete files for importing cases.");

        String[][] caseFiles = new String[numCases][3];

        // NOTE: needs to expand to support multiple cases import
        for (int i = 0; i < numCases; i++) {
            caseFiles[i][0] = getCorrectFile(files, "Summary");
            caseFiles[i][1] = getCorrectFile(files, "Input");
            caseFiles[i][2] = getCorrectFile(files, "Job");
        }

        return caseFiles;
    }

    private String getCorrectFile(String[] files, String fileType) {
        for (String file : files) {
            if (file.endsWith("_Summary_Parameters.csv") && fileType.equals("Summary"))
                return file;

            if (file.endsWith("_Inputs.csv") && fileType.equals("Input"))
                return file;

            if (file.endsWith("_Jobs.csv") && fileType.equals("Job"))
                return file;
        }

        return null;
    }

    private void resetCaseValues(User user, Case newCase, Session session) {
        Abbreviation abbr = newCase.getAbbreviation();

        if (abbr == null)
            abbr = new Abbreviation(newCase.getName());

        loadNSetObject(newCase, abbr, Abbreviation.class, abbr.getName(), session);

        CaseCategory cat = newCase.getCaseCategory();
        loadNSetObject(newCase, cat, CaseCategory.class, cat == null ? "" : cat.getName(), session);

        Project proj = newCase.getProject();
        loadNSetObject(newCase, proj, Project.class, proj == null ? "" : proj.getName(), session);

        ModelToRun model = newCase.getModel();
        loadNSetObject(newCase, model, ModelToRun.class, model == null ? "" : model.getName(), session);

        Region modRegion = newCase.getModelingRegion();
        loadNSetObject(newCase, modRegion, Region.class, modRegion == null ? "" : modRegion.getName(), session);

        AirQualityModel airMod = newCase.getAirQualityModel();
        loadNSetObject(newCase, airMod, AirQualityModel.class, airMod == null ? "" : airMod.getName(), session);

        Speciation spec = newCase.getSpeciation();
        loadNSetObject(newCase, spec, Speciation.class, spec == null ? "" : spec.getName(), session);

        MeteorlogicalYear metYear = newCase.getMeteorlogicalYear();
        loadNSetObject(newCase, metYear, MeteorlogicalYear.class, metYear == null ? "" : metYear.getName(), session);
        
        EmissionsYear emisYear = newCase.getEmissionsYear();
        loadNSetObject(newCase, emisYear, EmissionsYear.class, emisYear == null ? "" : emisYear.getName(), session);

        Sector[] sectors = newCase.getSectors();
        loadNSetSectors(newCase, sectors, session);

        GeoRegion[] regions = newCase.getRegions();
        loadNSetRegions(newCase, regions, session);

        newCase.setLastModifiedBy(user);
        newCase.setLastModifiedDate(new Date());
        newCase.setCreator(user);
    }

    private synchronized void loadNSetObject(Case newCase, Object obj, Class<?> clazz, String name, Session session) {
        Object temp = null;

        if (obj != null && name != null && !name.isEmpty())
            temp = checkDB(obj, clazz, name, session);

        if (obj instanceof Abbreviation) {
            newCase.setAbbreviation((Abbreviation) temp);
            return;
        }

        if (obj instanceof CaseCategory) {
            newCase.setCaseCategory((CaseCategory) temp);
            return;
        }

        if (obj instanceof Project) {
            newCase.setProject((Project) temp);
            return;
        }

        if (obj instanceof ModelToRun) {
            newCase.setModel((ModelToRun) temp);
            return;
        }

        if (obj instanceof Region) {
            newCase.setModelingRegion((Region) temp);
            return;
        }

        if (obj instanceof AirQualityModel) {
            newCase.setAirQualityModel((AirQualityModel) temp);
            return;
        }

        if (obj instanceof Speciation) {
            newCase.setSpeciation((Speciation) temp);
            return;
        }

        if (obj instanceof MeteorlogicalYear) {
            newCase.setMeteorlogicalYear((MeteorlogicalYear) temp);
            return;
        }
        
        if (obj instanceof EmissionsYear) {
            newCase.setEmissionsYear((EmissionsYear) temp);
            return;
        }
    }

    private void loadNSetRegions(Case newCase, GeoRegion[] regions, Session session) {
        if (regions == null || regions.length == 0)
            return;

        List<GeoRegion> all = new ArrayList<GeoRegion>();

        for (GeoRegion region : regions) {
            GeoRegion temp = (GeoRegion) checkDB(region, GeoRegion.class, region.getName(), session);
            all.add(temp);
        }

        newCase.setRegions(all.toArray(new GeoRegion[0]));
    }

    private void loadNSetSectors(Case newCase, Sector[] sectors, Session session) {
        if (sectors == null || sectors.length == 0)
            return;

        List<Sector> all = new ArrayList<Sector>();

        for (Sector sector : sectors) {
            if ( (sector != null) && (sector.getName().trim().length()>0)){
                //System.out.print("name = " +sector.getName()+"\n");
                if (sector.getName().toLowerCase().contains("all sectors"))
                    sector = new Sector("All Sectors", "All Sectors");
                Sector temp = (Sector) checkDB(sector, Sector.class, sector.getName(), session);
                all.add(temp);
            }
        }

        newCase.setSectors(all.toArray(new Sector[0]));
    }

    private Object checkDB(Object obj, Class<?> clazz, String name, Session session) {
        session.clear();
        session.flush();
        Object temp = caseDao.load(clazz, name, session);
        
        if (obj instanceof ModelToRun)
            temp = caseDao.loadModelTorun(name, session);

        if (temp != null && temp instanceof Abbreviation) {
            String random = Math.random() + "";
            String uniqueName = name + "_" + random.substring(2);
            ((Abbreviation) obj).setName(uniqueName);
            session.clear();
            session.flush();
            caseDao.addObject(obj, session);
            session.clear();
            session.flush();
            temp = caseDao.load(clazz, uniqueName, session);
        }
        
        if (temp == null) {
            session.clear();
            session.flush();
            caseDao.addObject(obj, session);
            session.clear();
            session.flush();
            temp = caseDao.load(clazz, name, session);
        }

        return temp;
    }

    private void insertJobs(int caseId, User user, List<CaseJob> jobs, CaseDaoHelper helper) throws EmfException {
        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();
            job.setCaseId(caseId);

            GeoRegion region = job.getRegion();
            job.setRegion(helper.getGeoRegion(region));

            Sector sector = job.getSector();
            job.setSector(helper.getSector(sector));

            job.setUser(helper.getUser(user));

            User runUser = job.getRunJobUser();
            job.setRunJobUser(helper.getUser(runUser));

            Host host = job.getHost();
            job.setHost(helper.getHost(host));

            JobRunStatus runStatus = job.getRunstatus();
            job.setRunstatus(helper.getJobRunStatus(runStatus));

            Executable exec = job.getExecutable();
            job.setExecutable(helper.getExcutable(exec));

            helper.insertCaseJob(job);
        }
    }

    private void insertParameters(int caseId, int model2RunId, List<CaseParameter> parameters, CaseDaoHelper helper,
            HashMap<String, Integer> jobIds) throws Exception {
        for (Iterator<CaseParameter> iter = parameters.iterator(); iter.hasNext();) {
            CaseParameter param = iter.next();
            param.setCaseID(caseId);

            ParameterName name = param.getParameterName();
            name.setModelToRunId(model2RunId);
            name = helper.getParameterName(name);
            param.setParameterName(name);

            ParameterEnvVar envVar = param.getEnvVar();
            envVar.setModelToRunId(model2RunId);
            envVar = helper.getParameterEnvVar(envVar);
            param.setEnvVar(envVar);

            ValueType type = param.getType();
            type = helper.getValueType(type);
            param.setType(type);

            GeoRegion region = param.getRegion();
            region = helper.getGeoRegion(region);
            param.setRegion(region);

            Sector sector = param.getSector();
            sector = helper.getSector(sector);
            param.setSector(sector);

            CaseProgram prog = param.getProgram();
            prog.setModelToRunId(model2RunId);
            prog = helper.getCaseProgram(prog);
            param.setProgram(prog);

            String jobName = param.getJobName();

            if (jobName != null && !jobName.trim().equalsIgnoreCase("All jobs for sector") && !jobName.trim().isEmpty())
                param.setJobId(jobIds.get(jobName) == null ? 0 : jobIds.get(jobName));

            helper.insertCaseParameter(param);
        }

    }

    private void insertInputs(int caseId, int model2RunId, List<CaseInput> inputs, CaseDaoHelper helper,
            HashMap<String, Integer> jobIds) throws Exception {
        int nameNoMatch = 0;
        int verNoMatch = 0;
        StringBuilder defltVerUsed = new StringBuilder();
        StringBuilder noMatchNames = new StringBuilder();
        
        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            input.setCaseID(caseId);

            InputName name = input.getInputName();
            name.setModelToRunId(model2RunId);
            name = helper.getInputName(name);
            input.setInputName(name);

            InputEnvtVar envVar = input.getEnvtVars();
            envVar.setModelToRunId(model2RunId);
            envVar = helper.getInputEnvVar(envVar);
            input.setEnvtVars(envVar);

            GeoRegion region = input.getRegion();
            region = helper.getGeoRegion(region);
            input.setRegion(region);

            Sector sector = input.getSector();
            sector = helper.getSector(sector);
            input.setSector(sector);

            CaseProgram prog = input.getProgram();
            prog.setModelToRunId(model2RunId);
            prog = helper.getCaseProgram(prog);
            input.setProgram(prog);

            DatasetType type = input.getDatasetType();
            type = helper.getDatasetType(type);
            input.setDatasetType(type);

            SubDir subDir = input.getSubdirObj();
            subDir.setModelToRunId(model2RunId);
            subDir = helper.getSubDir(subDir);
            input.setSubdirObj(subDir);

            EmfDataset ds = helper.getDataset(input.getDataset().getName(), type);
            Version ver = null;
            Version tempVer = input.getVersion();
            int verNum = 0;
            
            if (ds != null) {
                verNum = (tempVer == null ? ds.getDefaultVersion() : tempVer.getVersion());
                ver = helper.getDatasetVersion(ds.getId(), verNum);
                ver = (ver == null ? helper.getDatasetVersion(ds.getId(), ds.getDefaultVersion()) : ver);
            }
            
            if (ds == null) {
                nameNoMatch++; 
                noMatchNames.append("\t" + input.getName() + StringTools.SYS_NEW_LINE);
            }
            
            if (ver != null && ver.getVersion() != verNum) {
                verNoMatch++; 
                defltVerUsed.append("Default dataset version used for input \"" + input.getName() + "\"" 
                        + StringTools.SYS_NEW_LINE);
            }

            input.setDataset(ds);
            input.setVersion(ver);
            
            String jobName = input.getJobName();

            if (jobName != null && !jobName.trim().equalsIgnoreCase("All jobs for sector") && !jobName.trim().isEmpty())
                input.setCaseJobID(jobIds.get(jobName) == null ? 0 : jobIds.get(jobName));

            helper.insertCaseInput(input);
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (nameNoMatch > 0) {
            sb.append(nameNoMatch + " input(s) have no matching datasets in the database:" +
            		StringTools.SYS_NEW_LINE + noMatchNames.toString());
        }
        
        if (verNoMatch > 0) sb.append(defltVerUsed);
        
        if (sb.length() > 0) throw new Exception(sb.toString());
    }

    public synchronized String loadCMAQCase(String path, int jobId, int caseId, User user) throws EmfException {
        File logFile = new File(path);

        if (!logFile.exists())
            throw new EmfException("CMAQ log file doesn't exist: " + path + ".");

        if (!logFile.isFile())
            throw new EmfException("Please specify a valid log file.");

        if (!logFile.canRead())
            throw new EmfException("CMAQ log file is not readable by Tomcat: " + path + ".");

        List<CaseInput> inputs = getValidInputs(jobId, caseId);
        List<String> inputEnvs = getInputEnvVars(inputs);

        List<CaseParameter> paramObjects = getValidParameters(jobId, caseId);
        List<String> paramEnvs = getParamEnvVars(paramObjects);

        List<String> sumParamEnvs = combineEnvs(getSummaryEnvs(), new ArrayList<String>(), paramEnvs);

        if ((inputEnvs == null || inputEnvs.size() == 0) && (sumParamEnvs == null || sumParamEnvs.size() == 0))
            throw new EmfException("No valid inputs/parameters selected to load.");

        String lineSep = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        EMFCaseFile caseFile = new CMAQLogFile(logFile);
        caseFile.readInputs(inputEnvs, sb);
        caseFile.readParameters(sumParamEnvs, sb);

        try {
            resetSummaryValues(getSummaryEnvs(), caseFile, sb, lineSep, caseId);
        } catch (Exception e) {
            throw new EmfException("Error parsing summary info: " + e.getMessage());
        }

        int numInputsLoaded = resetInputValues(user, inputs, caseFile, sb, lineSep);
        int numParamsLoaded = resetParameterValues(user, paramObjects, caseFile, sb, lineSep);

        String msg = numInputsLoaded + " input value" + (numInputsLoaded > 1 ? "s" : "") + " loaded." + lineSep;
        msg += numParamsLoaded + " parameter value" + (numParamsLoaded > 1 ? "s" : "") + " loaded." + lineSep;

        return msg + sb.toString();
    }

    private List<String> getSummaryEnvs() {
        List<String> summaryEnvs = new ArrayList<String>();

        summaryEnvs.add("MODEL_LABEL");
        summaryEnvs.add("IOAPI_GRIDNAME_1");
        summaryEnvs.add("EMF_GRID");
        summaryEnvs.add("EMF_AQM");
        summaryEnvs.add("EMF_SPC");
        summaryEnvs.add("BASE_YEAR");
        summaryEnvs.add("FUTURE_YEAR");
        summaryEnvs.add("EPI_STDATE_TIME");
        summaryEnvs.add("EPI_ENDATE_TIME");
        summaryEnvs.add("EMF_PROJECT");

        return summaryEnvs;
    }

    private List<String> combineEnvs(List<String> summaryEnvs, List<String> inputEnvs, List<String> paramEnvs) {
        List<String> allEnvs = new ArrayList<String>();

        allEnvs.addAll(paramEnvs);

        for (Iterator<String> iter = summaryEnvs.iterator(); iter.hasNext();) {
            String env = iter.next();

            if (!allEnvs.contains(env))
                allEnvs.add(env);
        }

        allEnvs.addAll(inputEnvs);

        return allEnvs;
    }

    private void resetSummaryValues(List<String> paramEnvs, EMFCaseFile caseFile, StringBuffer sb, String lineSep,
            int caseId) throws Exception {
        Session session = sessionFactory.getSession();
        Case caze = caseDao.getCase(caseId, session);

        for (Iterator<String> iter = paramEnvs.iterator(); iter.hasNext();) {
            String envVar = iter.next();
            String value = caseFile.getParameterValue(envVar);

            if (value == null)
                continue;

            if (envVar.toUpperCase().equals("EMF_PROJECT")) {
                Project proj = caze.getProject();

                if (proj != null && proj.getName() != null && proj.getName().trim().equalsIgnoreCase(value))
                    continue;

                if (proj != null && proj.getName() != null)
                    sb.append("WARNING: project -- value replaced (previous: " + proj.getName() + ")" + lineSep);

                proj = new Project();
                proj.setName(value);
                proj = (Project) caseDao.load(Project.class, proj.getName(), session);

                caze.setProject(proj);
            }

            if (envVar.toUpperCase().equals("MODEL_LABEL")) {
                String origName = (caze.getModel() == null ? null : caze.getModel().getName());
                String origVersion = caze.getModelVersion();

                ModelToRun model = new ModelToRun();
                String version = setModelNVersion(model, value);
                model = addModelToRun(model);

                if (!model.getName().isEmpty() && !model.getName().equalsIgnoreCase(origName)) {
                    caze.setModel(model);
                    sb.append("WARNING: model -- value replaced (previous: " + origName + ")" + lineSep);
                }

                if (!version.isEmpty() && !version.equalsIgnoreCase(origVersion)) {
                    caze.setModelVersion(version);
                    sb.append("WARNING: version -- value replaced (previous: " + origVersion + ")" + lineSep);
                }
            }

            // if (envVar.toUpperCase().equals("IOAPI_GRIDNAME_1")) {
            // Grid grid = caze.getGrid();
            //
            // if (grid != null && grid.getName() != null && grid.getName().trim().equalsIgnoreCase(value))
            // continue;
            //
            // if (grid != null && grid.getName() != null)
            // sb.append("WARNING: grid -- value replaced (previous: " + grid.getName() + ")" + lineSep);
            //
            // grid = new Grid();
            // grid.setName(value);
            // grid = this.addGrid(grid);
            //
            // caze.setGrid(grid);
            // }

            // if (envVar.toUpperCase().equals("EMF_GRID")) {
            // GridResolution resltn = caze.getGridResolution();
            //
            // if (resltn != null && resltn.getName() != null && resltn.getName().trim().equalsIgnoreCase(value))
            // continue;
            //
            // if (resltn != null && resltn.getName() != null)
            // sb.append("WARNING: grid resolution -- value replaced (previous: " + resltn.getName() + ")"
            // + lineSep);
            //
            // resltn = new GridResolution();
            // resltn.setName(value);
            // resltn = this.addGridResolution(resltn);
            //
            // caze.setGridResolution(resltn);
            // }

            if (envVar.toUpperCase().equals("EMF_AQM")) {
                AirQualityModel aqm = caze.getAirQualityModel();

                if (aqm != null && aqm.getName() != null && aqm.getName().trim().equalsIgnoreCase(value))
                    continue;

                if (aqm != null && aqm.getName() != null)
                    sb.append("WARNING: air quality model -- value replaced (previous: " + aqm.getName() + ")"
                            + lineSep);

                aqm = new AirQualityModel();
                aqm.setName(value);
                aqm = this.addAirQualityModel(aqm);

                caze.setAirQualityModel(aqm);
            }

            if (envVar.toUpperCase().equals("EMF_SPC")) {
                Speciation spec = caze.getSpeciation();

                if (spec != null && spec.getName() != null && spec.getName().trim().equalsIgnoreCase(value))
                    continue;

                if (spec != null && spec.getName() != null)
                    sb.append("WARNING: speciation -- value replaced (previous: " + spec.getName() + ")" + lineSep);

                spec = new Speciation();
                spec.setName(value);
                spec = this.addSpeciation(spec);

                caze.setSpeciation(spec);
            }

            if (envVar.toUpperCase().equals("BASE_YEAR")) {
                EmissionsYear baseyr = caze.getEmissionsYear();

                if (baseyr != null && baseyr.getName() != null && baseyr.getName().trim().equalsIgnoreCase(value))
                    continue;
                
                if (baseyr != null && baseyr.getName() != null)
                    sb.append("WARNING: base year -- value replaced (previous: " + baseyr + ")" + lineSep);

                baseyr = new EmissionsYear(value.trim());
                baseyr = this.addEmissionsYear(baseyr);
                
                caze.setEmissionsYear(baseyr);
            }

            if (envVar.toUpperCase().equals("FUTURE_YEAR")) {
                int futureyr = caze.getFutureYear();

                sb.append("WARNING: base year -- value replaced (previous: " + futureyr + ")" + lineSep);

                caze.setFutureYear(Integer.parseInt(value.trim()));
            }

            if (envVar.toUpperCase().equals("EPI_STDATE_TIME")) {
                Date start = caze.getStartDate();

                if (start != null) {
                    String date = CustomDateFormat.format_MM_DD_YYYY_HH_mm(start);
                    sb.append("WARNING: start date -- value replaced (previous: " + date + ")" + lineSep);
                }

                caze.setStartDate(start);
            }

            if (envVar.toUpperCase().equals("EPI_ENDATE_TIME")) {
                Date end = caze.getEndDate();

                if (end != null) {
                    String date = CustomDateFormat.format_MM_DD_YYYY_HH_mm(end);
                    sb.append("WARNING: end date -- value replaced (previous: " + date + ")" + lineSep);
                }

                caze.setStartDate(end);
            }
        }

        try {
            caseDao.updateWithLock(caze, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private String setModelNVersion(ModelToRun model, String value) throws Exception {
        // NOTE: parsing mechanism in synch with the log file reader
        if (value == null || value.trim().isEmpty())
            return null;

        try {
            value = value.trim();
            int space = value.lastIndexOf(' ');
            String part1 = value;
            String part2 = "";

            if (space > 0) {
                String temp1 = value.substring(0, space);
                String temp2 = value.substring(space + 1).trim().toUpperCase();

                if (Character.isDigit(temp2.charAt(0)) || (temp2.startsWith("V") && Character.isDigit(temp2.charAt(1)))) {
                    part1 = temp1.trim();
                    part2 = value.substring(space + 1).trim();
                }
            }

            model.setName(part1);
            return part2;
        } catch (Exception e) {
            log.error("Error parsing case model and version string.", e);
            throw e;
        }
    }

    private int resetParameterValues(User user, List<CaseParameter> paramObjects, EMFCaseFile caseFile,
            StringBuffer sb, String lineSep) throws EmfException {
        int numLoaded = 0;

        for (Iterator<CaseParameter> iter = paramObjects.iterator(); iter.hasNext();) {
            CaseParameter param = iter.next();
            ParameterEnvVar envVar = param.getEnvVar();

            if (envVar == null)
                continue;

            String value = caseFile.getAttributeValue(envVar.getName());
            String existingVal = param.getValue();

            if (value == null || value.isEmpty() || value.equals(existingVal))
                continue;

            if (existingVal != null && !existingVal.trim().isEmpty())
                sb.append("WARNING: parameter \'" + param.getName() + "\'--value replaced (previous: " + existingVal
                        + ")" + lineSep);

            param.setValue(value);
            updateCaseParameter(user, param);
            numLoaded++;
        }

        return numLoaded;
    }

    private synchronized List<CaseInput> getValidInputs(int jobId, int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            CaseJob job = caseDao.getCaseJob(jobId, session);
            Sector sector = (job == null) ? null : job.getSector();

            List<CaseInput> jobSpecInputs = caseDao.getCaseInputsByJobIds(caseId, new int[] { jobId }, session);
            List<CaseInput> sectorSpecInputs = (sector != null) ? caseDao.getInputsBySector(caseId, sector, session)
                    : caseDao.getInputsForAllSectors(caseId, session);
            List<CaseInput> inputs4AllSectorsAllJobs = caseDao.getInputs4AllJobsAllSectors(caseId, session);

            return selectValidInputs(jobSpecInputs, sectorSpecInputs, inputs4AllSectorsAllJobs);
        } catch (Exception e) {
            log.error("Error reading case inputs for case id = " + caseId + " and job id = " + jobId + ".", e);
            throw new EmfException("Error reading case inputs for case id = " + caseId + " and job id = " + jobId + ".");
        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
    }

    private List<CaseInput> selectValidInputs(List<CaseInput> jobSpecInputs, List<CaseInput> sectorSpecInputs,
            List<CaseInput> inputs4AllSectorsAllJobs) {
        List<String> inputEnvs = getInputEnvVars(jobSpecInputs);

        for (Iterator<CaseInput> iter = sectorSpecInputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            InputEnvtVar envVar = input.getEnvtVars();
            int inputJobId = input.getCaseJobID();

            if (!inputEnvs.contains(envVar.getName()) && inputJobId == 0) {
                jobSpecInputs.add(input);
                inputEnvs.add(envVar.getName());
            }
        }

        for (Iterator<CaseInput> iter = inputs4AllSectorsAllJobs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            InputEnvtVar envVar = input.getEnvtVars();

            if (!inputEnvs.contains(envVar.getName())) {
                jobSpecInputs.add(input);
                inputEnvs.add(envVar.getName());
            }
        }

        return jobSpecInputs;
    }

    private synchronized List<String> getInputEnvVars(List<CaseInput> inputs) {
        List<String> envs = new ArrayList<String>();

        envs.add("OUTPERM"); // Mass storage variable

        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            InputEnvtVar env = iter.next().getEnvtVars();

            if (env != null && env.getName() != null && !env.getName().trim().isEmpty()) {
                envs.add(env.getName());
            }
        }

        return envs;
    }

    private int resetInputValues(User user, List<CaseInput> inputs, EMFCaseFile caseFile, StringBuffer sb,
            String lineSep) throws EmfException {
        int numLoaded = 0;

        for (Iterator<CaseInput> iter = inputs.iterator(); iter.hasNext();) {
            CaseInput input = iter.next();
            InputEnvtVar envVar = input.getEnvtVars();

            if (envVar == null)
                continue;

            if (input.getDatasetType() != null && !input.getDatasetType().isExternal()) {
                sb.append("Internal dataset type is not loaded for the time being for input: " + input.getName() + "."
                        + lineSep);
                continue;
            }

            String[] values = caseFile.getInputValue(envVar.getName());
            String massDir = caseFile.getAttributeValue("OUTPERM");

            if (values == null || values.length == 0)
                continue;

            checkNSetInputDataset(sb, lineSep, user, values, massDir, input);
            updateCaseInput(user, input);
            numLoaded++;
        }

        return numLoaded;
    }

    private void checkNSetInputDataset(StringBuffer sb, String lineSep, User user, String[] values, String massDir,
            CaseInput input) throws EmfException {
        String[] firstSrc = this.reconstructSources(new String[] { values[0], values[1] }, values[0]);
        Session session = sessionFactory.getSession();

        try {
            int[] dsIds = caseDao.getExternalDatasetIds(firstSrc[0], session);

            if (dsIds == null || dsIds.length == 0) {
                checkMassStorage(sb, lineSep, user, values, massDir, input);
                return;
            }

            EmfDataset dataset = getUniqueDataset(sb, lineSep, user, massDir, values, input, dsIds);
            setInputDatasetValues(user, values, input, dataset);
        } catch (Exception e) {
            log.error("Error resetting input values.", e);
            throw new EmfException(e.getMessage() == null ? "Error resetting input values." : e.getClass().toString()
                    + ": " + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private void setInputDatasetValues(User user, String[] values, CaseInput input, EmfDataset dataset)
            throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            if (dataset == null)
                return;

            DatasetType type = dataset.getDatasetType();
            input.setDataset(dataset);
            int dsVersion = dataset.getDefaultVersion();
            Version version = dsDao.getVersion(session, dataset.getId(), dsVersion);
            input.setVersion(version);

            if (!type.equals(input.getDatasetType()))
                input.setDatasetType(dataset.getDatasetType());
        } catch (Exception e) {
            log.error("Error resetting input values.", e);
            throw new EmfException("Error resetting input values. " + (e.getMessage() == null ? "" : e.getMessage()));
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private EmfDataset getUniqueDataset(StringBuffer sb, String lineSep, User user, String massDir, String[] values,
            CaseInput input, int[] dsIds) throws Exception {
        Session session = sessionFactory.getSession();

        try {
            String[] srcs = reconstructSources(values, values[0]);
            List<EmfDataset> dsWithAllSrcs = new ArrayList<EmfDataset>();
            EmfDataset latest = null;

            for (int id : dsIds) {
                EmfDataset ds = dsDao.getDataset(session, id);

                if (ds == null)
                    continue;

                Date modDate = ds.getModifiedDateTime();
                ExternalSource[] tempSrcs = dsDao.getExternalSrcs(id, -1, null, session);

                if (containsAllSrcs(tempSrcs, srcs)) {
                    dsWithAllSrcs.add(ds);

                    if (latest == null || (modDate != null && modDate.after(latest.getModifiedDateTime())))
                        latest = ds;
                }
            }

            if (dsWithAllSrcs.size() == 1)
                return dsWithAllSrcs.get(0);

            if (dsWithAllSrcs.size() > 1) {
                sb.append("There are multiple existing datasets could be loaded:" + lineSep);
                int count = 0;

                for (Iterator<EmfDataset> iter = dsWithAllSrcs.iterator(); iter.hasNext();) {
                    EmfDataset each = iter.next();
                    sb.append("\t" + (++count) + ": " + each.getName() + lineSep);
                }

                sb.append("Only dataset '" + latest.getName() + "' with the latest modified date was chosen.");

                return latest;
            }

            sb.append("For input '" + input.getName() + "': " + lineSep);

            return createNewDataset(input.getCaseID(), massDir, values, input.getDatasetType(), user, sb, lineSep);
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private void checkMassStorage(StringBuffer sb, String lineSep, User user, String[] values, String massdir,
            CaseInput input) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            EmfDataset dataset = null;
            int[] dsIds = null;

            if (massdir != null && !massdir.trim().isEmpty()) {
                String[] massSrcs = reconstructSources(values, massdir);
                dsIds = caseDao.getExternalDatasetIds(massSrcs[0], session);
            }

            if (dsIds != null && dsIds.length > 0)
                dataset = getUniqueDataset(sb, lineSep, user, massdir, values, input, dsIds);

            if (dataset == null) {
                sb.append("For input '" + input.getName() + "': " + lineSep);
                dataset = createNewDataset(input.getCaseID(), massdir, values, input.getDatasetType(), user, sb,
                        lineSep);
            }

            if (dataset == null)
                return;

            setInputDatasetValues(user, values, input, dataset);
        } catch (Exception e) {
            log.error("Error resetting input values.", e);
            throw new EmfException(e.getMessage() == null ? "Error resetting input values." : e.getClass().toString()
                    + ": " + e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private EmfDataset createNewDataset(int caseId, String massdir, String[] values, DatasetType type, User user,
            StringBuffer sb, String lineSep) throws Exception {
        if (type != null && !type.isExternal())
            return null;

        String[] origSrcs = reconstructSources(values, values[0]);
        boolean origExists = allExist(origSrcs);
        ExternalSource[] extSrcs = getExternalSrcs(origSrcs);

        if (origExists)
            return add2tables(caseId, values[1], extSrcs, type, user, false, values[0], massdir);

        boolean massExists = false;
        String[] massSrcs = new String[0];

        if (massdir != null && !massdir.trim().isEmpty()) {
            massSrcs = reconstructSources(values, massdir);
            massExists = allExist(massSrcs);
        }

        if (!massExists) {
            sb.append("  WARNING: Not all files (Ex. " + values[1]
                    + ") existed on either original source or mass storage place." + lineSep);
            sb.append("  Original source folder: " + values[0] + "." + lineSep);
            sb.append("  Mass storage source folder: " + massdir + "." + lineSep);

            return add2tables(caseId, values[1], extSrcs, type, user, false, values[0], massdir);
        }

        extSrcs = getExternalSrcs(massSrcs);

        return add2tables(caseId, values[1], extSrcs, type, user, true, values[0], massdir);
    }

    private synchronized EmfDataset add2tables(int caseId, String name, ExternalSource[] extSrcs, DatasetType type,
            User user, boolean moved, String preloc, String massloc) throws Exception {
        Session session = sessionFactory.getSession();

        if (type == null)
            type = dataDao.getDatasetType("External File (External)", session);

        if (dsDao.nameUsed(name, EmfDataset.class, session))
            name += "_caseID(" + caseId + ")_" + Math.abs(new Random().nextInt());

        Date date = new Date();
        EmfDataset external = new EmfDataset();
        String newName = name;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        external.setName(newName);
        
        external.setDatasetType(type);
        external.setCreator(user.getUsername());
        external.setCreatedDateTime(date);
        external.setAccessedDateTime(date);
        external.setModifiedDateTime(date);
        external.setStatus("Auto Imported");
        external.setDefaultVersion(0);

        if (moved && massloc != null && !massloc.trim().isEmpty()) {
            Keyword massLocKeyword = (Keyword) dataDao.load(Keyword.class, "MASS_STORAGE_LOCATION", session);
            Keyword prevLocKeyword = (Keyword) dataDao.load(Keyword.class, "PREVIOUS_LOCATION", session);
            KeyVal massKeyVal = new KeyVal(massLocKeyword, massloc);
            KeyVal prevLocKeyVal = new KeyVal(prevLocKeyword, preloc);
            external.addKeyVal(new KeyVal[] { massKeyVal, prevLocKeyVal });
        }

        dsDao.add(external, session);

        try {
            session.clear();
            EmfDataset ds = dsDao.getDataset(session, external.getName());

            for (int i = 0; i < extSrcs.length; i++)
                extSrcs[i].setDatasetId(ds.getId());

            dsDao.addExternalSources(extSrcs, session);

            Version version = new Version(0);
            version.setCreator(user);
            version.setName("Initial Version");
            version.setDatasetId(ds.getId());
            version.setFinalVersion(true);
            version.setLastModifiedDate(date);
            version.setPath("");
            dsDao.add(version, session);

            return ds;
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private ExternalSource[] getExternalSrcs(String[] srcs) {
        ExternalSource[] extSrcs = new ExternalSource[srcs.length];

        for (int i = 0; i < srcs.length; i++) {
            extSrcs[i] = new ExternalSource(srcs[i]);
            extSrcs[i].setListindex(i);
        }

        return extSrcs;
    }

    private synchronized boolean allExist(String[] srcs) {
        if (srcs == null || srcs.length == 0)
            return false;

        boolean allExisted = true;

        for (String src : srcs) {
            if (!new File(src).exists()) {
                allExisted = false;
                break;
            }
        }

        return allExisted;
    }

    private boolean containsAllSrcs(ExternalSource[] dsSrcs, String[] srcs) {
        List<String> temp = new ArrayList<String>();
        temp.addAll(Arrays.asList(srcs));
        int len = dsSrcs.length;

        for (int i = 0; i < len; i++) {
            int index = temp.indexOf(dsSrcs[i].getDatasource());

            if (index >= 0)
                temp.remove(index);
        }

        return temp.size() == 0;
    }

    private String[] reconstructSources(String[] values, String dir) {
        int len = values.length;
        String[] srcs = new String[len - 1]; // First be dir, the rest be file names

        if (dir.startsWith("/") || dir.endsWith("/"))
            dir += dir.endsWith("/") ? "" : "/";
        else
            dir += dir.endsWith("\\") ? "" : "\\";

        for (int i = 0; i < len - 1; i++)
            srcs[i] = dir + values[i + 1];

        return srcs;
    }

    private synchronized List<CaseParameter> getValidParameters(int jobId, int caseId) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            CaseJob job = caseDao.getCaseJob(jobId, session);
            Sector sector = (job == null) ? null : job.getSector();

            List<CaseParameter> jobSpecParams = caseDao.getCaseParametersByJobId(caseId, jobId, session);
            List<CaseParameter> sectorSpecParams = (sector != null) ? caseDao.getCaseParametersBySector(caseId, sector,
                    session) : caseDao.getCaseParametersForAllSectors(caseId, session);
            List<CaseParameter> params4AllSectorsAllJobs = caseDao.getCaseParametersForAllSectorsAllJobs(caseId,
                    session);

            return selectValidParameters(jobSpecParams, sectorSpecParams, params4AllSectorsAllJobs);
        } catch (Exception e) {
            log.error("Error reading case parameters for case id = " + caseId + " and job id = " + jobId + ".", e);
            throw new EmfException("Error reading case parameters for case id = " + caseId + " and job id = " + jobId
                    + ".");
        } finally {
            if (session != null && session.isOpen())
                session.close();
        }
    }

    private List<CaseParameter> selectValidParameters(List<CaseParameter> jobSpecParams,
            List<CaseParameter> sectorSpecParams, List<CaseParameter> params4AllSectorsAllJobs) {
        List<String> paramEnvs = getParamEnvVars(jobSpecParams);

        for (Iterator<CaseParameter> iter = sectorSpecParams.iterator(); iter.hasNext();) {
            CaseParameter param = iter.next();
            ParameterEnvVar envVar = param.getEnvVar();
            int paramJobId = param.getJobId();

            if (!paramEnvs.contains(envVar.getName()) && paramJobId == 0) {
                jobSpecParams.add(param);
                paramEnvs.add(envVar.getName());
            }
        }

        for (Iterator<CaseParameter> iter = params4AllSectorsAllJobs.iterator(); iter.hasNext();) {
            CaseParameter param = iter.next();
            ParameterEnvVar envVar = param.getEnvVar();

            if (!paramEnvs.contains(envVar.getName())) {
                jobSpecParams.add(param);
                paramEnvs.add(envVar.getName());
            }
        }

        return jobSpecParams;
    }

    private synchronized List<String> getParamEnvVars(List<CaseParameter> paramObjects) {
        List<String> envs = new ArrayList<String>();

        for (Iterator<CaseParameter> iter = paramObjects.iterator(); iter.hasNext();) {
            ParameterEnvVar env = iter.next().getEnvVar();

            if (env != null && env.getName() != null && !env.getName().trim().isEmpty())
                envs.add(env.getName());
        }

        return envs;
    }

    public synchronized AirQualityModel addAirQualityModel(AirQualityModel airQModel) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            AirQualityModel temp = (AirQualityModel) caseDao.load(AirQualityModel.class, airQModel.getName(), session);

            if (temp != null)
                return temp;
            caseDao.add(airQModel, session);
            return (AirQualityModel) caseDao.load(AirQualityModel.class, airQModel.getName(), session);
        } catch (Exception e) {
            log.error("Could not add new AirQualityModel '" + airQModel.getName() + "'\n", e);
            throw new EmfException("Could not add new AirQualityModel '" + airQModel.getName() + "'");
        } finally {
            session.close();
        }
    }
    
    public synchronized EmissionsYear addEmissionsYear(EmissionsYear emisyr) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            EmissionsYear temp = (EmissionsYear) caseDao.load(EmissionsYear.class, emisyr.getName(), session);

            if (temp != null)
                return temp;
            caseDao.add(emisyr, session);
            return (EmissionsYear) caseDao.load(EmissionsYear.class, emisyr.getName(), session);
        } catch (Exception e) {
            log.error("Could not add new EmissionsYear '" + emisyr.getName() + "'\n", e);
            throw new EmfException("Could not add new EmissionsYear '" + emisyr.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized GeoRegion addGrid(GeoRegion grid) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            GeoRegion temp = (GeoRegion) caseDao.load(GeoRegion.class, grid.getName(), session);

            if (temp != null)
                return temp;

            caseDao.add(grid, session);
            return (GeoRegion) caseDao.load(GeoRegion.class, grid.getName(), session);
        } catch (Exception e) {
            log.error("Could not add new Grid '" + grid.getName() + "'\n", e);
            throw new EmfException("Could not add new Grid '" + grid.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized ModelToRun addModelToRun(ModelToRun model) throws EmfException {
        Session session = sessionFactory.getSession();
        
        if (DebugLevels.DEBUG_20())
            System.out.println("Before insert ModelToRun: " + model.getName());
        
        try {
            if (DebugLevels.DEBUG_20())
                System.out.println("ModelToRun: " + model.getName() + " existed? " + (caseDao.load(ModelToRun.class, model.getName(), session) == null));
            
            ModelToRun temp = caseDao.loadModelTorun(model.getName(), session);
            
            if (DebugLevels.DEBUG_20())
                System.out.println("Does " + model.getName() + " have a similar one in DB? " + (temp != null));

            if (temp != null)
                return temp;

            caseDao.add(model, session);

            if (DebugLevels.DEBUG_20())
                System.out.println(model.getName() + " has been added into DB.");

            return (ModelToRun) caseDao.load(ModelToRun.class, model.getName(), session);
        } catch (Exception e) {
            log.error("Could not add new model to run '" + model.getName() + "'\n", e);
            throw new EmfException("Could not add new model to run '" + model.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized Speciation addSpeciation(Speciation speciation) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Speciation temp = (Speciation) caseDao.load(Speciation.class, speciation.getName(), session);

            if (temp != null)
                return temp;

            caseDao.add(speciation, session);
            return (Speciation) caseDao.load(Speciation.class, speciation.getName(), session);
        } catch (Exception e) {
            log.error("Could not add new speciation '" + speciation.getName() + "'\n", e);
            throw new EmfException("Could not add new speciation '" + speciation.getName() + "'");
        } finally {
            session.close();
        }
    }

    public synchronized void updateCaseInput(User user, CaseInput input) throws EmfException {
        Session localSession = sessionFactory.getSession();

        try {
            CaseInput loaded = (CaseInput) caseDao.loadCaseInput(input, localSession);

            if (loaded != null && loaded.getId() != input.getId())
                throw new EmfException("Case input uniqueness check failed (" + loaded.getId() + "," + input.getId()
                        + ")");

            // Clear the cached information. To update a case
            localSession.clear();
            caseDao.updateCaseInput(input, localSession);
        } catch (RuntimeException e) {
            log.error("Could not update case input: " + input.getName() + ".\n", e);
            throw new EmfException("Could not update case input: " + input.getName() + ".");
        } finally {
            localSession.close();
        }
    }

    public synchronized void updateCaseParameter(User user, CaseParameter parameter) throws EmfException {
        Session localSession = sessionFactory.getSession();

        try {
            CaseParameter loaded = caseDao.loadCaseParameter(parameter, localSession);

            if (loaded != null && loaded.getId() != parameter.getId())
                throw new EmfException("Case parameter uniqueness check failed (" + loaded.getId() + ","
                        + parameter.getId() + ")");

            localSession.clear();
            caseDao.updateCaseParameter(parameter, localSession);
        } catch (RuntimeException e) {
            log.error("Could not update case parameter: " + parameter.getName() + ".\n", e);
            throw new EmfException("Could not update case parameter: " + parameter.getName() + ".");
        } finally {
            localSession.close();
        }
    }
    
}
