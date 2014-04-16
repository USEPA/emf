package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;

public class FastNonPointDataset implements Serializable {

    private int id;
    private String name;
    private EmfDataset griddedSMKDataset;
    private int griddedSMKDatasetVersion;
    private EmfDataset baseNonPointDataset;
    private int baseNonPointDatasetVersion;
    private EmfDataset invTableDataset;
    private int invTableDatasetVersion;
    private Grid grid;
    private EmfDataset quasiPointDataset;
    private FastDataset fastDataset;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public FastNonPointDataset() {
        //
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {

        String retVal = name;
        if (this.quasiPointDataset != null) {
            retVal = this.quasiPointDataset.getName();
        }

        return retVal;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EmfDataset getGriddedSMKDataset() {
        return griddedSMKDataset;
    }

    public void setGriddedSMKDataset(EmfDataset griddedSMKDataset) {
        this.griddedSMKDataset = griddedSMKDataset;
    }
    
    public int getGriddedSMKDatasetVersion() {
        return griddedSMKDatasetVersion;
    }

    public void setGriddedSMKDatasetVersion(int griddedSMKDatasetVersion) {
        this.griddedSMKDatasetVersion = griddedSMKDatasetVersion;
    }
    
    public EmfDataset getBaseNonPointDataset() {
        return baseNonPointDataset;
    }

    public void setBaseNonPointDataset(EmfDataset baseNonPointDataset) {
        this.baseNonPointDataset = baseNonPointDataset;
    }
    
    public int getBaseNonPointDatasetVersion() {
        return baseNonPointDatasetVersion;
    }

    public void setBaseNonPointDatasetVersion(int baseNonPointDatasetVersion) {
        this.baseNonPointDatasetVersion = baseNonPointDatasetVersion;
    }
    
    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public EmfDataset getQuasiPointDataset() {
        return quasiPointDataset;
    }

    public void setQuasiPointDataset(EmfDataset quasiPointDataset) {
        this.quasiPointDataset = quasiPointDataset;
    }
    
    public boolean equals(Object other) {
        if (other == null || !(other instanceof FastNonPointDataset))
            return false;
        
        if (this == other) 
            return true;

        if (this.id == ((FastNonPointDataset) other).getId()) 
            return true;

        final FastNonPointDataset fastNonPointDataset = (FastNonPointDataset) other;

        if (
            this.griddedSMKDataset.equals(fastNonPointDataset.griddedSMKDataset)
            && this.griddedSMKDatasetVersion == fastNonPointDataset.griddedSMKDatasetVersion
            && this.baseNonPointDataset.equals(fastNonPointDataset.baseNonPointDataset)
            && this.baseNonPointDatasetVersion == fastNonPointDataset.baseNonPointDatasetVersion
            && this.grid.equals(fastNonPointDataset.grid)
            && this.quasiPointDataset.equals(fastNonPointDataset.quasiPointDataset)
            ) 
            return true;

        return false;
    }

    public int hashCode() {
        return new String(this.griddedSMKDataset.getId() + "_" 
            + this.griddedSMKDatasetVersion + "_"
            + this.baseNonPointDataset.getId() + "_"
            + this.baseNonPointDatasetVersion + "_"
            + this.grid.getId() + "_"
            + this.quasiPointDataset.getId()).hashCode();
    }

    public String toString() {
        return this.name;
    }

    public void setFastDataset(FastDataset fastDataset) {
        this.fastDataset = fastDataset;
    }

    public FastDataset getFastDataset() {
        return fastDataset;
    }

    public void setInvTableDataset(EmfDataset invTableDataset) {
        this.invTableDataset = invTableDataset;
    }

    public EmfDataset getInvTableDataset() {
        return invTableDataset;
    }

    public void setInvTableDatasetVersion(int invTableDatasetVersion) {
        this.invTableDatasetVersion = invTableDatasetVersion;
    }

    public int getInvTableDatasetVersion() {
        return invTableDatasetVersion;
    }
}
