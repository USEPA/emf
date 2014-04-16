package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.LockableImpl;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FastRun extends LockableImpl implements Serializable {

    private int id;

    private String name;

    private String description = "";

    private String abbreviation;

    private Grid grid;

    private String runStatus;

    private User creator;

    private Date lastModifiedDate;

    private Date startDate;

    private Date completionDate;

    private String copiedFrom;

    private EmfDataset invTableDataset;

    private Integer invTableDatasetVersion;

    private EmfDataset speciesMapppingDataset;

    private Integer speciesMapppingDatasetVersion;

    private EmfDataset transferCoefficientsDataset;

    private Integer transferCoefficientsDatasetVersion;

    private EmfDataset cancerRiskDataset;

    private Integer cancerRiskDatasetVersion;

    private EmfDataset domainPopulationDataset;

    private Integer domainPopulationDatasetVersion;

    private FastRunInventory[] inventories = new FastRunInventory[] {};

    private Sector[] outputSectors = new Sector[] {};

    public FastRun() {
        // no-op
    }

    public FastRun(String name) {
        this();
        this.name = name;
    }

    public FastRun(int id, String name) {
        this(name);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public String getCopiedFrom() {
        return copiedFrom;
    }

    public void setCopiedFrom(String copiedFrom) {
        this.copiedFrom = copiedFrom;
    }

    public Integer getInvTableDatasetVersion() {
        return invTableDatasetVersion;
    }

    public void setInvTableDatasetVersion(Integer invTableDatasetVersion) {
        this.invTableDatasetVersion = invTableDatasetVersion;
    }

    public EmfDataset getInvTableDataset() {
        return invTableDataset;
    }

    public void setInvTableDataset(EmfDataset invTableDataset) {
        this.invTableDataset = invTableDataset;
    }

    public Integer getSpeciesMapppingDatasetVersion() {
        return speciesMapppingDatasetVersion;
    }

    public void setSpeciesMapppingDatasetVersion(Integer speciesMapppingDatasetVersion) {
        this.speciesMapppingDatasetVersion = speciesMapppingDatasetVersion;
    }

    public EmfDataset getSpeciesMapppingDataset() {
        return speciesMapppingDataset;
    }

    public void setSpeciesMapppingDataset(EmfDataset speciesMapppingDataset) {
        this.speciesMapppingDataset = speciesMapppingDataset;
    }

    public Integer getTransferCoefficientsDatasetVersion() {
        return transferCoefficientsDatasetVersion;
    }

    public void setTransferCoefficientsDatasetVersion(Integer transferCoefficientsDatasetVersion) {
        this.transferCoefficientsDatasetVersion = transferCoefficientsDatasetVersion;
    }

    public EmfDataset getTransferCoefficientsDataset() {
        return transferCoefficientsDataset;
    }

    public void setTransferCoefficientsDataset(EmfDataset transferCoefficientsDataset) {
        this.transferCoefficientsDataset = transferCoefficientsDataset;
    }

    public FastRunInventory[] getInventories() {
        return inventories;
    }

    public void setInventories(FastRunInventory[] inventories) {
        this.inventories = inventories;
    }

    public void removeInventories(List<FastRunInventory> inventories) {

        if (this.inventories != null) {

            List<FastRunInventory> list = new ArrayList<FastRunInventory>();
            for (FastRunInventory fastRunInventory : this.inventories) {

                if (!inventories.contains(fastRunInventory)) {
                    list.add(fastRunInventory);
                }
            }

            this.inventories = list.toArray(new FastRunInventory[0]);
        }
    }

    public Sector[] getOutputSectors() {
        return outputSectors;
    }

    public void setOutputSectors(Sector[] outputSectors) {
        this.outputSectors = outputSectors;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof FastRun))
            return false;

        final FastRun ss = (FastRun) other;

        return ss.name.equals(name) || ss.id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return this.name;
    }

    public void setDomainPopulationDatasetVersion(Integer domainPopulationDatasetVersion) {
        this.domainPopulationDatasetVersion = domainPopulationDatasetVersion;
    }

    public Integer getDomainPopulationDatasetVersion() {
        return domainPopulationDatasetVersion;
    }

    public void setDomainPopulationDataset(EmfDataset domainPopulationDataset) {
        this.domainPopulationDataset = domainPopulationDataset;
    }

    public EmfDataset getDomainPopulationDataset() {
        return domainPopulationDataset;
    }

    public void setCancerRiskDatasetVersion(Integer cancerRiskDatasetVersion) {
        this.cancerRiskDatasetVersion = cancerRiskDatasetVersion;
    }

    public Integer getCancerRiskDatasetVersion() {
        return cancerRiskDatasetVersion;
    }

    public void setCancerRiskDataset(EmfDataset cancerRiskDataset) {
        this.cancerRiskDataset = cancerRiskDataset;
    }

    public EmfDataset getCancerRiskDataset() {
        return cancerRiskDataset;
    }
}
