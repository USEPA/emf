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
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class CaseDAO {

    private HibernateFacade hibernateFacade;

    private LockingScheme lockingScheme;

    private EntityManagerFactory entityManagerFactory;

    private final Sector ALL_SECTORS = null;
    
    private final GeoRegion ALL_REGIONS = null;

    private final int ALL_JOB_ID = 0;


    public CaseDAO(EntityManagerFactory entityManagerFactory) {
        super();
        this.entityManagerFactory = entityManagerFactory;
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
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            hibernateFacade.add(message, entityManager);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    public CaseOutput add(CaseOutput output) throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            hibernateFacade.add(output, entityManager);
            return getCaseOutput(output, entityManager);
        } catch (Exception ex) {
            throw new Exception("Problem adding case output: " + output.getName() + ". " + ex.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public void add(Object obj) throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            hibernateFacade.add(obj, entityManager);
        } catch (Exception ex) {
            throw new Exception("Problem adding object: " + obj.toString() + ". " + ex.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public CaseOutput updateCaseOutput(CaseOutput output) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CaseOutput toReturn = null;

        try {
            hibernateFacade.updateOnly(output, entityManager);
            toReturn = getCaseOutput(output, entityManager);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }

        return toReturn;
    }

    public CaseOutput updateCaseOutput(EntityManager entityManager, CaseOutput output) {
        hibernateFacade.updateOnly(output, entityManager);
        return getCaseOutput(output, entityManager);
    }

    public boolean caseOutputNameUsed(String outputName) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<CaseOutput> outputs = null;

        try {
            CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseOutput.class, entityManager);
            outputs = hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("name"), outputName));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }

        return (outputs != null && outputs.size() > 0);
    }

    public void add(Executable exe, EntityManager entityManager) {
        addObject(exe, entityManager);
    }

    public void add(SubDir subdir, EntityManager entityManager) {
        addObject(subdir, entityManager);
    }

    public void add(AirQualityModel object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(CaseCategory object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(EmissionsYear object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(GeoRegion object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(MeteorlogicalYear object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(Speciation object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(CaseProgram object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(InputName object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(InputEnvtVar object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(ModelToRun object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(Case object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(CaseInput object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(CaseOutput object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void add(Host object, EntityManager entityManager) {
        addObject(object, entityManager);
    }

    public void addObject(Object obj, EntityManager entityManager) {
        hibernateFacade.add(obj, entityManager);
    }

    public List<Abbreviation> getAbbreviations(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Abbreviation> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Abbreviation.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Abbreviation> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public Abbreviation getAbbreviation(Abbreviation abbr, EntityManager entityManager) {
        return hibernateFacade.load(Abbreviation.class, "name", abbr.getName(), entityManager);
    }

    public List<AirQualityModel> getAirQualityModels(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<AirQualityModel> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(AirQualityModel.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<AirQualityModel> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<CaseCategory> getCaseCategories(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseCategory> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseCategory.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseCategory> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public CaseCategory getCaseCategory(String name, EntityManager entityManager) {
        return hibernateFacade.load(CaseCategory.class, "name", name, entityManager);
    }

    public List<EmissionsYear> getEmissionsYears(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<EmissionsYear> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(EmissionsYear.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<EmissionsYear> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<MeteorlogicalYear> getMeteorlogicalYears(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<MeteorlogicalYear> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(MeteorlogicalYear.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<MeteorlogicalYear> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<Speciation> getSpeciations(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Speciation> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Speciation.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Speciation> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<Case> getCases(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Case> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Case.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Case> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public Case getCase(int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Case> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Case.class, entityManager);

        List<Case> caseObj = hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("id"), Integer.valueOf(caseId)));

        if (caseObj == null || caseObj.size() == 0)
            return null;

        return caseObj.get(0);
    }

    public Case getCaseFromAbbr(Abbreviation abbr, EntityManager entityManager) {
        return hibernateFacade.load(Case.class, "abbreviation", abbr, entityManager);
    }

    public Case getCaseFromName(String name, EntityManager entityManager) {
        // Get a case from it's name
        return hibernateFacade.load(Case.class, "name", name, entityManager);
    }

    public List<CaseProgram> getPrograms(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseProgram> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseProgram.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseProgram> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<InputName> getInputNames(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<InputName> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(InputName.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<InputName> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<InputEnvtVar> getInputEnvtVars(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<InputEnvtVar> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(InputEnvtVar.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<InputEnvtVar> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public void removeObject(Object object, EntityManager entityManager) {
        hibernateFacade.remove(object, entityManager);
    }

    public void removeObjects(Object[] objects, EntityManager entityManager) {
        hibernateFacade.remove(objects, entityManager);
    }

    public void remove(Case element, EntityManager entityManager) {
        hibernateFacade.remove(element, entityManager);
    }

    public void removeCaseInputs(CaseInput[] inputs, EntityManager entityManager) {
        hibernateFacade.remove(inputs, entityManager);
    }

    public void removeCaseOutputs(User user, CaseOutput[] outputs, boolean deleteDataset, EntityManager entityManager)
            throws EmfException {
        try {
            if (deleteDataset)
                removeDatasetsOnOutput(user, entityManager, outputs);
        } finally {
            hibernateFacade.remove(outputs, entityManager);
        }
    }

    public void removeCaseOutputs(CaseOutput[] outputs, EntityManager entityManager) {
        hibernateFacade.removeObjects(outputs, entityManager);
    }

    public Case obtainLocked(User owner, Case element, EntityManager entityManager) {
        return (Case) lockingScheme.getLocked(owner, current(element, entityManager), entityManager);
    }

    public Case releaseLocked(User owner, Case locked, EntityManager entityManager) {
        return (Case) lockingScheme.releaseLock(owner, current(locked, entityManager), entityManager);
    }

    public Case forceReleaseLocked(Case locked, EntityManager entityManager) {
        return (Case) lockingScheme.releaseLock(current(locked, entityManager), entityManager);
    }

    public Case update(Case locked, EntityManager entityManager) throws EmfException {
        return (Case) lockingScheme.releaseLockOnUpdate(locked, current(locked, entityManager), entityManager);
    }

    private Case current(Case caze, EntityManager entityManager) {
        return hibernateFacade.current(caze.getId(), Case.class, entityManager);
    }

    private Case current(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, Case.class, entityManager);
    }

    public boolean caseInputExists(CaseInput input, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, entityManager);

        Predicate[] predicates = uniqueCaseInputCriteria(input.getCaseID(), input, criteriaBuilderQueryRoot);

        return hibernateFacade.exists(criteriaBuilderQueryRoot, predicates, entityManager);
    }

//    public boolean exists(int id, Class<?> clazz, EntityManager entityManager) {
//        return hibernateFacade.exists(id, clazz, entityManager);
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
    
    public boolean caseJobExists(CaseJob job, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, entityManager);

        Predicate[] criterions = uniqueCaseJobCriteria(job.getCaseId(), job, criteriaBuilderQueryRoot);

        return hibernateFacade.exists(criteriaBuilderQueryRoot, criterions, entityManager);
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

    public Object load(Class<?> clazz, String name, EntityManager entityManager) {
        return hibernateFacade.load(clazz, "name", name, entityManager);
    }
    
    public <C> CriteriaBuilderQueryRoot<C> getCriteriaBuilderQueryRoot(Class<C> persistentClass, EntityManager entityManager) {
        return hibernateFacade.getCriteriaBuilderQueryRoot(persistentClass, entityManager);
    }

    public ModelToRun loadModelTorun(String name, EntityManager entityManager) {
        String query = " FROM " + ModelToRun.class.getSimpleName() + " as obj WHERE lower(obj.name)='" + name.toLowerCase()+ "'";
        List<ModelToRun> mods = entityManager.createQuery(query, ModelToRun.class).getResultList();
        
        if (mods == null || mods.size() == 0)
            return null;
        
        return mods.get(0);
    }

    public <C> C load(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate[] predicates, EntityManager entityManager) {
        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, predicates);
    }

    public <C> C load(Class<C> clazz, int id, EntityManager entityManager) {
        return hibernateFacade.load(clazz, "id", Integer.valueOf(id), entityManager);
    }

    public ParameterEnvVar loadParamEnvVar(ParameterEnvVar envVar, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ParameterEnvVar> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterEnvVar.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ParameterEnvVar> root = criteriaBuilderQueryRoot.getRoot();
        
        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(envVar.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), envVar.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, entityManager);
    }

    public CaseProgram loadCaseProgram(CaseProgram prog, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseProgram> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseProgram.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseProgram> root = criteriaBuilderQueryRoot.getRoot();

        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(prog.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), prog.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, entityManager);
    }

    public SubDir loadCaseSubdir(SubDir subdir, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<SubDir> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(SubDir.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<SubDir> root = criteriaBuilderQueryRoot.getRoot();

        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(subdir.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), subdir.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, entityManager);
    }

    public Object loadParameterName(ParameterName paramName, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ParameterName> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterName.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ParameterName> root = criteriaBuilderQueryRoot.getRoot();

        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(paramName.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), paramName.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, entityManager);
    }

    public Object loadParameterEnvVar(ParameterEnvVar envVar, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ParameterEnvVar> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterEnvVar.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ParameterEnvVar> root = criteriaBuilderQueryRoot.getRoot();

        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(envVar.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), envVar.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, entityManager);
    }

    public InputName loadInputName(InputName inputName, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<InputName> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(InputName.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<InputName> root = criteriaBuilderQueryRoot.getRoot();

        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(inputName.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), inputName.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, entityManager);
    }

    public InputEnvtVar loadInputEnvtVar(InputEnvtVar envVar, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<InputEnvtVar> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(InputEnvtVar.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<InputEnvtVar> root = criteriaBuilderQueryRoot.getRoot();

        Predicate pred1 = builder.equal(root.get("modelToRunId"), Integer.valueOf(envVar.getModelToRunId()));
        Predicate pred2 = builder.equal(root.get("name"), envVar.getName());

        return this.load(criteriaBuilderQueryRoot, new Predicate[] { pred1, pred2 }, entityManager);
    }

    public Object loadCaseInput(CaseInput input, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, entityManager);

        Predicate[] predicates = uniqueCaseInputCriteria(input.getCaseID(), input, criteriaBuilderQueryRoot);

        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, predicates);
    }

    public CaseInput loadCaseInput(int caseId, CaseInput input, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, entityManager);

        Predicate[] predicates = uniqueCaseInputCriteria(caseId, input, criteriaBuilderQueryRoot);

        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, predicates);
    }

    public List<CaseInput> getCaseInputs(int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, entityManager);
        return hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseID"), Integer.valueOf(caseId)));
    }

    public List<CaseInput> getJobSpecNonSpecCaseInputs(int caseId, int[] jobIds, EntityManager entityManager) {
        List<Integer> ids = entityManager
                .createQuery(
                        "SELECT obj.id from " + CaseInput.class.getSimpleName() + " as obj WHERE obj.caseID = "
                                + caseId + " AND (obj.caseJobID = 0 OR obj.caseJobID = "
                                + getAndOrClause(jobIds, "obj.caseJobID") + ")", Integer.class)
                .getResultList();
        List<CaseInput> inputs = new ArrayList<CaseInput>();

        for (Iterator<Integer> iter = ids.iterator(); iter.hasNext();) {
            Integer id = iter.next();
            inputs.add(this.getCaseInput(id, entityManager));
        }

        return inputs;
    }

    public List<CaseInput> getCaseInputsByJobIds(int caseId, int[] jobIds, EntityManager entityManager) {
        List<Integer> ids = entityManager.createQuery(
                "SELECT obj.id from " + CaseInput.class.getSimpleName() + " as obj WHERE obj.caseID = " + caseId
                        + " AND (obj.caseJobID = " + getAndOrClause(jobIds, "obj.caseJobID") + ")", Integer.class).getResultList();
        List<CaseInput> inputs = new ArrayList<CaseInput>();

        for (Iterator<Integer> iter = ids.iterator(); iter.hasNext();) {
            Integer id = iter.next();
            inputs.add(this.getCaseInput(id, entityManager));
        }

        return inputs;
    }

    public List<CaseInput> getCaseInputs(int pageSize, int caseId, Sector sector, String envNameContains, boolean showAll, EntityManager entityManager) {
        if (sector == null)
            return filterInputs(getAllInputs(pageSize, caseId, showAll, entityManager), envNameContains);

        String sectorName = sector.getName().toUpperCase();

        if (sectorName.equals("ALL"))
            return filterInputs(getCaseInputsWithLocal(showAll, caseId, entityManager), envNameContains);

        if (sectorName.equals("ALL SECTORS"))
            return filterInputs(getCaseInputsWithNullSector(showAll, caseId, entityManager), envNameContains);

        return filterInputs(getCaseInputsWithSector(showAll, sector, caseId, entityManager), envNameContains);
    }

    private List<CaseInput> getAllInputs(int pageSize, int caseId, boolean showAll, EntityManager entityManager) {
        List<CaseInput> inputs = getCaseInputsWithLocal(showAll, caseId, entityManager);

        if (inputs.size() < pageSize)
            return inputs;

        return inputs.subList(0, pageSize);
    }

    private List<CaseInput> getCaseInputsWithLocal(boolean showAll, int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("local"), true);
        Predicate[] crits = (showAll) ? new Predicate[] { crit1 } : new Predicate[] { crit1, crit2 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, entityManager);
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

    private List<CaseInput> getCaseInputsWithNullSector(boolean showAll, int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.isNull(root.get("sector"));
        Predicate crit3 = builder.equal(root.get("local"), true);
        Predicate[] crits = (showAll) ? new Predicate[] { crit1, crit2 } : new Predicate[] { crit1, crit2, crit3 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, entityManager);
    }

    public List<Case> getCasesThatInputToOtherCases(int caseId, EntityManager entityManager) {
        String sql = "select new gov.epa.emissions.framework.services.casemanagement.Case(cs.id, cs.name) from Case cs where cs.id in (select distinct cO.caseId from CaseInput as cI, CaseOutput as cO where cI.dataset.id = cO.datasetId and cI.caseID = :caseId)";
        return entityManager.createQuery(sql, Case.class).setParameter("caseId", caseId).getResultList();
    }

    public List<Case> getCasesThatOutputToOtherCases(int caseId, EntityManager entityManager) {
        String sql = "select new gov.epa.emissions.framework.services.casemanagement.Case(cs.id, cs.name) from Case cs where cs.id in (select distinct cI.caseID from CaseOutput as cO, CaseInput as cI where cI.dataset.id = cO.datasetId and cO.caseId = :caseId)";
        return entityManager.createQuery(sql, Case.class).setParameter("caseId", caseId).getResultList();
    }

    public List<Case> getCasesByOutputDatasets(int[] datasetIds, EntityManager entityManager) {
        String idList = "";
        for (int id : datasetIds)
            idList += (idList.length() > 0 ? "," : "") + id;
        String sql = "select new gov.epa.emissions.framework.services.casemanagement.Case(cs.id, cs.name) from Case cs where cs.id in (select distinct cO.caseId from CaseOutput as cO where cO.datasetId in ("
                + idList + "))";
        return entityManager.createQuery(sql, Case.class).getResultList();
    }

    public List<Case> getCasesByInputDataset(int datasetId, EntityManager entityManager) {
        String sql = "select new gov.epa.emissions.framework.services.casemanagement.Case(cs.id, cs.name) from Case cs where cs.id in (select distinct cI.caseID from CaseInput as cI where cI.dataset.id = :datasetId)";
        return entityManager.createQuery(sql).setParameter("datasetId", datasetId).getResultList();
    }

    private List<CaseInput> getCaseInputsWithSector(boolean showAll, Sector sector, int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("sector"), sector);
        Predicate crit3 = builder.equal(root.get("local"), true);
        Predicate[] crits = (showAll) ? new Predicate[] { crit1, crit2 } : new Predicate[] { crit1, crit2, crit3 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, entityManager);
    }

    public List<CaseInput> getInputsBySector(int caseId, Sector sector, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = builder.equal(root.get("sector"), sector);

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, entityManager);
    }

    public List<CaseInput> getInputsForAllSectors(int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = builder.isNull(root.get("sector"));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, entityManager);
    }

    public List<CaseInput> getInputs4AllJobsAllSectors(int caseId, EntityManager entityManager) {
        return getJobInputs(caseId, 0, null, entityManager);
    }
    
    public List<CaseInput> getJobInputs(int caseId, int jobId, Sector sector, EntityManager entityManager) {
        /**
         * Gets inputs for a job. Selects on the following 3 criteria: caseId, jobId, sectorId
         */
        Integer caseID = Integer.valueOf(caseId);
        Integer jobID = Integer.valueOf(jobId);

        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, entityManager);
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
        return hibernateFacade.get(criteriaBuilderQueryRoot, criterions, entityManager);

    }

    public List<CaseInput> getJobInputs(int caseId, int jobId, Sector sector, GeoRegion region, EntityManager entityManager) {
        /**
         * Gets inputs for a job. Selects on the following 3 criteria: caseId, jobId, sectorId
         */
        Integer caseID = Integer.valueOf(caseId);
        Integer jobID = Integer.valueOf(jobId);

        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, entityManager);
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
        return hibernateFacade.get(criteriaBuilderQueryRoot, criterions, entityManager);

    }

    public List<CaseParameter> getJobParameters(int caseId, int jobId, Sector sector, EntityManager entityManager) {
        /**
         * Gets parameters for a job. Selects on the following 3 criteria: caseId, jobId, sectorId
         */
        Integer caseID = Integer.valueOf(caseId);
        Integer jobID = Integer.valueOf(jobId);

        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        // setup the 3 criteria
        Predicate c1 = builder.equal(root.get("caseID"), caseID);
        Predicate c2 = (sector == null) ? builder.isNull(root.get("sector")) : builder.equal(root.get("sector"), sector);
        Predicate c3 = builder.equal(root.get("jobId"), jobID);
        Predicate[] criterions = { c1, c2, c3 };

        // query the db using hibernate for the parameters that
        // match the criterias
        return hibernateFacade.get(criteriaBuilderQueryRoot, criterions, entityManager);

    }
    
    public List<CaseParameter> getJobParameters(int caseId, int jobId, Sector sector, GeoRegion region, EntityManager entityManager) {
        /**
         * Gets parameters for a job. Selects on the following 3 criteria: caseId, jobId, sectorId
         */
        Integer caseID = Integer.valueOf(caseId);
        Integer jobID = Integer.valueOf(jobId);

        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);
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
        return hibernateFacade.get(criteriaBuilderQueryRoot, criterions, entityManager);
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
    
    public List<?> getAllCaseInputs(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseInput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseInput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseInput> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("id")), entityManager);
    }

    public void updateCaseInput(CaseInput input, EntityManager entityManager) {
        hibernateFacade.updateOnly(input, entityManager);
    }

    public List<ModelToRun> getModelToRuns(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ModelToRun> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ModelToRun.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ModelToRun> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<SubDir> getSubDirs(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<SubDir> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(SubDir.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<SubDir> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public void add(CaseJob job, EntityManager entityManager) {
        addObject(job, entityManager);
    }

    public List<CaseJob> getCaseJobs(int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, entityManager);
        return hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseId"), Integer.valueOf(caseId)));
    }

    public List<CaseJob> getCaseJobs(int caseId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<CaseJob> jobs = null;

        try {
            CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, entityManager);
            jobs = hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseId"), Integer.valueOf(caseId)));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }

        return jobs;
    }

    public CaseJob getCaseJob(String jobKey) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CaseJob job = null;

        try {
            job = hibernateFacade.load(CaseJob.class, "jobkey", jobKey, entityManager);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }

        return job;
    }

    public List<JobMessage> getJobMessages(int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<JobMessage> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(JobMessage.class, entityManager);
        return hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseId"), Integer.valueOf(caseId)));
    }

    public List<JobMessage> getJobMessages(int caseId, int jobId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<JobMessage> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(JobMessage.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<JobMessage> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseId"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("jobId"), Integer.valueOf(jobId));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { crit1, crit2 }, entityManager);
    }

    public CaseJob getCaseJob(int jobId, EntityManager entityManager) {
        if (jobId == 0)      //NOTE: to save a db access
            return null;
        
        return hibernateFacade.load(CaseJob.class, "id", Integer.valueOf(jobId), entityManager);
    }

    public List<Sector> getSectorsUsedbyJobs(int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Sector> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Sector.class, entityManager);
        return hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseId"), Integer.valueOf(caseId)));
    }

    public CaseJob getCaseJob(int jobId) {
        if (jobId == 0)
            return null;
        
        CaseJob caseJob = null;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            caseJob = hibernateFacade.load(CaseJob.class, "id", Integer.valueOf(jobId), entityManager);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
        return caseJob;
    }

    public CaseJob getCaseJob(int caseId, CaseJob job, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, entityManager);

        Predicate[] crits = uniqueCaseJobCriteria(caseId, job, criteriaBuilderQueryRoot);

        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, crits);
    }

    public void updateCaseJob(CaseJob job) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            hibernateFacade.updateOnly(job, entityManager);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    public void updateCaseJob(CaseJob job, EntityManager entityManager) throws Exception {
        try {
            hibernateFacade.updateOnly(job, entityManager);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        }
    }

    public List<CaseJobKey> getCaseJobKey(int jobId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseJobKey> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJobKey.class, entityManager);
        return hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("jobId"), Integer.valueOf(jobId)));
    }

    public List<CasesSens> getCasesSens(int parentId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CasesSens> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CasesSens.class, entityManager);
        return hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("parentId"), Integer.valueOf(parentId)));
    }

    public CaseJob getCaseJobFromKey(String key) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CaseJob job = null;

        try {
            CriteriaBuilderQueryRoot<CaseJobKey> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJobKey.class, entityManager);
            List<CaseJobKey> keyObjs = hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("key"), key));

            if (keyObjs == null || keyObjs.size() == 0)
                return null;

            job = getCaseJob(keyObjs.get(0).getJobId(), entityManager);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }

        return job;
    }

    public void updateCaseJobKey(int jobId, String jobKey, EntityManager entityManager) throws Exception {
        try {
            List<?> keys = getCaseJobKey(jobId, entityManager);
            CaseJobKey keyObj = (keys == null || keys.size() == 0) ? null : (CaseJobKey) keys.get(0);

            if (keyObj == null) {
                addObject(new CaseJobKey(jobKey, jobId), entityManager);
                return;
            }

            if (keyObj.getKey().equals(jobKey))
                return;

            keyObj.setKey(jobKey);
            hibernateFacade.updateOnly(keyObj, entityManager);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex.getMessage());
        }
    }

    public List<JobRunStatus> getJobRunStatuses(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<JobRunStatus> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(JobRunStatus.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<JobRunStatus> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public JobRunStatus getJobRunStatuse(String status, EntityManager entityManager) {
        return hibernateFacade.load(JobRunStatus.class, "name", status, entityManager);
    }

    public JobRunStatus getJobRunStatuse(String status) {
        if (DebugLevels.DEBUG_9())
            System.out
                    .println("In CaseDAO::getJobRunStatuse: Is the entityManager Factory null? " + (entityManagerFactory == null));

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        JobRunStatus jrs = null;

        try {
            jrs = hibernateFacade.load(JobRunStatus.class, "name", status, entityManager);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }

        return jrs;

    }

    public List<Host> getHosts(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Host> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Host.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Host> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<Executable> getExecutables(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Executable> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Executable.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Executable> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public boolean exeutableExists(EntityManager entityManager, Executable exe) {
        return hibernateFacade.exists(exe.getName(), Executable.class, entityManager);
    }

    public void add(Abbreviation element, EntityManager entityManager) {
        addObject(element, entityManager);
    }

    public void add(ParameterEnvVar envVar, EntityManager entityManager) {
        addObject(envVar, entityManager);
    }

    public List<ParameterEnvVar> getParameterEnvVars(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ParameterEnvVar> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterEnvVar.class, entityManager);
        return hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);
    }

    public void addValueType(ValueType type, EntityManager entityManager) {
        addObject(type, entityManager);
    }

    public List<ValueType> getValueTypes(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ValueType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ValueType.class, entityManager);
        return hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);
    }

    public void addParameterName(ParameterName name, EntityManager entityManager) {
        addObject(name, entityManager);
    }

    public List<ParameterName> getParameterNames(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ParameterName> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterName.class, entityManager);
        return hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);
    }

    public void addParameter(CaseParameter param, EntityManager entityManager) {
        addObject(param, entityManager);
    }

    public CaseParameter loadCaseParameter(CaseParameter param, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);

        Predicate[] criterions = uniqueCaseParameterCriteria(param.getCaseID(), param, criteriaBuilderQueryRoot);

        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, criterions);
    }

    public CaseParameter loadCaseParameter4Sensitivity(int caseId, CaseParameter param, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);

        Predicate[] criterions = sensitivityCaseParameterCriteria(caseId, param, entityManager, criteriaBuilderQueryRoot);

        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, criterions);
    }

    // NOTE: this method is soly for creating sensitivity case. The questions without clear answers include the
    // following:
    // What if the job does exist in the parent case, but the parameter to copy has a different job?
    private Predicate[] sensitivityCaseParameterCriteria(int caseId, CaseParameter param, EntityManager entityManager, CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot) {
        ParameterName paramname = param.getParameterName();
        Sector sector = param.getSector();
        CaseProgram program = param.getProgram();
        Integer jobID = Integer.valueOf(param.getJobId());

        CaseJob job = this.getCaseJob(jobID, entityManager);
        CaseJob parentJob = (job == null) ? null : this.getCaseJob(caseId, job, entityManager);

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

    public List<CaseParameter> getCaseParameters(int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);
        return hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseID"), Integer.valueOf(caseId)));
    }

    public List<CaseParameter> getJobSpecNonSpecCaseParameters(int caseId, int[] jobIds, EntityManager entityManager) {
        List<Integer> ids = entityManager.createQuery(
                "SELECT obj.id from " + CaseParameter.class.getSimpleName() + " as obj WHERE obj.caseID = " + caseId
                        + " AND (obj.jobId = 0 OR obj.jobId = " + getAndOrClause(jobIds, "obj.jobId") + ")", Integer.class).getResultList();

        List<CaseParameter> params = new ArrayList<CaseParameter>();

        for (Iterator<Integer> iter = ids.iterator(); iter.hasNext();)
            params.add(this.getCaseParameter(iter.next(), entityManager));

        return params;
    }

    public List<CaseParameter> getCaseParametersByJobIds(int caseId, int[] jobIds, EntityManager entityManager) {
        List<Integer> ids = entityManager.createQuery(
                "SELECT obj.id from " + CaseParameter.class.getSimpleName() + " as obj WHERE obj.caseID = " + caseId
                        + " AND (obj.jobId = " + getAndOrClause(jobIds, "obj.jobId") + ")", Integer.class).getResultList();

        List<CaseParameter> params = new ArrayList<CaseParameter>();

        for (Iterator<Integer> iter = ids.iterator(); iter.hasNext();)
            params.add(this.getCaseParameter(iter.next(), entityManager));

        return params;
    }

    public List<CaseParameter> getCaseParametersByJobId(int caseId, int jobId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = builder.equal(root.get("jobId"), Integer.valueOf(jobId));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, entityManager);
    }

    public List<CaseParameter> getCaseParametersBySector(int caseId, Sector sector, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = builder.equal(root.get("sector"), sector);

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, entityManager);
    }

    public List<CaseParameter> getCaseParametersForAllSectors(int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate c2 = builder.isNull(root.get("sector"));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, entityManager);
    }

    public List<CaseParameter> getCaseParametersForAllSectorsAllJobs(int caseId, EntityManager entityManager) {
        return getJobParameters(caseId, 0, null, entityManager);
    }

    public List<CaseParameter> getCaseParameters(int pageSize, int caseId, Sector sector, String envNameContains, boolean showAll,
            EntityManager entityManager) {
        if (sector == null)
            return  filterParameters(getAllParameters(pageSize, caseId, showAll, entityManager), envNameContains);

        String sectorName = sector.getName().toUpperCase();

        if (sectorName.equals("ALL"))
            return filterParameters(getCaseParametersWithLocal(showAll, caseId, entityManager), envNameContains);

        if (sectorName.equals("ALL SECTORS"))
            return filterParameters(getCaseParametersWithNullSector(showAll, caseId, entityManager), envNameContains);

        return filterParameters(getCaseParametersWithSector(showAll, sector, caseId, entityManager), envNameContains);
    }

    private List<CaseParameter> getAllParameters(int pageSize, int caseId, boolean showAll, EntityManager entityManager) {
        List<CaseParameter> params = getCaseParametersWithLocal(showAll, caseId, entityManager);

        if (params.size() < pageSize)
            return params;

        return params.subList(0, pageSize);
    }

    private List<CaseParameter> getCaseParametersWithLocal(boolean showAll, int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("local"), true);
        Predicate[] crits = (showAll) ? new Predicate[] { crit1 } : new Predicate[] { crit1, crit2 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, entityManager);
    }

    public List<CaseParameter> getCaseParametersFromEnv(int caseId, ParameterEnvVar envVar, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        // Get parameters based on environment variable
        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("envVar"), envVar);
        Predicate[] crits = { crit1, crit2 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, entityManager);
    }

    public ParameterEnvVar getParameterEnvVar(String envName, int model_to_run_id, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ParameterEnvVar> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterEnvVar.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ParameterEnvVar> root = criteriaBuilderQueryRoot.getRoot();

        // Get parameter environmental variables from name
        Predicate crit1 = builder.equal(root.get("name"), envName);
        Predicate crit2 = builder.equal(root.get("modelToRunId"), model_to_run_id);
        Predicate[] crits = { crit1, crit2 };

        // return hibernateFacade.get(ParameterEnvVar.class, crits, entityManager);
        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, crits);
    }

    private List<CaseParameter> getCaseParametersWithNullSector(boolean showAll, int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.isNull(root.get("sector"));
        Predicate crit3 = builder.equal(root.get("local"), true);
        Predicate[] crits = (showAll) ? new Predicate[] { crit1, crit2 } : new Predicate[] { crit1, crit2, crit3 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, entityManager);
    }

    private List<CaseParameter> getCaseParametersWithSector(boolean showAll, Sector sector, int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseParameter> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseID"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("sector"), sector);
        Predicate crit3 = builder.equal(root.get("local"), true);
        Predicate[] crits = (showAll) ? new Predicate[] { crit1, crit2 } : new Predicate[] { crit1, crit2, crit3 };

        return hibernateFacade.get(criteriaBuilderQueryRoot, crits, entityManager);
    }

    public boolean caseParameterExists(CaseParameter param, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseParameter> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseParameter.class, entityManager);

        Predicate[] criterions = uniqueCaseParameterCriteria(param.getCaseID(), param, criteriaBuilderQueryRoot);

        return hibernateFacade.exists(criteriaBuilderQueryRoot, criterions, entityManager);
    }

    public void updateCaseParameter(CaseParameter parameter, EntityManager entityManager) {
        hibernateFacade.updateOnly(parameter, entityManager);
    }

    public CaseJob loadCaseJob(CaseJob job, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, entityManager);

        Predicate[] criterions = uniqueCaseJobCriteria(job.getCaseId(), job, criteriaBuilderQueryRoot);
        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, criterions);
    }

    @SuppressWarnings("unchecked")
    public CaseJob loadCaseJobByName(CaseJob job) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CaseJob obj = null;
        
        try {
            CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<CaseJob> root = criteriaBuilderQueryRoot.getRoot();

            Predicate c1 = builder.equal(root.get("caseId"), Integer.valueOf(job.getCaseId()));
            Predicate c2 = builder.equal(root.get("name"), job.getName());
            Predicate[] criterions = { c1, c2 };
            List<CaseJob> jobs = hibernateFacade.get(criteriaBuilderQueryRoot, criterions, entityManager);
            
            if (jobs != null && jobs.size() > 0) 
                obj = jobs.get(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }
        
        return obj;

    }
    
    public CaseJob loadUniqueCaseJob(CaseJob job) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        
        try {
            CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, entityManager);

            Predicate[] criterions = uniqueCaseJobCriteria(job.getCaseId(), job, criteriaBuilderQueryRoot);
            return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, criterions);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("Can't load case job: " + job.getName() + ".");
        } finally {
            if (entityManager != null)
                entityManager.close();
        }
    }

    public void removeCaseJobs(CaseJob[] jobs, EntityManager entityManager) {
        hibernateFacade.remove(jobs, entityManager);
    }

    public void removeCaseParameters(CaseParameter[] params, EntityManager entityManager) {
        hibernateFacade.remove(params, entityManager);
    }

    public CaseInput getCaseInput(int inputId, EntityManager entityManager) {
        return hibernateFacade.load(CaseInput.class, "id", Integer.valueOf(inputId), entityManager);
    }

    public CaseParameter getCaseParameter(int paramId, EntityManager entityManager) {
        return hibernateFacade.load(CaseParameter.class, "id", Integer.valueOf(paramId), entityManager);
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
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            for (int i = 0; i < jobNames.length; i++) {
                CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, entityManager);
                CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
                Root<CaseJob> root = criteriaBuilderQueryRoot.getRoot();
                
                Predicate crit1 = builder.equal(root.get("caseId"), Integer.valueOf(caseId));
                Predicate crit2 = builder.equal(root.get("name"), jobNames[i]);
                CaseJob job = hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, new Predicate[] { crit1, crit2 });
                ids[i] = job.getId();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }

        return ids;
    }

    public synchronized List<PersistedWaitTask> getPersistedWaitTasksByUser(int userId) {
        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::getPersistedWaitTasks Start method");

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            CriteriaBuilderQueryRoot<PersistedWaitTask> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(PersistedWaitTask.class, entityManager);
            return hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("userId"), Integer.valueOf(userId)));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.clear();
            entityManager.close();
        }

        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::getPersistedWaitTasks End method");
        return null;
    }

    public synchronized List<PersistedWaitTask> getPersistedWaitTasks(int caseId, int jobId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<PersistedWaitTask> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(PersistedWaitTask.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<PersistedWaitTask> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseId"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("jobId"), Integer.valueOf(jobId));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { crit1, crit2 }, entityManager);
    }

    public List<IntegerHolder> getDistinctUsersOfPersistedWaitTasks() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<IntegerHolder> userIds = null;

        try {
            String sql = "select id,user_id from cases.taskmanager_persist";

            userIds = entityManager.createNativeQuery(sql, "IntegerHolderMapping").getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }

        return userIds;
    }

    public void removePersistedTasks(PersistedWaitTask[] pwTasks) {
        if (DebugLevels.DEBUG_9())
            System.out
                    .println("CaseDAO::removePersistedTasks BEFORE num of tasks is pwTask null? " + (pwTasks == null));

        if (pwTasks == null || pwTasks.length == 0)
            return;

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            for (int i = 0; i < pwTasks.length; i++) {
                entityManager.clear();
                hibernateFacade.delete(pwTasks[i], entityManager);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }

        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::removePersistedTasks AFTER num of tasks= ");
    }

    public void addPersistedTask(PersistedWaitTask persistedWaitTask) {
        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::addPersistedTask BEFORE num of tasks= ");
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            CriteriaBuilderQueryRoot<PersistedWaitTask> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(PersistedWaitTask.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<PersistedWaitTask> root = criteriaBuilderQueryRoot.getRoot();

            // NOTE: Remove the old one if pesistedWaitTask already exists
            Predicate crit1 = builder.equal(root.get("caseId"), Integer.valueOf(persistedWaitTask.getCaseId()));
            Predicate crit2 = builder.equal(root.get("jobId"), Integer.valueOf(persistedWaitTask.getJobId()));
            PersistedWaitTask existedTask = hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, 
                    new Predicate[] { crit1, crit2 });

            if (existedTask != null)
                hibernateFacade.remove(existedTask, entityManager);

            hibernateFacade.add(persistedWaitTask, entityManager);
            if (DebugLevels.DEBUG_15())
                System.out.println("Adding job to persisted table, jobID: " + persistedWaitTask.getJobId());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }

        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::addPersistedTask AFTER num of tasks= ");

    }

    public void removePersistedTask(PersistedWaitTask persistedWaitTask) {
        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::removePersistedTask (from CJTM) BEFORE num of tasks is pwTask null "
                    + (persistedWaitTask == null));

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            entityManager.clear();
            CriteriaBuilderQueryRoot<PersistedWaitTask> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(PersistedWaitTask.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<PersistedWaitTask> root = criteriaBuilderQueryRoot.getRoot();

            Predicate crit1 = builder.equal(root.get("userId"), Integer.valueOf(persistedWaitTask.getUserId()));
            Predicate crit2 = builder.equal(root.get("caseId"), Integer.valueOf(persistedWaitTask.getCaseId()));
            Predicate crit3 = builder.equal(root.get("jobId"), Integer.valueOf(persistedWaitTask.getJobId()));
            Object object = hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, new Predicate[] { crit1, crit2, crit3 });
            if (object != null) {
                hibernateFacade.remove(object, entityManager);
            } else {
                if (DebugLevels.DEBUG_15()) {
                    System.out.println("Removing from persisted table a job currently not there, jobID: "
                            + persistedWaitTask.getJobId());
                    CriteriaBuilderQueryRoot<PersistedWaitTask> criteriaBuilderQueryRoot2 = hibernateFacade.getCriteriaBuilderQueryRoot(PersistedWaitTask.class, entityManager);
                    int numberPersistedTasks = hibernateFacade.getAll(criteriaBuilderQueryRoot2, entityManager).size();
                    System.out.println("Current size of persisted table: " + numberPersistedTasks);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            entityManager.close();
        }

        if (DebugLevels.DEBUG_9())
            System.out.println("CaseDAO::removePersistedTasks  (from CJTM) AFTER num of tasks= ");

    }

    public Case[] getCases(CaseCategory category) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<?> cases = null;

        try {
            CriteriaBuilderQueryRoot<Case> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Case.class, entityManager);
        
            cases = hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseCategory"), category));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
 
        return cases == null ? null : (Case[]) cases.toArray(new Case[0]);
    }
    
    public Case[] getCases(EntityManager entityManager, String nameContains) {

        String ns = Utils.getPattern(nameContains.toLowerCase().trim());

        List<Case> cases=entityManager
        .createQuery(
                "FROM Case as CA WHERE lower(CA.name) like "  + ns
                + " order by CA.name", Case.class).getResultList();

        return cases == null ? null : (Case[]) cases.toArray(new Case[0]);
    }

    public Case[] getCases(EntityManager entityManager, CaseCategory category, String nameContains) {
        
        String ns = Utils.getPattern(nameContains.toLowerCase().trim());
        List<Case> cases = entityManager
        .createQuery(
                "FROM Case as CA WHERE lower(CA.name) like "  + ns
                + " and CA.caseCategory.id=" + category.getId() + " "
                + " order by CA.name", Case.class).getResultList();

        return cases == null ? null : (Case[]) cases.toArray(new Case[0]);

    }


    public CaseOutput getCaseOutput(CaseOutput output, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseOutput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseOutput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseId"), Integer.valueOf(output.getCaseId()));
        Predicate crit2 = builder.equal(root.get("jobId"), Integer.valueOf(output.getJobId()));
        Predicate crit3 = builder.equal(root.get("name"), output.getName());

        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, new Predicate[] { crit1, crit2, crit3 });
    }

    public List<CaseOutput> getCaseOutputs(int caseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseOutput.class, entityManager);
        return hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("caseId"), Integer.valueOf(caseId)));
    }

    public List<CaseOutput> getCaseOutputs(int caseId, int jobId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseOutput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseOutput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("caseId"), Integer.valueOf(caseId));
        Predicate crit2 = builder.equal(root.get("jobId"), Integer.valueOf(jobId));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { crit1, crit2 }, entityManager);
    }

    public Case updateWithLock(Case caseObj, EntityManager entityManager) throws EmfException {
        return (Case) lockingScheme.renewLockOnUpdate(caseObj, current(caseObj, entityManager), entityManager);
    }

    public void canUpdate(Case caseObj, EntityManager entityManager) throws EmfException {
//        if (!exists(caseObj.getId(), Case.class, entityManager)) 
//            throw new EmfException("This case id is not valid. ");
        
        Case current = current(caseObj.getId(), entityManager);
        entityManager.clear();// clear to flush current
       
        if (! current.getName().equals(caseObj.getName()) ) {
            if (nameUsed(caseObj.getName(), Case.class, entityManager))
                throw new EmfException("The case name is already in use. ");
        }
        if(!current.getAbbreviation().getName().equals(caseObj.getAbbreviation().getName()) ){
            if (nameUsed(caseObj.getAbbreviation().getName(), Abbreviation.class, entityManager))
                throw new EmfException("The case abbreviation is already in use. ");
        }      
     
    }

    public <C> boolean nameUsed(String name, Class<C> clazz, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, clazz, entityManager);
    }

    private void removeDatasetsOnOutput(User user, EntityManager entityManager, CaseOutput[] outputs) throws EmfException {
        DatasetDAO dsDao = new DatasetDAO();

        entityManager.clear();

        for (CaseOutput output : outputs) {
            EmfDataset dataset = dsDao.getDataset(entityManager, output.getDatasetId());

            if (dataset != null) {
                try {
                    dsDao.remove(user, dataset, entityManager);
                } catch (EmfException e) {
                    if (DebugLevels.DEBUG_12())
                        System.out.println(e.getMessage());

                    throw new EmfException(e.getMessage());
                }
            }
        }
    }

    public Object loadCaseOutput(CaseOutput output, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseOutput.class, entityManager);

        Predicate[] criterions = uniqueCaseOutputCriteria(output, criteriaBuilderQueryRoot);

        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, criterions);
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

    public boolean caseOutputExists(CaseOutput output, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseOutput.class, entityManager);

        Predicate[] criterions = uniqueCaseOutputCriteria(output, criteriaBuilderQueryRoot);

        return hibernateFacade.exists(criteriaBuilderQueryRoot, criterions, entityManager);
    }

    public void updateCaseOutput(CaseOutput output, EntityManager entityManager) {
        hibernateFacade.updateOnly(output, entityManager);
    }

    public void removeJobMessages(JobMessage[] msgs, EntityManager entityManager) {
        hibernateFacade.remove(msgs, entityManager);
    }

    public List<QueueCaseOutput> getQueueCaseOutputs(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<QueueCaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QueueCaseOutput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QueueCaseOutput> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("createDate")), entityManager);
    }

    public List<QueueCaseOutput> getQueueCaseOutputs(int caseId, int jobId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<QueueCaseOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(QueueCaseOutput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<QueueCaseOutput> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.equal(root.get("caseId"), Integer.valueOf(caseId));
        Predicate c2 = builder.equal(root.get("jobId"), Integer.valueOf(jobId));

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, entityManager);
    }

    public void addQueueCaseOutput(QueueCaseOutput output, EntityManager entityManager) {
        hibernateFacade.add(output, entityManager);
    }

    public void removeQedOutput(QueueCaseOutput output, EntityManager entityManager) {
        hibernateFacade.remove(output, entityManager);
    }

    public String[] getAllCaseNameIDs(EntityManager entityManager) {
        List<Case> names = entityManager.createQuery(
                "SELECT obj.name from " + Case.class.getSimpleName() + " as obj ORDER BY obj.name", Case.class).getResultList();
        List<Case> ids = entityManager.createQuery(
                "SELECT obj.id from " + Case.class.getSimpleName() + " as obj ORDER BY obj.name", Case.class).getResultList();
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
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            // Get environmental variable corresponding to this name
            ParameterEnvVar envVar = getParameterEnvVar(envName, model_to_run_id, entityManager);
            if (envVar == null) {
                throw new EmfException("Could not get parameter environmental variable for " + envName);

            }
            // Get parameters corresponding to this environmental variable
            List<CaseParameter> parameters = getCaseParametersFromEnv(caseId, envVar, entityManager);
            if (parameters == null) {
                throw new EmfException("Could not get parameters for " + envName);
            }
            return parameters.toArray(new CaseParameter[0]);

        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Could not get case parameters for " + envName);

        } finally {
            if (entityManager != null) {
                entityManager.close();
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

    public List<Case> getSensitivityCases(int parentCaseId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<CasesSens> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CasesSens.class, entityManager);
        List<CasesSens> caseIds = hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("parentCaseid"), Integer.valueOf(parentCaseId)));

        List<Case> sensitivityCases = new ArrayList<Case>();

        for (Iterator<CasesSens> iter = caseIds.iterator(); iter.hasNext();)
            sensitivityCases.add(this.getCase(iter.next().getSensCaseId(), entityManager));

        return sensitivityCases;
    }

    public String[] getJobGroups(int caseId, EntityManager entityManager) {
        List<String> groups = entityManager.createQuery(
                "SELECT DISTINCT obj.jobGroup from " + CaseJob.class.getSimpleName() + " as obj WHERE obj.caseId = "
                        + caseId + " ORDER BY obj.jobGroup", String.class).getResultList();

        return groups.toArray(new String[0]);
    }

    public void checkParentChildRelationship(Case caseObj, EntityManager entityManager) throws EmfException {
        int caseId = caseObj.getId();

        List<Integer> parentCases = entityManager.createQuery(
                "SELECT obj.sensCaseId FROM " + CasesSens.class.getSimpleName() + " as obj WHERE obj.parentCaseid = "
                        + caseId + " ORDER BY obj.parentCaseid", Integer.class).getResultList();

        if (parentCases.size() == 1)
            throw new EmfException("Case " + caseObj.getName() + " is the parent case of "
                    + getCase(Integer.parseInt(parentCases.get(0).toString()), entityManager).getName() + ".");

        if (parentCases.size() > 1)
            throw new EmfException("Case " + caseObj.getName() + " is the parent case of multiple cases: "
                    + getCase(Integer.parseInt(parentCases.get(0).toString()), entityManager).getName() + ", etc.");

    }

    public void checkJobDependency(CaseJob[] jobs, EntityManager entityManager) throws EmfException {
        CriteriaBuilderQueryRoot<CaseJob> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(CaseJob.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<CaseJob> root = criteriaBuilderQueryRoot.getRoot();

        Predicate c1 = builder.isNotNull(root.get("dependentJobs"));
        Predicate c2 = builder.isNotEmpty(root.get("dependentJobs"));

        List<CaseJob> jobsDeps = hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { c1, c2 }, entityManager);

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

    public void removeChildCase(int caseId, EntityManager entityManager) {
        List<Integer> childrenCases = entityManager.createQuery(
                "SELECT obj.id FROM " + CasesSens.class.getSimpleName() + " as obj WHERE obj.sensCaseId = " + caseId
                        + " ORDER BY obj.id", Integer.class).getResultList();

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

        final EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            String hqlDelete = "DELETE FROM " + CasesSens.class.getSimpleName() + " obj WHERE " + clause;
            entityManager.createQuery(hqlDelete).executeUpdate();
            tx.commit(); 
        }
        catch (RuntimeException e) {
            tx.rollback();
            throw e;
        } finally {
            //
        }
    }

    public CaseParameter getCaseParameter(int caseId, ParameterEnvVar var, EntityManager entityManager) {
        ParameterEnvVar loadedVar = this.getParameterEnvVar(var.getName(), var.getModelToRunId(), entityManager);

        if (loadedVar == null)
            return null;

        String query = "SELECT obj.id FROM " + CaseParameter.class.getSimpleName() + " obj WHERE " + "obj.caseID = "
                + caseId + " AND obj.envVar.id = " + loadedVar.getId();
        List<Integer> ids = entityManager.createQuery(query, Integer.class).getResultList();

        if (ids == null || ids.size() == 0)
            return null;

        return getCaseParameter(Integer.parseInt(ids.get(0).toString()), entityManager);
    }

    public int[] getExternalDatasetIds(String source, EntityManager entityManager) {
        if (source == null || source.trim().isEmpty())
            return new int[0];
        // NOTE: emf.external_sources and emf.datasets tables are hardwired here, not a desirable
        // way but necessary, because ExternalSource object is not explicitly mapped through hibernate
        // Another way to access the tables is to use DbServer object.
        String queryAll = "SELECT DISTINCT ex.dataset_id FROM emf.external_sources ex WHERE ex.datasource=" + "'"
                + source.replaceAll("\\\\", "\\\\\\\\") + "'";

        String queryNonDeleted = "SELECT ds.id FROM emf.datasets ds WHERE lower(ds.status) <> 'deleted' AND ds.id IN ("
                + queryAll + ")";
        List<Integer> ids = entityManager.createNativeQuery(queryNonDeleted, Integer.class).getResultList();

        int[] dsIds = new int[ids.size()];

        for (int i = 0; i < dsIds.length; i++) {
            dsIds[i] = Integer.parseInt(ids.get(i).toString());
        }

        return dsIds;
    }
    
}