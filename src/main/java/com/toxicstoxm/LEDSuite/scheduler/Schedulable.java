package com.toxicstoxm.LEDSuite.scheduler;

import java.time.Duration;

public interface Schedulable {
    void runTask();
    void runTaskLater(Duration delay);
    void runTaskTimer(Duration interval);
    void runTaskTimerLater(Duration delay, Duration interval);

    void runTaskAsync();
    void runTaskLaterAsync(Duration delay);
    void runTaskTimerAsync(Duration interval);
    void runTaskTimerLaterAsync(Duration delay, Duration interval);
}
