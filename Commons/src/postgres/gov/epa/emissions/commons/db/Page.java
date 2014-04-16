package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.db.version.VersionedRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Is a Data Struct
 */
public class Page {

    private List records;

    private int number;

    private int min;

    public Page() {// needed for serialization
        this(0);
    }

    public Page(int number) {
        this.number = number;
        records = new ArrayList();
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void add(VersionedRecord record) {
        records.add(record);
    }

    public int count() {
        return records.size();
    }

    public boolean remove(int index) {
        return index < count() ? records.remove(index) != null : false;
    }

    public VersionedRecord[] getRecords() {
        return (VersionedRecord[]) records.toArray(new VersionedRecord[0]);
    }

    public void setRecords(VersionedRecord[] array) {
        records.clear();
        records.addAll(Arrays.asList(array));
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        if (count() == 0)
            return min;
        
        return min + count() - 1;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public boolean contains(int record) {
        return (getMin() <= record) && (record <= getMax());
    }
    
    public void print() {
        for ( Object o : records) {
            VersionedRecord vr = (VersionedRecord) o;
            Object[] objArray = vr.getTokens();
            System.out.println("--------");
            if ( objArray != null)
            for ( int i=0; i<objArray.length; i++){
                if ( objArray[i] == null) 
                    System.out.println(i+ " > null");
                else
                    System.out.println(i+ "> class: " + objArray[i].getClass() + ", value: " + objArray[i]);
            }
        }
    }

}
