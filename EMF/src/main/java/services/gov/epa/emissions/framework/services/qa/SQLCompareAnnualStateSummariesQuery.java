package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.Session;

public class SQLCompareAnnualStateSummariesQuery extends SQLQAProgramQuery {

    private DbServer dbServer;
    
    public static final String invTag = "-inv";

    public static final String smkRptTag = "-smkrpt";

    public static final String toleranceTag = "-tolerance";

    public static final String coStCyTag = "-costcy";

    public SQLCompareAnnualStateSummariesQuery(HibernateSessionFactory sessionFactory, DbServer dbServer,
            String emissioDatasourceName, String tableName, QAStep qaStep) {
        super(sessionFactory,emissioDatasourceName,tableName,qaStep);
        this.dbServer = dbServer;
    }

    private String[] parseSwitchArguments(String programSwitches, int beginIndex, int endIndex) {
        List<String> inventoryList = new ArrayList<String>();
        String value = "";
        String valuesString = "";

        valuesString = programSwitches.substring(beginIndex, endIndex);
        StringTokenizer tokenizer2 = new StringTokenizer(valuesString, "\n");
        tokenizer2.nextToken(); // skip the flag

        while (tokenizer2.hasMoreTokens()) {
            value = tokenizer2.nextToken().trim();
            if (!value.isEmpty())
                inventoryList.add(value);
        }
        return inventoryList.toArray(new String[0]);
    }

