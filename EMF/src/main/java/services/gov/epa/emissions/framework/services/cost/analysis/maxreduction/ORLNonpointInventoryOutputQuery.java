package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.orl.ORLNonPointFileFormat;

public class ORLNonpointInventoryOutputQuery implements InventoryOutputQuery {

    private FileFormat format;

    public ORLNonpointInventoryOutputQuery(SqlDataTypes dataTypes) {
        this.format = new ORLNonPointFileFormat(dataTypes);
    }

    public String selectClause(String inputDatasetTableAlias, String detailResultTableAlias) {
        Column[] columns = format.cols();
        StringBuffer query = query(inputDatasetTableAlias, columns);
        replace(query, inputDatasetTableAlias + ".ANN_EMIS", detailResultTableAlias + ".final_emissions");
        replace(query, inputDatasetTableAlias + ".CEFF", detailResultTableAlias + ".control_eff");
        replace(query, inputDatasetTableAlias + ".REFF", detailResultTableAlias + ".rule_pen");
        replace(query, inputDatasetTableAlias + ".RPEN", detailResultTableAlias + ".rule_eff");
        return query.toString();
    }

    private StringBuffer replace(StringBuffer query, String string1, String string2) {
        int index = query.indexOf(string1);
        query.delete(index, index + string1.length());
        return query.insert(index, string2);
    }

    private StringBuffer query(String inputDatasetTableAlias, Column[] columns) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < columns.length - 1; i++) {
            sb.append(inputDatasetTableAlias + "." + columns[i].name() + ", ");
        }

        if (columns.length > 0)
            sb.append(columns[columns.length - 1].name());
        return sb;
    }

    public String conditionalClause(String inputDatasetTableAlias, String detailResultTableAlias) {
        return join(inputDatasetTableAlias, "dataset_id", detailResultTableAlias, "input_ds_id") + " AND "
                + join(inputDatasetTableAlias, "FIPS", detailResultTableAlias, "FIPS") + " AND "
                + join(inputDatasetTableAlias, "SCC", detailResultTableAlias, "SCC") + " AND "
                + join(inputDatasetTableAlias, "POLL", detailResultTableAlias, "POLLUTANT");
    }

    private String join(String inputDatasetTableAlias, String col1, String detailResultTableAlias, String col2) {
        return prefix(inputDatasetTableAlias) + col1 + "=" + prefix(detailResultTableAlias) + col2;
    }

    private String prefix(String inputDatasetTableAlias) {
        return inputDatasetTableAlias + ".";
    }

}
