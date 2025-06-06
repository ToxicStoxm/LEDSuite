package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.authentication;

import com.toxicstoxm.LEDSuite.authentication.Credentials;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.scheduler.SmartRunnable;
import com.toxicstoxm.LEDSuite.scheduler.Task;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs.ErrorData;
import com.toxicstoxm.YAJL.Logger;
import io.github.jwharm.javagi.gtk.annotations.GtkChild;
import io.github.jwharm.javagi.gtk.annotations.GtkTemplate;
import org.gnome.adw.AlertDialog;
import org.gnome.adw.EntryRow;
import org.gnome.adw.PasswordEntryRow;
import org.gnome.adw.ResponseAppearance;
import org.gnome.glib.GLib;
import org.gnome.gtk.Revealer;
import org.gnome.gtk.Widget;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Represents an authentication dialog that prompts the user to enter a username and password.
 * <p>
 * This dialog is used for authenticating a user by checking their credentials with a server.
 * The dialog shows a password entry field, a username entry field, and a spinner while awaiting
 * authentication. The dialog also handles authentication timeouts and user input validation.
 * </p>
 * <p>
 * Template file: {@code AuthenticationDialog.ui}
 * </p>
 *
 * @since 1.0.0
 */
@GtkTemplate(name = "AuthenticationDialog", ui = "/com/toxicstoxm/LEDSuite/AuthenticationDialog.ui")
public class AuthenticationDialog extends AlertDialog {

    private static final Logger logger = Logger.autoConfigureLogger();

    // The task responsible for checking user credentials
    private Task credentialCheckerTask;

    // Entry fields for username and password
    @GtkChild(name = "username_row")
    public EntryRow usernameRow;

    @GtkChild(name = "password_row")
    public PasswordEntryRow passwordRow;

    // Revealer for displaying the spinner during authentication
    @GtkChild(name = "spinner_revealer")
    public Revealer spinnerRevealer;

    // Task for handling authentication timeout
    private Task authenticationTimeoutTask;

    /**
     * Handles the response when the user interacts with the dialog.
     * This method processes the authentication request and manages the spinner display.
     * If authentication is successful, it closes the dialog. If authentication fails, the dialog
     * closes after a timeout.
     *
     * @param response the response string, which can either be "authenticate" or "cancel"
     */
    @Override
    protected void response(String response) {
        credentialCheckerTask.cancel();  // Cancel the credential checking task if response is received

        String username = usernameRow.getText();

        if (Objects.equals(response, "authenticate")) {
            spinnerRevealer.setRevealChild(true); // Show the spinner

            String passwordHash;
            try {
                // Hash the password using SHA-256
                byte[] hash = MessageDigest.getInstance("SHA-256").digest(passwordRow.getText().getBytes(StandardCharsets.UTF_8));
                passwordHash = new BigInteger(1, hash).toString(16);
            } catch (NoSuchAlgorithmException e) {
                // If password hashing fails, close the dialog and log the error
                setCanClose(true);
                close();
                LEDSuiteApplication.handleError(
                        ErrorData.builder()
                                .message(Translations.getText("Authentication -> Username: $ - Password hashing failed!", username))
                                .build()
                );
                return;
            }

            // Disable the input fields while the authentication request is being processed
            usernameRow.setSensitive(false);
            passwordRow.setSensitive(false);

            // Disable buttons to prevent further interaction
            setResponseEnabled("authenticate", false);
            setResponseEnabled("cancel", false);

            logger.info("Authentication -> Username: {} - Password Hash: {}", username, passwordHash);

            // Send the authentication request to the server
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

            logger.info("Authentication -> Waiting for server response...");

            // Start a task to handle authentication timeout
            authenticationTimeoutTask = new Task(new SmartRunnable() {
                @Override
                public void run() {
                    // After timeout, hide the spinner and change the response appearance
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
            });
            authenticationTimeoutTask.runTaskLaterAsync(10000); // 10-second timeout

        } else {
            // If canceled, close the dialog immediately
            setCanClose(true);
            close();
            LEDSuiteApplication.getAuthManager().authResult(username, false);
        }
    }

    /**
     * Cleans up when the dialog is closed. Cancels the authentication timeout task if it is still running.
     */
    @Override
    protected void closed() {
        super.closed();
        if (authenticationTimeoutTask != null) authenticationTimeoutTask.cancel();
    }

    /**
     * Presents the authentication dialog, enabling the response button based on the validity of the username.
     * This method also starts a background task to monitor the username input.
     *
     * @param parent the parent widget to attach this dialog to, or {@code null} if no parent is provided
     */
    @Override
    public void present(@Nullable Widget parent) {
        setCanClose(false);  // Prevent closing while waiting for authentication

        // Start a task to monitor the username field and enable the authenticate button when valid
        credentialCheckerTask = new Task(new SmartRunnable() {
            @Override
            public void run() {
                String username = usernameRow.getText();
                setResponseEnabled("authenticate", usernameRow != null && !username.isBlank());
            }
        });  // Check every 10 ms
        credentialCheckerTask.runTaskTimerLaterAsync(10, 10);

        super.present(parent);
    }
}
