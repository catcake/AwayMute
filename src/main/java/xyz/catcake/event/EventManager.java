package xyz.catcake.event;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static xyz.catcake.awaymute.AwayMuteMod.LOG;

public final class EventManager {
	private static int busIdCounter;

	static {
		busIdCounter = 0;
	}

	private final Map<Class<?>,List<Subscriber>> subscribers;
	private final int busId;

	public EventManager(final Map<Class<?>,List<Subscriber>> subscribers) {
		this.subscribers = subscribers;
		busId = busIdCounter++;
	}

	/**
	 * Registers all subscribers found in an object.
	 * @param subscriberOwner An object with methods annotated with {@link xyz.catcake.event.EventSubscribe}
	 */
	public void subscribe(final Object subscriberOwner) {
		for (final var method : subscriberOwner.getClass().getDeclaredMethods()) {
			if (!method.isAnnotationPresent(EventSubscribe.class)) continue;
			if (method.getParameterTypes().length != 1) continue;
			if (!Object.class.isAssignableFrom(method.getParameterTypes()[0])) continue;
			LOG.info(String.format("found: %s#%s", subscriberOwner.getClass().getCanonicalName(), method.getName()));

			final var listenerList = subscribers.computeIfAbsent(
				method.getParameterTypes()[0],
				k -> new ArrayList<>());
			listenerList.add(new Subscriber(subscriberOwner, method));
		}
	}

	public void publish(final Object eventContext) {
		final var retrievedSubscribers = subscribers.get(eventContext.getClass());
		if (retrievedSubscribers == null) {
			LOG.info(String.format("%s has no subscribers on bus-%d", eventContext.getClass().getCanonicalName(), busId));
			return;
		}
		for (final var s : retrievedSubscribers) {
			try {
				s.subscriber().invoke(s.owner(), eventContext);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
}