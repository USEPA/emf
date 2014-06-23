package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.Sector;

import java.io.Serializable;

public class FastAnalysisInputSector implements Serializable {

    private int fastRunId;
    private int gridId;
    private Sector sector;

    public FastAnalysisInputSector() {
        //
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FastAnalysisInputSector)) {
            return false;
        }

        FastAnalysisInputSector other = (FastAnalysisInputSector) obj;

        return (
            fastRunId == other.getFastRunId() 
            && gridId == other.getGridId()
            && sector.getId() == other.getSector().getId()
                );
    }

    public int hashCode() {
        return (this.fastRunId + "_" + this.gridId + "_" + this.sector.getId()).hashCode();
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public void setGridId(int gridId) {
        this.gridId = gridId;
    }

    public int getGridId() {
        return gridId;
    }

    public void setFastRunId(int fastRunId) {
        this.fastRunId = fastRunId;
    }

    public int getFastRunId() {
        return fastRunId;
    }
}