package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers;

import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl.LinearRegressionClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl.LinearRegressionNeutClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.utils.SntAccurateModel;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.utils.SntClassfierLoader;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import com.yeezhao.commons.util.ILineParser;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.List;

/**
 * @author lhfcws
 * @since 15/11/13.
 */
public class SntMainClassifier implements Serializable {
//    public Map<String, SntILClassifier> sntILClassifiers;
    public List<ISerialSntClassifier> iSerialSntClassifierList;
    public LinearRegressionClassifier linearRegressionClassifier;
    public LinearRegressionNeutClassifier linearRegressionNeutClassifier;

    public SntMainClassifier() {
        try {
//            sntILClassifiers = SntClassfierLoader.loadSntILClassifiers(false);
            iSerialSntClassifierList = SntClassfierLoader.loadSerialSntClassifiers(false);
            linearRegressionClassifier = new LinearRegressionClassifier();
            linearRegressionNeutClassifier = new LinearRegressionNeutClassifier();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int serialClassify(String text) {
        int value = MLLibConsts.UNCLASSIFY;
        if (iSerialSntClassifierList != null) {
            for (ISerialSntClassifier iSerialSntClassifier : iSerialSntClassifierList) {
                value = iSerialSntClassifier.classify(text);
                if (!MLLibConsts.isUnclassify(value))
                    break;
            }
        }
        int serialValue = value;
        if (MLLibConsts.isUnclassify(value))
            value = linearRegressionClassifier.classify(text);
        if (value < 0)
            return value;
        if (value > 0)
        	value = linearRegressionNeutClassifier.classify(text);
        if (value < 0) value = 0;
        if (value == 0 && serialValue > 0) value = 1;
        return value;
    }

//    public int classifyNVote(String text) {
//        return classifyNVote(sntILClassifiers.values(), text);
//    }
//
//    /**
//     * Calculate expectation of each class, choose max.
//     *
//     * @param sntClassifiers
//     * @return
//     */
//    public int classifyNVote(Collection<? extends ISntClassifier> sntClassifiers, String text) {
//        // classify
//        DoubleDist<Integer> scores = new DoubleDist<Integer>();
//        FreqDist<Integer> counter = new FreqDist<Integer>();
//        for (ISntClassifier sntClassifier : sntClassifiers) {
//            int label = sntClassifier.classify(text);
//            Double d = sntClassifier.getAccurateDist().safeGet(label).getAccRate();
//            scores.inc(label, d);
//            counter.inc(label);
//        }
//
//        // use score to vote, find max score
//        double mx = -210000000;
//        int label = -10;
//        for (Map.Entry<Integer, Double> entry : scores.entrySet()) {
//            // average
//            double score = 1.0 * entry.getValue() / counter.get(entry.getKey());
//            if (mx < score) {
//                label = entry.getKey();
//                mx = score;
//            }
//        }
//        return label;
//    }


    // ============= MAIN ===============

    /**************************************
     * Test main
     */
    public static void main(String[] args) throws Exception {
        final SntMainClassifier sntMainClassifier = new SntMainClassifier();
        final SntAccurateModel sntAccurateModel = new SntAccurateModel("Test sntClassifier accuracy.");
        FileSystemUtil.loadFileInLines(new FileInputStream(args[0]), new ILineParser() {
            @Override
            public void parseLine(String s) {
                String[] sarr = s.split("\t");
                int label = Integer.valueOf(sarr[sarr.length - 1]);
                String text = sarr[0];

                int prediction = sntMainClassifier.serialClassify(text);
                sntAccurateModel.getTotalAcc().total++;
                sntAccurateModel.getProbs().setDefault(prediction);
                sntAccurateModel.getProbs().get(prediction).total++;

                if (prediction == label) {
                    sntAccurateModel.getTotalAcc().correct++;
                    sntAccurateModel.getProbs().get(prediction).correct++;
                }
            }
        });
        sntAccurateModel.calc();
        System.out.println(sntAccurateModel);
    }
}
