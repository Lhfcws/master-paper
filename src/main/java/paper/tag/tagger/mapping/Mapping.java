package paper.tag.tagger.mapping;

import com.yeezhao.commons.util.FreqDist;

/**
 * @author lhfcws
 * @since 16/4/27
 */
public interface Mapping {
    FreqDist<String> map(Object o);
}
