package com.toxicstoxm.LEDSuite.event_handling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method is an event handler in the event handling system.
 *
 * <p>Methods annotated with `@EventHandler` are registered as listeners for specific events
 * and will be called when those events occur.
 * The method must have a single parameter
 * which is the event to listen for.
 *
 * <p>Event handler methods should be placed in classes that are registered with the
 * event handling system.
 * These classes are often referred to as "listener classes."
 *
 * <p>Usage example:
 * <pre>{@code
 * public class MyEventListener implements EventListener {
 *
 *     @EventHandler
 *     public void onEvent(MyEvent event) {
 *         // handle the event
 *     }
 * }
 * }</pre>
 *
 * <p>To register a class as a listener, it typically needs to be registered with
 * the event handling system, such as:
 * <pre>{@code
 * eventSystem.registerListener(new MyEventListener());
 * }</pre>
 *
 * <p>This annotation is retained at runtime, allowing the event handling system to reflectively
 * access and register the annotated methods.
 *
 * @implNote Inspired by the <a href="https://github.com/Bukkit/Bukkit/blob/master/src/main/java/org/bukkit/event/EventHandler.java">Bukkit event handling system</a>
 *
 * @see com.toxicstoxm.LEDSuite.event_handling.listener.EventListener
 * @see Events
 * @see EventManager
 *
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    // Annotation for marking event listener methods
}


