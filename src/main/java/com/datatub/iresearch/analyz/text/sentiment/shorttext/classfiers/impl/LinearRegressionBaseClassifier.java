package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl;

import com.datatub.iresearch.analyz.base.ITrainer;
import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.ISntClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.assist.LinearRegressionPredictor;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.utils.SntAccurateModel;
import com.datatub.iresearch.analyz.util.AccDist;
import com.datatub.iresearch.analyz.util.AccObject;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.datatub.iresearch.analyz.util.load.DictsLoader;
import com.yeezhao.commons.util.ILineParser;
import com.yeezhao.commons.util.Pair;
import de.bwaldvogel.liblinear.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Linear base classifier
 *
 * @author lhfcws
 * @since 15/11/19.
 */
public class LinearRegressionBaseClassifier implements ISntClassifier, ITrainer {
    protected static Log LOG = LogFactory.getLog(LinearRegressionBaseClassifier.class);
    protected static Configuration conf = MLLibConfiguration.getInstance();
    protected LinearRegressionPredictor linearRegressionPredictor = LinearRegressionPredictor.getInstance();
    protected SntAccurateModel sntAccurateModel = new SntAccurateModel(getName());
    protected Model model = null;

    public LinearRegressionBaseClassifier() {
        init();
    }

    @Override
    public ISntClassifier init() {
        DictsLoader.loadDict(Dicts.DIC_D, false);
        DictsLoader.loadDict(Dicts.POS_D, false);
        DictsLoader.loadDict(Dicts.NEG_D, false);
        DictsLoader.loadDict(Dicts.ROUGH_D, false);
        DictsLoader.loadSntAccModels(false);
        if (Dicts.sntAccurateModelMap.get(getName()) != null)
            update(Dicts.sntAccurateModelMap.get(getName()));
        return this;
    }

    @Override
    public ISntClassifier update(SntAccurateModel sntAccurateModel) {
        this.sntAccurateModel = new SntAccurateModel(sntAccurateModel, getName());
        return this;
    }

    @Override
    public AccObject getTotalAccurate() {
        return sntAccurateModel.getTotalAcc();
    }

    @Override
    public AccDist<Integer> getAccurateDist() {
        return sntAccurateModel.getProbs();
    }

    @Override
    public SntAccurateModel getSntAccurateModel() {
        return sntAccurateModel;
    }

    @Override
    public String getName() {
        return LinearRegressionBaseClassifier.class.getSimpleName();
    }

    @Override
    public int classify(String text) {
        if (getModel() == null)
            return MLLibConsts.UNCLASSIFY;
        else
            return score2label(linearRegressionPredictor.predict(text, getModel()));
    }

