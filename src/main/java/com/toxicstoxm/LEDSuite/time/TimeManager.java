package com.toxicstoxm.LEDSuite.time;

import java.util.HashMap;

/**
 * The {@code TimeManager} class provides a centralized mechanism for managing time-based operations,
 * including debouncing and tracking the last call time for various identifiers.
 * <p>
 * This class uses a {@link HashMap} to keep track of {@link CallTracker} instances associated with
 * unique identifiers. It allows for debouncing of operations, which helps in preventing repeated
 * actions within a specified time frame.
 * </p>
 * <p>
 * The {@code TimeManager} class is designed to be used in scenarios where time-based tracking of
 * events or operations is required, such as rate-limiting, event throttling, or scheduling.
 * </p>
 *
 * @since 1.0.0
 */
public class TimeManager {

    // A map that tracks the call status of various identifiers
    public static final HashMap<String, CallTracker> lastCallTracker = new HashMap<>();

    /**
     * Initializes a {@link CallTracker} for the specified identifier with a given debounce period.
     * If a tracker with the same identifier already exists, it will not be replaced.
     *
     * @param id       The unique identifier for the tracker.
     * @param debounce The debounce period in milliseconds.
     * @since 1.0.0
     */
    public static void initTimeTracker(String id, long debounce) {
        lastCallTracker.putIfAbsent(id, new CallTracker(debounce));
    }

    /**
     * Initializes a {@link CallTracker} for the specified identifier with a given debounce period
     * and last call time. If a tracker with the same identifier already exists, it will not be replaced.
     *
     * @param id       The unique identifier for the tracker.
     * @param debounce The debounce period in milliseconds.
     * @param last     The last call time in milliseconds.
     * @since 1.0.0
     */
    public static void initTimeTracker(String id, long debounce, long last) {
        lastCallTracker.putIfAbsent(id, new CallTracker(debounce, last));
    }

    /**
     * Initializes a {@link CallTracker} for the specified identifier with default settings.
     * If a tracker with the same identifier already exists, it will not be replaced.
     *
     * @param id The unique identifier for the tracker.
     * @since 1.0.0
     */
    public static void initTimeTracker(String id) {
        lastCallTracker.putIfAbsent(id, new CallTracker());
    }

    /**
     * Removes the {@link CallTracker} associated with the specified identifier.
     *
     * @param id The unique identifier for the tracker to be removed.
     * @since 1.0.0
     */
    public static void clearTimeTracker(String id) {
        lastCallTracker.remove(id);
    }

    /**
     * Performs a call operation for the specified identifier. If the {@link CallTracker} exists and
     * the debounce period has passed since the last call, it updates the last call time and returns {@code true}.
     * Otherwise, it returns {@code false}.
     *
     * @param id The unique identifier for the tracker.
     * @return {@code true} if the call is allowed based on the debounce period; {@code false} otherwise.
     * @since 1.0.0
     */
    public static boolean call(String id) {
        if (!lastCallTracker.containsKey(id)) return false;
        return lastCallTracker.get(id).call();
    }

    /**
     * Performs an alternative call operation for the specified identifier. This method returns {@code true}
     * if the tracker does not exist or if the debounce period has passed, otherwise it returns {@code false}.
     *
     * @param id The unique identifier for the tracker.
     * @return {@code true} if the alternative call is allowed; {@code false} otherwise.
     * @since 1.0.0
     */
    public static boolean alternativeCall(String id) {
        if (!lastCallTracker.containsKey(id)) return true;
        return lastCallTracker.get(id).alternativeCall();
    }

    /**
     * Resets the {@link CallTracker} associated with the specified identifier. This operation
     * sets the last call time to the current time and unlocks the tracker.
     *
     * @param id The unique identifier for the tracker to be reset.
     * @since 1.0.0
     */
    public static void ping(String id) {
        if (lastCallTracker.containsKey(id)) lastCallTracker.get(id).ping();
    }

    /**
     * Updates the last call time of the {@link CallTracker} associated with the specified identifier.
     *
     * @param id   The unique identifier for the tracker.
     * @param last The new last call time in milliseconds.
     * @since 1.0.0
     */
    public static void setTimeTracker(String id, long last) {
        if (!lastCallTracker.containsKey(id)) return;
        lastCallTracker.get(id).last = last;
    }

