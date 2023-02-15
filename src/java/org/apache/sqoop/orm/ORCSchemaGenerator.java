package org.apache.sqoop.orm;

import org.apache.sqoop.SqoopOptions;
import org.apache.sqoop.manager.ConnManager;;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.serde2.typeinfo.StructTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;

import java.io.IOException;
import java.util.*;

/**
 * Creates an Avro schema to represent a table from a database.
 */
public class ORCSchemaGenerator {

    public static final Log LOG =
            LogFactory.getLog(ORCSchemaGenerator.class.getName());

    private final SqoopOptions options;
    private final ConnManager connManager;
    private final String tableName;

    public ORCSchemaGenerator(final SqoopOptions opts, final ConnManager connMgr,
                              final String table) {
        this.options = opts;
        this.connManager = connMgr;
        this.tableName = table;
    }

    public String generate() throws IOException {
        ClassWriter classWriter = new ClassWriter(options, connManager,
                tableName, null);
        Map<String, Integer> columnTypes = classWriter.getColumnTypes();
        String[] columnNames = classWriter.getColumnNames(columnTypes);

        Map<String,List<Integer>> columnInfos = classWriter.getColumnInfo();
        LOG.info("columnInfos===");
        LOG.info(columnInfos);

        List<Field> fields = new ArrayList<Field>();

        // init empty buf
        ArrayList<String> cleanedCols = new ArrayList<String>();
        StringBuilder colTypeBuf = new StringBuilder();
        boolean first = true;
        for (String columnName : columnNames) {
            String cleanedCol = ClassWriter.toJavaIdentifier(columnName);
            int sqlType = columnTypes.get(columnName);
            String orcType = toORCType(sqlType);
            if(orcType.equalsIgnoreCase("DECIMAL")){
                int precise=columnInfos.get(columnName).get(1);
                int scale = columnInfos.get(columnName).get(2);
                orcType=orcType+"("+precise+","+scale+")";
            }
            if (!first) {
                colTypeBuf.append(":");
            }
            cleanedCols.add(cleanedCol);
            colTypeBuf.append(orcType);
            first = false;
        }

        LOG.info("colTypeBuf=="+colTypeBuf);

        ArrayList<TypeInfo> fieldTypes =
                TypeInfoUtils.getTypeInfosFromTypeString(colTypeBuf.toString().toLowerCase());
        StructTypeInfo rootType = new StructTypeInfo();
        rootType.setAllStructFieldNames(cleanedCols);
        rootType.setAllStructFieldTypeInfos(fieldTypes);
        return rootType.toString();
    }

    private String toORCType(int sqlType) {
        return connManager.toHiveORCType(sqlType);
    }

}
