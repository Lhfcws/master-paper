package com.datatub.iresearch.analyz.text.sentiment.shorttext.tools;

import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.ASntILClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl.LinearRegressionClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl.LinearRegressionNeutClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.utils.SntAccurateModel;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.datatub.iresearch.analyz.util.load.DictsLoader;
import com.yeezhao.commons.util.AdvCli;
import com.yeezhao.commons.util.CliRunner;
import com.yeezhao.commons.util.Pair;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;

import java.util.Map;

/**
 * @author lhfcws
 * @since 15/11/13.
 */
public class SntBatchRetrainer implements CliRunner {
    private static final String CLI_PARAM_TRAINER = "trainer";
    private static final String CLI_PARAM_RESET = "reset";
    private Map<String, ASntILClassifier> sntClassifiers;
    private static Configuration conf = MLLibConfiguration.getInstance();

    public static void main(String[] args) {
        AdvCli.initRunner(args, SntBatchRetrainer.class.getSimpleName(), new SntBatchRetrainer());
//        try {
//            new SntBatchRetrainer().loadSntILClassifiers();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public Options initOptions() {
        Options options = new Options();
//        options.addOption(CLI_PARAM_TRAINER, true, "Simple className of SntILClassifier, if no -trainer then retrain all.");
        options.addOption(AdvCli.CLI_PARAM_I, true, "input text file with label (pos 1, neut 0, neg -1, other 2,3,4...), format: content[\\t]label");
        options.addOption(CLI_PARAM_RESET, false, "Reset & clear the retrain model.");
        return options;
    }

    @Override
    public boolean validateOptions(CommandLine commandLine) {
        return commandLine.hasOption(AdvCli.CLI_PARAM_I);
    }

    @Override
    public void start(CommandLine commandLine) {
        try {
//            sntClassifiers = loadClassifiers();
            String trainerName = commandLine.getOptionValue(CLI_PARAM_TRAINER);
            retrain(commandLine.getOptionValue(AdvCli.CLI_PARAM_I), trainerName, commandLine.hasOption(CLI_PARAM_RESET));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public Map<String, ASntILClassifier> loadClassifiers() {
//        Map<String, ASntILClassifier> map = new HashMap<String, ASntILClassifier>();
//        LinearRegressionClassifier linearRegressionClassifier = new LinearRegressionClassifier();
//        LinearRegressionNeutClassifier linearRegressionNeutClassifier = new LinearRegressionNeutClassifier();
//        map.put(linearRegressionClassifier.getName(), linearRegressionClassifier);
//        map.put(linearRegressionNeutClassifier.getName(), linearRegressionNeutClassifier);
//        return map;
//    }

    public void retrain(String inputText, String trainerName, boolean reset) throws Exception {
        DictsLoader.loadSntAccModels(false);
        Pair<String, String> pair = LinearRegressionTrainerCli.splitFile(inputText);

        LinearRegressionClassifier linearRegressionClassifier = new LinearRegressionClassifier();
        LinearRegressionNeutClassifier linearRegressionNeutClassifier = new LinearRegressionNeutClassifier();
        if (reset) {
            FileSystemUtil.deleteFile(conf.get(MLLibConsts.FILE_SNT_LR_CORPUS_INCR));
            FileSystemUtil.deleteFile(conf.get(MLLibConsts.FILE_SNT_LR_CORPUS1_INCR));
        }

        _retrain(pair.first, linearRegressionClassifier);
        _retrain(pair.second, linearRegressionNeutClassifier);

        FileSystemUtil.deleteFile(SntAccurateModel.modelPath);
        SntAccurateModel.dumpAccModel(
                FileSystemUtil.getHDFSFileOutputStream(SntAccurateModel.modelPath),
                Dicts.sntAccurateModelMap.values());
        DictsLoader.loadSntAccModels(true);
    }

    private void _retrain(String inputText, ASntILClassifier sntILClassifier) {
        try {
            System.out.println("[SNT] Retrain " + sntILClassifier.getName());
            sntILClassifier.update(Dicts.sntAccurateModelMap);
            SntAccurateModel sntAccurateModel = sntILClassifier.retrain(inputText);
            sntILClassifier.update(sntAccurateModel);
            sntILClassifier.upgradeModel(Dicts.sntAccurateModelMap);
        } catch (Exception e) {
            System.err.println(sntILClassifier.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("[SNT][RESULT] " + Dicts.sntAccurateModelMap);
    }
}
