package paper.community;

import paper.community.model.Community;
import paper.community.model.WeiboUser;

import java.util.*;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class KOLLookup {

    public List<String> lookup(Community community, int topk) {
        Set<String> selected = new HashSet<String>();
        if (topk > community.users.size()) topk = community.users.size();

        for (int i = 0; i < topk; i++) {
            double max = 0;
            String uid = null;
            for (WeiboUser weiboUser : community.users.values())
            if (max < weiboUser.weight && !selected.contains(weiboUser.id)){
                max = weiboUser.weight;
                uid = weiboUser.id;
            }
            selected.add(uid);
        }
        return (community.kols = new ArrayList<>(selected));
    }
}
