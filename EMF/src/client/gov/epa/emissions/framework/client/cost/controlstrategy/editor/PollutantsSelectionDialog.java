package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.TargetPollutantListWidget;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class PollutantsSelectionDialog extends JDialog {

    private EditControlStrategyTabPresenter presenter;

    private TargetPollutantListWidget pollList;

    private EmfConsole parent;

    public PollutantsSelectionDialog(TargetPollutantListWidget pollList, EmfConsole parent) {
        super(parent);
        setModal(true);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        this.pollList = pollList;
        this.parent = parent;
    }

    public void display() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(pollList, BorderLayout.CENTER);
        contentPane.add(buttonPanel(), BorderLayout.SOUTH);

        setTitle("Select Pollutants");
        this.pack();
        this.setSize(400,300);
        this.setLocation(ScreenUtils.getPointToCenter(parent));
        this.setVisible(true);
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                presenter.doSetTargetPollutants(pollList.getPollutants());
                setVisible(false);
                dispose();
            }
        };
    }

    public void observe(EditControlStrategyTabPresenter presenter) {
        this.presenter = presenter;
    }
}
