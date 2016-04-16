package com.datatub.iresearch.analyz.base;

/**
 * Classifier constants
 *
 * @author Arber
 */
public class HornbillConsts {

    public static final String STR_ERROR = "ERROR!";
    public static final String OTHER_TYPE = "其他";


    // Hornbill config
    public static final String PARAM_HORNBILL_PORT = "hornbill.analyz.rpc.port";
    public static final String PARAM_WORD2VECTOR_PORT = "hornbill.word2vec.rpc.port";
    public static final String HORNBILL_RPC_SERV = "HornbillRpc";
    public static final String WORD2VECTOR_RPC_SERV = "Word2VectorRpc";
    public static final int HORNBILL_DEFAULT_PORT = 53433;
    public static final int WORD2VECTOR_DEFAULT_PORT = 14444;
    public static final String CLASSIFIER_TYPE_CONFIG = "classifier";
    public static final String CLASSIFIER_CONFIG_ID = "id";
    public static final String CLASSIFIER_CONFIG_CLASSPATH = "classpath";
    public static final String CLASSIFIER_CONFIG_MODEL = "model";
    public static final String CLASSIFIER_CONFIG_DESCRIPTION = "description";
    public static final String CLASSIFIER_CONFIG_DOCUMENTTYPE = "documenttype";
    public static final String REDIS_SERVER = "redis.server";

    /**
     * FILE NAME
     */
    public static final String FILE_CLASSIFIER_CONFIG = "classifier-id-config.xml";
    public static final String SentimentFeatureFile = "sentiment.feature.file";
    public static final String CONFIG_AD_MODEL = "twt.ad.model";
    public static final String FILE_NEWS_AD_MODEL = "news.ad.model";
    public static final String FILE_EMOTWT_MODEL = "emotwt.model";
    public static final String FILE_CATEGORY_MODEL = "category.model";
    public static final String FILE_KW_CATEGORY_MODEL = "kw.category.model";
    public static final String FILE_UNKNOWN_WORD_MODEL = "unknown.word.model";
    public static final String FILE_SENTIMENT_NEWS_MODEL = "sentiment.news.model";
    public static final String FILE_WORD2VECTOR_MODEL = "huge.bin";
    public static final String STOPWORDS = "stopwords";
    public static final String FILE_KEYWORD_IDF = "idf.txt";
    public static final String FILE_PRODUCT_FEATURE = "product.feature.file";
    public static final String FILE_ACTOR_KB = "actor.kb.file";
    public static final String FILE_ACTOR_NICKNAME_KB = "actor.nickname.kb.file";
    public static final String FILE_SENTIMENT_NEUTRAL_MODEL = "sentiment.neutral.model";


    //Sentiment
    public static final String CONFIG_SENTIMENT_NEGWORD = "sentiment.negative";
    public static final String CONFIG_SENTIMENT_POSWORD = "sentiment.positive";
    public static final String CONFIG_SENTIMENT_NEGEMOTION = "sentiment.negemotion";
    public static final String CONFIG_SENTIMENT_POSEMOTION = "sentiment.posemotion";
    public static final String CONFIG_SENTIMENT_ROUGHWORD = "sentiment.rough";
    public static final String CONFIG_SENTIMENT_THRESHOLD = "sentiment.lr.threshold";
    public static final String CONFIG_SENTIMENT_NEG = "neg";
    public static final String CONFIG_SENTIMENT_POS = "pos";
    public static final String CONFIG_SENTIMENT_NO = "no";

    // NER
    public static final String NER_TYPE_AREA = "AREA";
    public static final String NER_TYPE_FAME = "FAME";
    public static final String NER_TYPE_ORG = "ORG";
    public static final String NER_TYPE_COMMODITY = "COMMODITY";
    public static final String NER_TYPE_OTHERS = "null";
    public static final String APPELLATION = "appellation";
    public static final String COMMON_FAME = "common.fame";
    public static final String CLASS_FAME = "class.fame";
    public static final String COMMON_SURNAME = "common.surname";
    public static final String COMMON_ORG = "common.org";
    public static final String CLASSLIST_FAME = "classlist.fame";
    public static final String FILE_NER_AREA = "ner.file.area";
    public static final String FILE_KNOWL_BASE = "kb/knowl-base.txt";
    public static final String FILE_AMBIGUITY = "ner/ambiguity.txt";
    public static final String STANFORD_NER_MODEL = "ner.file.stanford";
    public static final String STANFORD_NER_MODEL_EN_3CLASS = "ner.file.stanford.en.3class";

    // Word Community Seeds
    public static final String SEED_PEOPLE_GROUP = "seed.people.group";
    //KEYWORD EXTRACTION
    public static final String FILE_KEYWORD_CORPUS = "keywordExtr_corpus.txt";
    public static final String PARAM_KEYWORD_TOPN = "hornbill.analyz.keyword.topn";

    //Keyword Classification
    public static final Float CLASS_SCORE_THRESHOLD = 0.25f;

    //Knowledge base
    public static final String FILE_MUSIC_KB = "kb/musics.txt";
    public static final String FILE_READING_KB = "kb/readings.txt";
    public static final String FILE_MOVIE_KB = "kb/movies.txt";


    public static final String KB_MUSIC = "kb_music.txt";
    public static final String KB_GAME = "kb_game.txt";
    public static final String KB_SOFTWARE = "kb_software.txt";
    public static final String KB_ANIME = "kb_anime.txt";

