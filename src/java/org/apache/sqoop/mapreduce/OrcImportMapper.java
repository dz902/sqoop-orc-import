package org.apache.sqoop.mapreduce;


import org.apache.sqoop.lib.SqoopRecord;
import org.apache.sqoop.mapreduce.AutoProgressMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.ql.io.orc.OrcSerde;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.StructTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Writable;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Imports records by transforming them to Orc records in an orc data file.
 */
public class OrcImportMapper
        extends AutoProgressMapper<LongWritable, SqoopRecord,
        Object, Writable> {
    public static final Log LOG = LogFactory.getLog(OrcImportMapper.class.getName());

    private StructTypeInfo typeInfo;
    private ObjectInspector oip;
    private OrcSerde serde = new OrcSerde();
    private Writable row;
    private Map<String, Integer> indexOFFieldsName;
    private ArrayList<String> fieldNameList;

    @Override
    protected void setup(Context context)
            throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        String orcSchema = ORCJob.getMapOutputSchema(conf);
        LOG.info("orc schema = " + orcSchema);
        typeInfo = (StructTypeInfo) TypeInfoUtils.getTypeInfoFromTypeString(orcSchema);
        oip = TypeInfoUtils.getStandardJavaObjectInspectorFromTypeInfo(typeInfo);
        fieldNameList = typeInfo.getAllStructFieldNames();
    }

    @Override
    protected void cleanup(Context context) throws IOException {
        // do clean
    }

    @Override
    public void map(LongWritable key, SqoopRecord val, Context context)
            throws IOException, InterruptedException {
        List<Object> struct = toGenericRecord(val.getFieldMap());
        row = serde.serialize(struct, oip);
        context.write(null, row);
    }

    public List<Object> toGenericRecord(Map<String, Object> fieldMap) {
        List<Object> struct = new ArrayList<Object>(fieldNameList.size());
        for (String fieldName:fieldNameList) {
            try {
                Object obj = fieldMap.get(fieldName);
                if(obj==null){
                    obj=fieldMap.get(fieldName.toUpperCase());
                }
                if(obj==null){
                    obj=fieldMap.get(fieldName.toLowerCase());
                }
                //LOG.info("java object to write into ORC file:"+obj.toString()+","+obj.getClass().getName());
                obj = ORCJob.convertSpecialRecordType(obj);
                struct.add(obj);
            }
            catch(Exception es){
                LOG.info("fieldName:"+fieldName + " not found neither in upper nor lower !");

            }
        }
        return struct;
    }
    //HiveDecimal
}