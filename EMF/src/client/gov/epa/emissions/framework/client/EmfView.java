package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.ui.Position;

/**
 * Can contain EMF UI widgets. It's specifically introduced so that either a JFrame or a JInternalFrame can contain a
 * EMF widget.
 */
public interface EmfView {

    /* dispose the view, won't release the locks if any */
    void disposeView();

    void display();
    
    void hideMe();

    Position getPosition();

    void setPosition(Position position);

    int height();

    int width();

    String getTitle();

    void setTitle(String title);
}
