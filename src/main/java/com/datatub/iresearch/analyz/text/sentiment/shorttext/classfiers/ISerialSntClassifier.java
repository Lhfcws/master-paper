package com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers;

import java.io.Serializable;

/**
 * @author lhfcws
 * @since 15/12/2.
 */
public interface ISerialSntClassifier extends Serializable {

    /**
     * Cassify the text into class label, return MLLibConsts.UNCLASSIFY to present it cannot deal with the text.
     * @param text
     * @return int label (1: positive, 0: neutral, -1: negative, MLLibConsts.UNCLASSIFY: cannot classify, next classifier will take over the job.)
     */
    public abstract int classify(String text);


}
