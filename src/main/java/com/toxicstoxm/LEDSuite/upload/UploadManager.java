package com.toxicstoxm.LEDSuite.upload;

import java.util.HashMap;

public class UploadManager {

    private final HashMap<String, Upload> pendingUploads;

    public UploadManager() {
        pendingUploads = new HashMap<>();
    }

    public void setPending(String fileName, Upload upload) {
        pendingUploads.putIfAbsent(fileName, upload);
    }

    public boolean removePending(String fileName) {
        return pendingUploads.remove(fileName) != null;
    }

    public void changePendingName(String oldName, String newName) {
        Upload associated = pendingUploads.remove(oldName);
        if (associated != null) pendingUploads.put(newName, associated);
    }
    public void call(String name, boolean uploadPermitted) {
        if (pendingUploads.containsKey(name)) pendingUploads.get(name).onServerResponse(uploadPermitted);
    }
}
