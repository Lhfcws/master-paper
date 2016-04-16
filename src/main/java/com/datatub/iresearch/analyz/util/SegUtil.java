package com.datatub.iresearch.analyz.util;

import com.yeezhao.commons.util.Pair;

import org.ansj.domain.AnsjItem;
import org.ansj.domain.Nature;
import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

public class SegUtil {
	public static String atUserReg = "(@[^(:| |@|,|.|：|，|。|！|!|?|？|'|~|/)]*)|(@[^(:| )]*$)";
	public static String engWordReg = "([a-zA-Z0-9]+)";
	public static String symbols = ",.?~;:!&()-+。，？～|｜！“‘；：'\"";

	public static boolean isSymbol(char ch) {
		if (symbols.indexOf(ch) >= 0)
			return true;
		return false;
	}

	public SegUtil() {
	}

	public static <T> List<Pair<Integer, Integer>> forwardMaxMatch(String s,
																   Map<String, T> dict, int maxWordLen, int minWordLen) {
		LinkedList<Pair<Integer, Integer>> wordList = new LinkedList<Pair<Integer, Integer>>();
		int curL = 0, curR = s.length(); // Left and Right end of current
		// matching, excluding the right
		// end.
		maxWordLen = maxWordLen > s.length() ? s.length() : maxWordLen;
		while (curL < s.length()) {
			boolean isMatched = false;
			curR = curL + maxWordLen > s.length() - curL ? s.length() - curL : (curL + maxWordLen);
			while (curR - curL >= minWordLen) { // try all subsets backwards
				if (dict.containsKey(s.substring(curL, curR))) { // matched
					wordList.add(new Pair<Integer, Integer>(new Integer(curL), new Integer(curR)));
					curL = curR;
					curR = curL + maxWordLen > s.length() - curL ? s.length() - curL : (curL + maxWordLen);
					isMatched = true;
					break;
				} else
					// not matched, try subset by moving left rightwards
					curR--;
			}
			if (!isMatched) {// not matched, move the right end leftwards
				wordList.add(new Pair<Integer, Integer>(new Integer(curL), new Integer(curL + 1)));
				curL++;
			}
		}
		return wordList;
	}

	public static List<Pair<Integer, Integer>> forwardMaxMatch(String s,
															   int maxWordLen, int minWordLen, Set<String>... dicts) {
		LinkedList<Pair<Integer, Integer>> wordList = new LinkedList<Pair<Integer, Integer>>();
		int curL = 0, curR = s.length(); // Left and Right end of current
		// matching, excluding the right
		// end.
		maxWordLen = maxWordLen > s.length() ? s.length() : maxWordLen;
		while (curL < s.length()) {
			boolean isMatched = false;
			curR = curL + maxWordLen > s.length() - curL ? s.length() - curL : (curL + maxWordLen);
			while (curR - curL >= minWordLen) { // try all subsets backwards
				for (Set<String> dict : dicts)
					if (dict.contains(s.substring(curL, curR))) { // matched
						wordList.add(new Pair<Integer, Integer>(new Integer(curL), new Integer(curR)));
						curL = curR;
						curR = curL + maxWordLen > s.length() - curL ? s.length() - curL : (curL + maxWordLen);
						isMatched = true;
						break;
					}

				if (isMatched)
					break;
				else
					// not matched, try subset by moving left rightwards
					curR--;
			}
			if (!isMatched) {// not matched, move the right end leftwards
				wordList.add(new Pair<Integer, Integer>(new Integer(curL), new Integer(curL + 1)));
				curL++;
			}
		}
		return wordList;
	}

