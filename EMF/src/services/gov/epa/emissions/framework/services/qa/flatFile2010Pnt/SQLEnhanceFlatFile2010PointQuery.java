package gov.epa.emissions.framework.services.qa.flatFile2010Pnt;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetVersion;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.SQLQAProgramQuery;
import gov.epa.emissions.framework.services.qa.SQLQueryParser;
import gov.epa.emissions.framework.tasks.DebugLevels;

public class SQLEnhanceFlatFile2010PointQuery extends SQLQAProgramQuery {
    
    public static final String FF10P_TAG = "-ff10p"; 
    
    public static final String SSFF_TAG = "-ssff"; 
    
    public static final String MANYNEIID_TAG = "-manyneiid"; 
    
    public static final String MANYFRS_TAG = "-manyfrs";    
    
    private Datasource datasource;
    
    private StatusDAO statusDao;
    
    public SQLEnhanceFlatFile2010PointQuery(HibernateSessionFactory sessionFactory, String emissioDatasourceName,
            String tableName, QAStep qaStep, Datasource datasource) {
        super(sessionFactory, emissioDatasourceName, tableName, qaStep);
        this.datasource = datasource;
        this.statusDao = new StatusDAO(sessionFactory);
    }
    
    public String createProgramQuery() throws EmfException, SQLException {

        String programArguments = qaStep.getProgramArguments();

        setStatus("Parsing program arguments...");
        
        String[] ff10pTokens = new String[] {};
        String[] ssffTokens = new String[] {};
        String[] facTokens = new String[] {};
        
        
        String[] arguments; 
        
        List<DatasetVersion> ff10pDatasetList = new ArrayList<DatasetVersion>();
        List<DatasetVersion> ssffDatasetList = new ArrayList<DatasetVersion>();
        List<DatasetVersion> facDatasetList = new ArrayList<DatasetVersion>();
        boolean boolMultiNEI = false;
        boolean boolMultiFRS = false;
        String whereFilter = new String();
        
        ///////////////////////////////////////////////////
        // parse the programArguments
        
        int indexFF10P = programArguments.indexOf(QAStep.FF10P_TAG);
        int indexSSFF = programArguments.indexOf(QAStep.SSFF_TAG);
        int indexFAC = programArguments.indexOf(QAStep.FAC_TAG);
        int indexMultiNEI = programArguments.indexOf(QAStep.MANYNEIID_TAG);
        int indexMultiFRS = programArguments.indexOf(QAStep.MANYFRS_TAG);
        int indexWhereFilter = programArguments.indexOf(QAStep.WHERE_FILTER_TAG);
        
        if (indexFF10P != -1) {
            arguments = parseSwitchArguments(programArguments, indexFF10P, programArguments.indexOf("\n-", indexFF10P) != -1 ? programArguments.indexOf("\n-", indexFF10P) : programArguments.length());
            if (arguments != null && arguments.length > 0) ff10pTokens = arguments;
            for (String datasetVersion : ff10pTokens) {
                String[] datasetVersionToken = new String[] {};
                EmfDataset qaStepDataset = null;
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                    qaStepDataset = getDataset( datasetVersionToken[0]);
                } else {
                    qaStepDataset = getDataset(qaStep.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }
                datasetNames.add(datasetVersionToken[0]);
                DatasetVersion dv = new DatasetVersion(datasetVersionToken[0], Integer.parseInt(datasetVersionToken[1]));
                dv.setDataset( qaStepDataset);
                Version v = version( qaStepDataset.getId(), Integer.parseInt(datasetVersionToken[1]));
                dv.setVersion(v);
                ff10pDatasetList.add(dv);
            }
        }

        if (indexSSFF != -1) {
            arguments = parseSwitchArguments(programArguments, indexSSFF, programArguments.indexOf("\n-", indexSSFF) != -1 ? programArguments.indexOf("\n-", indexSSFF) : programArguments.length());
            if (arguments != null && arguments.length > 0) ssffTokens = arguments;
            for (String datasetVersion : ssffTokens) {
                String[] datasetVersionToken = new String[] {};
                EmfDataset qaStepDataset = null;
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                    qaStepDataset = getDataset( datasetVersionToken[0]);
                } else {
                    qaStepDataset = getDataset(qaStep.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }
                datasetNames.add(datasetVersionToken[0]);
                DatasetVersion dv = new DatasetVersion(datasetVersionToken[0], Integer.parseInt(datasetVersionToken[1]));
                dv.setDataset( qaStepDataset);
                Version v = version( qaStepDataset.getId(), Integer.parseInt(datasetVersionToken[1]));
                dv.setVersion(v);
                ssffDatasetList.add(dv);
            }
        }
        
        if (indexFAC != -1) {
            arguments = parseSwitchArguments(programArguments, indexFAC, programArguments.indexOf("\n-", indexFAC) != -1 ? programArguments.indexOf("\n-", indexFAC) : programArguments.length());
            if (arguments != null && arguments.length > 0) facTokens = arguments;
            for (String datasetVersion : facTokens) {
                String[] datasetVersionToken = new String[] {};
                EmfDataset qaStepDataset = null;
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                    qaStepDataset = getDataset( datasetVersionToken[0]);
                } else {
                    qaStepDataset = getDataset(qaStep.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }
                datasetNames.add(datasetVersionToken[0]);
                DatasetVersion dv = new DatasetVersion(datasetVersionToken[0], Integer.parseInt(datasetVersionToken[1]));
                dv.setDataset( qaStepDataset);
                Version v = version( qaStepDataset.getId(), Integer.parseInt(datasetVersionToken[1]));
                dv.setVersion(v);
                facDatasetList.add(dv);
            }
        }
        String multiNEI = (indexMultiNEI != -1 ? programArguments.substring(indexMultiNEI + MANYNEIID_TAG.length() + 1, programArguments.indexOf("\n-", indexMultiNEI) != -1 ? programArguments.indexOf("\n-", indexMultiNEI) : programArguments.length()) : "");
        if ("TRUE".equals( multiNEI.trim().toUpperCase())) {
            boolMultiNEI = true;
        }
        String multiFRS = (indexMultiFRS != -1 ? programArguments.substring(indexMultiFRS + MANYFRS_TAG.length() + 1, programArguments.indexOf("\n-", indexMultiFRS) != -1 ? programArguments.indexOf("\n-", indexMultiFRS) : programArguments.length()) : "");
        if ("TRUE".equals( multiFRS.trim().toUpperCase())) {
            boolMultiFRS = true;
        }
        whereFilter = indexWhereFilter != -1 && (indexWhereFilter + QAStep.WHERE_FILTER_TAG.length() + 1) < (programArguments.indexOf("\n-", indexWhereFilter) != -1 ? programArguments.indexOf("\n-", indexWhereFilter) : programArguments.length()) ? programArguments.substring(indexWhereFilter + QAStep.WHERE_FILTER_TAG.length() + 1, programArguments.indexOf("\n-", indexWhereFilter) != -1 ? programArguments.indexOf("\n-", indexWhereFilter) : programArguments.length()) : "";
        
        ///////////////////////////////////////////////////
        // validation
        
        setStatus("Validating program arguments...");
        
        if ( ff10pDatasetList.size() != 1) {
            throw new EmfException("One and only one Flat File 2010 Point dataset should be chosen.");
        }
        
        if ( ssffDatasetList.size() != 1) {
            throw new EmfException("One and only one Smoke Supporting Flat File dataset should be chosen.");
        }
        
        if ( facDatasetList.size() != 1) {
            throw new EmfException("One and only one Supporting Flat File dataset for NEI and FRS should be chosen.");
        }        

        String ff10pTable = "";
        String ssffTable = "";
        String facTable = "";
        String fstcTable = "reference.facility_source_type_codes";
        String fipsTable = "reference.fips";
        String pcTable = "reference.pollutant_codes";
        String cdTable = "reference.control_device";
        String rcTable = "reference.regulatory_codes";

        String ff10pAlias = "ff10p";
        String ssffAlias = "ssff";
        String fstcAlias = "fstc";
        String fipsAlias = "fipst";
        String pcAlias = "pollutant_codes";
        String cdAlias = "cd";
        String frcdAlias = "ff10_reg_code_descs";
        String facAlias = "fac_ff10p";
        String facAlias2 = "fac_ff10p2";
        String fciAlias = "ff10_control_ids";
        String fcidAlias = "ff10_control_id_descs";
        String frcAlias = "ff10_reg_codes";
        String rcAlias = "rc";
        
        String ff10pVersionQuery = "";
        String ssffVersionQuery = "";
        String facVersionQuery = "";
       
        String selectClause = "select distinct on (" + ff10pAlias + ".record_id) ";
        String fromClause = "from ";
        String whereClause = "where ";
        String sqlStr = "";
        
        String ff10pCols = "";
        String otherCols = "";
        
        DatasetVersion dvFF10P = ff10pDatasetList.get(0);
        if ( dvFF10P == null) {
            throw new EmfException("This Flat File 2010 Point dataset DatasetVersion obj is null.");
        }
        if ( dvFF10P.getDataset() == null) {
            throw new EmfException("This Flat File 2010 Point dataset is null.");
        }
        if ( dvFF10P.getDataset().getInternalSources() == null) {
            throw new EmfException("This Flat File 2010 Point dataset internal source obj is null.");
        }
        InternalSource[] dvFF10PiSources = dvFF10P.getDataset().getInternalSources();
        if ( dvFF10PiSources.length != 1) {
            throw new EmfException("This Flat File 2010 Point dataset contains more than one tables.");
        }
        ff10pTable  = "emissions." + dvFF10PiSources[0].getTable(); // the dataset only has one table
        VersionedQuery ff10pDatasetVersionedQuery = new VersionedQuery(dvFF10P.getVersion(), ff10pAlias);
        ff10pVersionQuery = ff10pDatasetVersionedQuery.query();
        
//      Column[] cols = dvFF10P.getDataset().getDatasetType().getFileFormat().cols();
//      for ( Column col : cols) {
//          ff10pCols += ff10pAlias + "." + col.name() + " ";
//      }        
        try {
            ff10pCols = getFF10Columns( ff10pTable, ff10pAlias);
        } catch ( EmfException e) {
            throw e;
        }
        
        DatasetVersion dvSSFF = ssffDatasetList.get(0);
        InternalSource[] dvSSFFiSources = dvSSFF.getDataset().getInternalSources();
        if ( dvSSFFiSources.length != 1) {
            throw new EmfException("This Smoke Supporting Flat File dataset contains more than one tables.");
        }
        ssffTable = "emissions." + dvSSFFiSources[0].getTable();
        VersionedQuery ssffDatasetVersionedQuery = new VersionedQuery(dvSSFF.getVersion(), ssffAlias);
        ssffVersionQuery = ssffDatasetVersionedQuery.query();
        
        DatasetVersion dvFAC = facDatasetList.get(0);
        InternalSource[] dvFACiSources = dvFAC.getDataset().getInternalSources();
        if ( dvFACiSources.length != 1) {
            throw new EmfException("This Supporting Flat File dataset for NEI and FRS contains more than one tables.");
        }
        facTable = "emissions." + dvFACiSources[0].getTable();
        VersionedQuery facDatasetVersionedQuery = new VersionedQuery(dvFAC.getVersion(), facAlias);
        facVersionQuery = facDatasetVersionedQuery.query();
        
        // validate the datasets and versions
        this.validateDatasetVersion(ff10pTable, ff10pAlias, ff10pVersionQuery);
        this.validateDatasetVersion(ssffTable, ssffAlias, ssffVersionQuery);
        this.validateDatasetVersion(facTable, facAlias, facVersionQuery);
        // validate the where filter
        Map<String,Column> tableColumns = getDatasetColumnMap(datasource, dvFF10P.getDataset());
        whereFilter = this.aliasExpression(whereFilter, tableColumns, ff10pAlias);
        this.validateWhereFilter(ff10pTable, ff10pAlias, whereFilter);
        
        ///////////////////////////////////////////////////
        // update ff10pVersionQuery to include the where filter
        
        if ( whereFilter != null && !whereFilter.trim().equals("")) {
            ff10pVersionQuery += " and (" + whereFilter.trim() + ") ";
        }
        
        ///////////////////////////////////////////////////
        // indexing
        this.createFF10PIndexes(dvFF10P.getDataset());
        this.createFF10PIndexes(dvSSFF.getDataset());
        this.createFF10PIndexes(dvFAC.getDataset());
        
        ///////////////////////////////////////////////////
        // constructing query
        
        setStatus("Constructing query...");
        
        // --- select clause
        
        //        sff10.facility_company_name,
        //        facility_source_type_codes.description as facility_type_description,
        //        sff10.facilty_status_cd,
        //        sff10.facilty_address,
        //        fips.state_name,
        //        sff10.unit_desc,
        //        sff10.unit_status_cd,
        //        sff10.process_desc
        //        ,ff10_control_id_descs.control_descs
        //        ,ff10_reg_code_descs.reg_codes_descs
        //        ,pollutant_codes.pollutant_code_desc as poll_desc
        //        ,fac_ff10.nei_unique_ids
        //        ,fac_ff10.frs_ids
        otherCols += ssffAlias + ".facility_company_name, ";
        otherCols += fstcAlias + ".description as facility_type_description, ";
        otherCols += ssffAlias + ".facilty_status_cd, ";
        otherCols += ssffAlias + ".facilty_address, ";
        otherCols += fipsAlias + ".state_name, ";
        otherCols += ssffAlias + ".unit_desc, ";
        otherCols += ssffAlias + ".unit_status_cd, ";
        otherCols += ssffAlias + ".process_desc, "; //        
        otherCols += fcidAlias + ".control_descs, ";
        otherCols += frcdAlias + ".reg_codes_descs, ";
        otherCols += pcAlias + ".pollutant_code_desc as poll_desc, ";
        otherCols += facAlias2 + ".nei_unique_ids, ";
        otherCols += facAlias2 + ".frs_ids ";
        
        selectClause += ff10pCols;
        selectClause += ", " + otherCols;
        
        // --- from clause
        
        fromClause += ff10pTable + " as " + ff10pAlias + " ";
        
        // join 1
        // -- get supporting information -- facility_company_name, facilty_status_cd, facilty_address, unit_desc, unit_status_cd, process_desc
        //        left outer join emissions.ds_eflatfile_supporting_20110518_fixed3_02jun2011_v0_512508373 as sff10
        //        on sff10.region_cd = ff10.region_cd
        //        and sff10.facility_id = ff10.facility_id 
        //        and sff10.unit_id = ff10.unit_id
        //        and sff10.rel_point_id = ff10.rel_point_id
        //        and sff10.process_id = ff10.process_id
        //        and sff10.scc = ff10.scc
        //        and /*versioning query*/sff10.version IN (0) and sff10.dataset_id = 717        
        String ssffJoinStr = "left outer join ";
        ssffJoinStr += ssffTable + " as " + ssffAlias + " on ";
        ssffJoinStr += ssffAlias + ".region_cd" + " = " + ff10pAlias + ".region_cd"
                    +  " and " + ssffAlias + ".facility_id" + " = " + ff10pAlias + ".facility_id"
                    +  " and " + ssffAlias + ".unit_id" + " = " + ff10pAlias + ".unit_id"
                    +  " and " + ssffAlias + ".rel_point_id" + " = " + ff10pAlias + ".rel_point_id"
                    +  " and " + ssffAlias + ".process_id" + " = " + ff10pAlias + ".process_id"
                    +  " and " + ssffAlias + ".scc" + " = " + ff10pAlias + ".scc"
                    //+ /*supporting ff10 version query*/
                    +  " and " + ssffVersionQuery;        
        fromClause += ssffJoinStr + " ";
        
        // join 2
        //        -- get facility source type description
        //        left outer join reference.facility_source_type_codes
        //        on facility_source_type_codes.code || '' = ff10.fac_source_type
        String fstcvJoinStr = "left outer join " + fstcTable +  " as " + fstcAlias;
        fstcvJoinStr += " on " + fstcAlias + ".code || '' = " + ff10pAlias + ".fac_source_type";
        fromClause += fstcvJoinStr + " ";
        
        // join 3
        //        -- get state
        //        left outer join reference.fips
        //        on fips.country_code = ff10.country_cd
        //        and fips.state_county_fips = ff10.region_cd
        String fipsJoinStr = "left outer join " + fipsTable +  " as " + fipsAlias;
        fipsJoinStr += " on " + fipsAlias + ".country_code = " + ff10pAlias + ".country_cd";
        fipsJoinStr += " and " + fipsAlias + ".state_county_fips = " + ff10pAlias + ".region_cd";
        fromClause += fipsJoinStr + " ";        
        
        // join 4
        //        -- get pollutant descriptions
        //        left outer join reference.pollutant_codes
        //        on pollutant_codes.pollutant_code = ff10.poll    
        String pcJoinStr = "left outer join " + pcTable +  " as " + pcAlias;
        pcJoinStr += " on " + pcAlias + ".pollutant_code = " + ff10pAlias + ".poll";
        fromClause += pcJoinStr + " ";          
        
        // join 5
        //        -- get nei and frs ids 
        //        -- use string_agg for showing more than one
        //        -- use max for showing ONLY one
        //        left outer join (
        //                select fac_ff10.facility_id, 
        //                        /*USE WHEN ONLY SHOWING ALL*/ string_agg(distinct case when fac_ff10.program_system_code = 'EPANEI' then alt_agency_id else null end, '&') as nei_unique_ids,
        //                        --/*USE WHEN ONLY SHOWING ONE*/ max(distinct case when fac_ff10.program_system_code = 'EPANEI' then alt_agency_id else null end) as nei_unique_ids,
        //                        /*USE WHEN ONLY SHOWING ALL*/ string_agg(distinct case when fac_ff10.program_system_code = 'EPAFRS' then alt_agency_id else null end, '&') as frs_ids
        //                        --/*USE WHEN ONLY SHOWING ONE*/ max(distinct case when fac_ff10.program_system_code = 'EPAFRS' then alt_agency_id else null end) as frs_ids
        //                from emissions.ds_smokeflatfile_nei_facility_20110518_18may2011_v0_710448319 as fac_ff10 
        //                where fac_ff10.program_system_code in ( 'EPANEI', 'EPAFRS' ) 
        //                        and /*versioning query*/fac_ff10.version IN (0) and fac_ff10.dataset_id = 720
        //                group by fac_ff10.facility_id
        //        ) as fac_ff10
        //        on fac_ff10.facility_id = ff10.facility_id 
        /*USE WHEN ONLY SHOWING ALL*/ 
        String neiMulti = "string_agg(distinct case when " + facAlias + ".program_system_code = 'EPANEI' then alt_agency_id else null end, '&') as nei_unique_ids, ";
        /*USE WHEN ONLY SHOWING ONE*/
        String neiSingle = "max(distinct case when " + facAlias + ".program_system_code = 'EPANEI' then alt_agency_id else null end) as nei_unique_ids, ";
        /*USE WHEN ONLY SHOWING ALL*/
        String frsMulti = "string_agg(distinct case when " + facAlias + ".program_system_code = 'EPAFRS' then alt_agency_id else null end, '&') as frs_ids ";
        /*USE WHEN ONLY SHOWING ONE*/
        String frsSingle = "max(distinct case when " + facAlias + ".program_system_code = 'EPAFRS' then alt_agency_id else null end) as frs_ids ";
        String facSql = "select " + facAlias + ".facility_id, ";
        if ( boolMultiNEI) {
            facSql += neiMulti;
        } else {
            facSql += neiSingle;
        }
        if ( boolMultiFRS) {
            facSql += frsMulti;
        } else {
            facSql += frsSingle;
        }
        facSql += " from " + facTable + " as " + facAlias + " ";
        facSql += " where " + facAlias + ".program_system_code in ( 'EPANEI', 'EPAFRS' )";
        facSql += " and "; /*versioning query*/
        facSql += facVersionQuery + " ";
        facSql += "group by " + facAlias + ".facility_id";
        String facJoinStr = "left outer join (";
        facJoinStr += facSql;
        facJoinStr += ") as " + facAlias2;
        facJoinStr += " on " + facAlias2 + ".facility_id = " + ff10pAlias + ".facility_id ";
        
        fromClause += facJoinStr + " ";          
        
        // join 6
        //        -- get control id descriptions
        //        left outer join (
        //                select ff10_control_ids.control_ids,
        //               string_agg(coalesce(control_device_desc, 'Unknown control device code'), '&'
        //               ORDER BY public.array_idx(string_to_array(ff10_control_ids.control_ids, '&'),
        //               control_device.control_device_code || '')) as control_descs
        //                from (
        //                        select distinct ff10.control_ids 
        //                        FROM emissions.ds_smokeflatfile_point_20110517_csv_18may2011_v0_1013504575 as ff10 
        //                        where 
        //                                /*versioning query*/ff10.version IN (0) and ff10.dataset_id = 717 and ff10.control_ids is not null
        //                        ) as ff10_control_ids 
        //                left outer join reference.control_device 
        //                on control_device.control_device_code || '' = any(string_to_array(ff10_control_ids.control_ids, '&'))
        //                group by ff10_control_ids.control_ids
        //        ) ff10_control_id_descs
        //        on ff10_control_id_descs.control_ids = ff10.control_ids        
        String fciSql = "select distinct " + ff10pAlias + ".control_ids ";
        fciSql += "from " + ff10pTable + " as " + ff10pAlias + " ";
        fciSql += "where " + ff10pVersionQuery + " and " + ff10pAlias + ".control_ids is not null";
        
        String fcidSql = "select " + fciAlias + ".control_ids, ";
        fcidSql += "string_agg(coalesce(control_device_desc, 'Unknown control device code'), '&' ";
        fcidSql += "ORDER BY public.array_idx(string_to_array(ff10_control_ids.control_ids, '&'), ";
        fcidSql += cdAlias + ".control_device_code || '')) as control_descs ";
        fcidSql += "from (" + fciSql + ") as " + fciAlias + " ";
        fcidSql += "left outer join " + cdTable + " as " + cdAlias + " ";
        fcidSql += "on " + cdAlias + ".control_device_code || '' = any(string_to_array(" + fciAlias + ".control_ids, '&')) ";
        fcidSql += "group by " + fciAlias + ".control_ids";
      
        String fcidJoinStr = "left outer join (" + fcidSql + ") as " + fcidAlias + " ";
        fcidJoinStr += "on " + fcidAlias + ".control_ids = " + ff10pAlias + ".control_ids";
        
        fromClause += fcidJoinStr + " ";  

        // join 7
        //        -- get regulatory code descriptions
        //        left outer join (
        //                select ff10_reg_codes.reg_codes,
        //                       string_agg(coalesce(regulatory_codes.description, 'Unknown regulatory code'),
        //                       '&' ORDER BY public.array_idx(string_to_array(ff10_reg_codes.reg_codes, '&'),
        //                       regulatory_codes.code || '')) as reg_codes_descs
        //                from (
        //                        select distinct ff10.reg_codes 
        //                        FROM emissions.ds_smokeflatfile_point_20110517_csv_18may2011_v0_1013504575 as ff10 
        //                        where 
        //                                /*versioning query*/ff10.version IN (0) and
        //                                ff10.dataset_id = 717
        //                                and ff10.reg_codes is not null
        //                        ) as ff10_reg_codes 
        //                left outer join reference.regulatory_codes 
        //                on regulatory_codes.code || '' = any(string_to_array(ff10_reg_codes.reg_codes, '&'))
        //                group by ff10_reg_codes.reg_codes
        //        ) ff10_reg_code_descs
        //        on ff10_reg_code_descs.reg_codes = ff10.reg_codes
        String frcSql = "select distinct " + ff10pAlias + ".reg_codes "; 
        frcSql += "from " + ff10pTable + " as " + ff10pAlias + " ";
        frcSql += "where " + ff10pVersionQuery;
        String frcdSql = "select " + frcAlias + ".reg_codes, ";
        frcdSql += "string_agg(coalesce(" + rcAlias + ".description, 'Unknown regulatory code'), ";
        frcdSql += "'&' ORDER BY public.array_idx(string_to_array(" + frcAlias + ".reg_codes, '&'), ";
        frcdSql += rcAlias + ".code || '')) as reg_codes_descs ";
        frcdSql += "from (" + frcSql + ") as " + frcAlias + " ";
        frcdSql += "left outer join " + rcTable + " as " + rcAlias + " ";
        frcdSql += "on " + rcAlias + ".code || '' = any(string_to_array(" + frcAlias + ".reg_codes, '&')) ";
        frcdSql += "group by " + frcAlias + ".reg_codes";
        String frcdJoinStr = "left outer join (" + frcdSql + ") as " + frcdAlias + " ";
        frcdJoinStr += "on " + frcdAlias + ".reg_codes = " + ff10pAlias + ".reg_codes";
        fromClause += frcdJoinStr + " ";
                
        //where clause
        whereClause = "where " + ff10pVersionQuery;
        
        // construct the sql here
        sqlStr += selectClause + fromClause + whereClause + ";";
        
        SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissionDatasourceName, tableName );
        return parser.createTableQuery() + " " + sqlStr;
    }
    
    // the returned string does not end with a ,
    private String getFF10Columns( String qualifiedTableName, String alias) throws EmfException {
        
        //        ff10.country_cd, 
        //        ff10.region_cd, 
        //        ff10.tribal_code, 
        //        ff10.facility_id, 
        //        ff10.unit_id, 
        //        ff10.rel_point_id, 
        //        ff10.process_id, 
        //        ff10.agy_facility_id, 
        //        ff10.agy_unit_id, 
        //        ff10.agy_rel_point_id, 
        //        ff10.agy_process_id, 
        //        ff10.scc, 
        //        ff10.poll, 
        //        ff10.ann_value, 
        //        ff10.ann_pct_red, 
        //        ff10.facility_name, 
        //        ff10.erptype, 
        //        ff10.stkhgt, 
        //        ff10.stkdiam, 
        //        ff10.stktemp, 
        //        ff10.stkflow, 
        //        ff10.stkvel, 
        //        ff10.naics, 
        //        ff10.longitude, 
        //        ff10.latitude, 
        //        ff10.ll_datum, 
        //        ff10.horiz_coll_mthd, 
        //        ff10.design_capacity, 
        //        ff10.design_capacity_units, 
        //        ff10.reg_codes, 
        //        ff10.fac_source_type, 
        //        ff10.unit_type_code, 
        //        ff10.control_ids, 
        //        ff10.control_measures, 
        //        ff10.current_cost, 
        //        ff10.cumulative_cost, 
        //        ff10.projection_factor, 
        //        ff10.submitter_id, 
        //        ff10.calc_method, 
        //        ff10.data_set_id, 
        //        ff10.facil_category_code, 
        //        ff10.oris_facility_code, 
        //        ff10.oris_boiler_id, 
        //        ff10.ipm_yn, 
        //        ff10.calc_year, 
        //        ff10.date_updated, 
        //        ff10.fug_height, 
        //        ff10.fug_width_ydim, 
        //        ff10.fug_length_xdim, 
        //        ff10.fug_angle, 
        //        ff10.zipcode, 
        //        ff10.annual_avg_hours_per_year, 
        //        ff10.jan_value, 
        //        ff10.feb_value, 
        //        ff10.mar_value, 
        //        ff10.apr_value, 
        //        ff10.may_value, 
        //        ff10.jun_value, 
        //        ff10.jul_value, 
        //        ff10.aug_value, 
        //        ff10.sep_value, 
        //        ff10.oct_value, 
        //        ff10.nov_value, 
        //        ff10.dec_value, 
        //        ff10.jan_pctred, 
        //        ff10.feb_pctred, 
        //        ff10.mar_pctred, 
        //        ff10.apr_pctred, 
        //        ff10.may_pctred, 
        //        ff10.jun_pctred, 
        //        ff10.jul_pctred, 
        //        ff10.aug_pctred, 
        //        ff10.sep_pctred, 
        //        ff10.oct_pctred, 
        //        ff10.nov_pctred, 
        //        ff10.dec_pctred, 
        //        ff10."comment", 
        //--      ff10.comments,
        
        try {
            String colNames = "";
            
            String query = "select * from " + qualifiedTableName + " where 1=0;";
            
            if (DebugLevels.DEBUG_22())
                System.out.println("\n query: " + query);

            ResultSet rs = datasource.query().executeQuery(query);
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            int startingColumn=1;
            if (md.getColumnName(1).equalsIgnoreCase("record_id"))
            {
               // skip the first four columns from the dataset info and start at column 5
               startingColumn=5;
            }
            
            String colName = "";
            for (int i = startingColumn; i <= columnCount; i++) {
                colName = "" + md.getColumnName(i);
                if ( colName.trim().equalsIgnoreCase("disable") ||
                     colName.trim().equalsIgnoreCase("comments")) {
                    continue;
                }
                colNames += alias + ".\"" + md.getColumnName(i) + "\", ";
            }
            
            if ( colNames.endsWith(", "))
                colNames = colNames.substring(0, colNames.length()-2);
            else if ( colNames.endsWith(","))
                colNames = colNames.substring(0, colNames.length()-1);

            return colNames;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Error happened when fetching column names from " + qualifiedTableName + ": ", e);
        }
    }
    
    private void validateDatasetVersion( String qualifiedTableName, String alias, String versionQuery) throws EmfException {
        try {
            
            String query = "select * from " + qualifiedTableName + " as " + alias + " where 0=1";
            if ( versionQuery != null && !versionQuery.trim().equals("")) {
                query += " and " + versionQuery + ";";
            } else {
                query += ";";
            }
            if (DebugLevels.DEBUG_22())
                System.out.println("\n query: " + query);
            datasource.query().executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Dataset or version does not exist - " + qualifiedTableName + " : " + versionQuery + ": ", e);
        }        
    }
    private void validateWhereFilter( String qualifiedTableName, String alias, String sqlWhereFilter) throws EmfException {
        
        try {
            
            String query = "select * from " + qualifiedTableName + " as " + alias + " where 0=1";
            if ( sqlWhereFilter != null && !sqlWhereFilter.trim().equals("")) {
                query += " and " + sqlWhereFilter.trim() + ";";
            }else {
                query += ";";
            }                
            
            if (DebugLevels.DEBUG_22())
                System.out.println("\n query: " + query);

            datasource.query().executeQuery(query);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException("Invalid where filer for " + qualifiedTableName + ": ", e);
        }
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
    
    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(qaStep.getWho());
        endStatus.setType("RunQAStep");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);

    }
    
    private void createFF10PIndexes(Dataset dataset) {
        DataTable dataTable = new DataTable(dataset, datasource);
        String table = emissionTableName(dataset);
        setStatus("Started creating indexes on inventory, " 
                + dataset.getName() 
                + ".  Depending on the size of the dataset, this could take several minutes.");

        //ALWAYS create indexes for these core columns...
        dataTable.addIndex(table, "record_id", true);
        dataTable.addIndex(table, "dataset_id", false);
        dataTable.addIndex(table, "version", false);
        dataTable.addIndex(table, "delete_versions", false);

        dataTable.addIndex(table, "region_cd", false);
        dataTable.addIndex(table, "facility_id", false);
        dataTable.addIndex(table, "unit_id", false);
        dataTable.addIndex(table, "rel_point_id", false);
        dataTable.addIndex(table, "process_id", false);
        dataTable.addIndex(table, "scc", false);
        dataTable.addIndex(table, "poll", false);

        //finally analyze the table, so the indexes take affect immediately, 
        //NOT when the SQL engine gets around to analyzing eventually
        dataTable.analyzeTable(table);
    
        setStatus("Completed creating indexes on inventory, " 
                + dataset.getName() 
                + ".");
    }
    
    private void createSSFFIndexes(Dataset dataset) {
        DataTable dataTable = new DataTable(dataset, datasource);
        String table = emissionTableName(dataset);
        setStatus("Started creating indexes on inventory, " 
                + dataset.getName() 
                + ".  Depending on the size of the dataset, this could take several minutes.");

        //ALWAYS create indexes for these core columns...
        dataTable.addIndex(table, "record_id", true);
        dataTable.addIndex(table, "dataset_id", false);
        dataTable.addIndex(table, "version", false);
        dataTable.addIndex(table, "delete_versions", false);

        dataTable.addIndex(table, "region_cd", false);
        dataTable.addIndex(table, "facility_id", false);
        dataTable.addIndex(table, "unit_id", false);
        dataTable.addIndex(table, "rel_point_id", false);
        dataTable.addIndex(table, "process_id", false);
        dataTable.addIndex(table, "scc", false);

        //finally analyze the table, so the indexes take affect immediately, 
        //NOT when the SQL engine gets around to analyzing eventually
        dataTable.analyzeTable(table);
    
        setStatus("Completed creating indexes on inventory, " 
                + dataset.getName() 
                + ".");
    }    

    private void createFACIndexes(Dataset dataset) {
        DataTable dataTable = new DataTable(dataset, datasource);
        String table = emissionTableName(dataset);
        setStatus("Started creating indexes on inventory, " 
                + dataset.getName() 
                + ".  Depending on the size of the dataset, this could take several minutes.");

        //ALWAYS create indexes for these core columns...
        dataTable.addIndex(table, "record_id", true);
        dataTable.addIndex(table, "dataset_id", false);
        dataTable.addIndex(table, "version", false);
        dataTable.addIndex(table, "delete_versions", false);

        dataTable.addIndex(table, "facility_id", false);
        dataTable.addIndex(table, "program_system_code", false);

        //finally analyze the table, so the indexes take affect immediately, 
        //NOT when the SQL engine gets around to analyzing eventually
        dataTable.analyzeTable(table);
    
        setStatus("Completed creating indexes on inventory, " 
                + dataset.getName() 
                + ".");
    } 
}
