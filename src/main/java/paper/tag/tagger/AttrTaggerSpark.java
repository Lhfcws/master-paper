package paper.tag.tagger;

import com.datatub.iresearch.analyz.util.SparkUtil;
import com.yeezhao.commons.util.FileSystemHelper;
import com.yeezhao.commons.util.FreqDist;
import com.yeezhao.commons.util.GsonSerializer;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import paper.MLLibConfiguration;
import paper.community.model.WeiboUser;
import scala.Tuple2;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author lhfcws
 * @since 16/4/27
 */
public class AttrTaggerSpark implements Serializable {

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
        SparkConf sparkConf = SparkUtil.createSparkConf("Comm-AttrTagging", 40, this.getClass(), params);
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        jsc.textFile(input).repartition(20).flatMapToPair(new PairFlatMapFunction<String, String, FreqDist<String>>() {
            @Override
            public Iterable<Tuple2<String, FreqDist<String>>> call(String s) throws Exception {
                List<Tuple2<String, FreqDist<String>>> list = new LinkedList<>();
                try {
                    WeiboUser u = com.yeezhao.commons.util.serialize.GsonSerializer.deserialize(s, WeiboUser.class);
                    String uid = u.id;

                    AttrTagger tagger = AttrTagger.getInstance();
                    FreqDist<String> tagDist = tagger.tag(u);
                    list.add(new Tuple2<>(uid, tagDist));
                } catch (Exception e) {
                    System.err.println("[GSON ERROR] " + s);
                }
                return list;
            }
        }).reduceByKey(new Function2<FreqDist<String>, FreqDist<String>, FreqDist<String>>() {
            @Override
            public FreqDist<String> call(FreqDist<String> v1, FreqDist<String> v2) throws Exception {
                v1.merge(v2);
                return v1;
            }
        }).flatMap(new FlatMapFunction<Tuple2<String, FreqDist<String>>, String>() {
            @Override
            public Iterable<String> call(Tuple2<String, FreqDist<String>> tp) throws Exception {
                List<String> list = new ArrayList<String>();
                list.add(new StringBuilder(tp._1()).append("\t").append(GsonSerializer.toJson(tp._2())).toString());
                return list;
            }
        }).saveAsTextFile(output + ".dir");
        jsc.stop();

        System.out.println("Merging file " + output);
        try {
            fs.mergeDirsToFile(output, output + ".dir");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("AttrTaggerSpark DONE");
    }

}
