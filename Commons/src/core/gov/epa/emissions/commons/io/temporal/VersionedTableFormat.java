package gov.epa.emissions.commons.io.temporal;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.LongFormatter;
import gov.epa.emissions.commons.io.NullFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.TableFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VersionedTableFormat implements TableFormat {
    private FileFormat base;

    private Column[] cols;
    
    private int offset=0; 

    public VersionedTableFormat(FileFormat base, SqlDataTypes types) {
        this.base = base;
        cols = createCols(base, types);
    }

    public VersionedTableFormat(FileFormat base, SqlDataTypes types, String lineNum) {
        this.base = base;
        cols = createCols(base, types, lineNum); //add one more column named "lineNum"
    }

    public String key() {
        return "Record_Id";
    }

    public Column[] cols() {
        return cols;
    }

    private Column[] createCols(FileFormat base, SqlDataTypes types) {
        List<Column> cols = new ArrayList<Column>();
        offset =4; 
        cols.addAll(Arrays.asList(versionCols(types)));
        cols.addAll(Arrays.asList(base.cols()));// sandwich data b/w version cols and Comments

        Column inlineComments = new Column("Comments", types.text(), new NullFormatter());
        cols.add(inlineComments);

        return cols.toArray(new Column[0]);
    }

    private Column[] createCols(FileFormat base, SqlDataTypes types, String lineNum) {
        List<Column> cols = new ArrayList<Column>();
        offset =5; 
        cols.addAll(Arrays.asList(versionCols(types)));
        cols.add(new Column(lineNum, types.realType(), new RealFormatter())); //add line number column
        cols.addAll(Arrays.asList(base.cols()));// sandwich data b/w version cols and Comments
        Column inlineComments = new Column("Comments", types.text(), new NullFormatter());
        cols.add(inlineComments);
        
        return cols.toArray(new Column[0]);
    }

    private Column[] versionCols(SqlDataTypes types) {
        Column recordId = recordID(types);
        Column datasetId = new Column("Dataset_Id", types.longType(), new LongFormatter(), "NOT NULL");
        Column version = new Column("Version", types.intType(), new NullFormatter(), "NULL DEFAULT 0");
        Column deleteVersions = new Column("Delete_Versions", types.text(), new NullFormatter(), "DEFAULT ''::text");

        return new Column[] { recordId, datasetId, version, deleteVersions };
    }

    private Column recordID(SqlDataTypes types) {
        String key = key();
        String keyType = types.autoIncrement() + ",Primary Key(" + key + ")";
        Column recordId = new Column(key, keyType, new NullFormatter());
        return recordId;
    }

    public String identify() {
        return base.identify();
    }
    
    public int getOffset(){
        return offset; 
    }

    public int getBaseLength() {
        return base.cols().length;
    }
}
