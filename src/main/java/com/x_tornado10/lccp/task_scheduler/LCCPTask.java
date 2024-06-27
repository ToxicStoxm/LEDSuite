package com.x_tornado10.lccp.task_scheduler;

import com.x_tornado10.lccp.LCCP;
import com.x_tornado10.lccp.yaml_factory.YAMLMessage;

import java.io.InputStream;

public class LCCPTask implements Task, Runnable {

    private volatile LCCPTask next = null;
    /**
     * -1 means no repeating <br>
     * -2 means cancel <br>
     * -3 means processing for Future <br>
     * -4 means done for Future <br>
     * Never 0 <br>
     * >0 means number of ticks to wait between each execution
     */
    private volatile long period;
    private long nextRun;
    private final Runnable task;
    private YAMLMessage yaml;
    private final int id;

    LCCPTask() {
        this(null, -1, -1);
    }

    LCCPTask(final Runnable task) {
        this(task, -1, -1);
    }

    LCCPTask(final Runnable task, final int id, final long period) {
        this(task, id, period, null);
    }
    LCCPTask(final Runnable task, final int id, final long period, YAMLMessage yaml) {
        this.task = task;
        this.id = id;
        this.period = period;
        this.yaml = yaml;
    }

    public final int getTaskId() {
        return id;
    }

    public boolean isSync() {
        return true;
    }

    public void run() {
        task.run();
    }

    long getPeriod() {
        return period;
    }

    void setPeriod(long period) {
        this.period = period;
    }

    long getNextRun() {
        return nextRun;
    }

    void setNextRun(long nextRun) {
        this.nextRun = nextRun;
    }

    LCCPTask getNext() {
        return next;
    }

    void setNext(LCCPTask next) {
        this.next = next;
    }

    Class<? extends Runnable> getTaskClass() {
        return task.getClass();
    }

    public void cancel() {
        LCCP.getScheduler().cancelTask(id);
    }

    /**
     * This method properly sets the status to cancelled, synchronizing when required.
     *
     * @return false if it is a craft future task that has already begun execution, true otherwise
     */
    boolean cancel0() {
        setPeriod(-2L);
        return true;
    }
}