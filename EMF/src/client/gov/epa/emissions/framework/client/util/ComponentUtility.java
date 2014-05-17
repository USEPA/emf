package gov.epa.emissions.framework.client.util;

import java.awt.Component;
import java.awt.Container;

public class ComponentUtility {
    public static void enableComponents(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
//            System.out.println(component.toString());
//            System.out.println(component instanceof Container);
            component.setEnabled(enable);
            if (component instanceof Container) {
                enableComponents((Container)component, enable);
            }
        }
    }
}
