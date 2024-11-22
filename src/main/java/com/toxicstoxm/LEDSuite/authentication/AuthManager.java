package com.toxicstoxm.LEDSuite.authentication;

import com.toxicstoxm.LEDSuite.communication.packet_management.packets.requests.AuthenticationRequestPacket;
import com.toxicstoxm.LEDSuite.time.Action;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.UpdateCallback;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@Getter
public class AuthManager implements AuthManagerEndpoint {

    private final HashMap<String, UpdateCallback<Boolean>> awaitingResponse = new HashMap<>();

    private String username;

    @Override
    public void requestAuth(@NotNull Credentials credentials, Action finishCb) {
        if (username != null) {
            username = null;
        }

        String newUsername = credentials.username();

        awaitingResponse.put(newUsername, result -> {
            if (result) {
                username = newUsername;
            }
            LEDSuiteApplication.getWindow().setAuthenticated(true, username);
            finishCb.run();
        });

        LEDSuiteApplication.getWebSocketCommunication().enqueueMessage(
                AuthenticationRequestPacket.builder()
                        .username(newUsername)
                        .passwordHash(credentials.passwordHash())
                        .build().serialize()
        );
    }

    @Override
    public void authResult(String username, boolean result) {
        if ((username == null || username.isBlank()) && !result) {
            LEDSuiteApplication.getWindow().setAuthenticated(false);
        } else if (awaitingResponse.containsKey(username)) {
            awaitingResponse.remove(username).update(result);
        }
    }
}
