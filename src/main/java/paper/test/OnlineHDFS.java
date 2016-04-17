package paper.test;

import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.yeezhao.commons.util.AdvCli;
import com.yeezhao.commons.util.CliRunner;
import com.yeezhao.commons.util.FileSystemHelper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.IOException;

/**
 * @author lhfcws
 * @since 16/4/17
 */
public class OnlineHDFS implements CliRunner {

    public static final String PARAM_RM = "rm";

    @Override
    public Options initOptions() {
        Options options = new Options();
        options.addOption(PARAM_RM, true, "rm dir/file");
        return options;
    }

    @Override
    public boolean validateOptions(CommandLine commandLine) {
        return true;
    }

    /***
     * CliRunner main
     */
    public static void main(String[] args) {
        AdvCli.initRunner(args, OnlineHDFS.class.getSimpleName(), new OnlineHDFS());
    }

    @Override
    public void start(CommandLine commandLine) {
        FileSystemHelper fs = FileSystemHelper.getInstance(MLLibConfiguration.getInstance());
        try {
            if (commandLine.hasOption(PARAM_RM)) {
                System.out.println(fs.deleteFile(commandLine.getOptionValue(PARAM_RM)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
