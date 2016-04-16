package paper.query;

import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.yeezhao.commons.util.StringUtil;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.*;
import paper.community.model.WeiboUser;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author lhfcws
 * @since 16/1/7.
 */
public class FollowsScanner implements Serializable {
    private HConnection hConnection = null;
    private HTableInterface hTableInterface = null;

    private static final String TABLE = "yeezhao.user.info";
    private static final byte[] FOLLOW_FAMILY = "follow".getBytes();
    private static final byte[] ANALYZ_FAMILY = "analyz".getBytes();
    private static final byte[] METAGROUP_QUALIFIER = "meta_group".getBytes();


    // ========= singleton ==============
    /**
     * Singleton instance.
     */
    private static FollowsScanner _singleton = null;

    /**
     * Singleton access method.
     */
    public static FollowsScanner getInstance() {
        if (_singleton == null)
            synchronized (FollowsScanner.class) {
                if (_singleton == null) {
                    _singleton = new FollowsScanner();
                }
            }
        return _singleton;
    }

    /**
     * Private constructor.
     */
    private FollowsScanner() {
        try {
            hConnection = HConnectionManager.createConnection(MLLibConfiguration.getInstance());
            hTableInterface = hConnection.getTable(TABLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        hTableInterface.setAutoFlush(false, false);
    }

    public List<WeiboUser> getFollows(String... uids) {
//        Map<String, List<String>> ret = new HashMap<>();
        List<WeiboUser> ret = new LinkedList<>();

        List<Get> gets = new LinkedList<>();
        for (String uid : uids) {
            String rowkey = "sn|" + uid + "|";
            Get get = new Get(rowkey.getBytes());
            get.addFamily(FOLLOW_FAMILY);
            get.addColumn(ANALYZ_FAMILY, METAGROUP_QUALIFIER);
            gets.add(get);
        }

        List<List<Get>> getsList = segList(gets, 500);

        for (List<Get> g : getsList) {
            try {
                Result[] results = hTableInterface.get(g);
                for (Result result : results) {
                    if (result == null || result.getRow() == null) continue;
                    WeiboUser weiboUser = parseResult(result);
                    ret.add(weiboUser);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    protected WeiboUser parseResult(Result result) {
        String rowkey = new String(result.getRow());
        WeiboUser weiboUser = new WeiboUser();
        String id = rowkey.replaceAll(StringUtil.STR_DELIMIT_1ST, "").replaceAll("sn", "");
        List<String> follows = new LinkedList<>();
        Map<byte[], byte[]> mp = result.getFamilyMap(FOLLOW_FAMILY);
        if (mp != null)
            for (Map.Entry<byte[], byte[]> entry : mp.entrySet()) {
                String uid = new String(entry.getKey());
                follows.add(uid);
            }

        List<Cell> cells = result.getColumnCells(ANALYZ_FAMILY, METAGROUP_QUALIFIER);
        if (cells != null)
            for (Cell cell : cells) {
                if (cell != null && cell.getValueArray() != null) {
                    String value = new String(cell.getValueArray());
                    for (String v : value.split(StringUtil.STR_DELIMIT_1ST)) {
                        String w = v.split(StringUtil.STR_DELIMIT_2ND)[0];
                        if (w.length() > 1)
                            weiboUser.tags.inc(w);
                    }
                }
            }

        weiboUser.id = id;
        weiboUser.follows = follows;
        return weiboUser;
    }

    public static <T> List<List<T>> segList(List<T> totalList, int segSize) {
        List<List<T>> retList = new LinkedList<List<T>>();

        int start = 0;
        while (start < totalList.size()) {
            int end = start + segSize;
            if (end > totalList.size()) end = totalList.size();

            List<T> subList = totalList.subList(start, end);
            if (!subList.isEmpty())
                retList.add(subList);
            start += segSize;
        }

        return retList;
    }


    // ============= MAIN ===============

    /**************************************
     * Test main
     */
    public static void main(String[] args) {
        FollowsScanner followsScanner = FollowsScanner.getInstance();
        System.out.println(followsScanner.getFollows("1740248463"));
    }
}
