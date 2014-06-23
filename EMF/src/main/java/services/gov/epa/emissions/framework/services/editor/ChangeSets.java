package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChangeSets {

    private List list;

    public ChangeSets() {
        this.list = new ArrayList();
    }

    public ChangeSets(List list) {
        this.list = list;
    }

    public void add(ChangeSet set) {
        list.add(set);
    }

    public int size() {
        return list.size();
    }

    public int netIncrease() {
        int result = 0;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            ChangeSet element = (ChangeSet) iter.next();
            result += element.netIncrease();
        }

        return result;
    }

    public void add(ChangeSets another) {
        for (ChangeSetsIterator iter = another.iterator(); iter.hasNext();)
            add(iter.next());
    }

    public ChangeSetsIterator iterator() {
        return new ChangeSetsIterator(list);
    }

    public ChangeSet get(int index) {
        return (ChangeSet) list.get(index);
    }

    public class ChangeSetsIterator {

        private Iterator iterator;

        public ChangeSetsIterator(List list) {
            iterator = list.iterator();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public ChangeSet next() {
            return (ChangeSet) iterator.next();
        }

    }

    public boolean hasChanges() {
        for (ChangeSetsIterator iter = iterator(); iter.hasNext();) {
            ChangeSet element = iter.next();
            if (element.hasChanges())
                return true;

        }
        return false;
    }

}
