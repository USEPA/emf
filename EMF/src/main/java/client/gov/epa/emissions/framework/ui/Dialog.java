package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import javax.swing.JDialog;
import javax.swing.JRootPane;

public class Dialog extends JDialog {

    public Dialog(String title, EmfConsole parent) {
        super(parent);

        super.setTitle(title);
        super.setModal(true);
        super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        applyDecorations();
    }

    private void applyDecorations() {
        super.setUndecorated(true);
        super.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
    }

    public void center() {
        super.setLocation(ScreenUtils.getPointToCenter(this));
    }

    protected void close() {
        super.dispose();
    }

    public void display() {
        super.setVisible(true);
    }
}
