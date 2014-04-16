package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.services.EmfException;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class RefreshButton extends Button {

    public RefreshButton(String label, int iconSize, boolean borderPainted, final RefreshObserver observer,
            String message, final MessagePanel messagePanel) {
        super(label, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                try {
                    observer.doRefresh();
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });

        super.setIcon(refreshIcon(message, iconSize));
        super.setToolTipText(message);
        super.setBorderPainted(borderPainted);
        this.setMnemonic('R');
    }

    public RefreshButton(final RefreshObserver observer, String message, final MessagePanel messagePanel) {
        this("Refresh", 24,false, observer, message, messagePanel);
    }

    private ImageIcon refreshIcon(String message, int size) {
        return new ImageResources().refresh(message, size);
    }

}
