package paper.tag.tagger;

import com.yeezhao.commons.util.FreqDist;

import java.io.IOException;
import java.util.Collection;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class AttrTagger implements Tagger {

    @Override
    public void load() throws IOException {

    }

    @Override
    public FreqDist<String> tag(String text) {
        return null;
    }

    @Override
    public FreqDist<String> tag(Collection<String> texts) {
        return null;
    }
}
