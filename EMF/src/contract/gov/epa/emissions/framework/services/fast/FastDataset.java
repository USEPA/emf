package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class FastDataset implements Serializable {

    private int id;

    private EmfDataset dataset;

    private Date addedDate;

    private FastNonPointDataset fastNonPointDataset;

    public EmfDataset getDataset() {
        return dataset;
    }

    public void setDataset(EmfDataset dataset) {
        this.dataset = dataset;
    }

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public FastDataset() {
        //
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof FastDataset) || ((FastDataset) other).getDataset() == null
                || this.getDataset() == null)
            return false;

        return this.getDataset().getId() == ((FastDataset) other).getDataset().getId();
    }

    public int hashCode() {
        return dataset.getName().hashCode();
    }

    @Override
    public String toString() {
        return this.dataset.getName();
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setFastNonPointDataset(FastNonPointDataset fastNonPointDataset) {
        this.fastNonPointDataset = fastNonPointDataset;
    }

    public FastNonPointDataset getFastNonPointDataset() {
        return fastNonPointDataset;
    }
}
