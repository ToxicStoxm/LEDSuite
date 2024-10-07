package com.toxicstoxm.LEDSuite.task_scheduler;

import java.util.concurrent.*;

public class TaskScheduler {
    private final ScheduledExecutorService asyncExecutor;
    private final ExecutorService syncExecutor;
    private final BlockingQueue<Runnable> syncTaskQueue;

    public TaskScheduler() {
        // A single-threaded executor to handle synchronous tasks
        this.syncExecutor = Executors.newSingleThreadExecutor();
        this.syncTaskQueue = new LinkedBlockingQueue<>();

        // A scheduled thread pool for asynchronous tasks
        this.asyncExecutor = Executors.newScheduledThreadPool(4); // You can adjust the thread pool size
        startSyncTaskRunner();
    }

    private void startSyncTaskRunner() {
        // Starts the synchronous task runner in a loop to process tasks one by one
        syncExecutor.submit(() -> {
            while (true) {
                try {
                    Runnable task = syncTaskQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public void scheduleSyncTask(SchedulerTask task, long delayMillis) {
        syncExecutor.submit(() -> {
            try {
                Thread.sleep(delayMillis);
                syncTaskQueue.put(task::run);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void scheduleAsyncTask(SchedulerTask task, long delayMillis) {
        asyncExecutor.schedule(task::run, delayMillis, TimeUnit.MILLISECONDS);
    }

    public void scheduleRepeatingAsyncTask(SchedulerTask task, long initialDelayMillis, long periodMillis) {
        asyncExecutor.scheduleAtFixedRate(task::run, initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        asyncExecutor.shutdown();
        syncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
            if (!syncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                syncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            syncExecutor.shutdownNow();
        }
    }
}
