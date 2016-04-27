package paper.tag.tagger.mapping;

import com.yeezhao.commons.util.FreqDist;

import java.util.*;

/**
 * @author lhfcws
 * @since 16/4/27
 */
public class KeywordMapping implements Mapping {
    Map<String, Set<String>> mapping = new HashMap<>();

    public void add(String tag, Collection<String> kws) {
        mapping.put(tag, new HashSet<>(kws));
    }

    @Override
    public FreqDist<String> map(Object o) {
        FreqDist<String> dist = new FreqDist<>();
        String prov = (String)o;

        for (Map.Entry<String, Set<String>> entry : mapping.entrySet()) {
            if (entry.getValue().contains(prov))
                dist.inc(entry.getKey());
        }

        return dist;
    }
}
