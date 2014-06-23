package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.swingworker.LightSwingWorkerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.AirQualityModel;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.EmissionsYear;
import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;
import gov.epa.emissions.framework.services.casemanagement.ModelToRun;
import gov.epa.emissions.framework.services.casemanagement.Speciation;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface CaseSummaryTabPresenter extends CaseViewerTabPresenter, LightSwingWorkerPresenter {
    
    Sector[] getAllSectors() throws EmfException;
    
    CaseCategory[] getCaseCategories() throws EmfException; 
    
    CaseCategory getCaseCategory(Object selected) throws EmfException;
    
    Abbreviation[] getAbbreviations() throws EmfException;
    
    Abbreviation getAbbreviation(Object selected) throws EmfException;
    
    Project[] getProjects() throws EmfException;
    
    Project getProject(Object selected) throws EmfException;
    
    Region[] getRegions() throws EmfException;

    ModelToRun[] getModelToRuns() throws EmfException;
    
    ModelToRun getModelToRun(Object selected) throws EmfException;
    
    AirQualityModel[] getAirQualityModels() throws EmfException;
    
    AirQualityModel getAirQualityModel(Object selected) throws EmfException;
    
    EmissionsYear[] getEmissionsYears() throws EmfException;
    
    EmissionsYear getEmissionsYear(Object selected) throws EmfException;
    
    MeteorlogicalYear[] getMeteorlogicalYears() throws EmfException;
    
    MeteorlogicalYear getMeteorlogicalYear(Object selected) throws EmfException;
    
    Speciation[] getSpeciations() throws EmfException;
    
    Speciation getSpeciation(Object selected) throws EmfException;
    
    GeoRegion[] getGrids() throws EmfException;
    
    GeoRegion getGeoregion(Object selected) throws EmfException;
    
    void refreshObjectManager();

    GeoRegion[] getAllGeoRegions() throws EmfException;
}