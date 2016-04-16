package com.datatub.iresearch.analyz.util;

import java.util.TreeMap;

/**
 * @author lhfcws
 * @since 15/11/15.
 */
public class TopMap<K, V> extends TreeMap<K, V> {
    private int topsize;

    public TopMap(int tsize) {
        topsize = tsize;
    }

    public boolean canInsert(K key) {
        return size() < topsize || this.comparator().compare(lastKey(), key) < 0;
    }

    public boolean insert(K key, V value) {
        if (size() < topsize) {
            put(key, value);
            return true;
        } else if (this.comparator().compare(lastKey(), key) < 0) {
            remove(lastKey());
            put(key, value);
            return true;
        }
        return false;
    }
}
