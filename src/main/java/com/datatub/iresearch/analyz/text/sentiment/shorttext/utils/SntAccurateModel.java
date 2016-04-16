package com.datatub.iresearch.analyz.text.sentiment.shorttext.utils;

import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.util.AccDist;
import com.datatub.iresearch.analyz.util.AccObject;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import com.yeezhao.commons.util.ILineParser;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lhfcws
 * @since 15/11/13.
 */
public class SntAccurateModel implements Serializable {
    public static final String modelPath = MLLibConfiguration.getInstance().get(MLLibConsts.FILE_SNT_ACC_MODEL);

    public static final String MODEL_NAME = MLLibConsts.FILE_SNT_ACC_MODEL;

    private String name = null;
    /**
     * 增量学习中的分类结果权重，不用于多分类器的投票
     */
    private Double weight = 0.0;

    private AccObject totalAcc = new AccObject(0, 0, 0.0);
    private AccDist<Integer> probs = new AccDist<Integer>();

    public SntAccurateModel(String name, Double weight, AccObject totalAcc, AccDist<Integer> probs) {
        this.name = name;
        this.weight = weight;
        this.probs = probs.clone();
        this.totalAcc = totalAcc.clone();
    }

    public SntAccurateModel(SntAccurateModel m) {
        this.name = m.getName();
        this.weight = m.getWeight();
        this.probs = m.getProbs().clone();
        this.totalAcc = m.getTotalAcc().clone();
    }

    public SntAccurateModel(String name) {
        this.name = name;
    }

    public SntAccurateModel(String name, double weight) {
        this.name = name;
        this.weight = weight;
    }

    public SntAccurateModel(SntAccurateModel m, double weight) {
        this.name = m.getName();
        this.weight = weight;
        this.probs = m.getProbs().clone();
        this.totalAcc = m.getTotalAcc().clone();
    }

    public SntAccurateModel(SntAccurateModel m, String name) {
        this.name = name;
        this.weight = m.getWeight();
        this.probs = m.getProbs().clone();
        this.totalAcc = m.getTotalAcc().clone();
    }

    public SntAccurateModel(SntAccurateModel m, String name, double weight) {
        this.name = name;
        this.weight = weight;
        this.probs = m.getProbs().clone();
        this.totalAcc = m.getTotalAcc().clone();
    }

    public void calc() {
        this.getProbs().calcProbsAccRate();
        this.totalAcc.calcAccRate();
    }

    public AccObject getTotalAcc() {
        return totalAcc;
    }

    public AccDist<Integer> getProbs() {
        return probs;
    }

    public String getName() {
        return name;
    }

    public Double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "[SntAccurateModel]================" + "\n" +
                "name: " + name + '\t' +
                "weight: " + weight + "\n" +
                "totalAcc: " + totalAcc + "\n" +
                "probs: " + probs +
                "================";
    }

    // ============ static functions

    /**
     * weight值和name跟随model，不会受models影响。
     *
     * @param model
     * @param models
     * @return
     */
    public static SntAccurateModel merge(SntAccurateModel model, SntAccurateModel... models) {
        SntAccurateModel m = new SntAccurateModel(model);

        for (SntAccurateModel sntAccurateModel : models) {
            m.getProbs().merge(sntAccurateModel.getProbs());
            m.getTotalAcc().update(sntAccurateModel.getTotalAcc());
        }

        return m;
    }

    public static SntAccurateModel unserialize(String line) {
        try {
            line = line.trim().replaceAll("[\t]+", "\t");
            String[] sarr = line.split("\t");
            String name = sarr[0].trim();
            AccObject totalAcc = AccObject.unserialize(sarr[1]);
            double weight = Double.valueOf(sarr[2]);
            AccDist<Integer> probs = new AccDist<Integer>();
            for (int i = 3; i < sarr.length; i++) {
                String[] sarr1 = sarr[i].split(":");
                int label = Integer.valueOf(sarr1[0]);
                AccObject accObject = AccObject.unserialize(sarr1[1]);
                probs.put(label, accObject);
            }
            return new SntAccurateModel(name, weight, totalAcc, probs);
        } catch (Exception e) {
            line = line.trim().replaceAll("[\t]+", "\t");
            System.err.println(line);
            System.err.println(Arrays.toString(line.split("\t")));
            return new SntAccurateModel("null");
        }
    }

    public static String serialize(SntAccurateModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append(model.name).append("\t")
                .append(AccObject.serialize(model.totalAcc)).append("\t")
                .append(model.weight).append("\t");
        for (Map.Entry<Integer, AccObject> entry : model.probs.entrySet()) {
            sb.append("\t").append(entry.getKey()).append(":")
                    .append(AccObject.serialize(entry.getValue()));
        }

        return sb.toString();
    }

    public static void dumpAccModel(OutputStream os, Collection<SntAccurateModel> models) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
        for (SntAccurateModel model : models) {
            writer.write(SntAccurateModel.serialize(model) + "\n");
        }
        writer.flush();
        writer.close();
        os.flush();
        os.close();
    }

    public static void updateAccModel(SntAccurateModel... models) throws IOException {
        if (models == null || models.length <= 0) return;
        Map<String, SntAccurateModel> map = loadAccModel();
        for (SntAccurateModel model : models) {
            if (model != null)
                map.put(model.getName(), model);
        }
        FileSystemUtil.deleteFile(SntAccurateModel.modelPath);
        dumpAccModel(FileSystemUtil.getHDFSFileOutputStream(
                modelPath
        ), map.values());
    }

    public static Map<String, SntAccurateModel> loadAccModel() {
        return loadAccModel(modelPath);
    }

    public static Map<String, SntAccurateModel> loadAccModel(String path) {
        try {
            return loadAccModel(FileSystemUtil.getHDFSFileInputStream(path));
        } catch (IOException e) {
            System.err.println("[SNT] No SntAccurateModel model file. Init empty snt acc models.");
            return new HashMap<String, SntAccurateModel>();
        }
    }

    public static Map<String, SntAccurateModel> loadAccModel(InputStream is) {
        final Map<String, SntAccurateModel> sntAccurateModelMap = new HashMap<String, SntAccurateModel>();
        if (is == null)
            try {
                is = FileSystemUtil.getHDFSFileInputStream(modelPath);

            } catch (Exception e) {
                System.err.println("[SNT] No SntAccurateModel model file. Init empty snt acc models.");
            }
        try {
            if (is != null)
                FileSystemUtil.loadFileInLines(is, new ILineParser() {
                    @Override
                    public void parseLine(String s) {
                        SntAccurateModel sntAccurateModel = SntAccurateModel.unserialize(s);
                        sntAccurateModelMap.put(sntAccurateModel.getName(), sntAccurateModel);
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sntAccurateModelMap;
    }

    /**************************************
     * ************  MAIN  *****************
     * Test main
     */
    public static void main(String[] args) throws IOException {
        Map<String, SntAccurateModel> sntAccurateModelMap = new HashMap<String, SntAccurateModel>();
        sntAccurateModelMap.put("test", new SntAccurateModel("test"));
        sntAccurateModelMap.put("test1", new SntAccurateModel("test1"));
        sntAccurateModelMap.put("test1", new SntAccurateModel("test1"));

        dumpAccModel(FileSystemUtil.getHDFSFileOutputStream(modelPath), sntAccurateModelMap.values());
        System.out.println(loadAccModel());
    }
}
