package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.framework.client.ManagedView;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class WindowMenu extends JMenu implements WindowMenuView {

    private List permanentMenuItems;

    private SortedSet menuItems;

    private WindowMenuPresenter presenter;

    private Component parent;

    public WindowMenu(EmfConsole console) {
        super("Window");
        super.setName("window");
        this.parent = console;
        menuItems = new TreeSet();
        permanentMenuItems = new ArrayList();

        JMenuItem closeAllMenuItem = new JMenuItem("Close All");
        closeAllMenuItem.addActionListener(closeAll());
        permanentMenuItems.add(closeAllMenuItem);
        refreshMenuItems();
        
        JMenuItem hideAllMenuItem = new JMenuItem("Hide All");
        hideAllMenuItem.addActionListener(hideAll());
        permanentMenuItems.add(hideAllMenuItem);
        refreshMenuItems();
    }

    public void setWindowMenuViewPresenter(WindowMenuPresenter presenter) {
        this.presenter = presenter;
    }

    private ActionListener closeAll() {
        ActionListener listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (confirmCloseAll())
                    presenter.closeAll();
            }
        };
        return listener;
    }
    
    private ActionListener hideAll() {
        ActionListener listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (confirmHideAll())
                    presenter.hideAll();
            }
        };
        return listener;
    }

    protected boolean confirmCloseAll() {
        if (presenter.numberOfOpenWindows() == 0)
            return false;
        ConfirmDialog dialog = new ConfirmDialog("Do you want to close all the windows?", "Close All Confirmation",
                parent);
        return dialog.confirm();
    }
    
    protected boolean confirmHideAll() {
        if (presenter.numberOfOpenWindows() == 0)
            return false;
        return true;
    }

    public void addPermanently(ManagedView managedView) {
        JMenuItem menuItem = new WindowMenuItem(managedView);
        super.add(menuItem);
        permanentMenuItems.add(menuItem);
        refreshMenuItems();
    }

    public void register(ManagedView view) {
        JMenuItem menuItem = new WindowMenuItem(view);
        menuItems.add(menuItem);

        refreshMenuItems();

    }

    private void refreshMenuItems() {
        super.removeAll();
        addMenuItems(permanentMenuItems);
        if (!menuItems.isEmpty()) {
            super.addSeparator();
            addMenuItems(menuItems);
        }
        super.validate();
    }

    private void addMenuItems(Collection menuItems) {
        for (Iterator iter = menuItems.iterator(); iter.hasNext();) {
            super.add((JMenuItem) iter.next());
        }
    }

    public void unregister(ManagedView view) {
        JMenuItem menuItem = getMenuItem(view);
        menuItems.remove(menuItem);
        refreshMenuItems();
    }

    private JMenuItem getMenuItem(ManagedView view) {
        for (Iterator iter = menuItems.iterator(); iter.hasNext();) {
            WindowMenuItem element = (WindowMenuItem) iter.next();
            if (element.view() == view)
                return element;
        }
        return null;
    }

    public class WindowMenuItem extends JMenuItem implements Comparable {

        final private ManagedView view;

        public WindowMenuItem(ManagedView view) {
            super(view.getTitle());
            super.setName(view.getName() + "view name");
            this.view = view;

            super.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    WindowMenuItem.this.view.bringToFront();
                }
            });
        }

        private ManagedView view() {
            return view;
        }

        public int compareTo(Object menuItem) {
            String OtherName = ((WindowMenuItem) menuItem).view().getName();
            return this.view().getName().compareTo(OtherName);
        }

    }

}
