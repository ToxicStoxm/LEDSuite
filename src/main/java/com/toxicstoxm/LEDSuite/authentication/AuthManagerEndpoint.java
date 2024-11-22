package com.toxicstoxm.LEDSuite.authentication;


import com.toxicstoxm.LEDSuite.time.Action;

public interface AuthManagerEndpoint {
    void requestAuth(Credentials credentials,  Action finishCb);
    String getUsername();
    void authResult(String username, boolean result);
}
