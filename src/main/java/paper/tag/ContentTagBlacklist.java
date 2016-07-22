package paper.tag;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author lhfcws
 * @since 16/5/8
 */
public class ContentTagBlacklist extends HashSet<String> {
    // ============= SINGLETON ==========

    private static ContentTagBlacklist _singleton = null;

    public static ContentTagBlacklist getInstance() {
        if (_singleton == null)
            synchronized (ContentTagBlacklist.class) {
                if (_singleton == null) {
                    _singleton = new ContentTagBlacklist();
                }
            }
        return _singleton;
    }

    private ContentTagBlacklist() {
        this.addAll(Arrays.asList(blacks));
    }

    private static final String[] blacks = {
//             "媒体", "摄影", "分享", "美食", "动漫", "电影", "音乐", "旅游", "广州", "华南", "广东", "中大", "中山大学", "时尚", "学生", "青年", "新闻".
            "微博", "呵呵", "嘻嘻", "抽奖", "转发", "问题", "时间", "感觉", "偷笑","红包","回复", "馋嘴", "花心", "兔子", "奥特曼", "手机",
            "蜡烛", "博文", "蛋糕", "视频", "主页", "扇贝", "世界", "天气", "单词", "小时", "卧槽", "东西", "事情", "喵喵", "孩子", "生日"
    };
}
