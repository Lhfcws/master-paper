package com.datatub.iresearch.analyz.ner.recognizer;

import com.datatub.iresearch.analyz.base.HornbillConsts;
import com.datatub.iresearch.analyz.ner.util.NERUtil;
import com.datatub.iresearch.analyz.ner.util.NERWord;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import com.datatub.iresearch.analyz.util.SegUtil;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.datatub.iresearch.analyz.util.load.DictsLoader;
import com.yeezhao.commons.util.ILineParser;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 组织命名实体识别
 *
 * @author lhfcws
 * @since 15/11/30.
 */
public class OrgRecognizer extends AbstractNERRecognizer {
    // ======== static members
//    protected static Map<String, Pattern> SUFFIXES = new HashMap<String, Pattern>();
    protected static Pattern suffixPattern;

//    static {
//        SUFFIXES.put("COM", Pattern.compile(
//                "^[ ]+[总部|集团|公司]"
//        ));
//        SUFFIXES.put("GOV", Pattern.compile(
//                "[^ ]+(管委会|居委会|安委会|委员会|管理局|事务局|管理处|总署|办公室|办事处|秘书处|工作组|调查组|研究院|设计院|工会|党组织|省政府|市政府|区政府|县政府|州政府|村政府)"
//        ));
//        SUFFIXES.put("EDU", Pattern.compile(
//                "[^ ]+(大学|中学|小学|学院|职业学校|驾校"
//                        + ")"
//        ));
//        SUFFIXES.put("NGO", Pattern.compile(
//                "[^ ]+(协会|研究所|研究室|工作室|研究会|联盟|基金会|联合会|" +
//                        "球队|羽协|乒协|代表队|促进会|服务队|足协|篮协|作协|消协"
//                        + ")"
//        ));
//    }

    // ======== members

    // ======== Constructor
    public OrgRecognizer() {
        initDicts();
    }

    public void initDicts() {
        super.init();

        DictsLoader.loadNERDict(Dicts.ORG_EDU, false);
        DictsLoader.loadNERDict(Dicts.ORG_GOV, false);
        DictsLoader.loadNERDict(Dicts.ORG_NGO, false);
        DictsLoader.loadNERDict(Dicts.ORG_SUFFIX, false);

        SegUtil.ansjInsertWords(Dicts.nerDict.get(Dicts.ORG_EDU), "n");
        SegUtil.ansjInsertWords(Dicts.nerDict.get(Dicts.ORG_GOV), "n");
        SegUtil.ansjInsertWords(Dicts.nerDict.get(Dicts.ORG_NGO), "n");

        StringBuilder sb = new StringBuilder("[^ ]+(");
        for (String suffix : Dicts.nerDict.get(Dicts.ORG_SUFFIX)) {
            sb.append(suffix).append("|");
        }
        sb.setLength(sb.length() - 1);
        sb.append(")");

        suffixPattern = Pattern.compile(sb.toString());
    }

    // ======== public interfaces
    @Override
    protected List<NERWord> _recognize(String tweet) {
        // 1. match knowl-base
        List<Term> terms = NlpAnalysis.parse(tweet);
        return _recognize(terms);
    }

    @Override
    protected List<NERWord> _recognize(List<Term> terms) {
        List<NERWord> ret = new LinkedList<NERWord>();
        List<NERWord> nerWordList = new LinkedList<NERWord>();
        nerWordList.addAll(termMatchDict(terms, Dicts.nerDict.get(Dicts.ORG_GOV)));
        nerWordList.addAll(termMatchDict(terms, Dicts.nerDict.get(Dicts.ORG_EDU)));
        nerWordList.addAll(termMatchDict(terms, Dicts.nerDict.get(Dicts.ORG_NGO)));
        nerWordList.addAll(termMatchDict(terms, Dicts.nerDict.get(Dicts.ORG_COMMON)));

        // 2. find suffix and merge words
        for (NERWord nerWord : nerWordList)
            if (!getNERType().equals(nerWord.getNerType()) || nerWord.getContent().trim().isEmpty())
                if (suffixPattern.matcher(nerWord.getContent()).matches()
                        && !isInBlacklist(nerWord.getContent())) {
                    nerWord.setNerType(getNERType());
                }

        // RETURN
        for (NERWord nerWord : nerWordList)
            if (getNERType().equals(nerWord.getNerType()))
                ret.add(nerWord);

        return ret;
    }

    @Override
    public String getNERType() {
        return HornbillConsts.NER_TYPE_ORG;
    }

    protected List<NERWord> termMatchDict(List<Term> terms, Set<String> dict) {
        List<NERWord> nerWordList = new LinkedList<NERWord>();
        for (Term term : terms) {
            if (term.getName().trim().isEmpty()
                    || isInBlacklist(term.getName()))
                continue;

            if (dict.contains(term.getName())) {
                NERWord nerWord = NERUtil.ansjTerm2NERWord(term);
                nerWord.setNerType(getNERType());
                nerWordList.add(nerWord);
            } else {
                for (String word : dict)
                    if (term.getName().endsWith(word)) {
                        NERWord nerWord = NERUtil.ansjTerm2NERWord(term);
                        nerWord.setNerType(getNERType());
                        nerWordList.add(nerWord);
                        break;
                    }
            }
        }
        return nerWordList;
    }

    /**************************************
     * ************  MAIN  ****************
     * Test main
     */
    public static void main(String[] args) throws IOException {
        final AbstractNERRecognizer recognizer = new OrgRecognizer();
        String text = "沈阳森林动物园管理有限公司工会组织的职工趣味运动会在棋盘山森林动物园胜利召开。";
        System.out.println(recognizer.recognize(text));
        //if (3 + 2 * 2 == 7) return;
        String input = "/Users/lhfcws/tmp/airui/org200";
        FileSystemUtil.loadFileInLines(new FileInputStream(input), new ILineParser() {
            @Override
            public void parseLine(String s) {
                System.out.println(recognizer.recognize(s));
            }
        });
    }
}
