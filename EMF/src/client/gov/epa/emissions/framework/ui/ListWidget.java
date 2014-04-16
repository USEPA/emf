package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.Changeables;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

public class ListWidget extends JList implements Changeable {

    private boolean changed;

    private Changeables changeables;

    private DefaultListModel model;
    
    private IntList indexes;

    public ListWidget(Object[] items) {
        model = model(items);
        this.setModel(model);
        changed = false;
    }

    public ListWidget(Object[] items, Object[] selected) {
        this(items);
        setSelected(selected);
    }

    private DefaultListModel model(Object[] items) {
        DefaultListModel model = new DefaultListModel();
        if (items != null && items.length >0){
            for (int i = 0; i < items.length; i++) {
                model.addElement(items[i]);
            }
        }
        return model;
    }

    public void setSelected(Object[] selected) {
        indexes = new ArrayIntList();
        for (int i = 0; i < selected.length; i++)
            indexes.add(model.indexOf(selected[i]));

        super.setSelectedIndices(indexes.toArray());
    }
    
    public IntList getSelected() {
        return indexes;
    }

    public boolean hasChanges() {
        return changed;
    }

    public void clear() {
        changed = false;
    }

    public void observe(Changeables changeables) {
        this.changeables = changeables;
        addListDataListener();
    }

    private void addListDataListener() {
        ListModel model = this.getModel();
        model.addListDataListener(new ListDataListener() {
            public void contentsChanged(ListDataEvent e) {
                notifyChanges();
            }

            public void intervalAdded(ListDataEvent e) {
                notifyChanges();
            }

            public void intervalRemoved(ListDataEvent e) {
                notifyChanges();
            }
        });
    }

    void notifyChanges() {
        changed = true;
        if (changeables != null)
            changeables.onChanges();
    }

    public boolean contains(Object obj) {
        return model.contains(obj);
    }

    public void addElement(Object obj) {
        model.addElement(obj);
    }

    public void add(int index, Object obj) {
        model.add(index, obj);
    }

    public void removeElements(Object[] removeValues) {
        for (int i = 0; i < removeValues.length; i++) {
            model.removeElement(removeValues[i]);
        }
    }
    
    public void removeElement(Object removeValue) {
        model.removeElement(removeValue);
    }

    public void removeAllElements() {
        model.removeAllElements();
//        for (int i = 0; i < model.size(); i++) {
//            model.removeElement(model.get(i));
//        }
    }
    
    public void removeSelectedElements() {
        
        Object [] selectedValues = this.getSelectedValues();
       for (int i = 0; i < selectedValues.length; i++) {
           model.removeElement(selectedValues[i]);
       }
    }

    public Object[] getAllElements() {
        Object[] obj = new Object[model.getSize()];
        model.copyInto(obj);
        return obj;
    }

    //Swap two elements in the list.
    public void swap(int a, int b) {
        Object aObject = model.getElementAt(a);
        Object bObject = model.getElementAt(b);
        model.set(a, bObject);
        model.set(b, aObject);
    }

    public void setModelSize(int modelSize) {
        this.model.setSize(modelSize);
        this.model.trimToSize();
    }
}
