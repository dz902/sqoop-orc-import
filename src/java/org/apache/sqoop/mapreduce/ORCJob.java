package org.apache.sqoop.mapreduce;

import org.apache.avro.Schema;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.junit.Assert;

import java.math.BigDecimal;

public final class ORCJob {
    public static final String MAP_OUTPUT_SCHEMA = "orc.map.output.schema";

    /**
     * The configuration key for a job's output schema.
     */
    public static final String OUTPUT_SCHEMA = "orc.output.schema";

    private ORCJob() {
    }

    public static void setMapOutputSchema(Configuration job, String s) {
        job.set(MAP_OUTPUT_SCHEMA, s.toString());
    }

    /**
     * Return a job's map output key schema.
     */
    public static String getMapOutputSchema(Configuration job) {
        return job.get(MAP_OUTPUT_SCHEMA, job.get(OUTPUT_SCHEMA));
    }

    /**
     * Return a job's output key schema.
     */
    public static String getOutputSchema(Configuration job) {
        return job.get(OUTPUT_SCHEMA);
    }


    /**
     *
     * some special data type is not supported in orc,should covert it to hive type
     * @param obj Object
     * @return
     */
    public static Object convertSpecialRecordType(Object obj) {
        if (obj instanceof java.math.BigDecimal) {
            // not allow Rounding
            return HiveDecimal.create((BigDecimal) obj,false);
        } else {
            return obj;
        }
    }
}
