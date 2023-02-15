package org.apache.sqoop.hive;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Types;

/**
 * Defines conversion between SQL types and Hive types.
 */
public final class HiveTypesORC {

    public static final Log LOG = LogFactory.getLog(HiveTypesORC.class.getName());

    private HiveTypesORC() { }

    /**
     * Given JDBC SQL types coming from another database, what is the best
     * mapping to a Hive-specific type?
     */
    public static String toHiveType(int sqlType) {

        switch (sqlType) {
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                return "INT";
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.NVARCHAR:
            case Types.NCHAR:
            case Types.LONGNVARCHAR:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.CLOB:
                return "STRING";
            case Types.NUMERIC:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.REAL:
                return "DOUBLE";
            case Types.BIT:
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.BIGINT:
                return "BIGINT";
            case Types.DECIMAL:
                return "DECIMAL";
            default:
                // TODO(aaron): Support BINARY, VARBINARY, LONGVARBINARY, DISTINCT,
                // BLOB, ARRAY, STRUCT, REF, JAVA_OBJECT.
                return null;
        }
    }

    /**
     * @return true if a sql type can't be translated to a precise match
     * in Hive, and we have to cast it to something more generic.
     */
    public static boolean isHiveTypeImprovised(int sqlType) {
        return sqlType == Types.DATE || sqlType == Types.TIME
                || sqlType == Types.TIMESTAMP
                || sqlType == Types.DECIMAL
                || sqlType == Types.NUMERIC;
    }
}
