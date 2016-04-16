package com.datatub.iresearch.analyz.text.sentiment.shorttext.tools;

import com.datatub.iresearch.analyz.base.ITrainer;
import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.ISntClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl.LinearRegressionBaseClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl.LinearRegressionIncrClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl.LinearRegressionNeutBaseClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl.LinearRegressionNeutIncrClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.utils.SntAccurateModel;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.datatub.iresearch.analyz.util.load.DictsLoader;
import com.yeezhao.commons.util.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lhfcws
 * @since 15/11/24.
 */
public class LinearRegressionTrainerCli implements CliRunner {
    private static Configuration conf = MLLibConfiguration.getInstance();
    public static final String CLI_PARAM_TRAIN = "train";
    public static final String CLI_PARAM_SNTTRAIN = "snt";
    public static final String CLI_PARAM_MODEL = "model";
    public static final String CLI_PARAM_TEST = "test";
    public static final String CLI_PARAM_INCR = "incr";

    public static void main(String[] args) {
        AdvCli.initRunner(args, LinearRegressionTrainerCli.class.getSimpleName(), new LinearRegressionTrainerCli());
    }

    @Override
    public Options initOptions() {
        Options options = new Options();
        options.addOption(CLI_PARAM_SNTTRAIN, false, "train snt acc model by default model");
        options.addOption(CLI_PARAM_TRAIN, false, "train new lr model and snt model");
        options.addOption(CLI_PARAM_TEST, false, "test with base model");
        options.addOption(CLI_PARAM_MODEL, true, "train model output");
        options.addOption(CLI_PARAM_INCR, false, "user incr classifier.");
        options.addOption(AdvCli.CLI_PARAM_I, true, "input text file with [text]\\t[label] format");
        return options;
    }

    @Override
    public boolean validateOptions(CommandLine commandLine) {
        return commandLine.hasOption(AdvCli.CLI_PARAM_I) &&
                (commandLine.hasOption(CLI_PARAM_SNTTRAIN) || commandLine.hasOption(CLI_PARAM_TRAIN) || commandLine.hasOption(CLI_PARAM_TEST));
    }

