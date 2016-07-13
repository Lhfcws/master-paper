package paper.community.model.util;

import com.yeezhao.commons.util.FreqDist;
import paper.community.model.UserRelations;
import paper.render.NodeType;

import java.util.*;

/**
 * @author lhfcws
 * @since 16/7/10
 */
public class GraphFilter {
    public Set<String> existUsers = new HashSet<>();
    public FreqDist<String> weights = new FreqDist<>();

    public UserRelations filter(UserRelations userRelations, int limitSize) {
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

    public Map<NodeType, List<NodeType>> filter(Map<NodeType, List<NodeType>> userRelations, int limitSize) {
        for (Map.Entry<NodeType, List<NodeType>> entry : userRelations.entrySet()) {
            weights.inc(entry.getKey().getId());
            for (NodeType n : entry.getValue()) {
                weights.inc(n.getId());
            }
        }

        List<Map.Entry<String, Integer>> list = weights.sortValues(false);
        if (list.size() > limitSize)
            list = list.subList(0, limitSize);

        Map<NodeType, List<NodeType>> u = new HashMap<>();
        for (Map.Entry<String, Integer> e : list) {
            existUsers.add(e.getKey());
        }

        for (Map.Entry<NodeType, List<NodeType>> entry : userRelations.entrySet()) {
            if (!existUsers.contains(entry.getKey().getId())) continue;
            entry.getKey().setSize(weights.get(entry.getKey().getId()));

            u.put(entry.getKey(), new LinkedList<NodeType>());
            for (NodeType n : entry.getValue()) {
                n.setSize(weights.get(n.getId()));
                if (existUsers.contains(n.getId()))
                    u.get(entry.getKey()).add(n);
            }
        }

        return u;
    }

    public int size() {
        return existUsers.size();
    }
}
