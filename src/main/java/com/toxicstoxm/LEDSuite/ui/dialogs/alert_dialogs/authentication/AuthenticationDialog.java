package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.authentication;

import com.toxicstoxm.LEDSuite.authentication.Credentials;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteRunnable;
import com.toxicstoxm.LEDSuite.task_scheduler.LEDSuiteTask;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import io.github.jwharm.javagi.gtk.types.TemplateTypes;
import org.gnome.adw.AlertDialog;
import org.gnome.adw.EntryRow;
import org.gnome.adw.PasswordEntryRow;
import org.gnome.adw.ResponseAppearance;
import org.gnome.glib.GLib;
import org.gnome.glib.Type;
import org.gnome.gobject.GObject;
import org.gnome.gtk.Revealer;
import org.gnome.gtk.Widget;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.MemorySegment;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@GtkTemplate(name = "AuthenticationDialog", ui = "/com/toxicstoxm/LEDSuite/AuthenticationDialog.ui")
public class AuthenticationDialog extends AlertDialog {
    private static final Type gtype = TemplateTypes.register(AuthenticationDialog.class);

    public AuthenticationDialog(MemorySegment address) {
        super(address);
    }

    public static Type getType() {
        return gtype;
    }

    public static AuthenticationDialog create() {
        return GObject.newInstance(getType());
    }

    private LEDSuiteTask credentialCheckerTask;

    @GtkChild(name = "username_row")
    public EntryRow usernameRow;

    @GtkChild(name = "password_row")
    public PasswordEntryRow passwordRow;

    @GtkChild(name = "spinner_revealer")
    public Revealer spinnerRevealer;

    private LEDSuiteTask authenticationTimeoutTask;

    @Override
    protected void response(String response) {
        credentialCheckerTask.cancel();
        String username = usernameRow.getText();
        if (Objects.equals(response, "authenticate")) {
            spinnerRevealer.setRevealChild(true);

            String passwordHash;
            try {
                byte[] hash = MessageDigest.getInstance("SHA-256").digest(passwordRow.getText().getBytes(StandardCharsets.UTF_8));
                passwordHash = new BigInteger(1, hash).toString(16);
            } catch (NoSuchAlgorithmException e) {
                setCanClose(true);
                close();
                LEDSuiteApplication.getLogger().error("Authentication -> Username: " + username + " - Password hashing failed!", new LEDSuiteLogAreas.USER_INTERACTIONS());

                return;
            }

            usernameRow.setSensitive(false);
            passwordRow.setSensitive(false);
            setResponseEnabled("authenticate", false);
            setResponseEnabled("cancel", false);

            LEDSuiteApplication.getLogger().info("Authentication -> Username: " + username + " - Password Hash: " + passwordHash, new LEDSuiteLogAreas.USER_INTERACTIONS());


            LEDSuiteApplication.getAuthManager().requestAuth(
                    Credentials.builder()
                            .username(username)
                            .passwordHash(passwordHash)
                            .build(),
                    () -> GLib.idleAddOnce(() -> {
                        setCanClose(true);
                        close();
                    })
            );

            LEDSuiteApplication.getLogger().info("Authentication -> Waiting for server response...", new LEDSuiteLogAreas.USER_INTERACTIONS());

            authenticationTimeoutTask = new LEDSuiteRunnable() {
                @Override
                public void run() {

                    GLib.idleAddOnce(() -> {
                        spinnerRevealer.setRevealChild(false);
                        setResponseAppearance("authenticate", ResponseAppearance.DESTRUCTIVE);
                    });

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    GLib.idleAddOnce(() -> {
                        setCanClose(true);
                        close();
                        LEDSuiteApplication.getAuthManager().authResult(username, false);
                    });
                }
            }.runTaskLaterAsynchronously(10000);

        } else {
            setCanClose(true);
            close();
            LEDSuiteApplication.getAuthManager().authResult(username, false);
        }
    }

    @Override
    protected void closed() {
        super.closed();
        if (authenticationTimeoutTask != null) authenticationTimeoutTask.cancel();
    }

    @Override
    public void present(@Nullable Widget parent) {
        setCanClose(false);
        credentialCheckerTask = new LEDSuiteRunnable() {
            @Override
            public void run() {
                String username = usernameRow.getText();
                setResponseEnabled("authenticate", usernameRow != null && !username.isBlank());
            }
        }.runTaskTimerAsynchronously(10, 10);
        super.present(parent);
    }
}
