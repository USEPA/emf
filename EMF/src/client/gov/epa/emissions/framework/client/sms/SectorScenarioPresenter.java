package gov.epa.emissions.framework.client.sms;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastDataset;
import gov.epa.emissions.framework.services.fast.FastService;
import gov.epa.emissions.framework.services.sms.SectorScenario;

import java.util.Calendar;

public class SectorScenarioPresenter {

    private EmfSession session;

    private SectorScenarioView view;
    
    public SectorScenarioPresenter(SectorScenarioView view, EmfSession session) {
        this.session = session;
        this.view = view;
    }

    public void display() throws Exception {
        view.observe(this);

        view.display();
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
    
    public EmfSession getSession(){
        return session; 
    }

    public EmfDataset getDatasets(int id) throws EmfException{
        return session.dataService().getDataset(id);
    }
    
    public EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException{
        return session.dataService().getDatasets(datasetType);
    }
    
    public DatasetType getDatasetType(String name) throws EmfException{
        return session.getLightDatasetType(name);
    }
    
    public Version[] getVersions(EmfDataset dataset) throws EmfException 
    {
        if (dataset == null) {
            return new Version[0];
        }
        return session.dataEditorService().getVersions(dataset.getId());
    }

    public int addSectorScenario(SectorScenario sectorScenario) throws EmfException {
        return session.sectorScenarioService().addSectorScenario(sectorScenario);
    }

    public SectorScenario getSectorScenario(User user, int sectorScenarioId) throws EmfException {
        return session.sectorScenarioService().obtainLocked(user, sectorScenarioId);
    }

    public String[] getDistinctSectorListFromDataset(int datasetId, int versionNumber) throws EmfException {
        return session.sectorScenarioService().getDistinctSectorListFromDataset(datasetId, versionNumber);
    }

    public void runSectorScenario(int sectorScenarioId) throws EmfException {
        session.sectorScenarioService().runSectorScenario(session.user(), sectorScenarioId);
    }

    public void test() throws EmfException {
        FastService fastService = session.fastService();
        

//        fastService.getFastDatasetWrappers();
//        fastService.getFastNonPointDatasets();
//        fastService.getFastRuns();
        
//        FastRun fastRun = new FastRun();
//        fastRun.setName("test " + now.toString());
//        fastRun.setAbbreviation(Calendar.getInstance().getTimeInMillis() + "");
//        fastRun.setDescription("test");
//        fastRun.setRunStatus("Not started");
//        fastRun.setLastModifiedDate(Calendar.getInstance().getTime());
//        fastRun.setCreator(session.user());
//
//        fastRun.setId(fastService.addFastRun(fastRun));
//        fastRun = fastService.obtainLockedFastRun(session.user(), fastRun.getId());
//        
//        fastRun.setCompletionDate(Calendar.getInstance().getTime());
//        fastRun.setRunStatus("Maybe i'll start if your nice");
//        fastService.updateFastRun(fastRun);
//
//        fastService.getFastRun(fastRun.getId());
//
//        //get fast run outputs
//        fastService.getFastRunOutputs(fastRun.getId());
//        System.out.println(fastService.getFastRunStatus(fastRun.getId()));
//        

        System.out.println(fastService.getFastRunRunningCount());

//        fastService.removeFastRuns(new int[] { fastRun.getId() }, session.user());

//        fastService.getGrids();
        
        //add fast dataset
        EmfDataset dataset = getDataset("ptnonipm_xportfrac_cap2005v2_20nov2008_revised_20jan2009_v0");

        FastDataset fastDataset = new FastDataset();
        fastDataset.setDataset(dataset);
        fastDataset.setAddedDate(Calendar.getInstance().getTime());
//        fastService.addFastDataset(fastDataset);
        
        

//        EmfDataset baseNonPointDataset = getDataset("nonpt_2005ao_tox_detroit_CAP_nopfc_15may2009_v0");
//        EmfDataset griddedSMKDataset = getDataset("2020ac_det_link3_nonpt_det_scc_cell_4DET1");
//        FastNonPointDataset fastNonPointDataset = new FastNonPointDataset();
//        fastNonPointDataset.setBaseNonPointDataset(baseNonPointDataset);
//        fastNonPointDataset.setBaseNonPointDatasetVersion(0);
//        fastNonPointDataset.setGriddedSMKDataset(griddedSMKDataset);
//        fastNonPointDataset.setGriddedSMKDatasetVersion(0);
//        fastNonPointDataset.setGrid(fastService.getGrid("Detroit_36_45_4km"));
//        fastNonPointDataset.setFastDataset(fastDataset);
//        fastNonPointDataset.setId(fastService.addFastNonPointDataset(fastNonPointDataset));

//        fastService.addFastNonPointDataset("newInventoryDatasetName" + "_" + CustomDateFormat.format_HHMMSSSS(new Date()), "lm_no_c3_cap2002v3", 
//                0, "2020ac_det_link3_lmb_alm_det_scc_cell_4DET1 smk", 
//                0, "invtable_cap_hg", 
//                6, "Detroit_36_45_4km", 
//                "delvecch");
//  
        

        
        
        
//        FastRun fastRun = new FastRun();
//        fastRun.setName("test " + Calendar.getInstance().getTime().toString());
//        fastRun.setAbbreviation(Calendar.getInstance().getTimeInMillis() + "");
//        fastRun.setDescription("test");
//        fastRun.setRunStatus("Not started");
//        fastRun.setLastModifiedDate(Calendar.getInstance().getTime());
//        fastRun.setCreator(session.user());
//
//        fastRun.setGrid(fastService.getGrid("Detroit_36_45_4km"));
//        
//        fastRun.setInvTableDataset(getDataset("invtable_cap_hg"));
//        fastRun.setInvTableDatasetVersion(4);
//        fastRun.setSpeciesMapppingDataset(getDataset("fast_species_mapping"));
//        fastRun.setSpeciesMapppingDatasetVersion(0);
//        fastRun.setTransferCoefficientsDataset(getDataset("transfer_coefficients"));
//        fastRun.setTransferCoefficientsDatasetVersion(0);
//        fastRun.setCancerRiskDataset(getDataset("fast_cancer_risk"));
//        fastRun.setCancerRiskDatasetVersion(0);
//        fastRun.setDomainPopulationDataset(getDataset("4km_Detroit_Pop_24nov2009"));
//        fastRun.setDomainPopulationDatasetVersion(0);
//
//        FastDataset[] fastDatasets = fastService.getFastDatasets();
//        
//        for (FastDataset fd : fastDatasets) {
//            FastRunInventory fastRunInventory = new FastRunInventory(fd.getDataset(), 0);
//            fastRun.setInventories(new FastRunInventory[] {fastRunInventory});
//        }
//        
//
//        fastRun.setId(fastService.addFastRun(fastRun));
//        
//        Grid grid = fastService.getGrid("Detroit_36_45_4km");
//        
////        fastService.runFastRun(session.user(), fastRun.getId());
//
//        FastAnalysis fastAnalysis = new FastAnalysis();
//        fastAnalysis.setName("test " + now.toString());
//        fastAnalysis.setAbbreviation(Calendar.getInstance().getTimeInMillis() + "");
//        fastAnalysis.setDescription("test");
//        fastAnalysis.setRunStatus("Not started");
//        fastAnalysis.setLastModifiedDate(Calendar.getInstance().getTime());
//        fastAnalysis.setCreator(session.user());
//        fastAnalysis.setGrid(grid);
//
//        FastRun[] fastRuns = fastService.getFastRuns(grid.getId());
//        
////        for (FastRun fr : fastRuns) {
////            FastAnalysisRun fastAnalysisRun = new FastAnalysisRun();
////            fastAnalysisRun.setFastRunId(fr.getId());
////            fastAnalysisRun.setGridId(fr.getGrid().getId());
////            fastAnalysis.setBaselineRuns(new FastAnalysisRun[] {fastAnalysisRun});
////        }
//        FastRun fr = fastService.getFastRun(27);
//        FastAnalysisRun fastAnalysisRun = FastAnalysisRun.createBaselineRun(fr);
//        fastAnalysis.setBaselineRuns(new FastAnalysisRun[] {fastAnalysisRun});
//
//        fr = fastService.getFastRun(28);
//        fastAnalysisRun = FastAnalysisRun.createSensitivityRun(fr);
//        fastAnalysis.setSensitivityRuns(new FastAnalysisRun[] {fastAnalysisRun});
//
//        fastAnalysis.setId(fastService.addFastAnalysis(fastAnalysis));
//        
//        fastService.runFastAnalysis(session.user(), fastAnalysis.getId());
        
//        fastAnalysis = fastService.obtainLockedFastAnalysis(session.user(), fastAnalysis.getId());
//        
//        fastAnalysis.setCompletionDate(Calendar.getInstance().getTime());
//        fastAnalysis.setRunStatus("Running");
//        fastService.updateFastAnalysis(fastAnalysis);
//
//        fastService.getFastAnalyses();
//        fastService.getFastAnalysis(fastAnalysis.getId());
//
//        //get fast run outputs
//        fastService.getFastAnalysisOutputs(fastAnalysis.getId());
        
//        
//        String[] pollutants = fastService.getFastRunSpeciesMappingDatasetPollutants(getDataset("fast_species_mapping").getId(), 0);
        fastService.exportFastOutputToShapeFile(fastService.getFastRunOutputs(27)[1].getOutputDataset().getId(), 0, 1, "delvecch", "C:\\temp\\temp", "NO3");
        
    }

    protected EmfDataset getDataset(String name) throws EmfException {
        return session.dataService().getDataset(name);
    }
}