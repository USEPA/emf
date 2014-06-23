package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

public interface ControlMeasureSelectionView {

    void display(ControlMeasureTableData tableData);

    void observe(ControlMeasureSelectionPresenter presenter);
}
