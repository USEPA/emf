package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.module.Tag;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class AddTagsDialog extends JDialog implements AddTagsView, TagsObserver {

    private EmfConsole parent;
    private AddTagsPresenter presenter;

    Set<Tag> allTags;
    Set<Tag> unusedTags;
    Set<Tag> usedTags;
    TagsObserver tagsObserver;

    SelectableSortFilterWrapper table;
    
    public AddTagsDialog(EmfConsole parent, Set<Tag> usedTags, TagsObserver tagsObserver) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.parent = parent;
        this.allTags = null;
        this.unusedTags = null;
        this.usedTags = usedTags;
        this.tagsObserver = tagsObserver;
        setModal(true);
    }

    public void display() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(tablePanel(), BorderLayout.CENTER);
        panel.add(buttonsPanel(), BorderLayout.SOUTH);
        contentPane.add(panel);
        
        setTitle("Add Tags");           

        this.pack();
        this.setSize(820, 440);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    public void observe(AddTagsPresenter presenter) {
        this.presenter = presenter;
    }

    // computes (left - right)
    private Set<Tag> diff(final Set<Tag> left, final Set<Tag> right) {
        Set<Tag> result = new HashSet<Tag>();
        for(Tag item : left) {
            if (!right.contains(item))
                result.add(item);
        }
        return result;
    }

    private JPanel tablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        allTags = presenter.getAllTags();
        unusedTags = diff(allTags, usedTags);
        table = new SelectableSortFilterWrapper(parent, new TagsTableData(unusedTags), null);
        tablePanel.add(table);
        return tablePanel;
    }
    

    @Override
    public void refreshTags() {
        allTags = presenter.getAllTags();
        unusedTags = diff(allTags, usedTags);
        table.refresh(new TagsTableData(unusedTags));
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        
        Button newButton = new Button("New", newAction());
        newButton.setMnemonic('N');
        
        Button addButton = new Button("Add", addAction());
        newButton.setMnemonic('A');
        
        Button cancelButton = new CancelButton(cancelAction());
        
        panel.add(newButton);
        panel.add(addButton);
        panel.add(cancelButton);
        return panel;
    }

    private void createNewTag() {
        NewTagDialog view = new NewTagDialog(parent, this);
        try {
            presenter.displayNewTagView(view);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, 
                    "Failed to create new tag:\n\n" + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Action newAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createNewTag();
            }
        };
    }

    private Action addAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                List<?> selected = table.selected();
                if (!selected.isEmpty()) {
                    Tag[] selectedTags = selected.toArray(new Tag[0]);
                    for(Tag selectedTag : selectedTags) {
                        usedTags.add(selectedTag);
                    }
                    if (tagsObserver != null) {
                        tagsObserver.refreshTags();
                    }
                }
                setVisible(false);
                dispose();
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
