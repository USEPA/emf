package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ChangeObserver;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.Changeables;
import gov.epa.emissions.commons.gui.DefaultChangeables;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.ui.Position;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyVetoException;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

public abstract class EmfInternalFrame extends JInternalFrame implements ManagedView, ChangeObserver, ManageChangeables {

    protected DesktopManager desktopManager;

    private ChangeObserver changeObserver;

    private Changeables changeables;

    private WidgetChangesMonitor monitor;

    public EmfInternalFrame(String title, DesktopManager desktopManager) {
        super(title, true, // resizable
                true, // closable
                true, // maximizable
                true);// iconifiable
        super.setFrameIcon(EmfImageTool.createImageIcon("/logo.JPG"));
        
        setLabel(title);
        this.desktopManager = desktopManager;
        changeables = new DefaultChangeables(this);
        monitor = new WidgetChangesMonitor(changeables, this);
        changeObserver = new DefaultChangeObserver(this);

        addWindowClosingTrap();
    }

    public boolean isAlive() {
        return false;
    }

    public void setLabel(String name) {
        super.setTitle(name);
        super.setName(name);
    }

    private void addWindowClosingTrap() {
        super.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent event) {
                windowClosing();
                super.internalFrameClosing(event);
            }
        });
    }

    public EmfInternalFrame(String title, Dimension dimension, DesktopManager desktopManager) {
        this(title, desktopManager);
        dimensions(dimension);
    }

    public void bringToFront() {
        super.toFront();
        super.moveToFront();
        try {
                super.setSelected(true);
                super.setIcon(false);
        } catch (PropertyVetoException e) {
            throw new RuntimeException("could not bring the window - " + super.getTitle() + " to front of the desktop");
        }
        super.setVisible(true);
    }

    public void display() {
        desktopManager.openWindow(this);
    }

    public Position getPosition() {
        Point point = super.getLocation();
        return new Position(point.x, point.y);
    }

    public void setPosition(Position position) {
        super.setLocation(new Point(position.x(), position.y()));
    }

    public void refreshLayout() {
        super.validate();
    }

    public void disposeView() {
        desktopManager.closeWindow(this);
    }
    
    public void windowHiding() {
        desktopManager.hideWindow(this);
    }

    protected void dimensions(Dimension size) {
        super.setSize(size);
        super.setMinimumSize(size);
    }

    protected void dimensions(int width, int height) {
        this.dimensions(new Dimension(width, height));
    }

    public void signalChanges() {
        changeObserver.signalChanges();
    }

    public void signalSaved() {
        changeObserver.signalSaved();
    }

    public int height() {
        return (int) super.getSize().getHeight();
    }

    public int width() {
        return (int) super.getSize().getWidth();
    }

    public boolean hasChanges() {
        return changeables.hasChanges();
    }

    public void resetChanges() {
        monitor.resetChanges();
    }

    public void addChangeable(Changeable changeable) {
        changeables.add(changeable);
    }

    public boolean shouldDiscardChanges() {
        return monitor.shouldDiscardChanges();
    }
    
    public boolean shouldProcessChanges(String title, String message) {
        return monitor.shouldProcessChanges(title, message);
    }

    protected void setDefaultButton(Button button) {
        getRootPane().setDefaultButton(button);
    }
    
    public void hideMe() {
        //setVisible(false);
        //setState(JFrame.ICONIFIED);
        try {
            setIcon(true);
        } catch (PropertyVetoException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
    }
}
