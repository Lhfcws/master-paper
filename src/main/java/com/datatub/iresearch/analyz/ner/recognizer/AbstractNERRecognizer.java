package com.datatub.iresearch.analyz.ner.recognizer;

import com.datatub.iresearch.analyz.ner.util.NERWord;
import com.datatub.iresearch.analyz.util.KwFormatUtil;
import com.datatub.iresearch.analyz.util.SegUtil;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.datatub.iresearch.analyz.util.load.DictsLoader;
import com.yeezhao.commons.util.ChineseCC;
import org.ansj.domain.Term;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.List;

/**
 * @author lhfcws
 * @since 15/11/30.
 */
public abstract class AbstractNERRecognizer implements Serializable {
    public static Log LOG = LogFactory.getLog(AbstractNERRecognizer.class);

    protected void init() {
        DictsLoader.loadNERDict("ner/" + getNERType().toLowerCase() + ".common.txt", false);
        DictsLoader.loadNERDict("ner/" + getNERType().toLowerCase() + ".blacklist.txt", false);

        SegUtil.ansjInsertWords(Dicts.nerDict.get("ner/" + getNERType().toLowerCase() + ".common.txt"), "n", 100);
    }

    public static String preClean(String raw) {
        String tweet = ChineseCC.toShort(raw);
        return KwFormatUtil.tweetFilter(tweet);
    }

    protected abstract List<NERWord> _recognize(String cleanedTweet);

    protected abstract List<NERWord> _recognize(List<Term> terms);

    public List<NERWord> recognize(String tweet) {
        tweet = preClean(tweet);
        return _recognize(tweet);
    }

    public List<NERWord> recognize(List<Term> terms) {
        return _recognize(terms);
    }

    protected boolean isInBlacklist(String word) {
        return Dicts.nerDict.get("ner/" + getNERType().toLowerCase() + ".blacklist.txt").contains(word);
    }

    public abstract String getNERType();
}
