package gov.epa.emissions.framework.client.data.datasettype;

import com.sun.org.apache.xpath.internal.operations.Bool;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.JdbcToCommonsSqlTypeMap;
import gov.epa.emissions.commons.db.postgres.PostgresSqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.ui.RowSource;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class EditableColumnRowSource implements RowSource {

    private Column source;

    private Boolean selected;

    public EditableColumnRowSource(Column source) {
        this.source = source;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected, source.getName(), source.getSqlType(), source.getDefaultValue(),
                source.isMandatory(), source.getDescription(), source.getFormatterClass(), source.getConstraints(),
        source.getWidth(), source.getSpaces(), source.getFixFormatStart(), source.getFixFormatEnd()};
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
        case 1:
            source.setName((String)val);
            break;
        case 2:
            source.setSqlType((String)val);
            break;
        case 3:
            source.setDefaultValue((String)val);
            break;
        case 4:
            source.setMandatory(Boolean.parseBoolean((String)val));
            break;
        case 5:
            source.setDescription((String)val);
            break;
        case 6:
            source.setFormatterClass((String)val);
            break;
        case 7:
            source.setConstraints((String)val);
            break;
        case 8:
            source.setWidth(Integer.parseInt((String)val));
            break;
        case 9:
            source.setSpaces(Integer.parseInt((String)val));
            break;
        case 10:
            source.setFixFormatStart(Integer.parseInt((String)val));
            break;
        case 11:
            source.setFixFormatEnd(Integer.parseInt((String)val));
            break;
        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    public Object source() {
        return source;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }

    public void validate(int rowNumber) throws EmfException {
        Column column = source;
        if (StringUtils.isBlank(column.getName())) {
            throw new EmfException("On File Format panel, missing column name at row " + rowNumber);
        }
        if (StringUtils.isBlank(column.getSqlType())) {
            throw new EmfException("On File Format panel, missing column SQL data type for column, " + column.getName());
        }
        if (!validateDataType(column)) {
            throw new EmfException("On File Format panel, unknown column SQL data type for column, " + column.getName());
        }
    }

    private boolean validateDataType(Column column) throws EmfException {
        JdbcToCommonsSqlTypeMap typeMap = new JdbcToCommonsSqlTypeMap(new PostgresSqlDataTypes());

        String columnDataType = column.getSqlType().toUpperCase();

        for (Object sqlType : typeMap.getSqlTypeMap().values()) {
            String sqlTypeString = ((String)sqlType).toUpperCase();

            if (columnDataType.startsWith("VARCHAR")) { //VARCHAR
//TODO:  evaluate width, doesnt seem to be used....
//                int end =  columnDataType.lastIndexOf(")");
//                //Here startIndex is inclusive while endIndex is exclusive.
//                int length = Integer.parseInt(columnDataType.substring(8, end));
//                if ((column.getWidth()+"").length() > length)
//                    throw new EmfException("Error format for column, " + column.getName() + ", expected: VARCHAR(" + column.getWidth() + "), but was: " + columnDataType);
                return true;
            } else if (columnDataType.equals(sqlTypeString)) {
                return true;
            }
        }
        return false;
    }
}