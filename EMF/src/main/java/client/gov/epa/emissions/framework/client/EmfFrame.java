package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.ui.Position;

import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JRootPane;

public abstract class EmfFrame extends JFrame implements EmfView {

    public EmfFrame(String name, String title) {
        super(title);
        super.setName(name);
        super.setUndecorated(true);
        super.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        super.setResizable(false);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                EmfFrame.this.windowClosing();
                super.windowClosing(event);
            }
        });
    }

    protected void windowClosing() {// needs to be overridden, if needed
        super.dispose();
    }

    public Position getPosition() {
        Point point = super.getLocation();
        return new Position(point.x, point.y);
    }

    public void setPosition(Position position) {
        super.setLocation(new Point(position.x(), position.y()));
    }

    final public void disposeView() {
        super.dispose();
    }

    public void display() {
        this.setVisible(true);
    }

    public void refreshLayout() {
        super.validate();
    }

    public int height() {
        return (int) super.getSize().getHeight();
    }

    public int width() {
        return (int) super.getSize().getWidth();
    }

    protected void setDefaultButton(Button button) {
        getRootPane().setDefaultButton(button);
    }
    
    public void hideMe() {
        //setVisible(false);
        setState(JFrame.ICONIFIED);
    }
}
