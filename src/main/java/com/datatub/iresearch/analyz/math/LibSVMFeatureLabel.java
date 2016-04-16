package com.datatub.iresearch.analyz.math;

import org.apache.spark.mllib.linalg.DenseVector;
import org.apache.spark.mllib.linalg.SparseVector;
import org.apache.spark.mllib.regression.LabeledPoint;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lhfcws
 * @since 15/11/13.
 */
public class LibSVMFeatureLabel implements Serializable {
    public Map<Integer, Double> features;
    public int label;

    public LibSVMFeatureLabel(Vector vector, int label) {
        features = new HashMap<Integer, Double>();
        this.label = label;
        for (int i = 0; i < vector.dimension(); i++) {
            if (vector.getVec()[i] != 0)
                features.put(i, vector.getVec()[i]);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        for (Map.Entry<Integer, Double> entry : features.entrySet()) {
            sb.append(" ").append(entry.getKey() + 1).append(":").append(entry.getValue());
        }
        return sb.toString();
    }

    public LabeledPoint toLabeledPoint(int featureSize) {
        double l = 1.0 * label;
        int[] indices = new int[features.size()];
        double[] vals = new double[features.size()];
        int i = 0;

        for (Map.Entry<Integer, Double> entry : features.entrySet()) {
            indices[i] = entry.getKey() + 1;
            vals[i] = entry.getValue();
            i++;
        }

        SparseVector sparseVector = new SparseVector(featureSize, indices, vals);

        return new LabeledPoint(l, sparseVector);
    }

    public static LibSVMFeatureLabel parseFromLabeledPoint(LabeledPoint labeledPoint) {
        Vector v = new Vector(labeledPoint.features().toArray());
        int lbl = (int) Math.round(labeledPoint.label());
        return new LibSVMFeatureLabel(v, lbl);
    }

    /***************************************
     * small test
     * @param args
     */
    public static void main(String[] args) {
        double[] vec = {0.1, 0, 0, 0.008, 0.003, 0};
        DenseVector denseVector = new DenseVector(vec);
        System.out.println(new LabeledPoint(1.0, denseVector).toString());
        Vector vector = new Vector(vec);
        LibSVMFeatureLabel libSVMFeatureLabel = new LibSVMFeatureLabel(vector, 1);
    }
}
