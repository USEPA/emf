package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.ui.TableData;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;

public class ViewSectorWindow extends DisposableInteralFrame implements ViewableSectorView {

    private ViewableSectorPresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    public ViewSectorWindow(DesktopManager desktopManager) {
        super("View Sector", new Dimension(550, 400), desktopManager);

        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void observe(ViewableSectorPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Sector sector) {
        super.setTitle("View Sector: " + sector.getName());
        super.setName("sectorView:" + sector.getId());

        layout.removeAll();
        doLayout(layout, sector);

        super.display();
    }

    // FIXME: CRUD panel. Refactor to use in DatasetTypes Manager
    private void doLayout(JPanel layout, Sector sector) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createBasicDataPanel(sector));
        layout.add(createCriteriaPanel(sector));
        layout.add(createButtonsPanel());

        messagePanel.setMessage(lockStatus(sector));
    }

    private String lockStatus(Sector sector) {
        if (!sector.isLocked())
            return "";

        return "Locked by " + sector.getLockOwner() + " at " + CustomDateFormat.format_YYYY_MM_DD_HH_MM(sector.getLockDate());
    }

    private JPanel createBasicDataPanel(Sector sector) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        Label name = new Label("name", sector.getName());
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        TextArea description = new TextArea("description", sector.getDescription(), 40);
        description.setEditable(false);
        ScrollableComponent descTextArea = new ScrollableComponent(description);
        descTextArea.setMinimumSize(new Dimension(80, 80));
        layoutGenerator.addLabelWidgetPair("Description:", descTextArea, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createCriteriaPanel(Sector sector) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Criteria"));

        TableData tableData = new ViewableSectorCriteriaTableData(sector.getSectorCriteria());
        JTable table = new JTable(new EmfTableModel(tableData));
        table.setRowHeight(20);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(10);
        container.setLayout(layout);

        Button closeButton = new CloseButton(closeAction());
        container.add(closeButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        };

        return action;
    }

}
