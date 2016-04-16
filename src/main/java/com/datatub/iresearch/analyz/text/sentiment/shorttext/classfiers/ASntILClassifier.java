package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers;

import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.math.MathFunctions;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.utils.SntAccurateModel;
import com.datatub.iresearch.analyz.util.AccDist;
import com.datatub.iresearch.analyz.util.AccObject;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.datatub.iresearch.analyz.util.load.DictsLoader;
import com.yeezhao.commons.util.Pair;

import java.util.Map;

/**
 * Sentiment increament learning supported classifier.
 *
 * @author lhfcws
 * @since 15/11/16.
 */
public abstract class ASntILClassifier
        extends Pair<ISntClassifier, ISntClassifier> implements ISntClassifier {
    protected SntAccurateModel sntAccurateModel = new SntAccurateModel(getName());

    /**
     * 初始训练分类器和增量分类器分开
     *
     * @param iSntClassifier
     * @param iSntClassifier2
     */
    public ASntILClassifier(ISntClassifier iSntClassifier, ISntClassifier iSntClassifier2) {
        super(iSntClassifier, iSntClassifier2);
    }

    /**
     * 增量学习重训练接口
     *
     * @return
     */
    public abstract SntAccurateModel retrain(String inputText);

    @Override
    public ISntClassifier init() {
        DictsLoader.loadSntAccModels(false);
        this.update(Dicts.sntAccurateModelMap);
        return this;
    }

    @Override
    public ISntClassifier update(SntAccurateModel sntAccurateModel) {
        if (sntAccurateModel == null) return this;
        if (sntAccurateModel.getName().equals(first.getName()))
            this.first.update(sntAccurateModel);
        else if (sntAccurateModel.getName().equals(second.getName()))
            this.second.update(sntAccurateModel);
        else if (sntAccurateModel.getName().equals(getName()))
            this.sntAccurateModel = sntAccurateModel;
        return this;
    }

    public ASntILClassifier update(Map<String, SntAccurateModel> sntAccurateModelMap) {
        if (sntAccurateModelMap.containsKey(getName()))
            this.update(sntAccurateModelMap.get(getName()));
        if (sntAccurateModelMap.containsKey(first.getName()))
            this.first.update(sntAccurateModelMap.get(first.getName()));
        if (sntAccurateModelMap.containsKey(second.getName()))
            this.second.update(sntAccurateModelMap.get(second.getName()));
        return this;
    }

    public ASntILClassifier upgradeModel(Map<String, SntAccurateModel> sntAccurateModelMap) {
        sntAccurateModelMap.put(getName(), getSntAccurateModel());
        sntAccurateModelMap.put(first.getName(), first.getSntAccurateModel());
        sntAccurateModelMap.put(second.getName(), second.getSntAccurateModel());
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

    /**
     * 一个分类器可同时支持初始训练和增量学习
     *
     * @param iSntClassifier
     */
    public ASntILClassifier(ISntClassifier iSntClassifier) {
        super(iSntClassifier, iSntClassifier);
    }

    public ISntClassifier getBaseClassifier() {
        return first;
    }

    public void setBaseClassifier(ISntClassifier iSntClassifier) {
        this.first = iSntClassifier;
    }

    public ISntClassifier getIncrClassifier() {
        return second;
    }

    public void setIncrClassifier(ISntClassifier iSntClassifier) {
        this.second = iSntClassifier;
    }

    /**
     * Use sigmoid function to calculate the weight.
     *
     * @param newDataSize
     * @param oldDataSize
     * @return
     */
    public double calcIncrClassifierWeight(int newDataSize, int oldDataSize) {
        double sigmoidVar = Math.sqrt(oldDataSize) * Math.sqrt(Math.sqrt(oldDataSize));
        return MathFunctions.sigmoid(newDataSize, sigmoidVar);
    }

    /**
     * Merge base and incr accModels.
     *
     * @return
     */
    public SntAccurateModel mergeAccModel() {
        boolean firstNull = false, secondNull = false;
        if (this.first == null || this.first.getSntAccurateModel() == null)
            firstNull = true;
        if (this.second == null || this.second.getSntAccurateModel() == null)
            secondNull = true;

        if (firstNull && secondNull)
            return new SntAccurateModel(getName());
        else if (firstNull)
            return new SntAccurateModel(this.second.getSntAccurateModel(), getName());
        else if (secondNull)
            return new SntAccurateModel(this.first.getSntAccurateModel(), getName());
        else {
            return SntAccurateModel.merge(new SntAccurateModel(getName()),
                    this.first.getSntAccurateModel(), this.second.getSntAccurateModel());
        }
    }

    @Override
    public int classify(String text) {
        // 1. classify
        int labelB = this.first.classify(text);
        int labelI = this.second.classify(text);

        // 2. if same result, easy to decide.
        if (labelB == labelI) return labelB;

        if (labelI == MLLibConsts.UNCLASSIFY)
            return labelB;

        // 3. if different result, compare [ P(Label|Model) * Weight ]
        double probB = this.first.getAccurateDist().safeGet(labelB).getAccRate();
        double probI = this.second.getAccurateDist().safeGet(labelI).getAccRate();
        double scoreB = probB * this.first.getSntAccurateModel().getWeight();
        double scoreI = probI * this.second.getSntAccurateModel().getWeight();

        if (scoreB > scoreI)
            return labelB;
        else if (scoreB < scoreI)
            return labelI;
        else {
            // 4. if result of step3 is the same, the compare total accurate of the model.
            return this.first.getTotalAccurate().getAccRate() >= this.second.getTotalAccurate().getAccRate() ?
                    labelB : labelI;

        }
    }
}