	public static <T> List<Pair<Integer, Integer>> backwardMaxMatch(String s,
																	Map<String, T> dict, int maxWordLen, int minWordLen) {
		LinkedList<Pair<Integer, Integer>> wordList = new LinkedList<Pair<Integer, Integer>>();
		int curL = 0, curR = s.length(); // Left and Right end of current
		// matching, excluding the right
		// end.
		while (curR >= 1) {
			boolean isMatched = false;
			curL = curR - maxWordLen < 0 ? 0 : (curR - maxWordLen);
			while (curR - curL >= minWordLen) { // try all subsets backwards
				if (dict.containsKey(s.substring(curL, curR))) { // matched
					wordList.addFirst(new Pair<Integer, Integer>(new Integer(curL), new Integer(curR)));
					curR = curL;
					curL = curR - maxWordLen < 0 ? 0 : (curR - maxWordLen);
					isMatched = true;
					break;
				} else
					// not matched, try subset by moving left rightwards
					curL++;
			}
			if (!isMatched) {// not matched, move the right end leftwards
				curR--;
				wordList.addFirst(new Pair<Integer, Integer>(new Integer(curR), new Integer(curR + 1)));
			}
		}
		return wordList;
	}

	public static List<Pair<Integer, Integer>> backwardMaxMatch(String s,
																int maxWordLen, int minWordLen, Set<String>... dicts) {
		LinkedList<Pair<Integer, Integer>> wordList = new LinkedList<Pair<Integer, Integer>>();
		int curL = 0, curR = s.length(); // Left and Right end of current
		// matching, excluding the right
		// end.
		while (curR >= 1) {
			boolean isMatched = false;
			curL = curR - maxWordLen < 0 ? 0 : (curR - maxWordLen);
			while (curR - curL >= minWordLen) { // try all subsets backwards
				for (Set<String> dict : dicts)
					if (dict.contains(s.substring(curL, curR))) { // matched
						wordList.addFirst(new Pair<Integer, Integer>(new Integer(curL), new Integer(curR)));
						curR = curL;
						curL = curR - maxWordLen < 0 ? 0 : (curR - maxWordLen);
						isMatched = true;
						break;
					}

				if (isMatched) {
					break;
				} else
					// not matched, try subset by moving left rightwards
					curL++;
			}
			if (!isMatched) {// not matched, move the right end leftwards
				curR--;
				wordList.addFirst(new Pair<Integer, Integer>(new Integer(curR), new Integer(curR + 1)));
			}
		}
		return wordList;
	}


	/**
	 * Double direction match.
	 *
	 * @param tweet
	 * @return
	 */
	public List<MatchUnit> doubleDirectionMaxMatch(String tweet, Set<String> ... dicts) {
		List<MatchUnit> list = new LinkedList<MatchUnit>();

		List<MatchUnit> bwlist = new LinkedList<MatchUnit>();
		List<Pair<Integer, Integer>> rawPos = backwardMaxMatch(tweet, 100, 2, dicts);
		for (Pair<Integer, Integer> pos : rawPos) {
			String keyword = tweet.substring(pos.first, pos.second);
			MatchUnit unit = new MatchUnit(pos.first, pos.second, keyword);
			bwlist.add(unit);
		}

		List<MatchUnit> fwlist = new LinkedList<MatchUnit>();
		rawPos = forwardMaxMatch(tweet, 100, 2, dicts);
		for (Pair<Integer, Integer> pos : rawPos) {
			String keyword = tweet.substring(pos.first, pos.second);
			MatchUnit unit = new MatchUnit(pos.first, pos.second, keyword);
			fwlist.add(unit);
		}

		// 对于分词不同的每一段尽可能要KB词多的分法
		int flen = fwlist.size();
		int blen = bwlist.size();
		int fcnt = 0;
		int bcnt = 0;
		int i = 0;
		int j = 0;
		int starti = 0;
		int startj = 0;

		while (i < flen && j < blen) {
			MatchUnit unit1 = fwlist.get(i);
			MatchUnit unit2 = bwlist.get(j);

			if (unit1.end < unit2.end) {
				i++;
				if (!inSets(fwlist.get(i).content, dicts))
					fcnt++;
			} else if (unit1.end > unit2.end) {
				j++;
				if (!inSets(bwlist.get(j).content, dicts))
					bcnt++;
			} else {
				if (fcnt >= bcnt) {
					for (int k = startj; k <= j; k++) {
						list.add(bwlist.get(k));
					}
				} else {
					for (int k = starti; k <= i; k++) {
						list.add(fwlist.get(k));
					}
				}

				fcnt = bcnt = 0;
				i++;
				j++;
				starti = i;
				startj = j;
			}
		}

		while (i < flen) {
			list.add(fwlist.get(i));
			i++;
		}

		while (j < blen) {
			list.add(bwlist.get(j));
			j++;
		}

		return list;
	}

