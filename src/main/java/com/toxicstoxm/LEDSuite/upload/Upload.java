package com.toxicstoxm.LEDSuite.upload;

/**
 * Functional interface for handling server responses related to upload permission.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface Upload {

    /**
     * Callback method for handling server responses regarding upload permission.
     *
     * @param uploadPermitted a boolean indicating whether the upload is permitted by the server
     */
    void onServerResponse(boolean uploadPermitted);
}
