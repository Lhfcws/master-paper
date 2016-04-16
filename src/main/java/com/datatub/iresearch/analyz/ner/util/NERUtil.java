package com.datatub.iresearch.analyz.ner.util;

import com.datatub.iresearch.analyz.base.HornbillConsts;
import com.datatub.iresearch.analyz.util.KwFormatUtil;
import com.yeezhao.commons.util.Pair;
import com.yeezhao.commons.util.StringUtil;
import org.ansj.domain.Term;

import java.util.*;
import java.util.regex.Matcher;

public class NERUtil {
    public static List<NERWord> sort(List<NERWord> nerWords) {
        Collections.sort(nerWords, new Comparator<NERWord>() {
            @Override
            public int compare(NERWord o1, NERWord o2) {
                int v = o1.getStart() - o2.getStart();
                if (v > 0)
                    return 1;
                else if (v < 0)
                    return -1;
                return v;
            }
        });
        return nerWords;
    }

    public static void classPut(Map<String, String> map, String entity, String klass) {
        String value = map.get(entity);
        if (value == null) {
            value = klass;
        } else {
            value = value + StringUtil.DELIMIT_1ST + klass;
        }
        map.put(entity, value);
    }

    public static void kbPut(Map<String, Set<String>> map, String key, String setItem) {
        Set<String> set;
        if (!map.containsKey(key)) {
            set = new HashSet<String>();
            set.add(setItem);
            map.put(key, set);
        } else {
            set = map.get(key);
            set.add(setItem);
            map.put(key, set);
        }
    }

    public static String filter(String rawline) {
        // Filter url
        rawline = rawline.replaceAll("http[s]?:[. /a-zA-Z0-9-_]+", "").trim();
        rawline = rawline.replaceAll("转发微博", "");
        rawline = rawline.replaceAll("[mark|Mark|马克]//", "//");
        rawline = rawline.replaceAll("转发", "");
        rawline = rawline.replaceAll("我在[:：]", "");
        rawline = rawline.replaceAll("•-_", " ").trim();
        rawline = rawline.replaceAll("//@[^:]*:", "");
        rawline = rawline.replaceAll("@", "").trim();
        rawline = rawline.replaceAll("\\[[^]]*]", "");
        rawline = rawline.replaceAll("[‘”“’]", "");
        rawline = rawline.replaceAll("[:：\\|]+", " ").trim();
        rawline = rawline.replaceAll("via[ ]?[^ ]* ", "");
        rawline = rawline.replaceAll("via[ ]?[^ ]*$", "");
        rawline = rawline.replaceAll("[①②③④⑤⑥⑦⑧❶❷❸❹❺❻❼❽❾❿]", " ");
        rawline = rawline.replaceAll("[ ]+", " ").trim();

        return rawline;
    }

    public static <T> T safeget(List<T> list, int index) {
        if (index < 0 || index >= list.size())
            return null;
        return list.get(index);
    }

    /**
     * 根据ner结果字符串，转换成实体词映射, 完全保留原字符串
     *
     * @param nerStr  ner结果字符串，eg：Jack^FAME#is#happy#
     * @param content 原字符串 , 如为null，则不保证字符串完全一致
     * @return 实体词映射 key:ner类型，value:该类型对应的实体词集合
     */
    public static Map<String, List<String>> genNERWordMap(String content, String nerStr) {
        if (nerStr == null || nerStr.isEmpty()) {
            return new HashMap<String, List<String>>();
        }
        Map<String, List<String>> nerWordsMap = new HashMap<String, List<String>>();
        //nerWord,eg:Jack^FAME
        String[] nerWords = nerStr.split(StringUtil.STR_DELIMIT_3RD);

        //线性扫描， 确保 University of Central Florida 这些词实体合并成一个实体
        String lastWord = "";
        String lastWordType = "";
        int index = 0;
        for (String nerWord : nerWords) {
            //wordSplit[0] --> 实体词，wordSplit[1]--> ner类型
            String[] wordSplit = nerWord.split(StringUtil.STR_DELIMIT_4TH);

            String wordType;
            if (wordSplit.length < 2) {
                wordType = HornbillConsts.NER_TYPE_OTHERS;
            } else {
                wordType = wordSplit[1];
            }

            //对最后一个词做处理，如跟前词构成实体则一起写入map，否则，分别写入map
            if (index == (nerWords.length - 1)) {
                if (wordType.equals(lastWordType)) {
                    Matcher matcher = KwFormatUtil.regexEnPunc.matcher(wordSplit[0]);
                    if (matcher.find()) {
                        putWord2NERMap(nerWordsMap, wordType, lastWord.trim() + wordSplit[0]);
                    } else {
                        putWord2NERMap(nerWordsMap, wordType, lastWord + wordSplit[0]);
                    }
                } else if (nerWords.length != 1) {
                    putWord2NERMap(nerWordsMap, wordType, wordSplit[0]);
                    putWord2NERMap(nerWordsMap, lastWordType, lastWord.substring(0, lastWord.length() - 1));
                } else {
                    //nerWords大小为1
                    putWord2NERMap(nerWordsMap, wordType, wordSplit[0]);
                }
                continue;
            }

            //判断是否跟前一个词一致，一致则等下一个词，不一致则把前词放入map
            if (wordType.equals(lastWordType)) {
                Matcher matcher = KwFormatUtil.regexEnPunc.matcher(wordSplit[0]);
                if (matcher.find()) {
                    lastWord = lastWord.trim() + wordSplit[0] + " ";
                } else {
                    lastWord += wordSplit[0] + " ";
                }
            } else {
                //第一个词的前词总为空
                if (lastWord.isEmpty()) {
                    lastWord = wordSplit[0] + " ";
                    lastWordType = wordType;
                    index++;
                    continue;
                }

                putWord2NERMap(nerWordsMap, lastWordType, lastWord.trim());

                lastWord = wordSplit[0] + " ";
                lastWordType = wordType;
            }
            index++;
        }
        return nerWordsMap;
    }

