package gov.epa.emissions.framework.services.daos;

import gov.epa.emissions.framework.services.EmfException;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {
    
    Optional<T> get(int id);
    
    List<T> getAll();
    
    public T add(T t);
    
    T update(T t) throws EmfException;
    
    void delete(T t) throws EmfException;   
}