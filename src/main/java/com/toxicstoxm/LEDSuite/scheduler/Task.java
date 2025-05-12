package com.toxicstoxm.LEDSuite.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Task implements Schedulable {

    @Setter
    @Getter
    private static boolean debug = true;

    private static ExceptionHandler defaultExceptionHandler = (task, t) -> {
        System.out.println("Exception occurred while executing task " + task.runnable.toString());
        t.printStackTrace();
        if (debug) {
            System.out.println("-----------------------< DEBUG >-----------------------");
            System.out.println("---------------------< Created at >--------------------");
            for (StackTraceElement trace : task.runnable.getCreationOrigin()) {
                System.out.println(trace.toString());
            }
            System.out.println("-----------------< Last started from >-----------------");
            for (StackTraceElement trace : task.runnable.getLastStarted()) {
                System.out.println(trace.toString());
            }
            System.out.println("-------------------------------------------------------");

        }
        return false;
    };

    private final SmartRunnable runnable;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean async = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<Thread> runner = new AtomicReference<>();
    private final AtomicReference<ExceptionHandler> exceptionHandler = new AtomicReference<>();

    public static void setDefaultExceptionHandler(@NotNull ExceptionHandler exceptionHandler) {
        defaultExceptionHandler = exceptionHandler;
    }

    public StackTraceElement[] getCreationTrace() {
        return runnable.getCreationOrigin() != null ? runnable.getCreationOrigin() : new StackTraceElement[]{};
    }

    public StackTraceElement[] getLastCalledStack() {
        return runnable.getLastStarted() != null ? runnable.getLastStarted() : new StackTraceElement[]{};
    }

    public String getName() {
        return runnable != null ? runnable.toString() : toString();
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler.set(exceptionHandler);
    }

    private void startRunner() {
        if (debug) runnable.setLastStarted(Thread.currentThread().getStackTrace());
        async.set(true);
        runner.get().start();
    }

    private void newRunner(Thread runner) {
        checkRunning();
        this.runner.set(runner);
    }

    private void startRunner(Thread runner) {
        newRunner(runner);
        startRunner();
    }

    private void killRunner() {
        runner.get().interrupt();
    }

    private void checkRunning() throws AlreadyStartedException {
        if (running.get()) throw new AlreadyStartedException("Cannot run task " + runnable.toString() + " twice!");
    }

    public Task(@NotNull SmartRunnable runnable) {
        this.runnable = runnable;
        if (!runnable.isSelfInit()) this.runnable.setRef(this);
    }

    public void runTask() {
        checkRunning();
        running.set(true);
        if (debug && !async.get()) runnable.setLastStarted(Thread.currentThread().getStackTrace());
        try {
            runnable.run();
        } catch (Throwable t) {
            if (!handleException(t)) throw t;
        } finally {
            running.set(false);
            async.set(false);
        }
    }

    public void runTaskLater(long delayMillis) {
       runTaskLater(Duration.ofMillis(delayMillis));
    }

    public void runTaskLater(Duration delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            if (!handleException(e)) return;
        }
        runTask();
    }

    public void runTaskTimer(long intervalMills) {
        runTaskTimer(Duration.ofMillis(intervalMills));
    }

    public void runTaskTimer(Duration interval) {
        while (!cancelled.get()) {
            runTask();
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                if (!handleException(e)) return;
            }
        }
    }

    public void runTaskTimerLater(long delayMills, long intervalMills) {
        runTaskTimerLater(Duration.ofMillis(delayMills), Duration.ofMillis(intervalMills));
    }

    public void runTaskTimerLater(Duration delay, Duration interval) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            if (!handleException(e)) return;
        }
        runTaskTimer(interval);
    }

    public void runTaskAsync() {
        startRunner(new Thread(this::runTask));
    }

    public void runTaskLaterAsync(long delayMills) {
        runTaskLaterAsync(Duration.ofMillis(delayMills));
    }

    public void runTaskLaterAsync(Duration delay) {
        startRunner(new Thread(() -> runTaskLater(delay)));
    }

    public void runTaskTimerAsync(long intervalMillis) {
        runTaskTimerAsync(Duration.ofMillis(intervalMillis));
    }

    public void runTaskTimerAsync(Duration interval) {
        startRunner(new Thread(() -> runTaskTimer(interval)));
    }

    public void runTaskTimerLaterAsync(long delayMills, long intervalMillis) {
        runTaskTimerLaterAsync(Duration.ofMillis(delayMills), Duration.ofMillis(intervalMillis));
    }

    public void runTaskTimerLaterAsync(Duration delay, Duration interval) {
        startRunner(new Thread(() -> runTaskTimerLater(delay, interval)));
    }

    public void cancel() {
        cancelled.set(true);
    }

    public void cancelForced() {
        cancel();
        if (runner.get() != null) {
            runner.get().interrupt();
        }
    }

    private boolean handleException(@NotNull Throwable t) {
        ExceptionHandler customHandler = this.exceptionHandler.get();
        return customHandler != null ?
                customHandler.handleException(this, t) :
                defaultExceptionHandler.handleException(this, t);
    }

    public void join() throws InterruptedException {
        Thread runner = this.runner.get();
        if (runner != null) runner.join();
    }

    public void join(Duration duration) throws InterruptedException {
        Thread runner = this.runner.get();
        if (runner != null) runner.join(duration);
    }
}
