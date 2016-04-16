package paper.community;

import paper.community.model.Community;

import java.util.LinkedList;
import java.util.List;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class KOLLookup {

    public List<String> lookup(Community community) {
        community.kols = new LinkedList<>(community.users.keySet());
        return community.kols;
    }
}
