package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface EditableCaseSummaryTabPresenter extends CaseSummaryTabPresenter, CaseEditorTabPresenter{
    
    String[] isGeoRegionUsed(GeoRegion[] grids)throws EmfException;

}
