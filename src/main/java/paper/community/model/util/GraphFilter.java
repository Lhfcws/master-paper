package paper.community.model.util;

import com.yeezhao.commons.util.FreqDist;
import paper.community.model.UserRelations;

import java.util.*;

/**
 * @author lhfcws
 * @since 16/7/10
 */
public class GraphFilter {
    public Set<String> existUsers = new HashSet<>();

    public UserRelations filter(UserRelations userRelations, int limitSize) {
        FreqDist<String> weights = new FreqDist<>();

        for (Map.Entry<String, List<String>> entry : userRelations.entrySet()) {
            weights.inc(entry.getKey());
            for (String followee : entry.getValue()) {
                weights.inc(followee);
            }
        }

        List<Map.Entry<String, Integer>> list = weights.sortValues(false);
        if (list.size() > limitSize)
            list = list.subList(0, limitSize);

        UserRelations u = new UserRelations();
        for (Map.Entry<String, Integer> e : list) {
            existUsers.add(e.getKey());
        }

        for (Map.Entry<String, List<String>> entry : userRelations.entrySet()) {
            if (!existUsers.contains(entry.getKey())) continue;

            u.put(entry.getKey(), new LinkedList<String>());
            for (String followee : entry.getValue()) {
                if (existUsers.contains(followee))
                    u.get(entry.getKey()).add(followee);
            }
        }

        return u;
    }
}
