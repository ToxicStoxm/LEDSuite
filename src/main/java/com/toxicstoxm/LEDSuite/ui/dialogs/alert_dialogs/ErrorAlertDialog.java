package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.Constants;
import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.tools.ExceptionTools;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import lombok.Builder;
import org.gnome.adw.ResponseAppearance;
import org.gnome.glib.GLib;
import org.gnome.gtk.UriLauncher;
import org.gnome.gtk.Widget;

import java.util.Objects;

public class ErrorAlertDialog {

    private static boolean disableReportResponse = false;

    private final AlertDialog<AlertDialogData> alertDialog;
    private static final AlertDialogResponse okResponse;
    private static final AlertDialogResponse issuesResponse;

    static {
        okResponse = AlertDialogResponse.builder()
                .id("ok")
                .label(Translations.getText("_OK"))
                .activated(true)
                .responseCallback(() -> LEDSuiteApplication.getLogger().verbose("Error acknowledged by the user!", new LEDSuiteLogAreas.USER_INTERACTIONS()))
                .build();

        issuesResponse = AlertDialogResponse.builder()
                .id("report")
                .label(Translations.getText("_Report"))
                .activated(!disableReportResponse)
                .appearance(ResponseAppearance.SUGGESTED)
                .responseCallback(() -> {
                    try {
                        UriLauncher launcher = UriLauncher.builder()
                                .setUri(Constants.Application.ISSUES)
                                .build();
                        launcher.launch(LEDSuiteApplication.getWindow().asApplicationWindow(), null, (_, _, _) ->
                                LEDSuiteApplication.getLogger().verbose("Error acknowledged and reported by the user!", new LEDSuiteLogAreas.USER_INTERACTIONS()));
                    } catch (Exception e) {
                        ErrorAlertDialog.disableReportResponse = true;
                        ExceptionTools.printStackTrace(e, message -> LEDSuiteApplication.getLogger().stacktrace(message, new LEDSuiteLogAreas.USER_INTERACTIONS()));
                        LEDSuiteApplication.handleError("An error occurred during opening issue URL!", new LEDSuiteLogAreas.USER_INTERACTIONS());
                    }
                })
                .build();
    }

    @Builder
    public ErrorAlertDialog(String errorMessage, String heading) {
        alertDialog = GeneralAlertDialog.create().configure(
                AlertDialogData.builder()
                        .body(
                                Objects.requireNonNullElse(
                                        errorMessage,
                                        Translations.getText("An error occurred!"))
                        )
                        .heading(
                                Objects.requireNonNullElse(
                                        heading,
                                        Translations.getText("Error"))
                        )
                        .response(okResponse)
                        .response(issuesResponse)
                        .build()
        );
    }


    public void present(Widget parent) {
        GLib.idleAddOnce(() -> alertDialog.present(parent));
    }

}
