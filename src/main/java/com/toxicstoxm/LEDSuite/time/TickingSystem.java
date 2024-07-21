package com.toxicstoxm.LEDSuite.time;

import com.toxicstoxm.LEDSuite.LEDSuite;

public class TickingSystem {
    private static final int conversionNumber = 1000;

    private static final int TICKS_PER_SECOND = 1000;
    private static final long TICK_DELAY_MS = conversionNumber / TICKS_PER_SECOND;

    private boolean running;
    private Thread tickThread;
    private int currentTick = 0;

    public TickingSystem() {
        running = false;
        this.start();
    }

    public static long translate(long val) {
        return val / TICK_DELAY_MS;
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
                    LEDSuite.logger.error("Ticking System was interrupted!");
                    LEDSuite.logger.warn("Stopping program to prevent further errors!");
                    LEDSuite.exit(1);
                }
            }
        }
    }

    // Example method for performing tick operations
    private void tick() {
        // Perform tick operations here
        currentTick ++;
        LEDSuite.getScheduler().mainThreadHeartbeat(currentTick);
    }
}
