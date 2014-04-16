package gov.epa.emissions.framework.client;

public interface ManagedView extends EmfView {

    void bringToFront();

    String getName();

    boolean isAlive();

    boolean hasChanges();

    void resetChanges();

    boolean shouldDiscardChanges(); // FIXME: do we need this method

    boolean shouldProcessChanges(String title, String message);

    /* Should release the locks if any and call disposeView() */
    void windowClosing();
    
    void windowHiding();
}
