package com.datatub.iresearch.analyz.math;

import java.util.List;

/**
 * @author lhfcws
 * @since 15/11/15.
 */
public class MathFunctions {
    public static double sigmoid(double x, double mid) {
        return 1.0 / (1 + Math.pow(Math.E, -(x - mid) / mid));
    }

    public static double logLoss(double predict, double label) {
        return - 4.0 * label / (1.0 + Math.exp(2.0 * label * predict));
    }

    public static double variance(List<? extends Number> arr) {
        double sum = 0;
        for (Number d : arr) {
            sum += d.doubleValue() * d.doubleValue();
        }
        return sum;
    }
}
