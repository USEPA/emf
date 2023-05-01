package gov.epa.emissions.framework.services.dao;

import java.util.List;

import gov.epa.emissions.framework.services.cost.data.ControlTechnology;

public interface ControlTechnologyDao {

    ControlTechnology create(ControlTechnology technology);

    List<ControlTechnology> getAll();

    boolean canUpdate(ControlTechnology technology);

}