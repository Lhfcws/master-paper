package paper.query;

import paper.MLLibConfiguration;
import com.yeezhao.commons.util.AdvCli;
import com.yeezhao.commons.util.CliRunner;
import com.yeezhao.commons.util.config.CommConsts;
import com.yeezhao.sealion.base.YZDoc;
import com.yeezhao.sealion.searcher.DefaultYZIndexSearcher;
import com.yeezhao.sealion.searcher.YZSearchQuery;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.conf.Configuration;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import paper.community.model.WeiboUser;

import java.util.LinkedList;
import java.util.List;

/**
 * @author lhfcws
 * @since 16/1/4.
 */
public class WeiboUserESScanner implements CliRunner {

    public static final String INDEX = "ds-weibo-user-v4";
    public static final String CLI_ID = "id";
    protected DefaultYZIndexSearcher indexSearcher;
    protected Configuration conf;

    // ============= MAIN ===============

    /**************************************
     * AdvCli main.
     */
    public static void main(String[] args) {
        AdvCli.initRunner(args, WeiboUserESScanner.class.getSimpleName(), new WeiboUserESScanner());
    }

    public WeiboUserESScanner() {
        conf = MLLibConfiguration.getInstance();
        String esClusterName = conf.get(CommConsts.ES_CLUSTER_NAME, "amkt_es_cluster");
        indexSearcher = new DefaultYZIndexSearcher(esClusterName, INDEX, "user", conf.get(CommConsts.ES_HOSTS).split(","));
    }

    @Override
    public Options initOptions() {
        Options options = new Options();
        options.addOption(CLI_ID, true, "weibo uid");
        return options;
    }

    @Override
    public boolean validateOptions(CommandLine commandLine) {
        return commandLine.hasOption(CLI_ID);
    }

    @Override
    public void start(CommandLine commandLine) {
        String originID = commandLine.getOptionValue(CLI_ID);

        run(originID);
    }

    public void run(String originID) {
        List<WeiboUser> weiboUsers = getFollowers(originID);
        System.out.println(weiboUsers.size());

//        for (WeiboUser weiboUser : weiboUsers) {
//            int size = getFollowers(originID, weiboUser.id).size();
//            if (size > 0)
//                System.out.println(weiboUser.id + ": " + size);
//        }
    }

    public List<WeiboUser> getFollowers(String... uids) {
        YZSearchQuery originQuery = buildFollowerQuery(uids);
        int count = (int) indexSearcher.getCount(originQuery);
        if (count == 0) {
//            System.err.println("No result with id: " + Arrays.toString(uids));
            return new LinkedList<>();
        }
        int per_page = 2000;
        int total_pages = count / per_page + (count % per_page > 0 ? 1 : 0);
        List<WeiboUser> weiboUsers = new LinkedList<>();
        for (int i = 1; i <= total_pages; i++) {
            List<YZDoc> res = indexSearcher.search(originQuery, i, per_page);
            for (YZDoc yzDoc : res)
                weiboUsers.add(DocWrapper.parseUserYZDoc(yzDoc));
        }
        return weiboUsers;
    }

    public WeiboUser getWeiboUser(String uid) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.should(new TermQueryBuilder("id", uid));
        YZSearchQuery yzSearchQuery = new YZSearchQuery();
        yzSearchQuery.addAddCond(boolQueryBuilder);

        List<YZDoc> res = indexSearcher.search(yzSearchQuery, 1, 10);
        if (CollectionUtils.isEmpty(res)) {
            System.err.println("No result with id: " + uid);
            return null;
        }

        return DocWrapper.parseUserYZDoc(res.get(0));
    }

    public YZSearchQuery buildFollowerQuery(String... uids) {
        YZSearchQuery yzSearchQuery = new YZSearchQuery();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        for (String uid : uids) {
            boolQueryBuilder.must(new TermQueryBuilder("关注", uid));
        }
        boolQueryBuilder.must(new TermsQueryBuilder("认证类型", "普通用户", "微博达人", "黄v"));
        yzSearchQuery.addAddCond(boolQueryBuilder);
//        System.out.println(yzSearchQuery);
        return yzSearchQuery;
    }
}
