package com.toxicstoxm.LEDSuite.upload;

@FunctionalInterface
public interface Upload {

    void onServerResponse(boolean uploadPermitted);

}
