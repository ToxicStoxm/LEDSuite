package com.toxicstoxm.LEDSuite.upload;

import java.util.HashMap;

/**
 * Manages the state of pending uploads by storing them in a map where the key is the file name
 * and the value is an {@link Upload} instance associated with that file.
 * <p>
 * This class allows setting, removing, renaming, and invoking actions on pending uploads based on their file names.
 * </p>
 *
 * @since 1.0.0
 */
public class UploadManager {

    /** A map holding pending uploads where the key is the file name and the value is the corresponding upload action. */
    private final HashMap<String, Upload> pendingUploads;

    /**
     * Constructs a new {@code UploadManager} with an empty set of pending uploads.
     */
    public UploadManager() {
        pendingUploads = new HashMap<>();
    }

    /**
     * Adds a new upload to the pending uploads list if it does not already exist for the given file name.
     *
     * @param fileName The name of the file to be uploaded.
     * @param upload The {@link Upload} instance that will be called when the server responds.
     */
    public void setPending(String fileName, Upload upload) {
        // If the file is not already pending, add it to the map
        pendingUploads.putIfAbsent(fileName, upload);
    }

    /**
     * Removes a pending upload by its file name.
     *
     * @param fileName The name of the file to be removed from the pending uploads.
     * @return {@code true} if the upload was removed, {@code false} if no upload with the given file name existed.
     */
    public boolean removePending(String fileName) {
        // Remove and return true if the file was in the map
        return pendingUploads.remove(fileName) != null;
    }

    /**
     * Changes the file name associated with an existing pending upload.
     *
     * @param oldName The current file name of the pending upload.
     * @param newName The new file name to be associated with the upload.
     */
    public void changePendingName(String oldName, String newName) {
        // Remove the old entry and add the new entry with the same upload action
        Upload associated = pendingUploads.remove(oldName);
        if (associated != null) {
            pendingUploads.put(newName, associated);
        }
    }

    /**
     * Calls the {@link Upload#onServerResponse(boolean)} method of the upload associated with the given file name.
     *
     * @param name The name of the file to check in the pending uploads.
     * @param uploadPermitted The permission status from the server (whether the upload is permitted).
     */
    public void call(String name, boolean uploadPermitted) {
        // If the file exists in pending uploads, trigger the associated upload's server response
        if (pendingUploads.containsKey(name)) {
            pendingUploads.get(name).onServerResponse(uploadPermitted);
        }
    }
}
