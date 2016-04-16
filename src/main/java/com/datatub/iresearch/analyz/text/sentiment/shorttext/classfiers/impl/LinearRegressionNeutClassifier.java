package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl;

import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.ISntClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.tools.LinearRegressionTrainerCli;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.utils.SntAccurateModel;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import com.yeezhao.commons.util.ILineParser;
import com.yeezhao.commons.util.Pair;

import java.io.FileInputStream;

/**
 * @author lhfcws
 * @since 15/12/19.
 */
public class LinearRegressionNeutClassifier extends LinearRegressionClassifier {

    public LinearRegressionNeutClassifier() {
        super(new LinearRegressionNeutBaseClassifier(), new LinearRegressionNeutIncrClassifier());
        init();
    }

    public LinearRegressionNeutClassifier(ISntClassifier iSntClassifier, ISntClassifier iSntClassifier2) {
        super(iSntClassifier, iSntClassifier2);
    }

    @Override
    public String getName() {
        return LinearRegressionNeutClassifier.class.getSimpleName();
    }


    // ============= MAIN ===============

    /**************************************
     * Test main
     */
    public static void main(String[] args) throws Exception {
        Pair<String, String> pair = LinearRegressionTrainerCli.splitFile(args[0]);
        final LinearRegressionClassifier linearRegressionClassifier = new LinearRegressionClassifier();
        final LinearRegressionNeutClassifier linearRegressionNeutClassifier = new LinearRegressionNeutClassifier();

        final SntAccurateModel sntAccurateModel = new SntAccurateModel("LinearRegressionNeutClassifier accuracy.");
        FileSystemUtil.loadFileInLines(new FileInputStream(pair.second), new ILineParser() {
            @Override
            public void parseLine(String s) {
                String[] sarr = s.split("\t");
                int label = Integer.valueOf(sarr[sarr.length - 1]);
                if (label < 0) label = 0;
                String text = sarr[0];

                int prediction = linearRegressionClassifier.classify(text);

                if (prediction > 0) {
                    prediction = linearRegressionNeutClassifier.classify(text);
                    if (prediction < 0) prediction = 0;
                    sntAccurateModel.getTotalAcc().total++;
                    sntAccurateModel.getProbs().setDefault(prediction);
                    sntAccurateModel.getProbs().get(prediction).total++;

                    if (prediction == label) {
                        sntAccurateModel.getTotalAcc().correct++;
                        sntAccurateModel.getProbs().get(prediction).correct++;
                    }
                } else {
                    sntAccurateModel.getProbs().setDefault(-1);
                    sntAccurateModel.getProbs().get(-1).total++;
                }
            }
        });
        sntAccurateModel.calc();
        System.out.println(sntAccurateModel);
    }
}
