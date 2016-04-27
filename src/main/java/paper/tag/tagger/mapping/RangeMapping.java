package paper.tag.tagger.mapping;

import com.yeezhao.commons.util.FreqDist;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lhfcws
 * @since 16/4/27
 */
public class RangeMapping implements Mapping {
    Map<Range, String> map = new HashMap<>();

    public void add(String tag, String rangeStr) throws Exception {
        map.put(Range.parse(rangeStr), tag);
    }

    @Override
    public FreqDist<String> map(Object o) {
        FreqDist<String> freq = new FreqDist<>();
        Integer n = (Integer)o;
        for (Range r : map.keySet()) {
            if (r.in(n))
                freq.inc(map.get(r));
        }
        return freq;
    }
}
