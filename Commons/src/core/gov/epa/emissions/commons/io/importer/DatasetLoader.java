package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatasetLoader {

    private Dataset dataset;

    public DatasetLoader(Dataset dataset) {
        this.dataset = dataset;
    }

    public InternalSource internalSource(File file, String table, TableFormat tableFormat) {
        InternalSource source = new InternalSource();
        source.setTable(table);
        source.setType(tableFormat.identify());
        source.setCols(colNames(tableFormat.cols()));
        source.setSource(file.getAbsolutePath());
        source.setSourceSize(file.length());

        dataset.addInternalSource(source);// FIXME: needs to be explicit and separate

        return source;
    }

    private String[] colNames(Column[] cols) {
        List names = new ArrayList();
        for (int i = 0; i < cols.length; i++)
            names.add(cols[i].name());

        return (String[]) names.toArray(new String[0]);
    }

    public InternalSource summarySource() {
        InternalSource source = new InternalSource();
        source.setType("Summary Table");
        source.setTable(tableName(dataset.getName()) + "_summary");
        source.setSource("TODO: get a name");

        return source;
    }

    // FIXME: get rid of it
    public String tableName(String datasetName) {
        return format(datasetName).trim().replaceAll(" ", "_");
    }

    private String format(String name) {
        String result = name;

        for (int i = 0; i < result.length(); i++) {
            if (!Character.isJavaLetterOrDigit(result.charAt(i))) {
                result = result.replace(result.charAt(i), '_');
            }
        }

        if (Character.isDigit(result.charAt(0))) {
            result = result.replace(result.charAt(0), '_');
            result = "DS" + result;
        }

        return result;
    }
}
