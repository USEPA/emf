package gov.epa.emissions.framework.services.dao.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.dao.ControlTechnologyDao;
import gov.epa.emissions.framework.services.persistence.AbstractJpaDao;

@Repository(value = "controlTechnologyDao")
public class ControlTechnologyDaoImpl extends AbstractJpaDao<ControlTechnology> implements ControlTechnologyDao {

    public ControlTechnologyDaoImpl() {
        super();
        setClazz(ControlTechnology.class);
    }

    @Transactional("transactionManager")
    @Override
    public ControlTechnology create(ControlTechnology technology) {
        return create(technology);
    }

    @Override
    public List<ControlTechnology> getAll() {
        return findAll().stream().sorted(Comparator.comparing(ControlTechnology::getName)).collect(Collectors.toList());
    }

    @Override
    public boolean canUpdate(ControlTechnology technology) {
        ControlTechnology current = findOne(technology.getId());
        if (current == null) {
            return false;
        }

        if (current.getName().equals(technology.getName()))
            return true;

        return findByName(technology.getName()) == null;
    }
}
