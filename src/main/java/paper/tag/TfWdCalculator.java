package paper.tag;

import com.yeezhao.commons.util.DoubleDist;
import paper.community.model.WeiboUser;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class TfWdCalculator implements Serializable {

    protected Collection<WeiboUser> docs;
    protected int totalDocsSize;
    protected double totalWeight;

    public void setWeiboUsers(Collection<WeiboUser> docs) {
        this.docs = docs;
        totalDocsSize = this.docs.size();
        totalWeight = 0;
        for (WeiboUser doc : this.docs) {
            for (Map.Entry<String, Integer> entry : doc.tags.entrySet())
                totalWeight += entry.getValue() * doc.weight;
        }
    }

    public DoubleDist<String> calc(int topk) {
        DoubleDist<String> tfDist = new DoubleDist<>();
        DoubleDist<String> wdDist = new DoubleDist<>();
        Set<String> allTags = new HashSet<>();

        for (WeiboUser doc : this.docs) {
            for (Map.Entry<String, Integer> entry : doc.tags.entrySet()) {
                tfDist.inc(entry.getKey(), entry.getValue() * doc.weight);
                wdDist.inc(entry.getKey(), 1.0);
                allTags.add(entry.getKey());
            }
        }

        DoubleDist<String> ret = new DoubleDist<>();
        for (String tag : allTags) {
            double score = tfDist.get(tag) * wdDist.get(tag) / (totalWeight * totalDocsSize);
            ret.put(tag, score);
        }

        return ret.topKDistByValues(false, topk);
    }
}
