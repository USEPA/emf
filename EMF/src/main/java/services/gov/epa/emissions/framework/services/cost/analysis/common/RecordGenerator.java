package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface RecordGenerator {

    Record getRecord(ResultSet resultSet, BestMeasureEffRecord bestMeasureEffRecord, double originalEmissions, boolean displayOriginalEmissions, boolean displayFinalEmissions) throws SQLException, EmfException;
    
    double reducedEmission();
    
    Double totalCost();
    
    void calculateEmissionReduction(ResultSet resultSet, BestMeasureEffRecord bestMeasureEffRecord) throws SQLException;
    
    List tokens(ResultSet resultSet, BestMeasureEffRecord bestMeasureEffRecord, double originalEmissions, boolean displayOriginalEmissions, boolean displayFinalEmissions, boolean hasSICandNAICS) throws SQLException, EmfException;
}
