package paper.query;

import paper.MLLibConfiguration;
import com.datatub.iresearch.analyz.util.SparkUtil;
import com.yeezhao.commons.util.FileSystemHelper;
import com.yeezhao.commons.util.GsonSerializer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import paper.community.model.WeiboUser;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author lhfcws
 * @since 16/1/4.
 */
public class WeiboUserScanSpark implements Serializable {
    private Configuration conf = MLLibConfiguration.getInstance();

    public void run(final String uid, String outputUsers, String outputRel) {
        Map<String, String> params = new HashMap<>();
        params.put("spark.executor.memory", "2g");

        WeiboUserESScanner scanner = new WeiboUserESScanner();
        List<WeiboUser> followers = scanner.getFollowers(uid);
        System.out.println("[COMMUNITY] Get followers of " + uid + ", size: " + followers.size());
        final Set<String> originFollowers = new HashSet<>();
        for (WeiboUser weiboUser: followers)
            originFollowers.add(weiboUser.id);

        FileSystemHelper fs = FileSystemHelper.getInstance(conf);
        try {
            fs.deleteFile(outputRel);
            fs.deleteFile(outputRel + ".txt");
            fs.deleteFile(outputUsers);
            fs.deleteFile(outputUsers + ".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Begin to run spark1.");
        SparkConf sparkConf = SparkUtil.createSparkConf("Comm-WeiboUserScan", 40, this.getClass(), params);
        JavaSparkContext jsc = new JavaSparkContext(sparkConf);
        jsc.parallelize(followers).repartition(20).map(new Function<WeiboUser, String>() {
            @Override
            public String call(WeiboUser weiboUser) throws Exception {
//                WeiboUserESScanner scanner = new WeiboUserESScanner();
//                List<WeiboUser> list = scanner.getFollowers(uid, weiboUser.id);
//                for (WeiboUser w : list)
//                        weiboUser.followers.add(w.id);
                FollowsScanner followsScanner = FollowsScanner.getInstance();
                List<WeiboUser> weiboUsers = followsScanner.getFollows(weiboUser.id);
                if (!CollectionUtils.isEmpty(weiboUsers)) {
                    WeiboUser w = weiboUsers.get(0);
                    Set<String> set = new HashSet<>(w.follows);
                    set = intersect(set, originFollowers);
                    weiboUser.tags = w.tags;
                    weiboUser.follows = new LinkedList<>(set);
                }
                return GsonSerializer.toJson(weiboUser);
            }
        }).saveAsTextFile(outputUsers);
        jsc.stop();

        System.out.println("Begin to run spark2.");
        sparkConf = SparkUtil.createSparkConf("Comm-BuildRelations", 40, this.getClass(), params);
        jsc = new JavaSparkContext(sparkConf);
        jsc.textFile(outputUsers).map(new Function<String, String>() {
            @Override
            public String call(String s) throws Exception {
                WeiboUser v1 = GsonSerializer.fromJson(s, WeiboUser.class);
                StringBuilder sb = new StringBuilder();
                sb.append(v1.id).append("\t");
                for (String fid : v1.follows) {
                    sb.append(fid).append("|");
                }
                if (sb.lastIndexOf("|") > 0)
                    sb.setLength(sb.length() - 1);

                return sb.toString();
            }
        }).saveAsTextFile(outputRel);
        jsc.stop();

        System.out.println("Spark finished. Merging file ...");

        try {
            fs.mergeDirsToFile(outputRel + ".txt", outputRel);
            System.out.println("Merged " + outputRel);
            fs.mergeDirsToFile(outputUsers + ".txt", outputUsers);
            System.out.println("Merged " + outputUsers);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("DONE");
    }

    public static <T> Set<T> intersect(Set<T> set1, Set<T> set2) {
        Set<T> ret = new HashSet<>();
        for (T t : set1)
            if (set2.contains(t))
                ret.add(t);
        return ret;
    }


    // ============= MAIN ===============

    /**************************************
     * Test main
     */
    public static void main(String[] args) {
        String mideaUID = "1740248463";
        WeiboUserScanSpark weiboUserScanSpark = new WeiboUserScanSpark();
        weiboUserScanSpark.run(mideaUID, "/tmp/midea_users", "/tmp/midea_relations");
    }
}
