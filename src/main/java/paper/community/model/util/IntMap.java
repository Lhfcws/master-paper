package paper.community.model.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lhfcws
 * @since 16/7/9
 */
public class IntMap {
    protected volatile int index = 0;
    protected Map<Object, Integer> mp = new HashMap<>();

    public synchronized int get(Object obj) {
        if (mp.containsKey(obj))
            return mp.get(obj);
        else {
            mp.put(obj, index++);
            return index - 1;
        }
    }
}
