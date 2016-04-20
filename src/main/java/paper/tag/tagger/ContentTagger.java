package paper.tag.tagger;

import com.yeezhao.commons.util.*;
import paper.MLLibConfiguration;
import com.datatub.iresearch.analyz.util.KwFormatUtil;
import com.datatub.iresearch.analyz.util.SegUtil;
import org.ansj.library.UserDefineLibrary;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class ContentTagger implements Tagger {
    // {tag : {keyword : weight}}
    private AdvHashMap<String, FreqDist<String>> tagRules = new AdvHashMap<>();


    // ============= SINGLETON ==========

    private static ContentTagger _singleton = null;

    public static ContentTagger getInstance() {
        if (_singleton == null)
            synchronized (ContentTagger.class) {
                if (_singleton == null) {
                    _singleton = new ContentTagger();
                }
            }
        return _singleton;
    }

    private ContentTagger() {
        try {
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load() throws IOException {
        String tagRuleFile = "content_tag_rules.txt";

        AdvFile.loadFileInDelimitLine(ClassUtil.getResourceAsInputStream(tagRuleFile), new ILineParser() {
            @Override
            public void parseLine(String s) {
                String[] arr = KwFormatUtil.simpleFormat(s).split("\t");
                if (arr.length == 3) {
                    tagRules.setDefault(arr[0], new FreqDist<String>());
                    tagRules.get(arr[0]).put(arr[1], Integer.valueOf(arr[2]));
                    SegUtil.ansjInsertWord(arr[1]);
                }
            }
        });
    }

    @Override
    public FreqDist<String> tag(String text) {
        text = KwFormatUtil.simpleFormat(text);

        FreqDist<String> ret = new FreqDist<>();
        AdvHashMap<String, FreqDist<Integer>> rf = new AdvHashMap<>();

        for (String word : SegUtil.ansjNlpSeg(text)) {
            for (Map.Entry<String, FreqDist<String>> entry : tagRules.entrySet()) {
                if (entry.getValue().containsKey(word)) {
                    rf.setDefault(entry.getKey(), new FreqDist<Integer>());
                    rf.get(entry.getKey()).inc(entry.getValue().get(word));
                }
            }
        }

        for (Map.Entry<String, FreqDist<Integer>> entry : rf.entrySet()) {
            for (Map.Entry<Integer, Integer> rfEntry : entry.getValue().entrySet()) {
                if (rfEntry.getKey() <= rfEntry.getValue()) {
                    ret.inc(entry.getKey());
                }
            }
        }

        return ret;
    }

    @Override
    public FreqDist<String> tag(Collection<String> texts) {
        FreqDist<String> ret = new FreqDist<>();
        for (String text : texts)
            ret.merge(tag(text));
        return ret;
    }
}
