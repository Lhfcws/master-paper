package paper.tag;

import com.yeezhao.commons.util.CollectionUtil;
import com.yeezhao.commons.util.DoubleDist;
import com.yeezhao.commons.util.FreqDist;
import com.yeezhao.commons.util.serialize.GsonSerializer;

import java.util.*;

/**
 * @author lhfcws
 * @since 16/7/17
 */
public class Evaluator {
    public DoubleDist<String> simple = new DoubleDist<>();
    public DoubleDist<String> tfwd = null;
    public int cid;

    public Evaluator(int cid) {
        this.cid = cid;
    }

    public void addSimple(FreqDist<String> tags) {
        for (Map.Entry<String, Integer> e : tags.entrySet()) {
            simple.inc(e.getKey(), e.getValue());
        }
    }

    public void printSimple(int topn) {
        List<Map.Entry<String, Double>> list1 = simple.sortValues(false);
        list1 = CollectionUtil.subList(list1, topn);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> e : list1) {
            sb.append(e.getKey()).append("$");
        }
        sb.setLength(sb.length() - 1);
        System.out.println(cid + " simple tags: " + sb.toString());
    }

    public void evaluate(int topn) {
        if (tfwd != null && simple != null) {
            List<Map.Entry<String, Double>> list = tfwd.sortValues(false);
            List<Map.Entry<String, Double>> list1 = simple.sortValues(false);

            list = CollectionUtil.subList(list, topn);
            list1 = CollectionUtil.subList(list1, topn);

            Map<String, Integer> index1 = new HashMap<>();
            for (int i = 0; i < list1.size(); i++) {
                index1.put(list1.get(i).getKey(), i);
            }

            List<String> keys = new ArrayList<>();
            List<Integer> diffs = new ArrayList<>();

            int i = 0;
            for (Map.Entry<String, Double> e : list) {
                keys.add(e.getKey());
                Integer index = index1.get(e.getKey());
                int diff = topn;
                if (index != null)
                    diff = Math.abs(i - index);
                diffs.add(diff);
                i++;
            }

            System.out.println(cid + " categories: " + GsonSerializer.serialize(keys));
            System.out.println(cid + " data: " + GsonSerializer.serialize(diffs));
        }
    }
}
