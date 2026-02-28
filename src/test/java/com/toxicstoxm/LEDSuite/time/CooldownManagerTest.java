package com.toxicstoxm.LEDSuite.time;

import com.toxicstoxm.YAJL.core.LoggerManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CooldownManagerTest {

    private static final long SHORT_COOLDOWN = 100; // 100 ms cooldown for testing

    @BeforeAll
    static void beforeAll() {
        LoggerManager.configure()
                .muteLogger(true)
                .done();
    }

    @BeforeEach
    void reset() {
        // Reset static state by removing any actions or groups that might interfere
        // No direct clear method in CooldownManager, so we try removing test keys
        CooldownManager.remove("testAction");
        CooldownManager.remove("groupedAction");
        CooldownManager.remove("forceAction");
        CooldownManager.remove("nonExistent");
    }

    @Test
    void testAddAndCallActionExecutes() {
        AtomicBoolean called = new AtomicBoolean(false);
        CooldownManager.addAction("testAction", () -> called.set(true), SHORT_COOLDOWN);
        CooldownManager.clearCooldown("testAction");

        assertTrue(CooldownManager.call("testAction"));
        assertTrue(called.get(), "Action should have been called");
    }

    @Test
    void testCallActionRespectsCooldown() throws InterruptedException {
        AtomicBoolean called = new AtomicBoolean(false);
        CooldownManager.addAction("testAction", () -> called.set(true), SHORT_COOLDOWN);
        CooldownManager.clearCooldown("testAction");

        assertTrue(CooldownManager.call("testAction"));
        called.set(false);

        // Immediately calling again should not call action
        assertFalse(CooldownManager.call("testAction"));
        assertFalse(called.get());

        // Wait past cooldown and call again
        Thread.sleep(SHORT_COOLDOWN + 10);
        assertTrue(CooldownManager.call("testAction"));
        assertTrue(called.get());
    }

    @Test
    void testClearCooldownAllowsImmediateCall() throws InterruptedException {
        AtomicBoolean called = new AtomicBoolean(false);
        CooldownManager.addAction("testAction", () -> called.set(true), 1000);
        CooldownManager.clearCooldown("testAction");

        assertTrue(CooldownManager.call("testAction"));
        called.set(false);

        // Immediately calling again returns false (cooldown)
        assertFalse(CooldownManager.call("testAction"));

        // Clear cooldown forcibly
        assertTrue(CooldownManager.clearCooldown("testAction"));

        // Now call should succeed immediately
        assertTrue(CooldownManager.call("testAction"));
        assertTrue(called.get());
    }

    @Test
    void testRemoveAction() {
        AtomicBoolean called = new AtomicBoolean(false);
        CooldownManager.addAction("testAction", () -> called.set(true), SHORT_COOLDOWN);

        assertTrue(CooldownManager.remove("testAction"));
        assertFalse(CooldownManager.call("testAction"));
        assertFalse(called.get());

        // Removing again returns false
        assertFalse(CooldownManager.remove("testAction"));
    }

    @Test
    void testAddActionWithForceOverrides() {
        AtomicBoolean calledFirst = new AtomicBoolean(false);
        AtomicBoolean calledSecond = new AtomicBoolean(false);

        CooldownManager.addAction("forceAction", () -> calledFirst.set(true), SHORT_COOLDOWN);
        CooldownManager.addAction("forceAction", () -> calledSecond.set(true), SHORT_COOLDOWN, true);
        CooldownManager.clearCooldown("forceAction");

        // Calling should trigger second action, not first
        assertTrue(CooldownManager.call("forceAction"));
        assertFalse(calledFirst.get());
        assertTrue(calledSecond.get());
    }

    @Test
    void testActionGroupCooldown() throws InterruptedException {
        AtomicBoolean called = new AtomicBoolean(false);
        String groupName = "group1";

        CooldownManager.createActionGroup(groupName, SHORT_COOLDOWN);
        CooldownManager.addAction("groupedAction", () -> called.set(true), SHORT_COOLDOWN, groupName);
        CooldownManager.clearCooldown("groupedAction");

        // First call executes
        assertTrue(CooldownManager.call("groupedAction"));
        assertTrue(called.get());
        called.set(false);

        // Immediately calling again should not execute
        assertFalse(CooldownManager.call("groupedAction"));
        assertFalse(called.get());

        // Wait past cooldown, should execute again
        Thread.sleep(SHORT_COOLDOWN + 10);
        assertTrue(CooldownManager.call("groupedAction"));
        assertTrue(called.get());
    }

    @Test
    void testCallNonExistentActionReturnsFalse() {
        assertFalse(CooldownManager.call("nonExistent"));
    }

    @Test
    void testClearCooldownNonExistentReturnsFalse() {
        assertFalse(CooldownManager.clearCooldown("nonExistent"));
    }

    @Test
    void testRemoveNonExistentReturnsFalse() {
        assertFalse(CooldownManager.remove("nonExistent"));
    }
}
