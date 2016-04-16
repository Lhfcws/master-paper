package com.datatub.iresearch.analyz.ner.util;


import com.datatub.iresearch.analyz.util.SegUtil;

/**
 * Created by peiqian on 5/6/14.
 */
public class NERWord extends SegUtil.MatchUnit implements Comparable {
    public String nerType;

    public NERWord(int start, int end, String content) {
        super(start, end, content);
    }

    public NERWord(int start, int end, String content, String nerType) {
        super(start, end, content);
        setNerType(nerType);
    }

    public String getNerType() {
        return nerType;
    }

    public NERWord setNerType(String nerType) {
        this.nerType = nerType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        NERWord m = (NERWord) o;
        return (this.start == m.start &&
                this.end == m.end &&
                this.content.equals(m.content));
    }

    @Override
    public int compareTo(Object o) {
        NERWord m = (NERWord) o;
        return start - m.start;
    }

	@Override
	public String toString() {
		return "[content=" + content + ", nerType=" + nerType + ", start=" + start + ", end="
				+ end + "]";
	}
    

}
