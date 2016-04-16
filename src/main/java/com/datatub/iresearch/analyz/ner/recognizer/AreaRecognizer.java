package com.datatub.iresearch.analyz.ner.recognizer;

import com.datatub.iresearch.analyz.base.HornbillConsts;
import com.datatub.iresearch.analyz.ner.util.NERUtil;
import com.datatub.iresearch.analyz.ner.util.NERWord;
import com.datatub.iresearch.analyz.util.SegUtil;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.datatub.iresearch.analyz.util.load.DictsLoader;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;

import java.util.LinkedList;
import java.util.List;

/**
 * @author lhfcws
 * @since 15/12/1.
 */
public class AreaRecognizer extends AbstractNERRecognizer {
    public AreaRecognizer() {
        initDicts();
    }

    public static final String SEP = "市|省|区|县|镇|村";

    private void initDicts() {
        super.init();
        DictsLoader.loadNERDict(Dicts.AREA_SUFFIX, false);
        DictsLoader.loadNERDict(Dicts.AREA_ADM, false);
        DictsLoader.loadNERDict(Dicts.AREA_GEO, false);

        SegUtil.ansjInsertWords(Dicts.nerDict.get(Dicts.AREA_ADM), "n");
        SegUtil.ansjInsertWords(Dicts.nerDict.get(Dicts.AREA_GEO), "n");
    }

    @Override
    protected List<NERWord> _recognize(String cleanedTweet) {
        List<Term> terms = NlpAnalysis.parse(cleanedTweet);
        return _recognize(terms);
    }

	@SuppressWarnings("unchecked")
	@Override
    protected List<NERWord> _recognize(List<Term> terms) {
        List<NERWord> ret = new LinkedList<NERWord>();

        for (Term term : terms) {
            if (!isInBlacklist(term.getName())) {
                if (SegUtil.inSets(term.getName(),
                        Dicts.nerDict.get(Dicts.AREA_ADM),
                        Dicts.nerDict.get(Dicts.AREA_COMMON),
                        Dicts.nerDict.get(Dicts.AREA_GEO))) {

                    ret.add(NERUtil.ansjTerm2NERWord(term).setNerType(getNERType()));
                } else {
                    boolean suffixFind = false;
                    for (String suffix : Dicts.nerDict.get(Dicts.AREA_SUFFIX)) {
                        suffixFind = term.getName().length() > suffix.length()
                                && term.getName().endsWith(suffix)
                                && term.getNatureStr().startsWith("n")
                                && !term.getNatureStr().equals("nr");
                        if (suffixFind) {
                            ret.add(NERUtil.ansjTerm2NERWord(term).setNerType(getNERType()));
                            break;
                        }
                    }

                    if (!suffixFind && term.getNatureStr().startsWith("n") && term.getName().length() > 4) {
                        String[] sarr = term.getName().split(SEP);
                        for (String item : sarr) {
                            if (SegUtil.inSets(item,
                                    Dicts.nerDict.get(Dicts.AREA_ADM),
                                    Dicts.nerDict.get(Dicts.AREA_COMMON),
                                    Dicts.nerDict.get(Dicts.AREA_GEO))) {
                                int start = term.getName().indexOf(item);
                                ret.add(new NERWord(
                                        term.getOffe() + start, term.getOffe() + start + item.length(), item
                                ).setNerType(getNERType()));
                            }
                        }
                    }
                }
            }
        }

        return ret;
    }

    @Override
    public String getNERType() {
        return HornbillConsts.NER_TYPE_AREA;
    }
}
