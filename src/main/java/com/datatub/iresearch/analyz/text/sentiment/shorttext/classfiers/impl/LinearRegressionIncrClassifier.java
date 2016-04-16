package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.impl;

import com.datatub.iresearch.analyz.base.ITrainer;
import com.datatub.iresearch.analyz.base.MLLibConsts;
import com.datatub.iresearch.analyz.util.FileSystemUtil;
import de.bwaldvogel.liblinear.Model;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author lhfcws
 * @since 15/11/19.
 */
public class LinearRegressionIncrClassifier extends LinearRegressionBaseClassifier implements ITrainer {
    protected static Log LOG = LogFactory.getLog(LinearRegressionIncrClassifier.class);

    public LinearRegressionIncrClassifier() {
        super();
    }

    @Override
    public Object retrain(String inputText, String outputModel) throws Exception {
        return this.train(inputText, outputModel);
    }

    @Override
    public String getName() {
        return LinearRegressionIncrClassifier.class.getSimpleName();
    }

    @Override
    public Model getModel() {
        if (model == null)
            synchronized (Model.class) {
                if (model == null)
                    try {
                        model = Model.load(new InputStreamReader(
                                FileSystemUtil.getHDFSFileInputStream(
                                        conf.get(MLLibConsts.FILE_SNT_LR_MODEL_INCR)
                                )
                        ));
                    } catch (IOException e) {
                        System.out.println("[WARNING] No incr model.");
                        try {
                            FileSystemUtil.copyFile(
                                    conf.get(MLLibConsts.FILE_SNT_LR_MODEL_BASE),
                                    conf.get(MLLibConsts.FILE_SNT_LR_MODEL_INCR)
                            );
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
            }
        return model;
    }

    public String getCorpusPath() {
        return conf.get(MLLibConsts.FILE_SNT_LR_CORPUS_INCR);
    }

    public String getModelPath() {
        return conf.get(MLLibConsts.FILE_SNT_LR_MODEL_INCR);
    }
}
