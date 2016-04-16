package com.datatub.iresearch.analyz.math;

/**
 * @author lhfcws
 * @since 15/11/13.
 */
public class VectorOp {
    public static double euclidDistance(Vector v1, Vector v2) {
        if (v1.dimension() != v2.dimension()) return -1;

        double sum = 0.0;
        double[] vec1 = v1.getVec();
        double[] vec2 = v2.getVec();
        for (int i = 0; i < v1.dimension(); i++) {
            sum += (vec1[i] - vec2[i]) * (vec1[i] - vec2[i]);
        }
        return Math.sqrt(sum);
    }

    public static double cosDistance(Vector v1, Vector v2) {
        if (v1.dimension() != v2.dimension()) return -1;

        double sum = 0.0;
        double[] vec1 = v1.getVec();
        double[] vec2 = v2.getVec();
        for (int i = 0; i < v1.dimension(); i++) {
            sum += vec1[i] * vec2[i];
        }

        return sum / (v1.getEuclidLength() * v2.getEuclidLength());
    }

    public static double advCosDistance(Vector v1, Vector v2) {
        if (v1.dimension() != v2.dimension()) return -1;

        double sum = 0.0;
        double avg = (v1.sum() + v2.sum()) / (v1.dimension() + v2.dimension());
        double[] vec1 = v1.getVec();
        double[] vec2 = v2.getVec();
        for (int i = 0; i < v1.dimension(); i++) {
            sum += (vec1[i] - avg) * (vec2[i] - avg);
        }

        return sum / (v1.getEuclidLength() * v2.getEuclidLength());
    }
}
