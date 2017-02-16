package gov.epa.emissions.framework.client.module;

public interface ModuleDatasetNamePatternView {

    void display();

    void observe(ModuleDatasetNamePatternPresenter presenter);

    String getDatasetNamePattern();
}