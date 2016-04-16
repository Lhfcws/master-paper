package com.datatub.iresearch.analyz.ner.classifier;

import com.datatub.iresearch.analyz.ner.recognizer.*;
import com.datatub.iresearch.analyz.ner.util.NERUtil;
import com.datatub.iresearch.analyz.ner.util.NERWord;
import com.datatub.iresearch.analyz.util.SegUtil;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.datatub.iresearch.analyz.util.load.DictsLoader;
import com.yeezhao.commons.util.Pair;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * Main classifier of NER.
 *
 * @author lhfcws
 * @since 15/12/2.
 */
public class NERClassifier {
    @SuppressWarnings("unused")
	private static final String[] KW_CLASS_PREFIX = {"n", "t", "f", "s", "a"};
    List<AbstractNERRecognizer> nerRecognizers;

    public NERClassifier() {
        nerRecognizers = new ArrayList<AbstractNERRecognizer>();
        DictsLoader.loadDict(Dicts.DIC_D, false);
        DictsLoader.loadDict(Dicts.POS_D, false);
        DictsLoader.loadDict(Dicts.NEG_D, false);
        DictsLoader.loadDict(Dicts.KEYWORD_BLACKLIST, false);
        nerRecognizers.add(new AreaRecognizer());
        nerRecognizers.add(new CommodityRecognizer());
        nerRecognizers.add(new FameRecognizer());
        nerRecognizers.add(new OrgRecognizer());
    }

    public List<NERWord> classify(String text) {
        return _recognize(text);
    }

    protected List<NERWord> _recognize(String text) {
        String cleanedTweet = AbstractNERRecognizer.preClean(text);

        Map<Integer, NERWord> positions = new HashMap<Integer, NERWord>();
        List<Term> terms = NlpAnalysis.parse(cleanedTweet);
        terms = improveByDicts(terms, cleanedTweet); // add by guohui
        for (AbstractNERRecognizer recognizer : nerRecognizers) {
            List<NERWord> result = recognizer.recognize(terms);
            if (!CollectionUtils.isEmpty(result))
                for (NERWord nerWord : result)
                    positions.put(nerWord.getStart(), nerWord);
        }

        List<NERWord> ret = new LinkedList<NERWord>(positions.values());
        for (Term term : terms) {
            if (positions.containsKey(term.getOffe()) || !isKeyword(term))
                continue;
            ret.add(NERUtil.ansjTerm2NERWord(term).setNerType(""));
        }

        ret = NERUtil.sort(ret);
        return ret;
    }

	private boolean isKeyword(Term term) {
        String word = term.getName();
        if (word.length() < 2) return false;
        
//        if (Dicts.dict.get(Dicts.KEYWORD_BLACKLIST).contains(word) || !SegUtil.inSets(word,  
//        		Dicts.dict.get(Dicts.DIC_D), Dicts.dict.get(Dicts.POS_D), Dicts.dict.get(Dicts.NEG_D))) // add by guohui
        if (Dicts.dict.get(Dicts.KEYWORD_BLACKLIST).contains(word)) // add by guohui
            return false;

        if (!term.getNatureStr().startsWith("n")) return false;  // add by guohui
//        for (String ntr : KW_CLASS_PREFIX)
//            if (term.getNatureStr().startsWith(ntr))
//                return true;
//        return false;  // add by guohui
        return true;
    }
	
	// add by guohui 通过最大后向匹配优化用ANSJ分隔出来的词，否则common/dic.txt就没用到
	private List<Term> improveByDicts(List<Term> terms, String tweet) {
		List<Term> newTerms = new LinkedList<Term>();
		List<Term> copyTerms = new LinkedList<Term>(terms);
		@SuppressWarnings("unchecked")
		List<Pair<Integer, Integer>> segRes = SegUtil.backwardMaxMatch(
                tweet, 10, 2, Dicts.dict.get(Dicts.DIC_D));
		for (Pair<Integer, Integer> pair : segRes) {
			boolean isNeedStay = false;
			String termNature = null;
			for (Term term : terms) {
				if (pair.first <= term.getOffe() && pair.second >= (term.getOffe()+term.getName().length())) {
					copyTerms.remove(term);
					termNature = term.getNatureStr();
					isNeedStay = true;
				}
			}
			if (isNeedStay) {
				Term newTerm = new Term(tweet.substring(pair.first, pair.second), pair.first, termNature, 1);
				newTerms.add(newTerm);
			}
		}
		newTerms.addAll(copyTerms);
        return newTerms;
	}
}
