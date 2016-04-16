package paper.render;

import com.yeezhao.commons.util.AdvFile;
import com.yeezhao.commons.util.BatchWriter;
import com.yeezhao.commons.util.ILineParser;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author lhfcws
 * @since 16/1/13.
 */
public class GexfFilter {
    protected static final String FILTER_STR = "r=\"65025\" g=\"65025\" b=\"65025\"";
    protected static final String[] NOT_DIRECT_WRITE_TAG = {
            "<viz", "<attvalue", "<attvalues", "<node", "</node", "<edge", "</edge", "</viz", "</attvalue", "</attvalues"
    };
    protected InputStream inputStream;

    public GexfFilter(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void filterNOutput(final OutputStream outputStream) throws IOException {
        final BatchWriter batchWriter = new BatchWriter(outputStream);
        final List<String> node = new LinkedList<>();
        final List<String> edge = new LinkedList<>();
        final AtomicBoolean isFilter = new AtomicBoolean(false);

        AdvFile.loadFileInRawLines(this.inputStream, new ILineParser() {
            @Override
            public void parseLine(String s) {
                String line = s.trim();
                try {
                    if (
                            line.startsWith("<nodes") ||
                            line.startsWith("</nodes") ||
                            line.startsWith("<edges") ||
                            line.startsWith("</edges") ||
                            canWrite(line)
                            )
                        batchWriter.writeWithCache(s + "\n");

                    else {
                        if (line.startsWith("<node")) {
                            node.add(s);
                            return;
                        } else if (line.startsWith("</node")) {
                            if (isFilter.get()) {
                                node.clear();
                                isFilter.set(false);
                                return;
                            } else {
                                node.add(s);
                                write(batchWriter, node);
                            }
                        } else  if (line.startsWith("<edge")) {
                            edge.add(s);
                            return;
                        } else if (line.startsWith("</edge")) {
                            if (isFilter.get()) {
                                edge.clear();
                                isFilter.set(false);
                                return;
                            } else {
                                edge.add(s);
                                write(batchWriter, edge);
                            }
                        } else if (line.startsWith("<viz")) {
                            if (line.contains(FILTER_STR)) {
                                isFilter.set(true);
                            } else {
                                if (!node.isEmpty())
                                    node.add(s);
                                else if (!edge.isEmpty())
                                    edge.add(s);
                            }
                        } else {
                            if (!node.isEmpty())
                                node.add(s);
                            else if (!edge.isEmpty())
                                edge.add(s);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        batchWriter.flushNClose();
        outputStream.close();
    }

    protected void write(BatchWriter batchWriter, List<String> lines) throws IOException {
        for (String line : lines)
            batchWriter.writeWithCache(line + "\n");
        lines.clear();
    }

    protected static boolean canWrite(String line) {
        for (String w : NOT_DIRECT_WRITE_TAG)
            if (line.startsWith(w))
                return false;
        return true;
    }


    // ============= MAIN ===============

    /**************************************
     * Test main
     */
    public static void main(String[] args) throws IOException {
        GexfFilter gexfFilter = new GexfFilter(new FileInputStream(args[0]));
        gexfFilter.filterNOutput(new FileOutputStream("/tmp/test.gexf"));
    }
}
