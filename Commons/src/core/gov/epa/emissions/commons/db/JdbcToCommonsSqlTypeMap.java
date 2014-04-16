package gov.epa.emissions.commons.db;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class JdbcToCommonsSqlTypeMap {

    private Map map;

    public JdbcToCommonsSqlTypeMap(SqlDataTypes types) {
        map = new HashMap();
        map.put(new Integer(Types.INTEGER), types.intType());
        map.put(new Integer(Types.BOOLEAN), types.booleanType());
        map.put(new Integer(Types.BIT), types.booleanType());
        map.put(new Integer(Types.CHAR), types.charType());
        map.put(new Integer(Types.BIGINT), types.longType());
        map.put(new Integer(Types.REAL), types.realType());
        map.put(new Integer(Types.DOUBLE), types.realType());
        map.put(new Integer(Types.SMALLINT), types.smallInt());
        map.put(new Integer(Types.LONGVARCHAR), types.text());
        map.put(new Integer(Types.VARCHAR), types.stringType());
        map.put(new Integer(Types.DATE), types.timestamp());
        map.put(new Integer(Types.TIMESTAMP), types.timestamp());
    }

    public String get(int type) {
        return (String) map.get(new Integer(type));
    }

}
