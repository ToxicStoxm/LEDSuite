package com.toxicstoxm.LEDSuite.ui;

import lombok.Builder;

/**
 * Wrapper for upload statistics.
 * Used by the animation upload implementation to display upload statistics in {@link UploadPage}.
 * @since 1.0.0
 * @param bytesPerSecond
 * @param millisecondsRemaining
 */
@Builder
public record UploadStatistics(long bytesPerSecond, long millisecondsRemaining) {}