    public static final String DPI_RECORD_DELIMITER = "\\$\\$\\$\\$";
    public static final String PARAM_CRAWL_IN_PATH = "crawlInputPath";
    public static final String PARAM_CRAWL_OUT_PATH = "crawlOutputPath";
    public static final String PARAM_GEN_IN_PATH = "genInputPath";
    public static final String PARAM_GEN_OUT_PATH = "genOutputPath";
    public static final String PARAM_ANALYZ_IN_PATH = "analyzInputPath";
    public static final String PARAM_ANALYZ_OUT_PATH = "analyzOutputPath";
    public static final String PARAM_SYNC_IN_PATH = "syncInputPath";
    public static final String PARAM_SYNC_OUT_PATH = "syncOutputPath";
    public static final String PARAM_SCORE_THRESHOLD = "scoreThreshold";
    public static final String PARAM_FREQ_THRESHOLD = "freqThreshold";
    public static final String PARAM_ENTROPY_THRESHOLD = "entropyThreshold";
    public static final String PARAM_START_INDEX = "startIndex";
    public static final String PARAM_END_INDEX = "endIndex";

    // segmentation keyword file
    public static final String FILE_IDF = "idf.txt";
    public static final String FILE_META_LABEL = "hanuman_metalabel.txt";
    public static final String FILE_WORDS_LIB = "366w_words.txt";

    //classifier Ids consts
    public static final String CLASSIFIER_ADTWT = "ADTWT";//短文本广告过滤，返回值：0代表非广告，1代表广告
    public static final String CLASSIFIER_ADNEWS = "ADNEWS"; //长文本广告过滤（基于规则，词频），返回值：0代表非广告，1代表广告
    public static final String CLASSIFIER_KWCLASS = "KWCLASS"; //短文本分类，返回值：现有财经，娱乐，体育，科技等；未知分类返回UNKNOWN
    public static final String CLASSIFIER_CATEGORY = "CATEGORY"; //标签分类器，返回值：现有digital\,ent\,economy\,sport\,tech\,health\,politics等;未知分类返回UNKNOWN
    public static final String CLASSIFIER_SHORT_CATEGORY = "SHORT_CATEGORY"; //标签分类器，返回值：现有digital\,ent\,economy\,sport\,tech\,health\,politics等;未知分类返回UNKNOWN
    public static final String CLASSIFIER_SNTNEWS = "SNTNEWS"; //长文本情感分类器，返回值：-1代表负面，0代表中性，1代表正面
    public static final String CLASSIFIER_SNTTWT = "SNTTWT"; //短文本情感分类器，返回值：-1代表负面，0代表中性，1代表正面
    public static final String CLASSIFIER_SNTECOMM = "SNTECOMM"; //短文本情感分类器，返回值：-1代表负面，0代表中性，1代表正面
    public static final String CLASSIFIER_KEYWORD = "KEYWORD"; //抽取文本关键词，返回值：15个关键词，用‘#’隔开；关键词和权重间用“：”隔开；eg: KEYWORD$大雨:8#广州:5#
    public static final String CLASSIFIER_SUMMARY = "SUMMARY"; //抽取文章摘要，返回值：字符串
    public static final String CLASSIFIER_NER = "NER"; //实体识别，覆盖地名，人名和商品，返回值：每个词用‘#’隔开；实体词与相应领域间用‘^’隔开，领域目前有地名AREA，人名FAME，商品名COMMODITY，组织名ORG；eg:NER$广州^AREA#下#大雨#了#，#张伟^FAME#不#高兴#
    public static final String CLASSIFIER_NER_FAME = "NER_FAME"; //人名实体识别，返回值：每个词用‘#’隔开；
    public static final String CLASSIFIER_NER_EN = "NER_EN"; //english实体识别，返回值：每个词用‘#’隔开；
    public static final String CLASSIFIER_NER_AREA = "NER_AREA"; //地区实体识别，返回值：每个词用‘#’隔开；
    public static final String CLASSIFIER_NER_ORG = "NER_ORG"; //组织实体识别，返回值：每个词用‘#’隔开；
    public static final String CLASSIFIER_NER_COMM = "COMM"; //COMM实体识别，返回值：每个词用‘#’隔开；
    public static final String CLASSIFIER_NER_SIMCOMM = "NER_SIMCOMM"; //COMM实体识别简易版，返回值：每个词用‘#’隔开；
    public static final String CLASSIFIER_PEOPLE_GROUP = "PEOPLE_GROUP"; //按关键词标签对人分类
    public static final String CLASSIFIER_SYNONYM = "SYNONYM"; //按关键词标签对人分类
    public static final String CLASSIFIER_URL = "URL"; //URL综合翻译
    public static final String CLASSIFIER_URL_WX = "URL_WX"; //wx URL综合翻译
    public static final String CLASSIFIER_URL_APP = "URL_APP"; //app综合翻译
    public static final String CLASSIFIER_URL_APPURL = "URL_APPURL"; //app + URL综合翻译
    public static final String CLASSIFIER_CLUSTER = "CLUSTER"; // cluster聚类
    public static final String CLASSIFIER_CLUSTER_SPACE_ID = "CLUSTER_SPACE_ID"; // 创建聚类空间ID
    public static final String CLASSIFIER_SEGWORD = "SEGWORD"; // 文本分词

    // OTHERS
    public static final String SERV_TYPE = "rpc";
    public static final String ORG = "yeezhao";
    public static final String APP = "hornbill";

    public enum Sentiment {
        POSITIVE(1), NEGATIVE(-1), NEUTRAl(0);
        private int senticode;

        private Sentiment(int code) {
            senticode = code;
        }

        public static Sentiment fromValue(int code) {
            for (Sentiment sentiment : Sentiment.values()) {
                if (sentiment.getValue() == code) {
                    return sentiment;
                }
            }
            return null;
        }

        public int getValue() {
            return senticode;
        }
    }

}
