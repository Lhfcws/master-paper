package com.datatub.iresearch.analyz.test;

import com.datatub.iresearch.analyz.api.IRAnalyzAPI;
import com.datatub.iresearch.analyz.ner.util.NERWord;
import com.datatub.iresearch.analyz.util.BatchWriter;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import com.datatub.iresearch.analyz.util.SegUtil;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.yeezhao.commons.util.AdvCli;
import com.yeezhao.commons.util.CliRunner;
import com.yeezhao.commons.util.ILineParser;
import com.yeezhao.commons.util.Pair;

import org.ansj.domain.AnsjItem;
import org.ansj.domain.Term;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lhfcws
 * @since 15/12/3.
 */
public class TestIRAnalyzeAPI implements CliRunner {

    @Override
    public Options initOptions() {
        Options options = new Options();
        options.addOption(AdvCli.CLI_PARAM_I, true, "intput test file");
        return options;
    }

    @Override
    public boolean validateOptions(CommandLine commandLine) {
        return commandLine.hasOption(AdvCli.CLI_PARAM_I);
    }

    @Override
    public void start(CommandLine commandLine) {
        final IRAnalyzAPI irAnalyzAPI = new IRAnalyzAPI();
        try {
            final BatchWriter bw1 = new BatchWriter(new FileOutputStream("ner.txt"));
            final BatchWriter bw2 = new BatchWriter(new FileOutputStream("senti.txt"));
            FileSystemUtil.loadFileInLines(new FileInputStream(commandLine.getOptionValue(AdvCli.CLI_PARAM_I)), new ILineParser() {
                @Override
                public void parseLine(String s) {
                    List<NERWord> nerWordList = irAnalyzAPI.extractKeywords(s);
                    int snt = irAnalyzAPI.classifySentiment(s);

                    try {
                        bw1.writeWithCache(s + "\n");
                        bw1.writeWithCache(nerWordList + "\n");
                        bw2.writeWithCache(s + "\n");
                        bw2.writeWithCache(snt + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            bw1.close();
            bw2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**************************************
     * ************  MAIN  *****************
     * AdvCli main.
     */
    public static void main(String[] args) {
    	TestIRAnalyzeAPI testIRAnalyzeAPI = new TestIRAnalyzeAPI();
        AdvCli.initRunner(args, TestIRAnalyzeAPI.class.getSimpleName(), testIRAnalyzeAPI);
//    	testIRAnalyzeAPI.demo();
    }

    public void demo() {
    	String text = "包括天猫、淘宝、京东、苏宁易购、携程、一号店、亚马逊、聚美优品等400多家电商网站，以及 4000多个知名品牌店铺。目前返利的移动端占比达70%。";
        IRAnalyzAPI irAnalyzAPI = new IRAnalyzAPI();
        // 关键词分析接口，保证返回不会为null
        List<NERWord> nerWordList = irAnalyzAPI.extractKeywords(text);
        // 情感分析接口，1：正面，0：中性，-1：负面
        int snt = irAnalyzAPI.classifySentiment(text);
        System.out.println("情感分析：\n\t--> " + snt);
        System.out.println("内容分析：");
        for (NERWord nerWord : nerWordList) {
			System.out.println(String.format("\t--> %s,%s,%s,%s", nerWord.getContent(), 
					nerWord.getStart(), nerWord.getEnd(), nerWord.getNerType()));
		}
        demoBackwordMaxMatch(text);
    }
    
    public void demoBackwordMaxMatch(String tweet) {
    	@SuppressWarnings("unchecked")
		List<Pair<Integer, Integer>> segRes = SegUtil.backwardMaxMatch(
                tweet, 10, 2, Dicts.dict.get(Dicts.DIC_D));
    	System.out.println("最大后向匹配分词：");
    	List<Term> terms = positionPairs2Terms(tweet, segRes);
    	for (Term term : terms) {
    		System.out.println(String.format("\t--> %s,%s,%s", term.getName(), term.getOffe(), 
    				term.getOffe()+term.getName().length()));
		}
    }
    
	// add by guohui
	public static List<Term> positionPairs2Terms(String tweet, List<Pair<Integer, Integer>> pairs) {
		List<Term> terms = new ArrayList<Term>(pairs.size());
		for (Pair<Integer, Integer> pair : pairs) {
			String w = tweet.substring(pair.first, pair.second);
			Term term = new Term(w, pair.first, new AnsjItem());
			terms.add(term);
		}
		return terms;
	}
	
}
