package com.datatub.iresearch.analyz.base;

import java.io.Serializable;

/**
 * @author lhfcws
 * @since 15/11/13.
 */
public interface ITrainer extends Serializable {

//    Vector generateFeatureVector(String raw);

    /**
     * Train interface.
     * @param inputFile
     * @param outputModel
     * @return
     */
    Object train(String inputFile, String outputModel) throws Exception;

    /**
     * Re-train interface.
     * @param inputText
     * @return
     * @throws Exception
     */
    Object retrain(String inputText, String outputModel) throws Exception;

    /**
     * Test interface.
     * @param inputModel
     * @param inputTestFile
     * @return test accurate
     */
    Object test(String inputModel, String inputTestFile) throws Exception;
}
