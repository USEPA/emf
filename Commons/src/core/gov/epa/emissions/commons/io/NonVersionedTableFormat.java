package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.db.SqlDataTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NonVersionedTableFormat implements TableFormat {

    private FileFormat base;

    private SqlDataTypes types;
    
    private Column[] cols;
    
    private int offset=0; 

    public NonVersionedTableFormat(FileFormat base, SqlDataTypes types) {
        this.base = base;
        this.types = types;
        this.cols = createCols();
    }

    public NonVersionedTableFormat(FileFormat base, SqlDataTypes types, String lineNum) {
        this.base = base;
        this.types = types;
        this.cols =createCols(lineNum); //add a column called "lineNum"
    }

    public String key() {
        return "Dataset_Id";
    }

    public String identify() {
        return base.identify();
    }

    public Column[] cols() {
        return this.cols;
    }

    private Column[] createCols() {
        List<Column> cols = new ArrayList<Column>();
        //offset=0;   //NOTE: WRONG! Since you add datasetId column to the base file format
        offset = 1;
        cols.addAll(Arrays.asList(base.cols()));

        Column datasetId = new Column(key(), types.longType(), new LongFormatter());
        cols.add(0, datasetId);

        Column inlineComments = new Column("Comments", types.text(), new NullFormatter());
        cols.add(inlineComments);

        return cols.toArray(new Column[0]);
    }

    private Column[] createCols(String lineNum) {
        List<Column> cols = new ArrayList<Column>();
        //offset =1;   //NOTE: WRONG! Since you add "lineNum" and "datasetId" columns before the base columns
        offset = 2;
        cols.add(new Column(lineNum, types.realType(), new RealFormatter())); //add line number column
        cols.addAll(Arrays.asList(base.cols()));
        
        Column datasetId = new Column(key(), types.longType(), new LongFormatter());
        cols.add(0, datasetId);
        
        Column inlineComments = new Column("Comments", types.text(), new NullFormatter());
        cols.add(inlineComments);
        
        return cols.toArray(new Column[0]);
    }
    
    public int getOffset(){
        return offset; 
    }
    
    public int getBaseLength(){
        return base.cols().length;
    }

}
