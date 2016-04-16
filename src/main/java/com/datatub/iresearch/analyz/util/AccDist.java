package com.datatub.iresearch.analyz.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lhfcws
 * @since 15/11/15.
 */
public class AccDist<K> extends HashMap<K, AccObject> {

    public AccDist() {
    }

    public void setDefault(K key) {
        if (!containsKey(key))
            put(key, new AccObject());
    }

    public int incTotal(K key, int t) {
        setDefault(key);
        get(key).total += t;
        return get(key).total;
    }

    public int incCorrect(K key, int c) {
        setDefault(key);
        get(key).correct += c;
        return get(key).correct;
    }

    public double calcAccRate(K key) {
        setDefault(key);
        return get(key).calcAccRate();
    }

    public void calcProbsAccRate() {
        for (AccObject accObject : values()) {
            accObject.calcAccRate();
        }
    }

    public AccObject safeGet(K key) {
        if (!containsKey(key))
            return new AccObject();
        else
            return get(key);
    }

    public AccDist<K> merge(AccDist<K> accDist) {
        if (accDist == null) return this;
        for (Map.Entry<K, AccObject> entry : accDist.entrySet()) {
            if (!containsKey(entry.getKey()))
                put(entry.getKey(), entry.getValue().clone());
            else {
                get(entry.getKey()).update(entry.getValue());
            }
        }
        return this;
    }

    @Override
    public AccDist<K> clone() {
        AccDist<K> accDist = new AccDist<K>();
        for (Map.Entry<K, AccObject> entry : entrySet()) {
            accDist.put(entry.getKey(), entry.getValue().clone());
        }
        return accDist;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nAccDist--------\n");
        for (Map.Entry<K, AccObject> entry : entrySet()) {
            sb.append(entry.getKey()).append("  =  ").append(entry.getValue().toString()).append("\n");
        }
        sb.append("--------\n");
        return sb.toString();
    }
}
