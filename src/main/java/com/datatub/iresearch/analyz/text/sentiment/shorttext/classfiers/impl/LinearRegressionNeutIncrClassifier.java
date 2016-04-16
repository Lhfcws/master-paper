package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl;

import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import de.bwaldvogel.liblinear.Model;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author lhfcws
 * @since 15/12/19.
 */
public class LinearRegressionNeutIncrClassifier extends LinearRegressionIncrClassifier {
    public static Log LOG = LogFactory.getLog(LinearRegressionNeutIncrClassifier.class);

    public LinearRegressionNeutIncrClassifier() {
        super();
    }

    @Override
    public String getName() {
        return LinearRegressionNeutIncrClassifier.class.getSimpleName();
    }

    @Override
    public Model getModel() {
        if (model == null)
            synchronized (Model.class) {
                if (model == null)
                    try {
                        model = Model.load(new InputStreamReader(
                                FileSystemUtil.getHDFSFileInputStream(
                                        conf.get(MLLibConsts.FILE_SNT_LR_MODEL1_INCR)
                                )
                        ));
                    } catch (IOException e) {
                        System.out.println("[WARNING] No incr model1.");
                        try {
                            FileSystemUtil.copyFile(
                                    conf.get(MLLibConsts.FILE_SNT_LR_MODEL1_BASE),
                                    conf.get(MLLibConsts.FILE_SNT_LR_MODEL1_INCR)
                            );
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
            }
        return model;
    }

    public String getCorpusPath() {
        return conf.get(MLLibConsts.FILE_SNT_LR_CORPUS1_INCR);
    }

    public String getModelPath() {
        return conf.get(MLLibConsts.FILE_SNT_LR_MODEL1_INCR);
    }
}
