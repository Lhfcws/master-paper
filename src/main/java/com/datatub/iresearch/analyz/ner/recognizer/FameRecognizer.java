package com.datatub.iresearch.analyz.ner.recognizer;

import com.datatub.iresearch.analyz.base.HornbillConsts;
import com.datatub.iresearch.analyz.ner.util.NERWord;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import com.datatub.iresearch.analyz.util.SegUtil;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.datatub.iresearch.analyz.util.load.DictsLoader;
import com.yeezhao.commons.util.FreqDist;
import com.yeezhao.commons.util.ILineParser;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * 短文本人名识别
 *
 * @author lhfcws
 * @since 15/12/1.
 */
public class FameRecognizer extends AbstractNERRecognizer {

    public static Set<String> FAME_BAN_LAST_CHAR = new HashSet<String>(Arrays.asList(new String[]{
            "镇", "岛", "区", "庄", "市", "省", "县",
            "乡", "村", "都", "旗", "郡", "率", "处",
            "局", "部", "州", "号", "路", "委", "于",
            "和", "与", "在", "对", "跟", "及", "就",
            "却", "或", "乃", "是", "像", "向", "到",
            "乎", "与", "为", "兜", "即", "去", "从",
            "以", "似", "假", "去", "让", "诸", "往",
            "寻", "将", "当", "叫", "吃", "同", "问",
            "如", "打", "执", "把", "投", "拦", "按",
            "捉", "给", "因", "比", "自", "趁", "践",
            "较", "爿", "暨", "拿", "替", "望", "朝",
            "由", "率", "被", "用", "繇", "至", "管",
    }));

    public FameRecognizer() {
        initDicts();
    }

    private void initDicts() {
        super.init();

        DictsLoader.loadNERDict(Dicts.FAME_COMMON, false);
        DictsLoader.loadNERDict(Dicts.FAME_CH_SURNAME, false);
        DictsLoader.loadDict(Dicts.STOPWORD_D, false);
        DictsLoader.loadDict(Dicts.CH_DIC_D, false);

        SegUtil.ansjInsertWords(Dicts.nerDict.get(Dicts.FAME_COMMON), "nr");
    }

    public List<NERWord> seg(String rawline) {
        // Seg groups
        String flag = "/p ";
        String newtweet = "";
        List<Term> terms = NlpAnalysis.parse(rawline);
        List<NERWord> unitList = new LinkedList<NERWord>();
        for (int i = 0; i < terms.size(); i++) {
            Term term = terms.get(i);
            newtweet = newtweet + term.getName();
            if (term.getName().length() <= 1)
                continue;
            if (Dicts.dict.get(Dicts.STOPWORD_D).contains(term.getName()))
                continue;

            if (isPerson(term)) {
                NERWord u = new NERWord(term.getOffe(), term.getOffe() + term.getName().length(), term.getName(), HornbillConsts.NER_TYPE_FAME);
                unitList.add(u);
                newtweet = newtweet + flag;
            }
        }
        //     System.out.println(newtweet);
        return unitList;
    }

    @Override
    protected List<NERWord> _recognize(String tweet) {
        List<Term> terms = NlpAnalysis.parse(tweet);
//
//        StringBuilder sb = new StringBuilder();
//        for (Term term : terms)
//            sb.append("#").append(term);
//        System.out.println(sb.toString());
        return _recognize(terms);
    }

    @Override
    protected List<NERWord> _recognize(List<Term> terms) {
        List<NERWord> unitList = new LinkedList<NERWord>();
        for (int i = 0; i < terms.size(); i++) {
            Term term = terms.get(i);
            if (term.getName().length() <= 1)
                continue;
            if (isInBlacklist(term.getName()))
                continue;
            if (Dicts.dict.get(Dicts.STOPWORD_D).contains(term.getName()))
                continue;

            if (isPerson(term)) {
                NERWord u = new NERWord(term.getOffe(), term.getOffe() + term.getName().length(), term.getName(), getNERType());
                unitList.add(u);
            }
        }
        //     System.out.println(newtweet);
        unitList = clearAmbigious(unitList);
        return unitList;
    }

