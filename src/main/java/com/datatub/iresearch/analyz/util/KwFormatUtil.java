package com.datatub.iresearch.analyz.util;

import com.yeezhao.commons.util.ChineseCC;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: congzicun
 * Date: 5/11/13
 * Time: 4:14 PM
 */
public class KwFormatUtil {
    public static final String regexNumAndAlph = "\\w";
    public static final String regexMentionStr = "@[0-9a-zA-Z一-龥_-]*";
    public static final String regexTopicStr = "#(.+?)#|【(.+?)】";
    public static final String regexBookTitleStr = "《(.+?)》";
    public static final String regexQuotaStr = "\"(.+?)\"";
    public static final String regexEngPuncStr = "[!\"#$%&'()*+,-./:;<=>?@\\[\\\\\\]^_`\\{\\|\\}~]";
    public static final String regexChnPuncStr = "[《》（）&％￥＃@！｛｝【】？—、！；：。“”，…］［]";
    public static final String regexEmogiStr = "\\[.+?\\]";
    public static Pattern regexEnPunc = Pattern.compile(regexEngPuncStr);
    public static Pattern regexCnPunc = Pattern.compile(regexChnPuncStr);
    public static Pattern regexEmoticon = Pattern.compile(regexEmogiStr);
    public static Pattern regexTopic = Pattern.compile(regexTopicStr);
    public static Pattern regexMention = Pattern.compile(regexMentionStr);
    //    public static Pattern regexUrl = Pattern.compile("(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\\\".,<>?«»“”‘’]))");
    private static Pattern simpleUrlReg = Pattern.compile("http:[. /a-zA-Z0-9]+");

    public static String puncReplace(String ln) {
        ln = ln.replaceAll("。", ".").replaceAll("；", ";").replaceAll("！", "!").replaceAll("？", "?").replaceAll("，", ",")
                .replaceAll("（", "(").replaceAll("）", ")").replaceAll("\\.\\.", "…").replaceAll("～", "~").replaceAll("、", ",").replaceAll("“", "\"")
                .replaceAll("”", "\"").replaceAll("【", "[").replaceAll("】", "]").replaceAll("『", "[").replaceAll("』", "]")
                .replaceAll("＃", "#").replaceAll("\\(.*?\\)", "").replaceAll("：", ":");
        return ln;
    }

    public static String trimHTML(String tweet) {
        tweet = tweet.replaceAll("\\&[a-zA-Z]{1,10};", "");
        tweet = tweet.replaceAll("@[^>]*>", "");
        tweet = tweet.replaceAll("网页链接\\s*<", "<");
        tweet = tweet.replaceAll("<[^>]*>", "");
        return tweet;
    }

    public static String tweetFilter(String ln) {
//        Matcher matcher = regexMention.matcher(ln);
//        ln = matcher.replaceAll(" ").trim();
        Matcher matcher = regexEmoticon.matcher(ln);
        ln = matcher.replaceAll(" ");
        matcher = regexCnPunc.matcher(ln);
        ln = matcher.replaceAll(" ");
        matcher = simpleUrlReg.matcher(ln);
        ln = matcher.replaceAll(" ");
        matcher = regexEnPunc.matcher(ln);
        return matcher.replaceAll(" ").replaceAll("\\s+", " ").replaceAll(" +", " ");
    }

    public static String tweetFilterPart(String ln) {
        Matcher matcher = regexMention.matcher(ln);
        ln = matcher.replaceAll(" ").trim();
        matcher = regexEmoticon.matcher(ln);
        ln = matcher.replaceAll(" ");
        matcher = simpleUrlReg.matcher(ln);
        return matcher.replaceAll(" ").replaceAll("\\s+", " ");
    }

    public static String simpleFormat(String tweet) {
        if (tweet != null) {
            tweet = tweet.trim().toLowerCase();
            tweet = stripChinese(tweet);
            tweet = ChineseCC.toShort(tweet);
        }
        return tweet;
    }

    public static String stripChinese(String text) {
        StringBuilder sb = new StringBuilder();
        char[] ch = text.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            if (isChinese(ch[i]))
                sb.append(ch[i]);
            else
                sb.append(" ");
        }
        return sb.toString().replaceAll(" +", " ");
    }

    // 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    // 完整的判断中文汉字和符号
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    /****************
     * Test main
     */
    public static void main(String[] args) {
        String text = ":1,\"\uD83D\uDE04\":1,\"烟火\":1,\"滴滴\":1,\"总会\":1,\"能量\":1,\"光亮\":1,\"\uD83D\uDE0B\":1,\"制度\":1,\"国籍\":1,\"学会\":1,\"星座\":1,\"理由\":1,\"感觉\":1,\"\uD83D\uDE0D\":1,\"\uD83D\uDE19\":1,\"礼物\":1,\"落日\":1,\"\uD83D\uDE1C\":1,\"土豆\":1,\"\uD83D\uDE18\":1,\"中国好声音\":1,\"道理\":1,\"院校\":1,\"时刻\":1,\"状态\":1,\"世界\":1,\"凉台\":1,\"代金\":1,\"??\":1,\"病情\":1,\"父母\":1,\"沙包\":1,\"自助餐\":1,\"火大\":1,\"止疼药\":1,\"夕阳\":1,\"顺其自然\":1,\"你妹\":1,\"蘑菇\":1,\"黑色\":1,\"小孩子\":1,\"习惯\":1,\"家长\":1,\"加载\":1,\"心态\":1,\"帅呆\":1,\"界面\":1,\"表婶\":1,\"时代\":1,\"医生\":1,\"车次\":1,\"东西\":1,\"火锅\":1,\"关系\":1,\"阳台\":1,\"催泪\":1,\"双方\":1,\"文化\":1,\"吴昕\":1,\"手气\":1,\"牙医\":1,\"能耐\":1,\"片子\":1,\"美女\":1,\"视频\":1,\"英国\":1,\"兴趣\":1,\"玩意儿\":1,\"宝贝\":1,\"日子\":1,\"岁月\":1,\"深圳\":1,\"地方\":1,\"吉隆坡\":1,\"银联\":1,\"游客\":1,\"麻麻\":1,\"台湾\":1,\"小孩\":1,\"笑容\":1,\"蛋挞\":1,\"微博\":1,\"补习社\":1,\"粉丝\":1,\"黄牛\":1,\"信息流\":1,\"证据\":1,\"温泉\":1,\"\uD83D\uDC4C\":1,\"现金\":1,\"雅安\":1,\"身材\":1,\"双子\":1,\"新西兰\":1,\"范冰冰\":1,\"前途\":1,\"尼玛\":1,\"奖品\":1,\"早茶\":1,\"我家\":1,\"宝宝\":1,\"淡定\":1,\"风景线\":1,\"眼睛\":1,\"奶奶\":1,\"青春\":1,\"妈妈\":1,\"年代\":1,\"话费\":1,\"老娘\":1,\"素质\":1,\"游戏\":1,\"童鞋\":1,\"女友\":1,\"\uD83D\uDE4F\":1,\"梯田\":1,\"小时\":1,\"困难户\":1,\"屁用\":1,\"一日游\":1,\"吉利\":1,\"系统\":1,\"爷爷\":1,\"生命\":1,\"名车\":1,\"事情\":1,\"风衣\":1,\"喉咙\":1,\"变态\":1,\"包养\":1,\"马尔代夫\":1,\"精神\":1,\"胡子\":1,\"\uD83D\uDE2D\":1,\"\uD83D\uDE31\":1";
        System.out.println(stripChinese(text));
    }
}
