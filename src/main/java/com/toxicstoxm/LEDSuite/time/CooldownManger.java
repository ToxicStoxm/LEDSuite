package com.toxicstoxm.LEDSuite.time;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CooldownManger {

    private record CooldownAction(Action action, long cooldown, long lastCall) {}

    private static final HashMap<String, CooldownAction> actions = new HashMap<>();

    public static void addAction(@NotNull String name, @NotNull Action action, long cooldown) {
        addAction(name, action, cooldown, false);
    }
    public static void addAction(@NotNull String name, @NotNull Action action, long cooldown, boolean force) {
        CooldownAction cooldownAction = new CooldownAction(action, cooldown, System.currentTimeMillis());
        if (!force) actions.putIfAbsent(name, cooldownAction);
        else actions.put(name, cooldownAction);
    }

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

    public static boolean remove(String name) {
        return actions.remove(name) != null;
    }

}
