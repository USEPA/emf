package gov.epa.emissions.framework.ui;

import java.awt.Dimension;
import java.awt.Toolkit;

public class Dimensions {

    public Dimension getSize(double percentOfScreenWidth, double percentOfScreenHeight) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        double height = screen.getHeight();
        double width = screen.getWidth();

        Dimension dim = new Dimension();
        dim.setSize(width * percentOfScreenWidth, height * percentOfScreenHeight);

        return dim;
    }

}
