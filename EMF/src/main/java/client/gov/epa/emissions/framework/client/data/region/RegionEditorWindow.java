package gov.epa.emissions.framework.client.data.region;

import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.services.data.RegionType;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class RegionEditorWindow extends DisposableInteralFrame implements RegionEditorView {

    private RegionEditorPresenter presenter;

    private MessagePanel messagePanel;

    private RegionEditorPanel regionEditorPanel;

    private GeoRegion region;

    private ListWidget caseRegions;
    
    private ListWidget regionsList;

    private EmfConsole parentConsole;

    private boolean newRegion;

    public RegionEditorWindow(ListWidget caseRegions, ListWidget listWidget, DesktopManager desktopManager, EmfConsole parentConsole) {
        super("Region Editor Window", new Dimension(500, 480), desktopManager);
        this.parentConsole = parentConsole;
        this.caseRegions = caseRegions;
        this.regionsList = listWidget;
    }

    public void display() {
        JPanel layout = null;
        layout = createLayout();
        super.getContentPane().add(layout);
        super.display();
        super.resetChanges();
        setLocation(ScreenUtils.getPointToCenter(this));
        super.setLayer(0);
    }

    private JPanel createLayout() {
        RegionType[] types = new RegionType[0];
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);

        try {
            types = presenter.getAllRegionTypes();
        } catch (Exception e) {
            messagePanel.setError("Error retrieving region types: " + e.getMessage());
        }

        this.regionEditorPanel = new RegionEditorPanel(this, region, types);
        panel.add(regionEditorPanel.createPanel());
        panel.add(buttonPanel());
        return panel;
    }

    private JPanel buttonPanel() {
        OKButton okButton = new OKButton(okAction());
        CancelButton cancelButton = new CancelButton(cancelAction());

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 20, 1));

        return buttonPanel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (RegionEditorWindow.this.hasChanges()) {
                    int option = JOptionPane.showConfirmDialog(parentConsole,
                            "Would you like to discard the changes and close the editor?",
                            "Region Parameters' Value Changed", JOptionPane.YES_NO_OPTION);

                    if (option == JOptionPane.NO_OPTION)
                        return;
                }

                disposeView();
            }
        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    regionEditorPanel.setFields();
                    region = presenter.saveRegion(region, newRegion);
                    
                    if (newRegion)
                        addNewGrids(region);
                    else
                        reloadAllRegions();
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                    return;
                }

                disposeView();
            }
        };
    }

    private void reloadAllRegions() throws EmfException {
        sort(presenter.getAllRegions(), regionsList);
        
        if (caseRegions.contains(region)) {
            Object[] objs = caseRegions.getAllElements();
           
            for (int i = 0; i < objs.length; i++)
                if (((GeoRegion)objs[i]).getId() == region.getId())
                    objs[i] = region;
            
            sort(Arrays.asList(objs).toArray(new GeoRegion[0]), caseRegions);
        }
    }

    public void observe(RegionEditorPresenter presenter) {
        this.presenter = presenter;
    }

    public void setRegion(GeoRegion region, boolean isNew) throws EmfException {
        if (!isNew)
            this.region = presenter.obtainLock(region);
        else
            this.region = region;

        this.newRegion = isNew;
    }

    private void addNewGrids(GeoRegion newRegion) {
        if (!caseRegions.contains(newRegion))
            caseRegions.addElement(newRegion);
        
        sort(Arrays.asList(caseRegions.getAllElements()).toArray(new GeoRegion[0]), caseRegions);
        presenter.refreshParentRegionCash();
    }

    private void sort(GeoRegion[] grids, ListWidget list) {
        Arrays.sort(grids);
        list.removeAllElements();

        for (int i = 0; i < grids.length; i++) {
            list.addElement(grids[i]);
        }
    }

}
