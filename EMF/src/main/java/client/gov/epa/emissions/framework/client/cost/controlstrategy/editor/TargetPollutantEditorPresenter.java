package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyTargetPollutant;

public class TargetPollutantEditorPresenter {

    private TargetPollutantEditorView view;
    private EditControlStrategyPresenter editControlStrategyPresenter;
    private EditControlStrategyConstraintsTabPresenter parentPresenter;
    
    public TargetPollutantEditorPresenter(EditControlStrategyConstraintsTabPresenter parentPresenter, TargetPollutantEditorView view, 
            EmfSession session, EditControlStrategyPresenter editControlStrategyPresenter) {
        this.parentPresenter = parentPresenter;
        this.view = view;
        this.editControlStrategyPresenter = editControlStrategyPresenter;
    }

    public void display() throws Exception {
        view.observe(this, editControlStrategyPresenter);
        view.display();

    }

    public void doEdit(ControlStrategyTargetPollutant controlStrategyTargetPollutant) {
        this.parentPresenter.doSetTargetPollutant(controlStrategyTargetPollutant);
    }
}
