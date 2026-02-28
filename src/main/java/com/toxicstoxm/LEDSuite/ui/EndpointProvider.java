package com.toxicstoxm.LEDSuite.ui;

import com.toxicstoxm.LEDSuite.tools.NullSaveGetter;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.DefaultSettingsDialogEndpoint;
import com.toxicstoxm.LEDSuite.ui.dialogs.settings_dialog.SettingsDialogEndpoint;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.DefaultStatusDialogEndpoint;
import com.toxicstoxm.LEDSuite.ui.dialogs.status_dialog.StatusDialogEndpoint;
import lombok.Builder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A provider class that manages the different endpoints used in the application.
 * It ensures the proper initialization, connection, and disconnection of endpoints like the settings dialog,
 * status dialog, and upload page, with fallback behavior in case of disconnection.
 *
 * <p>This class provides methods for accessing and managing the state of the following endpoints:</p>
 * <ul>
 *   <li>{@link SettingsDialogEndpoint}</li>
 *   <li>{@link StatusDialogEndpoint}</li>
 *   <li>{@link UploadPageEndpoint}</li>
 * </ul>
 *
 * <p>The endpoints are wrapped in a {@link NullSaveGetter}, allowing for the use of default implementations
 * if the actual endpoint is unavailable.</p>
 *
 * @since 1.0.0
 */
@Builder
public class EndpointProvider {
    // Default getters for each endpoint type, providing fallback behavior when disconnected
    @Builder.Default
    private NullSaveGetter<SettingsDialogEndpoint> settingsDialogEndpoint = generateDefaultGetterFor(new DefaultSettingsDialogEndpoint() {});

    @Builder.Default
    private NullSaveGetter<StatusDialogEndpoint> statusDialogEndpoint = generateDefaultGetterFor(new DefaultStatusDialogEndpoint() {});

    @Builder.Default
    private NullSaveGetter<UploadPageEndpoint> uploadPageEndpoint = generateDefaultGetterFor(new DefaultUploadPageEndpoint() {});

    /**
     * Retrieves the instance of the {@link SettingsDialogEndpoint}.
     *
     * @return The {@link SettingsDialogEndpoint} instance, or the default if unavailable.
     */
    protected @NotNull SettingsDialogEndpoint getSettingsDialogInstance() {
        return this.settingsDialogEndpoint.getInstance();
    }

    /**
     * Checks if the {@link SettingsDialogEndpoint} is currently connected.
     *
     * @return {@code true} if the settings dialog endpoint is connected, {@code false} otherwise.
     */
    protected boolean isSettingsDialogEndpointConnected() {
        return settingsDialogEndpoint.isAvailable();
    }

    /**
     * Connects a custom {@link SettingsDialogEndpoint} instance.
     *
     * <p>If {@code null} is passed, it will reset to the default implementation.</p>
     *
     * @param settingsDialogEndpoint The {@link SettingsDialogEndpoint} to connect, or {@code null} to reset to the default.
     */
    protected void connectSettingsDialogEndpoint(@Nullable SettingsDialogEndpoint settingsDialogEndpoint) {
        this.settingsDialogEndpoint = new NullSaveGetter<>() {
            @Override
            public SettingsDialogEndpoint value() {
                return settingsDialogEndpoint;
            }

            @Override
            public @NotNull SettingsDialogEndpoint defaultValue() {
                return new DefaultSettingsDialogEndpoint() {};
            }
        };
    }

    /**
     * Disconnects the current {@link SettingsDialogEndpoint} and resets to the default.
     */
    protected void disconnectSettingsDialogEndpoint() {
        settingsDialogEndpoint = generateDefaultGetterFor(new DefaultSettingsDialogEndpoint() {});
    }

    /**
     * Retrieves the instance of the {@link StatusDialogEndpoint}.
     *
     * @return The {@link StatusDialogEndpoint} instance, or the default if unavailable.
     */
    protected @NotNull StatusDialogEndpoint getStatusDialogInstance() {
        return this.statusDialogEndpoint.getInstance();
    }

    /**
     * Checks if the {@link StatusDialogEndpoint} is currently connected.
     *
     * @return {@code true} if the status dialog endpoint is connected, {@code false} otherwise.
     */
    protected boolean isStatusDialogEndpointConnected() {
        return statusDialogEndpoint.isAvailable();
    }

    /**
     * Connects a custom {@link StatusDialogEndpoint} instance.
     *
     * <p>If {@code null} is passed, it will reset to the default implementation.</p>
     *
     * @param statusDialogEndpoint The {@link StatusDialogEndpoint} to connect, or {@code null} to reset to the default.
     */
    protected void connectStatusDialogEndpoint(@Nullable StatusDialogEndpoint statusDialogEndpoint) {
        this.statusDialogEndpoint = new NullSaveGetter<>() {
            @Override
            public StatusDialogEndpoint value() {
                return statusDialogEndpoint;
            }

            @Override
            public @NotNull StatusDialogEndpoint defaultValue() {
                return new DefaultStatusDialogEndpoint() {};
            }
        };
    }

    /**
     * Disconnects the current {@link StatusDialogEndpoint} and resets to the default.
     */
    protected void disconnectStatusDialogEndpoint() {
        statusDialogEndpoint = generateDefaultGetterFor(new DefaultStatusDialogEndpoint() {});
    }

    /**
     * Retrieves the instance of the {@link UploadPageEndpoint}.
     *
     * @return The {@link UploadPageEndpoint} instance, or the default if unavailable.
     */
    protected @NotNull UploadPageEndpoint getUploadPageEndpoint() {
        return this.uploadPageEndpoint.getInstance();
    }

    /**
     * Checks if the {@link UploadPageEndpoint} is currently connected.
     *
     * @return {@code true} if the upload page endpoint is connected, {@code false} otherwise.
     */
    protected boolean isUploadPageEndpointConnected() {
        return uploadPageEndpoint.isAvailable();
    }

    /**
     * Connects a custom {@link UploadPageEndpoint} instance.
     *
     * <p>If {@code null} is passed, it will reset to the default implementation.</p>
     *
     * @param uploadPageEndpoint The {@link UploadPageEndpoint} to connect, or {@code null} to reset to the default.
     */
    protected void connectUploadPageEndpoint(@Nullable UploadPageEndpoint uploadPageEndpoint) {
        this.uploadPageEndpoint = new NullSaveGetter<>() {
            @Override
            public UploadPageEndpoint value() {
                return uploadPageEndpoint;
            }

            @Override
            public @NotNull UploadPageEndpoint defaultValue() {
                return new DefaultUploadPageEndpoint() {};
            }
        };
    }

    /**
     * Disconnects the current {@link UploadPageEndpoint} and resets to the default.
     */
    protected void disconnectUploadPageEndpoint() {
        uploadPageEndpoint = generateDefaultGetterFor(new DefaultUploadPageEndpoint() {});
    }

    /**
     * Generates a default {@link NullSaveGetter} for a given endpoint, providing fallback behavior when the instance is unavailable.
     *
     * @param defaultImplementation The default implementation to be used if the instance is {@code null}.
     * @param <T> The type of the endpoint.
     * @return A {@link NullSaveGetter} that returns the default implementation.
     */
    @Contract(value = "_ -> new", pure = true)
    private static <T> @NotNull NullSaveGetter<T> generateDefaultGetterFor(T defaultImplementation) {
        return new NullSaveGetter<T>() {
            @Override
            public T value() {
                return null;
            }

            @Override
            public @NotNull T defaultValue() {
                return defaultImplementation;
            }
        };
    }
}
