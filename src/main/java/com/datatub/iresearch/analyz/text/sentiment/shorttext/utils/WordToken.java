package com.datatub.iresearch.analyz.text.sentiment.shorttext.utils;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: congzicun
 * Date: 5/11/13
 * Time: 4:03 PM
 */
public class WordToken implements Serializable {
    public String keyword;
    public String clss;
    public String orgin;
    public int twtFragIndex = -1;
    public int sntnc = -1;
    public int phrase = -1;
    public int wrdStPos = -1;
    public int wrdEndPos = -1;

    public WordToken(String keyword, String clss, String origin) {
        this.keyword = keyword;
        this.clss = clss;
        this.orgin = origin;
    }
    
    public String toString() {
    	return String.format("<keyword:'%s', clss:'%s', orgin:'%s', sntnc:%d, phrase:%d, wrdStPos:%d, wrdEndPos:%d>"
    			, keyword, clss, orgin, sntnc, phrase, wrdStPos, wrdEndPos);
    }
    
    public boolean equals(WordToken other) {
    	return (keyword.equals(other.keyword) && 
    			clss.equals(other.clss) && orgin.equals(other.orgin)
    			&& twtFragIndex == other.twtFragIndex && sntnc == other.sntnc
    			&& phrase == other.phrase && wrdStPos == other.wrdStPos
    			&& wrdEndPos == other.wrdEndPos);
    }
}
