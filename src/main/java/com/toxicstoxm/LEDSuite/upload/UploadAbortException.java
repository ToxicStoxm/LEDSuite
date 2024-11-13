package com.toxicstoxm.LEDSuite.upload;

import com.toxicstoxm.LEDSuite.time.Action;

public class UploadAbortException extends RuntimeException {
    private Action printMessage;

    public UploadAbortException(Action printMessage) {
        this.printMessage = printMessage;
    }

    public void printErrorMessage() {
        if (printMessage != null) {
            printMessage.run();
        }
    }

    public UploadAbortException(String message) {
        super(message);
    }

    public UploadAbortException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadAbortException(Throwable cause) {
        super(cause);
    }
}
