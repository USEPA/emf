package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
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

public class SQLECControlScenarioQuery extends SQLQAProgramQuery{
    
    private DbServer dbServer;
    
    public static final String invTag = "-inv";

    public static final String gsrefTag = "-gsref";

    public static final String gsproTag = "-gspro";

    public static final String detailedResultTag = "-detailed_result";

    public SQLECControlScenarioQuery(HibernateSessionFactory sessionFactory, DbServer dbServer, 
            String emissioDatasourceName, String tableName, 
            QAStep qaStep) {
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
    
    private  KeyVal[] keyValFound(EmfDataset dataset, String keyword) {
        KeyVal[] keys = dataset.getKeyVals();
        List<KeyVal> list = new ArrayList<KeyVal>();
        
        for (KeyVal key : keys)
            if (key.getName().equalsIgnoreCase(keyword)) 
                list.add(key);
        
        return list.toArray(new KeyVal[0]);
    }

    public String createCompareQuery() throws EmfException {
        String sql = "";
        String programArguments = qaStep.getProgramArguments();
        
        int gsproIndex = programArguments.indexOf(gsproTag);
        int invIndex = programArguments.indexOf(invTag);
        int gsrefIndex = programArguments.indexOf(gsrefTag);
        int detailedResultIndex = programArguments.indexOf(detailedResultTag);

        String[] gsproNames = null;
        String[] gsrefNames = null; 
        String inventoryName = null;
        String detailedResultName = null;

        String[] arguments;
        String version;
        String table;


        if (invIndex != -1) {
            arguments = parseSwitchArguments(programArguments, invIndex, programArguments.indexOf("\n-", invIndex) != -1 ? programArguments.indexOf("\n-", invIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0)  {
                inventoryName = arguments[0];
                datasetNames.add(inventoryName);
            }
        }
        if (gsproIndex != -1) {
            arguments = parseSwitchArguments(programArguments, gsproIndex, programArguments.indexOf("\n-", gsproIndex) != -1 ? programArguments.indexOf("\n-", gsproIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                gsproNames = arguments;
                for ( String item : gsproNames )
                    datasetNames.add(item);
            }
        }
        if (gsrefIndex != -1) {
            arguments = parseSwitchArguments(programArguments, gsrefIndex, programArguments.indexOf("\n-", gsrefIndex) != -1 ? programArguments.indexOf("\n-", gsrefIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                gsrefNames = arguments;
                for ( String item : gsrefNames )
                    datasetNames.add(item);
            }
        }
        if (detailedResultIndex != -1) {
            arguments = parseSwitchArguments(programArguments, detailedResultIndex, programArguments.indexOf("\n-", detailedResultIndex) != -1 ? programArguments.indexOf("\n-", detailedResultIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                detailedResultName = arguments[0];
                datasetNames.add(detailedResultName);
            }
        }
        
        //validate everything has been specified...
        String errors = "";
        //make sure all dataset were specified, look at the names
        if (gsproNames == null) {
            errors = "Missing " + DatasetType.chemicalSpeciationProfilesGSPRO + " datasets. ";
        }
        if (gsrefNames == null) {
            errors += "Missing " + DatasetType.chemicalSpeciationCrossReferenceGSREF + " dataset(s). ";
        }
//        if (inventoryName == null || inventoryName.length() == 0) {
//            errors += "Missing inventory dataset. ";
//        }
//        if (detailedResultName == null || detailedResultName.length() == 0) {
//            errors += "Missing " + DatasetType.strategyDetailedResult + " dataset. ";
//        }
        //go ahead and throw error from here, no need to validate anymore if the above is not there...
        if (errors.length() > 0) {
            throw new EmfException(errors);
        }
        
        checkDataset();
        //make sure the all the datasets actually exist
        EmfDataset[] gspros  = new EmfDataset[] {};
        if (gsproNames != null) {
            gspros = new EmfDataset[gsproNames.length];
            for (int i = 0; i < gsproNames.length; i++) {
                gspros[i] = getDataset(gsproNames[i]);
            }
        }
        EmfDataset[] gsrefs = new EmfDataset[] {};
        if (gsrefNames != null) {
            gsrefs = new EmfDataset[gsrefNames.length];
            for (int i = 0; i < gsrefNames.length; i++) {
                gsrefs[i] = getDataset(gsrefNames[i]);
            }
        }
        
        //make sure detailed result dataset exists
        //1st:  look in the arguments for it being explicitly set
        //2nd:  if blank OR $DATASET then use the current dataset this qa program is assigned to
        EmfDataset detailedResult = null;
        if (detailedResultName == null || detailedResultName.length() == 0 || detailedResultName.equalsIgnoreCase("$DATASET")) {
            detailedResult = getDataset(this.qaStep.getDatasetId());
        } else {
            detailedResult = getDataset(detailedResultName);
        }
        
        String detailedResultTableName = qualifiedEmissionTableName(detailedResult);
        String detailedResultVersion = new VersionedQuery(version(detailedResult.getId(), detailedResult.getDefaultVersion()), "dr").query();

        EmfDataset inventory = null;
        Integer inventoryVersionNumber = null;
        //now lets determine the inventory
        //1st:  look in the arguments for it being explicitly set
        //2nd:  look in the keywords of the detailed result for the inventory name ("STRATEGY_INVENTORY_NAME") and version ("STRATEGY_INVENTORY_VERSION")
        if (inventoryName == null || inventoryName.length() == 0) {
            KeyVal[] keyVals = keyValFound(detailedResult, "STRATEGY_INVENTORY_NAME");
            if (keyVals.length > 0) {
                inventoryName = keyVals[0].getValue();
                //also get version number...
                keyVals = keyValFound(detailedResult, "STRATEGY_INVENTORY_VERSION");
                if (keyVals.length > 0) {
                    inventoryVersionNumber = new Integer(keyVals[0].getValue());
                }
            }
        }
        if (inventoryName == null || inventoryName.length() == 0) {
            errors += "Missing inventory dataset. ";
        }

        //go ahead and throw error from here, no need to validate anymore if the above is not there...
        if (errors.length() > 0) {
            throw new EmfException(errors);
        }

        inventory = getDataset(inventoryName);
        
        //use the inventory default version if one can't be found
        if (inventoryVersionNumber == null) 
            inventoryVersionNumber = inventory.getDefaultVersion();
        String inventoryTableName = qualifiedEmissionTableName(inventory);
        String inventoryVersion = new VersionedQuery(version(inventory.getId(), inventoryVersionNumber), "inv").query();
        boolean isPointInventory = inventory.getDatasetType().getName().equals(DatasetType.orlPointInventory);
        boolean isMergedInventory = inventory.getDatasetType().getName().equals(DatasetType.orlMergedInventory);
        boolean isNonPointInventory = inventory.getDatasetType().getName().equals(DatasetType.orlNonpointInventory);
        
        
//        capIsPoint = checkTableForColumns(emissionTableName(dataset), "plantid,pointid,stackid,segment");

        
        int month = inventory.applicableMonth();
        int noOfDaysInMonth = 31;
        if (month != -1) {
            noOfDaysInMonth = getDaysInMonth(inventory.getYear(), month);
        }
        
        //Outer SELECT clause
        sql = "select inv.fips,\n"
                + (isPointInventory || isMergedInventory ?
                        "inv.plantid,\n"
                        + "inv.pointid,\n"
                        + "inv.stackid,\n"
                        + "inv.segment,\n"
                        : 
                        ""
                        )
                + "inv.scc,\n"
                + "gspro.species as poll,\n"
                + "scc.scc_description,\n"
                + (isPointInventory || isMergedInventory ?
                        "inv.plant,\n"
                        : 
                        ""
                        )
                + (isPointInventory ?
                        "inv.nei_unique_id,\n"
                        : 
                        ""
                        )
                + "sum(coalesce(dr.input_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)) * gspro.massfrac) as inv_ann_emis,\n"
                + "sum(coalesce(dr.output_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)) * gspro.massfrac) as strat_ann_emis,\n"
                + "sum(coalesce(dr.input_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)) * gspro.massfrac) - sum(coalesce(dr.output_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)) * gspro.massfrac) as inv_minus_strat_emis,\n"
                + "case when sum(coalesce(dr.input_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis))) is not null and sum(coalesce(dr.input_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis))) != 0.0 then (sum(coalesce(dr.input_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis))) - sum(coalesce(dr.output_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)))) / (sum(coalesce(dr.input_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)))) * 100.0 else null end as pct_diff_inv_minus_strat,\n"
                + "dr.cm_abbrev,\n"
                + "dr.apply_order,\n"
                + "ct.name as control_technology,\n"
                + "sg.name as source_group,\n"
                + "sum(dr.annual_cost) as annual_cost\n"
                + (isPointInventory || isMergedInventory ?
                ",inv.design_capacity,\n"
                + "inv.design_capacity_unit_numerator,\n"
                + "inv.design_capacity_unit_denominator\n"
                : 
                ""
                )
                + (isPointInventory || isNonPointInventory ?
                ",inv.ANNUAL_AVG_DAYS_PER_WEEK,\n"
                + "inv.ANNUAL_AVG_WEEKS_PER_YEAR,\n"
                + "inv.ANNUAL_AVG_HOURS_PER_DAY,\n"
                + "inv.ANNUAL_AVG_HOURS_PER_YEAR\n"
                    : 
                    (isMergedInventory ?
                            ",inv.ANNUAL_AVG_HOURS_PER_YEAR\n\n"
                            : 
                            ""
                            )
                );

        sql += "from " + inventoryTableName + " inv\n"
            + "left outer join " + detailedResultTableName + " dr\n"
            + (isMergedInventory ?
                    "on dr.source_id = inv.original_record_id\n"
                    + "and dr.original_dataset_id = inv.original_dataset_id\n"
                    : 
                    "on dr.source_id = inv.record_id\n"
                    )
            + "and " + detailedResultVersion + " \n"
            + "left outer join emf.control_measures cm\n"
            + "on cm.abbreviation = dr.cm_abbrev\n"
            + "left outer join emf.control_technologies ct\n"
            + "on ct.id = cm.control_technology\n"
            + "left outer join emf.source_groups sg\n"
            + "on sg.id = cm.source_group\n"
            + "left outer join reference.scc\n"
            + "on scc.scc = inv.scc\n"
            ;

        //union together all ...
        //make sure an only get one of the GSREF records, choose randomly, it should be same code, so it shouldn't matter.
        sql += "inner join ( \n"
        + "select distinct on (scc, code, pollutant) scc, code, pollutant\n"
        + "from (\n";

        int i = 0;
        for (EmfDataset dataset : gsrefs) {
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion()), "smk").query();
            table = qualifiedEmissionTableName(dataset);
            if (i > 0) sql += " union \n";
            sql += "select scc, \n"
                + "code, \n"
                + "pollutant\n"
                + "from " + table + " smk \n"
                + "where " + version + "\n";
            ++i;
        }

        sql += ") gsref\n" 
            + ") gsref\n" 
            + "on gsref.scc = inv.scc\n"
            + "and gsref.pollutant = inv.poll\n";

        //union together all ...
        //make sure an only get one of the GSPRO records, the one with the greatest massfrac
        sql += "inner join ( \n"
            + "select distinct on (code, pollutant, species) code, pollutant, species, massfrac\n"
            + "from (\n";

        i = 0;
        for (EmfDataset dataset : gspros) {
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion()), "smk").query();
            table = qualifiedEmissionTableName(dataset);
            if (i > 0) sql += " union \n";
            sql += "select code,\n"
                + "pollutant,\n"
                + "species,\n"
                + "massfrac\n"
                + "from " + table + " smk \n"
                + "where " + version + "\n";
//                + "and species in ('PEC','POC')\n";
            ++i;
        }

        sql += ") gspro\n" 
            + "order by code, pollutant, species, massfrac desc\n"
            + ") gspro\n" 
            + "on gspro.code = gsref.code\n"
            + "and gspro.pollutant = 'PM2_5'\n";
        
        //added 'BRK__PM2_5','EXH__PM2_5','TIR__PM2_5' see bug 3396
        sql += "where inv.poll in ('PM2_5','BRK__PM2_5','EXH__PM2_5','TIR__PM2_5')\n"
            + "and " + inventoryVersion + "\n"
            + "group by inv.fips,\n"
            + (isPointInventory || isMergedInventory ?
                    "inv.plantid,\n"
                    + "inv.pointid,\n"
                    + "inv.stackid,\n"
                    + "inv.segment,\n"
                    : 
                    ""
                    )
            + "inv.scc,\n"
            + "gspro.species,\n"
            + "scc.scc_description,\n"
            + (isPointInventory || isMergedInventory ?
                    "inv.plant,\n"
                    : 
                    ""
                    )
            + (isPointInventory ?
                    "inv.nei_unique_id,\n"
                    : 
                    ""
                    )
            + "dr.cm_abbrev,\n"
            + "dr.apply_order,\n"
            + "ct.name,\n"
            + "sg.name\n"
            + (isPointInventory || isMergedInventory ?
                ",inv.design_capacity,\n"
                + "inv.design_capacity_unit_numerator,\n"
                + "inv.design_capacity_unit_denominator\n"
                : 
                ""
                )
                + (isPointInventory || isNonPointInventory ?
                ",inv.ANNUAL_AVG_DAYS_PER_WEEK,\n"
                + "inv.ANNUAL_AVG_WEEKS_PER_YEAR,\n"
                + "inv.ANNUAL_AVG_HOURS_PER_DAY,\n"
                + "inv.ANNUAL_AVG_HOURS_PER_YEAR\n"
                    : 
                    (isMergedInventory ?
                            ",inv.ANNUAL_AVG_HOURS_PER_YEAR\n\n"
                            : 
                            ""
                            )
                );
        //add PM2_5 records
        sql += "union all\n"
            + "select inv.fips,\n"
            + (isPointInventory || isMergedInventory ?
                    "inv.plantid,\n"
                    + "inv.pointid,\n"
                    + "inv.stackid,\n"
                    + "inv.segment,\n"
                    : 
                    ""
                    )
            + "inv.scc,\n"
            + "inv.poll,\n"
            + "scc.scc_description,\n"
            + (isPointInventory || isMergedInventory ?
                    "inv.plant,\n"
                    : 
                    ""
                    )
            + (isPointInventory ?
                    "inv.nei_unique_id,\n"
                    : 
                    ""
                    )
            + "coalesce(dr.input_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)) as inv_ann_emis,\n"
            + "coalesce(dr.output_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)) as strat_ann_emis,\n"
            + "coalesce(dr.input_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)) - coalesce(dr.output_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)) as inv_minus_strat_emis,\n"
            + "case when coalesce(dr.input_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)) is not null and coalesce(dr.input_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)) != 0.0 then (coalesce(dr.input_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis)) - coalesce(dr.output_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis))) / (coalesce(dr.input_emis, coalesce(inv.avd_emis * " + (month != -1 ? noOfDaysInMonth : "365") + ", inv.ann_emis))) * 100.0 else null end as pct_diff_inv_minus_strat,\n"
            + "dr.cm_abbrev,\n"
            + "dr.apply_order,\n"
            + "ct.name as control_technology,\n"
            + "sg.name as source_group,\n"
            + "dr.annual_cost\n"
            + (isPointInventory || isMergedInventory ?
                    ",inv.design_capacity,\n"
                    + "inv.design_capacity_unit_numerator,\n"
                    + "inv.design_capacity_unit_denominator\n"
                    : 
                    ""
                    )
                + (isPointInventory || isNonPointInventory ?
                ",inv.ANNUAL_AVG_DAYS_PER_WEEK,\n"
                + "inv.ANNUAL_AVG_WEEKS_PER_YEAR,\n"
                + "inv.ANNUAL_AVG_HOURS_PER_DAY,\n"
                + "inv.ANNUAL_AVG_HOURS_PER_YEAR\n"
                    : 
                    (isMergedInventory ?
                            ",inv.ANNUAL_AVG_HOURS_PER_YEAR\n\n"
                            : 
                            ""
                            )
                );

        sql += "from " + inventoryTableName + " inv\n"
            + "left outer join " + detailedResultTableName + " dr\n"
            + (isMergedInventory ?
                    "on dr.source_id = inv.original_record_id\n"
                    + "and dr.original_dataset_id = inv.original_dataset_id\n"
                    : 
                    "on dr.source_id = inv.record_id\n"
                    )
//            + "and coalesce(dr.original_dataset_id, inv.dataset_id) = inv.dataset_id\n"
            + "and " + detailedResultVersion + " \n"
            + "left outer join emf.control_measures cm\n"
            + "on cm.abbreviation = dr.cm_abbrev\n"
            + "left outer join emf.control_technologies ct\n"
            + "on ct.id = cm.control_technology\n"
            + "left outer join emf.source_groups sg\n"
            + "on sg.id = cm.source_group\n"
            + "left outer join reference.scc\n"
            + "on scc.scc = inv.scc\n"
            ;
    
        sql += "where inv.poll in ('PM2_5','BRK__PM2_5','EXH__PM2_5','TIR__PM2_5')\n"
            + "and " + inventoryVersion + "\n";

        sql += "order by fips"
            + (isPointInventory ?
                    ", plantid, pointid, stackid, segment"
                    : 
                    ""
                    )
            +", scc, cm_abbrev, poll\n";
        
//        sql = query(sql, true);
        sql = "CREATE TABLE " + emissionDatasourceName + "." + tableName + " AS " + sql;
        System.out.println(sql);
        
        return sql;
    }

    protected String query(String partialQuery, boolean createClause) throws EmfException {

        SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissionDatasourceName, tableName );
        return parser.parse(partialQuery, createClause);
    }

    protected boolean checkTableForColumns(String table, String colList) throws EmfException {
        String query = "select public.check_table_for_columns('" + table + "', '" + colList + "', ',');";
        ResultSet rs = null;
        boolean tableHasColumns = false;
        //System.out.println(System.currentTimeMillis() + " " + query);
        try {
            rs = dbServer.getEmissionsDatasource().query().executeQuery(query);
            while (rs.next()) {
                tableHasColumns = rs.getBoolean(1);
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
        return tableHasColumns;
    }

    private int getDaysInMonth(int year, int month) {
        return month != -1 ? DateUtil.daysInZeroBasedMonth(year, month) : 31;
    }
}
