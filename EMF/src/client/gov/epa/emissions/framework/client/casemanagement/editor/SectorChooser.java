package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SectorChooser extends JDialog {

    private Sector[] allSectors;

    private ListWidget allSectorsListwidget;

    private ListWidget sectorsListWidget;

    public SectorChooser(Sector[] allSectors, ListWidget sectorsListWidget, EmfConsole parentConsole) {
        super(parentConsole);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        setTitle("Select Sectors");
        this.allSectors = allSectors;
        this.sectorsListWidget = sectorsListWidget;
    }

    public void display() {
        JScrollPane pane = listWidget();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(pane);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel);

        pack();
        setSize(300, 300);
        setLocation(ScreenUtils.getPointToCenter(this));
        setModal(true);
        setVisible(true);
    }

    private JScrollPane listWidget() {
        allSectorsListwidget = new ListWidget(allSectors);
        JScrollPane pane = new JScrollPane(allSectorsListwidget);
        return pane;
    }

    private JPanel buttonPanel() {
        OKButton okButton = new OKButton(okAction());
        CancelButton cancelButton = new CancelButton(cancelAction());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
            }
        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setSelectedValues();
                disposeView();
            }
        };
    }

    private void disposeView() {
        dispose();
        setVisible(false);
    }

    private void setSelectedValues() {
        Object[] values = allSectorsListwidget.getSelectedValues();
        Sector[] selectedValues = Arrays.asList(values).toArray(new Sector[0]);
        addNewSectors(selectedValues);
    }

    private void addNewSectors(Sector[] selected) {
        for (int i = 0; i < selected.length; i++) {
            if (!sectorsListWidget.contains(selected[i]))
                sectorsListWidget.addElement(selected[i]);
        }
        sort(); 
    }
    
    private void sort() {
        Sector[] sectors = Arrays.asList(sectorsListWidget.getAllElements()).toArray(new Sector[0]);
        Arrays.sort(sectors);
        sectorsListWidget.removeAllElements();
        for (int i = 0; i < sectors.length; i++) {
            sectorsListWidget.addElement(sectors[i]);
        }
    }
   
}
