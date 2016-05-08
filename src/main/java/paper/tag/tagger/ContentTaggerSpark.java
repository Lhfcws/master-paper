package paper.tag.tagger;

import com.datatub.iresearch.analyz.util.SparkUtil;
import com.yeezhao.commons.util.FileSystemHelper;
import com.yeezhao.commons.util.FreqDist;
import com.yeezhao.commons.util.GsonSerializer;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
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
 * @since 16/4/18
 */
public class ContentTaggerSpark implements Serializable {

    public ContentTaggerSpark() {

    }

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
        SparkConf sparkConf = SparkUtil.createSparkConf("Comm-ContentTagging", 40, this.getClass(), params);
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        jsc.textFile(input).repartition(20).flatMapToPair(new PairFlatMapFunction<String, String, FreqDist<String>>() {
            @Override
            public Iterable<Tuple2<String, FreqDist<String>>> call(String s) throws Exception {
                String[] sarr = s.split("\t");
                List<Tuple2<String, FreqDist<String>>> list = new LinkedList<>();
                FreqDist<String> tagDist = new FreqDist<>();

                if (sarr.length == 2) {
                    String uid = sarr[0];
                    ContentTagger tagger = ContentTagger.getInstance();
                    FreqDist<String> tags = tagger.tag(sarr[1]);
                    if (tags != null)
                        tagDist.merge(tags);
                    list.add(new Tuple2<>(uid, tagDist));
                } else {
                    System.err.println(s);
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
                FreqDist<String> freqDist = new FreqDist<String>();
                for (String key : tp._2().keySet())
                    freqDist.put(key, 1);
                list.add(new StringBuilder(tp._1()).append("\t").append(GsonSerializer.toJson(freqDist)).toString());
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

        System.out.println("ContentTaggerSpark DONE");
    }

    public static class Taggers implements Serializable {
        // ============= SINGLETON ==========

        private static Taggers _singleton = null;

        public static Taggers getInstance() {
            if (_singleton == null)
                synchronized (Taggers.class) {
                    if (_singleton == null) {
                        _singleton = new Taggers();
                    }
                }
            return _singleton;
        }

        protected List<Tagger> taggerList;

        private Taggers() {
            taggerList = Arrays.asList(new Tagger[]{
                    ContentTagger.getInstance()
            });
        }

        public List<Tagger> getTaggerList() {
            return taggerList;
        }

        public void setTaggerList(List<Tagger> taggerList) {
            this.taggerList = taggerList;
        }
    }
}
