package com.datatub.iresearch.analyz.math;

import java.io.Serializable;
import java.util.List;

/**
 * @author lhfcws
 * @since 15/11/13.
 */
public class Vector implements Serializable {
    double[] vec;

    public Vector() {
        vec = new double[0];
    }

    public Vector(double[] vec) {
        this.vec = vec;
    }

    public Vector(List<Double> list) {
        setVector(list);
    }

    public int dimension() {
        return vec.length;
    }

    public void setVector(List<Double> list) {
        if (list != null) {
            vec = new double[list.size()];
            for (int i = 0; i < vec.length; i++)
                vec[i] = list.get(i);
        }
    }
    
    public double getEuclidLength() {
        double sum = 0;
        for (double f : vec)
            sum += f * f;
        return Math.sqrt(sum);
    }

    public double[] getVec() {
        return vec;
    }

    public double avg() {
        return sum() / dimension();
    }

    public double sum() {
        double _sum = 0;
        for (double f : vec) {
            _sum += f;
        }

        return _sum;
    }
}
