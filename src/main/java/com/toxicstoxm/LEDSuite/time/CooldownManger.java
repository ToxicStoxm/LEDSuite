package com.toxicstoxm.LEDSuite.time;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Simple wrapper class for adding a cooldown to actions.<br>
 * New action can be registered with {@link #addAction(String, Action, long)}.
 * Registered actions can be called with {@link #call(String)} and the specified action name.
 * The action will only be executed if the action is not on cooldown.
 * @since 1.0
 */
public class CooldownManger {

    private record CooldownAction(Action action, long cooldown, long lastCall) {}

    private static final HashMap<String, CooldownAction> actions = new HashMap<>();

    /**
     * Adds a new action. The action can now be called using {@link #call(String)} and the specified action name.
     * @param name the action name
     * @param action the action to execute
     * @param cooldown the min amount of time (in millis) that needs to pass between calls
     * @see #remove(String)
     */
    public static void addAction(@NotNull String name, @NotNull Action action, long cooldown) {
        addAction(name, action, cooldown, false);
    }
    /**
     * Adds a new action. The action can now be called using {@link #call(String)} and the specified action name.
     * @param name the action name
     * @param action the action to execute
     * @param cooldown the min amount of time (in millis) that needs to pass between calls
     * @param force overwrite any existing action with the same name
     * @see #remove(String)
     */
    public static void addAction(@NotNull String name, @NotNull Action action, long cooldown, boolean force) {
        CooldownAction cooldownAction = new CooldownAction(action, cooldown, System.currentTimeMillis());
        if (!force) actions.putIfAbsent(name, cooldownAction);
        else actions.put(name, cooldownAction);
    }

    /**
     * Tries to call the action associated with the specified action name. The action will only be executed if it is not on cooldown.
     * @param name the action name
     * @return {@code false} if the action is on cooldown<br>
     *         {@code true} if the action was successfully executed
     */
    public static boolean call(String name) {
        if (name == null || !actions.containsKey(name)) return false;
        CooldownAction cooldownAction = actions.remove(name);

        long timeElapsed = System.currentTimeMillis() - cooldownAction.lastCall;
        if (timeElapsed >= cooldownAction.cooldown) {
            cooldownAction.action.run();
            actions.put(name, new CooldownAction(cooldownAction.action, cooldownAction.cooldown, System.currentTimeMillis()));
            return true;
        } else {
            actions.put(name, cooldownAction);
            return false;
        }
    }

    /**
     * De-registers the action associated with the specified action name.
     * @param name the action name
     * @return {@code true} if the action was successfully deregistered, else {@code false}
     */
    public static boolean remove(String name) {
        return actions.remove(name) != null;
    }

}
