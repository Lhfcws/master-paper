package com.datatub.iresearch.analyz.text.sentiment.shorttext.utils;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: congzicun
 * Date: 5/11/13
 * Time: 4:14 PM
 */
public class WeiboFormatUtil {
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
    public static Pattern simpleUrlReg = Pattern.compile("http[s]?:[. /a-zA-Z0-9]+");
    //    private WeiboFilterOp _adFilter = null;
    private static String[] urlWords = {"视频", "投票", "博文", "分享自"};
    private static String[] adWords = {"关注", "转发", "获取", "机会", "赢取", "推荐",
            "活动", "好友", "支持", "话题", "地址", "赢", "抽奖", "好运", "中奖", "红包"
            , "宝贝来源", "抢购", ">>", "宝贝地址", "→", "浏览地址", "速戳围观", "宝贝链接", "售量", "猛戳"};

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
        Matcher matcher = regexMention.matcher(ln);
        ln = matcher.replaceAll(" ").trim();
        matcher = regexEmoticon.matcher(ln);
        ln = matcher.replaceAll(" ");
        matcher = regexCnPunc.matcher(ln);
        ln = matcher.replaceAll(" ");
        matcher = simpleUrlReg.matcher(ln);
        ln = matcher.replaceAll(" ");
        matcher = regexEnPunc.matcher(ln);
        return matcher.replaceAll(" ").replaceAll("\\s+", " ");
    }

    public static String tweetFilterPart(String ln) {
        Matcher matcher = regexMention.matcher(ln);
        ln = matcher.replaceAll(" ").trim();
        matcher = regexEmoticon.matcher(ln);
        ln = matcher.replaceAll(" ");
        matcher = simpleUrlReg.matcher(ln);
        return matcher.replaceAll(" ").replaceAll("\\s+", " ");
    }

    public static String lower(String tweet) {
        tweet = StringUtils.lowerCase(tweet);
        return tweet;
    }

    public static String filterSpChars(String tweet) {
        tweet = tweet.trim().replaceAll("//\\s*@.+", "");
        tweet = WeiboFormatUtil.puncReplace(tweet);
        tweet = WeiboFormatUtil.lower(tweet);
        return tweet;
    }

    public static boolean simpleIsAD(String tweet) throws Exception {
        double adWordCnt = 0;
        Matcher matcher = simpleUrlReg.matcher(tweet);
        boolean hasUrl = false;

        while (matcher.find()) {
            adWordCnt += 1;
            hasUrl = true;
        }
        if (hasUrl) {
            for (String word : urlWords) {
                if (tweet.contains(word))
                    return true;
            }
        }

        for (String adWord : adWords) {
            if (tweet.contains(adWord))
                adWordCnt++;
        }

        return adWordCnt >= 2;
    }
}
