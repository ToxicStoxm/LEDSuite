package com.toxicstoxm.LEDSuite.task_scheduler;

import java.util.concurrent.*;

/**
 * The `LEDSuiteFuture` class represents a future result of an asynchronous computation
 * within the LEDSuite task scheduler. It extends `LEDSuiteTask` and implements `Future<T>`.
 *
 * <p>This class is used to manage the lifecycle and state of a callable task, including
 * its execution, cancellation, and retrieval of the result.
 *
 * @param <T> The type of the result returned by this future.
 * @since 1.0.0
 */
public class LEDSuiteFuture<T> extends LEDSuiteTask implements Future<T> {
    private final Callable<T> callable;
    private T value;
    private Exception exception = null;

    /**
     * Constructs a new `LEDSuiteFuture` for the given callable task with the specified ID.
     *
     * @param callable The callable task to be executed.
     * @param id       The unique identifier for this task.
     * @since 1.0.0
     */
    LEDSuiteFuture(final Callable<T> callable, final int id) {
        super(null, id, -1L);
        this.callable = callable;
    }

    /**
     * Attempts to cancel the execution of this task.
     *
     * @param mayInterruptIfRunning If true, the thread executing this task should be interrupted.
     * @return true if the task was canceled, false otherwise.
     * @since 1.0.0
     */
    public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
        if (getPeriod() != -1L) {
            return false;
        }
        setPeriod(-2L);
        return true;
    }

    /**
     * Checks if the task has been canceled.
     *
     * @return true if the task was canceled, false otherwise.
     * @since 1.0.0
     */
    public boolean isCancelled() {
        return getPeriod() == -2L;
    }

    /**
     * Checks if the task is done.
     *
     * @return true if the task is done, false otherwise.
     * @since 1.0.0
     */
    public boolean isDone() {
        final long period = this.getPeriod();
        return period != -1L && period != -3L;
    }

    /**
     * Retrieves the result of the task, waiting if necessary for the computation to complete.
     *
     * @return The computed result.
     * @throws CancellationException If the task was canceled.
     * @throws InterruptedException  If the current thread was interrupted while waiting.
     * @throws ExecutionException    If the computation threw an exception.
     * @since 1.0.0
     */
    public T get() throws CancellationException, InterruptedException, ExecutionException {
        try {
            return get(0, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
            throw new Error(e);
        }
    }

    /**
     * Retrieves the result of the task, waiting if necessary for the specified timeout for the computation to complete.
     *
     * @param timeout The maximum time to wait.
     * @param unit    The time unit of the timeout argument.
     * @return The computed result.
     * @throws CancellationException If the task was canceled.
     * @throws InterruptedException  If the current thread was interrupted while waiting.
     * @throws ExecutionException    If the computation threw an exception.
     * @throws TimeoutException      If the wait timed out.
     * @since 1.0.0
     */
    public synchronized T get(long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        timeout = unit.toMillis(timeout);
        long period = this.getPeriod();
        long timestamp = timeout > 0 ? System.currentTimeMillis() : 0L;
        while (true) {
            if (period == -1L || period == -3L) {
                this.wait(timeout);
                period = this.getPeriod();
                if (period == -1L || period == -3L) {
                    if (timeout == 0L) {
                        continue;
                    }
                    timeout += timestamp - (timestamp = System.currentTimeMillis());
                    if (timeout > 0) {
                        continue;
                    }
                    throw new TimeoutException();
                }
            }
            if (period == -2L) {
                throw new CancellationException();
            }
            if (period == -4L) {
                if (exception == null) {
                    return value;
                }
                throw new ExecutionException(exception);
            }
            throw new IllegalStateException("Expected " + -1L + " to " + -4L + ", got " + period);
        }
    }

    /**
     * Executes the task.
     *
     * <p>This method is intended to be called by the task scheduler. It manages the execution state
     * and ensures that the result or any exception is properly stored.
     *
     * @since 1.0.0
     */
    @Override
    public void run() {
        synchronized (this) {
            if (getPeriod() == -2L) {
                return;
            }
            setPeriod(-3L);
        }
        try {
            value = callable.call();
        } catch (final Exception e) {
            exception = e;
        } finally {
            synchronized (this) {
                setPeriod(-4L);
                this.notifyAll();
            }
        }
    }

    /**
     * Attempts to cancel the task without interrupting it.
     *
     * @return true if the task was canceled, false otherwise.
     * @since 1.0.0
     */
    synchronized boolean cancel0() {
        if (getPeriod() != -1L) {
            return false;
        }
        setPeriod(-2L);
        notifyAll();
        return true;
    }
}
