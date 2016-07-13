package paper.tag.tagger;

import com.datatub.iresearch.analyz.util.KwFormatUtil;
import com.datatub.iresearch.analyz.util.SegUtil;
import com.yeezhao.commons.util.*;
import com.yeezhao.commons.util.Entity.Document;
import com.yeezhao.hornbill.analyz.algo.text.keyword.KeywordExtractionClassifier;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class SimpleContentTagger implements Tagger {
    // {tag : {keyword : weight}}
    private AdvHashMap<String, FreqDist<String>> tagRules = new AdvHashMap<>();


    // ============= SINGLETON ==========

    private static SimpleContentTagger _singleton = null;

    public static SimpleContentTagger getInstance() {
        if (_singleton == null)
            synchronized (SimpleContentTagger.class) {
                if (_singleton == null) {
                    _singleton = new SimpleContentTagger();
                }
            }
        return _singleton;
    }

    KeywordExtractionClassifier keywordExtractor;

    private SimpleContentTagger() {
        keywordExtractor = new KeywordExtractionClassifier();
    }

    @Override
    public void load() throws IOException {

    }

    @Override
    public FreqDist<String> tag(String text) {
        FreqDist<String> ret = new FreqDist<>();
        text = KwFormatUtil.simpleFormat(text);
        String resStr = keywordExtractor.extract(text, 20);

        if (!StringUtil.isNullOrEmpty(resStr)) {
            String[] arr0 = resStr.split("#");
            for (String entry : arr0) {
                String[] arr1 = entry.split(":");
                if (arr1.length == 2) {
                    if (arr1[1].equals("1"))
                        continue;
                    try {
                        ret.inc(arr1[0], Integer.valueOf(arr1[1]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
