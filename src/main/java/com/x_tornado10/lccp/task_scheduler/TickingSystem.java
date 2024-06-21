package com.x_tornado10.lccp.task_scheduler;

import com.x_tornado10.lccp.LCCP;

public class TickingSystem {

    private static final int TICKS_PER_SECOND = 100;
    private static final long TICK_DELAY_MS = 1000 / TICKS_PER_SECOND;

    private boolean running;
    private Thread tickThread;
    private int currentTick = 0;

    public TickingSystem() {
        running = false;
        this.start();
    }

    public void start() {
        if (!running) {
            running = true;
            tickThread = new Thread(this::tickLoop);
            tickThread.start();
        }
    }

    public void stop() {
        running = false;
        try {
            tickThread.join();
        } catch (InterruptedException e) {
            // Handle interruption if necessary
        }
    }

    private void tickLoop() {
        while (running) {
            long startTime = System.currentTimeMillis();

            tick();

            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime < TICK_DELAY_MS) {
                try {
                    Thread.sleep(TICK_DELAY_MS - elapsedTime);
                } catch (InterruptedException e) {
                    LCCP.logger.error("Ticking System was interrupted!");
                    LCCP.logger.warn("Stopping program to prevent further errors!");
                    LCCP.exit(1);
                }
            }
        }
    }

    // Example method for performing tick operations
    private void tick() {
        // Perform tick operations here
        currentTick ++;
        LCCP.getScheduler().mainThreadHeartbeat(currentTick);
    }
}
