package com.x_tornado10.lccp.time;

import java.util.HashMap;

public class TimeManager {
    public static final HashMap<String, CallTracker> lastCallTracker = new HashMap<>();
    public static void initTimeTracker(String id, long debounce) {
        lastCallTracker.putIfAbsent(id, new CallTracker(debounce));
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
    public static boolean call(String id, long current) {
        if (!lastCallTracker.containsKey(id)) return false;
        return lastCallTracker.get(id).call(current);
    }

    public static class CallTracker {
        private final long debounce;
        public long last;
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
            long current = System.currentTimeMillis();
            if (current - last >= debounce) {
                this.last = current;
                return true;
            }
            return false;
        }
        public boolean call(long current) {
            if (current - last >= debounce) {
                this.last = current;
                return true;
            }
            return false;
        }
        public boolean alternativeCall() {
            return System.currentTimeMillis() - last <= debounce;
        }
        public void ping() {
            this.last = System.currentTimeMillis();
        }
    }
}
