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
            tweet = ChineseCC.toShort(tweet);
        }
        return tweet;
    }

}
