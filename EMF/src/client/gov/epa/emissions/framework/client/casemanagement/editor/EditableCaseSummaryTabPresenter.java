package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface EditableCaseSummaryTabPresenter extends CaseEditorTabPresenter {
    void addSector(Sector sector);

    void addRegion(GeoRegion region);
}
