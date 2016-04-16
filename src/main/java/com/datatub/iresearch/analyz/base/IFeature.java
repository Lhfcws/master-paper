package com.datatub.iresearch.analyz.base;

import java.io.Serializable;

/**
 * @author lhfcws
 * @since 15/11/13.
 */
public interface IFeature extends Serializable {
    /**
     * Extract feature from raw input.
     * @param raw
     * @return
     */
    double extractFeature(Object raw);

    /**
     * Extract features from raw input.
     * @param raw
     * @return
     */
    double[] extractFeatures(Object raw);
}
