package paper.community;

import com.yeezhao.commons.util.CollectionUtil;
import com.yeezhao.commons.util.FreqDist;
import paper.community.model.Community;

import java.util.List;
import java.util.Map;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class RubbishCommunityRecognizer {
    protected static int PREFIX = 3;

    public boolean isRubbish(Community community) {
        FreqDist<String> freqDist = new FreqDist<>();

        if (CollectionUtil.isEmpty(community.kols))
            return true;

        int size = community.kols.size();
        for (String kol : community.kols) {
            freqDist.inc(kol.substring(0, PREFIX));
        }

        List<Map.Entry<String, Integer>> list = freqDist.sortValues(false);
        if (list.get(0).getValue() * 2 >= size)
            return true;

        if (list.size() > 1) {
            int v = (list.get(0).getValue()) + list.get(1).getValue();
            if (1.0 * v >= size * 2.0 / 3.0)
                return true;
        }

        return false;
    }
}
