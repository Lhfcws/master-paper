package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.features;

import com.datatub.iresearch.analyz.base.IFeature;
import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.util.load.Dicts;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lhfcws
 * @since 15/11/13.
 */
public class SentiDictFeature implements IFeature {
    private static SentiDictFeature _iFeature = null;

    public static SentiDictFeature getInstance() {
        if (_iFeature == null)
            synchronized (SentiDictFeature.class) {
                if (_iFeature == null) {
                    _iFeature = new SentiDictFeature();
                }
            }
        return _iFeature;
    }

    public SentiDictFeature() {
        Collections.addAll(prefixNeg, prefixNegArr);
    }

    private static final String[] prefixNegArr = {"不是", "毫无", "没有", "绝非", "不用", "毫不"};
    private static final Set<String> prefixNeg = new HashSet<String>();
    private static final Pattern catchWhole = Pattern
            .compile("(滴|哦|丫|吖|哇|噢|咯|呗|呐|亲|呀|~)$");

    @Override
    public double extractFeature(Object raw) {

       List<String> words = (List<String>) raw;

        Matcher m;
        int score = 0;

        int scalar = 1;
        for (String word : words) {
            if (Dicts.dict.get(Dicts.ROUGH_D).contains(word)) {
                score -= 2;
                scalar = 1;
            } else if (Dicts.dict.get(Dicts.NEG_D).contains(word)) {
                score += scalar * -1;
                scalar = -scalar;
            } else if (Dicts.dict.get(Dicts.POS_D).contains(word)) {
                score += scalar;
            } else {
                scalar = 1;
            }
            if (prefixNeg.contains(word))
                scalar = -1;
        }
        if (!words.isEmpty()) {
            String lastWord = words.get(words.size() - 1);
            m = catchWhole.matcher(lastWord);
            if (m.find())
                score += 2;
        }

        return 1.0 * score / MLLibConsts.MAX_SNT_TWT_WORDS;
    }

    @Override
    public double[] extractFeatures(Object raw) {
        return new double[0];
    }
}
