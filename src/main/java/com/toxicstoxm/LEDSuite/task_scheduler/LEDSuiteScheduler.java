package com.toxicstoxm.LEDSuite.task_scheduler;

import com.toxicstoxm.LEDSuite.LEDSuite;
import com.toxicstoxm.LEDSuite.yaml_factory.YAMLMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LEDSuiteScheduler implements TaskScheduler {

    /**
     * Counter for IDs. Order doesn't matter, only uniqueness.
     */
    private final AtomicInteger ids = new AtomicInteger(1);
    private volatile LEDSuiteTask head = new LEDSuiteTask();
    /**
     * Tail of a linked-list. AtomicReference only matters when adding to queue
     */
    private final AtomicReference<LEDSuiteTask> tail = new AtomicReference<>(head);
    /**
     * Main thread logic only
     */
    private final PriorityQueue<LEDSuiteTask> pending = new PriorityQueue<>(10,
            (o1, o2) -> (int) (o1.getNextRun() - o2.getNextRun()));
    /**
     * Main thread logic only
     */
    private final List<LEDSuiteTask> temp = new ArrayList<>();
    /**
     * These are tasks that are currently active. It's provided for 'viewing' the current state.
     */
    private final ConcurrentHashMap<Integer, LEDSuiteTask> runners = new ConcurrentHashMap<>();
    private volatile int currentTick = -1;
    private final Executor executor = Executors.newCachedThreadPool();
    private static final int RECENT_TICKS;

    static {
        RECENT_TICKS = 30;
    }

    @Override
    public LEDSuiteTask runTask(Runnable runnable) {
        return runTaskLater(runnable, 0);
    }

    @Override
    public LEDSuiteTask runTaskAsynchronously(Runnable runnable) {
        return runTaskLaterAsynchronously(runnable, 0);
    }

    @Override
    public LEDSuiteTask runTaskAsynchronously(Runnable runnable, YAMLMessage yaml) {
        return runTaskLaterAsynchronously(runnable, 0,  yaml);
    }


    @Override
    public LEDSuiteTask runTaskLater(Runnable runnable, long delay) {
        return runTaskTimer(runnable, delay, -1);
    }

    @Override
    public LEDSuiteTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
        return runTaskTimerAsynchronously(runnable, delay, -1);
    }

    @Override
    public LEDSuiteTask runTaskLaterAsynchronously(Runnable runnable, long delay, YAMLMessage yaml) {
        return runTaskTimerAsynchronously(runnable, delay, -1, yaml);
    }

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

    @Override
    public LEDSuiteTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period, YAMLMessage yaml) {
        if (delay < 0) {
            delay = 0;
        }
        if (period == 0) {
            period = 1;
        } else if (period < -1) {
            period = -1;
        }
        return handle(new LEDSuiteAsyncTask(runners, runnable, nextId(), period, yaml), delay);
    }


    @Override
    public void cancelTask(final int taskId) {
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
                    private boolean check(final Iterable<LEDSuiteTask> collection) {
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
                    }});
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

    @Override
    public void cancelAllTasks() {
        final LEDSuiteTask task = new LEDSuiteTask(
                new Runnable() {
                    public void run() {
                        Iterator<LEDSuiteTask> it = LEDSuiteScheduler.this.runners.values().iterator();
                        while (it.hasNext()) {
                            LEDSuiteTask task = it.next();
                            task.cancel0();
                            if (task.isSync()) {
                                it.remove();
                            }
                        }
                        LEDSuiteScheduler.this.pending.clear();
                        LEDSuiteScheduler.this.temp.clear();
                    }
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

    @Override
    public List<LEDSuiteWorker> getActiveWorkers() {
        final ArrayList<LEDSuiteWorker> workers = new ArrayList<LEDSuiteWorker>();
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
                    LEDSuite.logger.warn(String.format(
                            "Task #%s for %s generated an exception",
                            task.getTaskId(),
                            LEDSuite.class.getName()));
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

    private void addTask(final LEDSuiteTask task) {
        final AtomicReference<LEDSuiteTask> tail = this.tail;
        LEDSuiteTask tailTask = tail.get();
        while (!tail.compareAndSet(tailTask, task)) {
            tailTask = tail.get();
        }
        tailTask.setNext(task);
    }

    private LEDSuiteTask handle(final LEDSuiteTask task, final long delay) {
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
        // We split this because of the way things are ordered for all of the async calls in LEDSuiteScheduler
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

    @Override
    public String toString() {
        int debugTick = currentTick;
        StringBuilder string = new StringBuilder("Recent tasks from ").append(debugTick - RECENT_TICKS).append('-').append(debugTick).append('{');
        return string.append('}').toString();
    }
}
