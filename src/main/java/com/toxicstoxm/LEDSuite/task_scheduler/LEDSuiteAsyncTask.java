package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Represents an asynchronous task that can be scheduled to run with a delay.
 * Manages its own workers and handles execution in a separate thread.
 *
 * @implNote This is inspired by the <a href=""></a>
 * @since 1.0.0
 */
public class LEDSuiteAsyncTask extends LEDSuiteTask {

    // List of workers handling this async task
    private final LinkedList<LEDSuiteWorker> workers = new LinkedList<>();
    // Map of all scheduled tasks
    private final Map<Integer, LEDSuiteTask> runners;

    /**
     * Constructor for creating a new asynchronous task with additional YAML configuration.
     *
     * @param runners The map of all scheduled tasks.
     * @param task The task to run.
     * @param id The unique identifier for the task.
     * @param delay The delay before the task runs.
     * @since 1.0.0
     */
    LEDSuiteAsyncTask(final Map<Integer, LEDSuiteTask> runners, final Runnable task, final int id, final long delay) {
        super(task, id, delay);
        this.runners = runners;
    }

    /**
     * Indicates that this task is asynchronous and not synchronous.
     *
     * @return `false`, as this is an asynchronous task.
     * @since 1.0.0
     */
    @Override
    public boolean isSync() {
        return false;
    }

    /**
     * Executes the task in a separate thread. Manages worker threads and handles exceptions.
     */
    @Override
    public void run() {
        // Get the current thread executing the task
        final Thread thread = Thread.currentThread();
        synchronized (workers) {
            if (getPeriod() == -2) {
                // If the task has been canceled, stop execution.
                // Synchronizing here is important to avoid race conditions.
                return;
            }
            // Add a new worker to the list
            workers.add(
                    new LEDSuiteWorker() {
                        @Override
                        public Thread getThread() {
                            return thread;
                        }

                        @Override
                        public int getTaskId() {
                            return LEDSuiteAsyncTask.this.getTaskId();
                        }
                    });
        }

        Throwable thrown = null;
        try {
            // Run the task
            super.run();
        } catch (final Throwable t) {
            thrown = t;
            throw new RuntimeException(
                    String.format(
                            "Thread %s generated an exception while executing task %s",
                            LEDSuiteApplication.class.getName(),
                            getTaskId()),
                    thrown);
        } finally {
            // Clean up the workers list after task completion
            synchronized (workers) {
                try {
                    final Iterator<LEDSuiteWorker> workersIterator = this.workers.iterator();
                    boolean removed = false;
                    while (workersIterator.hasNext()) {
                        if (workersIterator.next().getThread() == thread) {
                            workersIterator.remove();
                            removed = true; // Task successfully removed
                            break;
                        }
                    }
                    if (!removed) {
                        throw new IllegalStateException(
                                String.format(
                                        "Unable to remove worker %s on task %s for %s",
                                        thread.getName(),
                                        getTaskId(),
                                        LEDSuiteApplication.class.getName()),
                                thrown); // Preserve the original exception, if any
                    }
                } finally {
                    // If the task has no period and workers list is empty, remove the task from the runner map
                    if (getPeriod() < 0 && workers.isEmpty()) {
                        runners.remove(getTaskId());
                    }
                }
            }
        }
    }

    /**
     * Gets the list of workers associated with this task.
     *
     * @return The list of workers.
     * @since 1.0.0
     */
    LinkedList<LEDSuiteWorker> getWorkers() {
        return workers;
    }

    /**
     * Cancels the task and removes it from the runners map if necessary.
     *
     * @return `true` if the task was successfully canceled.
     * @since 1.0.0
     */
    boolean cancel0() {
        synchronized (workers) {
            // Synchronize to prevent race conditions with a completing task
            setPeriod(-2L);
            if (workers.isEmpty()) {
                runners.remove(getTaskId());
            }
        }
        return true;
    }
}
