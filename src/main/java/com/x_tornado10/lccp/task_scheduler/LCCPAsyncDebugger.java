package com.x_tornado10.lccp.task_scheduler;

import com.x_tornado10.lccp.LCCP;

// unused for now
public class LCCPAsyncDebugger {
    private LCCPAsyncDebugger next = null;
    private final int expiry;
    private final Class<? extends Runnable> clazz;

    LCCPAsyncDebugger(final int expiry, final Class<? extends Runnable> clazz) {
        this.expiry = expiry;
        this.clazz = clazz;

    }

    final LCCPAsyncDebugger getNextHead(final int time) {
        LCCPAsyncDebugger next, current = this;
        while (time > current.expiry && (next = current.next) != null) {
            current = next;
        }
        return current;
    }

    final LCCPAsyncDebugger setNext(final LCCPAsyncDebugger next) {
        return this.next = next;
    }

    StringBuilder debugTo(final StringBuilder string) {
        for (LCCPAsyncDebugger next = this; next != null; next = next.next) {
            string.append(LCCP.class.getName()).append(':').append(next.clazz.getName()).append('@').append(next.expiry).append(',');
        }
        return string;
    }
}