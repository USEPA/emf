package gov.epa.emissions.framework.services.casemanagement;

import java.util.Random;

import org.hibernate.Session;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class CaseAssistanceServiceTest extends ServiceTestCase {

    private CaseAssistanceService service;

    private UserServiceImpl userService;

    private HibernateSessionFactory sessionFactory;
    
    private CaseDAO dao;
    
    private User user;

    protected void doSetUp() throws Exception {
        sessionFactory = sessionFactory(configFile());
        service = new CaseAssistanceService(sessionFactory);
        userService = new UserServiceImpl(sessionFactory);
        user = userService.getUser("emf");
        dao = new CaseDAO();
    }

    protected void doTearDown() throws Exception {
        dropAll(CaseParameter.class);
        dropAll(CaseInput.class);
        dropAll(CaseJob.class);
        dropAll(Case.class);
        dropAll(Abbreviation.class);
        dropAll(MeteorlogicalYear.class);
        dropAll(Project.class);
        dropAll(Region.class);
        dropAll(GeoRegion.class);
        dropAll(AirQualityModel.class);
        dropAll(Speciation.class);
        dropAll(CaseCategory.class);
        
        service = null;
        userService = null;
        sessionFactory = null;
        System.gc();
    }

    public void testShouldImportACase() throws Exception {
        String folder = "test/data/case-management";
        String inputsFile = "2002ac V3 CAP for EMF training_2002acT_Inputs.csv";
        String jobsFile = "2002ac V3 CAP for EMF training_2002acT_Jobs.csv";
        String sumParamsFile = "2002ac V3 CAP for EMF training_2002acT_Summary_Parameters.csv";
        
        try {
            service.importCase(folder, new String[] {sumParamsFile, inputsFile, jobsFile}, user);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            doTearDown();
        }
    }
    
    public void testLoadCMAQCase() throws Exception {
        String folder = "test/data/case-management";
        String inputsFile = "CMAQ4_7_CCTM_template_cmaq4_7_cctm__Inputs.csv";
        String jobsFile = "CMAQ4_7_CCTM_template_cmaq4_7_cctm__Jobs.csv";
        String sumParamsFile = "CMAQ4_7_CCTM_template_cmaq4_7_cctm__Summary_Parameters.csv";
        String logfile = folder + "/cctm_variables_log_setenv_M2b_06emisv2soa_12km_wrf2_20060729.txt";
        String caseName = "CMAQ4.7 CCTM template";
        
        CaseJob job = new CaseJob("Test" + Math.abs(new Random().nextInt()));
        job.setUser(user);
        
        Session session = sessionFactory.getSession();
        
        try {
            //Import a case
            service.importCase(folder, new String[] {sumParamsFile, inputsFile, jobsFile}, user);
            
            //Obtain a lock on case
            Case caseObj = dao.getCaseFromName(caseName, session);
            dao.obtainLocked(user, caseObj, session);
            
            //Get a sector
            Sector sec = (Sector)dao.load(Sector.class, "chemtrans", session);
            
            job.setCaseId(caseObj.getId());
            job.setSector(sec);
            dao.add(job, session);
            CaseJob loaded = (CaseJob)dao.load(CaseJob.class, job.getName(), session);
            
            //Load summary/inputs/parameters into the template case
            service.loadCMAQCase(logfile, loaded.getId(), caseObj.getId(), user);
            dao.releaseLocked(user, caseObj, session);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            doTearDown();
        }
    }
}
