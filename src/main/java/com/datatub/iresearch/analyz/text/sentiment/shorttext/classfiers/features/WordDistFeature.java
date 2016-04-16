package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.features;

import com.datatub.iresearch.analyz.base.IFeature;
import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import org.apache.hadoop.conf.Configuration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lhfcws
 * @since 15/11/25.
 */
public class WordDistFeature implements IFeature {
    private ConcurrentHashMap<String, Integer> featureMap = null;
    /**
     * Singleton instance.
     */
    private static WordDistFeature _singleton = null;

    /**
     * Singleton access method.
     */
    public static WordDistFeature getInstance() {
        if (_singleton == null)
            synchronized (WordDistFeature.class) {
                if (_singleton == null) {
                    _singleton = new WordDistFeature();
                }
            }
        return _singleton;
    }

    /**
     * Private constructor.
     */
    private WordDistFeature() {
        Configuration conf = MLLibConfiguration.getInstance();
        try {
            loadFeatures(conf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public double extractFeature(Object raw) {
        return 0;
    }

    @Override
    public double[] extractFeatures(Object raw) {
        List<String> words = (List<String>) raw;
        double[] f_array = new double[featureMap.size() + 1];
        Arrays.fill(f_array, 0);
        for (String kw : words) {
            if (featureMap.containsKey(kw)) {
                f_array[featureMap.get(kw)] += 1;
            }
        }

        return f_array;
    }

    /**
     * Load feature.txt for generating features.
     *
     * @param conf
     * @throws Exception
     */
    private void loadFeatures(Configuration conf) throws Exception {
        featureMap = new ConcurrentHashMap<String, Integer>();
        InputStream inputStream = conf.getConfResourceAsInputStream("sentiment/feature.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        // Load Feature
        String line;
        int index = 1;
        while ((line = br.readLine()) != null) {
            featureMap.put(line.toLowerCase().trim(), index);
            index += 1;
        }
        br.close();
        inputStream.close();
        System.out.println("[SNT] LR features loaded.");
    }

    public int getFeatureSize() {
        return featureMap.size();
    }
}
