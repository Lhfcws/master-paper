package paper.tag.tagger;

import com.yeezhao.commons.util.FreqDist;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public interface Tagger extends Serializable {

    public void load() throws IOException;
    public FreqDist<String> tag(String text);
    public FreqDist<String> tag(Collection<String> texts);
}
