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

@Builder
public class EndpointProvider {

    @Builder.Default
    private NullSaveGetter<SettingsDialogEndpoint> settingsDialogEndpoint = generateDefaultGetterFor(new DefaultSettingsDialogEndpoint() {});

    @Builder.Default
    private NullSaveGetter<StatusDialogEndpoint> statusDialogEndpoint = generateDefaultGetterFor(new DefaultStatusDialogEndpoint() {});

    @Builder.Default
    private NullSaveGetter<UploadPageEndpoint> uploadPageEndpoint = generateDefaultGetterFor(new DefaultUploadPageEndpoint() {});

    protected @NotNull SettingsDialogEndpoint getSettingsDialogInstance() {return this.settingsDialogEndpoint.getInstance();}

    protected boolean isSettingsDialogEndpointConnected() {
        return settingsDialogEndpoint.isAvailable();
    }

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

    protected void disconnectSettingsDialogEndpoint() {
        settingsDialogEndpoint = generateDefaultGetterFor(new DefaultSettingsDialogEndpoint() {});
    }

    protected @NotNull StatusDialogEndpoint getStatusDialogInstance() {return this.statusDialogEndpoint.getInstance();}

    protected boolean isStatusDialogEndpointConnected() {
        return statusDialogEndpoint.isAvailable();
    }

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

    protected void disconnectStatusDialogEndpoint() {
        statusDialogEndpoint = generateDefaultGetterFor(new DefaultStatusDialogEndpoint() {});
    }

    protected @NotNull UploadPageEndpoint getUploadPageEndpoint() {return this.uploadPageEndpoint.getInstance();}

    protected boolean isUploadPageEndpointConnected() {
        return uploadPageEndpoint.isAvailable();
    }

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

    protected void disconnectUploadPageEndpoint() {
        uploadPageEndpoint = generateDefaultGetterFor(new DefaultUploadPageEndpoint() {});
    }

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
