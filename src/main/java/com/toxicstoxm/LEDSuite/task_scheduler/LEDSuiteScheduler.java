package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.ui.LEDSuiteApplication;
import com.toxicstoxm.YAJL.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The `LEDSuiteScheduler` class implements the `TaskScheduler` interface and manages the scheduling and execution
 * of tasks within the LEDSuite application.
 *
 * <p>This class handles both synchronous and asynchronous tasks, supporting various scheduling options including
 * immediate, delayed, and periodic execution.
 *
 * @since 1.0.0
 */
public class LEDSuiteScheduler implements TaskScheduler {

    private static final Logger logger = Logger.autoConfigureLogger();

    /**
     * Counter for IDs. Order doesn't matter, only uniqueness.
     * @since 1.0.0
     */
    private final AtomicInteger ids = new AtomicInteger(1);
    private volatile LEDSuiteTask head = new LEDSuiteTask();
    /**
     * Tail of a linked-list. AtomicReference only matters when adding to queue.
     * @since 1.0.0
     */
    private final AtomicReference<LEDSuiteTask> tail = new AtomicReference<>(head);
    /**
     * Main thread logic only.
     * @since 1.0.0
     */
    private final PriorityQueue<LEDSuiteTask> pending = new PriorityQueue<>(10,
            (o1, o2) -> (int) (o1.getNextRun() - o2.getNextRun()));
    /**
     * Main thread logic only.
     * @since 1.0.0
     */
    private final List<LEDSuiteTask> temp = new ArrayList<>();
    /**
     * These are tasks that are currently active. It's provided for 'viewing' the current state.
     * @since 1.0.0
     */
    private final ConcurrentHashMap<Integer, LEDSuiteTask> runners = new ConcurrentHashMap<>();
    private volatile int currentTick = -1;
    private final Executor executor = Executors.newCachedThreadPool();
    private static final int RECENT_TICKS;

    static {
        RECENT_TICKS = 30;
    }

    /**
     * Schedules a task to run synchronously.
     *
     * @param runnable The task to be run.
     * @return A reference to the scheduled task.
     * @since 1.0.0
     */
    @Override
    public LEDSuiteTask runTask(Runnable runnable) {
        return runTaskLater(runnable, 0);
    }

    /**
     * Schedules a task to run asynchronously.
     *
     * @param runnable The task to be run.
     * @return A reference to the scheduled task.
     * @since 1.0.0
     */
    @Override
    public LEDSuiteTask runTaskAsynchronously(Runnable runnable) {
        return runTaskLaterAsynchronously(runnable, 0);
    }

    /**
     * Schedules a task to run after a specified delay.
     *
     * @param runnable The task to be run.
     * @param delay The delay in ticks before the task is run.
     * @return A reference to the scheduled task.
     * @since 1.0.0
     */
    @Override
    public LEDSuiteTask runTaskLater(Runnable runnable, long delay) {
        return runTaskTimer(runnable, delay, -1);
    }

