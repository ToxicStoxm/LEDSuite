package com.toxicstoxm.LEDSuite.time;

import com.toxicstoxm.LEDSuite.LEDSuite;

/**
 * The {@code TickingSystem} class manages the main ticking loop for scheduling and running tasks
 * within the LEDSuite system. It operates in its own thread, providing a regular tick interval
 * for managing timed operations.
 * <p>
 * The ticking system is initialized and started automatically, and it provides methods to control
 * its execution. The tick interval is defined in milliseconds, and tasks are executed based on
 * this interval.
 * </p>
 *
 * @since 1.0.0
 */
public class TickingSystem {

    // Conversion factor for milliseconds to ticks
    private static final int CONVERSION_NUMBER = 1000;

    // Number of ticks per second
    private static final int TICKS_PER_SECOND = 1000;

    // Delay between ticks in milliseconds
    private static final long TICK_DELAY_MS = CONVERSION_NUMBER / TICKS_PER_SECOND;

    // Indicates whether the ticking system is currently running
    private boolean running;

    // Thread that runs the tick loop
    private Thread tickThread;

    // The current tick count
    private int currentTick = 0;

    /**
     * Constructs a new {@code TickingSystem} and starts the ticking thread.
     * The system will begin ticking immediately upon construction.
     */
    public TickingSystem() {
        running = false;
        this.start();
    }

    /**
     * Converts a value from milliseconds to ticks.
     *
     * @param val The value in milliseconds to convert.
     * @return The equivalent value in ticks.
     * @since 1.0.0
     */
    public static long translate(long val) {
        return val / TICK_DELAY_MS;
    }

    /**
     * Starts the ticking system if it is not already running. This method initializes
     * and starts a new thread that continuously runs the ticking loop.
     *
     * @since 1.0.0
     */
    public void start() {
        if (!running) {
            running = true;
            tickThread = new Thread(this::tickLoop);
            tickThread.start();
        }
    }

    /**
     * Stops the ticking system and waits for the ticking thread to finish. This method
     * ensures that the tick loop is properly terminated.
     *
     * @since 1.0.0
     */
    public void stop() {
        running = false;
        try {
            tickThread.join();
        } catch (InterruptedException e) {
            // Handle interruption if necessary
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    /**
     * The main loop that runs continuously while the system is active. It handles
     * the ticking operations and maintains the interval between ticks.
     */
    private void tickLoop() {
        while (running) {
            long startTime = System.currentTimeMillis();

            tick();

            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime < TICK_DELAY_MS) {
                try {
                    Thread.sleep(TICK_DELAY_MS - elapsedTime);
                } catch (InterruptedException e) {
                    LEDSuite.logger.fatal("Ticking System was interrupted! " + LEDSuite.logger.getErrorMessage(e));
                    LEDSuite.getInstance().exit(4);
                }
            }
        }
    }

    /**
     * Performs tick operations. This method increments the tick count and informs
     * the LEDSuite scheduler about the current tick.
     * <p>
     * Subclasses or additional methods can override or extend this method to perform
     * custom operations during each tick.
     * </p>
     */
    private void tick() {
        currentTick++;
        LEDSuite.getScheduler().mainThreadHeartbeat(currentTick);
    }
}
