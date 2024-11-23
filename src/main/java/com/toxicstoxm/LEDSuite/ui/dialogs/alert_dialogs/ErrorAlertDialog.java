package com.toxicstoxm.LEDSuite.ui.dialogs.alert_dialogs;

import com.toxicstoxm.LEDSuite.gettext.Translations;
import com.toxicstoxm.LEDSuite.logger.LEDSuiteLogAreas;
import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import lombok.Builder;
import org.gnome.adw.ResponseAppearance;
import org.gnome.gtk.Widget;

@Builder
public class ErrorAlertDialog {

    @Builder.Default
    private String errorMessage = Translations.getText("An error occurred!");

    @Builder.Default
    private String heading = Translations.getText("Error");

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
                .activated(true)
                .appearance(ResponseAppearance.SUGGESTED)
                .responseCallback(() -> LEDSuiteApplication.getLogger().verbose("Error acknowledged and reported by the user!", new LEDSuiteLogAreas.USER_INTERACTIONS()))
                .build();
    }

    public ErrorAlertDialog(String errorMessage, String heading) {
        this.errorMessage = errorMessage;
        this.heading = heading;

        alertDialog = GeneralAlertDialog.create().configure(
                AlertDialogData.builder()
                        .body(errorMessage)
                        .heading(heading)
                        .response(okResponse)
                        .response(issuesResponse)
                        .build()
        );
    }


    public void present(Widget parent) {
        alertDialog.present(parent);
    }

}
