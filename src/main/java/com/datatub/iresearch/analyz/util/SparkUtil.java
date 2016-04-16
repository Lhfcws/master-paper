package com.datatub.iresearch.analyz.util;

import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.yeezhao.commons.util.config.CommConsts;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Map;

/**
 * @author lhfcws
 * @since 15/11/13.
 */
public class SparkUtil {
    private static JavaSparkContext jSparkContext;

    public static SparkConf createSparkConf(String appName, int parallism, Class klazz, Map<String, String> params) {
        Configuration conf = MLLibConfiguration.getInstance();
        SparkConf sparkConf = new SparkConf()
                .setMaster(conf.get(CommConsts.SPARK_MASTER_URL, "local[*]")).setAppName(appName)
                .setJars(JavaSparkContext.jarOfClass(klazz))
                .set("spark.executor.memory", conf.get("spark.common.mem"))
                .set("spark.cores.max", String.valueOf(parallism))
                .set("spark.default.parallelism", String.valueOf(parallism));

        if (params != null)
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sparkConf = sparkConf.set(entry.getKey(), entry.getValue());
            }

        String sparkHome = System.getenv("SPARK_HOME");
        if (sparkHome != null && sparkHome.length() > 0) {
            System.out.println("SparkHome: " + sparkHome);
            sparkConf = sparkConf.setSparkHome(sparkHome);
        }
        return sparkConf;
    }

    public static JavaSparkContext createLocalJavaSparkContext(String appName, int parallism, Class klazz, Map<String, String> params) {
        if (parallism < 1) parallism = 1;
        Configuration conf = MLLibConfiguration.getInstance();
        SparkConf sparkConf = new SparkConf()
                .setMaster("local[*]").setAppName(appName)
                .setJars(JavaSparkContext.jarOfClass(klazz))
                .set("spark.executor.memory", conf.get("spark.local.mem"))
                .set("spark.cores.max", String.valueOf(parallism))
                .set("spark.default.parallelism", String.valueOf(parallism));

        if (params != null)
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sparkConf = sparkConf.set(entry.getKey(), entry.getValue());
            }

        String sparkHome = System.getenv("SPARK_HOME");
        if (sparkHome != null && sparkHome.length() > 0) {
            System.out.println("SparkHome: " + sparkHome);
            sparkConf = sparkConf.setSparkHome(sparkHome);
        }
        return new JavaSparkContext(sparkConf);
    }
}
