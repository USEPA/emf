package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class NewTagDialog extends JDialog implements NewTagView {

    private EmfConsole parent;
    private NewTagPresenter presenter;

    TagsObserver tagsObserver;
    
    private JTextField name;
    private JTextArea description;

    public NewTagDialog(EmfConsole parent, TagsObserver tagsObserver) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.parent = parent;
        this.tagsObserver = tagsObserver;
        setModal(true);
    }

    public void display() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(tagPanel(), BorderLayout.NORTH);
        panel.add(buttonsPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);
        
        setTitle("Create New Tag");           

        this.pack();
        this.setSize(600, 250);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    public void observe(NewTagPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel tagPanel() {

        JPanel formPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new JTextField("name", 60);
        name.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (Character.isWhitespace(e.getKeyChar()))
                    e.consume();
                if (e.getKeyChar() == ',')
                    e.consume();
            }
        });

        layoutGenerator.addLabelWidgetPair("Name:", name, formPanel);
        
        description = new TextArea("description", "", 60, 4);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setMaximumSize(new Dimension(575, 200));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, formPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(formPanel, 2, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        return formPanel;
    }
    
    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        
        Button createButton = new Button("Create", createAction());
        createButton.setMnemonic('r');
        
        Button cancelButton = new CancelButton(cancelAction());
        
        panel.add(createButton);
        panel.add(cancelButton);
        return panel;
    }

    private Action createAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String nameText = name.getText().trim();
                if (nameText.isEmpty()) { 
                    JOptionPane.showMessageDialog(parent, 
                            "Please enter the new tag name", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        presenter.addTag(nameText, description.getText().trim());
                    } catch(EmfException ex) {
                        JOptionPane.showMessageDialog(parent, 
                                "Failed to create new tag:\n\n" + ex.getMessage(), 
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (tagsObserver != null) {
                        tagsObserver.refreshTags();
                    }
                    setVisible(false);
                    dispose();
                }
            }
        };
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        };
    }
}