package com.x_tornado10.lccp.task_scheduler;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.yaml_factory.YAMLMessage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LCCPScheduler implements TaskScheduler {

    /**
     * Counter for IDs. Order doesn't matter, only uniqueness.
     */
    private final AtomicInteger ids = new AtomicInteger(1);
    private volatile LCCPTask head = new LCCPTask();
    /**
     * Tail of a linked-list. AtomicReference only matters when adding to queue
     */
    private final AtomicReference<LCCPTask> tail = new AtomicReference<>(head);
    /**
     * Main thread logic only
     */
    private final PriorityQueue<LCCPTask> pending = new PriorityQueue<>(10,
            (o1, o2) -> (int) (o1.getNextRun() - o2.getNextRun()));
    /**
     * Main thread logic only
     */
    private final List<LCCPTask> temp = new ArrayList<>();
    /**
     * These are tasks that are currently active. It's provided for 'viewing' the current state.
     */
    private final ConcurrentHashMap<Integer, LCCPTask> runners = new ConcurrentHashMap<>();
    private volatile int currentTick = -1;
    private final Executor executor = Executors.newCachedThreadPool();
    //private LCCPAsyncDebugger debugHead = new LCCPAsyncDebugger(-1, null) {@Override StringBuilder debugTo(StringBuilder string) {return string;}};
    //private LCCPAsyncDebugger debugTail = debugHead;
    private static final int RECENT_TICKS;

    static {
        RECENT_TICKS = 30;
    }

    @Override
    public LCCPTask runTask(Runnable runnable) {
        return runTaskLater(runnable, 0);
    }

    @Override
    public LCCPTask runTaskAsynchronously(Runnable runnable) {
        return runTaskLaterAsynchronously(runnable, 0);
    }

    @Override
    public LCCPTask runTaskAsynchronously(Runnable runnable, YAMLMessage yaml) {
        return runTaskLaterAsynchronously(runnable, 0,  yaml);
    }


    @Override
    public LCCPTask runTaskLater(Runnable runnable, long delay) {
        return runTaskTimer(runnable, delay, -1);
    }

    @Override
    public LCCPTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
        return runTaskTimerAsynchronously(runnable, delay, -1);
    }

    @Override
    public LCCPTask runTaskLaterAsynchronously(Runnable runnable, long delay, YAMLMessage yaml) {
        return runTaskTimerAsynchronously(runnable, delay, -1, yaml);
    }

    @Override
    public LCCPTask runTaskTimer(Runnable runnable, long delay, long period) {
        if (delay < 0) {
            delay = 0;
        }
        if (period == 0) {
            period = 1;
        } else if (period < -1) {
            period = -1;
        }
        return handle(new LCCPTask(runnable, nextId(), period), delay);
    }

    @Override
    public LCCPTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        if (delay < 0) {
            delay = 0;
        }
        if (period == 0) {
            period = 1;
        } else if (period < -1) {
            period = -1;
        }
        return handle(new LCCPAsyncTask(runners, runnable, nextId(), period), delay);
    }

    @Override
    public LCCPTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period, YAMLMessage yaml) {
        if (delay < 0) {
            delay = 0;
        }
        if (period == 0) {
            period = 1;
        } else if (period < -1) {
            period = -1;
        }
        return handle(new LCCPAsyncTask(runners, runnable, nextId(), period, yaml), delay);
    }


    @Override
    public void cancelTask(final int taskId) {
        if (taskId <= 0) {
            return;
        }
        LCCPTask task = runners.get(taskId);
        if (task != null) {
            task.cancel0();
        }
        task = new LCCPTask(
                new Runnable() {
                    public void run() {
                        if (!check(LCCPScheduler.this.temp)) {
                            check(LCCPScheduler.this.pending);
                        }
                    }
                    private boolean check(final Iterable<LCCPTask> collection) {
                        final Iterator<LCCPTask> tasks = collection.iterator();
                        while (tasks.hasNext()) {
                            final LCCPTask task = tasks.next();
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
        for (LCCPTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
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
        final LCCPTask task = new LCCPTask(
                new Runnable() {
                    public void run() {
                        Iterator<LCCPTask> it = LCCPScheduler.this.runners.values().iterator();
                        while (it.hasNext()) {
                            LCCPTask task = it.next();
                            task.cancel0();
                            if (task.isSync()) {
                                it.remove();
                            }
                        }
                        LCCPScheduler.this.pending.clear();
                        LCCPScheduler.this.temp.clear();
                    }
                });
        handle(task, 0);
        for (LCCPTask taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) {
                break;
            }
            taskPending.cancel0();
        }
        for (LCCPTask runner : runners.values()) {
            runner.cancel0();
        }
    }

    @Override
    public boolean isCurrentlyRunning(final int taskId) {
        final LCCPTask task = runners.get(taskId);
        if (task == null || task.isSync()) {
            return false;
        }
        final LCCPAsyncTask asyncTask = (LCCPAsyncTask) task;
        synchronized (asyncTask.getWorkers()) {
            return asyncTask.getWorkers().isEmpty();
        }
    }

    @Override
    public boolean isQueued(final int taskId) {
        if (taskId <= 0) {
            return false;
        }
        for (LCCPTask task = head.getNext(); task != null; task = task.getNext()) {
            if (task.getTaskId() == taskId) {
                return task.getPeriod() >= -1; // The task will run
            }
        }
        LCCPTask task = runners.get(taskId);
        return task != null && task.getPeriod() >= -1;
    }

    @Override
    public List<LCCPWorker> getActiveWorkers() {
        final ArrayList<LCCPWorker> workers = new ArrayList<LCCPWorker>();
        for (final LCCPTask taskObj : runners.values()) {
            // Iterator will be a best-effort (may fail to grab very new values) if called from an async thread
            if (taskObj.isSync()) {
                continue;
            }
            final LCCPAsyncTask task = (LCCPAsyncTask) taskObj;
            synchronized (task.getWorkers()) {
                // This will never have an issue with stale threads; it's state-safe
                workers.addAll(task.getWorkers());
            }
        }
        return workers;
    }

    @Override
    public List<LCCPTask> getPendingTasks() {
        final ArrayList<LCCPTask> truePending = new ArrayList<>();
        for (LCCPTask task = head.getNext(); task != null; task = task.getNext()) {
            if (task.getTaskId() != -1) {
                // -1 is special code
                truePending.add(task);
            }
        }

        final ArrayList<LCCPTask> pending = new ArrayList<>();
        for (LCCPTask task : runners.values()) {
            if (task.getPeriod() >= -1) {
                pending.add(task);
            }
        }

        for (final LCCPTask task : truePending) {
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
        final List<LCCPTask> temp = this.temp;
        parsePending();
        while (isReady(currentTick)) {
            final LCCPTask task = pending.remove();
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
                    LCCP.logger.warn(String.format(
                                    "Task #%s for %s generated an exception",
                                    task.getTaskId(),
                                    LCCP.class.getName()));

                }
                parsePending();
            } else {
                //debugTail = debugTail.setNext(new LCCPAsyncDebugger(currentTick + RECENT_TICKS, task.getTaskClass()));
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
        //debugHead = debugHead.getNextHead(currentTick);
    }
    
    private void addTask(final LCCPTask task) {
        final AtomicReference<LCCPTask> tail = this.tail;
        LCCPTask tailTask = tail.get();
        while (!tail.compareAndSet(tailTask, task)) {
            tailTask = tail.get();
        }
        tailTask.setNext(task);
    }
    
    private LCCPTask handle(final LCCPTask task, final long delay) {
        task.setNextRun(currentTick + delay);
        addTask(task);
        return task;
    }

    private int nextId() {
        return ids.incrementAndGet();
    }

    private void parsePending() {
        LCCPTask head = this.head;
        LCCPTask task = head.getNext();
        LCCPTask lastTask = head;
        for (; task != null; task = (lastTask = task).getNext()) {
            if (task.getTaskId() == -1) {
                task.run();
            } else if (task.getPeriod() >= -1) {
                pending.add(task);
                runners.put(task.getTaskId(), task);
            }
        }
        // We split this because of the way things are ordered for all of the async calls in LCCPScheduler
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
        //debugHead.debugTo(string);
        return string.append('}').toString();
    }
}