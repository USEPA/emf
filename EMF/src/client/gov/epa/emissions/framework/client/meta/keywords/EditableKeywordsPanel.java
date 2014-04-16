package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.gui.Editor;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.ui.EditableTablePanel;

import java.awt.BorderLayout;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class EditableKeywordsPanel extends JPanel implements Editor {

    private EditableTablePanel editableTablePanel;
    
    private JComboBox comboBox;
    
    public EditableKeywordsPanel(String label, EditableKeyValueTableData tableData, Keywords masterKeywords, ManageChangeables changeablesList, EmfConsole parent) {
        editableTablePanel = new EditableKeyValueTablePanel(label, tableData, changeablesList, parent);
        editableTablePanel.setColumnEditor(keywordColumnEditor(masterKeywords), 1, "Select from the list");

        super.setLayout(new BorderLayout());
        super.add(editableTablePanel, BorderLayout.CENTER);
    }

    private TableCellEditor keywordColumnEditor(Keywords masterKeywords) {
        comboBox = new JComboBox();
        comboBox.setEditable(true);
        Keyword[] list = masterKeywords.all();
        for (int i = 0; i < list.length; i++)
            comboBox.addItem(list[i].getName());
        
        return new DefaultCellEditor(comboBox);
    }
    
    public void addListener(KeyListener keyListener) {
        editableTablePanel.addListener(keyListener);
    }
    
    public void addComboBoxListener(ItemListener itemListener) {
        comboBox.addItemListener(itemListener);
    }
    
    public void commit() {
        editableTablePanel.commit();
    }

}