    /**
     * Performs a call operation for the specified identifier using a provided current time. If the
     * {@link CallTracker} exists and the debounce period has passed since the last call, it updates the
     * last call time and returns {@code true}. Otherwise, it returns {@code false}.
     *
     * @param id      The unique identifier for the tracker.
     * @param current The current time in milliseconds.
     * @return {@code true} if the call is allowed based on the debounce period; {@code false} otherwise.
     * @since 1.0.0
     */
    public static boolean call(String id, long current) {
        if (!lastCallTracker.containsKey(id)) return false;
        return lastCallTracker.get(id).call(current);
    }

    /**
     * Locks the {@link CallTracker} associated with the specified identifier, preventing further
     * calls until it is released.
     *
     * @param id The unique identifier for the tracker to be locked.
     * @since 1.0.0
     */
    public static void lock(String id) {
        if (lastCallTracker.containsKey(id)) lastCallTracker.get(id).lock();
    }

    /**
     * Releases the lock on the {@link CallTracker} associated with the specified identifier,
     * allowing further calls.
     *
     * @param id The unique identifier for the tracker to be released.
     * @since 1.0.0
     */
    public static void release(String id) {
        if (lastCallTracker.containsKey(id)) lastCallTracker.get(id).release();
    }

    /**
     * The {@code CallTracker} class tracks the timing and state of calls for a specific identifier.
     * It supports debouncing and locking mechanisms to manage the timing of repeated operations.
     * <p>
     * The {@code CallTracker} allows for debouncing of calls to prevent excessive operations within
     * a short period. It also provides methods to lock and unlock the tracker, which can be used to
     * control access based on certain conditions.
     * </p>
     *
     * @since 1.0.0
     */
    public static class CallTracker {
        private final long debounce; // The debounce period in milliseconds
        public long last; // The timestamp of the last call
        private boolean lock = false; // Indicates whether the tracker is locked

        /**
         * Constructs a new {@code CallTracker} with default debounce period.
         */
        public CallTracker() {
            this.debounce = 500;
            this.last = System.currentTimeMillis();
        }

        /**
         * Constructs a new {@code CallTracker} with a specified debounce period.
         *
         * @param debounce The debounce period in milliseconds.
         */
        public CallTracker(long debounce) {
            this.debounce = debounce;
            this.last = System.currentTimeMillis();
        }

        /**
         * Constructs a new {@code CallTracker} with a specified debounce period and last call time.
         *
         * @param debounce The debounce period in milliseconds.
         * @param last     The last call time in milliseconds.
         */
        public CallTracker(long debounce, long last) {
            this.debounce = debounce;
            this.last = last;
        }

        /**
         * Checks if the call can be made based on the debounce period. If the period has elapsed
         * since the last call, it updates the last call time and returns {@code true}. Otherwise, it returns {@code false}.
         *
         * @return {@code true} if the call is allowed based on the debounce period; {@code false} otherwise.
         * @since 1.0.0
         */
        public boolean call() {
            if (lock) return false;
            long current = System.currentTimeMillis();
            if (current - last >= debounce) {
                this.last = current;
                return true;
            }
            return false;
        }

        /**
         * Checks if the call can be made based on the debounce period using a provided current time.
         * If the period has elapsed since the last call, it updates the last call time and returns {@code true}.
         * Otherwise, it returns {@code false}.
         *
         * @param current The current time in milliseconds.
         * @return {@code true} if the call is allowed based on the debounce period; {@code false} otherwise.
         * @since 1.0.0
         */
        public boolean call(long current) {
            if (lock) return false;
            if (current - last >= debounce) {
                this.last = current;
                return true;
            }
            return false;
        }

        /**
         * Checks if an alternative call can be made based on the debounce period.
         * If the period has elapsed since the last call, it updates the lock status and returns {@code true}.
         * Otherwise, it returns {@code false}.
         *
         * @return {@code true} if the alternative call is allowed; {@code false} otherwise.
         * @since 1.0.0
         */
        public boolean alternativeCall() {
            if (lock) return false;
            boolean bool = (System.currentTimeMillis() - last) >= debounce;
            return lock = bool;
        }

        /**
         * Resets the tracker by setting the last call time to the current time and unlocking the tracker.
         *
         * @since 1.0.0
         */
        public void ping() {
            lock = false;
            this.last = System.currentTimeMillis();
        }

        /**
         * Locks the tracker, preventing further calls until it is released.
         *
         * @since 1.0.0
         */
        public void lock() {
            lock = true;
        }

        /**
         * Releases the lock on the tracker, allowing further calls.
         *
         * @since 1.0.0
         */
        public void release() {
            lock = false;
        }
    }
}