    public String createCompareQuery() throws EmfException {
        String sql = "";
        String programArguments = qaStep.getProgramArguments();

        int smkRptIndex = programArguments.indexOf(smkRptTag);
        int invIndex = programArguments.indexOf(invTag);
        int invTableIndex = programArguments.indexOf(QAStep.invTableTag);
        int toleranceIndex = programArguments.indexOf(toleranceTag);
        int coStCyIndex = programArguments.indexOf(coStCyTag);
        // int yearIndex = programArguments.indexOf(yearTag);
        String[] inventoryNames = null;
        String[] smkRptNames = null;
        String invTableName = null;
        String toleranceName = null;
        String coStCyName = null;
        // Integer year = null;
        String[] arguments;
        String version;
        String table;

        if (invIndex != -1) {
            arguments = parseSwitchArguments(programArguments, invIndex,
                    programArguments.indexOf("\n-", invIndex) != -1 ? programArguments.indexOf("\n-", invIndex)
                            : programArguments.length());
            if (arguments != null && arguments.length > 0){
                inventoryNames = arguments;
                for (String item: inventoryNames)
                    datasetNames.add(item);
            }
        }
        if (smkRptIndex != -1) {
            arguments = parseSwitchArguments(programArguments, smkRptIndex, programArguments
                    .indexOf("\n-", smkRptIndex) != -1 ? programArguments.indexOf("\n-", smkRptIndex)
                    : programArguments.length());
            if (arguments != null && arguments.length > 0){
                smkRptNames = arguments;
                for (String item: smkRptNames)
                    datasetNames.add(item);
            }
        }
        if (invTableIndex != -1) {
            arguments = parseSwitchArguments(programArguments, invTableIndex, programArguments.indexOf("\n-",
                    invTableIndex) != -1 ? programArguments.indexOf("\n-", invTableIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0){
                invTableName = arguments[0];
                datasetNames.add(invTableName);
            }
        }
        if (toleranceIndex != -1) {
            arguments = parseSwitchArguments(programArguments, toleranceIndex, programArguments.indexOf("\n-",
                    toleranceIndex) != -1 ? programArguments.indexOf("\n-", toleranceIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0){
                toleranceName = arguments[0];
                datasetNames.add(toleranceName);
            }
        }
        if (coStCyIndex != -1) {
            arguments = parseSwitchArguments(programArguments, coStCyIndex, programArguments
                    .indexOf("\n-", coStCyIndex) != -1 ? programArguments.indexOf("\n-", coStCyIndex)
                    : programArguments.length());
            if (arguments != null && arguments.length > 0){
                coStCyName = arguments[0];
                datasetNames.add(coStCyName);
            }
        }

        // validate everything has been specified...
        String errors = "";
        // make sure all dataset were specified, look at the names
        if (inventoryNames == null) {
            errors = "Missing ORL inventory datasets. ";
        }
        if (smkRptNames == null) {
            errors += "Missing " + DatasetType.smkmergeRptStateAnnualSummary + " dataset(s). ";
        }
        if (invTableName == null || invTableName.length() == 0) {
            errors += "Missing " + DatasetType.invTable + " dataset. ";
        }
        if (toleranceName == null || toleranceName.length() == 0) {
            errors += "Missing " + DatasetType.stateComparisonTolerance + " dataset. ";
        }
        if (coStCyName == null || coStCyName.length() == 0) {
            errors += "Missing " + DatasetType.countryStateCountyNamesAndDataCOSTCY + " dataset. ";
        }
        
        // go ahead and throw error from here, no need to validate anymore if the above is not there...
        if (errors.length() > 0) {
            throw new EmfException(errors);
        }
        checkDataset();

        // make sure the all the datasets actually exist
        EmfDataset[] inventories = new EmfDataset[] {};
        if (inventoryNames != null) {
            inventories = new EmfDataset[inventoryNames.length];
            for (int i = 0; i < inventoryNames.length; i++) {
                inventories[i] = getDataset(inventoryNames[i]);
            }
        }
        EmfDataset[] smkRpts = new EmfDataset[] {};
        if (smkRptNames != null) {
            smkRpts = new EmfDataset[smkRptNames.length];
            for (int i = 0; i < smkRptNames.length; i++) 
                smkRpts[i] = getDataset(smkRptNames[i]);
        }

        EmfDataset invTable = null;
        invTable = getDataset(invTableName);
        String invTableTableName = qualifiedEmissionTableName(invTable);
        String invTableVersion = new VersionedQuery(version(invTable.getId(), invTable.getDefaultVersion()), "invtable")
                .query();       

        // make sure tolerance dataset exists
        EmfDataset tolerance = null;
        tolerance = getDataset(toleranceName);
        String toleranceTableName = qualifiedEmissionTableName(tolerance);
        String toleranceVersion = new VersionedQuery(version(tolerance.getId(), tolerance.getDefaultVersion()),
                "tolerance").query();
        if (tolerance == null) {
            errors += "Uknown " + DatasetType.stateComparisonTolerance + " dataset, " + toleranceName + ". ";
        }

        // make sure country, state, county data dataset exists
        EmfDataset coStCy = null;
        coStCy = getDataset(coStCyName);
        String coStCyTableName = "emissions.state";
        String coStCyVersion = new VersionedQuery(version(coStCy.getId(), coStCy.getDefaultVersion()), "costcy")
                .query();
        

        // look to see if the sector has been specified
        for (EmfDataset dataset : inventories) {
            String sector = getDatasetSector(dataset);
        }

        // capIsPoint = checkTableForColumns(emissionTableName(dataset), "plantid,pointid,stackid,segment");

        // Outer SELECT clause
        sql = "select distinct on (coalesce(inv.sector, smk.sector),\n"
                + "coalesce(inv.state_name, smk.state_name),\n"
                + "coalesce(inv.fipsst, smk.fipsst),\n"
                + "coalesce(inv.smoke_name, smk.smoke_name))\n"
                + "coalesce(inv.sector, smk.sector) as sector,\n"
                + "coalesce(inv.state_name, smk.state_name) as state_name,\n"
                + "coalesce(inv.fipsst, smk.fipsst) as fipsst,\n"
                + "coalesce(inv.smoke_name, smk.smoke_name) as smoke_name,\n"
                + "inv.ann_emis as inv_ann_emis,\n"
                + "smk.ann_emis as smk_ann_emis,\n"
                + "coalesce(inv.ann_emis, 0.0) - coalesce(smk.ann_emis, 0.0) as inv_minus_smk_emis,\n"
                + "(inv.ann_emis - smk.ann_emis) / inv.ann_emis * 100.0 as pct_diff_inv_minus_smk,\n"
                + "abs(inv.ann_emis - smk.ann_emis) / inv.ann_emis * 100.0 as abs_pct_diff,\n"
                + "case when tolerance.tolerance_in_pct is not null and (inv.ann_emis - smk.ann_emis) / inv.ann_emis * 100.0 < tolerance.tolerance_in_pct then true when tolerance.tolerance_in_pct is not null and (inv.ann_emis - smk.ann_emis) / inv.ann_emis * 100.0 > tolerance.tolerance_in_pct then false else null::boolean end as within_tolerance \n";

        sql += "from \n" + "(" + "select \n" + "sector, \n" + "fipsst, \n" + "state_name, \n" + "smoke_name, \n"
                + "sum(ann_emis) as ann_emis \n" + "from ( \n";

        int i = 0;
        for (EmfDataset dataset : inventories) {
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion()), "inv").query();
            table = qualifiedEmissionTableName(dataset);

            int month = dataset.applicableMonth();
            int noOfDaysInMonth = 31;
            if (month != -1) {
                noOfDaysInMonth = getDaysInMonth(dataset.getYear(), month);
            }

            if (i > 0)
                sql += " union all \n";
            sql += "select '"
                    + getDatasetSector(dataset)
                    + "'::character varying(32) as sector, \n"
                    + "substring(inv.fips, case when length(inv.fips) = 5 then 1 when length(inv.fips) = 6 then 2 end, 2) as fipsst, \n"
                    + "costcy.statename as state_name, \n"
                    + "coalesce(invtable.name, 'AN UNSPECIFIED DESCRIPTION') as smoke_name, \n"
                    + "sum(coalesce(invtable.factor::double precision, 1.0) * coalesce(case when inv.avd_emis != -9.0 then inv.avd_emis else null end * "
                    + (month != -1 ? noOfDaysInMonth : "365")
                    + ", inv.ann_emis)) as ann_emis \n"
                    + "from "
                    + table
                    + " inv \n"
                    + "inner join "
                    + coStCyTableName
                    + " costcy \n"
                    + "on costcy.statecode = substring(inv.fips, case when length(inv.fips) = 5 then 1 when length(inv.fips) = 6 then 2 end, 2)::integer \n"
                    + "and coalesce(costcy.countrycode,0) = coalesce("
                    + getInventoryCountryCode(dataset, coStCyVersion)
                    + ",0) \n"
                    + "inner join "
                    + invTableTableName
                    + " invtable \n"
                    + "on invtable.cas = inv.poll \n"
                    + "where "
                    + version
                    + " \n"
                    + "and "
                    + invTableVersion
                    + " \n"
                    + "and "
                    + coStCyVersion
                    + " \n"
                    + "and invtable.keep = 'Y' \n"
                    + "group by substring(inv.fips, case when length(inv.fips) = 5 then 1 when length(inv.fips) = 6 then 2 end, 2), costcy.statename, coalesce(invtable.name, 'AN UNSPECIFIED DESCRIPTION')";
            ++i;
        }

        sql += ") tbl\n" + "group by \n" + "sector, \n" + "fipsst, \n" + "state_name, \n" + "smoke_name \n" + ") inv\n";

        // union together all smkmerge reports and aggregate to the sector, fipsst, state_name, smoke_name level
        sql += "inner join \n" + "(select \n" + "sector, \n" + "trim(TO_CHAR(fipsst,'00')) as fipsst, \n"
                + "state_name, \n" + "smoke_name, \n" + "sum(ann_emis) as ann_emis \n" + "from ( \n";

        i = 0;
        for (EmfDataset dataset : smkRpts) {
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion()), "smk").query();
            table = qualifiedEmissionTableName(dataset);
            if (i > 0)
                sql += " union all \n";
            sql += "select sector, \n" + "costcy.statecode as fipsst, \n" + "smk.state as state_name, \n"
                    + "coalesce(invtable.name, 'AN UNSPECIFIED DESCRIPTION') as smoke_name, \n"
                    + "coalesce(invtable.factor::double precision, 1.0) * ann_emis as ann_emis \n" + "from " + table
                    + " smk \n" + "inner join " + coStCyTableName + " costcy \n" + "on costcy.statename = smk.state \n"
                    + "inner join " + invTableTableName + " invtable \n" + "on invtable.cas = smk.species \n"
                    + "where " + version + " \n" + "and " + invTableVersion + " \n" + "and " + coStCyVersion + " \n"
                    + "and invtable.keep = 'Y' \n";
            ++i;
        }

        sql += ") tbl\n" + "group by \n" + "sector, \n" + "fipsst, \n" + "state_name, \n" + "smoke_name \n" + ") smk\n";

        // union together all inventories and aggregate to the sector, fipsst, state_name, smoke_name level

        sql += "on inv.sector = smk.sector\n" + "and inv.fipsst = smk.fipsst\n"
                + "and inv.state_name = smk.state_name\n" + "and inv.smoke_name = smk.smoke_name\n";

        sql += "left outer join "
                + toleranceTableName
                + " tolerance\n"
                + "on (coalesce(trim(tolerance.state_name),'') = '' or tolerance.state_name = coalesce(inv.state_name, smk.state_name))\n"
                + "and (coalesce(trim(tolerance.poll),'') = '' or tolerance.poll = coalesce(inv.smoke_name, smk.smoke_name))\n"
                + "and " + toleranceVersion + " \n";

        sql += "order by coalesce(inv.sector, smk.sector),\n"
                + "coalesce(inv.state_name, smk.state_name),\n"
                + "coalesce(inv.fipsst, smk.fipsst),\n"
                + "coalesce(inv.smoke_name, smk.smoke_name),\n"
                + "case \n"
                + "when coalesce(trim(tolerance.state_name),'') <> '' and coalesce(trim(tolerance.poll),'') <> '' then 1.0 \n"
                + "when coalesce(trim(tolerance.state_name),'') <> '' and coalesce(trim(tolerance.poll),'') = '' then 2.0 \n"
                + "when coalesce(trim(tolerance.state_name),'') = '' and coalesce(trim(tolerance.poll),'') <> '' then 3.0 \n"
                + "when coalesce(trim(tolerance.state_name),'') = '' and coalesce(trim(tolerance.poll),'') = '' then 4.0 \n"
                + "end, \n" + "tolerance.tolerance_in_pct";

        //        
        // sql = query(sql, true);
        sql = "CREATE TABLE " + emissionDatasourceName + "." + tableName + " AS " + sql;
        System.out.println(sql);

        return sql;
    }

    protected int getDaysInMonth(int year, int month) {
        return month != -1 ? DateUtil.daysInZeroBasedMonth(year, month) : 31;
    }

    protected String query(String partialQuery, boolean createClause) throws EmfException {

        SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissionDatasourceName, tableName);
        return parser.parse(partialQuery, createClause);
    }

    private String getDatasetSector(EmfDataset dataset) {
        String sector = null;
        // try and get sector from dataset
        if (dataset.getSectors() != null && dataset.getSectors().length > 0)
            sector = dataset.getSectors()[0].getName();

        return sector;
    }

    private String getInventoryCountryFromDatasetDesc(EmfDataset dataset) {
        String value = "";
        String country = null;
        String valuesString = "";

        valuesString = dataset.getDescription();
        StringTokenizer tokenizer = new StringTokenizer(valuesString, "\n");

        while (tokenizer.hasMoreTokens()) {
            value = tokenizer.nextToken().trim();
            if (!value.isEmpty() && value.contains("#COUNTRY")) {
                StringTokenizer tokenizer2 = new StringTokenizer(value, " ");
                tokenizer2.nextToken(); // skip the #COUNTRY part
                country = tokenizer2.nextToken().trim();
            }
        }
        return country;
    }

    private String getInventoryCountryCode(EmfDataset dataset, String coStCyVersion) throws EmfException {
        String country = null;
        String countryCode = "null";
        // try and get country from dataset
        if (dataset.getCountry() != null)
            country = dataset.getCountry().getName();

        // try and get country from dataset header
        if (country == null)
            country = getInventoryCountryFromDatasetDesc(dataset);

        // default to US
        if (country == null)
            country = "US";

        String query = "select costcy.code from emissions.country costcy where upper(costcy.name) = upper('" + country
                + "') and " + coStCyVersion + ";";
        ResultSet rs = null;
        try {
            rs = dbServer.getEmissionsDatasource().query().executeQuery(query);
            while (rs.next()) {
                countryCode = rs.getInt(1) + "";
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }
        return countryCode;
    }
}
