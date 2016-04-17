package paper.query;

import com.yeezhao.sealion.base.YZDoc;
import paper.community.model.WeiboUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author lhfcws
 * @since 16/1/4.
 */
public class DocWrapper {

    public static WeiboUser parseUserYZDoc(YZDoc yzDoc) {
        WeiboUser weiboUser = new WeiboUser();

//        weiboUser.id = url2mid(yzDoc.getId());
        weiboUser.id = yzDoc.getId();
//        weiboUser.area = getString(yzDoc, "area");
        weiboUser.area = getString(yzDoc, "省份");
//        weiboUser.city = getString(yzDoc, "city");
        weiboUser.city = getString(yzDoc, "城市");
//        weiboUser.company = getList(yzDoc, "company");
        weiboUser.company = getList(yzDoc, "公司");
//        weiboUser.school = getList(yzDoc, "school");
        weiboUser.school = getList(yzDoc, "学校");
//        weiboUser.sex = getInt(yzDoc, "sex");
        weiboUser.sex = getString(yzDoc, "性别");
//        weiboUser.birthYear = getInt(yzDoc, "birthYear");
        weiboUser.birthYear = getInt(yzDoc, "出生年月");
//        weiboUser.follows = getList(yzDoc, "follow");

        weiboUser.vType = getString(yzDoc, "认证类型");

        List<String> hobbies = getList(yzDoc, "爱好");
        if (hobbies != null) {
            for (String hobby : hobbies)
                weiboUser.tags.inc(hobby);
        }
        Collection<Map<String, Object>> tags = getNested(yzDoc, "标签");
        if (tags != null) {
            for (Map<String, Object> tag : tags) {
                String w = (String) tag.get("名称");
                weiboUser.tags.inc(w);
            }
        }

        return weiboUser;
    }

    public static String getString(YZDoc yzDoc, String key) {
        Object obj = yzDoc.get(key);
        return obj == null ? null : (String) obj;
    }

    public static Integer getInt(YZDoc yzDoc, String key) {
        Object obj = yzDoc.get(key);
        return obj == null ? null : (Integer) obj;
    }

    public static List<String> getList(YZDoc yzDoc, String key) {
        Object obj = yzDoc.get(key);
        return obj == null ? new ArrayList<String>() : (List<String>) obj;
    }

    private static Collection<Map<String, Object>> getNested(YZDoc doc, String key) {
        Object v = doc.get(key);
        if (v == null)
            return null;
        return (Collection<Map<String, Object>>) v;
    }

    /**
     * 62进制值转换为10进制
     *
     * @param {String} int62 62进制值
     * @return {int} 10进制值
     */
    public static String int62to10(String int62, boolean needPending) {
        int res = 0;
        int base = 62;
        for (int i = 0; i < int62.length(); ++i) {
            res *= base;
            char charofint62 = int62.charAt(i);
            if (charofint62 >= '0' && charofint62 <= '9') {
                int num = charofint62 - '0';
                res += num;
            } else if (charofint62 >= 'a' && charofint62 <= 'z') {
                int num = charofint62 - 'a' + 10;
                res += num;
            } else if (charofint62 >= 'A' && charofint62 <= 'Z') {
                int num = charofint62 - 'A' + 36;
                res += num;
            } else {
                System.err.println("this is not a 62base number");
                return null;
            }
        }
        String resstr = String.valueOf(res);
        if (needPending) {
            while (resstr.length() < 7) {
                resstr = "0" + resstr;
            }
        }
        return resstr;
    }

    public static String url2mid(String url) {
        String mid = "";
        int index = url.length();
        while (index > 0) {
            String substr = "";
            if (index - 4 < 0) {
                substr = url.substring(0, index);
                mid = int62to10(substr, false) + mid;
            } else {
                substr = url.substring(index - 4, index);
                mid = int62to10(substr, true) + mid;
            }
            index -= 4;
        }
        return mid;
    }
}
