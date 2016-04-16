package com.datatub.iresearch.analyz.util;

import com.yeezhao.commons.util.StringUtil;

import java.io.Serializable;

/**
 * @author lhfcws
 * @since 15/11/15.
 */
public class AccObject implements Serializable, Cloneable {
    public int total = 0;
    public int correct = 0;
    public double accRate = 0.0;
    
    public AccObject() {
    }

    public AccObject(Integer total, Integer correct, Double accRate) {
        this.total = total;
        this.correct = correct;
        this.accRate = accRate;
    }

    public AccObject update(AccObject accObject) {
        if (accObject != null) {
            this.total += accObject.total;
            this.correct += accObject.correct;
        }
        this.accRate = this.calcAccRate();
        return this;
    }

    public double calcAccRate() {
        if (total == 0) {
            this.accRate = 0.0;
        } else {
            this.accRate = correct * 1.0 / total;
        }
        return this.accRate;
    }

    public int getTotal() {
        return total;
    }

    public int getCorrect() {
        return correct;
    }

    public double getAccRate() {
        return accRate;
    }

    public void setTotal(int t) {
        total = t;
    }

    public void setCorrect(int c) {
        correct = c;
    }

    public void setAccRate(double a) {
        accRate = a;
    }

    @Override
    public String toString() {
        return
                "accRate: " + accRate +
                "\tcorrect: " + correct +
                "\ttotal: " + total;
    }

    @Override
    public AccObject clone() {
        return new AccObject(total, correct, accRate);
    }

    // ========= serialization
    public static String serialize(AccObject accObject) {
        StringBuilder sb = new StringBuilder();
        sb.append(accObject.total).append(StringUtil.DELIMIT_2ND)
                .append(accObject.correct).append(StringUtil.DELIMIT_2ND)
                .append(accObject.accRate);
        return sb.toString();
    }

    public static AccObject unserialize(String str) {
        String[] sarr = str.trim().split(StringUtil.STR_DELIMIT_2ND);
        AccObject accObject = new AccObject(Integer.valueOf(sarr[0]), Integer.valueOf(sarr[1]), Double.valueOf(sarr[2]));
        return accObject;
    }
}