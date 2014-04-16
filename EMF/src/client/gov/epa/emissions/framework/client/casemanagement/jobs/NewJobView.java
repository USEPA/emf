package gov.epa.emissions.framework.client.casemanagement.jobs;


public interface NewJobView {
    void display();

    boolean shouldCreate();

    void register(Object presenter);
}
