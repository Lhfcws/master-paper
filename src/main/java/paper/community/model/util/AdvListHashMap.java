package paper.community.model.util;

import com.yeezhao.commons.util.AdvHashMap;

import java.util.LinkedList;
import java.util.List;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class AdvListHashMap<T> extends AdvHashMap<String, List<T>> {

    public AdvListHashMap<T> add(String key, T value) {
        this.setDefault(key, new LinkedList<T>());
        this.get(key).add(value);
        return this;
    }

    public int totalSize() {
        int sum = 0;
        for (List<T> list : values())
            sum += list.size();
        return sum;
    }
}


