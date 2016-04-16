package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl;

import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.ISntClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.ASntILClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.tools.LinearRegressionTrainerCli;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.utils.SntAccurateModel;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.yeezhao.commons.util.ILineParser;
import com.yeezhao.commons.util.Pair;

import java.io.FileInputStream;

/**
 * @author lhfcws
 * @since 15/11/19.
 */
public class LinearRegressionClassifier extends ASntILClassifier {
    public static final int OLD_CORPUS_SIZE = MLLibConsts.DEFAULT_CORPUS_SIZE;

    public LinearRegressionClassifier() {
        super(new LinearRegressionBaseClassifier(), new LinearRegressionIncrClassifier());
        init();
    }

    public LinearRegressionClassifier(ISntClassifier iSntClassifier, ISntClassifier iSntClassifier2) {
        super(iSntClassifier, iSntClassifier2);
    }

    @Override
    public String getName() {
        return LinearRegressionClassifier.class.getSimpleName();
    }

    @Override
    public SntAccurateModel retrain(String inputText) {
        try {
            return _retrain(inputText);
        } catch (Exception e) {
            e.printStackTrace();
            return mergeAccModel();
        }
    }

    protected SntAccurateModel _retrain(String inputTextFile) throws Exception {
        LinearRegressionIncrClassifier incrClassifier = (LinearRegressionIncrClassifier) this.second;
        LinearRegressionBaseClassifier baseClassifier = (LinearRegressionBaseClassifier) this.first;
        // 1. init model and corpus
        String corpusModelPath = incrClassifier.getCorpusPath();
        int incrDataSize = 0;
        try {
            incrDataSize = FileSystemUtil.countLines(corpusModelPath);
        } catch (Exception ignore) {
        }

        int incrTrainSize = FileSystemUtil.countLinesLocal(inputTextFile);
        // 2. Use sigmoid to calculate the weight
        double newWeight = this.calcIncrClassifierWeight(incrDataSize + incrTrainSize, OLD_CORPUS_SIZE);
        SntAccurateModel baseModel = new SntAccurateModel(baseClassifier.getSntAccurateModel(), 1 - newWeight);

        // 3. Train new model with (incrCorpus + inputTextFile)
        //// init file paths
        String inputText = FileSystemUtil.mergeHDFSWithLocalFile(corpusModelPath, inputTextFile);
        String outputTmpModel = FileSystemUtil.getTimestampFile("/tmp/mllib-" + getName() + "-model-");
        //// retrain
        SntAccurateModel incrAccModel = (SntAccurateModel) incrClassifier.retrain(inputText, outputTmpModel);
        incrAccModel = new SntAccurateModel(incrAccModel, incrClassifier.getName(), newWeight);
        //// cleanup
        FileSystemUtil.deleteFile(incrClassifier.getModelPath());
        FileSystemUtil.renameFile(outputTmpModel, incrClassifier.getModelPath());
        FileSystemUtil.forceUploadFile2HDFS(inputText, incrClassifier.getCorpusPath());
        FileSystemUtil.deleteLocalFile(inputText);

        // 4. Update sntAccurateModels
        incrClassifier.update(incrAccModel);
        baseClassifier.update(baseModel);
        Dicts.sntAccurateModelMap.put(incrClassifier.getName(), incrClassifier.getSntAccurateModel());
        Dicts.sntAccurateModelMap.put(baseClassifier.getName(), baseModel);

        return mergeAccModel();
    }

    // ============= MAIN ===============

    /**************************************
     * Test main
     */
    public static void main(String[] args) throws Exception {
        Pair<String, String> pair = LinearRegressionTrainerCli.splitFile(args[0]);
        final LinearRegressionClassifier linearRegressionClassifier = new LinearRegressionClassifier();

        final SntAccurateModel sntAccurateModel = new SntAccurateModel("LinearRegressionClassifier accuracy.");
        FileSystemUtil.loadFileInLines(new FileInputStream(pair.first), new ILineParser() {
            @Override
            public void parseLine(String s) {
                String[] sarr = s.split("\t");
                int label = Integer.valueOf(sarr[sarr.length - 1]);
                String text = sarr[0];

                int prediction = linearRegressionClassifier.classify(text);
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
