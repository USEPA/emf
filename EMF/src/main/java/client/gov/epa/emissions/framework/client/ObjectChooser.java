package gov.epa.emissions.framework.client;

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

public class ObjectChooser extends JDialog {

    private Object[] allObjects;

    private ListWidget allObjectsListwidget;

    private ListWidget selectedListWidget;
    
    public ObjectChooser(String typeOfObject, Object[] allObjects, ListWidget selectedList, 
            EmfConsole parentConsole) {
        super(parentConsole);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        String title = "Select ";
        
        if (typeOfObject == null)
        {
           String className = (allObjects == null || allObjects.length == 0) ? ".Object" : allObjects[0].getClass().getName();
           int dot = className.lastIndexOf('.');
           title += className.substring(dot + 1)+"s";
        }
        else
        {
           title += typeOfObject;
        }
        setTitle(title);
        this.allObjects = allObjects;
        this.selectedListWidget = selectedList;
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
        setSize(500, 300);
        setLocation(ScreenUtils.getPointToCenter(this));
        setModal(true);
        setVisible(true);
    }

    private JScrollPane listWidget() {
        allObjectsListwidget = new ListWidget(allObjects);
        JScrollPane pane = new JScrollPane(allObjectsListwidget);
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
        Object[] values = allObjectsListwidget.getSelectedValues();
        Object[] selectedValues = Arrays.asList(values).toArray(new Object[0]);
        addNewObjects(selectedValues);
    }

    private void addNewObjects(Object[] selected) {
        for (int i = 0; i < selected.length; i++) {
            if (!selectedListWidget.contains(selected[i]))
                selectedListWidget.addElement(selected[i]);
        }
    }

}
