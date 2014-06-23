package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.AddButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.ListWidget;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class AddRemoveWidget extends JPanel {

    private ListWidget objectsList;

    private Object[] allObjects;

    private EmfConsole parentConsole;
    
    private Button addButton;
    private Button removeButton;
    private String topic ="Jobs"; 

    public AddRemoveWidget(Object[] allObjects, ManageChangeables changeables, EmfConsole parentConsole) {
        this(allObjects, changeables, parentConsole, true, true);
        
    }
    
    public AddRemoveWidget(Object[] allObjects, ManageChangeables changeables, EmfConsole parentConsole, String topic) {
        this(allObjects, changeables, parentConsole, true, true);
        this.topic = topic;
    }
    
    public AddRemoveWidget(Object[] allObjects, ManageChangeables changeables, EmfConsole parentConsole, 
            boolean horizontalBar, boolean verticalBar) {
        this.allObjects = allObjects;
        this.parentConsole = parentConsole;
        setupLayout(changeables, horizontalBar, verticalBar);

    }

    public void setObjects(Object[] objects) {
        for (int i = 0; i < objects.length; i++) {
            objectsList.addElement(objects[i]);
        }
    }

    public Object[] getObjects() {
        return Arrays.asList(objectsList.getAllElements()).toArray(new Object[0]);
    }

    private void setupLayout(ManageChangeables changeables, boolean horizontalBar, boolean verticalBar) {
        this.objectsList = new ListWidget(new Object[0]);
        final JPanel container = this;
        this.objectsList.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent event) {
                if (objectsList.getSelectedValue()==null)
                    return; 
                container.setToolTipText(objectsList.getSelectedValue().toString());
            }

            public void mouseEntered(MouseEvent arg0) {
                // NOTE Auto-generated method stub
                
            }

            public void mouseExited(MouseEvent arg0) {
                // NOTE Auto-generated method stub
                
            }

            public void mousePressed(MouseEvent arg0) {
                // NOTE Auto-generated method stub
                
            }

            public void mouseReleased(MouseEvent arg0) {
                // NOTE Auto-generated method stub
                
            }
        });
        changeables.addChangeable(objectsList);
        
        JScrollPane pane = new JScrollPane(objectsList);
        pane.setHorizontalScrollBarPolicy(horizontalBar ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED 
                : ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setVerticalScrollBarPolicy(verticalBar ? ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED 
                : ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        JPanel buttonPanel = addRemoveButtonPanel();

        this.setLayout(new BorderLayout(1, 1));
        this.add(pane);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel addRemoveButtonPanel() {
        JPanel panel = new JPanel();
        addButton = new AddButton("Add", addAction());
        removeButton = new RemoveButton("Remove", removeAction());
        addButton.setMargin(new Insets(1, 2, 1, 2));      
        removeButton.setMargin(new Insets(1, 2, 1, 2));
        panel.add(addButton);
        panel.add(removeButton);

        return panel;
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeObjects();
            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addObjects();
            }
        };
    }

    private void addObjects() {
        ObjectChooser objectSelector = new ObjectChooser(topic, allObjects, objectsList, parentConsole);
        objectSelector.display();
    }

    private void removeObjects() {
        Object[] removeValues = objectsList.getSelectedValues();
        objectsList.removeElements(removeValues);

    }

    public void viewOnly() {
        addButton.setVisible(false);
        removeButton.setVisible(false);  
    }
}
