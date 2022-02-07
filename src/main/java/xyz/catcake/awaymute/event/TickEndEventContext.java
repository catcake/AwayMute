package xyz.catcake.awaymute.event;

import xyz.catcake.event.IEventContext;

/** Subscribers to this event will be called each game tick. */
public record TickEndEventContext() implements IEventContext {}