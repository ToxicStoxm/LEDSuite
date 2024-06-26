package com.x_tornado10.lccp.task_scheduler;

import com.x_tornado10.lccp.LCCP;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class LCCPAsyncTask extends LCCPTask {

    private final LinkedList<LCCPWorker> workers = new LinkedList<>();
    private final Map<Integer, LCCPTask> runners;

    LCCPAsyncTask(final Map<Integer, LCCPTask> runners, final Runnable task, final int id, final long delay) {
        super(task, id, delay);
        this.runners = runners;
    }
    LCCPAsyncTask(final Map<Integer, LCCPTask> runners, final Runnable task, final InputStream is, final int id, final long delay) {
        super(task, is, id, delay);
        this.runners = runners;
    }

    @Override
    public boolean isSync() {
        return false;
    }

    @Override
    public void run() {
        final Thread thread = Thread.currentThread();
        synchronized(workers) {
            if (getPeriod() == -2) {
                // Never continue running after cancelled.
                // Checking this with the lock is important!
                return;
            }
            workers.add(
                    new LCCPWorker() {
                        public Thread getThread() {
                            return thread;
                        }

                        public int getTaskId() {
                            return LCCPAsyncTask.this.getTaskId();
                        }
                    });
        }
        Throwable thrown = null;
        try {
            super.run();
        } catch (final Throwable t) {
            thrown = t;
            throw new RuntimeException (
                    String.format(
                            "Plugin %s generated an exception while executing task %s",
                            LCCP.class.getName(),
                            getTaskId()),
                    thrown);
        } finally {
            // Cleanup is important for any async task, otherwise ghost tasks are everywhere
            synchronized(workers) {
                try {
                    final Iterator<LCCPWorker> workers = this.workers.iterator();
                    boolean removed = false;
                    while (workers.hasNext()) {
                        if (workers.next().getThread() == thread) {
                            workers.remove();
                            removed = true; // Don't throw exception
                            break;
                        }
                    }
                    if (!removed) {
                        throw new IllegalStateException(
                                String.format(
                                        "Unable to remove worker %s on task %s for %s",
                                        thread.getName(),
                                        getTaskId(),
                                        LCCP.class.getName()),
                                thrown); // We don't want to lose the original exception, if any
                    }
                } finally {
                    if (getPeriod() < 0 && workers.isEmpty()) {
                        // At this spot, we know we are the final async task being executed!
                        // Because we have the lock, nothing else is running or will run because delay < 0
                        runners.remove(getTaskId());
                    }
                }
            }
        }
    }

    LinkedList<LCCPWorker> getWorkers() {
        return workers;
    }

    boolean cancel0() {
        synchronized (workers) {
            // Synchronizing here prevents race condition for a completing task
            setPeriod(-2L);
            if (workers.isEmpty()) {
                runners.remove(getTaskId());
            }
        }
        return true;
    }
}