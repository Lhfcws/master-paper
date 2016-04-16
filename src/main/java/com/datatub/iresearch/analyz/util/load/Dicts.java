package com.datatub.iresearch.analyz.util.load;

import com.datatub.iresearch.analyz.text.sentiment.shorttext.utils.SntAccurateModel;
import com.yeezhao.commons.util.DoubleDist;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global storage dicts and models.
 * @author lhfcws
 * @since 15/11/13.
 */
public class Dicts {
    public static Map<String, Set<String>> dict = new ConcurrentHashMap<String, Set<String>>();
    public static Map<String, Set<String>> nerDict = new ConcurrentHashMap<String, Set<String>>();
    public static Map<String, SntAccurateModel> sntAccurateModelMap = new ConcurrentHashMap<String, SntAccurateModel>();
    public static Map<String, DoubleDist<String>> weightDict = new ConcurrentHashMap<String, DoubleDist<String>>();


    // ************** Names
    public static final String DIC_D = "common/dic.txt";
    public static final String CH_DIC_D = "common/chinese_dict.txt";
    public static final String STOPWORD_D = "common/stopwords.txt";
    public static final String POS_D = "sentiment/positive.txt";
    public static final String POS_WD = "sentiment/positive.weight.txt";
    public static final String NEG_D = "sentiment/negative.txt";
    public static final String NEG_WD = "sentiment/negative.weight.txt";
    public static final String NEUT_M = "sentiment/neutral_tweet_model.txt";
    public static final String POSEM_D = "sentiment/posemotion.txt";
    public static final String NEGEM_D = "sentiment/negemotion.txt";
    public static final String ROUGH_D = "sentiment/rough.txt";

    public static final String ORG_SUFFIX = "ner/org.suffix.txt";
    public static final String ORG_EDU = "ner/org.edu.txt";
    public static final String ORG_GOV = "ner/org.gov.txt";
    public static final String ORG_NGO = "ner/org.ngo.txt";
    public static final String ORG_COMMON = "ner/org.common.txt";
    public static final String ORG_BLACKLIST = "ner/org.blacklist.txt";
    public static final String FAME_CH_SURNAME = "ner/fame.surname.txt";
    public static final String FAME_COMMON = "ner/fame.common.txt";
    public static final String FAME_BLACKLIST = "ner/fame.blacklist.txt";
    public static final String AREA_SUFFIX = "ner/area.suffix.txt";
    public static final String AREA_ADM = "ner/area.adm.txt";
    public static final String AREA_GEO = "ner/area.geo.txt";
    public static final String AREA_COMMON = "ner/area.common.txt";
    public static final String AREA_BLACKLIST = "ner/area.blacklist.txt";
    public static final String COMM_REAL = "ner/commodity.real.txt";
    public static final String COMM_VIRTUAL = "ner/commodity.virtual.txt";
    public static final String COMM_COMMON = "ner/commodity.common.txt";
    public static final String COMM_BLACKLIST = "ner/commodity.blacklist.txt";
    public static final String KEYWORD_BLACKLIST = "ner/keyword.blacklist.txt";
}
