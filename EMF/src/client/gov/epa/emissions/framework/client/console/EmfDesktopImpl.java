package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.ManagedView;

import java.util.Arrays;
import java.util.List;

import javax.swing.JDesktopPane;

public class EmfDesktopImpl implements EmfDesktop {

    private JDesktopPane desktop;

    public EmfDesktopImpl(JDesktopPane desktop) {
        this.desktop = desktop;
    }

    public void add(ManagedView view) {
        EmfInternalFrame frame = (EmfInternalFrame) view;// unfortunate class-cast
        if (!isPresent(frame))
            desktop.add(frame);
    }

    public void ensurePresence(ManagedView view) {
        EmfInternalFrame window = (EmfInternalFrame) view;// unfortunate class-cast
        if (window.isIcon())
            return;
        if (!isPresent(window))
            desktop.add(window);
    }

    private boolean isPresent(EmfInternalFrame window) {
        if (window.isIcon())
            return true;
        List componentsList = Arrays.asList(desktop.getAllFrames());
        return componentsList.contains(window);
    }

}