    @Override
    public void start(CommandLine commandLine) {
        System.out.println("[SNT] Init LR.");
        ITrainer trainer, trainer1;

        DictsLoader.loadSntAccModels(false);
        if (commandLine.hasOption(CLI_PARAM_INCR)) {
            trainer = new LinearRegressionIncrClassifier();
            trainer1 = new LinearRegressionNeutIncrClassifier();
        } else {
            trainer = new LinearRegressionBaseClassifier();
            trainer1 = new LinearRegressionNeutBaseClassifier();
        }

        ISntClassifier classifier = (ISntClassifier) trainer;
        ISntClassifier classifier1 = (ISntClassifier) trainer1;

        if (Dicts.sntAccurateModelMap.containsKey(classifier.getName()))
            classifier.update(Dicts.sntAccurateModelMap.get(classifier.getName()));
        if (Dicts.sntAccurateModelMap.containsKey(classifier1.getName()))
            classifier1.update(Dicts.sntAccurateModelMap.get(classifier1.getName()));

        System.out.println("[SNT] Init LinearRegressionBaseClassifier.");
        if (commandLine.hasOption(CLI_PARAM_TEST)) {
            String modelPath = commandLine.getOptionValue(CLI_PARAM_MODEL);
            if (modelPath == null) {
                if (commandLine.hasOption(CLI_PARAM_INCR))
                    modelPath = conf.get(MLLibConsts.FILE_SNT_LR_MODEL_INCR);
                else
                    modelPath = conf.get(MLLibConsts.FILE_SNT_LR_MODEL_BASE);
            }
            try {
                Pair<String, String> pair = splitFile(commandLine.getOptionValue(AdvCli.CLI_PARAM_I));
                SntAccurateModel accurateModel = (SntAccurateModel) trainer.test(modelPath, pair.first);
                SntAccurateModel accurateModel1 = (SntAccurateModel) trainer1.test(modelPath, pair.second);
                accurateModel = new SntAccurateModel(accurateModel, classifier.getSntAccurateModel().getWeight());
                accurateModel1 = new SntAccurateModel(accurateModel1, classifier1.getSntAccurateModel().getWeight());
                System.out.println("[SNT] [RESULT] NEG-POS " + accurateModel.toString());
                System.out.println("[SNT] [RESULT] NEUT-POS " + accurateModel1.toString());
                FileSystemHelper fs = FileSystemHelper.getInstance(conf);
                fs.deleteLocalFile(pair.first);
                fs.deleteLocalFile(pair.second);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("[SNT] Test LR done.");
            return;
        }

        try {
            System.out.println("[SNT] Train LR.");
            SntAccurateModel model = null, model1 = null;
            Pair<String, String> pair = splitFile(commandLine.getOptionValue(AdvCli.CLI_PARAM_I));

            if (commandLine.hasOption(CLI_PARAM_SNTTRAIN)) {
                model = (SntAccurateModel) trainer.retrain(pair.first, null);
                model1 = (SntAccurateModel) trainer1.retrain(pair.second, null);
            } else if (commandLine.hasOption(CLI_PARAM_TRAIN)) {
                String modelDirectory = commandLine.getOptionValue(CLI_PARAM_MODEL);
                if (modelDirectory == null)
                    modelDirectory = conf.get(MLLibConsts.PATH_SNT_MODEL_ROOT) + "/snt/";
                model = (SntAccurateModel) trainer.train(
                        pair.first,
                        modelDirectory + MLLibConsts.FILE_SNT_LR_MODEL_BASE
                );
                model = new SntAccurateModel(model, classifier.getName());
                model1 = (SntAccurateModel) trainer1.train(
                        pair.second,
                        modelDirectory + MLLibConsts.FILE_SNT_LR_MODEL1_BASE
                );
                model1 = new SntAccurateModel(model1, classifier1.getName());
            }
            SntAccurateModel.updateAccModel(model, model1);
            System.out.println("[SNT] Train LR done.");
            FileSystemHelper fs = FileSystemHelper.getInstance(conf);
            fs.deleteLocalFile(pair.first);
            fs.deleteLocalFile(pair.second);
            System.out.println(" Model :" + model);
            System.out.println(" Model1 :" + model1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Pair<String, String> splitFile(String inputFile) throws Exception {
        final List<String> negPosList = new LinkedList<String>();
        final List<String> neutPosList = new LinkedList<String>();
        FileSystemUtil.loadFileInLines(new FileInputStream(inputFile), new ILineParser() {
            @Override
            public void parseLine(String s) {
                String sarr[] = s.split("\t");
                String text = StringUtils.join(ArrayUtils.subarray(sarr, 0, sarr.length - 1), " ");
                s = text + "\t" + sarr[sarr.length - 1];

                if (s.endsWith("-1"))
                    negPosList.add(s.toLowerCase());
                else if (s.endsWith("0")) {
                    String r = s.replaceAll("0$", "-1");
                    neutPosList.add(r.toLowerCase());
                    r = s.replaceAll("0$", "1");
                    negPosList.add(r.toLowerCase());
                } else if (s.endsWith("1")) {
                    negPosList.add(s.toLowerCase());
                    neutPosList.add(s.toLowerCase());
                } else {
                    System.err.println("[ERROR] Format ERROR: " + s);
                }
            }
        });

        long timestamp = System.currentTimeMillis();
        String negFn = "/tmp/negPosLRTrain-" + timestamp;
        String neutFn = "/tmp/neutPosLRTrain-" + timestamp;

        saveLocal(negFn, negPosList);
        saveLocal(neutFn, neutPosList);
        System.out.println("[SPLIT] Split LR training file: " + negFn + ", " + neutFn);

        return new Pair<String, String>(negFn, neutFn);
    }

    public static void saveLocal(String fn, List<String> data) throws IOException {
        BatchWriter batchWriter = new BatchWriter(new FileOutputStream(fn));

        for (String s : data) {
            batchWriter.writeWithCache(s + "\n");
        }

        batchWriter.close();
    }
}
