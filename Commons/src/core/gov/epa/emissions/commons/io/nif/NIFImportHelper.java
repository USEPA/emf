package gov.epa.emissions.commons.io.nif;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.io.FormatUnit;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NIFImportHelper {

    public String notation(File file) throws ImporterException {
        String notation = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith("#") && line.length() > 2) {
                    notation = line.substring(0, 2).toLowerCase();
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            throw new ImporterException(e.getMessage());
        }
        return notation;
    }

    public String notation(Datasource datasource, String table) {
        
        String key = null;
        try {
            ResultSet rs = datasource.query().selectAll(table);
            if (rs.next()) {
                key = rs.getString("record_type");
            }
        } catch (SQLException e) {
            return key;
        }
        return key;
    }

    public InternalSource internalSource(String tablePrefix, String key, File file, FormatUnit formatUnit) {
        InternalSource internalSource = new InternalSource();
        internalSource.setSource(file.getAbsolutePath());
        internalSource.setType(formatUnit.fileFormat().identify());
        internalSource.setTable(tablePrefix + "_" + key);
        return internalSource;
    }

    public InternalSource internalSource(String table, String source, FormatUnit formatUnit) {
        InternalSource internalSource = new InternalSource();
        internalSource.setSource(source);
        internalSource.setType(formatUnit.fileFormat().identify());
        internalSource.setTable(table);
        return internalSource;
    }

    public void loadDataset(String table, Dataset dataset, Datasource datasource) throws ImporterException {
        try {
            ResultSet rs = datasource.query().selectAll(table);
            if (rs.next()) {
                loadStartDate(rs, dataset);
                loadEndDate(rs,dataset);
                loadUnit(rs,dataset);
                rs.close();
            }
        } catch (Exception e) {
            throw new ImporterException("Error in loading data to the dataset: "+e.getMessage());
        }
    }

    private void loadUnit(ResultSet rs, Dataset dataset) throws SQLException {
        String unit = rs.getString("emission_units_code");
        dataset.setUnits(unit);
        
    }

    private void loadStartDate(ResultSet rs, Dataset dataset) throws SQLException, ImporterException {
        String startDate = rs.getString("start_date");
        String startTime = rs.getString("start_time");
        dataset.setStartDateTime(date(startDate,startTime));
    }
    
    private void loadEndDate(ResultSet rs, Dataset dataset) throws SQLException, ImporterException {
        String endDate = rs.getString("end_date");
        String endTime = rs.getString("end_time");
        dataset.setStopDateTime(date(endDate,endTime));        
    }


    private Date date(String date, String time) throws ImporterException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HHmm");
        String dateAndTime = date + " " + formatTime(time);
        try {
            return format.parse(dateAndTime);
        } catch (ParseException e) {
            throw new ImporterException("Could not format the date and time '" + dateAndTime + "'");
        }

    }

    private String formatTime(String time) {
        String t = (time == null) ? "" : time;
        int i=t.length();
        for (; i < 4; i++) {
            t = "0" + t;
        }
        return t;
    }
}
