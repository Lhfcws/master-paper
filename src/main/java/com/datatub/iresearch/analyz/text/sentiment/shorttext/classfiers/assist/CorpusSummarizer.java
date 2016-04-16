package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.assist;

import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.math.LibSVMFeatureLabel;
import com.datatub.iresearch.analyz.math.MathFunctions;
import com.datatub.iresearch.analyz.util.TopMap;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.tree.model.RandomForestModel;

import java.io.Serializable;


/**
 * @author lhfcws
 * @since 15/11/15.
 */
public class CorpusSummarizer implements Serializable {
    public static final int MAX_IL_CORPUS = MLLibConsts.DEFAULT_CORPUS_SIZE;

    public static TopMap<Double, LibSVMFeatureLabel> summarize(JavaRDD<LabeledPoint> data, final RandomForestModel model) {
        final TopMap<Double, LibSVMFeatureLabel> topMap = new TopMap<Double, LibSVMFeatureLabel>(MAX_IL_CORPUS);
        data.foreach(new VoidFunction<LabeledPoint>() {
            @Override
            public void call(LabeledPoint labeledPoint) throws Exception {
                if (model == null || labeledPoint == null) {
                    System.out.println("[DEBUG] LabeledPoint: " + labeledPoint);
                    System.out.println("[DEBUG] Model: " + model);
                    return;
                }
                double r = model.predict(labeledPoint.features());
                double loss = MathFunctions.logLoss(r, labeledPoint.label());
                if (topMap.canInsert(loss))
                    topMap.insert(loss, LibSVMFeatureLabel.parseFromLabeledPoint(labeledPoint));
            }
        });
        return topMap;
    }


}
