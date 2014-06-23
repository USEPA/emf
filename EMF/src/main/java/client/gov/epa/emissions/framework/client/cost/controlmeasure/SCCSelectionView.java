package gov.epa.emissions.framework.client.cost.controlmeasure;

public interface SCCSelectionView {

    void display(SCCTableData tableData);

    void observe(Object presenter);
}
