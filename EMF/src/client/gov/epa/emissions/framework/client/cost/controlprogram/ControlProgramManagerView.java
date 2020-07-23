package gov.epa.emissions.framework.client.cost.controlprogram;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.cost.ControlProgram;

public interface ControlProgramManagerView extends ManagedView {

    void display(ControlProgram[] controlPrograms) throws EmfException;

    void observe(ControlProgramManagerPresenter presenter);

    void refresh(ControlProgram[] controlPrograms) throws EmfException;

    BasicSearchFilter getSearchFilter();

}
