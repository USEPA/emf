package gov.epa.emissions.framework.ui;

import javax.swing.border.TitledBorder;

public class Border extends TitledBorder {

    public Border(String title) {
        super(title);
        super.setTitleJustification(TitledBorder.LEFT);
    }

}
