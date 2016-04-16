package paper;

import com.yeezhao.commons.config.AbstractCommConfiguration;
import com.yeezhao.commons.config.ConfigUtil;
import com.yeezhao.commons.util.config.CommConsts;
import org.apache.hadoop.conf.Configuration;

import java.io.Serializable;

/**
 * Configuration, only can be accessed by getInstance()
 *
 * @author lhfcws
 */
public class MLLibConfiguration extends AbstractCommConfiguration implements Serializable {
    private static final String[] confParams = {
//            MLLibConsts.PARAM_HORNBILL_PORT,
//            MLLibConsts.PARAM_KEYWORD_TOPN,
            CommConsts.SPARK_MASTER_URL,
//            MLLibConsts.PARAM_WORD2VECTOR_PORT
            CommConsts.ES_HOSTS
    };

    private static final String[] servParams = {
    };

    /**
     * Singleton instance.
     */
    private static MLLibConfiguration _singleton = null;

    /**
     * Singleton access method.
     */
    public static MLLibConfiguration getInstance() {
        if (_singleton == null)
            synchronized (MLLibConfiguration.class) {
                if (_singleton == null) {
                    _singleton = new MLLibConfiguration();
                }
            }
        return _singleton;
    }

    /**
     * Private constructor.
     */
    private MLLibConfiguration() {
        super();
    }

    @Override
    protected void init(Configuration configuration) {
        this.addResource("mllib-config.xml");
    }

    @Override
    protected void fastLoadConfigs() throws Exception {
        ConfigUtil.fastLoadConfig(this, CommConsts.ORG, CommConsts.APP, confParams, new String[] {});
    }
}
