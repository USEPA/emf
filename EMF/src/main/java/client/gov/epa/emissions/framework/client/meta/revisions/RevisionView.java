package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;

public interface RevisionView {
    void display(Revision revision, EmfDataset dataset);
}
