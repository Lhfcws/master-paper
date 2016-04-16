package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers;

import com.datatub.iresearch.analyz.text.sentiment.shorttext.utils.SntAccurateModel;
import com.datatub.iresearch.analyz.util.AccDist;
import com.datatub.iresearch.analyz.util.AccObject;

import java.io.Serializable;

/**
 * @author lhfcws
 * @since 15/11/13.
 */
public interface ISntClassifier extends Serializable {

    /**
     * Init classifiers. If using trainer, then u may not need to invoke init().
     */
    ISntClassifier init();

    /**
     *
     * @param sntAccurateModel
     */
    ISntClassifier update(SntAccurateModel sntAccurateModel);

    /**
     * The total training accurate of the classifier.
     * @return
     */
    AccObject getTotalAccurate();

    /**
     *  The training accurate of each class of the classifier.
     * @return
     */
    AccDist<Integer> getAccurateDist();

    /**
     * The training stat result.
     * @return
     */
    SntAccurateModel getSntAccurateModel();

    /**
     * classifier name.
     * @return
     */
    String getName();

    /**
     * Cassify the text into class label, return UNCLASSIFY to present it cannot deal with the text.
     * @param text
     * @return int label
     */
    int classify(String text);

}