    /**
     * 根据ner结果字符串，转换成实体词映射
     *
     * @param nerStr ner结果字符串，eg：Jack^FAME#is#happy#
     * @return 实体词映射 key:ner类型，value:该类型对应的实体词集合
     */
    public static Map<String, List<String>> genNERWordMap(String nerStr) {
        return genNERWordMap(null, nerStr);
    }

    public static Map<String, List<Pair<String, Integer>>> genNERWordMapCHN(String nerStr) {
        if (nerStr == null || nerStr.isEmpty()) {
            return new HashMap<String, List<Pair<String, Integer>>>();
        }
        Map<String, List<Pair<String, Integer>>> nerWordsMap = new HashMap<String, List<Pair<String, Integer>>>();
        //nerWord,eg:Jack^FAME^1
        String[] nerWords = nerStr.split(StringUtil.STR_DELIMIT_3RD);

        String lastWordType = "";
        String lastWord = "";
        String lastWordPosition = "";
        for (String nerWord : nerWords) {
            //wordSplit[0] --> 实体词，wordSplit[1]--> ner类型,wordSplit[2]--> 实体词开始位置
            String[] wordSplit = nerWord.split(StringUtil.STR_DELIMIT_4TH);
            String word = wordSplit[0];
            String wordType;
            String wordPosition = "-1";
            if (wordSplit.length < 2) {
                wordType = HornbillConsts.NER_TYPE_OTHERS;
            } else {
                wordType = wordSplit[1];
                wordPosition = wordSplit[2];
            }

            putWord2NERMapCHN(nerWordsMap, wordPosition, wordType, word, lastWordPosition, lastWordType, lastWord);
            lastWordPosition = wordPosition;
            lastWordType = wordType;
            lastWord = word;
        }

        return nerWordsMap;
    }

    public static void putWord2NERMapCHN(Map<String, List<Pair<String, Integer>>> nerWordsMap, String wordPosition, String wordType,
                                         String word, String lastWordPosition, String lastWordType, String lastWord) {
        if (!nerWordsMap.containsKey(wordType)) {
            nerWordsMap.put(wordType, new LinkedList<Pair<String, Integer>>());
        }
        List<Pair<String, Integer>> wordList = nerWordsMap.get(wordType);

        if (wordList.isEmpty()) {
            wordList.add(new Pair<String, Integer>(word, Integer.parseInt(wordPosition)));
            return;
        }

        if (!wordType.equals(lastWordType)) {
            wordList.add(new Pair<String, Integer>(word, Integer.parseInt(wordPosition)));
            return;
        } else if (!wordType.equals("ORG")) {
            wordList.add(new Pair<String, Integer>(word, Integer.parseInt(wordPosition)));
            return;
        } else {
            String mergedWord = lastWord + word;
            wordList.remove(new Pair<String, Integer>(lastWord, Integer.parseInt(lastWordPosition)));
            wordList.add(new Pair<String, Integer>(mergedWord, Integer.parseInt(lastWordPosition)));
        }

    }

    public static void putWord2NERMap(Map<String, List<String>> nerWordsMap, String wordType, String word) {
        List<String> wordList;
        wordList = nerWordsMap.get(wordType);
        if (wordList == null) {
            wordList = new LinkedList<String>();
        }
        wordList.add(word);
        nerWordsMap.put(wordType, wordList);
    }

    public static boolean checkLatinSeg(String sentence, String keyword,
                                        int start, int end) {
        if (start > 0 && isLatin(keyword.charAt(0))
                && isLatin(sentence.charAt(start - 1))) {
            return false;
        } else if (end < sentence.length() - 1
                && isLatin(keyword.charAt(keyword.length() - 1))
                && isLatin(sentence.charAt(end))) {
            return false;
        } else
            return true;
    }

    public static boolean isLatin(char c) {
        if (c >= 'a' && c <= 'z')
            return true;
        else if (c >= 'A' && c <= 'Z')
            return true;
        else if (c >= '0' && c <= '9')
            return true;
        else
            return false;
    }

    public static NERWord ansjTerm2NERWord(Term term) {
        return new NERWord(term.getOffe(), term.getOffe() + term.getName().length(), term.getName());
    }

    public static List<NERWord> ansjTerms2NERWords(List<Term> terms) {
        List<NERWord> nerWordList = new LinkedList<NERWord>();
        if (terms != null)
            for (Term term : terms) {
                nerWordList.add(ansjTerm2NERWord(term));
            }
        return nerWordList;
    }
}
