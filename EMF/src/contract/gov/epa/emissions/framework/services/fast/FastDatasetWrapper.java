package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.Date;

public class FastDatasetWrapper {

    private FastDataset fastDataset;

    private static final String N_A = "N/A";

    public FastDatasetWrapper(FastDataset pointDataset) {
        this.fastDataset = pointDataset;
    }

    public FastDataset getPointDataset() {
        return fastDataset;
    }

    public FastNonPointDataset getNonPointDataset() {
        return this.fastDataset.getFastNonPointDataset();
    }

    public boolean isNonPoint() {
        return this.fastDataset.getFastNonPointDataset() != null;
    }

    public boolean isPoint() {
        return !this.isNonPoint();
    }

    public String getType() {

        String type = N_A;
        if (this.isPoint()) {
            type = "Point";
        } else if (this.isNonPoint()) {
            type = "Non-Point";
        }

        return type;
    }

    public String getName() {

        String name = this.fastDataset.getDataset().getName();
        return name;
    }

    public Date getAddedDate() {
        return this.fastDataset.getAddedDate();
    }

    public String getBaseNonPointName() {

        String name = N_A;

        if (this.isNonPoint()) {

            EmfDataset baseNonPointDataset = this.getNonPointDataset().getBaseNonPointDataset();
            if (baseNonPointDataset != null) {
                name = baseNonPointDataset.getName();
            }
        }

        return name;
    }

    public String getGriddedSMOKEName() {

        String name = N_A;

        if (this.isNonPoint()) {

            EmfDataset smokeDataset = this.getNonPointDataset().getGriddedSMKDataset();
            if (smokeDataset != null) {
                name = smokeDataset.getName();
            }
        }

        return name;
    }

    public String getGridName() {

        String name = N_A;

        if (this.isNonPoint()) {

            Grid grid = this.getNonPointDataset().getGrid();
            if (grid != null) {
                name = grid.getName();
            }
        }

        return name;
    }

    public int getId() {

        int id = -1;

        if (this.isPoint()) {
            id = this.fastDataset.getId();
        } else if (this.isNonPoint()) {
            id = this.getNonPointDataset().getId();
        }

        return id;
    }
}
