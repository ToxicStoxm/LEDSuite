package com.toxicstoxm.LEDSuite.time;

import java.util.HashMap;

public class TimeManager {
    public static final HashMap<String, CallTracker> lastCallTracker = new HashMap<>();
    public static void initTimeTracker(String id, long debounce) {
        lastCallTracker.putIfAbsent(id, new CallTracker(debounce));
    }
    public static void initTimeTracker(String id, long debounce, long last) {
        lastCallTracker.putIfAbsent(id, new CallTracker(debounce, last));
    }
    public static void initTimeTracker(String id) {
        lastCallTracker.putIfAbsent(id, new CallTracker());
    }
    public static void clearTimeTracker(String id) {
        lastCallTracker.remove(id);
    }
    public static boolean call(String id) {
        if (!lastCallTracker.containsKey(id)) return false;
        return lastCallTracker.get(id).call();
    }
    public static boolean alternativeCall(String id) {
        if (!lastCallTracker.containsKey(id)) return true;
        return lastCallTracker.get(id).alternativeCall();
    }
    public static void ping(String id) {
        if (lastCallTracker.containsKey(id)) lastCallTracker.get(id).ping();
    }
    public static void setTimeTracker(String id, long last) {
        if (!lastCallTracker.containsKey(id)) return;
        lastCallTracker.get(id).last = last;
    }
    public static boolean call(String id, long current) {
        if (!lastCallTracker.containsKey(id)) return false;
        return lastCallTracker.get(id).call(current);
    }
    public static void lock(String id) {if (lastCallTracker.containsKey(id)) lastCallTracker.get(id).lock();}
    public static void release(String id) {if (lastCallTracker.containsKey(id)) lastCallTracker.get(id).release();}

    public static class CallTracker {
        private final long debounce;
        public long last;
        private boolean lock = false;
        public CallTracker() {
            this.debounce = 500;
            this.last = System.currentTimeMillis();
        }
        public CallTracker(long debounce) {
            this.debounce = debounce;
            this.last = System.currentTimeMillis();
        }
        public CallTracker(long debounce, long last) {
            this.debounce = debounce;
            this.last = last;
        }
        public boolean call() {
            if (lock) return false;
            long current = System.currentTimeMillis();
            if (current - last >= debounce) {
                this.last = current;
                return true;
            }
            return false;
        }
        public boolean call(long current) {
            if (lock) return false;
            if (current - last >= debounce) {
                this.last = current;
                return true;
            }
            return false;
        }
        public boolean alternativeCall() {
            if (lock) return false;
            boolean bool = (System.currentTimeMillis() - last) >= debounce;
            return lock = bool;
        }
        public void ping() {
            lock = false;
            this.last = System.currentTimeMillis();
        }
        public void lock() {
            lock = true;
        }
        public void release() {
            lock = false;
        }
    }
}
