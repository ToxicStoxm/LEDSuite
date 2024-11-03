package com.toxicstoxm.LEDSuite.ui;

import lombok.Builder;

@Builder
public record UploadStatistics(long bytesPerSecond, long millisecondsRemaining) {
}
