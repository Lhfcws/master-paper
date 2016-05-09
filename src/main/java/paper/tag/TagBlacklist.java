package paper.tag;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author lhfcws
 * @since 16/5/8
 */
public class TagBlacklist extends HashSet<String> {
    // ============= SINGLETON ==========

    private static TagBlacklist _singleton = null;

    public static TagBlacklist getInstance() {
        if (_singleton == null)
            synchronized (TagBlacklist.class) {
                if (_singleton == null) {
                    _singleton = new TagBlacklist();
                }
            }
        return _singleton;
    }

    private TagBlacklist() {
        this.addAll(Arrays.asList(blacks));
    }

    private static final String[] blacks = {
            "美食", "动漫", "电影", "音乐", "旅游", "广州", "华南", "广东", "中大", "中山大学", "时尚", "学生", "青年", "新闻"
    };
}
