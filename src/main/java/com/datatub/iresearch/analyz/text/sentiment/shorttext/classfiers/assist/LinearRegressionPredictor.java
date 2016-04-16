package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.assist;

import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.features.WordDistFeature;
import com.datatub.iresearch.analyz.util.SegUtil;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.yeezhao.commons.util.Pair;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import org.apache.hadoop.conf.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * It can be a classifier to join IL-Classifier voting.
 * But it is not easy to deal it with increment learning, so I lazily change it as a feature of RandomForest.
 * @author zicun, lhfcws
 * @since 15/11/13.
 */
public class LinearRegressionPredictor {
    private WordDistFeature wordDistFeature;

    private double threshold = 0;
//    private static Model model = null;

    /**
     * Singleton instance.
     */
    private static LinearRegressionPredictor _singleton = null;

    /**
     * Singleton access method.
     */
    public static LinearRegressionPredictor getInstance() {
        if (_singleton == null)
            synchronized (LinearRegressionPredictor.class) {
                if (_singleton == null) {
                    _singleton = new LinearRegressionPredictor();
                }
            }
        return _singleton;
    }

    private LinearRegressionPredictor() {
        wordDistFeature = WordDistFeature.getInstance();
        Configuration conf = MLLibConfiguration.getInstance();
        try {
//            loadModel(conf);
            threshold = Double.parseDouble(
                    conf.get(MLLibConsts.CONFIG_SENTIMENT_THRESHOLD)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * predict result as feature.
     * @param tweet
     * @return
     */
    public double predict(String tweet, Model _model) {
        Feature[] instance = genTweetFeatureVector(tweet);

        double[] prob = new double[2];
        double rst = Linear.predictProbability(_model, instance, prob);
//        if (Math.abs(prob[0] - prob[1]) < 0.1) {
//            return 0;
//        }

        if (prob[0] > threshold) {
            return prob[0];
        } else if (prob[1] > threshold) {
            return prob[1] * (-1);
        }
        return rst;
    }

    /**
     * Load LR Model
     * @throws Exception
     */
//    private void loadModel(Configuration conf) {
//        if (model == null) {
//            System.out.println("[SNT] Loading LR Model");
//            try {
//                InputStream is = FileSystemUtil.getHDFSFileInputStream(conf.get(
//                        MLLibConsts.FILE_SNT_LR_MODEL_BASE
//                ));
//                model = Model.load(new InputStreamReader(is));
//                is.close();
//            } catch (Exception ignore) {
//                model = new Model();
//                System.out.println("[SNT] No init LR base model, init empty model.");
//            }
//
//            System.out.println("[SNT] LR model loaded.");
//        }
//    }

    /**
     * Generate features array from raw text.
     * @param tweet
     * @return
     */
    public Feature[] genTweetFeatureVector(String tweet) {
        List<Pair<Integer, Integer>> segRes = SegUtil.backwardMaxMatch(
                tweet, 10, 2, Dicts.dict.get(Dicts.DIC_D),
                Dicts.dict.get(Dicts.POS_D), Dicts.dict.get(Dicts.NEG_D),
                Dicts.dict.get(Dicts.ROUGH_D)
        );
        List<String> words = SegUtil.positionPairs2Words(tweet, segRes);
        double[] f_array = wordDistFeature.extractFeatures(words);

        List<Feature> features = new ArrayList<Feature>();
        for (int i = 1; i < f_array.length; i++) {
            if (f_array[i] != 0)
                features.add(new FeatureNode(i, f_array[i]));
        }
        Feature[] instance = new Feature[features.size()];
        features.toArray(instance);
        return instance;
    }

    public double getThreshold() {
        return threshold;
    }

    public int getFeatureSize() {
        return wordDistFeature.getFeatureSize();
    }

//    public static Model getModel() {
//        return model;
//    }
}
