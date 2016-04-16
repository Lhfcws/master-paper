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
public class LinearRegressionNeutBaseClassifier extends LinearRegressionBaseClassifier {
    public static Log LOG = LogFactory.getLog(LinearRegressionNeutBaseClassifier.class);

    public LinearRegressionNeutBaseClassifier() {
        super();
    }

    @Override
    public String getName() {
        return LinearRegressionNeutBaseClassifier.class.getSimpleName();
    }

    @Override
    public Model getModel() {
        if (model == null)
            synchronized (Model.class) {
                if (model == null)
                    try {
                        model = Model.load(new InputStreamReader(
                                FileSystemUtil.getHDFSFileInputStream(
                                        conf.get(MLLibConsts.FILE_SNT_LR_MODEL1_BASE)
                                )
                        ));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        return model;
    }
}
