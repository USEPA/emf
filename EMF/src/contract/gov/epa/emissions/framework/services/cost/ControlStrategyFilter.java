package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.basic.FilterField;
import gov.epa.emissions.framework.services.module.SearchFilterFields;

import java.util.Date;

public class ControlStrategyFilter extends SearchFilterFields {

    public ControlStrategyFilter() {
        addFilterField("Name", new FilterField("cs.name", String.class));
        addFilterField("Last Modified", new FilterField("cs.lastModifiedDate", Date.class));
        addFilterField("Is Final", new FilterField("cs.isFinal", Boolean.class));
        addFilterField("Inventory", new FilterField("inputDataset.inputDataset.name", String.class));
        addFilterField("Program", new FilterField("controlProgram.name", String.class));
        addFilterField("Control Measure Class", new FilterField("controlMeasureClass.name", String.class));
        addFilterField("Control Measure Abbreviation", new FilterField("controlMeasure.controlMeasure.abbreviation", String.class));
        addFilterField("Control Measure Name", new FilterField("controlMeasure.controlMeasure.name", String.class));
        addFilterField("Run Status", new FilterField("cs.runStatus", String.class));
        addFilterField("Region", new FilterField("region.name", String.class));
        addFilterField("Target Pollutant", new FilterField("targetPollutant.name", String.class));
        addFilterField("Project", new FilterField("project.name", String.class));
        addFilterField("Strategy Type", new FilterField("strategyType.name", String.class));
        addFilterField("Inv. Year", new FilterField("cs.inventoryYear", Integer.class));
        addFilterField("Creator", new FilterField("creator.name", String.class));
    }
}