    public Model getModel() {
        if (model == null)
            synchronized (Model.class) {
                if (model == null)
                    try {
                        model = Model.load(new InputStreamReader(
                                FileSystemUtil.getHDFSFileInputStream(
                                        conf.get(MLLibConsts.FILE_SNT_LR_MODEL_BASE)
                                )
                        ));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        return model;
    }

    @Override
    public Object train(String inputFile, String outputModel) throws Exception {
        Problem problem = genLRProblem(new FileInputStream(inputFile));
        Parameter parameter = genLRParameter();
        Model model = Linear.train(problem, parameter);
        model.save(new OutputStreamWriter(FileSystemUtil.getHDFSFileOutputStream(
                outputModel
        )));
        return lrTest(new FileInputStream(inputFile), model, false);
    }

    @Override
    public Object retrain(String inputText, String outputModel) throws Exception {
        final SntAccurateModel sntAccurateModel = new SntAccurateModel(getName());
        sntAccurateModel.getProbs().clear();
        sntAccurateModel.getTotalAcc().setTotal(0);
        sntAccurateModel.getTotalAcc().setCorrect(0);
        System.out.println("Retrain " + inputText);
        FileSystemUtil.loadFileInLines(new FileInputStream(inputText), new ILineParser() {
            @Override
            public void parseLine(String s) {
                Pair<String, Integer> pair = split2TextNLabel(s);
                String text = pair.first;
                int label = pair.second;

                double p = linearRegressionPredictor.predict(text, getModel());
                int pl = score2label(p);
                sntAccurateModel.getProbs().setDefault(pl);
                if (pl == label) {
                    sntAccurateModel.getTotalAcc().correct++;
                    sntAccurateModel.getProbs().get(pl).correct++;
                }
                sntAccurateModel.getTotalAcc().total++;
                sntAccurateModel.getProbs().get(pl).total++;
            }
        });

        sntAccurateModel.calc();
        return new SntAccurateModel(sntAccurateModel);
    }

    @Override
    public Object test(String inputModel, String inputTestFile) throws Exception {
        InputStream is = FileSystemUtil.getHDFSFileInputStream(inputModel);
        InputStreamReader reader = new InputStreamReader(is);

        Model _model = Model.load(reader);
        return lrTest(new FileInputStream(inputTestFile), _model, true);
    }

    /**
     * Test given dataset.
     *
     * @param inputText
     * @param model_
     * @return
     * @throws IOException
     */
    public SntAccurateModel lrTest(InputStream inputText, Model model_, boolean verbose) throws IOException {
        SntAccurateModel sntAccurateModel = new SntAccurateModel(getName());
        sntAccurateModel.getProbs().clear();
        sntAccurateModel.getTotalAcc().setTotal(0);
        sntAccurateModel.getTotalAcc().setCorrect(0);

        InputStreamReader isr = new InputStreamReader(inputText);
        BufferedReader br = new BufferedReader(isr);

//        List<String> list = new LinkedList<String>();
        String line = null;
        while ((line = br.readLine()) != null) {
            if( line.trim().isEmpty() ) continue;
            try {
                String s = line.trim();
                Pair<String, Integer> labels = split2TextNLabel(s);
                String text = labels.getFirst();
                int label = labels.getSecond();

                double predictionScore = linearRegressionPredictor.predict(text, model_);
                int prediction = score2label(predictionScore);

                sntAccurateModel.getTotalAcc().total++;
                sntAccurateModel.getProbs().setDefault(prediction);
                sntAccurateModel.getProbs().get(prediction).total++;

                if (prediction == label) {
                    sntAccurateModel.getTotalAcc().correct++;
                    sntAccurateModel.getProbs().get(prediction).correct++;
                } else if (verbose) {
                    System.out.println(prediction + "\t" + label + "\t" + text);
                }
            } catch (Exception e) {
                System.err.println("[Test ERROR] " + line);
            }
        }

        br.close();
        isr.close();
        inputText.close();

        sntAccurateModel.calc();
//        return new SntAccurateModel(sntAccurateModel);
        return sntAccurateModel;
    }

    /**
     * Translate double score into int label.
     *
     * @param score
     * @return
     */
    public int score2label(double score) {
        if (score > 0)
            return MLLibConsts.POS;
        else if (score < 0)
            return MLLibConsts.NEG;
        else
            return MLLibConsts.NEUT;
    }

    public Pair<String, Integer> split2TextNLabel(String s) {
        try {
            s = s.toLowerCase().trim();
            String[] sarr = s.split("\t");
            return new Pair<String, Integer>(sarr[0], transformLabel(Integer.valueOf(sarr[1])));
        } catch (Exception ignore) {
            return new Pair<String, Integer>("", 1);
        }
    }

    /**
     * Generate Problem object for train.
     *
     * @param inputStream inputTrainFile
     * @return
     * @see <a href="http://www.ziliao1.com/Article/Show/3FD27F1E6C6F481E0524F81ECB0CFAAA.html">DEMO</a>
     */
    public Problem genLRProblem(InputStream inputStream) throws IOException {
        Problem problem = new Problem();
        final List<Feature[]> samples = new LinkedList<Feature[]>();
        final List<Double> labelValues = new ArrayList<Double>();

        FileSystemUtil.loadFileInLines(inputStream, new ILineParser() {
            @Override
            public void parseLine(String s) {
                Pair<String, Integer> labels = split2TextNLabel(s);
                String text = labels.getFirst();
                int label = labels.getSecond();

                Feature[] features = linearRegressionPredictor.genTweetFeatureVector(text);
                samples.add(features);
                labelValues.add(1.0 * label);
            }
        });

        Feature[][] features = new Feature[samples.size()][];
        samples.toArray(features);
        double[] targetLabels = new double[labelValues.size()];
        for (int i = 0; i < targetLabels.length; i++)
            targetLabels[i] = labelValues.get(i);

        problem.l = samples.size();
        problem.n = linearRegressionPredictor.getFeatureSize();
        problem.x = features;
        problem.y = targetLabels;
        return problem;
    }

    /**
     * Generate Parameter object for train.
     *
     * @return
     * @see <a href="http://www.ziliao1.com/Article/Show/3FD27F1E6C6F481E0524F81ECB0CFAAA.html">DEMO</a>
     */
    public Parameter genLRParameter() {
        return new Parameter(SolverType.L2R_LR, 1, 0.01);
    }

    public int transformLabel(int label) {
        if (label >= 0) return 1;
        else return -1;
    }
}
