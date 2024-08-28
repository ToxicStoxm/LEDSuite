package com.toxicstoxm.LEDSuite.logger.areas;

import com.toxicstoxm.LEDSuite.logger.colors.LEDSuiteMessage;

import java.util.HashMap;

import static com.toxicstoxm.LEDSuite.settings.config.LEDSuiteSettingsBundle.*;

public class LEDSuiteSpacer implements Spacer {

    private static final HashMap<String, Integer> spacings = new HashMap<>();

    @Override
    public String getSpacingFor(String messageElementGroup, String messageElement) {
        if (EnableAutoSpacing.getInstance().get()) {
            spacings.putIfAbsent(messageElementGroup, messageElement.length());
            int currentMax = spacings.get(messageElementGroup);
            int current = messageElement.length();
            if (currentMax < current) {
                spacings.put(messageElementGroup, current);
                return messageElement + getRSTAndBase();
            }
            if (currentMax > current)
                return messageElement + getRSTAndBase() + genSpacing(currentMax - current);
        }
        return messageElement + getRSTAndBase();
    }

    private String getRSTAndBase() {
        return (EnableAutoReset.getInstance().get() ? LEDSuiteMessage.builder().reset().build() : "") + BaseSpacing.getInstance().get();
    }

    private String genSpacing(int i) {
        return " ".repeat(i);
    }
}
