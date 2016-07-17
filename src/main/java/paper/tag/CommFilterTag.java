package paper.tag;

import com.yeezhao.commons.util.DoubleDist;
import com.yeezhao.commons.util.FreqDist;

import java.util.*;

/**
 * Created by lhfcws on 16/7/17.
 */
public class CommFilterTag {
    public static List<List<Map.Entry<String, Double>>> filter(List<DoubleDist<String>> doubleDists, int topn) {
//        List<List<Map.Entry<String, Double>>> list = new ArrayList<>();
//        for (DoubleDist<String> doubleDist : doubleDists) {
//            list.add(doubleDist.sortValues(false));
//        }

//        while (true) {
//            FreqDist<String> counter = new FreqDist<>();
//            for (int i = 0; i < list.size(); i++) {
//                List<Map.Entry<String, Double>> l = list.get(i);
//                for (int j = 0; j < l.size(); j++) {
//                    if (j > topn) break;
//                    Map.Entry<String, Double> entry = l.get(j);
//                    counter.inc(entry.getKey());
//                }
//            }
//
//            Set<String> toRemove = new HashSet<>();
//            for (Map.Entry<String, Integer> e : counter.entrySet()) {
//                if (e.getValue() > list.size() / 3) {
//                    toRemove.add(e.getKey());
//                }
//            }
//
//            if (!toRemove.isEmpty())
//                for (int i = 0; i < list.size(); i++) {
//                    List<Map.Entry<String, Double>> l = list.get(i);
//                    int j = 0;
//                    while (j < topn && j < l.size()) {
//                        Map.Entry<String, Double> e = l.get(j);
//                        if (toRemove.contains(e.getKey())) {
//                            l.remove(j);
//                        } else {
//                            j++;
//                        }
//                    }
//                }
//            else
//                break;
//        }

        List<List<Map.Entry<String, Double>>> ret = new ArrayList<>();
        for (int i = 0; i < doubleDists.size(); i++) {
            List<Map.Entry<String, Double>> l = doubleDists.get(i).sortValues(false);

            if (topn < l.size())
                ret.add(l.subList(0, topn));
            else
                ret.add(l);
        }
        return ret;
    }
}
