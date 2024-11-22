package com.toxicstoxm.LEDSuite.authentication;

import lombok.Builder;

@Builder
public record Credentials(String username, String passwordHash) {
}
