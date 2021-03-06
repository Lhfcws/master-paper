package paper.query;

import org.apache.spark.api.java.function.Function2;
import paper.MLLibConfiguration;
import com.datatub.iresearch.analyz.util.SparkUtil;
import com.yeezhao.commons.util.FileSystemHelper;
import com.yeezhao.commons.util.GsonSerializer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import paper.community.model.WeiboUser;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author lhfcws
 * @since 16/1/6.
 */
public class WeiboContentScanSpark implements Serializable {

    public void run(String input, String output) {
        Configuration conf = MLLibConfiguration.getInstance();

        FileSystemHelper fs = FileSystemHelper.getInstance(conf);
        try {
            fs.deleteFile(output + ".dir");
        } catch (IOException e) {
        }
        try {
            fs.deleteFile(output);
        } catch (IOException e) {
        }

        Map<String, String> params = new HashMap<>();
        params.put("spark.executor.memory", "3g");
        SparkConf sparkConf = SparkUtil.createSparkConf("Comm-ESWeiboContentScan", 40, this.getClass(), params);
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        jsc.textFile(input).repartition(20).flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(String s) throws Exception {
//                WeiboUser weiboUser = GsonSerializer.fromJson(s.trim(), WeiboUser.class);
//                String uid = s;
                return HbaseContentScanner.getInstance().scanContent(s);
            }
        }).saveAsTextFile(output);
        jsc.stop();

//        System.out.println("Merging file " + output);
//        try {
//            fs.mergeDirsToFile(output, output + ".dir");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        System.out.println("WeiboContentScanSpark DONE");
    }

    public static <T> Set<T> intersect(Set<T> set1, Set<T> set2) {
        Set<T> ret = new HashSet<>();
        for (T t : set1)
            if (set2.contains(t))
                ret.add(t);
        return ret;
    }

    // ============= ContentScanner ===============

    public static class HbaseContentScanner implements Serializable {
        private static final String TABLE = "yeezhao.user.info";
        private HConnection hConnection = null;
        private HTableInterface hTableInterface = null;

        // ============= SINGLETON ==========

        private static HbaseContentScanner _singleton = null;

        public static HbaseContentScanner getInstance() {
            if (_singleton == null)
                synchronized (HbaseContentScanner.class) {
                    if (_singleton == null) {
                        _singleton = new HbaseContentScanner();
                    }
                }
            return _singleton;
        }

        private HbaseContentScanner() {
            try {
                Configuration conf = MLLibConfiguration.getInstance();
                hConnection = HConnectionManager.createConnection(conf);
                hTableInterface = hConnection.getTable(TABLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            hTableInterface.setAutoFlush(false, false);
        }

        public List<String> scanContent(String uid) throws IOException {
            Scan scan = new Scan();
            scan.setStartRow(("sn|" + uid + "|wb|").getBytes());
            scan.setStopRow(("sn|" + uid + "|wc|").getBytes());
            scan.addColumn("crawl".getBytes(), "fp_content".getBytes());

            LinkedList<String> list = new LinkedList<>();
            ResultScanner scanner = hTableInterface.getScanner(scan);
            for (Result result : scanner) {
                byte[] v = result.getValue("crawl".getBytes(), "fp_content".getBytes());
                String content = new String(v).replaceAll("\t", " ").trim();
                if (content.length() > 5)
                    list.add(uid + "\t" + content);
            }
            return list;
        }
    }

    // ============= MAIN ===============

    /**************************************
     * Test main
     */
    public static void main(String[] args) {
        WeiboContentScanSpark weiboContentScanSpark = new WeiboContentScanSpark();
        weiboContentScanSpark.run("/tmp/midea_users.txt", "/tmp/midea_contents");
    }
}