    /**
     * Schedules a task to run asynchronously after a specified delay.
     *
     * @param runnable The task to be run.
     * @param delay The delay in ticks before the task is run.
     * @return A reference to the scheduled task.
     * @since 1.0.0
     */
    @Override
    public LEDSuiteTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
        return runTaskTimerAsynchronously(runnable, delay, -1);
    }

    /**
     * Schedules a task to run periodically after an initial delay.
     *
     * @param runnable The task to be run.
     * @param delay The delay in ticks before the first execution.
     * @param period The period in ticks between subsequent executions.
     * @return A reference to the scheduled task.
     * @since 1.0.0
     */
    @Override
    public LEDSuiteTask runTaskTimer(Runnable runnable, long delay, long period) {
        if (delay < 0) {
            delay = 0;
        }
        if (period == 0) {
            period = 1;
        } else if (period < -1) {
            period = -1;
        }
        return handle(new LEDSuiteTask(runnable, nextId(), period), delay);
    }

    /**
     * Schedules a task to run asynchronously and periodically after an initial delay.
     *
     * @param runnable The task to be run.
     * @param delay The delay in ticks before the first execution.
     * @param period The period in ticks between subsequent executions.
     * @return A reference to the scheduled task.
     * @since 1.0.0
     */
    @Override
    public LEDSuiteTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        if (delay < 0) {
            delay = 0;
        }
        if (period == 0) {
            period = 1;
        } else if (period < -1) {
            period = -1;
        }
        return handle(new LEDSuiteAsyncTask(runners, runnable, nextId(), period), delay);
    }

    /**
     * Cancels a task with the specified task ID.
     *
     * @param taskId The ID of the task to be cancelled.
     * @since 1.0.0
     */
    @Override
    public void cancelTask(final int taskId) {
        logger.verbose("Cancelling task with id '{}'", taskId);
        if (taskId <= 0) {
            return;
        }
        LEDSuiteTask task = runners.get(taskId);
        if (task != null) {
            task.cancel0();
        }
        task = new LEDSuiteTask(
                new Runnable() {
                    public void run() {
                        if (!check(LEDSuiteScheduler.this.temp)) {
                            check(LEDSuiteScheduler.this.pending);
                        }
                    }

                    private boolean check(final @NotNull Iterable<LEDSuiteTask> collection) {
                        final Iterator<LEDSuiteTask> tasks = collection.iterator();
                        while (tasks.hasNext()) {
                            final LEDSuiteTask task = tasks.next();
                            if (task.getTaskId() == taskId) {
                                task.cancel0();
                                tasks.remove();
                                if (task.isSync()) {
                                    runners.remove(taskId);
                                }
                                return true;
                            }
                        }
                        return false;
                    }
                });
        handle(task, 0);
        for (LEDSuiteTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                return;
            }
            if (taskPending.getTaskId() == taskId) {
                taskPending.cancel0();
            }
        }
    }

    /**
     * Cancels all currently scheduled tasks.
     *
     * @since 1.0.0
     */
    @Override
    public void cancelAllTasks() {
        logger.verbose("Cancelling all tasks");
        final LEDSuiteTask task = new LEDSuiteTask(
                () -> {
                    Iterator<LEDSuiteTask> it = LEDSuiteScheduler.this.runners.values().iterator();
                    while (it.hasNext()) {
                        LEDSuiteTask task1 = it.next();
                        task1.cancel0();
                        if (task1.isSync()) {
                            it.remove();
                        }
                    }
                    LEDSuiteScheduler.this.pending.clear();
                    LEDSuiteScheduler.this.temp.clear();
                });
        handle(task, 0);
        for (LEDSuiteTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                break;
            }
            taskPending.cancel0();
        }
        for (LEDSuiteTask runner : runners.values()) {
            runner.cancel0();
        }
    }

    /**
     * Checks if a task with the specified task ID is currently running.
     *
     * @param taskId The ID of the task.
     * @return True if the task is currently running, false otherwise.
     * @since 1.0.0
     */
    @Override
    public boolean isCurrentlyRunning(final int taskId) {
        final LEDSuiteTask task = runners.get(taskId);
        if (task == null || task.isSync()) {
            return false;
        }
        final LEDSuiteAsyncTask asyncTask = (LEDSuiteAsyncTask) task;
        synchronized (asyncTask.getWorkers()) {
            return asyncTask.getWorkers().isEmpty();
        }
    }

    /**
     * Checks if a task with the specified task ID is currently queued.
     *
     * @param taskId The ID of the task.
     * @return True if the task is queued, false otherwise.
     * @since 1.0.0
     */
    @Override
    public boolean isQueued(final int taskId) {
        if (taskId <= 0) {
            return false;
        }
        for (LEDSuiteTask task = head.getNext(); task != null; task = task.getNext()) {
            if (task.getTaskId() == taskId) {
                return task.getPeriod() >= -1; // The task will run
            }
        }
        LEDSuiteTask task = runners.get(taskId);
        return task != null && task.getPeriod() >= -1;
    }

    /**
     * Returns a list of currently active workers.
     *
     * @return A list of active workers.
     * @since 1.0.0
     */
    @Override
    public List<LEDSuiteWorker> getActiveWorkers() {
        final ArrayList<LEDSuiteWorker> workers = new ArrayList<>();
        for (final LEDSuiteTask taskObj : runners.values()) {
            // Iterator will be a best-effort (may fail to grab very new values) if called from an async thread
            if (taskObj.isSync()) {
                continue;
            }
            final LEDSuiteAsyncTask task = (LEDSuiteAsyncTask) taskObj;
            synchronized (task.getWorkers()) {
                // This will never have an issue with stale threads; it's state-safe
                workers.addAll(task.getWorkers());
            }
        }
        return workers;
    }

    /**
     * Returns a list of currently pending tasks.
     *
     * @return A list of pending tasks.
     * @since 1.0.0
     */
    @Override
    public List<LEDSuiteTask> getPendingTasks() {
        final ArrayList<LEDSuiteTask> truePending = new ArrayList<>();
        for (LEDSuiteTask task = head.getNext(); task != null; task = task.getNext()) {
            if (task.getTaskId() != -1) {
                // -1 is special code
                truePending.add(task);
            }
        }

        final ArrayList<LEDSuiteTask> pending = new ArrayList<>();
        for (LEDSuiteTask task : runners.values()) {
            if (task.getPeriod() >= -1) {
                pending.add(task);
            }
        }

        for (final LEDSuiteTask task : truePending) {
            if (task.getPeriod() >= -1 && !pending.contains(task)) {
                pending.add(task);
            }
        }
        return pending;
    }

    /**
     * This method is designed to never block or wait for locks; an immediate execution of all current tasks.
     *
     * @param currentTick The current tick count.
     * @since 1.0.0
     */
    public void mainThreadHeartbeat(final int currentTick) {
        this.currentTick = currentTick;
        final List<LEDSuiteTask> temp = this.temp;
        parsePending();
        while (isReady(currentTick)) {
            final LEDSuiteTask task = pending.remove();
            if (task.getPeriod() < -1) {
                if (task.isSync()) {
                    runners.remove(task.getTaskId(), task);
                }
                parsePending();
                continue;
            }
            if (task.isSync()) {
                try {
                    task.run();
                } catch (final Throwable throwable) {
                    logger.warn("Task #{} for {} generated an exception",
                            task.getTaskId(),
                            LEDSuiteApplication.class.getName()
                    );
                }
                parsePending();
            } else {
                executor.execute(task);
                // We don't need to parse pending
                // (async tasks must live with race-conditions if they attempt to cancel between these few lines of code)
            }
            final long period = task.getPeriod(); // State consistency
            if (period > 0) {
                task.setNextRun(currentTick + period);
                temp.add(task);
            } else if (task.isSync()) {
                runners.remove(task.getTaskId());
            }
        }
        pending.addAll(temp);
        temp.clear();
    }

    private void addTask(final @NotNull LEDSuiteTask task) {
        logger.verbose("Add task -> '{}'", task.getTaskId());
        final AtomicReference<LEDSuiteTask> tail = this.tail;
        LEDSuiteTask tailTask = tail.get();
        while (!tail.compareAndSet(tailTask, task)) {
            tailTask = tail.get();
        }
        tailTask.setNext(task);
    }

    @Contract("_, _ -> param1")
    private @NotNull LEDSuiteTask handle(final @NotNull LEDSuiteTask task, final long delay) {
        task.setNextRun(currentTick + delay);
        addTask(task);
        return task;
    }

    private int nextId() {
        return ids.incrementAndGet();
    }

    private void parsePending() {
        LEDSuiteTask head = this.head;
        LEDSuiteTask task = head.getNext();
        LEDSuiteTask lastTask = head;
        for (; task != null; task = (lastTask = task).getNext()) {
            if (task.getTaskId() == -1) {
                task.run();
            } else if (task.getPeriod() >= -1) {
                pending.add(task);
                runners.put(task.getTaskId(), task);
            }
        }
        // We split this because of the way things are ordered for all the async calls in LEDSuiteScheduler
        // (it prevents race-conditions)
        for (task = head; task != lastTask; task = head) {
            head = task.getNext();
            task.setNext(null);
        }
        this.head = lastTask;
    }

    private boolean isReady(final int currentTick) {
        return !pending.isEmpty() && pending.peek().getNextRun() <= currentTick;
    }

    /**
     * Returns a string representation of recent tasks.
     *
     * @return A string representation of recent tasks.
     * @since 1.0.0
     */
    @Override
    public String toString() {
        int debugTick = currentTick;
        return "Recent tasks from " + (debugTick - RECENT_TICKS) + '-' + debugTick + '{' + '}';
    }
}
