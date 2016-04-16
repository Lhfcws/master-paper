package com.datatub.iresearch.analyz.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * Batch Writer.
 * @author lhfcws
 * @since 15/11/22.
 */
public class BatchWriter extends OutputStreamWriter {
    private List<String> cache;
    private int size = 1000;

    public BatchWriter(OutputStream out) throws UnsupportedEncodingException {
        super(out, "UTF-8");
        cache = new LinkedList<String>();
    }

    public BatchWriter(OutputStream out, int batchSize) throws UnsupportedEncodingException {
        super(out, "UTF-8");
        cache = new LinkedList<String>();
        size = batchSize;
    }

    public void writeWithCache(String s) throws IOException {
        cache.add(s);
        if (cache.size() < size) {
            flushCache();
        }
    }

    public void flushCache() throws IOException {
        for (String s : cache)
            write(s);
        cache.clear();
    }

    public void flushNClose() throws IOException {
        flushCache();
        this.flush();
        this.close();
    }
}
