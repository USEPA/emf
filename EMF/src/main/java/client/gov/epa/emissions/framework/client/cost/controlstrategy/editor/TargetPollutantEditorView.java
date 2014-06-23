package gov.epa.emissions.framework.client.cost.controlstrategy.editor;


public interface TargetPollutantEditorView {

    void display();

    void observe(TargetPollutantEditorPresenter presenter, EditControlStrategyPresenter editControlStrategyPresenter);
}
