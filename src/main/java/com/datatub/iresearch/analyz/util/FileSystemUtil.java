package com.datatub.iresearch.analyz.util;

import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.yeezhao.commons.util.ILineParser;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lhfcws
 * @since 15/11/15.
 */
public class FileSystemUtil {
    private static Configuration conf = MLLibConfiguration.getInstance();

    public static boolean existFile(String fn) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        return fs.exists(new Path(fn));
    }

    public static void mkdir(String fn) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        try {
            fs.mkdirs(new Path(fn));
        } catch (Exception e) {}
    }

    /**
     * Delete
     *
     * @param fn
     * @throws IOException
     */
    public static void deleteFile(String fn) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        try {
            fs.delete(new Path(fn));
        } catch (Exception ignore) {
        }
    }

    /**
     * Delete
     *
     * @param fn
     * @throws IOException
     */
    public static void deleteLocalFile(String fn) throws IOException {
        FileSystem fs = FileSystem.getLocal(conf);
        try {
            fs.delete(new Path(fn));
        } catch (Exception ignore) {
        }
    }

    /**
     * Save Collection type data (i.e. List, Set) to HDFS.
     *
     * @param fn
     * @param data
     * @throws IOException
     */
    public static void saveFile(String fn, Collection data) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        OutputStream os = fs.create(new Path(fn));
        OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");

        for (Object obj : data)
            writer.write(obj.toString() + "\n");

        writer.flush();
        writer.close();
        os.flush();
        os.close();
    }

    public static void mergeDirsToFile(String fn, String dir) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        FileUtil.copyMerge(fs, new Path(dir), fs, new Path(fn), true, conf, "");
//        Path p = new Path(fn);
//        FileStatus[] fileStatuses = fs.listStatus(new Path(dir));
//        for (FileStatus fileStatus : fileStatuses) {
//            OutputStream os;
//            if (fs.exists(p))
//                os = fs.append(p);
//            else
//                os = fs.create(p);
//
//            IOUtils.copy(fs.open(fileStatus.getPath()), os);
//            os.flush();
//            os.close();
//        }
    }

    /**
     * Read lines to a list of String
     * @param is
     * @return
     * @throws IOException
     */
    public static List<String> read2List(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        List<String> list = new LinkedList<String>();
        String line = null;
        while ((line = br.readLine()) != null) {
            list.add(line.trim());
        }

        br.close();
        isr.close();
        is.close();

        return list;
    }

    /**
     * Count lines of HDFS file.
     *
     * @param fn
     * @return
     * @throws IOException
     */
    public static int countLines(String fn) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        return countLinesInputStream(fs.open(new Path(fn)));
    }

    /**
     * Count lines of local file.
     *
     * @param fn
     * @return
     * @throws IOException
     */
    public static int countLinesLocal(String fn) throws IOException {
        FileSystem fs = FileSystem.getLocal(conf);
        return countLinesInputStream(fs.open(new Path(fn)));
    }

    /**
     * Count lines of given inputstream.
     *
     * @param is
     * @return
     * @throws IOException
     */
    public static int countLinesInputStream(InputStream is) throws IOException {
        int counter = 0;
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String line = null;
        while ((line = br.readLine()) != null) {
            counter++;
        }

        br.close();
        isr.close();
        is.close();
        return counter;
    }

    /**
     * Merge to a new local file. newLocalFile = hdfsFile + localFile.
     *
     * @param hdfsFile
     * @param localFile
     * @return new local filepath
     * @throws IOException
     */
    public static synchronized String mergeHDFSWithLocalFile(String hdfsFile, String localFile) throws IOException {
        String fn = "/tmp/mllib-merge-" + System.currentTimeMillis();
        OutputStream os = new FileOutputStream(fn);
        final OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");

        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(new Path(hdfsFile)))
            loadFileInLines(fs.open(new Path(hdfsFile)), new ILineParser() {
                @Override
                public void parseLine(String s) {
                    try {
                        writer.write(s.trim() + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        writer.flush();
        os.flush();

        loadFileInLines(new FileInputStream(localFile), new ILineParser() {
            @Override
            public void parseLine(String s) {
                try {
                    writer.write(s.trim() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        writer.flush();
        writer.close();
        os.flush();
        os.close();

        return fn;
    }

    public static void copyFile(String src, String dest) throws IOException {
        InputStream in = getHDFSFileInputStream(src);
        OutputStream out = getHDFSFileOutputStream(dest);
        IOUtils.copy(in, out);
        out.flush();
        out.close();
        in.close();
    }

    /**
     * Sometimes using this method may need a lock to guarantee that nobody is using oldFn.
     *
     * @param oldFn
     * @param newFn
     * @throws IOException
     */
    public static void renameFile(String oldFn, String newFn) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        fs.rename(new Path(oldFn), new Path(newFn));
    }

    /**
     * Sometimes using this method may need a lock to guarantee that nobody is using oldDir.
     *
     * @param newDir
     * @param oldDir
     * @throws IOException
     */
    public static void replaceDir(String oldDir, String newDir) throws IOException {
        try {
            FileSystemUtil.deleteFile(oldDir);
        } catch (Exception ignore) {
        }
        FileSystemUtil.renameFile(newDir, oldDir);
    }

    /**
     * Get outputStream of a HDFS Path, just throw exception if not exist.
     *
     * @param fn
     * @return
     * @throws IOException
     */
    public static OutputStream getHDFSFileOutputStream(String fn) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        return fs.create(new Path(fn));
    }

    public static InputStream getHDFSFileInputStream(String fn) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        return fs.open(new Path(fn));
    }

    public static void forceUploadFile2HDFS(String localFn, String hdfsFn) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path hdfsPath = new Path(hdfsFn);
        if (fs.exists(hdfsPath))
            fs.delete(hdfsPath);
        fs.copyFromLocalFile(false, true, new Path(localFn), hdfsPath);
    }

    /**
     * Get a unique filename with timestamp by given prefix.
     *
     * @param prefix
     * @return
     */
    public static synchronized String getTimestampFile(String prefix) {
        return prefix + System.currentTimeMillis();
    }

    /**
     * Replace AdvFile.loadFileInDelimitLine
     * @param is
     * @param iLineParser
     * @throws IOException
     */
    public static void loadFileInLines(InputStream is, ILineParser iLineParser) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        List<String> list = new LinkedList<String>();
        String line = null;
        while ((line = br.readLine()) != null) {
            if( line.trim().isEmpty() ) continue;
            iLineParser.parseLine(line.trim());
        }

        br.close();
        isr.close();
        is.close();
    }

    /**************************************
     * ************  MAIN  *****************
     * Test main
     */
    public static void main(String[] args) throws IOException {
        int cnt = FileSystemUtil.countLinesLocal("/Users/lhfcws/tmp/airui/airui1w.txt");
        System.out.println(cnt);
    }
}