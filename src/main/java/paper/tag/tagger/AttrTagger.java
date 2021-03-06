package paper.tag.tagger;

import com.yeezhao.commons.util.*;
import paper.community.model.WeiboUser;
import paper.tag.tagger.mapping.KeywordMapping;
import paper.tag.tagger.mapping.RangeMapping;

import java.io.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class AttrTagger implements Serializable {
    RangeMapping ageRule = new RangeMapping();
    KeywordMapping areaRule = new KeywordMapping();

    // ============= SINGLETON ==========

    private static AttrTagger _singleton = null;

    public static AttrTagger getInstance() {
        if (_singleton == null)
            synchronized (AttrTagger.class) {
                if (_singleton == null) {
                    _singleton = new AttrTagger();
                }
            }
        return _singleton;
    }

    private AttrTagger() {
        try {
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() throws IOException {
        final AtomicInteger mode = new AtomicInteger(0);
        AdvFile.loadFileInDelimitLine(ClassUtil.getResourceAsInputStream("attr_tag_rules.txt"), new ILineParser() {
            @Override
            public void parseLine(String s) {
                if (s.startsWith("@")) {
                    mode.incrementAndGet();
                    return;
                }
                else if (mode.get() == 1) {
                    String[] arr = s.split("\t");
                    try {
                        ageRule.add(arr[0], arr[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (mode.get() == 2) {
                    String[] arr = s.split("\t");
                    List<String> kws = Arrays.asList(arr[1].split("\\|"));
                    areaRule.add(arr[0], kws);
                }
            }
        });
    }

    /**
     * 属性标签抽取函数
     * @param weiboUser
     * @return
     */
    public FreqDist<String> tag(WeiboUser weiboUser) {
        FreqDist<String> tagDistribution = new FreqDist<>();

        // 直接统计
        incTag(tagDistribution, weiboUser.area);
        incTag(tagDistribution, weiboUser.city);
        incList(tagDistribution, weiboUser.school);
        incList(tagDistribution, weiboUser.company);

        // RangeRule 年龄映射
        int year = Calendar.getInstance().get(Calendar.YEAR);
        if (weiboUser.birthYear != null)
            tagDistribution.merge(ageRule.map(year - weiboUser.birthYear));

        // MappingRule 地域映射
        if (weiboUser.area != null) {
            tagDistribution.merge(areaRule.map(weiboUser.area));
        }

        return tagDistribution;
    }

    public FreqDist<String> tag(Collection<WeiboUser> wus) {
        FreqDist<String> dist = new FreqDist<>();
        for (WeiboUser wu : wus)
            dist.merge(tag(wu));
        return dist;
    }

    protected void incTag(FreqDist<String> dist, String tag) {
        if (tag != null)
            dist.inc(tag);
    }

    protected void incList(FreqDist<String> dist, List<String> list) {
        if (!CollectionUtil.isEmpty(list)) {
            for (String s : list)
                dist.inc(s);
        }
    }

    /****************
     * Test main
     */
    public static void main(String[] args) throws Exception {
        InputStream in = new FileInputStream(args[0]);
        final AttrTagger attrTagger = AttrTagger.getInstance();

        AdvFile.loadFileInDelimitLine(in, new ILineParser() {
            @Override
            public void parseLine(String s) {
                WeiboUser w = com.yeezhao.commons.util.serialize.GsonSerializer.deserialize(s, WeiboUser.class);
                FreqDist<String> freq = attrTagger.tag(w);
                System.out.println(w.id + ": " + freq);
            }
        });
    }
}
