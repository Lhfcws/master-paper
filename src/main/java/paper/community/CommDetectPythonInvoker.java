package paper.community;

import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.yeezhao.commons.util.FileSystemHelper;
import com.yeezhao.commons.util.exec.CommandExecutor;
import com.yeezhao.commons.util.exec.LineHandler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lhfcws
 * @since 16/1/4.
 */
public class CommDetectPythonInvoker {
    public static String PY_FILE = "src/main/python/community.py";

    public void run(String graphmlFile, String output) {
        FileSystemHelper fs = FileSystemHelper.getInstance(MLLibConfiguration.getInstance());
        String pyFile = PY_FILE;
        try {
            if (!fs.existLocalFile(pyFile))
                pyFile = "python/community.py";
            if (!fs.existLocalFile(pyFile))
                pyFile = "community.py";
        } catch (IOException e) {
            e.printStackTrace();
        }

        String cmd = "python " + pyFile + " " + graphmlFile + " " + output;
        System.out.println("Run cmd : " + cmd);
        List<String> cmds = new LinkedList<>();
        cmds.add("/bin/sh");
        cmds.add("-c");
        cmds.add(cmd);
        long start = System.currentTimeMillis();
        CommandExecutor commandExecutor = new CommandExecutor(cmds);
        commandExecutor.run(new LineHandler() {
            @Override
            public void handleLine(String s) {
                System.out.println(s);
            }
        });
        System.out.println("Invoked python. Cost " + (System.currentTimeMillis() - start) + " ms");
    }
}
