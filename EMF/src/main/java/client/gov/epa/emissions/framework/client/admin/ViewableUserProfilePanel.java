package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.ui.Border;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewableUserProfilePanel extends JPanel {

    private MessagePanel messagePanel;

    private User user;

    public ViewableUserProfilePanel(User user, Action closeAction) {
        this.user = user;
        createLayout(user, closeAction);
    }

    private void createLayout(User user, Action closeAction) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        this.add(messagePanel);
        this.add(createProfilePanel(user));

        this.add(createLoginPanel());
        this.add(createButtonsPanel(closeAction));
    }

    private JPanel createButtonsPanel(Action closeAction) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button closeButton = new CloseButton(closeAction);
        container.add(closeButton);

        panel.add(container, BorderLayout.EAST);

        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(new Border("Login"));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Username", new JLabel(user.getUsername()), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createProfilePanel(User user) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new Border("Profile"));

        panel.add(createManadatoryProfilePanel(user));
        panel.add(createOptionsPanel(user));

        return panel;
    }

    private JPanel createOptionsPanel(User user) {
        JPanel optionsPanel = new JPanel();

        JCheckBox isAdmin = new JCheckBox("Is Admin?");
        isAdmin.setSelected(user.isAdmin());
        isAdmin.setEnabled(false);
        optionsPanel.add(isAdmin);

        return optionsPanel;
    }

    private JPanel createManadatoryProfilePanel(User user) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Name", new JLabel(user.getName()), panel);
        layoutGenerator.addLabelWidgetPair("Affiliation", new JLabel(user.getAffiliation()), panel);
        layoutGenerator.addLabelWidgetPair("Phone", new JLabel(user.getPhone()), panel);
        layoutGenerator.addLabelWidgetPair("Email", new JLabel(user.getEmail()), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

}
