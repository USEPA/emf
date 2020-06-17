package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.cost.ControlProgram;

public interface ControlProgramSelectionView {

    void display(ControlProgram[] controlPrograms) throws EmfException;

    void observe(Object presenter);

    void refresh(ControlProgram[] controlPrograms);

    BasicSearchFilter getSearchFilter();

}
