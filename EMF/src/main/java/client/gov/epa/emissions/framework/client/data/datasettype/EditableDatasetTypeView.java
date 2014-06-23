package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.client.ManagedView;

public interface EditableDatasetTypeView extends ManagedView {

    void observe(EditableDatasetTypePresenter presenter);

    void display(DatasetType type, QAProgram[] programs, Keyword[] masterKeywords);

    void disposeView();

}
