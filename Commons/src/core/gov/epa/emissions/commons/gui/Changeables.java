package gov.epa.emissions.commons.gui;

import java.util.List;

public interface Changeables {

    void add(Changeable changeable);

    void add(List changeables);

    boolean hasChanges();

    void resetChanges();

    void onChanges();

}