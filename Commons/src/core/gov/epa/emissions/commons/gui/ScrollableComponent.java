package gov.epa.emissions.commons.gui;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollPane;

public class ScrollableComponent extends JScrollPane {

    public ScrollableComponent(Component component) {
        super.setViewportView(component);
        super.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        super.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    public ScrollableComponent(Component component, Dimension size) {
        this(component);
        super.setPreferredSize(size);
    }

    public static ScrollableComponent createWithVerticalScrollBar(Component component) {
        ScrollableComponent scrollable = new ScrollableComponent(component);
        scrollable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollable;
    }

}
