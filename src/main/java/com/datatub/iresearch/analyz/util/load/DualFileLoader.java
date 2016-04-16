package com.datatub.iresearch.analyz.util.load;

import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import com.datatub.iresearch.analyz.util.JXMLQuery;
import com.yeezhao.commons.util.ILineParser;
import com.yeezhao.commons.util.Pair;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Both load classpath 'conf/' and assigned hdfs path.
 *
 * @author lhfcws
 * @since 15/11/30.
 */
public class DualFileLoader implements Serializable {
    private static Configuration conf = MLLibConfiguration.getInstance();
    private static String HDFS_PATH = "/tmp/iresearch";

    static {
        HDFS_PATH = conf.get("dual.model.path", HDFS_PATH);
        if (HDFS_PATH.endsWith("/"))
            HDFS_PATH = HDFS_PATH.substring(0, HDFS_PATH.length() - 1);

        try {
            FileSystemUtil.mkdir(HDFS_PATH);
        } catch (IOException ignore) {
        }
    }

    /**
     * 加载对应模型，获取输入流
     *
     * @param name
     * @return
     */
    public static DualInputStream load(String name) {
        InputStream is1 = null;
        try {
            is1 = conf.getConfResourceAsInputStream(name);
        } catch (Exception e) {
            System.err.println("[LOAD] " + name + " has no classpath file.");
        }

        InputStream is2 = null;
        try {
            is2 = FileSystemUtil.getHDFSFileInputStream(HDFS_PATH + "/" + name);
        } catch (IOException e) {
            System.err.println("[LOAD] " + name + " has no additional file.");
        }

        return new DualInputStream(is1, is2);
    }

    /**
     * 读取本地与增量的InputStream
     *
     * @param dis
     * @param iLineParser
     */
    public static void loadDualInputStream(DualInputStream dis, ILineParser iLineParser) {
        if (dis == null) return;
        try {
            if (dis.getFirst() != null)
                FileSystemUtil.loadFileInLines(dis.getFirst(), iLineParser);
            if (dis.getSecond() != null)
                FileSystemUtil.loadFileInLines(dis.getSecond(), iLineParser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 包装接口，直接按名字加载。
     *
     * @param name
     * @param iLineParser
     */
    public static void loadDualFileInLines(String name, ILineParser iLineParser) {
        loadDualInputStream(
                load(name), iLineParser
        );
    }

    public static void loadDualXMLFile(String name, IJXMLQueryParser iJXMLQueryParser) {
        DualInputStream dis = load(name);
        JXMLQuery q1, q2;
        if (dis.getFirst() != null) {
            try {
                q1 = JXMLQuery.load(dis.getFirst());
                iJXMLQueryParser.parse(q1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (dis.getSecond() != null) {
            try {
                q2 = JXMLQuery.load(dis.getSecond());
                iJXMLQueryParser.parse(q2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 本地与增量的InputStream组合类
     */
    public static class DualInputStream extends Pair<InputStream, InputStream> {
        public DualInputStream(InputStream inputStream, InputStream inputStream2) {
            super(inputStream, inputStream2);
        }
    }

    public interface IJXMLQueryParser {
        void parse(JXMLQuery jxmlQuery);
    }
}
