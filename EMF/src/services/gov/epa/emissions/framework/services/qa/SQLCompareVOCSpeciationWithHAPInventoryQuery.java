package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.hibernate.Session;

public class SQLCompareVOCSpeciationWithHAPInventoryQuery extends SQLQAProgramQuery{
   
    public static final String capTag = "-cap";
    
    public static final String hapTag = "-hap";
    
    public static final String gstsiTag = "-gstsi";

    public static final String gscnvTag = "-gscnv";

    public static final String gspwTag = "-gspw";

    public static final String gsrefTag = "-gsref";

    public static final String filterTag = "-filter";
    
    private DbServer dbServer;

    ArrayList<String> baseDatasetNames = new ArrayList<String>();
    
    ArrayList<String> compareDatasetNames = new ArrayList<String>();
    
    //private boolean hasInvTableDataset;
    
    public SQLCompareVOCSpeciationWithHAPInventoryQuery(HibernateSessionFactory sessionFactory, DbServer dbServer, 
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
    
    private String parseSummaryType(String programSwitches, int beginIndex, int endIndex) {
        String value = "";
        String valuesString = "";
        
        valuesString = programSwitches.substring(beginIndex, endIndex);
        StringTokenizer tokenizer2 = new StringTokenizer(valuesString, "\n");
        tokenizer2.nextToken(); // skip the switch tag

        //get only the first value, that is not empty...
        while (tokenizer2.hasMoreTokens()) {
            value = tokenizer2.nextToken().trim();
            if (!value.isEmpty()) 
                break;
        }
        return value;
    }

    public String createCompareQuery() throws EmfException {
        String sql = "";
        String programArguments = qaStep.getProgramArguments();
        
        int capIndex = programArguments.indexOf(capTag);
        int hapIndex = programArguments.indexOf(hapTag);
        int gstsiIndex = programArguments.indexOf(gstsiTag);
        int gscnvIndex = programArguments.indexOf(gscnvTag);
        int gspwIndex = programArguments.indexOf(gspwTag);
        int gsrefIndex = programArguments.indexOf(gsrefTag);
        int sumTypeIndex = programArguments.indexOf(QAStep.summaryTypeTag);
        int filterIndex = programArguments.indexOf(filterTag);
        String capInventory = null;
        String hapInventory = null;
        String speciationToolSpecieInfoDataset = null;
        String pollToPollConversionDataset = null;
        String[] speciationProfileWeightDatasets = null; 
        String[] speciationCrossReferenceDatasets = null;
        String[] arguments;
        String summaryType;
        String filter;
        String version;
        String table;
        boolean capIsPoint = false;
        boolean hapIsPoint = false;
        boolean capHasSIC = false;
        boolean hapHasSIC = false;
        boolean capHasMACT = false;
        boolean hapHasMACT = false;
        boolean capHasNAICS = false;
        boolean hapHasNAICS = false;


        if (capIndex != -1) {
            arguments = parseSwitchArguments(programArguments, capIndex, programArguments.indexOf("\n-", capIndex) != -1 ? programArguments.indexOf("\n-", capIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                capInventory = arguments[0];
                datasetNames.add(capInventory);
            }
        }
        if (hapIndex != -1) {
            arguments = parseSwitchArguments(programArguments, hapIndex, programArguments.indexOf("\n-", hapIndex) != -1 ? programArguments.indexOf("\n-", hapIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                hapInventory = arguments[0];
                datasetNames.add(hapInventory);
            }
        }
        if (gstsiIndex != -1) {
            arguments = parseSwitchArguments(programArguments, gstsiIndex, programArguments.indexOf("\n-", gstsiIndex) != -1 ? programArguments.indexOf("\n-", gstsiIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                speciationToolSpecieInfoDataset = arguments[0];
                datasetNames.add(speciationToolSpecieInfoDataset);
            }
        }
        if (gscnvIndex != -1) {
            arguments = parseSwitchArguments(programArguments, gscnvIndex, programArguments.indexOf("\n-", gscnvIndex) != -1 ? programArguments.indexOf("\n-", gscnvIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                pollToPollConversionDataset = arguments[0];
                datasetNames.add(pollToPollConversionDataset);
            }
        }
        if (gspwIndex != -1) {
            arguments = parseSwitchArguments(programArguments, gspwIndex, programArguments.indexOf("\n-", gspwIndex) != -1 ? programArguments.indexOf("\n-", gspwIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                speciationProfileWeightDatasets = arguments;
                for (String item : speciationProfileWeightDatasets)
                    datasetNames.add(item);
            }
        }
        if (gsrefIndex != -1) {
            arguments = parseSwitchArguments(programArguments, gsrefIndex, programArguments.indexOf("\n-", gsrefIndex) != -1 ? programArguments.indexOf("\n-", gsrefIndex) : programArguments.length());
            if (arguments != null && arguments.length > 0) {
                speciationCrossReferenceDatasets = arguments;
                for (String item : speciationCrossReferenceDatasets)
                    datasetNames.add(item);
            }
        }
        summaryType = parseSummaryType(programArguments, sumTypeIndex, programArguments.indexOf("\n-", sumTypeIndex) != -1 ? programArguments.indexOf("\n-", sumTypeIndex) : programArguments.length());
        filter = parseSummaryType(programArguments, filterIndex, programArguments.indexOf("\n-", filterIndex) != -1 ? programArguments.indexOf("\n-", filterIndex) : programArguments.length());

//        System.out.println(summaryType);
//        System.out.println(filter);
        
        //validate everything has been specified...
        String errors = "";
        if (capInventory == null) {
            errors = "Missing CAP inventory. ";
        }
        if (hapInventory == null) {
            errors += "Missing HAP inventory. ";
        }
        if (speciationToolSpecieInfoDataset == null) {
            errors += "Missing Speciation Tool Gas Profiles Dataset. ";
        }
        if (pollToPollConversionDataset == null) {
            errors += "Missing Pollutant-To-Pollutant Conversion Dataset. ";
        }
        if (speciationProfileWeightDatasets == null || speciationProfileWeightDatasets.length == 0) {
            errors += "Missing Speciation Profile Weight Dataset(s). ";
        }
        if (speciationCrossReferenceDatasets == null || speciationCrossReferenceDatasets.length == 0) {
            errors += "Missing Speciation Cross Reference Dataset(s). ";
        }
        if (summaryType == null || summaryType.length() == 0) {
            errors += "Missing summary type value. ";
        }
        if (filter != null) {
            filter = filter.trim();
            filter = Pattern.compile("sic", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("i.sic");
            filter = Pattern.compile("mact", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("i.mact");
            filter = Pattern.compile("naics", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("i.naics");
            filter = Pattern.compile("fips", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("i.fips");
            filter = Pattern.compile("scc", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("i.scc");
            filter = Pattern.compile("poll", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("i.poll");
            filter = Pattern.compile("plantid", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("i.plantid");
            filter = Pattern.compile("pointid", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("i.pointid");
            filter = Pattern.compile("stackid", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("i.stackid");
            filter = Pattern.compile("segment", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("i.segment");
            filter = Pattern.compile("plant", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("i.plant");
            filter = Pattern.compile("'", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("''");
        } else
            filter = "";

        if (errors.length() > 0) {
            throw new EmfException(errors);
        }
        
        checkDataset(); // check existance of datasets
        
        EmfDataset dataset = getDataset(capInventory);
        String capInventoryTable = qualifiedEmissionTableName(dataset);
        String capInventoryVersion = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion()), "i").query();
        //lets make sure the inventories are indexed and vacuumed
        indexORLInventory(emissionTableName(dataset));
        //lets make sure the sources table is populated from the input inventories 
        populateSourcesTable(emissionTableName(dataset), (capInventoryVersion + (filter.length() == 0 ? "" : " and (" + filter.replaceAll("''", "'") + ")")).replace("i.",""));
        capIsPoint = checkTableForColumns(emissionTableName(dataset), "plantid,pointid,stackid,segment");
        capHasSIC = checkTableForColumns(emissionTableName(dataset), "sic");
        capHasMACT = checkTableForColumns(emissionTableName(dataset), "mact");
        capHasNAICS = checkTableForColumns(emissionTableName(dataset), "naics");
        dataset = getDataset(hapInventory);
        String hapInventoryTable = qualifiedEmissionTableName(dataset);
        String hapInventoryVersion = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion()), "i").query();
        //lets make sure the inventories are indexed and vacuumed
        indexORLInventory(emissionTableName(dataset));
        //lets make sure the sources table is populated from the input inventories 
        populateSourcesTable(emissionTableName(dataset), (hapInventoryVersion + (filter.length() == 0 ? "" : " and (" + filter.replaceAll("''", "'") + ")")).replace("i.",""));
        hapIsPoint = checkTableForColumns(emissionTableName(dataset), "plantid,pointid,stackid,segment");
        hapHasSIC = checkTableForColumns(emissionTableName(dataset), "sic");
        hapHasMACT = checkTableForColumns(emissionTableName(dataset), "mact");
        hapHasNAICS = checkTableForColumns(emissionTableName(dataset), "naics");
        dataset = getDataset(speciationToolSpecieInfoDataset);
        String speciationToolSpecieInfoDatasetTable = qualifiedEmissionTableName(dataset);
        String speciationToolSpecieInfoDatasetVersion = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion()), "p").query();
        dataset = getDataset(pollToPollConversionDataset);
        String pollToPollConversionDatasetTable = qualifiedEmissionTableName(dataset);
        String pollToPollConversionDatasetVersion = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion()), "g").query();
        
        

        String capSql = "";
        String hapSql = "";
        String capDatasourceSql = "";
        String hapDatasourceSql = "";

        if (summaryType.equals("By NAICS")) {
            sql = "select upper(tbl.naics) as naics, coalesce(n.naics_description, 'UNSPECIFIED') as naics_description, \n";
        } else if (summaryType.equals("By MACT")) {
            sql = "select upper(tbl.mact) as mact, coalesce(m.mact_source_category, 'UNSPECIFIED') as mact_description, \n";
        } else if (summaryType.equals("By SIC")) {
            sql = "select upper(tbl.sic) as sic, coalesce(s.description, 'UNSPECIFIED') as sic_description, \n";
        } else if (summaryType.equals("By Profile Code")) {
            sql = "select profile_code, coalesce(profile_name,'NO PROFILE') as profile_name, profile_date, documentation, \n";
        } else if (summaryType.equals("By SCC")) {
            sql = "select tbl.scc, coalesce(s.scc_description,'UNSPECIFIED') as scc_description, tbl.profile_code, coalesce(p.profile_name,'NO PROFILE') as profile_name, p.date_added as profile_date, documentation, \n";
            
            sql += "    ( select percent from ( \n";
            for (int i = 0; i < speciationProfileWeightDatasets.length; i++) {
                if (i > 0) sql += " union \n";
                dataset = getDataset(speciationProfileWeightDatasets[i]);
                version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
                table = qualifiedEmissionTableName(dataset);
                sql += "       select profile_id, percent::double precision \n"
                    + "       from " + table + " s \n"
                    + "       where specie_id = '302' \n"
                    + "       and " + version;
            }
            sql += "        ) rp \n"
                + "        where rp.profile_id = tbl.profile_code \n"
                + "        limit 1) as benzene_percent, \n";
            sql += "    ( select percent from ( \n";
            for (int i = 0; i < speciationProfileWeightDatasets.length; i++) {
                if (i > 0) sql += " union \n";
                dataset = getDataset(speciationProfileWeightDatasets[i]);
                version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
                table = qualifiedEmissionTableName(dataset);
                sql += "       select profile_id, percent::double precision \n"
                    + "       from " + table + " s \n"
                    + "       where specie_id = '465' \n"
                    + "       and " + version;
            }
            sql += "        ) rp \n"
                + "        where rp.profile_id = tbl.profile_code \n"
                + "        limit 1) as formaldehyde_percent, \n";
            sql += "    ( select percent from ( \n";
            for (int i = 0; i < speciationProfileWeightDatasets.length; i++) {
                if (i > 0) sql += " union \n";
                dataset = getDataset(speciationProfileWeightDatasets[i]);
                version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
                table = qualifiedEmissionTableName(dataset);
                sql += "       select profile_id, percent::double precision \n"
                    + "       from " + table + " s \n"
                    + "       where specie_id = '279' \n"
                    + "       and " + version;
            }
            sql += "        ) rp \n"
                + "        where rp.profile_id = tbl.profile_code \n"
                + "        limit 1) as acetaldehyde_percent, \n";
            sql += "    ( select percent from ( \n";
            for (int i = 0; i < speciationProfileWeightDatasets.length; i++) {
                if (i > 0) sql += " union \n";
                dataset = getDataset(speciationProfileWeightDatasets[i]);
                version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
                table = qualifiedEmissionTableName(dataset);
                sql += "       select profile_id, percent::double precision \n"
                    + "       from " + table + " s \n"
                    + "       where specie_id = '531' \n"
                    + "       and " + version;
            }
            sql += "        ) rp \n"
                + "        where rp.profile_id = tbl.profile_code \n"
                + "        limit 1) as methanol_percent, \n";

        
        } else if (summaryType.equals("By NEI Unique Id")) {
            sql = "select nei.fips, tbl.nei_unique_id, "
                + (
                    capIsPoint || hapIsPoint 
                    ? "nei.plant, \n"
                    : ""
                    )
                + "    nei.sic, coalesce(s2.description, 'UNSPECIFIED') as sic_description,  \n"
                + "    nei.mact, coalesce(m.mact_source_category, 'UNSPECIFIED') as mact_description, \n"
                + "    nei.naics, coalesce(n.naics_description, 'UNSPECIFIED') as naics_description, \n"
                + "    voc,  \n"
                + "    cap_acetaldehyde, \n"
                + "    hap_acetaldehyde,  \n"
                + "    over_acetaldehyde,  \n"
                + "    under_acetaldehyde,  \n"
                + "    cap_acetaldehyde_records, \n" 
                + "    hap_acetaldehyde_records,  \n"
                + "    cap_benzene,  \n"
                + "    hap_benzene, \n"
                + "    over_benzene,  \n"
                + "    under_benzene,  \n"
                + "    cap_benzene_records,  \n"
                + "    hap_benzene_records,  \n"
                + "    cap_formaldehyde,  \n"
                + "    hap_formaldehyde, \n"
                + "    over_formaldehyde,  \n"
                + "    under_formaldehyde,  \n"
                + "    cap_formaldehyde_records,  \n"
                + "    hap_formaldehyde_records,  \n"
                + "    cap_methanol,  \n"
                + "    hap_methanol,  \n"
                + "    over_methanol,  \n"
                + "    under_methanol, \n"
                + "    cap_methanol_records,  \n"
                + "    hap_methanol_records \n"
                + "from ( \n"
                + "    select  \n"
                + "    tbl.nei_unique_id, \n";
        }
        //select tbl.scc, coalesce(s.scc_description,'UNSPECIFIED') as scc_description,
        
        if (summaryType.equals("By NAICS") 
            || summaryType.equals("By MACT")
            || summaryType.equals("By SIC")
            || summaryType.equals("By Profile Code")
            || summaryType.equals("By SCC")
            || summaryType.equals("By NEI Unique Id")) {
            sql += "sum(voc) as voc,  \n"
                + "sum(cap_acetaldehyde) as cap_acetaldehyde, \n"
                + "sum(hap_acetaldehyde) as hap_acetaldehyde,  \n"
                + "sum(over_acetaldehyde) as over_acetaldehyde,  \n"
                + "sum(under_acetaldehyde) as under_acetaldehyde,  \n"
                + "sum(case when cap_acetaldehyde is not null then 1 else 0 end) as cap_acetaldehyde_records, \n" 
                + "sum(case when hap_acetaldehyde is not null then 1 else 0 end) as hap_acetaldehyde_records,  \n"
                + "sum(cap_benzene) as cap_benzene,  \n"
                + "sum(hap_benzene) as hap_benzene, \n"
                + "sum(over_benzene) as over_benzene,  \n"
                + "sum(under_benzene) as under_benzene,  \n"
                + "sum(case when cap_benzene is not null then 1 else 0 end) as cap_benzene_records, \n" 
                + "sum(case when hap_benzene is not null then 1 else 0 end) as hap_benzene_records,  \n"
                + "sum(cap_formaldehyde) as cap_formaldehyde,  \n"
                + "sum(hap_formaldehyde) as hap_formaldehyde, \n"
                + "sum(over_formaldehyde) as over_formaldehyde,  \n"
                + "sum(under_formaldehyde) as under_formaldehyde,  \n"
                + "sum(case when cap_formaldehyde is not null then 1 else 0 end) as cap_formaldehyde_records, \n" 
                + "sum(case when hap_formaldehyde is not null then 1 else 0 end) as hap_formaldehyde_records,  \n"
                + "sum(cap_methanol) as cap_methanol,  \n"
                + "sum(hap_methanol) as hap_methanol,  \n"
                + "sum(over_methanol) as over_methanol,  \n"
                + "sum(under_methanol) as under_methanol, \n"
                + "sum(case when cap_methanol is not null then 1 else 0 end) as cap_methanol_records, \n" 
                + "sum(case when hap_methanol is not null then 1 else 0 end) as hap_methanol_records \n"
                + "from ( \n";
        }
//upper(tbl.mact) as mact, coalesce(m.mact_source_category, 'UNSPECIFIED') as mact_description,
        
        sql += "select  \n"
        + "c.fips,  \n"
        + "c.scc,  \n"
        + (summaryType.equals("Details") 
            ? "coalesce(s.scc_description,'UNSPECIFIED') as scc_description, \n" 
            : "") 
        + (
            capIsPoint || hapIsPoint 
            ? "c.nei_unique_id, \n"
            + "c.plantid, \n"
            + "c.pointid, \n"
            + "c.stackid, \n"
            + "c.segment, \n"
            + "c.plant, \n"
            : "c.nei_unique_id, \n"
            )
        + "c.sic,  \n"
        + "c.mact, \n"
        + "c.naics, \n"
        + "c.ann_emis as voc,  \n"
        + "c.tog, \n"
        + "c.factor as gscnv_factor,  \n"
        + "case  \n"
        + "    when coalesce(cap_acetaldehyde,cap_benzene,cap_formaldehyde,cap_methanol) is not null  \n"
        + "        and coalesce(hap_acetaldehyde,hap_benzene,hap_formaldehyde,hap_methanol) is not null then  \n"
        + "        true \n"
        + "    else \n"
        + "        false \n"
        + "end as match,  \n"
        + "cap_acetaldehyde, \n"
        + "hap_acetaldehyde,  \n"
        + "case  \n"
        + "    when coalesce(cap_acetaldehyde,0.0) - coalesce(hap_acetaldehyde,0.0) > 0.0 then  \n"
        + "        coalesce(cap_acetaldehyde,0.0) - coalesce(hap_acetaldehyde,0.0)  \n"
        + "    else \n"
        + "        null::double precision \n"
        + "end as over_acetaldehyde,  \n"
        + "case  \n"
        + "    when coalesce(cap_acetaldehyde,0.0) - coalesce(hap_acetaldehyde,0.0) < 0.0 then  \n"
        + "        coalesce(cap_acetaldehyde,0.0) - coalesce(hap_acetaldehyde,0.0)  \n"
        + "    else \n"
        + "        null::double precision \n"
        + "end as under_acetaldehyde,  \n"
        + "cap_benzene,  \n"
        + "hap_benzene, \n"
        + "case  \n"
        + "    when coalesce(cap_benzene,0.0) - coalesce(hap_benzene,0.0) > 0.0 then  \n"
        + "        coalesce(cap_benzene,0.0) - coalesce(hap_benzene,0.0)  \n"
        + "    else \n"
        + "        null::double precision \n"
        + "end as over_benzene,  \n"
        + "case  \n"
        + "    when coalesce(cap_benzene,0.0) - coalesce(hap_benzene,0.0) < 0.0 then  \n"
        + "        coalesce(cap_benzene,0.0) - coalesce(hap_benzene,0.0)  \n"
        + "    else \n"
        + "        null::double precision \n"
        + "end as under_benzene,  \n"
        + "cap_formaldehyde,  \n"
        + "hap_formaldehyde, \n"
        + "case  \n"
        + "    when coalesce(cap_formaldehyde,0.0) - coalesce(hap_formaldehyde,0.0) > 0.0 then  \n"
        + "        coalesce(cap_formaldehyde,0.0) - coalesce(hap_formaldehyde,0.0)  \n"
        + "    else \n"
        + "        null::double precision \n"
        + "end as over_formaldehyde,  \n"
        + "case  \n"
        + "    when coalesce(cap_formaldehyde,0.0) - coalesce(hap_formaldehyde,0.0) < 0.0 then  \n"
        + "        coalesce(cap_formaldehyde,0.0) - coalesce(hap_formaldehyde,0.0)  \n"
        + "    else \n"
        + "        null::double precision \n"
        + "end as under_formaldehyde,  \n"
        + "cap_methanol,  \n"
        + "hap_methanol,  \n"
        + "case  \n"
        + "    when coalesce(cap_methanol,0.0) - coalesce(hap_methanol,0.0) > 0.0 then  \n"
        + "        coalesce(cap_methanol,0.0) - coalesce(hap_methanol,0.0)  \n"
        + "    else \n"
        + "        null::double precision \n"
        + "end as over_methanol,  \n"
        + "case  \n"
        + "    when coalesce(cap_methanol,0.0) - coalesce(hap_methanol,0.0) < 0.0 then  \n"
        + "        coalesce(cap_methanol,0.0) - coalesce(hap_methanol,0.0)  \n"
        + "    else \n"
        + "        null::double precision \n"
        + "end as under_methanol,  \n"
        + "cap_data_source,  \n"
        + "hap_acetaldehyde_data_source,  \n"
        + "hap_benzene_data_source,  \n"
        + "hap_formaldehyde_data_source,  \n"
        + "hap_methanol_data_source, \n"
        + "speciated_code as profile_code \n"
        + (summaryType.equals("By Profile Code") || summaryType.equals("Details")
            ? ", coalesce(p.profile_name,'NO PROFILE') as profile_name, \n"
        + "p.date_added as profile_date, p.documentation \n" 
            : "");
 
        if (summaryType.equals("Details")) {
            
            sql += "    ,( select percent from ( \n";
            for (int i = 0; i < speciationProfileWeightDatasets.length; i++) {
                if (i > 0) sql += " union \n";
                dataset = getDataset(speciationProfileWeightDatasets[i]);
                version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
                table = qualifiedEmissionTableName(dataset);
                sql += "       select profile_id, percent::double precision \n"
                    + "       from " + table + " s \n"
                    + "       where specie_id = '302' \n"
                    + "       and " + version;
            }
            sql += "        ) rp \n"
                + "        where rp.profile_id = c.speciated_code \n"
                + "        limit 1) as benzene_percent, \n";
            sql += "    ( select percent from ( \n";
            for (int i = 0; i < speciationProfileWeightDatasets.length; i++) {
                if (i > 0) sql += " union \n";
                dataset = getDataset(speciationProfileWeightDatasets[i]);
                version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
                table = qualifiedEmissionTableName(dataset);
                sql += "       select profile_id, percent::double precision \n"
                    + "       from " + table + " s \n"
                    + "       where specie_id = '465' \n"
                    + "       and " + version;
            }
            sql += "        ) rp \n"
                + "        where rp.profile_id = c.speciated_code \n"
                + "        limit 1) as formaldehyde_percent, \n";
            sql += "    ( select percent from ( \n";
            for (int i = 0; i < speciationProfileWeightDatasets.length; i++) {
                if (i > 0) sql += " union \n";
                dataset = getDataset(speciationProfileWeightDatasets[i]);
                version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
                table = qualifiedEmissionTableName(dataset);
                sql += "       select profile_id, percent::double precision \n"
                    + "       from " + table + " s \n"
                    + "       where specie_id = '279' \n"
                    + "       and " + version;
            }
            sql += "        ) rp \n"
                + "        where rp.profile_id = c.speciated_code \n"
                + "        limit 1) as acetaldehyde_percent, \n";
            sql += "    ( select percent from ( \n";
            for (int i = 0; i < speciationProfileWeightDatasets.length; i++) {
                if (i > 0) sql += " union \n";
                dataset = getDataset(speciationProfileWeightDatasets[i]);
                version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
                table = qualifiedEmissionTableName(dataset);
                sql += "       select profile_id, percent::double precision \n"
                    + "       from " + table + " s \n"
                    + "       where specie_id = '531' \n"
                    + "       and " + version;
            }
            sql += "        ) rp \n"
                + "        where rp.profile_id = c.speciated_code \n"
                + "        limit 1) as methanol_percent \n";
        }
            
        sql += "from crosstab( \n"
        + "' \n";

        //figure out the cap speciated records...
        capSql = ""
        + "--cap speciated records (inner join makes sure we only create records for known mappings..., unkown mappings will  \n"
        + "--taken care of during the datasource record creation) \n"
        + "select distinct on (s.id, i.poll, rp.specie_id) \n"
        + " \n"
        + "    s.id, \n"
        + (
            capIsPoint 
            ? "i.nei_unique_id,  \n"
            + "i.plantid, \n"
            + "i.pointid,  \n"
            + "i.stackid,  \n"
            + "i.segment,  \n"
            + "i.plant, \n"
            : 
                hapIsPoint 
                ? "null::character varying(20) as nei_unique_id,  \n"
                + "null::character varying(15) as plantid, \n"
                + "null::character varying(15) as pointid,  \n"
                + "null::character varying(15) as stackid,  \n"
                + "null::character varying(15) as segment,  \n"
                + "null::character varying(40) as plant, \n"
                : "null::character varying(20) as nei_unique_id,  \n"
            )
        + "    i.fips,  \n"
        + "    i.scc,  \n"
        + "    i.poll, \n"
        + (
            capHasSIC
            ? "i.sic, \n"
            : "null::character varying(4) as sic, \n"
            )
        + (
            capHasMACT
            ? "i.mact, \n"
            : "null::character varying(6) as mact, \n"
            )
        + (
            capHasNAICS
            ? "i.naics, \n"
            : "null::character varying(6) as naics, \n"
            )
        + "    i.ann_emis,  \n"
        + "    r.code,  \n"
        + "    case when r.code <> ''COMBO'' then coalesce(g.factor, 1.0) else null::double precision end as factor,  \n"
        + "    case when r.code <> ''COMBO'' then i.ann_emis * coalesce(g.factor, 1.0) else null::double precision end as tog,  \n"
        + "    case  \n"
        + "        when rp.specie_id = 302 then ''cap_benzene'' \n"
        + "        when rp.specie_id = 465 then ''cap_formaldehyde'' \n"
        + "        when rp.specie_id = 279 then ''cap_acetaldehyde'' \n"
        + "        when rp.specie_id = 531 then ''cap_methanol'' \n"
        + "        else '''' \n"
        + "    end as attr, \n"
        + "    case when r.code <> ''COMBO'' then i.ann_emis * coalesce(g.factor, 1) * rp.percent / 100 || '''' else null::character varying end as value \n"
        + " \n"
        + "from " + capInventoryTable + " i \n"
        + " \n"
        + "    inner join emf.sources s \n"
        + "    on s.source = i.scc || i.fips || "
        + (
            capIsPoint
            ? "rpad(coalesce(i.plantid, ''''), 15) || rpad(coalesce(i.pointid, ''''), 15) || rpad(coalesce(i.stackid, ''''), 15) || rpad(coalesce(i.segment, ''''), 15) \n"
            : "repeat('' '', 60)  \n"
            )
        + " \n"
        + "   -- get profilecode by SCC and pollutant \n"
        + "   inner join ( \n";
        for (int i = 0; i < speciationCrossReferenceDatasets.length; i++) {
            if (i > 0) capSql += " union \n";
            dataset = getDataset(speciationCrossReferenceDatasets[i]);
            table = qualifiedEmissionTableName(dataset);
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
            capSql += "       select scc, code \n"
                + "        from " + table + " s \n"
                + "        where pollutant = ''VOC'' \n"
                + "        and " + version.replaceAll("'", "''");
        }
        capSql += "        ) r \n"
        + "        on i.scc = r.scc \n"
        + " \n"
        + "   -- get factor by profilecode and pollutant \n"
        + "    inner join " + pollToPollConversionDatasetTable + " g \n"
        + "    on g.speciation_code = r.code \n"
        + "    and g.pollutant_1 = ''VOC'' \n"
        + "    and " + pollToPollConversionDatasetVersion.replaceAll("'", "''") + " \n"
        + " \n"
        + "    -- speciate based on raw profile data \n"
        + "    inner join ( \n";
        if (speciationProfileWeightDatasets!=null) {
        for (int i = 0; i < speciationProfileWeightDatasets.length; i++) {
            if (i > 0) capSql += " union \n";
            dataset = getDataset(speciationProfileWeightDatasets[i]);
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
            table = qualifiedEmissionTableName(dataset);
            capSql += "       select profile_id, percent::double precision, specie_id::integer \n"
                + "       from " + table + " s \n"
                + "       where specie_id in (''302'',''465'',''279'',''531'') \n"
                + "       and " + version.replaceAll("'", "''");
        }
        }

        capSql += "        ) rp \n"
        + "        on rp.profile_id = g.speciation_code \n"
        
        + " \n"
        + "where i.poll = ''VOC''  \n"
        + "and (" + capInventoryVersion.replaceAll("'", "''") + ") \n"
        + (filter.length() > 0 ? "and (" + filter + ") \n" : "") 
        + " \n";
        
        sql += capSql;
        
        //figure out the hap records...
        sql += " union all \n";
        
        hapSql += " --hap voc records (inner join makes sure we only create records for known mappings..., unkown mappings will  \n"
        + " --taken care of during the datasource record creation) \n"
        + "select distinct on (s.id, i.poll) \n"
        + "    s.id, \n"
        + (
            hapIsPoint 
            ? "i.nei_unique_id,  \n"
            + "i.plantid, \n"
            + "i.pointid,  \n"
            + "i.stackid,  \n"
            + "i.segment,  \n"
            + "i.plant, \n"
            : 
                capIsPoint 
                ? "null::character varying(20) as nei_unique_id,  \n"
                + "null::character varying(15) as plantid, \n"
                + "null::character varying(15) as pointid,  \n"
                + "null::character varying(15) as stackid,  \n"
                + "null::character varying(15) as segment,  \n"
                + "null::character varying(40) as plant, \n"
                : "null::character varying(20) as nei_unique_id,  \n"
            )
        + "    i.fips, \n"
        + "    i.scc, \n"
        + "    ''VOC'' as poll, \n"
        + (
            hapHasSIC
            ? "i.sic, \n"
            : "null::character varying(4) as sic, \n"
            )
        + (
            hapHasMACT
            ? "i.mact, \n"
            : "null::character varying(6) as mact, \n"
            )
        + (
            hapHasNAICS
            ? "i.naics, \n"
            : "null::character varying(6) as naics, \n"
            )
        + "    null::double precision as ann_emis, \n"
        + "    r.code,  \n"
        + "    case when r.code <> ''COMBO'' then coalesce(g.factor, 1.0) else null::double precision end as factor,  \n"
        + "    null::double precision as tog,  \n"
        + "    case  \n"
        + "       when i.poll = ''71432'' then ''hap_benzene'' \n"
        + "       when i.poll = ''50000'' then ''hap_formaldehyde'' \n"
        + "       when i.poll = ''75070'' then ''hap_acetaldehyde'' \n"
        + "       when i.poll = ''67561'' then ''hap_methanol'' \n"
        + "       else '''' \n"
        + "    end as attr, \n"
        + "    i.ann_emis || '''' as value \n"
        + " \n"
        + "from " + hapInventoryTable + " i \n"
        + " \n"
        + "    inner join emf.sources s \n"
        + "    on s.source = i.scc || i.fips || "
        + (
            hapIsPoint
            ? "rpad(coalesce(i.plantid, ''''), 15) || rpad(coalesce(i.pointid, ''''), 15) || rpad(coalesce(i.stackid, ''''), 15) || rpad(coalesce(i.segment, ''''), 15) \n"
            : "repeat('' '', 60)  \n"
            )
        + " \n"
        + "   -- get profilecode by SCC and pollutant \n"
        + "   left outer join ( \n";
        for (int i = 0; i < speciationCrossReferenceDatasets.length; i++) {
            if (i > 0) hapSql += " union \n";
            dataset = getDataset(speciationCrossReferenceDatasets[i]);
            table = qualifiedEmissionTableName(dataset);
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
            hapSql += "       select scc, code \n"
                + "        from " + table + " s \n"
                + "        where pollutant = ''VOC'' \n"
                + "        and " + version.replaceAll("'", "''");
        }
        hapSql += "        ) r \n"
        + "        on i.scc = r.scc \n"
        + " \n"
        + "    -- get factor by profilecode and pollutant \n"
        + "    left outer join " + pollToPollConversionDatasetTable + " g \n"
        + "    on g.speciation_code = r.code \n"
        + "    and g.pollutant_1 = ''VOC'' \n"
        + "    and " + pollToPollConversionDatasetVersion.replaceAll("'", "''") + " \n"
        + " \n"
        + "where i.poll in (''71432'',''50000'',''75070'',''67561'') \n"
        + "and (" + hapInventoryVersion.replaceAll("'", "''") + ") \n"
        + (filter.length() > 0 ? "and (" + filter + ") \n" : "") 
        + " \n";
        
        sql += hapSql;
        
        sql += "union all \n";
        
        capDatasourceSql = " --cap datasource records (this will also include any voc records that do not have any profile mappings) \n"
        + "select distinct on (s.id) \n"
        + "    s.id, \n"
        + (
                capIsPoint 
                ? "i.nei_unique_id,  \n"
                + "i.plantid, \n"
                + "i.pointid,  \n"
                + "i.stackid,  \n"
                + "i.segment,  \n"
                + "i.plant, \n"
                : 
                    hapIsPoint 
                    ? "null::character varying(20) as nei_unique_id,  \n"
                    + "null::character varying(15) as plantid, \n"
                    + "null::character varying(15) as pointid,  \n"
                    + "null::character varying(15) as stackid,  \n"
                    + "null::character varying(15) as segment,  \n"
                    + "null::character varying(40) as plant, \n"
                    : "null::character varying(20) as nei_unique_id,  \n"
                )
        + "   i.fips,  \n"
        + "   i.scc,  \n"
        + "   i.poll,  \n"
        + (
            capHasSIC
            ? "i.sic, \n"
            : "null::character varying(4) as sic, \n"
            )
        + (
            capHasMACT
            ? "i.mact, \n"
            : "null::character varying(6) as mact, \n"
            )
        + (
            capHasNAICS
            ? "i.naics, \n"
            : "null::character varying(6) as naics, \n"
            )
        + "    i.ann_emis,  \n"
        + "    r.code,  \n"
        + "    case when r.code <> ''COMBO'' then coalesce(g.factor, 1.0) else null::double precision end as factor,  \n"
        + "    case when r.code <> ''COMBO'' then i.ann_emis * coalesce(g.factor, 1.0) else null::double precision end as tog,  \n"
        + "    ''cap_data_source'' as attr, \n"
        + "    i.data_source as value \n"
        + " \n"
        + " from " + capInventoryTable + " i \n"
        + "    inner join emf.sources s \n"
        + "    on s.source = i.scc || i.fips || "
        + (
            capIsPoint
            ? "rpad(coalesce(i.plantid, ''''), 15) || rpad(coalesce(i.pointid, ''''), 15) || rpad(coalesce(i.stackid, ''''), 15) || rpad(coalesce(i.segment, ''''), 15) \n"
            : "repeat('' '', 60)  \n"
            )
        + " \n"
        + "    -- get profilecode by SCC and pollutant \n"
        + "    left outer join ( \n";
        for (int i = 0; i < speciationCrossReferenceDatasets.length; i++) {
            if (i > 0) capDatasourceSql += " union \n";
            dataset = getDataset(speciationCrossReferenceDatasets[i]);
            table = qualifiedEmissionTableName(dataset);
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
            capDatasourceSql += "       select scc, code \n"
                + "        from " + table + " s \n"
                + "        where pollutant = ''VOC'' \n"
                + "        and " + version.replaceAll("'", "''");
        }
        capDatasourceSql += "        ) r \n"
        + "        on i.scc = r.scc \n"
        + " \n"
        + "    -- get factor by profilecode and pollutant \n"
        + "    left outer join " + pollToPollConversionDatasetTable + " g \n"
        + "    on g.speciation_code = r.code \n"
        + "    and g.pollutant_1 = ''VOC'' \n"
        + "    and " + pollToPollConversionDatasetVersion.replaceAll("'", "''") + " \n"
        + " \n"
        + " where i.poll = ''VOC'' \n"
        + " and (" + capInventoryVersion.replaceAll("'", "''") + ") \n"
        + (filter.length() > 0 ? "and (" + filter + ") \n" : "") 
        + " \n";

        sql += capDatasourceSql;
        
        sql += " union \n";
        
        hapDatasourceSql = " --hap datasource records (this will also include any applicable hap records that do not have any profile mappings) \n"
        + "select --distinct on (s.id) \n"
        + "    s.id, \n"
        + (
            hapIsPoint 
            ? "i.nei_unique_id,  \n"
            + "i.plantid, \n"
            + "i.pointid,  \n"
            + "i.stackid,  \n"
            + "i.segment,  \n"
            + "i.plant, \n"
            : 
                capIsPoint 
                ? "null::character varying(20) as nei_unique_id,  \n"
                + "null::character varying(15) as plantid, \n"
                + "null::character varying(15) as pointid,  \n"
                + "null::character varying(15) as stackid,  \n"
                + "null::character varying(15) as segment,  \n"
                + "null::character varying(40) as plant, \n"
                : "null::character varying(20) as nei_unique_id,  \n"
            )
        + "   i.fips,  \n"
        + "   i.scc,  \n"
        + "   i.poll,  \n"
        + (
            hapHasSIC
            ? "i.sic, \n"
            : "null::character varying(4) as sic, \n"
            )
        + (
            hapHasMACT
            ? "i.mact, \n"
            : "null::character varying(6) as mact, \n"
            )
        + (
            hapHasNAICS
            ? "i.naics, \n"
            : "null::character varying(6) as naics, \n"
            )
        + "    null::double precision as ann_emis,  \n"
        + "    r.code,  \n"
        + "    case when r.code <> ''COMBO'' then coalesce(g.factor, 1.0) else null::double precision end as factor,  \n"
        + "    null::double precision as tog,  \n"
        + "    case  \n"
        + "        when i.poll = ''71432'' then ''hap_benzene_data_source'' \n"
        + "       when i.poll = ''50000'' then ''hap_formaldehyde_data_source'' \n"
        + "        when i.poll = ''75070'' then ''hap_acetaldehyde_data_source'' \n"
        + "        when i.poll = ''67561'' then ''hap_methanol_data_source'' \n"
        + "        else '''' \n"
        + "    end as attr, \n"
        + "    i.data_source as value \n"
        + " \n"
        + "from " + hapInventoryTable + " i \n"
        + "    inner join emf.sources s \n"
        + "    on s.source = i.scc || i.fips || "
        + (
            hapIsPoint
            ? "rpad(coalesce(i.plantid, ''''), 15) || rpad(coalesce(i.pointid, ''''), 15) || rpad(coalesce(i.stackid, ''''), 15) || rpad(coalesce(i.segment, ''''), 15) \n"
            : "repeat('' '', 60)  \n"
            )
        + " \n"
        + "    -- get profilecode by SCC and pollutant \n"
        + "   left outer join ( \n";
        for (int i = 0; i < speciationCrossReferenceDatasets.length; i++) {
            if (i > 0) hapDatasourceSql += " union \n";
            dataset = getDataset(speciationCrossReferenceDatasets[i]);
            table = qualifiedEmissionTableName(dataset);
            version = new VersionedQuery(version(dataset.getId(), dataset.getDefaultVersion())).query();
            hapDatasourceSql += "       select scc, code \n"
                + "        from " + table + " s \n"
                + "        where pollutant = ''VOC'' \n"
                + "        and " + version.replaceAll("'", "''");
        }
        hapDatasourceSql += "        ) r \n"
        + "        on i.scc = r.scc \n"
        + " \n"
        + "    -- get factor by profilecode and pollutant \n"
        + "    left outer join " + pollToPollConversionDatasetTable + " g \n"
        + "    on g.speciation_code = r.code \n"
        + "    and g.pollutant_1 = ''VOC'' \n"
        + "    and " + pollToPollConversionDatasetVersion.replaceAll("'", "''") + " \n"
        + " \n"
        + " where i.poll in (''71432'',''50000'',''75070'',''67561'') \n"
        + " and (" + hapInventoryVersion.replaceAll("'", "''") + ") \n"
        + (filter.length() > 0 ? "and (" + filter + ") \n" : "");
        
        sql += hapDatasourceSql;

        sql += " \n"
        + " order by id, attr \n"
        + "', \n"
        + "' \n"
        + "select ''cap_acetaldehyde'' \n"
        + "union select ''cap_benzene'' \n"
        + "union select ''cap_data_source'' \n"
        + "union select ''cap_formaldehyde'' \n"
        + "union select ''cap_methanol'' \n"
        + "union select ''hap_acetaldehyde'' \n"
        + "union select ''hap_acetaldehyde_data_source'' \n"
        + "union select ''hap_benzene'' \n"
        + "union select ''hap_benzene_data_source'' \n"
        + "union select ''hap_formaldehyde'' \n"
        + "union select ''hap_formaldehyde_data_source'' \n"
        + "union select ''hap_methanol'' \n"
        + "union select ''hap_methanol_data_source'' \n"
        + "order by 1 \n"
        + " ' \n"
        + ") as c( \n"
        + " id int, \n"
        + (
            hapIsPoint || capIsPoint
            ? " nei_unique_id character varying(20), \n"
            + "plantid character varying(15), \n"
            + "pointid character varying(15), \n"
            + "stackid character varying(15), \n"
            + "segment character varying(15), \n"
            + "plant character varying(40), \n"
            : " nei_unique_id character varying(20), \n"
            )
        + "fips character varying(6), \n"
        + "scc character varying(10), \n"
        + "poll character varying(16), \n"
        + "sic character varying(4), \n"
        + "mact character varying(6), \n"
        + "naics character varying(6), \n"
        + "ann_emis double precision, \n"
        + "speciated_code character varying(5), \n"
        + "factor double precision, \n"
        + "tog double precision, \n"
        + "cap_acetaldehyde double precision, \n"
        + "cap_benzene double precision, \n"
        + "cap_data_source character varying(10), \n"
        + "cap_formaldehyde double precision, \n"
        + "cap_methanol double precision, \n"
        + "hap_acetaldehyde double precision, \n"
        + "hap_acetaldehyde_data_source character varying(10), \n"
        + "hap_benzene double precision, \n"
        + "hap_benzene_data_source character varying(10), \n"
        + "hap_formaldehyde double precision, \n"
        + "hap_formaldehyde_data_source character varying(10), \n"
        + "hap_methanol double precision, \n"
        + "hap_methanol_data_source character varying(10) \n"
        + ")  \n";
//        + "left outer join " + speciationToolSpecieInfoDatasetTable + " p \n"
//        + "on p.profile_id = c.speciated_code \n"
//        + "and " + speciationToolSpecieInfoDatasetVersion + " \n"
//        + " \n"
//        + " left outer join reference.scc s \n"
//        + "on s.scc = c.scc \n"
//        + " \n"
//        + "order by c.fips, c.nei_unique_id, c.plantid, c.pointid, c.stackid, c.segment, c.scc";
        
        if (summaryType.equals("By NAICS")) {
            sql += ") tbl \n"
            + "left outer join reference.naics_codes n \n"
            + "on n.naics_code = tbl.naics \n"
            + "group by upper(tbl.naics), coalesce(n.naics_description, 'UNSPECIFIED') \n"
            + "order by upper(tbl.naics) \n";
        } else if (summaryType.equals("By MACT")) {
            sql += ") tbl \n"
            + "left outer join reference.mact_codes m \n"
            + "on lower(m.mact_code) = lower(tbl.mact) \n"
            + "group by upper(tbl.mact), coalesce(m.mact_source_category, 'UNSPECIFIED') \n"
            + "order by upper(tbl.mact) \n";
        } else if (summaryType.equals("By SIC")) {
            sql += ") tbl \n"
            + "left outer join reference.sic_codes s \n"
            + "on s.sic = tbl.sic \n"
            + "group by upper(tbl.sic), coalesce(s.description, 'UNSPECIFIED') \n"
            + "order by upper(tbl.sic) \n";
        } else if (summaryType.equals("By Profile Code")) {
            sql += "left outer join " + speciationToolSpecieInfoDatasetTable + " p \n"
            + "on p.profile_id = c.speciated_code \n"
            + "and " + speciationToolSpecieInfoDatasetVersion + " \n"
            + " \n"
            + ") tbl \n"
            + "group by profile_code, profile_name, profile_date, documentation \n"
            + "order by profile_code \n";
        } else if (summaryType.equals("By SCC")) {
            sql += ") tbl \n"
                + "left outer join " + speciationToolSpecieInfoDatasetTable + " p \n"
            + "on p.profile_id = tbl.profile_code \n"
            + "and " + speciationToolSpecieInfoDatasetVersion + " \n"
            + "left outer join reference.scc s \n"
            + "on s.scc = tbl.scc \n"
            + "group by tbl.scc, coalesce(s.scc_description, 'UNSPECIFIED'), tbl.profile_code, coalesce(p.profile_name,'NO PROFILE'), p.date_added, p.documentation \n"
            + "order by tbl.scc \n";
        } else if (summaryType.equals("Details")) {
            sql += "left outer join " + speciationToolSpecieInfoDatasetTable + " p \n"
            + "on p.profile_id = c.speciated_code \n"
            + "and " + speciationToolSpecieInfoDatasetVersion + " \n"
            + " \n"
            + " left outer join reference.scc s \n"
            + "on s.scc = c.scc \n"
            + " \n"
            + "order by c.fips, c.nei_unique_id, "
            + (
                capIsPoint || hapIsPoint
                ? "c.plantid, c.pointid, c.stackid, c.segment, "
                : ""
                )
            + "c.scc";
        } else if (summaryType.equals("By NEI Unique Id")) {
            filter = Pattern.compile("''", Pattern.CASE_INSENSITIVE).matcher(filter).replaceAll("'");

            sql += ") tbl \n"

            + "group by tbl.nei_unique_id \n"
            + ") tbl \n"

            + "inner join ( \n"
            + "-- get nei unique ids most occuring sic, mact and naics codes \n"
            + "select nei.nei_unique_id,  \n"
            + "    maxsic.sic, \n"
            + "    coalesce(maxmact.mact,'NONE'::character varying(6)) as mact, \n"
            + "    maxnaics.naics, \n"
            + "    maxplant.plant, \n"
            + "    maxfips.fips \n"
            + "from ( \n"
            + (
                capIsPoint
                ? "        select distinct  nei_unique_id \n"
                + "        from " + capInventoryTable + " i  \n"
                + "        where i.poll in ('VOC') \n"
                + "        and (" + capInventoryVersion + ") \n"
                + (filter.length() > 0 ? "and (" + filter + ") \n" : "")
                : "        select null::character varying(20) as nei_unique_id \n"
                )
            + "        union  \n"
            + (
                hapIsPoint
                ? "        select distinct  nei_unique_id \n"
                + "        from " + hapInventoryTable + " i \n"
                + "        where i.poll in ('71432','50000','75070','67561') \n"
                + "        and (" + hapInventoryVersion + ") \n"
                + (filter.length() > 0 ? "and (" + filter + ") \n" : "")
                : "        select null::character varying(20) as nei_unique_id \n"
                )
            + "    ) nei \n"
            + " \n"
            + "    left outer join ( \n"
            + " \n"
            + "        select distinct on (nei_unique_id)  \n"
            + "            nei_unique_id,  \n"
            + "            sic,  \n"
            + "            sum(cnt) \n"
            + "        from ( \n"
            + " \n"
            + "            select  \n"
            + (
                capIsPoint
                ? "                nei_unique_id,  \n"
                : "                null::character varying(20) as nei_unique_id,  \n"
                )
            + (
                capHasSIC
                ? "                sic,  \n"
                : "                null::character varying(4) as sic,  \n"
                )
            + "                count(1) as cnt  \n"
            + "            from " + capInventoryTable + " i  \n"
            + "            where i.poll in ('VOC') \n"
            + "            and (" + capInventoryVersion + ") \n"
            + (filter.length() > 0 ? "and (" + filter + ") \n" : "")
            + "            group by "
            + (
                capIsPoint
                ? "                nei_unique_id,  \n"
                : "                null::character varying(20), \n"
                )
            + (
                capHasSIC
                ? "                sic \n"
                : "                null::character varying(4) \n"
                )
            + " \n"
            + "            union all \n"
            + "            select  \n"
            + (
                hapIsPoint
                ? "                nei_unique_id,  \n"
                : "                null::character varying(20) as nei_unique_id,  \n"
                )
            + (
                hapHasSIC
                ? "                sic,  \n"
                : "                null::character varying(4) as sic,  \n"
                )
            + "                count(1) as cnt \n"
            + "            from " + hapInventoryTable + " i \n"
            + "            where i.poll in ('71432','50000','75070','67561') \n"
            + "            and (" + hapInventoryVersion + ") \n"
            + (filter.length() > 0 ? "and (" + filter + ") \n" : "")
            + "            group by "
            + (
                hapIsPoint
                ? "                nei_unique_id,  \n"
                : "                null::character varying(20), \n"
                )
            + (
                hapHasSIC
                ? "                sic \n"
                : "                null::character varying(4) \n"
                )
            + "        ) tbl \n"
            + "        group by nei_unique_id, sic \n"
            + "        order by nei_unique_id, sum(cnt) desc \n"
            + " \n"
            + "    ) maxsic \n"
            + "    on coalesce(maxsic.nei_unique_id,'') = coalesce(nei.nei_unique_id,'') \n"
            + " \n"
            + "    left outer join ( \n"
            + "        select distinct on (nei_unique_id)  \n"
            + "            nei_unique_id,  \n"
            + "            mact,  \n"
            + "            sum(cnt) \n"
            + "        from ( \n"
            + " \n"
            + "            select  \n"
            + (
                capIsPoint
                ? "                nei_unique_id,  \n"
                : "                null::character varying(20) as nei_unique_id,  \n"
                )
            + (
                capHasMACT
                ? "                mact,  \n"
                : "                null::character varying(4) as mact,  \n"
                )
            + "                count(1) as cnt  \n"
            + "            from " + capInventoryTable + " i  \n"
            + "            where i.poll in ('VOC') \n"
            + "            and (" + capInventoryVersion + ") \n"
            + (filter.length() > 0 ? "and (" + filter + ") \n" : "")
            + (
                capHasMACT
                ? "            and upper(mact) <> 'NONE' \n"
                : ""
                )
            + "            group by "
            + (
                capIsPoint
                ? "                nei_unique_id,  \n"
                : "                null::character varying(20), \n"
                )
            + (
                capHasMACT
                ? "                mact \n"
                : "                null::character varying(6) \n"
                )
            + " \n"
            + "            union all \n"
            + "            select  \n"
            + (
                hapIsPoint
                ? "                nei_unique_id,  \n"
                : "                null::character varying(20) as nei_unique_id,  \n"
                )
            + (
                hapHasMACT
                ? "                mact,  \n"
                : "                null::character varying(4) as mact,  \n"
                )
            + "                count(1) as cnt \n"
            + "            from " + hapInventoryTable + " i \n"
            + "            where i.poll in ('71432','50000','75070','67561') \n"
            + "            and (" + hapInventoryVersion + ") \n"
            + (filter.length() > 0 ? "and (" + filter + ") \n" : "")
            + (
                hapHasMACT
                ? "            and upper(mact) <> 'NONE' \n"
                : ""
                )
            + "            group by "
            + (
                hapIsPoint
                ? "                nei_unique_id,  \n"
                : "                null::character varying(20), \n"
                )
            + (
                hapHasMACT
                ? "                mact \n"
                : "                null::character varying(6) \n"
                )
            + "        ) tbl \n"
            + "        group by nei_unique_id, mact \n"
            + "        order by nei_unique_id, sum(cnt) desc \n"
            + "    ) maxmact \n"
            + "    on coalesce(maxmact.nei_unique_id,'') = coalesce(nei.nei_unique_id,'') \n"
            + " \n"
            + "    left outer join ( \n"
            + "       select distinct on (nei_unique_id)  \n"
            + "            nei_unique_id,  \n"
            + "            naics,  \n"
            + "           sum(cnt) \n"
            + "        from ( \n"

            + "            select  \n"
            + (
                capIsPoint
                ? "                nei_unique_id,  \n"
                : "                null::character varying(20) as nei_unique_id,  \n"
                )
            + (
                capHasNAICS
                ? "                naics,  \n"
                : "                null::character varying(6) as naics,  \n"
                )
            + "                count(1) as cnt  \n"
            + "            from " + capInventoryTable + " i  \n"
            + "            where i.poll in ('VOC') \n"
            + "            and (" + capInventoryVersion + ") \n"
            + (filter.length() > 0 ? "and (" + filter + ") \n" : "")
            + "            group by "
            + (
                capIsPoint
                ? "                nei_unique_id,  \n"
                : "                null::character varying(20), \n"
                )
            + (
                capHasNAICS
                ? "                naics \n"
                : "                null::character varying(6) \n"
                )
            + " \n"
            + "            union all \n"
            + "            select  \n"
            + (
                hapIsPoint
                ? "                nei_unique_id,  \n"
                : "                null::character varying(20) as nei_unique_id,  \n"
                )
            + (
                hapHasNAICS
                ? "                naics,  \n"
                : "                null::character varying(6) as naics,  \n"
                )
            + "                count(1) as cnt \n"
            + "            from " + hapInventoryTable + " i \n"
            + "            where i.poll in ('71432','50000','75070','67561') \n"
            + "            and (" + hapInventoryVersion + ") \n"
            + (filter.length() > 0 ? "and (" + filter + ") \n" : "")
            + "            group by "
            + (
                hapIsPoint
                ? "                nei_unique_id,  \n"
                : "                null::character varying(20),  \n"
                )
            + (
                hapHasNAICS
                ? "                naics \n"
                : "                null::character varying(6) \n"
                )
            + "        ) tbl \n"
            + "        group by nei_unique_id, naics \n"
            + "       order by nei_unique_id, sum(cnt) desc \n"
            + "    ) maxnaics \n"
            + "    on coalesce(maxnaics.nei_unique_id,'') = coalesce(nei.nei_unique_id,'') \n"
            + " \n"
            + "    left outer join ( \n"
            + "       select distinct on (nei_unique_id)  \n"
            + "           nei_unique_id,  \n"
            + "            plant,  \n"
            + "            sum(cnt) \n"
            + "        from ( \n"
            + " \n"
            + "           select  \n"
            + (
                capIsPoint
                ? "               nei_unique_id,  \n"
                + "               plant,  \n"
                : "                null::character varying(20) as nei_unique_id,  \n"
                + "                null::character varying(40) as plant,  \n"
                )
            + "               count(1) as cnt  \n"
            + "            from " + capInventoryTable + " i  \n"
            + "            where i.poll in ('VOC') \n"
            + "            and (" + capInventoryVersion + ") \n"
            + (filter.length() > 0 ? "and (" + filter + ") \n" : "")
            + "            group by "
            + (
                capIsPoint
                ? "               nei_unique_id, \n"
                + "               plant \n"
                : "                null::character varying(20),  \n"
                + "                null::character varying(40) \n"
                )
            + " \n"
            + "           union all \n"
            + "           select  \n"
            + (
                hapIsPoint
                ? "               nei_unique_id,  \n"
                + "               plant,  \n"
                : "                null::character varying(20) as nei_unique_id,  \n"
                + "                null::character varying(40) as plant,  \n"
                )
            + "                count(1) as cnt \n"
            + "            from " + hapInventoryTable + " i \n"
            + "            where i.poll in ('71432','50000','75070','67561') \n"
            + "            and (" + hapInventoryVersion + ") \n"
            + (filter.length() > 0 ? "and (" + filter + ") \n" : "")
            + "            group by "
            + (
                hapIsPoint
                ? "               nei_unique_id, \n"
                + "               plant \n"
                : "                null::character varying(20),  \n"
                + "                null::character varying(40) \n"
                )
            + "        ) tbl \n"
            + "       group by nei_unique_id, plant \n"
            + "       order by nei_unique_id, sum(cnt) desc \n"
            + "   ) maxplant \n"
            + "    on coalesce(maxplant.nei_unique_id,'') = coalesce(nei.nei_unique_id,'') \n"
            + " \n"
            + "    left outer join ( \n"
            + "        select distinct on (nei_unique_id)  \n"
            + "            nei_unique_id,  \n"
            + "           fips,  \n"
            + "            sum(cnt) \n"
            + "        from ( \n"
            + " \n"
            + "            select  \n"
            + (
                capIsPoint
                ? "               nei_unique_id, \n"
                : "                null::character varying(20) as nei_unique_id, \n"
                )
            + "                fips,  \n"
            + "                count(1) as cnt  \n"
            + "            from " + capInventoryTable + " i  \n"
            + "            where i.poll in ('VOC') \n"
            + "            and (" + capInventoryVersion + ") \n"
            + (filter.length() > 0 ? "and (" + filter + ") \n" : "")
            + "            group by "
            + (
                capIsPoint
                ? "               nei_unique_id, \n"
                : "               null::character varying(20),  \n"
                )
            + "               fips \n"

            + "            union all \n"
            + "           select  \n"
            + (
                hapIsPoint
                ? "               nei_unique_id, \n"
                : "                null::character varying(20) as nei_unique_id, \n"
                )
            + "                fips,  \n"
            + "               count(1) as cnt \n"
            + "            from " + hapInventoryTable + " i \n"
            + "            where i.poll in ('71432','50000','75070','67561') \n"
            + "            and (" + hapInventoryVersion + ") \n"
            + (filter.length() > 0 ? "and (" + filter + ") \n" : "")
            + "            group by "
            + (
                hapIsPoint
                ? "               nei_unique_id, \n"
                : "               null::character varying(20),  \n"
                )
            + "               fips \n"
            + "       ) tbl \n"
            + "        group by nei_unique_id, fips \n"
            + "        order by nei_unique_id, sum(cnt) desc \n"
            + "    ) maxfips \n"
            + "    on coalesce(maxfips.nei_unique_id,'') = coalesce(nei.nei_unique_id,'') \n"
            + ") nei \n"
            + "on coalesce(nei.nei_unique_id,'') = coalesce(tbl.nei_unique_id,'') \n"

            + "left outer join reference.naics_codes n \n"
            + "on n.naics_code = nei.naics \n"

            + "left outer join reference.mact_codes m \n"
            + "on lower(m.mact_code) = lower(nei.mact) \n"

            + "left outer join reference.sic_codes s2 \n"
            + "on s2.sic = nei.sic \n"

            + "order by fips, nei_unique_id \n";
        } 
//        
//        sql = query(sql, true);
        sql = "CREATE TABLE " + emissionDatasourceName + "." + tableName + " AS " + sql;
//        System.out.println(sql);
        
        return sql;
    }

    protected String query(String partialQuery, boolean createClause) throws EmfException {

        SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissionDatasourceName, tableName );
        return parser.parse(partialQuery, createClause);
    }

    private void populateSourcesTable(String qualifiedTable, String filter) throws EmfException { 
        String sql = "select public.populate_sources_table('" + qualifiedTable + "'," + (filter.length() == 0 ? "null::text" : "'" + filter.replaceAll("'", "''") + "'") + ");";
//        System.out.println(System.currentTimeMillis() + " " + sql);
        try {
            dbServer.getEmissionsDatasource().query().execute(sql);
//            System.out.println(System.currentTimeMillis() + " analyze emf.sources;");
            dbServer.getEmissionsDatasource().query().execute("analyze emf.sources;");
        } catch (SQLException e) {
            throw new EmfException("Error occured when populating the sources table " + "\n" + e.getMessage());
        }
    }

    private void indexORLInventory(String table) {
        String query = "SELECT public.create_orl_table_indexes('" + table.toLowerCase() + "');";
//public.create_table_index(table_name character varying, table_col_list character varying, index_name_prefix character varying)
//        System.out.println(query);
        try {
            try {
                query = "SELECT public.create_table_index('" + table.toLowerCase() + "','record_id','recordid');";
//                System.out.println(System.currentTimeMillis() + " " + query);
                dbServer.getEmissionsDatasource().query().execute(query);
            } catch (SQLException e) {
                //e.printStackTrace();
            }
            try {
                query = "SELECT public.create_table_index('" + table.toLowerCase() + "','fips','fips');";
//                System.out.println(System.currentTimeMillis() + " " + query);
                dbServer.getEmissionsDatasource().query().execute(query);
            } catch (SQLException e) {
                //e.printStackTrace();
            }
            try {
                query = "SELECT public.create_table_index('" + table.toLowerCase() + "','poll','poll');";
//                System.out.println(System.currentTimeMillis() + " " + query);
                dbServer.getEmissionsDatasource().query().execute(query);
            } catch (SQLException e) {
                //e.printStackTrace();
            }
            try {
                query = "SELECT public.create_table_index('" + table.toLowerCase() + "','scc','scc');";
//                System.out.println(System.currentTimeMillis() + " " + query);
                dbServer.getEmissionsDatasource().query().execute(query);
            } catch (SQLException e) {
                //e.printStackTrace();
            }
            try {
                query = "SELECT public.create_table_index('" + table.toLowerCase() + "','plantid','plantid');";
//                System.out.println(System.currentTimeMillis() + " " + query);
                dbServer.getEmissionsDatasource().query().execute(query);
            } catch (SQLException e) {
                //e.printStackTrace();
            }
            try {
                query = "SELECT public.create_table_index('" + table.toLowerCase() + "','pointid','pointid');";
//                System.out.println(System.currentTimeMillis() + " " + query);
                dbServer.getEmissionsDatasource().query().execute(query);
            } catch (SQLException e) {
                //e.printStackTrace();
            }
            try {
                query = "SELECT public.create_table_index('" + table.toLowerCase() + "','stackid','stackid');";
//                System.out.println(System.currentTimeMillis() + " " + query);
                dbServer.getEmissionsDatasource().query().execute(query);
            } catch (SQLException e) {
                //e.printStackTrace();
            }
            try {
                query = "SELECT public.create_table_index('" + table.toLowerCase() + "','segment','segment');";
//                System.out.println(System.currentTimeMillis() + " " + query);
                dbServer.getEmissionsDatasource().query().execute(query);
            } catch (SQLException e) {
                //e.printStackTrace();
            }
            try {
                query = "SELECT public.create_table_index('" + table.toLowerCase() + "','mact','mact');";
//                System.out.println(System.currentTimeMillis() + " " + query);
                dbServer.getEmissionsDatasource().query().execute(query);
            } catch (SQLException e) {
                //e.printStackTrace();
            }
            try {
                query = "SELECT public.create_table_index('" + table.toLowerCase() + "','sic','sic');";
//                System.out.println(System.currentTimeMillis() + " " + query);
                dbServer.getEmissionsDatasource().query().execute(query);
            } catch (SQLException e) {
                //e.printStackTrace();
            }

//record_id
//fips
//poll
//scc
//plantid
//pointid
//stackid
//segment
//mact
//sic
            
//            dbServer.getEmissionsDatasource().query().execute(query);
//            System.out.println(System.currentTimeMillis() + " " + "analyze ");
            dbServer.getEmissionsDatasource().query().execute("analyze " + emissionDatasourceName + "." + table.toLowerCase() + ";");
        } catch (SQLException e) {
            e.printStackTrace();
            //supress all errors, the indexes might already be on the table...
        } finally {
            //
        }
    }

    private boolean checkTableForColumns(String table, String colList) throws EmfException {
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
}
