package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

public interface PollutantSelectionView {

    void display(PollutantTableData tableData);

    void observe(PollutantSelectionPresenter presenter);
}
