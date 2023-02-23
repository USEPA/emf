package gov.epa.emissions.commons.db;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class JdbcToCommonsSqlTypeMap {

    private Map map;

    public JdbcToCommonsSqlTypeMap(SqlDataTypes types) {
        map = new HashMap();
        map.put(Integer.valueOf(Types.INTEGER), types.intType());
        map.put(Integer.valueOf(Types.BOOLEAN), types.booleanType());
        map.put(Integer.valueOf(Types.BIT), types.booleanType());
        map.put(Integer.valueOf(Types.CHAR), types.charType());
        map.put(Integer.valueOf(Types.BIGINT), types.longType());
        map.put(Integer.valueOf(Types.REAL), types.realType());
        map.put(Integer.valueOf(Types.DOUBLE), types.realType());
        map.put(Integer.valueOf(Types.SMALLINT), types.smallInt());
        map.put(Integer.valueOf(Types.LONGVARCHAR), types.text());
        map.put(Integer.valueOf(Types.VARCHAR), types.stringType());
        map.put(Integer.valueOf(Types.DATE), types.timestamp());
        map.put(Integer.valueOf(Types.TIMESTAMP), types.timestamp());
    }

    public String get(int type) {
        return (String) map.get(Integer.valueOf(type));
    }

    public Map getSqlTypeMap() {
        return map;
    }

}
