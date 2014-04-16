package gov.epa.emissions.framework.client.fast.datasets.view;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.datasets.AbstractFastDatasetWindow;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastDatasetWrapper;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FastNonPointDatasetViewWindow extends AbstractFastDatasetWindow {

    public FastNonPointDatasetViewWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {

        super("View Fast Non-Point Dataset", desktopManager, session, parentConsole);

        this.dimensions(650, 240);
        this.setResizable(false);
    }

    protected void doLayout(FastDatasetWrapper wrapper) {

        Container contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.getMessagePanel(), BorderLayout.PAGE_START);
        contentPane.add(createMiddlePane(wrapper));
        contentPane.add(createButtonsPanel(), BorderLayout.PAGE_END);
    }

    public void refresh(FastDatasetWrapper wrapper) {
        //
    }
    
    private JPanel createMiddlePane(FastDatasetWrapper wrapper) {

        JPanel panel = new JPanel(new GridBagLayout());

        Insets labelInsets = new Insets(8, 10, 8, 5);
        Insets valueInsets = new Insets(0, 0, 0, 0);
        Dimension fieldSize = new Dimension(500, 24);

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = labelInsets;

        JLabel smokeLabel = new JLabel("SMOKE Dataset:");
        panel.add(smokeLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = valueInsets;

        Label smokeDatasetLabel = new Label(wrapper.getGriddedSMOKEName());
        smokeDatasetLabel.setPreferredSize(fieldSize);
        panel.add(smokeDatasetLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = labelInsets;

        JLabel baseLabel = new JLabel("Non-Point Dataset:");
        panel.add(baseLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = valueInsets;

        Label nonPointDatasetLabel = new Label(wrapper.getBaseNonPointName());
        nonPointDatasetLabel.setPreferredSize(fieldSize);
        panel.add(nonPointDatasetLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = labelInsets;

        JLabel gridLabel = new JLabel("Grid:");
        panel.add(gridLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = valueInsets;

        Label gridNameLabel = new Label(wrapper.getGridName());
        gridNameLabel.setPreferredSize(fieldSize);
        panel.add(gridNameLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.NORTH;

        JLabel emptyLabel = new JLabel();
        emptyLabel.setOpaque(false);

        panel.add(emptyLabel, constraints);

        return panel;
    }

    @Override
    protected boolean showSave() {
        return false;
    }

    public void save(FastDatasetWrapper wrapper) {
        /*
         * no-op
         */
    }

    public void refresh(FastAnalysis analysis) {
        /*
         * no-op
         */
    }
}