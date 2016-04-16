package com.datatub.iresearch.analyz.api;

import com.datatub.iresearch.analyz.ner.classifier.NERClassifier;
import com.datatub.iresearch.analyz.ner.util.NERWord;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.SntMainClassifier;

import java.io.Serializable;
import java.util.List;

/**
 * API for IResearch.
 * @author lhfcws
 * @since 15/11/27.
 */
public class IRAnalyzAPI implements Serializable {
    private SntMainClassifier sntMainClassifier;
    private NERClassifier nerClassifier;

    public IRAnalyzAPI() {
        sntMainClassifier = new SntMainClassifier();
        nerClassifier = new NERClassifier();
    }

    public int classifySentiment(String text) {
        return sntMainClassifier.serialClassify(text);
    }

    public List<NERWord> extractKeywords(String text) {
        return nerClassifier.classify(text);
    }

    /**************************************
     * ************  MAIN  ****************
     * Test main
     */
    public static void main(String[] args) {
        String text = "机械院-星海夜航#【白石山景区】白石山景区位于河北省保定市涞源县境内，总面积为100余平方公里。白石山拥有三顶六台九谷八十一峰，主峰佛光顶海拔2096米，自古被称>    为太行之首。其山势陡峭,奇峰林立，怪石遍布，植被茂密，瀑飞泉流，云蒸霞蔚，佛光频现是中国山岳景观奇与险的杰出代表。";
        IRAnalyzAPI irAnalyzAPI = new IRAnalyzAPI();
        System.out.println(irAnalyzAPI.extractKeywords(text));
        System.out.println(irAnalyzAPI.classifySentiment(text));
    }
}
