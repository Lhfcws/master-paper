package paper.tag;

import com.yeezhao.commons.util.DoubleDist;

import java.io.Serializable;
import java.util.*;

/**
 * @author lhfcws
 * @since 16/7/15
 */
public class TfIdfCalculator implements Serializable {
    Map<Integer, Doc> docs = new HashMap<>();
    DoubleDist<String> idf = new DoubleDist<>();

    public void addDoc(int id, DoubleDist<String> doc) {
        docs.put(id, new Doc(id, doc));
    }

    public Doc getDoc(int id) {
        return docs.get(id);
    }

    public Collection<Doc> getDocs() {
        return docs.values();
    }

    protected void calcTf() {
        int docSize = docs.size();
        for (Doc doc : docs.values()) {
            double sum = doc.words.sum();
            double diff = sum / docSize;
            if (sum > 0)
                for (Map.Entry<String, Double> e : doc.words.entrySet()) {
                    // the same as: f / (sum * diff)
                    doc.tf.put(e.getKey(), (e.getValue() * docSize) / (sum * sum));
                }
        }
    }

    protected void calcIdf() {
        int docSize = docs.size();
        idf.clear();

        for (Doc doc : docs.values()) {
            for (String word : doc.words.keySet()) {
                idf.inc(word);
            }
        }

        Set<String> set = new HashSet<>(idf.keySet());
        for (String word : set) {
            double score = docSize / (idf.get(word) + 1);
            idf.put(word, score);
        }
        set.clear();
    }

    public void calc() {
        calcTf();
        calcIdf();

        for (Doc doc : docs.values()) {
            for (Map.Entry<String, Double> e : doc.tf.entrySet()) {
                double score = e.getValue() * idf.get(e.getKey());
                doc.tfidf.inc(e.getKey(), score);
            }
        }
    }

    /**
     * Tf Idf cal Doc
     */
    public static class Doc {
        public int id;
        DoubleDist<String> tf = new DoubleDist<>();
        DoubleDist<String> words = new DoubleDist<>();
        DoubleDist<String> tfidf = new DoubleDist<>();

        Doc(int id, DoubleDist<String> words) {
            this.id = id;
            this.words = words;
        }

        boolean containsWord(String word) {
            return words.containsKey(word);
        }

        public DoubleDist<String> getTfIdf() {
            return tfidf;
        }

        public List<Map.Entry<String, Double>> topn(int n) {
            if (n < tfidf.size()) {
                return tfidf.sortValues(false).subList(0, n);
            } else
                return tfidf.sortValues(false);
        }
    }
}
