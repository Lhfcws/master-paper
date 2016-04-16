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

/**
 * @author lhfcws
 * @since 15/12/1.
 */
public class CommodityRecognizer extends AbstractNERRecognizer {
    public CommodityRecognizer() {
        initDicts();
    }

    private void initDicts() {
        super.init();
        DictsLoader.loadNERDict(Dicts.COMM_REAL, false);
        DictsLoader.loadNERDict(Dicts.COMM_VIRTUAL, false);

        SegUtil.ansjInsertWords(Dicts.nerDict.get(Dicts.COMM_REAL), "n");
        SegUtil.ansjInsertWords(Dicts.nerDict.get(Dicts.COMM_VIRTUAL), "n");
    }

    @Override
    protected List<NERWord> _recognize(String cleanedTweet) {
        List<Term> terms = NlpAnalysis.parse(cleanedTweet);
        return _recognize(terms);
    }

    @Override
    protected List<NERWord> _recognize(List<Term> terms) {
        List<NERWord> ret = new LinkedList<NERWord>();

//        StringBuilder sb = new StringBuilder();
//        for (Term term : terms)
//            sb.append("#").append(term);
//        System.out.println(sb.toString());

        for (Term term : terms) {
            if (!term.getNatureStr().startsWith("n") || isInBlacklist(term.getName()))
                continue;
            if (Dicts.nerDict.get(Dicts.COMM_VIRTUAL).contains(term.getName())
                    || Dicts.nerDict.get(Dicts.COMM_REAL).contains(term.getName())
                    || Dicts.nerDict.get(Dicts.COMM_COMMON).contains(term.getName())) {
                NERWord nerWord = NERUtil.ansjTerm2NERWord(term);
                nerWord.setNerType(getNERType());
                ret.add(nerWord);
            }
        }

        return ret;
    }

    @Override
    public String getNERType() {
        return HornbillConsts.NER_TYPE_COMMODITY;
    }


    /**************************************
     * ************  MAIN  ****************
     * Test main
     */
    public static void main(String[] args) throws IOException {
        final AbstractNERRecognizer recognizer = new CommodityRecognizer();
        String text = "沈阳森林动物园管理有限公司工会组织的职工趣味运动会在棋盘山森林动物园胜利召开。";
//        System.out.println(recognizer.recognize(text));
//        if (3 + 2 * 2 == 7) return;
        String input = "/Users/lhfcws/tmp/airui/commodity200";
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
