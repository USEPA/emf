package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
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
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class CaseDAO {

    private HibernateFacade hibernateFacade;

    private LockingScheme lockingScheme;

    private HibernateSessionFactory sessionFactory;

    private final Sector ALL_SECTORS = null;
    
    private final GeoRegion ALL_REGIONS = null;

    private final int ALL_JOB_ID = 0;


    public CaseDAO(HibernateSessionFactory sessionFactory) {
        super();
        this.sessionFactory = sessionFactory;
        daoInit();
    }

    public CaseDAO() {
        daoInit();
    }

    private void daoInit() {
        hibernateFacade = new HibernateFacade();
        lockingScheme = new LockingScheme();
    }

    public void add(JobMessage message) {
        Session session = sessionFactory.getSession();
        try {
            hibernateFacade.add(message, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }
    }

    public CaseOutput add(CaseOutput output) throws Exception {
        Session session = sessionFactory.getSession();

        try {
            hibernateFacade.add(output, session);
            return getCaseOutput(output, session);
        } catch (Exception ex) {
            throw new Exception("Problem adding case output: " + output.getName() + ". " + ex.getMessage());
        } finally {
            session.close();
        }
    }

    public void add(Object obj) throws Exception {
        Session session = sessionFactory.getSession();

        try {
            hibernateFacade.add(obj, session);
        } catch (Exception ex) {
            throw new Exception("Problem adding object: " + obj.toString() + ". " + ex.getMessage());
        } finally {
            session.close();
        }
    }

    public CaseOutput updateCaseOutput(CaseOutput output) {
        Session session = sessionFactory.getSession();
        CaseOutput toReturn = null;

        try {
            hibernateFacade.updateOnly(output, session);
            toReturn = getCaseOutput(output, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return toReturn;
    }

    public CaseOutput updateCaseOutput(Session session, CaseOutput output) {
        hibernateFacade.updateOnly(output, session);
        return getCaseOutput(output, session);
    }

    public boolean caseOutputNameUsed(String outputName) {
        Session session = sessionFactory.getSession();
        List<CaseOutput> outputs = null;

        try {
            CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseOutput.class, session);
            outputs = hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("name"), outputName));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return (outputs != null && outputs.size() > 0);
    }

    public void add(Executable exe, Session session) {
        addObject(exe, session);
    }

    public void add(SubDir subdir, Session session) {
        addObject(subdir, session);
    }

    public void add(AirQualityModel object, Session session) {
        addObject(object, session);
    }

    public void add(CaseCategory object, Session session) {
        addObject(object, session);
    }

    public void add(EmissionsYear object, Session session) {
        addObject(object, session);
    }

    public void add(GeoRegion object, Session session) {
        addObject(object, session);
    }

    public void add(MeteorlogicalYear object, Session session) {
        addObject(object, session);
    }

    public void add(Speciation object, Session session) {
        addObject(object, session);
    }

    public void add(CaseProgram object, Session session) {
        addObject(object, session);
    }

    public void add(InputName object, Session session) {
        addObject(object, session);
    }

    public void add(InputEnvtVar object, Session session) {
        addObject(object, session);
    }

    public void add(ModelToRun object, Session session) {
        addObject(object, session);
    }

    public void add(Case object, Session session) {
        addObject(object, session);
    }

    public void add(CaseInput object, Session session) {
        addObject(object, session);
    }

    public void add(CaseOutput object, Session session) {
        addObject(object, session);
    }

    public void add(Host object, Session session) {
        addObject(object, session);
    }

    public void addObject(Object obj, Session session) {
        hibernateFacade.add(obj, session);
    }

    public List<Abbreviation> getAbbreviations(Session session) {
        CriteriaBuilderQueryRoot<Abbreviation> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Abbreviation.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Abbreviation> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public Abbreviation getAbbreviation(Abbreviation abbr, Session session) {
        return hibernateFacade.load(Abbreviation.class, "name", abbr.getName(), session);
    }

    public List<AirQualityModel> getAirQualityModels(Session session) {
        CriteriaBuilderQueryRoot<AirQualityModel> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(AirQualityModel.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<AirQualityModel> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List<CaseCategory> getCaseCategories(Session session) {
        CriteriaBuilderQueryRoot<CaseCategory> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseCategory.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseCategory> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public CaseCategory getCaseCategory(String name, Session session) {
        return hibernateFacade.load(CaseCategory.class, "name", name, session);
    }

    public List<EmissionsYear> getEmissionsYears(Session session) {
        CriteriaBuilderQueryRoot<EmissionsYear> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(EmissionsYear.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<EmissionsYear> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List<MeteorlogicalYear> getMeteorlogicalYears(Session session) {
        CriteriaBuilderQueryRoot<MeteorlogicalYear> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(MeteorlogicalYear.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<MeteorlogicalYear> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List<Speciation> getSpeciations(Session session) {
        CriteriaBuilderQueryRoot<Speciation> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Speciation.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Speciation> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List<Case> getCases(Session session) {
        CriteriaBuilderQueryRoot<Case> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Case.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Case> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public Case getCase(int caseId, Session session) {
        CriteriaBuilderQueryRoot<Case> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Case.class, session);

        List<Case> caseObj = hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("id"), Integer.valueOf(caseId)));

        if (caseObj == null || caseObj.size() == 0)
            return null;

        return caseObj.get(0);
    }

    public Case getCaseFromAbbr(Abbreviation abbr, Session session) {
        return hibernateFacade.load(Case.class, "abbreviation", abbr, session);
    }

    public Case getCaseFromName(String name, Session session) {
        // Get a case from it's name
        return hibernateFacade.load(Case.class, "name", name, session);
    }

    public List<CaseProgram> getPrograms(Session session) {
        CriteriaBuilderQueryRoot<CaseProgram> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseProgram.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseProgram> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List<InputName> getInputNames(Session session) {
        CriteriaBuilderQueryRoot<InputName> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(InputName.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<InputName> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List<InputEnvtVar> getInputEnvtVars(Session session) {
        CriteriaBuilderQueryRoot<InputEnvtVar> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(InputEnvtVar.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<InputEnvtVar> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public void removeObject(Object object, Session session) {
        hibernateFacade.remove(object, session);
    }

    public void removeObjects(Object[] objects, Session session) {
        hibernateFacade.remove(objects, session);
    }

    public void remove(Case element, Session session) {
        hibernateFacade.remove(element, session);
    }

    public void removeCaseInputs(CaseInput[] inputs, Session session) {
        hibernateFacade.remove(inputs, session);
    }

    public void removeCaseOutputs(User user, CaseOutput[] outputs, boolean deleteDataset, Session session)
            throws EmfException {
        try {
            if (deleteDataset)
                removeDatasetsOnOutput(user, session, outputs);
        } finally {
            hibernateFacade.remove(outputs, session);
        }
    }

    public void removeCaseOutputs(CaseOutput[] outputs, Session session) {
        hibernateFacade.removeObjects(outputs, session);
    }

    public Case obtainLocked(User owner, Case element, Session session) {
        return (Case) lockingScheme.getLocked(owner, current(element, session), session);
    }

    public Case releaseLocked(User owner, Case locked, Session session) {
        return (Case) lockingScheme.releaseLock(owner, current(locked, session), session);
    }

    public Case forceReleaseLocked(Case locked, Session session) {
        return (Case) lockingScheme.releaseLock(current(locked, session), session);
    }

    public Case update(Case locked, Session session) throws EmfException {
        return (Case) lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
    }

    private Case current(Case caze, Session session) {
        return hibernateFacade.current(caze.getId(), Case.class, session);
    }

    private Case current(int id, Session session) {
        return hibernateFacade.current(id, Case.class, session);
    }

    public boolean caseInputExists(CaseInput input, Session session) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, session);

        Predicate[] predicates = uniqueCaseInputCriteria(input.getCaseID(), input, criteriaBuilderQueryRoot);

        return hibernateFacade.exists(criteriaBuilderQueryRoot, predicates, session);
    }

//    public boolean exists(int id, Class<?> clazz, Session session) {
//        return hibernateFacade.exists(id, clazz, session);
//    }

    private Predicate[] uniqueCaseInputCriteria(int caseId, CaseInput input, CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot) {
        InputName inputname = input.getInputName();
        Sector sector = input.getSector();
        GeoRegion region = input.getRegion();
        CaseProgram program = input.getProgram();
        Integer jobID = Integer.valueOf(input.getCaseJobID());

        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();
        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = (inputname == null) ? builder.isNull(root.get("inputName")) : builder.equal(root.get("inputName"), inputname);
        Predicate c3 = (region == null) ? builder.isNull(root.get("region")) : builder.equal(root.get("region"), region);
        Predicate c4 = (sector == null) ? builder.isNull(root.get("sector")) : builder.equal(root.get("sector"), sector);
        Predicate c5 = (program == null) ? builder.isNull(root.get("program")) : builder.equal(root.get("program"), program);
        Predicate c6 = builder.equal(root.get("caseJobID"), jobID);

        return new Predicate[] { c1, c2, c3, c4, c5, c6 };
    }
    
    public boolean caseJobExists(CaseJob job, Session session) {
        CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, session);

        Predicate[] criterions = uniqueCaseJobCriteria(job.getCaseId(), job, criteriaBuilderQueryRoot);

        return hibernateFacade.exists(criteriaBuilderQueryRoot, criterions, session);
    }

    private Predicate[] uniqueCaseJobCriteria(int caseId, CaseJob job, CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot) {
        String jobname = job.getName();
        Sector sector = job.getSector();
        GeoRegion region = job.getRegion();

        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseJob> root = criteriaBuilderQueryRoot.getRoot();
        Predicate c1 = builder.equal(root.get("caseId"), Integer.valueOf(caseId));
        Predicate c2 = (jobname == null) ? builder.isNull(root.get("name")) : builder.equal(root.get("name"), jobname);
        Predicate c3 = (region == null) ? builder.isNull(root.get("region")) : builder.equal(root.get("region"), region);
        Predicate c4 = (sector == null) ? builder.isNull(root.get("sector")) : builder.equal(root.get("sector"), sector);

        return new Predicate[] { c1, c2, c3, c4 };
    }

    public Object load(Class<?> clazz, String name, Session session) {
        return hibernateFacade.load(clazz, "name", name, session);
    }
    
    public <C> CriteriaBuilderQueryRoot<C> getCriteriaBuilderQueryRoot(Class<C> persistentClass, Session session) {
        return hibernateFacade.getCriteriaBuilderQueryRoot(persistentClass, session);
    }

    public ModelToRun loadModelTorun(String name, Session session) {
        String query = " FROM " + ModelToRun.class.getSimpleName() + " as obj WHERE lower(obj.name)='" + name.toLowerCase()+ "'";
        List<?> mods = session.createQuery(query).list();
        
        if (mods == null || mods.size() == 0)
            return null;
        
        return (ModelToRun) mods.get(0);
    }

    public <C> C load(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate[] predicates, Session session) {
        return hibernateFacade.load(session, criteriaBuilderQueryRoot, predicates);
    }

    public <C> C load(Class<C> clazz, int id, Session session) {
        return hibernateFacade.load(clazz, "id", Integer.valueOf(id), session);
    }

    public ParameterEnvVar loadParamEnvVar(ParameterEnvVar envVar, Session session) {
        CriteriaBuilderQueryRoot<ParameterEnvVar> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterEnvVar.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ParameterEnvVar> root = criteriaBuilderQueryRoot.getRoot();
        
        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(envVar.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), envVar.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, session);
    }

    public CaseProgram loadCaseProgram(CaseProgram prog, Session session) {
        CriteriaBuilderQueryRoot<CaseProgram> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseProgram.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseProgram> root = criteriaBuilderQueryRoot.getRoot();

        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(prog.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), prog.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, session);
    }

    public SubDir loadCaseSubdir(SubDir subdir, Session session) {
        CriteriaBuilderQueryRoot<SubDir> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(SubDir.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<SubDir> root = criteriaBuilderQueryRoot.getRoot();

        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(subdir.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), subdir.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, session);
    }

    public Object loadParameterName(ParameterName paramName, Session session) {
        CriteriaBuilderQueryRoot<ParameterName> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterName.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ParameterName> root = criteriaBuilderQueryRoot.getRoot();

        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(paramName.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), paramName.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, session);
    }

    public Object loadParameterEnvVar(ParameterEnvVar envVar, Session session) {
        CriteriaBuilderQueryRoot<ParameterEnvVar> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterEnvVar.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ParameterEnvVar> root = criteriaBuilderQueryRoot.getRoot();

        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(envVar.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), envVar.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, session);
    }

    public InputName loadInputName(InputName inputName, Session session) {
        CriteriaBuilderQueryRoot<InputName> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(InputName.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<InputName> root = criteriaBuilderQueryRoot.getRoot();

        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(inputName.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), inputName.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, session);
    }

    public InputEnvtVar loadInputEnvtVar(InputEnvtVar envVar, Session session) {
        CriteriaBuilderQueryRoot<InputEnvtVar> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(InputEnvtVar.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<InputEnvtVar> root = criteriaBuilderQueryRoot.getRoot();

        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(envVar.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), envVar.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, session);
    }

    public Object loadCaseInput(CaseInput input, Session session) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, session);

        Predicate[] predicates = uniqueCaseInputCriteria(input.getCaseID(), input, criteriaBuilderQueryRoot);

        return hibernateFacade.load(session, criteriaBuilderQueryRoot, predicates);
    }

    public CaseInput loadCaseInput(int caseId, CaseInput input, Session session) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, session);

        Predicate[] predicates = uniqueCaseInputCriteria(caseId, input, criteriaBuilderQueryRoot);

        return hibernateFacade.load(session, criteriaBuilderQueryRoot, predicates);
    }

    public List<CaseInput> getCaseInputs(int caseId, Session session) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, session);
        return hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseID"), Integer.valueOf(caseId)));
    }

    public List<CaseInput> getJobSpecNonSpecCaseInputs(int caseId, int[] jobIds, Session session) {
        List<?> ids = session
                .createQuery(
                        "SELECT obj.id from " + CaseInput.class.getSimpleName() + " as obj WHERE obj.caseID = "
                                + caseId + " AND (obj.caseJobID = 0 OR obj.caseJobID = "
                                + getAndOrClause(jobIds, "obj.caseJobID") + ")").list();
        List<CaseInput> inputs = new ArrayList<CaseInput>();

        for (Iterator<?> iter = ids.iterator(); iter.hasNext();) {
            Integer id = (Integer) iter.next();
            inputs.add(this.getCaseInput(id, session));
        }

        return inputs;
    }

    public List<CaseInput> getCaseInputsByJobIds(int caseId, int[] jobIds, Session session) {
        List<?> ids = session.createQuery(
                "SELECT obj.id from " + CaseInput.class.getSimpleName() + " as obj WHERE obj.caseID = " + caseId
                        + " AND (obj.caseJobID = " + getAndOrClause(jobIds, "obj.caseJobID") + ")").list();
        List<CaseInput> inputs = new ArrayList<CaseInput>();

        for (Iterator<?> iter = ids.iterator(); iter.hasNext();) {
            Integer id = (Integer) iter.next();
            inputs.add(this.getCaseInput(id, session));
        }

        return inputs;
    }

    public List<CaseInput> getCaseInputs(int pageSize, int caseId, Sector sector, String envNameContains, boolean showAll, Session session) {
        if (sector == null)
            return filterInputs(getAllInputs(pageSize, caseId, showAll, session), envNameContains);

        String sectorName = sector.getName().toUpperCase();

        if (sectorName.equals("ALL"))
            return filterInputs(getCaseInputsWithLocal(showAll, caseId, session), envNameContains);

        if (sectorName.equals("ALL SECTORS"))
            return filterInputs(getCaseInputsWithNullSector(showAll, caseId, session), envNameContains);

        return filterInputs(getCaseInputsWithSector(showAll, sector, caseId, session), envNameContains);
    }

    private List<CaseInput> getAllInputs(int pageSize, int caseId, boolean showAll, Session session) {
        List<CaseInput> inputs = getCaseInputsWithLocal(showAll, caseId, session);

        if (inputs.size() < pageSize)
            return inputs;

        return inputs.subList(0, pageSize);
    }

    private List<CaseInput> getCaseInputsWithLocal(boolean showAll, int caseId, Session session) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("local"), true);
        Predicate[] crits = (showAll) ? new Predicate[] { crit1 } : new Predicate[] { crit1, crit2 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, session);
    }
    
    private List<CaseInput> filterInputs(List<CaseInput> allInputs, String envNameContains) {
        if (envNameContains == null || envNameContains.trim().isEmpty())
            return allInputs;
        
        List<CaseInput> filtered = new ArrayList<CaseInput>();
        
        for (CaseInput input : allInputs) {
            InputEnvtVar envVar = input.getEnvtVars();
            
            if (envVar != null && envVar.getName().toUpperCase().contains(envNameContains.toUpperCase()))
                filtered.add(input);
        }
        
        return filtered;
    }

    private List<CaseInput> getCaseInputsWithNullSector(boolean showAll, int caseId, Session session) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.isNull(root.get("sector"));
        Predicate crit3 = builder.equal(root.get("local"), true);
        Predicate[] crits = (showAll) ? new Predicate[] { crit1, crit2 } : new Predicate[] { crit1, crit2, crit3 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, session);
    }

    public List<Case> getCasesThatInputToOtherCases(int caseId, Session session) {
        String sql = "select new gov.epa.emissions.framework.services.casemanagement.Case(cs.id, cs.name) from Case cs where cs.id in (select distinct cO.caseId from CaseInput as cI, CaseOutput as cO where cI.dataset.id = cO.datasetId and cI.caseID = :caseId)";
        Query query = session.createQuery(sql).setInteger("caseId", caseId);
        return query.list();
    }

    public List<Case> getCasesThatOutputToOtherCases(int caseId, Session session) {
        String sql = "select new gov.epa.emissions.framework.services.casemanagement.Case(cs.id, cs.name) from Case cs where cs.id in (select distinct cI.caseID from CaseOutput as cO, CaseInput as cI where cI.dataset.id = cO.datasetId and cO.caseId = :caseId)";
        Query query = session.createQuery(sql).setInteger("caseId", caseId);
        return query.list();
    }

    public List<Case> getCasesByOutputDatasets(int[] datasetIds, Session session) {
        String idList = "";
        for (int id : datasetIds)
            idList += (idList.length() > 0 ? "," : "") + id;
        String sql = "select new gov.epa.emissions.framework.services.casemanagement.Case(cs.id, cs.name) from Case cs where cs.id in (select distinct cO.caseId from CaseOutput as cO where cO.datasetId in ("
                + idList + "))";
        Query query = session.createQuery(sql);
        return query.list();
    }

    public List<Case> getCasesByInputDataset(int datasetId, Session session) {
        String sql = "select new gov.epa.emissions.framework.services.casemanagement.Case(cs.id, cs.name) from Case cs where cs.id in (select distinct cI.caseID from CaseInput as cI where cI.dataset.id = :datasetId)";
        Query query = session.createQuery(sql).setInteger("datasetId", datasetId);
        return query.list();
    }

    private List<CaseInput> getCaseInputsWithSector(boolean showAll, Sector sector, int caseId, Session session) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("sector"), sector);
        Predicate crit3 = builder.equal(root.get("local"), true);
        Predicate[] crits = (showAll) ? new Predicate[] { crit1, crit2 } : new Predicate[] { crit1, crit2, crit3 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, session);
    }

    public List<CaseInput> getInputsBySector(int caseId, Sector sector, Session session) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = builder.equal(root.get("sector"), sector);

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, session);
    }

    public List<CaseInput> getInputsForAllSectors(int caseId, Session session) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = builder.isNull(root.get("sector"));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, session);
    }

    public List<CaseInput> getInputs4AllJobsAllSectors(int caseId, Session session) {
        return getJobInputs(caseId, 0, null, session);
    }
    
    public List<CaseInput> getJobInputs(int caseId, int jobId, Sector sector, Session session) {
        /**
         * Gets inputs for a job. Selects on the following 3 criteria: caseId, jobId, sectorId
         */
        Integer caseID = Integer.valueOf(caseId);
        Integer jobID = Integer.valueOf(jobId);

        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        // setup the 3 criteria
        Predicate c1 = builder.equal(root.get("caseID"), caseID);
        Predicate c2 = (sector == null) ? builder.isNull(root.get("sector")) : builder.equal(root.get("sector"), sector);
        Predicate c3 = builder.equal(root.get("caseJobID"), jobID);
        Predicate[] criterions = { c1, c2, c3 };

        // query the db using hibernate for the inputs that
        // match the criterias
        // what is the difference b/w hibernate get and getAll
        return hibernateFacade.get(criteriaBuilderQueryRoot, criterions, session);

    }

    public List<CaseInput> getJobInputs(int caseId, int jobId, Sector sector, GeoRegion region, Session session) {
        /**
         * Gets inputs for a job. Selects on the following 3 criteria: caseId, jobId, sectorId
         */
        Integer caseID = Integer.valueOf(caseId);
        Integer jobID = Integer.valueOf(jobId);

        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        // setup the 3 criteria
        Predicate c1 = builder.equal(root.get("caseID"), caseID);
        Predicate c2 = (sector == null) ? builder.isNull(root.get("sector")) : builder.equal(root.get("sector"), sector);
        Predicate c3 = builder.equal(root.get("caseJobID"), jobID);
        Predicate c4 = (region == null) ? builder.isNull(root.get("region")) : builder.equal(root.get("region"), region);
        Predicate[] criterions = { c1, c2, c3, c4 };

        // query the db using hibernate for the inputs that
        // match the criterias
        // what is the difference b/w hibernate get and getAll
        return hibernateFacade.get(criteriaBuilderQueryRoot, criterions, session);

    }

    public List<CaseParameter> getJobParameters(int caseId, int jobId, Sector sector, Session session) {
        /**
         * Gets parameters for a job. Selects on the following 3 criteria: caseId, jobId, sectorId
         */
        Integer caseID = Integer.valueOf(caseId);
        Integer jobID = Integer.valueOf(jobId);

        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        // setup the 3 criteria
        Predicate c1 = builder.equal(root.get("caseID"), caseID);
        Predicate c2 = (sector == null) ? builder.isNull(root.get("sector")) : builder.equal(root.get("sector"), sector);
        Predicate c3 = builder.equal(root.get("jobId"), jobID);
        Predicate[] criterions = { c1, c2, c3 };

        // query the db using hibernate for the parameters that
        // match the criterias
        return hibernateFacade.get(criteriaBuilderQueryRoot, criterions, session);

    }
    
    public List<CaseParameter> getJobParameters(int caseId, int jobId, Sector sector, GeoRegion region, Session session) {
        /**
         * Gets parameters for a job. Selects on the following 3 criteria: caseId, jobId, sectorId
         */
        Integer caseID = Integer.valueOf(caseId);
        Integer jobID = Integer.valueOf(jobId);

        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        // setup the 3 criteria
        Predicate c1 = builder.equal(root.get("caseID"), caseID);
        Predicate c2 = (sector == null) ? builder.isNull(root.get("sector")) : builder.equal(root.get("sector"), sector);
        Predicate c3 = builder.equal(root.get("jobId"), jobID);
        Predicate c4 = (region == null) ? builder.isNull(root.get("region")) : builder.equal(root.get("region"), region);
        Predicate[] criterions = { c1, c2, c3, c4 };

        // query the db using hibernate for the parameters that
        // match the criterias
        return hibernateFacade.get(criteriaBuilderQueryRoot, criterions, session);
    }

    private List<CaseParameter> filterParameters(List<CaseParameter> allParams, String envNameContains) {
        if (envNameContains == null || envNameContains.trim().isEmpty())
            return allParams;
        
        List<CaseParameter> filtered = new ArrayList<CaseParameter>();
        
        for (CaseParameter param : allParams) {
            ParameterEnvVar envVar = param.getEnvVar();
            
            if (envVar != null && envVar.getName().toUpperCase().contains(envNameContains.toUpperCase()))
                filtered.add(param);
        }
        
        return filtered;
    }
    
    public List<?> getAllCaseInputs(Session session) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("id")), session);
    }

    public void updateCaseInput(CaseInput input, Session session) {
        hibernateFacade.updateOnly(input, session);
    }

    public List<ModelToRun> getModelToRuns(Session session) {
        CriteriaBuilderQueryRoot<ModelToRun> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ModelToRun.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ModelToRun> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List<SubDir> getSubDirs(Session session) {
        CriteriaBuilderQueryRoot<SubDir> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(SubDir.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<SubDir> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public void add(CaseJob job, Session session) {
        addObject(job, session);
    }

    public List<CaseJob> getCaseJobs(int caseId, Session session) {
        CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, session);
        return hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseId"), Integer.valueOf(caseId)));
    }

    public List<CaseJob> getCaseJobs(int caseId) {
        Session session = sessionFactory.getSession();
        List<CaseJob> jobs = null;

        try {
            CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, session);
            jobs = hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseId"), Integer.valueOf(caseId)));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return jobs;
    }

    public CaseJob getCaseJob(String jobKey) {
        Session session = sessionFactory.getSession();
        CaseJob job = null;

        try {
            job = hibernateFacade.load(CaseJob.class, "jobkey", jobKey, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return job;
    }

    public List<JobMessage> getJobMessages(int caseId, Session session) {
        CriteriaBuilderQueryRoot<JobMessage> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(JobMessage.class, session);
        return hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseId"), Integer.valueOf(caseId)));
    }

    public List<JobMessage> getJobMessages(int caseId, int jobId, Session session) {
        CriteriaBuilderQueryRoot<JobMessage> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(JobMessage.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<JobMessage> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseId"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("jobId"), Integer.valueOf(jobId));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { crit1, crit2 }, session);
    }

    public CaseJob getCaseJob(int jobId, Session session) {
        if (jobId == 0)      //NOTE: to save a db access
            return null;
        
        return hibernateFacade.load(CaseJob.class, "id", Integer.valueOf(jobId), session);
    }

    public List<Sector> getSectorsUsedbyJobs(int caseId, Session session) {
        CriteriaBuilderQueryRoot<Sector> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Sector.class, session);
        return hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseId"), Integer.valueOf(caseId)));
    }

    public CaseJob getCaseJob(int jobId) {
        if (jobId == 0)
            return null;
        
        CaseJob caseJob = null;
        Session session = sessionFactory.getSession();
        try {
            caseJob = hibernateFacade.load(CaseJob.class, "id", Integer.valueOf(jobId), session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }
        return caseJob;
    }

    public CaseJob getCaseJob(int caseId, CaseJob job, Session session) {
        CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, session);

        Predicate[] crits = uniqueCaseJobCriteria(caseId, job, criteriaBuilderQueryRoot);

        return hibernateFacade.load(session, criteriaBuilderQueryRoot, crits);
    }

    public void updateCaseJob(CaseJob job) {
        Session session = sessionFactory.getSession();
        try {
            hibernateFacade.updateOnly(job, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }
    }

    public void updateCaseJob(CaseJob job, Session session) throws Exception {
        try {
            hibernateFacade.updateOnly(job, session);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        }
    }

    public List<CaseJobKey> getCaseJobKey(int jobId, Session session) {
        CriteriaBuilderQueryRoot<CaseJobKey> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJobKey.class, session);
        return hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("jobId"), Integer.valueOf(jobId)));
    }

    public List<CasesSens> getCasesSens(int parentId, Session session) {
        CriteriaBuilderQueryRoot<CasesSens> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CasesSens.class, session);
        return hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("parentId"), Integer.valueOf(parentId)));
    }

    public CaseJob getCaseJobFromKey(String key) {
        Session session = sessionFactory.getSession();
        CaseJob job = null;

        try {
            CriteriaBuilderQueryRoot<CaseJobKey> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJobKey.class, session);
            List<CaseJobKey> keyObjs = hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("key"), key));

            if (keyObjs == null || keyObjs.size() == 0)
                return null;

            job = getCaseJob(keyObjs.get(0).getJobId(), session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return job;
    }

    public void updateCaseJobKey(int jobId, String jobKey, Session session) throws Exception {
        try {
            List<?> keys = getCaseJobKey(jobId, session);
            CaseJobKey keyObj = (keys == null || keys.size() == 0) ? null : (CaseJobKey) keys.get(0);

            if (keyObj == null) {
                addObject(new CaseJobKey(jobKey, jobId), session);
                return;
            }

            if (keyObj.getKey().equals(jobKey))
                return;

            keyObj.setKey(jobKey);
            hibernateFacade.updateOnly(keyObj, session);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        }
    }

    public List<JobRunStatus> getJobRunStatuses(Session session) {
        CriteriaBuilderQueryRoot<JobRunStatus> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(JobRunStatus.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<JobRunStatus> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public JobRunStatus getJobRunStatuse(String status, Session session) {
        return hibernateFacade.load(JobRunStatus.class, "name", status, session);
    }

    public JobRunStatus getJobRunStatuse(String status) {
        if (DebugLevels.DEBUG_9())
            System.out
                    .println("In CaseDAO::getJobRunStatuse: Is the session Factory null? " + (sessionFactory == null));

        Session session = sessionFactory.getSession();
        JobRunStatus jrs = null;

        try {
            jrs = hibernateFacade.load(JobRunStatus.class, "name", status, session);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return jrs;

    }

    public List<Host> getHosts(Session session) {
        CriteriaBuilderQueryRoot<Host> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Host.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Host> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List<Executable> getExecutables(Session session) {
        CriteriaBuilderQueryRoot<Executable> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Executable.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Executable> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public boolean exeutableExists(Session session, Executable exe) {
        return hibernateFacade.exists(exe.getName(), Executable.class, session);
    }

    public void add(Abbreviation element, Session session) {
        addObject(element, session);
    }

    public void add(ParameterEnvVar envVar, Session session) {
        addObject(envVar, session);
    }

    public List<ParameterEnvVar> getParameterEnvVars(Session session) {
        CriteriaBuilderQueryRoot<ParameterEnvVar> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterEnvVar.class, session);
        return hibernateFacade.getAll(criteriaBuilderQueryRoot, session);
    }

    public void addValueType(ValueType type, Session session) {
        addObject(type, session);
    }

    public List<ValueType> getValueTypes(Session session) {
        CriteriaBuilderQueryRoot<ValueType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ValueType.class, session);
        return hibernateFacade.getAll(criteriaBuilderQueryRoot, session);
    }

    public void addParameterName(ParameterName name, Session session) {
        addObject(name, session);
    }

    public List<ParameterName> getParameterNames(Session session) {
        CriteriaBuilderQueryRoot<ParameterName> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterName.class, session);
        return hibernateFacade.getAll(criteriaBuilderQueryRoot, session);
    }

    public void addParameter(CaseParameter param, Session session) {
        addObject(param, session);
    }

    public CaseParameter loadCaseParameter(CaseParameter param, Session session) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);

        Predicate[] criterions = uniqueCaseParameterCriteria(param.getCaseID(), param, criteriaBuilderQueryRoot);

        return hibernateFacade.load(session, criteriaBuilderQueryRoot, criterions);
    }

    public CaseParameter loadCaseParameter4Sensitivity(int caseId, CaseParameter param, Session session) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);

        Predicate[] criterions = sensitivityCaseParameterCriteria(caseId, param, session, criteriaBuilderQueryRoot);

        return hibernateFacade.load(session, criteriaBuilderQueryRoot, criterions);
    }

    // NOTE: this method is soly for creating sensitivity case. The questions without clear answers include the
    // following:
    // What if the job does exist in the parent case, but the parameter to copy has a different job?
    private Predicate[] sensitivityCaseParameterCriteria(int caseId, CaseParameter param, Session session, CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot) {
        ParameterName paramname = param.getParameterName();
        Sector sector = param.getSector();
        CaseProgram program = param.getProgram();
        Integer jobID = Integer.valueOf(param.getJobId());

        CaseJob job = this.getCaseJob(jobID, session);
        CaseJob parentJob = (job == null) ? null : this.getCaseJob(caseId, job, session);

        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();
        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = (paramname == null) ? builder.isNull(root.get("parameterName")) : builder.equal(root.get("parameterName"),
                paramname);
        Predicate c3 = (sector == null) ? builder.isNull(root.get("sector")) : builder.equal(root.get("sector"), sector);
        Predicate c4 = (program == null) ? builder.isNull(root.get("program")) : builder.equal(root.get("program"), program);
        Predicate c5 = null;

        if (parentJob != null)
            c5 = builder.equal(root.get("jobId"), parentJob.getId());
        else
            c5 = builder.equal(root.get("jobId"), Integer.valueOf(0));

        return new Predicate[] { c1, c2, c3, c4, c5 };
    }

    private Predicate[] uniqueCaseParameterCriteria(int caseId, CaseParameter param, CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot) {
        ParameterName paramname = param.getParameterName();
        GeoRegion region = param.getRegion();
        Sector sector = param.getSector();
        CaseProgram program = param.getProgram();
        Integer jobID = Integer.valueOf(param.getJobId());

        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();
        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = (paramname == null) ? builder.isNull(root.get("parameterName")) : builder.equal(root.get("parameterName"),
                paramname);
        Predicate c3 = (region == null) ? builder.isNull(root.get("region")) : builder.equal(root.get("region"), region);
        Predicate c4 = (sector == null) ? builder.isNull(root.get("sector")) : builder.equal(root.get("sector"), sector);
        Predicate c5 = (program == null) ? builder.isNull(root.get("program")) : builder.equal(root.get("program"), program);
        Predicate c6 = builder.equal(root.get("jobId"), jobID);

        return new Predicate[] { c1, c2, c3, c4, c5, c6 };
    }

    public List<CaseParameter> getCaseParameters(int caseId, Session session) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);
        return hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseID"), Integer.valueOf(caseId)));
    }

    public List<CaseParameter> getJobSpecNonSpecCaseParameters(int caseId, int[] jobIds, Session session) {
        List<?> ids = session.createQuery(
                "SELECT obj.id from " + CaseParameter.class.getSimpleName() + " as obj WHERE obj.caseID = " + caseId
                        + " AND (obj.jobId = 0 OR obj.jobId = " + getAndOrClause(jobIds, "obj.jobId") + ")").list();

        List<CaseParameter> params = new ArrayList<CaseParameter>();

        for (Iterator<?> iter = ids.iterator(); iter.hasNext();)
            params.add(this.getCaseParameter((Integer) iter.next(), session));

        return params;
    }

    public List<CaseParameter> getCaseParametersByJobIds(int caseId, int[] jobIds, Session session) {
        List<?> ids = session.createQuery(
                "SELECT obj.id from " + CaseParameter.class.getSimpleName() + " as obj WHERE obj.caseID = " + caseId
                        + " AND (obj.jobId = " + getAndOrClause(jobIds, "obj.jobId") + ")").list();

        List<CaseParameter> params = new ArrayList<CaseParameter>();

        for (Iterator<?> iter = ids.iterator(); iter.hasNext();)
            params.add(this.getCaseParameter((Integer) iter.next(), session));

        return params;
    }

    public List<CaseParameter> getCaseParametersByJobId(int caseId, int jobId, Session session) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = builder.equal(root.get("jobId"), Integer.valueOf(jobId));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, session);
    }

    public List<CaseParameter> getCaseParametersBySector(int caseId, Sector sector, Session session) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = builder.equal(root.get("sector"), sector);

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, session);
    }

    public List<CaseParameter> getCaseParametersForAllSectors(int caseId, Session session) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = builder.isNull(root.get("sector"));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, session);
    }

    public List<CaseParameter> getCaseParametersForAllSectorsAllJobs(int caseId, Session session) {
        return getJobParameters(caseId, 0, null, session);
    }

    public List<CaseParameter> getCaseParameters(int pageSize, int caseId, Sector sector, String envNameContains, boolean showAll,
            Session session) {
        if (sector == null)
            return  filterParameters(getAllParameters(pageSize, caseId, showAll, session), envNameContains);

        String sectorName = sector.getName().toUpperCase();

        if (sectorName.equals("ALL"))
            return filterParameters(getCaseParametersWithLocal(showAll, caseId, session), envNameContains);

        if (sectorName.equals("ALL SECTORS"))
            return filterParameters(getCaseParametersWithNullSector(showAll, caseId, session), envNameContains);

        return filterParameters(getCaseParametersWithSector(showAll, sector, caseId, session), envNameContains);
    }

    private List<CaseParameter> getAllParameters(int pageSize, int caseId, boolean showAll, Session session) {
        List<CaseParameter> params = getCaseParametersWithLocal(showAll, caseId, session);

        if (params.size() < pageSize)
            return params;

        return params.subList(0, pageSize);
    }

    private List<CaseParameter> getCaseParametersWithLocal(boolean showAll, int caseId, Session session) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("local"), true);
        Predicate[] crits = (showAll) ? new Predicate[] { crit1 } : new Predicate[] { crit1, crit2 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, session);
    }

    public List<CaseParameter> getCaseParametersFromEnv(int caseId, ParameterEnvVar envVar, Session session) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        // Get parameters based on environment variable
        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("envVar"), envVar);
        Predicate[] crits = { crit1, crit2 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, session);
    }

    public ParameterEnvVar getParameterEnvVar(String envName, int model_to_run_id, Session session) {
        CriteriaBuilderQueryRoot<ParameterEnvVar> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterEnvVar.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ParameterEnvVar> root = criteriaBuilderQueryRoot.getRoot();

        // Get parameter environmental variables from name
        Predicate crit1 = builder.equal(root.get("name"), envName);
        Predicate crit2 = builder.equal(root.get("modelToRunId"), model_to_run_id);
        Predicate[] crits = { crit1, crit2 };

        // return hibernateFacade.get(ParameterEnvVar.class, crits, session);
        return hibernateFacade.load(session, criteriaBuilderQueryRoot, crits);
    }

    private List<CaseParameter> getCaseParametersWithNullSector(boolean showAll, int caseId, Session session) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.isNull(root.get("sector"));
        Predicate crit3 = builder.equal(root.get("local"), true);
        Predicate[] crits = (showAll) ? new Predicate[] { crit1, crit2 } : new Predicate[] { crit1, crit2, crit3 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, session);
    }

    private List<CaseParameter> getCaseParametersWithSector(boolean showAll, Sector sector, int caseId, Session session) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("sector"), sector);
        Predicate crit3 = builder.equal(root.get("local"), true);
        Predicate[] crits = (showAll) ? new Predicate[] { crit1, crit2 } : new Predicate[] { crit1, crit2, crit3 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, session);
    }

    public boolean caseParameterExists(CaseParameter param, Session session) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, session);

        Predicate[] criterions = uniqueCaseParameterCriteria(param.getCaseID(), param, criteriaBuilderQueryRoot);

        return hibernateFacade.exists(criteriaBuilderQueryRoot, criterions, session);
    }

    public void updateCaseParameter(CaseParameter parameter, Session session) {
        hibernateFacade.updateOnly(parameter, session);
    }

    public CaseJob loadCaseJob(CaseJob job, Session session) {
        CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, session);

        Predicate[] criterions = uniqueCaseJobCriteria(job.getCaseId(), job, criteriaBuilderQueryRoot);
        return hibernateFacade.load(session, criteriaBuilderQueryRoot, criterions);
    }

    @SuppressWarnings("unchecked")
    public CaseJob loadCaseJobByName(CaseJob job) {
        Session session = sessionFactory.getSession();
        CaseJob obj = null;
        
        try {
            CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, session);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<CaseJob> root = criteriaBuilderQueryRoot.getRoot();

            Predicate c1 = builder.equal(root.get("caseId"), Integer.valueOf(job.getCaseId()));
            Predicate c2 = builder.equal(root.get("name"), job.getName());
            Predicate[] criterions = { c1, c2 };
            List<CaseJob> jobs = hibernateFacade.get(criteriaBuilderQueryRoot, criterions, session);
            
            if (jobs != null && jobs.size() > 0) 
                obj = jobs.get(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }
        
        return obj;

    }
    
    public CaseJob loadUniqueCaseJob(CaseJob job) throws EmfException {
        Session session = sessionFactory.getSession();
        
        try {
            CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, session);

            Predicate[] criterions = uniqueCaseJobCriteria(job.getCaseId(), job, criteriaBuilderQueryRoot);
            return hibernateFacade.load(session, criteriaBuilderQueryRoot, criterions);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("Can't load case job: " + job.getName() + ".");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public void removeCaseJobs(CaseJob[] jobs, Session session) {
        hibernateFacade.remove(jobs, session);
    }

    public void removeCaseParameters(CaseParameter[] params, Session session) {
        hibernateFacade.remove(params, session);
    }

    public CaseInput getCaseInput(int inputId, Session session) {
        return hibernateFacade.load(CaseInput.class, "id", Integer.valueOf(inputId), session);
    }

    public CaseParameter getCaseParameter(int paramId, Session session) {
        return hibernateFacade.load(CaseParameter.class, "id", Integer.valueOf(paramId), session);
    }

    public CaseJob[] getAllValidJobs(int jobId, int caseId) {
        List<CaseJob> validJobs = new ArrayList<CaseJob>();
        List<CaseJob> jobs = getCaseJobs(caseId);

        for (Iterator<CaseJob> iter = jobs.iterator(); iter.hasNext();) {
            CaseJob job = iter.next();

            if (canDependOn(jobId, job.getId()))
                validJobs.add(job);
        }

        return validJobs.toArray(new CaseJob[0]);
    }

    public CaseJob[] getDependentJobs(int jobId) {
        if (jobId <= 0)
            return new CaseJob[0];

        DependentJob[] dependentJobs = getCaseJob(jobId).getDependentJobs();
        CaseJob[] selectedJobs = new CaseJob[dependentJobs.length];

        for (int i = 0; i < dependentJobs.length; i++) {
            int id = dependentJobs[i].getJobId();
            selectedJobs[i] = getCaseJob(id);
        }

        return selectedJobs;
    }

    private boolean canDependOn(int jobId, int dependentJobId) {
        // FIXME: this really should be a recursive check on all the possible dependencies
        // to avoid cycle dependencies.
        if (jobId <= 0) // a new job
            return true;

        if (jobId == dependentJobId)
            return false;

        CaseJob job = getCaseJob(dependentJobId);
        DependentJob[] depentdentJobs = job.getDependentJobs();

        for (DependentJob dpj : depentdentJobs)
            if (jobId == dpj.getJobId())
                return false;

        return true;
    }

    public int[] getJobIds(int caseId, String[] jobNames) {
        int[] ids = new int[jobNames.length];
        Session session = sessionFactory.getSession();

        try {
            for (int i = 0; i < jobNames.length; i++) {
                CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, session);
                CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
                Root<CaseJob> root = criteriaBuilderQueryRoot.getRoot();
                
                Predicate crit1 = builder.equal(root.get("caseId"), Integer.valueOf(caseId));
                Predicate crit2 = builder.equal(root.get("name"), jobNames[i]);
                CaseJob job = hibernateFacade.load(session, criteriaBuilderQueryRoot, new Predicate[] { crit1, crit2 });
                ids[i] = job.getId();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return ids;
    }

    public synchronized List<PersistedWaitTask> getPersistedWaitTasksByUser(int userId) {
        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::getPersistedWaitTasks Start method");

        Session session = sessionFactory.getSession();

        try {
            CriteriaBuilderQueryRoot<PersistedWaitTask> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(PersistedWaitTask.class, session);
            return hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("userId"), Integer.valueOf(userId)));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.clear();
            session.close();
        }

        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::getPersistedWaitTasks End method");
        return null;
    }

    public synchronized List<PersistedWaitTask> getPersistedWaitTasks(int caseId, int jobId, Session session) {
        CriteriaBuilderQueryRoot<PersistedWaitTask> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(PersistedWaitTask.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<PersistedWaitTask> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseId"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("jobId"), Integer.valueOf(jobId));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { crit1, crit2 }, session);
    }

    public List<?> getDistinctUsersOfPersistedWaitTasks() {
        Session session = sessionFactory.getSession();
        List<?> userIds = null;

        try {
            String sql = "select id,user_id from cases.taskmanager_persist";

            userIds = session.createSQLQuery(sql).addEntity(IntegerHolder.class).list();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        return userIds;
    }

    public void removePersistedTasks(PersistedWaitTask[] pwTasks) {
        if (DebugLevels.DEBUG_9())
            System.out
                    .println("CaseDAO::removePersistedTasks BEFORE num of tasks is pwTask null? " + (pwTasks == null));

        if (pwTasks == null || pwTasks.length == 0)
            return;

        Session session = sessionFactory.getSession();

        try {
            for (int i = 0; i < pwTasks.length; i++) {
                session.clear();
                hibernateFacade.delete(pwTasks[i], session);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::removePersistedTasks AFTER num of tasks= ");
    }

    public void addPersistedTask(PersistedWaitTask persistedWaitTask) {
        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::addPersistedTask BEFORE num of tasks= ");
        Session session = sessionFactory.getSession();

        try {
            CriteriaBuilderQueryRoot<PersistedWaitTask> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(PersistedWaitTask.class, session);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<PersistedWaitTask> root = criteriaBuilderQueryRoot.getRoot();

            // NOTE: Remove the old one if pesistedWaitTask already exists
            Predicate crit1 = builder.equal(root.get("caseId"), Integer.valueOf(persistedWaitTask.getCaseId()));
            Predicate crit2 = builder.equal(root.get("jobId"), Integer.valueOf(persistedWaitTask.getJobId()));
            PersistedWaitTask existedTask = hibernateFacade.load(session, criteriaBuilderQueryRoot, 
                    new Predicate[] { crit1, crit2 });

            if (existedTask != null)
                hibernateFacade.remove(existedTask, session);

            hibernateFacade.add(persistedWaitTask, session);
            if (DebugLevels.DEBUG_15())
                System.out.println("Adding job to persisted table, jobID: " + persistedWaitTask.getJobId());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::addPersistedTask AFTER num of tasks= ");

    }

    public void removePersistedTask(PersistedWaitTask persistedWaitTask) {
        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::removePersistedTask (from CJTM) BEFORE num of tasks is pwTask null "
                    + (persistedWaitTask == null));

        Session session = sessionFactory.getSession();

        try {
            session.clear();
            CriteriaBuilderQueryRoot<PersistedWaitTask> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(PersistedWaitTask.class, session);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<PersistedWaitTask> root = criteriaBuilderQueryRoot.getRoot();

            Predicate crit1 = builder.equal(root.get("userId"), Integer.valueOf(persistedWaitTask.getUserId()));
            Predicate crit2 = builder.equal(root.get("caseId"), Integer.valueOf(persistedWaitTask.getCaseId()));
            Predicate crit3 = builder.equal(root.get("jobId"), Integer.valueOf(persistedWaitTask.getJobId()));
            Object object = hibernateFacade.load(session, criteriaBuilderQueryRoot, new Predicate[] { crit1, crit2, crit3 });
            if (object != null) {
                hibernateFacade.deleteTask(object, session);
            } else {
                if (DebugLevels.DEBUG_15()) {
                    System.out.println("Removing from persisted table a job currently not there, jobID: "
                            + persistedWaitTask.getJobId());
                    CriteriaBuilderQueryRoot<PersistedWaitTask> criteriaBuilderQueryRoot2 = hibernateFacade.getCriteriaBuilderQueryRoot(PersistedWaitTask.class, session);
                    int numberPersistedTasks = hibernateFacade.getAll(criteriaBuilderQueryRoot2, session).size();
                    System.out.println("Current size of persisted table: " + numberPersistedTasks);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }

        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::removePersistedTasks  (from CJTM) AFTER num of tasks= ");

    }

    public Case[] getCases(CaseCategory category) {
        Session session = sessionFactory.getSession();
        List<?> cases = null;

        try {
            CriteriaBuilderQueryRoot<Case> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Case.class, session);
        
            cases = hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseCategory"), category));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
 
        return cases == null ? null : (Case[]) cases.toArray(new Case[0]);
    }
    
    public Case[] getCases(Session session, String nameContains) {

        String ns = Utils.getPattern(nameContains.toLowerCase().trim());

        List<?> cases=session
        .createQuery(
                "FROM Case as CA WHERE lower(CA.name) like "  + ns
                + " order by CA.name").list();

        return cases == null ? null : (Case[]) cases.toArray(new Case[0]);
    }

    public Case[] getCases(Session session, CaseCategory category, String nameContains) {
        
        String ns = Utils.getPattern(nameContains.toLowerCase().trim());
        List<?> cases = session
        .createQuery(
                "FROM Case as CA WHERE lower(CA.name) like "  + ns
                + " and CA.caseCategory.id=" + category.getId() + " "
                + " order by CA.name").list();

        return cases == null ? null : (Case[]) cases.toArray(new Case[0]);

    }


    public CaseOutput getCaseOutput(CaseOutput output, Session session) {
        CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseOutput.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseOutput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseId"), Integer.valueOf(output.getCaseId()));
        Predicate crit2 = builder.equal(root.get("jobId"), Integer.valueOf(output.getJobId()));
        Predicate crit3 = builder.equal(root.get("name"), output.getName());

        return hibernateFacade.load(session, criteriaBuilderQueryRoot, new Predicate[] { crit1, crit2, crit3 });
    }

    public List<CaseOutput> getCaseOutputs(int caseId, Session session) {
        CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseOutput.class, session);
        return hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseId"), Integer.valueOf(caseId)));
    }

    public List<CaseOutput> getCaseOutputs(int caseId, int jobId, Session session) {
        CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseOutput.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseOutput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseId"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("jobId"), Integer.valueOf(jobId));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { crit1, crit2 }, session);
    }

    public Case updateWithLock(Case caseObj, Session session) throws EmfException {
        return (Case) lockingScheme.renewLockOnUpdate(caseObj, current(caseObj, session), session);
    }

    public void canUpdate(Case caseObj, Session session) throws EmfException {
//        if (!exists(caseObj.getId(), Case.class, session)) 
//            throw new EmfException("This case id is not valid. ");
        
        Case current = current(caseObj.getId(), session);
        session.clear();// clear to flush current
       
        if (! current.getName().equals(caseObj.getName()) ) {
            if (nameUsed(caseObj.getName(), Case.class, session))
                throw new EmfException("The case name is already in use. ");
        }
        if(!current.getAbbreviation().getName().equals(caseObj.getAbbreviation().getName()) ){
            if (nameUsed(caseObj.getAbbreviation().getName(), Abbreviation.class, session))
                throw new EmfException("The case abbreviation is already in use. ");
        }      
     
    }

    public <C> boolean nameUsed(String name, Class<C> clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private void removeDatasetsOnOutput(User user, Session session, CaseOutput[] outputs) throws EmfException {
        DatasetDAO dsDao = new DatasetDAO();

        session.clear();

        for (CaseOutput output : outputs) {
            EmfDataset dataset = dsDao.getDataset(session, output.getDatasetId());

            if (dataset != null) {
                try {
                    dsDao.remove(user, dataset, session);
                } catch (EmfException e) {
                    if (DebugLevels.DEBUG_12())
                        System.out.println(e.getMessage());

                    throw new EmfException(e.getMessage());
                }
            }
        }
    }

    public Object loadCaseOutput(CaseOutput output, Session session) {
        CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseOutput.class, session);

        Predicate[] criterions = uniqueCaseOutputCriteria(output, criteriaBuilderQueryRoot);

        return hibernateFacade.load(session, criteriaBuilderQueryRoot, criterions);
    }

    private Predicate[] uniqueCaseOutputCriteria(CaseOutput output, CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot) {
        Integer caseID = Integer.valueOf(output.getCaseId());
        String outputname = output.getName();
        Integer datasetID = Integer.valueOf(output.getDatasetId());
        Integer jobID = Integer.valueOf(output.getJobId());

        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseOutput> root = criteriaBuilderQueryRoot.getRoot();
        Predicate c1 = builder.equal(root.get("caseId"), caseID);
        Predicate c2 = builder.equal(root.get("name"), outputname);
        Predicate c3 = (datasetID == null) ? builder.isNull(root.get("datasetId")) : builder.equal(root.get("datasetId"), datasetID);
        Predicate c4 = builder.equal(root.get("jobId"), jobID);

        return new Predicate[] { c1, c2, c3, c4 };
    }

    public boolean caseOutputExists(CaseOutput output, Session session) {
        CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseOutput.class, session);

        Predicate[] criterions = uniqueCaseOutputCriteria(output, criteriaBuilderQueryRoot);

        return hibernateFacade.exists(criteriaBuilderQueryRoot, criterions, session);
    }

    public void updateCaseOutput(CaseOutput output, Session session) {
        hibernateFacade.updateOnly(output, session);
    }

    public void removeJobMessages(JobMessage[] msgs, Session session) {
        hibernateFacade.remove(msgs, session);
    }

    public List<QueueCaseOutput> getQueueCaseOutputs(Session session) {
        CriteriaBuilderQueryRoot<QueueCaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QueueCaseOutput.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QueueCaseOutput> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("createDate")), session);
    }

    public List<QueueCaseOutput> getQueueCaseOutputs(int caseId, int jobId, Session session) {
        CriteriaBuilderQueryRoot<QueueCaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QueueCaseOutput.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QueueCaseOutput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("caseId"), Integer.valueOf(caseId));
        Predicate c2 = builder.equal(root.get("jobId"), Integer.valueOf(jobId));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, session);
    }

    public void addQueueCaseOutput(QueueCaseOutput output, Session session) {
        hibernateFacade.add(output, session);
    }

    public void removeQedOutput(QueueCaseOutput output, Session session) {
        hibernateFacade.remove(output, session);
    }

    public String[] getAllCaseNameIDs(Session session) {
        List<?> names = session.createQuery(
                "SELECT obj.name from " + Case.class.getSimpleName() + " as obj ORDER BY obj.name").list();
        List<?> ids = session.createQuery(
                "SELECT obj.id from " + Case.class.getSimpleName() + " as obj ORDER BY obj.name").list();
        int size = names.size();

        if (size != ids.size())
            return new String[0];

        String[] nameIDStrings = new String[size];

        for (int i = 0; i < size; i++)
            nameIDStrings[i] = names.get(i).toString() + "  (" + ids.get(i).toString() + ")";

        return nameIDStrings;
    }

    private String getAndOrClause(int[] ids, String attrName) {
        StringBuffer sb = new StringBuffer();
        int numIDs = ids.length;

        if (numIDs == 1)
            return "" + ids[0];

        for (int i = 0; i < numIDs - 1; i++)
            sb.append(ids[i] + " OR " + attrName + " = ");

        sb.append(ids[numIDs - 1]);

        return sb.toString();
    }

    public CaseParameter[] getCaseParametersFromEnvName(int caseId, String envName, int model_to_run_id)
            throws EmfException {
        // Get case parameters that match a specific environment variables name
        Session session = sessionFactory.getSession();
        try {
            // Get environmental variable corresponding to this name
            ParameterEnvVar envVar = getParameterEnvVar(envName, model_to_run_id, session);
            if (envVar == null) {
                throw new EmfException("Could not get parameter environmental variable for " + envName);

            }
            // Get parameters corresponding to this environmental variable
            List<CaseParameter> parameters = getCaseParametersFromEnv(caseId, envVar, session);
            if (parameters == null) {
                throw new EmfException("Could not get parameters for " + envName);
            }
            return parameters.toArray(new CaseParameter[0]);

        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Could not get case parameters for " + envName);

        } finally {
            if (session != null || !session.isConnected()) {
                session.close();
            }
        }
    }

    private String[] findEnvVars(String input, String delimiter) {
        // Find any environmental variables that are in a string
        List<String> envVars = new ArrayList<String>();
        if (input.contains("$")) {
            // Split the string at spaces
            StringTokenizer st = new StringTokenizer(input, delimiter);

            // loop over substrings
            while (st.hasMoreTokens()) {
                String temp = st.nextToken();
                // add variable if starts w/ $, but remove the dollar
                if (temp.startsWith("$") && temp.length() > 1) {
                    envVars.add(temp.substring(1));
                }
            }
        }
        return envVars.toArray(new String[0]);
    }

    public String replaceEnvVars(String input, String delimiter, int caseId, int model_to_run_id, int jobId, Sector jobSector, GeoRegion jobRegion)
            throws EmfException {
        // replace any environemental variables with their values
        // use the delimiter to separate out environment variables
        try {
            if (input.contains("$")) {
                String[] envVarsStrs = findEnvVars(input, delimiter);
                if (envVarsStrs.length > 0) {

                    String cleanedInput = input;
                    for (String envName : envVarsStrs) {
                        // loop over env variable names, get the parameter,
                        // and replace the env name in input string w/ that value
                        CaseParameter envVar = getUniqueCaseParametersFromEnvName(caseId, envName, jobId,
                                model_to_run_id, jobSector, jobRegion);

                        // Replace exact matches of environmental variable name

                        // Split the string at delimeter
                        StringTokenizer st = new StringTokenizer(cleanedInput, delimiter);

                        String tempInput = "";
                        // loop over substrings
                        while (st.hasMoreTokens()) {
                            String temp = st.nextToken();
                            // check if temp = environemental variable name, if so replace
                            if (temp.equals("$" + envName))
                                temp = envVar.getValue();

                            // reconstruct new input string, if first time through don't add preceding delimiter
                            if (tempInput.equals("")) {
                                tempInput = tempInput + temp;
                            } else {
                                tempInput = tempInput + delimiter + temp;
                            }

                        }
                        // reset the cleaned input to the latest tempInput
                        cleanedInput = tempInput;
                    } // end loop over environmental variables

                    // check if input starts or ends with delimiter
                    if (input.startsWith(delimiter))
                        cleanedInput = delimiter + cleanedInput;
                    if (input.endsWith(delimiter))
                        cleanedInput = cleanedInput + delimiter;

                    // replace input w/ cleaned input
                    input = cleanedInput;
                }
            }
            return input;
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public String replaceEnvVarsCase(String input, String delimiter, Case caseObj, int jobId, Sector jobSector, GeoRegion jobRegion) throws EmfException {
        // replace any environmental variables with their values
        // use the delimiter to separate out environment variables
        // If $CASE is found, replace it from the case summary abbreviation
        

        try {
            String tempInput = "";
            if (!input.contains("$"))
                return input;
            if (input.contains("$CASE")) {
                // Replace exact matches of CASE
                if (input.startsWith(delimiter))
                    tempInput = delimiter;
                // Split the string at delimeter
                StringTokenizer st = new StringTokenizer(input, delimiter);

                // loop over substrings
                while (st.hasMoreTokens()) {
                    String temp = st.nextToken();
                    // check if temp = $CASE, if so replace
                    if (temp.equals("$CASE"))
                        temp = caseObj.getAbbreviation().getName();
                    // reconstruct new input string, if first time through don't add preceding delimiter
                    if (tempInput.equals(delimiter) || tempInput.equals("")) {
                        tempInput = tempInput + temp;
                    } else {
                        tempInput = tempInput + delimiter + temp;
                    }
                }
                // check if input ends with delimiter
                if (input.endsWith(delimiter))
                    tempInput = tempInput + delimiter;
            } else
                tempInput = input;

            // replace any remaining environmental variables
            int caseId = caseObj.getId();
            int model_to_run_id = caseObj.getModel().getId();
            tempInput = replaceEnvVars(tempInput, delimiter, caseId, model_to_run_id, jobId, jobSector, jobRegion);
            return tempInput;

        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    private CaseParameter selectCaseParameterByHierarchy(int jobId, Sector sector, GeoRegion region, CaseParameter[] parameters){
        // Select parameter based on jobID, sector, and region
        CaseParameter matchingParam = null;

        for (CaseParameter param: parameters){
            boolean regionMatch = false;
            boolean sectorMatch = false;
            if (param.getSector() == null) {
                if (sector == null) sectorMatch = true;
            } else {
                if (param.getSector().equals(sector)) sectorMatch = true;
            }
            if (param.getRegion() == null) {
                if (region == null) regionMatch = true;
            } else {
                if (param.getRegion().equals(region)) regionMatch = true;
            }
            
            // if both region and sector match, test jobId
            if (regionMatch && sectorMatch){
                if (param.getJobId() == jobId) {
                    matchingParam = param;
                }
            }       
        }
        return matchingParam;
    }
    
    public CaseParameter getUniqueCaseParametersFromEnvName(int caseId, String envName, int jobId, int model_to_run_id,
              Sector jobSector, GeoRegion jobRegion)
            throws EmfException {
        // Get case parameters that match a specific environment variables name
        // If more than 1 matches the environmental variable name, uses the job Id, sector
        // and region to find unique one
        

        try {
            CaseParameter[] tempParams = getCaseParametersFromEnvName(caseId, envName, model_to_run_id);
            List<CaseParameter> params = new ArrayList<CaseParameter>();
            CaseParameter matchingParam = null;
            
            /** Go through the parameter hierarchy and get the most exact match
             * Present hierarchy from general to specific
             *    AAA - all regions, all sectors, all jobs
             *    RAA - specific region, all sectors, all jobs
             *    ASA - all regions, specific sector, all jobs
             *    RSA - specific region, specific sector, all jobs
             *    AAJ - all regions, all sectors, specific job
             *    RAJ - specific region, all sectors, specific job
             *    ASJ - all regions, specific sector, specific job
             *    RSJ - specific region, specific sector, specific job
             */
            
            // AAA
            matchingParam = selectCaseParameterByHierarchy(this.ALL_JOB_ID, this.ALL_SECTORS, this.ALL_REGIONS, tempParams);
            if (matchingParam != null) params.add(matchingParam);
            
            // If job, sector, and/or region are set, continue search
            // otherwise, test params
            if ((jobId != this.ALL_JOB_ID) || (jobSector != this.ALL_SECTORS) || (jobRegion != this.ALL_REGIONS)){
                //RAA
                matchingParam = selectCaseParameterByHierarchy(this.ALL_JOB_ID, this.ALL_SECTORS, jobRegion, tempParams);
                if (matchingParam != null) {
                    params.clear(); // clear because we've found one more specific
                    params.add(matchingParam);
                }
                
                //ASA
                matchingParam = selectCaseParameterByHierarchy(this.ALL_JOB_ID, jobSector, this.ALL_REGIONS, tempParams);
                if (matchingParam != null) {
                    params.clear(); // clear because we've found one more specific
                    params.add(matchingParam);
                }
                
                //RSA
                matchingParam = selectCaseParameterByHierarchy(this.ALL_JOB_ID, jobSector, jobRegion, tempParams);
                if (matchingParam != null) {
                    params.clear(); // clear because we've found one more specific
                    params.add(matchingParam);
                }
                
                
                //AAJ
                matchingParam = selectCaseParameterByHierarchy(jobId, this.ALL_SECTORS, this.ALL_REGIONS, tempParams);
                if (matchingParam != null) {
                    params.clear(); // clear because we've found one more specific
                    params.add(matchingParam);
                }
                
                
                //RAJ
                matchingParam = selectCaseParameterByHierarchy(jobId, this.ALL_SECTORS, jobRegion, tempParams);
                if (matchingParam != null) {
                    params.clear(); // clear because we've found one more specific
                    params.add(matchingParam);
                }
                
                
                //ASJ
                matchingParam = selectCaseParameterByHierarchy(jobId, jobSector, this.ALL_REGIONS, tempParams);
                if (matchingParam != null) {
                    params.clear(); // clear because we've found one more specific
                    params.add(matchingParam);
                }
                
                
                //RSJ
                matchingParam = selectCaseParameterByHierarchy(jobId, jobSector, jobRegion, tempParams);
                if (matchingParam != null) {
                    params.clear(); // clear because we've found one more specific
                    params.add(matchingParam);
                }
                

            }
            
            
            
            if (params.size() > 1) {
                throw new EmfException("Could not find a unique case parameter for " + envName + ", jobId" + jobId);
            }
            if  (params.size() == 0) {
                throw new EmfException("Could not find any matching case parameters for " + envName + ", jobId" + jobId);
            }
            
            
            // return the matching param
            return params.get(0);
    
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Could not get unique case parameters for " + envName);
        }
    }

    public List<Case> getSensitivityCases(int parentCaseId, Session session) {
        CriteriaBuilderQueryRoot<CasesSens> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CasesSens.class, session);
        List<CasesSens> caseIds = hibernateFacade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("parentCaseid"), Integer.valueOf(parentCaseId)));

        List<Case> sensitivityCases = new ArrayList<Case>();

        for (Iterator<CasesSens> iter = caseIds.iterator(); iter.hasNext();)
            sensitivityCases.add(this.getCase(iter.next().getSensCaseId(), session));

        return sensitivityCases;
    }

    public String[] getJobGroups(int caseId, Session session) {
        List<?> groups = session.createQuery(
                "SELECT DISTINCT obj.jobGroup from " + CaseJob.class.getSimpleName() + " as obj WHERE obj.caseId = "
                        + caseId + " ORDER BY obj.jobGroup").list();

        return groups.toArray(new String[0]);
    }

    public void checkParentChildRelationship(Case caseObj, Session session) throws EmfException {
        int caseId = caseObj.getId();

        List<?> parentCases = session.createQuery(
                "SELECT obj.sensCaseId FROM " + CasesSens.class.getSimpleName() + " as obj WHERE obj.parentCaseid = "
                        + caseId + " ORDER BY obj.parentCaseid").list();

        if (parentCases.size() == 1)
            throw new EmfException("Case " + caseObj.getName() + " is the parent case of "
                    + getCase(Integer.parseInt(parentCases.get(0).toString()), session).getName() + ".");

        if (parentCases.size() > 1)
            throw new EmfException("Case " + caseObj.getName() + " is the parent case of multiple cases: "
                    + getCase(Integer.parseInt(parentCases.get(0).toString()), session).getName() + ", etc.");

    }

    public void checkJobDependency(CaseJob[] jobs, Session session) throws EmfException {
        CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseJob> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.isNotNull(root.get("dependentJobs"));
        Predicate c2 = builder.isNotEmpty(root.get("dependentJobs"));

        List<CaseJob> jobsDeps = hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, session);

        for (Iterator<CaseJob> iter = jobsDeps.iterator(); iter.hasNext();) {
            CaseJob jobDeps = iter.next();
            DependentJob[] depJobs = jobDeps.getDependentJobs();

            for (DependentJob depJob : depJobs) {
                int depJobId = depJob.getJobId();

                for (CaseJob del : jobs)
                    if (depJobId == del.getId())
                        throw new EmfException("job '" + del.getName() + "', because job '" + jobDeps.getName()
                                + "' depends on it.");
            }
        }
    }

    public void removeChildCase(int caseId, Session session) {
        List<?> childrenCases = session.createQuery(
                "SELECT obj.id FROM " + CasesSens.class.getSimpleName() + " as obj WHERE obj.sensCaseId = " + caseId
                        + " ORDER BY obj.id").list();

        int numOfChildren = childrenCases.size();

        if (numOfChildren == 0)
            return;

        String clause = " obj.id = ";

        for (int i = 0; i < numOfChildren; i++) {
            if (i < numOfChildren - 1)
                clause += Integer.parseInt(childrenCases.get(i).toString()) + " AND obj.id = ";
            else
                clause += Integer.parseInt(childrenCases.get(i).toString());
        }

        Transaction tx = session.beginTransaction();
        String hqlDelete = "DELETE FROM " + CasesSens.class.getSimpleName() + " obj WHERE " + clause;
        session.createQuery(hqlDelete).executeUpdate();
        tx.commit();
    }

    public CaseParameter getCaseParameter(int caseId, ParameterEnvVar var, Session session) {
        ParameterEnvVar loadedVar = this.getParameterEnvVar(var.getName(), var.getModelToRunId(), session);

        if (loadedVar == null)
            return null;

        String query = "SELECT obj.id FROM " + CaseParameter.class.getSimpleName() + " obj WHERE " + "obj.caseID = "
                + caseId + " AND obj.envVar.id = " + loadedVar.getId();
        List<?> ids = session.createQuery(query).list();

        if (ids == null || ids.size() == 0)
            return null;

        return getCaseParameter(Integer.parseInt(ids.get(0).toString()), session);
    }

    public int[] getExternalDatasetIds(String source, Session session) {
        if (source == null || source.trim().isEmpty())
            return new int[0];
        // NOTE: emf.external_sources and emf.datasets tables are hardwired here, not a desirable
        // way but necessary, because ExternalSource object is not explicitly mapped through hibernate
        // Another way to access the tables is to use DbServer object.
        String queryAll = "SELECT DISTINCT ex.dataset_id FROM emf.external_sources ex WHERE ex.datasource=" + "'"
                + source.replaceAll("\\\\", "\\\\\\\\") + "'";

        String queryNonDeleted = "SELECT ds.id FROM emf.datasets ds WHERE lower(ds.status) <> 'deleted' AND ds.id IN ("
                + queryAll + ")";
        List<?> ids = session.createSQLQuery(queryNonDeleted).list();

        int[] dsIds = new int[ids.size()];

        for (int i = 0; i < dsIds.length; i++) {
            dsIds[i] = Integer.parseInt(ids.get(i).toString());
        }

        return dsIds;
    }
    
}