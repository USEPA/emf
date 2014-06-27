package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseObjectManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.AirQualityModel;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.EmissionsYear;
import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;
import gov.epa.emissions.framework.services.casemanagement.ModelToRun;
import gov.epa.emissions.framework.services.casemanagement.Speciation;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.util.Date;

public class EditableCaseSummaryTabPresenterImpl  implements EditableCaseSummaryTabPresenter {

    private EditableCaseSummaryTabView view;
    protected EmfSession session;
    protected Case caseObj;
    protected CaseObjectManager caseObjectManager;

    public EditableCaseSummaryTabPresenterImpl(EmfSession session, Case caseObj, EditableCaseSummaryTabView view) {
        this(session, caseObj);
        this.view = view;
        
    }

    public EditableCaseSummaryTabPresenterImpl(EmfSession session, Case caseObj) {
        this.caseObj = caseObj;
        this.session = session;
        this.caseObjectManager = CaseObjectManager.getCaseObjectManager(session);
    }

    public void doSave() throws EmfException {
        caseObj.setLastModifiedDate(new Date());
        view.save();
    }
    
    
    public String[] isGeoRegionUsed(GeoRegion[] grids)throws EmfException {
        Integer caseId = caseObj.getId();
        if ( (caseId ==0) || (grids == null) ){
            throw new EmfException("Incorrect case Id or georegion values. ");
        }       
         return caseObjectManager.isGeoRegionUsed(caseId, grids);
    }

    @Override
    public String isGeoRegionInSummary(int selectedCaseId, GeoRegion[] georegions) throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public Object[] refreshProcessData() throws EmfException {
        refreshObjectManager(); 
        checkIfLockedByCurrentUser();
        return new Case[]{this.caseObj};
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.refresh(this.caseObj);
    }

    @Override
    public Object[] swProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void swDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public void checkIfLockedByCurrentUser() throws EmfException{
        Case reloaded = session.caseService().reloadCase(caseObj.getId());

        if (reloaded.isLocked() && !reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current case object expired. User " + reloaded.getLockOwner()
                    + " has it now.");

        if (!reloaded.isLocked())
            reloaded = session.caseService().obtainLocked(session.user(), caseObj);

        if (reloaded == null)
            throw new EmfException("Acquire lock on case failed. Please exit editing the case.");
    }

    @Override
    public Object[] saveProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void saveDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }
    
 
    public Case getCaseObj() {
        return this.caseObj;
    }
    
    public Sector[] getAllSectors() throws EmfException {
        return caseObjectManager.getSectors();
    }
    
    public CaseCategory[] getCaseCategories() throws EmfException {
        return caseObjectManager.getCaseCategories();
    }
    
    public CaseCategory getCaseCategory(Object selected) throws EmfException {
        return caseObjectManager.getOrAddCaseCategory(selected);
    }
    
    public Abbreviation[] getAbbreviations() throws EmfException {
        return caseObjectManager.getAbbreviations();
    }
    
    public Abbreviation getAbbreviation(Object selected) throws EmfException {
        return caseObjectManager.getOrAddAbbreviation(selected);
    }
    
    public Project[] getProjects() throws EmfException {
        return caseObjectManager.getProjects();
    }
    
    public Project getProject(Object selected) throws EmfException {
        return caseObjectManager.getOrAddProject(selected);
    }
    
    public Region[] getRegions() throws EmfException {
        return caseObjectManager.getRegions();
    }

    public ModelToRun[] getModelToRuns() throws EmfException {
        return caseObjectManager.getModelToRuns();
    }
    
    public ModelToRun getModelToRun(Object selected) throws EmfException {
        return caseObjectManager.getOrAddModelToRun(selected);
    }
    
    public AirQualityModel[] getAirQualityModels() throws EmfException {
        return caseObjectManager.getAirQualityModels();
    }
    
    public AirQualityModel getAirQualityModel(Object selected) throws EmfException {
        return caseObjectManager.getOrAddAirQualityModel(selected);
    }
    
    public EmissionsYear[] getEmissionsYears() throws EmfException {
        return caseObjectManager.getEmissionsYears();
    }
    
    public EmissionsYear getEmissionsYear(Object selected) throws EmfException {
        return caseObjectManager.getOrAddEmissionsYear(selected);
    }
    
    public MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException {
        return caseObjectManager.getMeteorlogicalYears();
    }
    
    public MeteorlogicalYear getMeteorlogicalYear(Object selected) throws EmfException {
        return caseObjectManager.getOrAddMeteorlogicalYear(selected);
    }
    
    public Speciation[] getSpeciations() throws EmfException {
        return caseObjectManager.getSpeciations();
    }
    
    public Speciation getSpeciation(Object selected) throws EmfException {
        return caseObjectManager.getOrAddSpeciation(selected);
    }
    
    public GeoRegion[] getGrids() throws EmfException {
        return caseObjectManager.getGeoRegions();
    }
    
    public GeoRegion getGeoregion(Object selected) throws EmfException {
        return caseObjectManager.getOrAddGrid(selected);
    }
    
    public void refreshObjectManager() {
        caseObjectManager.refresh();
    }

    public GeoRegion[] getAllGeoRegions() throws EmfException {
        return caseObjectManager.getGeoRegions();
    }

}
