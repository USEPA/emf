package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.EquationType;

public interface EquationTypeSelectionView {

    void display(EquationType[] equationTypes);

    void observe(EquationTypeSelectionPresenter presenter);

    EquationType getEquationType();
}