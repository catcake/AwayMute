package xyz.catcake.event;

/**
 * Guarantees events will be routed properly, as opposed to accidentally
 * subscribing to an event because the method signature was identical.
 */
public abstract class EventContext {}