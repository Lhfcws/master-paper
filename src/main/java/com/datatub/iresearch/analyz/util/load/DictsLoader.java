package com.datatub.iresearch.analyz.util.load;

import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.utils.SntAccurateModel;
import com.datatub.iresearch.analyz.util.KwFormatUtil;
import com.yeezhao.commons.util.DoubleDist;
import com.yeezhao.commons.util.ILineParser;
import com.yeezhao.commons.util.StringUtil;
import org.apache.hadoop.conf.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * @author lhfcws
 * @since 15/11/13.
 */
public class DictsLoader {
    private static Configuration conf = MLLibConfiguration.getInstance();
    private static final String DICT_SEP = "\t";

    /**
     * Format : word[\t]weight
     * Example: 喜欢[\t]3
     * @param name
     * @param force
     * @return
     */
    public static boolean loadWeightDict(String name, boolean force) {
        if (Dicts.weightDict.containsKey(name) && !force)
            return true;

        final DoubleDist<String> dict = new DoubleDist<String>();
        try {
            DualFileLoader.loadDualFileInLines(name, new ILineParser() {
                @Override
                public void parseLine(String s) {
                    s = KwFormatUtil.simpleFormat(s);
                    if (s.startsWith("#") || s.isEmpty()) return;
                    String[] sarr = s.split(DICT_SEP);
                    try {
                        double weight = Double.valueOf(sarr[1]);
                        dict.put(sarr[0], weight);
                    } catch (Exception ignore) {}
                }
            });
            Dicts.weightDict.put(name, dict);
            return true;
        } catch (Exception e) {
            Dicts.weightDict.put(name, dict);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Format : name[\t]alias1|alias2|...
     * Example: 谷歌[\t]google
     * @param name
     * @param force
     * @return
     */
    public static boolean loadNERDict(String name, boolean force) {
        if (Dicts.nerDict.containsKey(name) && !force)
            return true;

        final Set<String> dict = new HashSet<String>();
        try {
            DualFileLoader.loadDualFileInLines(name, new ILineParser() {
                @Override
                public void parseLine(String s) {
                    s = KwFormatUtil.simpleFormat(s);
                    if (s.startsWith("#") || s.isEmpty()) return;
                    String[] sarr = s.split(DICT_SEP);
                    dict.add(sarr[0]);
                    if (sarr.length > 1) {
                        for (String synonym : sarr[1].split(StringUtil.STR_DELIMIT_1ST)) {
                            dict.add(synonym);
                        }
                    }
                }
            });
            Dicts.nerDict.put(name, dict);
            return true;
        } catch (Exception e) {
            Dicts.nerDict.put(name, dict);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Format : word
     * Example: 单词
     * @param name
     * @param force
     * @return
     */
    public static boolean loadDict(String name, boolean force) {
        if (Dicts.dict.containsKey(name) && !force)
            return true;

        final Set<String> dict = new HashSet<String>();
        try {
            DualFileLoader.loadDualFileInLines(name, new ILineParser() {
                @Override
                public void parseLine(String s) {
                    s = KwFormatUtil.simpleFormat(s);
                    if (s.startsWith("#") || s.isEmpty()) return;
                    dict.add(s);
                }
            });
            Dicts.dict.put(name, dict);
            return true;
        } catch (Exception e) {
            Dicts.dict.put(name, dict);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load SntACcurateModel
     * @param force
     * @return
     */
    public static boolean loadSntAccModels(boolean force) {
        if (!Dicts.sntAccurateModelMap.isEmpty() && !force)
            return true;

        try {
            Dicts.sntAccurateModelMap = SntAccurateModel.loadAccModel();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
