package com.toxicstoxm.LEDSuite.scheduler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SmartRunnable implements Runnable, Schedulable {
    @Getter
    private final StackTraceElement[] creationOrigin;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private StackTraceElement[] lastStarted;

    @Getter(AccessLevel.PROTECTED)
    private Task ref;

    protected void setRef(Task ref) {
        if (selfInit.get()) throw new UnsupportedOperationException("Self initialized runnable can not be reused!");
        this.ref = ref;
    }

    @Getter
    private final String name;

    private final AtomicBoolean selfInit = new AtomicBoolean(false);

    protected boolean isSelfInit() {
        return selfInit.get();
    }

    public SmartRunnable() {
        this(null);
    }

    public SmartRunnable(String name) {
        creationOrigin = Thread.currentThread().getStackTrace();
        this.name = name;
    }

    private void checkInitialized() {
        if (getRef() == null) throw new UnsupportedOperationException("SmartRunnable can only be used through the LEDSuiteTask API!");
    }

    public void cancel() {
        checkInitialized();
        getRef().cancel();
    }

    public void cancelForced() {
        checkInitialized();
        getRef().cancelForced();
    }

    private void selfInit() {
        setRef(new Task(this));
        this.selfInit.set(true);
    }

    @Override
    public void runTask() {
        selfInit();
        ref.runTask();
    }

    public void runTaskLater(long delayMillis) {
        runTaskLater(Duration.ofMillis(delayMillis));
    }

    @Override
    public void runTaskLater(Duration delay) {
        selfInit();
        ref.runTaskLater(delay);
    }

    public void runTaskTimer(long intervalMillis) {
        runTaskTimer(Duration.ofMillis(intervalMillis));
    }

    @Override
    public void runTaskTimer(Duration interval) {
        selfInit();
        ref.runTaskTimer(interval);
    }

    public void runTaskTimerLater(long delayMillis, long intervalMillis) {
        runTaskTimerLater(Duration.ofMillis(delayMillis), Duration.ofMillis(intervalMillis));
    }

    @Override
    public void runTaskTimerLater(Duration delay, Duration interval) {
        selfInit();
        ref.runTaskTimerLater(delay, interval);
    }

    @Override
    public void runTaskAsync() {
        selfInit();
        ref.runTaskAsync();
    }

    public void runTaskLaterAsync(long delayMillis) {
        runTaskLaterAsync(Duration.ofMillis(delayMillis));
    }

    @Override
    public void runTaskLaterAsync(Duration delay) {
        selfInit();
        ref.runTaskLaterAsync(delay);
    }

    public void runTaskTimerAsync(long intervalMillis) {
        runTaskTimerAsync(Duration.ofMillis(intervalMillis));
    }

    @Override
    public void runTaskTimerAsync(Duration interval) {
        selfInit();
        ref.runTaskTimerAsync(interval);
    }

    public void runTaskTimerLaterAsync(long delayMillis, long intervalMillis) {
        runTaskTimerLaterAsync(Duration.ofMillis(delayMillis), Duration.ofMillis(intervalMillis));
    }

    @Override
    public void runTaskTimerLaterAsync(Duration delay, Duration interval) {
        selfInit();
        ref.runTaskTimerLaterAsync(delay, interval);
    }

    public void join() throws InterruptedException {
        checkInitialized();
        ref.join();
    }

    public void join(Duration duration) throws InterruptedException {
        checkInitialized();
        ref.join(duration);
    }

    @Override
    public String toString() {
        return name != null ? name : super.toString();
    }
}
