package paper.test;

import com.yeezhao.commons.util.FreqDist;
import org.junit.Test;
import paper.tag.tagger.ContentTagger;

/**
 * @author lhfcws
 * @since 16/4/20
 */
public class TestContentTagger {
    @Test
    public void testLoad() {
        ContentTagger contentTagger = ContentTagger.getInstance();
        FreqDist<String> freqDist = contentTagger.tag("周杰伦是个娱乐摄影师");
        System.out.println(freqDist);
    }
}