	public static List<String> ansjStdSeg(String content) {
		List<Term> terms = ToAnalysis.parse(content);
		List<String> result = new LinkedList<String>();
		for (Term term : terms)
			result.add(term.getName());
		return result;
	}

	public static List<String> ansjNlpSeg(String content) {
		List<Term> terms = NlpAnalysis.parse(content);
		List<String> result = new LinkedList<String>();
		for (Term term : terms)
			result.add(term.getName());
		return result;
	}

	/**
	 * For ansj seg words.
	 * @param word
	 */
	public static void ansjInsertWord(String word) {
		UserDefineLibrary.insertWord(word, "", 1000);
	}

	public static void ansjInsertWord(String word, String wordClass) {
		UserDefineLibrary.insertWord(word, wordClass, 1000);
	}

	public static void ansjInsertWord(String word, String wordClass, int weight) {
		UserDefineLibrary.insertWord(word, wordClass, weight);
	}

	public static void ansjInsertWords(Collection<String> words) {
		for (String word : words)
			ansjInsertWord(word);
	}

	public static void ansjInsertWords(Collection<String> words, String wordClass) {
		for (String word : words)
			ansjInsertWord(word, wordClass);
	}

	public static void ansjInsertWords(Collection<String> words, String wordClass, int weight) {
		for (String word : words)
			ansjInsertWord(word, wordClass, weight);
	}

	public static List<String> token(String input) {
		return token(new StringReader(input));
	}

	public static List<String> token(Reader reader) {
		IKSegmenter segmenter = new IKSegmenter(reader, true);
		ArrayList<String> tokens = new ArrayList<String>();
		Lexeme lexeme = null;
		try {
			while ((lexeme = segmenter.next()) != null) {
				tokens.add(lexeme.getLexemeText());
			}
		} catch (Exception e) {
		}
		return tokens;
	}

	public static <K> boolean inSets(K item, Set<K> ... sets) {
		for (Set<K> set : sets) {
			if (set.contains(item))
				return true;
		}
		return false;
	}

	public static List<String> positionPairs2Words(String tweet, List<Pair<Integer, Integer>> pairs) {
		List<String> words = new ArrayList<String>(pairs.size());
		for (Pair<Integer, Integer> pair : pairs) {
			String w = tweet.substring(pair.first, pair.second);
			words.add(w);
		}
		return words;
	}

	@SuppressWarnings("rawtypes")
	public static class MatchUnit implements Comparable {
		protected int start;
		protected int end;
		protected String content;

		public MatchUnit(int start, int end, String content) {
			super();
			this.start = start;
			this.end = end;
			this.content = content;
		}

		@Override
		public String toString() {
			return "MatchUnit [start=" + start + ", end=" + end + ", content="
					+ content + "]";
		}

		public int length() {
			return start - end + 1;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

		public String getContent() {
			return content;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public void setEnd(int end) {
			this.end = end;
		}

		@Override
		public boolean equals(Object o) {
			MatchUnit m = (MatchUnit) o;
			return (this.start == m.start &&
					this.end == m.end &&
					this.content.equals(m.content));
		}

		@Override
		public int compareTo(Object o) {
			MatchUnit m = (MatchUnit) o;
			return start - m.start;
		}
	}
}
