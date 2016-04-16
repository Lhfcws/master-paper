package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl;

import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.ISerialSntClassifier;
import com.datatub.iresearch.analyz.util.SegUtil;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.datatub.iresearch.analyz.util.load.DictsLoader;
import com.yeezhao.commons.util.Pair;

import java.util.List;

/**
 * 情感词权值统计分类器,串行分类器实现DEMO.
 * @author lhfcws
 * @since 15/12/2.
 */
public class WordScoreSrSntClassifier implements ISerialSntClassifier {

    /**
     * 构造函数
     */
    public WordScoreSrSntClassifier() {
        init();
    }

    /**
     * 初始化,加载对应词典
     */
    private void init() {
        // 加载情感词权值词典
        DictsLoader.loadWeightDict(Dicts.POS_WD, false);
        DictsLoader.loadWeightDict(Dicts.NEG_WD, false);
        // 加载通用分词词典
        DictsLoader.loadDict(Dicts.DIC_D, false);
    }

    /**
     * 重载分类接口,
     * @param text
     * @return int label (1: positive, 0: neutral, -1: negative, MLLibConsts.UNCLASSIFY: cannot classify, next classifier will take over the job.)
     */
    @Override
    public int classify(String text) {
        // 利用通用词典和情感词典进行最大后向分词
        List<Pair<Integer, Integer>> pairList = SegUtil.backwardMaxMatch(text, 10, 2,
                Dicts.dict.get(Dicts.DIC_D),
                Dicts.weightDict.get(Dicts.POS_WD).keySet(),
                Dicts.weightDict.get(Dicts.NEG_WD).keySet()
        );
        List<String> words = SegUtil.positionPairs2Words(text, pairList);

        // 对分完词的句子开始算分
        double score = 0;
        for (String w : words) {
            score += Dicts.weightDict.get(Dicts.POS_WD).getCount(w);
            score += Dicts.weightDict.get(Dicts.NEG_WD).getCount(w);
        }

        // 如果分数为0,则暂时无法确定其情感偏向,返回 UNCLASSIFY
        if (score == 0)
            return MLLibConsts.UNCLASSIFY;

        // 返回整型的分值,规整化为 1 或 -1
        if (score > 0)
            return 1;
        else
            return -1;
    }
}
