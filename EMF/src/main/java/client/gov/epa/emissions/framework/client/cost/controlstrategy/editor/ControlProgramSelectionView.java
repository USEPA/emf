package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

public interface ControlProgramSelectionView {

    void display(ControlProgramTableData tableData);

    void observe(Object presenter);
}
