package gov.epa.emissions.framework.client.cost.controlstrategy;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


public class TableComboBox extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
{
//    private static final long serialVersionUID = 5647725208335645741L;

    public interface TableComboBoxPressedHandler
    {
        /**
         * Called when the button is pressed.
         * @param row The row in which the button is in the table.
         * @param column The column the button is in in the table.
         */
        void onButtonPress(int row, int column);
    }
    
    private List<TableComboBoxPressedHandler> handlers;
    private Hashtable<Integer, JComboBox> comboBoxes;
    private Object[] values;
    
    public TableComboBox(Object[] values)
    {
        this.handlers = new ArrayList<TableComboBoxPressedHandler>();
        this.comboBoxes = new Hashtable<Integer, JComboBox>();
        this.values = values;
    }
    
    /**
     * Add a slide callback handler
     * @param handler
     */
    public void addHandler(TableComboBoxPressedHandler handler)
    {
        if (handlers != null)
        {
            handlers.add(handler);
        }
    }

    /**
     * Remove a slide callback handler
     * @param handler
     */
    public void removeHandler(TableComboBoxPressedHandler handler)
    {
        if (handlers != null)
        {
            handlers.remove(handler);
        }
    }

    public JComboBox getComboBox(int row) {
        return comboBoxes.get(row);
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, final int row, final int column)
    {
        JComboBox button = null;
        if(comboBoxes.containsKey(row))
        {
            button = comboBoxes.get(row);
        }
        else
        {
            button = new JComboBox();
            button.addItem("None selected");
            for (Object object : values) {
                button.addItem(object);
            }
//            button.addItem("test");
//            button.addItem("test1");
//            button.addItem("test2");
//            button.addItem("test3");
            if(value != null)
            {
                button.setSelectedItem(value);
            }
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if(handlers != null)
                    {
                        for(TableComboBoxPressedHandler handler : handlers)
                        {
                            handler.onButtonPress(row, column);
                        }
                    }
                }
            });
            comboBoxes.put(row, button);
        }

        return button;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int column)
    {
        JComboBox button = null;
        if(comboBoxes.containsKey(row))
        {
            button = comboBoxes.get(row);
        }
        else
        {
            button = new JComboBox();
//            button.addItem("test");
//            button.addItem("test1");
//            button.addItem("test2");
//            button.addItem("test3");
            if(value != null)
            {
                button.setSelectedItem(value);
            }

            comboBoxes.put(row, button);
        }

        return button;
    }
   

    public Object getCellEditorValue()
    {
        return null;
    }

    public void dispose()
    {
        if (handlers != null)
        {
            handlers.clear();
        }
    }
}
