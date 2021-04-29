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
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class RemoveTagsDialog extends JDialog implements RemoveTagsView {

    private EmfConsole parent;
    private RemoveTagsPresenter presenter;

    Set<Tag> tags;
    TagsObserver tagsObserver;

    SelectableSortFilterWrapper table;
    
    public RemoveTagsDialog(EmfConsole parent, Set<Tag> tags, TagsObserver tagsObserver) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.parent = parent;
        this.tags = tags;
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
        
        setTitle("Remove Tags");           

        this.pack();
        this.setSize(820, 440);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    public void observe(RemoveTagsPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel tablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parent, new TagsTableData(tags), null);
        tablePanel.add(table);
        return tablePanel;
    }
    
    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        
        Button removeButton = new Button("Remove", removeAction());
        removeButton.setMnemonic(KeyEvent.VK_R);
        
        Button cancelButton = new CancelButton(cancelAction());
        
        panel.add(removeButton);
        panel.add(cancelButton);
        return panel;
    }

    private Action removeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                List<?> selected = table.selected();
                if (!selected.isEmpty()) {
                    tags.removeAll(selected);
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
