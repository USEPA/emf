package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetTypeUnit;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.Comments;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.DatasetLoader;
import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.FixedWidthFileReader;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.Reader;
import gov.epa.emissions.commons.io.importer.TemporalResolution;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class IDAImporter {

    private Datasource emissionDatasource;

    private Datasource referenceDatasource;

    private Dataset dataset;

    private SqlDataTypes sqlDataTypes;

    private DatasetTypeUnit unit;

    private FileVerifier fileVerifier;

    private File file;

    private DataTable dataTable;

    private String country;

    public IDAImporter(Dataset dataset, DbServer dbServer, SqlDataTypes sqlDataTypes) {
        this.dataset = dataset;
        this.emissionDatasource = dbServer.getEmissionsDatasource();
        this.referenceDatasource = dbServer.getReferenceDatasource();
        this.sqlDataTypes = sqlDataTypes;
        this.fileVerifier = new FileVerifier();
    }

    public void setup(File folder, String[] fileNames, IDAFileFormat fileFormat, DataFormatFactory formatFactory)
            throws ImporterException {
        File file = new File(folder, fileNames[0]);
        fileVerifier.shouldExist(file);
        this.file = file;
        IDAHeaderReader headerReader = new IDAHeaderReader(file);
        headerReader.read();
        headerReader.close();

        fileFormat.addPollutantCols(headerReader.polluntants());
        TableFormat tableFormat = new IDATableFormat(fileFormat, sqlDataTypes);
        tableFormat = formatFactory.tableFormat(tableFormat, sqlDataTypes);

        unit = new DatasetTypeUnit(tableFormat, fileFormat);
        DatasetLoader loader = new DatasetLoader(dataset);

        dataTable = new DataTable(dataset, emissionDatasource);
        InternalSource internalSource = loader.internalSource(file, dataTable.name(), tableFormat);
        unit.setInternalSource(internalSource);

        validateIDAFile(headerReader.comments());
    }

    public void run() throws ImporterException {
        dataTable.create(unit.tableFormat());
        try {
            doImport(unit, dataset, dataTable.name());
        } catch (Exception e) {
            try {
                dataTable.drop();
            } catch ( ImporterException e1) {
                //throw new ImporterException(e.getMessage() + "; " + e1.getMessage());
            }
            throw new ImporterException("Filename: " + file.getAbsolutePath() + ", " + e.getMessage());
        }
    }

    private void doImport(DatasetTypeUnit unit, Dataset dataset, String table) throws Exception {
        Reader idaReader = null;
        try {
            idaReader = new FixedWidthFileReader(unit.getInternalSource().getSource(), unit.fileFormat());
            IDADataLoader loader = new IDADataLoader(emissionDatasource, referenceDatasource, unit.tableFormat(),country);
            loader.load(idaReader, dataset, table);
            loadDataset(idaReader.comments(), dataset);
        } finally {
            close(idaReader);
        }
    }

    private void close(Reader reader) throws ImporterException {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                throw new ImporterException(e.getMessage());
            }
        }
    }

    private void loadDataset(List commentsList, Dataset dataset) {
        dataset.setUnits("short tons/year");
        Comments comments = new Comments(commentsList);
        dataset.setDescription(comments.all());
        dataset.setTemporalResolution(TemporalResolution.ANNUAL.getName());
    }

    private void validateIDAFile(List comments) throws ImporterException {
        addAttributesExtractedFromComments(comments, dataset);
    }

    private void addAttributesExtractedFromComments(List commentsList, Dataset dataset) throws ImporterException {
        Comments comments = new Comments(commentsList);
        if (!comments.have("IDA"))
            throw new ImporterException("The tag - 'IDA' is mandatory.");

        if (!comments.have("COUNTRY"))
            throw new ImporterException("The tag - 'COUNTRY' is mandatory.");
        country = comments.content("COUNTRY");

        // FIXME: get the country object from the db
        // dataset.setCountry(new Country(country));
        // dataset.setRegion(new Region(country));

        if (!comments.have("YEAR"))
            throw new ImporterException("The tag - 'YEAR' is mandatory.");
        String year = comments.content("YEAR");
        dataset.setYear(Integer.parseInt(year));
        setStartStopDateTimes(dataset, Integer.parseInt(year));
    }

    private void setStartStopDateTimes(Dataset dataset, int year) {
        Date start = new GregorianCalendar(year, Calendar.JANUARY, 1).getTime();
        dataset.setStartDateTime(start);

        Calendar endCal = new GregorianCalendar(year, Calendar.DECEMBER, 31, 23, 59, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        dataset.setStopDateTime(endCal.getTime());
    }

}
