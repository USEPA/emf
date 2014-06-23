package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public interface NewCustomQAStepView {

    void display(EmfDataset dataset, QAProgram[] programs, Version[] versions, EditableQATabView view, EmfSession session);

    void observe(NewCustomQAStepPresenter presenter);

    QAStep save() throws EmfException;

}