    @Override
    public String getNERType() {
        return HornbillConsts.NER_TYPE_FAME;
    }

    /**
     * n:        普通名词		20
     * ng:        名词性语素	21
     * nr:        人名		22
     * ns:        地名		23
     * nt:        机构团体	24
     * nx:        名词性非语素		25
     * nz:        其他专名	26
     *
     * @param term
     * @return
     */
    private boolean isPerson(Term term) {
        //update by huaping
//        return term.getNatureStr().equals("nr");

    	if (Dicts.nerDict.get(Dicts.FAME_COMMON).contains(term.getName())) {
    		return true;
    	}
    	
        if (term.getName().length() <= 1)
            return false;
        String name = term.getName();

        boolean result = true;
        String firstChar = name.substring(0, 1);
        String firstTwoChar = name.substring(0, 2);
        String lastChar = name.substring(name.length() - 1, name.length());

        // If term is a noun and term is in common.fame
        String nature = term.getNatureStr();
        // commented by guohui start 2016-01-06
//        result = nature.startsWith("n") && Dicts.nerDict.get(Dicts.FAME_COMMON).contains(term.getName());
        // commented by guohui end 2016-01-06

        // OOV word. With term-nature == nr and correct Chinese surname and no BAN_LAST_CHAR.
        boolean hasChSurname = (name.length() <= 3 && Dicts.nerDict.get(Dicts.FAME_CH_SURNAME).contains(firstChar))
                || Dicts.nerDict.get(Dicts.FAME_CH_SURNAME).contains(firstTwoChar);
        boolean newName = ("nr".equals(nature)) && hasChSurname && (!FAME_BAN_LAST_CHAR.contains(lastChar));
        result &= newName;

        // Not the usual chinese word. (We just want more accuracy than recall rate.)
        boolean isNotCommonWord = !Dicts.dict.get(Dicts.CH_DIC_D).contains(term.getName());
        result &= isNotCommonWord;

        // Some features that the previous term of a name term should have.
//        Term prev = term.from();
//        if (prev != null)
//            result &= prev.getNatureStr().startsWith("n");

        // === RETURN
        return result;
    }

    protected List<NERWord> clearAmbigious(List<NERWord> nerWords) {
        FreqDist<String> names = new FreqDist<String>();
        for (NERWord nw : nerWords)
            names.inc(nw.getContent());

        Set<String> keySet = new HashSet<String>(names.keySet());
        for (String n1 : keySet)
            for (String n2 : keySet)
                if (similar(n1, n2)) {
                    String shortName = n1.length() > n2.length() ? n2 : n1;
                    String longName = n1.length() > n2.length() ? n1 : n2;

                    if (names.get(shortName) > names.get(longName)) {
                        for (NERWord nerWord : nerWords)
                            if (nerWord.getContent().equals(longName)) {
                                if (longName.startsWith(shortName)) {
                                    nerWord.setContent(shortName);
                                    nerWord.setStart(nerWord.getStart() + (longName.length() - shortName.length()));
                                } else if (longName.endsWith(shortName)) {
                                    nerWord.setContent(shortName);
                                    nerWord.setEnd(nerWord.getEnd() - (longName.length() - shortName.length()));
                                }
                            }
                    }
                }

        return nerWords;
    }

    protected boolean similar(String w1, String w2) {
        return w1.startsWith(w2) || w1.endsWith(w2) || (w2.startsWith(w1) || w2.endsWith(w1));
    }

    /**************************************
     * ************  MAIN  ****************
     * Test main
     */
    @SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
        final AbstractNERRecognizer recognizer = new FameRecognizer();
        String text = "沈阳森林动物园管理有限公司工会组织的职工趣味运动会在棋盘山森林动物园胜利召开。";
//        System.out.println(recognizer.recognize(text));
//        if (3 + 2 * 2 == 7) return;
        String input = "/Users/lhfcws/tmp/airui/fame200";
        FileSystemUtil.loadFileInLines(new FileInputStream(input), new ILineParser() {
            @Override
            public void parseLine(String s) {
//                try {
                System.out.println(recognizer.recognize(s));
//                } catch (Exception e) {
//                    System.err.println(s);
//                }
            }
        });
    }

}