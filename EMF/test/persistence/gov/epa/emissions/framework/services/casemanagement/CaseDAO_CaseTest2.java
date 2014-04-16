package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SectorCriteria;
import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.DependentJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobMessage;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class CaseDAO_CaseTest2 extends ServiceTestCase {

    private CaseDAO dao;

    private UserServiceImpl userService;

    private CaseService service;

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = sessionFactory(configFile());
        dao = new CaseDAO();
        service = new CaseServiceImpl(sessionFactory, dbServerFactory);
        userService = new UserServiceImpl(sessionFactory);
    }

    protected void doTearDown() {// no op
    }

    public void testShouldSaveJobMessages() {
        Case caseObj = load(newCase());
        CaseJob job = load(newCaseJob(caseObj));
        
        JobMessage msg = new JobMessage();
        msg.setCaseId(caseObj.getId());
        msg.setJobId(job.getId());
        msg.setMessage("Test persistance of JobMessage object.");
        
        session.clear();
        try {
            dao.add(msg);
            JobMessage msgRetrieved = dao.getJobMessages(caseObj.getId(), session).get(0);
            
            assertEquals(msg.getMessage(), msgRetrieved.getMessage());
            assertEquals(caseObj.getId(), msgRetrieved.getCaseId());
            assertEquals(job.getId(), msgRetrieved.getJobId());
        } finally {
            remove(msg);
            remove(job);
            remove(caseObj);
        }
    }

    public void testShouldDeepCopyACaseWithAllAttributesFilled() throws Exception {
        String caseToCopyName = "test" + Math.random();
        Case toCopy = new Case(caseToCopyName);

        Abbreviation abbr = new Abbreviation();
        abbr.setName("test" + Math.random());
        add(abbr);

        ModelToRun model2Run = new ModelToRun();
        model2Run.setName("test" + Math.random());
        add(model2Run);

        AirQualityModel airModel = new AirQualityModel();
        airModel.setName("test" + Math.random());
        add(airModel);

        CaseCategory cat = new CaseCategory();
        cat.setName("test" + Math.random());
        add(cat);

        EmissionsYear emisYear = new EmissionsYear();
        emisYear.setName("test" + Math.random());
        add(emisYear);

        GeoRegion grid = new GeoRegion();
        grid.setName("test" + Math.random());
        add(grid);

        MeteorlogicalYear metYear = new MeteorlogicalYear();
        metYear.setName("test" + Math.random());
        add(metYear);

        Speciation spec = new Speciation();
        spec.setName("test" + Math.random());
        add(spec);

        Project proj = new Project();
        proj.setName("test" + Math.random());
        add(proj);

        Mutex lock = new Mutex();
        lock.setLockDate(new Date());
        lock.setLockOwner("emf");

        Region modRegion = new Region();
        modRegion.setName("test" + Math.random());
        add(modRegion);

        Region contrlRegion = new Region();
        contrlRegion.setName("test" + Math.random());
        add(contrlRegion);

        User owner = userService.getUser("emf");

        SectorCriteria crit = new SectorCriteria();
        crit.setCriteria("new rule");
        crit.setType("new type");

        List<SectorCriteria> criteriaList = new ArrayList<SectorCriteria>();
        criteriaList.add(crit);

        SectorCriteria[] criteriaArray = new SectorCriteria[] { crit };

        Sector dssector = new Sector();
        dssector.setDescription("description");
        dssector.setLockDate(new Date());
        dssector.setLockOwner("emf");
        dssector.setName("test" + Math.random());
        dssector.setSectorCriteria(criteriaList);
        dssector.setSectorCriteria(criteriaArray);
        add(dssector);

        Sector casesector = new Sector();
        casesector.setDescription("description");
        casesector.setLockDate(new Date());
        casesector.setLockOwner("emf");
        casesector.setName("test" + Math.random());
        casesector.setSectorCriteria(criteriaList);
        casesector.setSectorCriteria(criteriaArray);
        add(casesector);

        IntendedUse use = new IntendedUse();
        use.setName("test" + Math.random());
        add(use);

        Country country = new Country();
        country.setName("test" + Math.random());
        add(country);

        Keyword keyword = new Keyword();
        keyword.setName("test" + Math.random());
        add(keyword);

        KeyVal keyval = new KeyVal();
        keyval.setKeyword(keyword);
        keyval.setListindex(0);
        keyval.setValue("test string");

        QAProgram qaprog = new QAProgram();
        qaprog.setName("test" + Math.random());
        qaprog.setRunClassName("run class name");
        add(qaprog);

        QAStepTemplate qatemplate = new QAStepTemplate();
        qatemplate.setDescription("description");
        qatemplate.setListIndex(0);
        qatemplate.setName("test" + Math.random());
        qatemplate.setOrder(0);
        qatemplate.setProgram(qaprog);
        qatemplate.setProgramArguments("args");
        qatemplate.setRequired(true);

        DatasetType dstype = loadDatasetType(DatasetType.orlNonpointInventory);

        InternalSource internalSrc = new InternalSource();
        internalSrc.setCols(new String[] { "col one" });
        internalSrc.setColsList("col list");
        internalSrc.setListindex(0);
        internalSrc.setSource("source");
        internalSrc.setSourceSize(123);
        internalSrc.setTable("table");
        internalSrc.setType("new type");

        ExternalSource externalSrc = new ExternalSource();
        externalSrc.setDatasource("data source");
        externalSrc.setListindex(0);

        EmfDataset dataset = new EmfDataset();
        dataset.setAccessedDateTime(new Date());
        dataset.setCountry(country);
        dataset.setCreatedDateTime(new Date());
        dataset.setCreator("emf");
        dataset.setDatasetType(dstype);
        dataset.setDefaultVersion(0);
        dataset.setDescription("description");
        dataset.setIntendedUse(use);
        dataset.setInternalSources(new InternalSource[] { internalSrc });
        dataset.setKeyVals(new KeyVal[] { keyval });
        dataset.setLockDate(new Date());
        dataset.setLockOwner("emf");
        dataset.setModifiedDateTime(new Date());
        dataset.setName("test" + Math.random());
        dataset.setProject(proj);
        dataset.setRegion(modRegion);
        dataset.setSectors(new Sector[] { dssector });
        dataset.setStartDateTime(new Date());
        dataset.setStatus("copied");
        dataset.setStopDateTime(new Date());
        dataset.setSummarySource(internalSrc);
        dataset.setTemporalResolution("resolutation");
        dataset.setUnits("unit ton");
        dataset.setYear(1999);
        add(dataset);

        EmfDataset reloadedDS = (EmfDataset)load(EmfDataset.class, dataset.getName());
        externalSrc.setDatasetId(reloadedDS.getId());
        add(externalSrc);
        
        InputEnvtVar envtVar = new InputEnvtVar();
        envtVar.setName("test" + Math.random());
        add(envtVar);

        Version version = new Version();
        version.setCreator(owner);
        version.setDatasetId(0);
        version.setFinalVersion(true);
        version.setLastModifiedDate(new Date());
        version.setLockDate(new Date());
        version.setLockOwner("emf");
        version.setName("test" + Math.random());
        version.setPath("path|path|path");
        version.setVersion(0);
        add(version);

        InputName inputName = new InputName();
        inputName.setName("test" + Math.random());
        add(inputName);

        CaseProgram caseProg = new CaseProgram();
        caseProg.setName("test" + Math.random());
        add(caseProg);

        toCopy.setAbbreviation(abbr);
        toCopy.setAirQualityModel(airModel);
        toCopy.setBaseYear(1999);
        toCopy.setCaseCategory(cat);
        toCopy.setCaseTemplate(true);
        toCopy.setControlRegion(contrlRegion);
        toCopy.setCreator(owner);
        toCopy.setDescription("Description");
        toCopy.setEmissionsYear(emisYear);
        toCopy.setEndDate(new Date());
        toCopy.setFutureYear(2005);
        toCopy.setGridDescription("grid description");
        toCopy.setInputFileDir("input/file/dir");
        toCopy.setIsFinal(true);
        toCopy.setLastModifiedBy(owner);
        toCopy.setLastModifiedDate(new Date());
        toCopy.setLock(lock);
        toCopy.setLockDate(new Date());
        toCopy.setLockOwner("emf");
        toCopy.setMeteorlogicalYear(metYear);
        toCopy.setModel(model2Run);
        toCopy.setModelingRegion(modRegion);
        toCopy.setNumEmissionsLayers(1);
        toCopy.setNumMetLayers(1);
        toCopy.setOutputFileDir("output/file/dir");
        toCopy.setProject(proj);
        toCopy.setRunStatus("copy");
        toCopy.setSectors(new Sector[] { casesector });
        toCopy.setSpeciation(spec);
        toCopy.setStartDate(new Date());
        toCopy.setTemplateUsed("orig");
        add(toCopy);

        CaseInput input = new CaseInput();
        input.setDataset(dataset);
        input.setDatasetType(dstype);
        input.setEnvtVars(envtVar);
        input.setInputName(inputName);
        input.setProgram(caseProg);
        input.setRequired(true);
        input.setSector(casesector);
        input.setLocal(true);
        input.setVersion(version);
        input.setCaseID(toCopy.getId());
        add(input);

        Case loaded = load(toCopy);

        //Case coppied = null;

        try {
            service.copyCaseObject(new int[] { loaded.getId() }, owner);
//            assertTrue(coppied.getName().startsWith("Copy of " + caseToCopyName));
//            assertTrue(coppied.getIsFinal());
        } finally {
            dropAll(CaseInput.class);
            dropAll(Case.class);
            remove(casesector);
            remove(abbr);
            remove(model2Run);
            remove(airModel);
            remove(cat);
            remove(emisYear);
            remove(grid);
            remove(metYear);
            remove(spec);
            dropAll(InternalSource.class);
            dropAll(ExternalSource.class);
            dropAll(KeyVal.class);
            dropAll(QAStepTemplate.class);
            dropAll(EmfDataset.class);
            dropAll(Country.class);
            remove(dssector);
            dropAll(Region.class);
            dropAll(IntendedUse.class);
            dropAll(Project.class);
            remove(qaprog);
            dropAll(Keyword.class);
            dropAll(InputEnvtVar.class);
            dropAll(Version.class);
            dropAll(InputName.class);
            dropAll(CaseProgram.class);
        }
    }
    
    public void testShouldSaveJobDependencies() {
        Case caseObj = load(newCase());
        CaseJob jobA = load(newCaseJob(caseObj));
        CaseJob jobB = load(newCaseJob(caseObj));
        CaseJob jobC = load(newCaseJob(caseObj));
        DependentJob dependJobA = new DependentJob();
        dependJobA.setJobId(jobA.getId());
        DependentJob dependJobB = new DependentJob();
        dependJobB.setJobId(jobB.getId());
        DependentJob dependJobC = new DependentJob();
        dependJobC.setJobId(jobC.getId());
        CaseJob jobD = load(newCaseJob(caseObj, new DependentJob[]{dependJobA, dependJobB, dependJobC}));
        
        session.clear();
        try {
            DependentJob[] dependents = jobD.getDependentJobs();
            assertEquals(jobA.getId(), dependents[0].getJobId());
            assertEquals(jobB.getId(), dependents[1].getJobId());
            assertEquals(jobC.getId(), dependents[2].getJobId());
        } finally {
            remove(jobD);
            remove(jobA);
            remove(jobB);
            remove(jobC);
            remove(caseObj);
        }
    }

    private Case newCase() {
        Case element = new Case("test" + Math.random());
        add(element);

        return element;
    }

    private CaseJob newCaseJob(Case caseObj) {
        CaseJob job = new CaseJob("test" + Math.random());
        job.setCaseId(caseObj.getId());
        add(job);
        
        return job;
    }

    private CaseJob newCaseJob(Case caseObj, DependentJob[] dependJobIds) {
        CaseJob element = new CaseJob("test" + Math.random());
        element.setCaseId(caseObj.getId());
        element.setDependentJobs(dependJobIds);
        add(element);
        
        return element;
    }

    private Case load(Case caseObj) {
        Transaction tx = null;

        session.clear();
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(Case.class).add(Restrictions.eq("name", caseObj.getName()));
            tx.commit();

            return (Case) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private CaseJob load(CaseJob job) {
        Transaction tx = null;
        
        session.clear();
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(CaseJob.class).add(Restrictions.eq("name", job.getName()));
            tx.commit();
            
            return (CaseJob) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
    
    private DatasetType loadDatasetType(String name) {
        Transaction tx = null;
        
        session.clear();
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(DatasetType.class).add(Restrictions.eq("name", name));
            tx.commit();
            
            return (DatasetType) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
}
