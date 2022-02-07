package xyz.catcake.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import xyz.catcake.awaymute.AwayMuteMod;
import xyz.catcake.log.PrefixedMessageFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO: Unsubscribe.
public final class EventBus {

	private static final Logger log;
	private static       int    busIdIncrementor;

	static {
		log              = LogManager.getLogger(new PrefixedMessageFactory(AwayMuteMod.LOG_PREFIX));
		busIdIncrementor = 0;
	}

	/** A unique identifier given to each bus. */
	public final  int                                                                                  busId;
	private final Map<Class<? extends IEventContext>,List<Subscriber>> mappedSubscribers;

	public EventBus() { this(""); }

	/**
	 * @param name A name for the bus, used in log messages. Buses will be assigned a unique {@link #busId} regardless
	 *             of the provided {@code name}.
	 */
	public EventBus(@NotNull final String name) {
		Objects.requireNonNull(name, "name must not be null");
		mappedSubscribers = new HashMap<>();
		busId             = busIdIncrementor++;
	}

	/**
	 * Registers all methods annotated with {@link EventSubscriber} found in an object.
	 *
	 * @param owner An object containing methods annotated with {@link EventSubscriber}.
	 * @throws IllegalArgumentException If an {@link EventSubscriber} annotated method in {@code owner} is invalid.
	 * @throws NullPointerException   If {@code owner} is null.
	 */
	public void subscribe(@NotNull final Object owner) {
		Objects.requireNonNull(owner, "owner must not be null");
		final int preSubscriberCount = mappedSubscribers.size();
		trySubscribeMethods(owner);
		if (preSubscriberCount >= mappedSubscribers.size()) log.warn(String.format(
				"#subscribe was called on %s, but no subscribers were found",
				owner.getClass().getCanonicalName()));
	}

	/** @throws IllegalArgumentException If an {@link EventSubscriber} annotated method in {@code owner} is invalid. */
	private void trySubscribeMethods(final Object owner)
		{ for (final Method method : owner.getClass().getDeclaredMethods()) subscribeValidSubscribers(owner, method); }

	/** @throws IllegalArgumentException If an {@link EventSubscriber} annotated method in {@code owner} is invalid. */
	private void subscribeValidSubscribers(final Object owner, final Method method) {
		if (!method.isAnnotationPresent(EventSubscriber.class)) return;
		if (!validSubscriberParameters(method.getParameterTypes())) throw new IllegalArgumentException(
				"methods with @EventSubscribe must have 1 parameter, deriving IEventContext");
		log.info(String.format("found subscriber: %s#%s",
		                       method.getDeclaringClass().getCanonicalName(),
		                       method.getName()));
		subscribeMethod(owner, method);
	}

	private boolean validSubscriberParameters(final Class<?>[] parameterTypes)
	{ return parameterTypes.length == 1 && IEventContext.isEventContext(parameterTypes[0]); }

	@SuppressWarnings("unchecked")
	private void subscribeMethod(final Object owner, final Method subscriberMethod) {
		final List<Subscriber> listenerList = mappedSubscribers.computeIfAbsent(
				(Class<? extends IEventContext>) subscriberMethod.getParameterTypes()[0],
				k -> new ArrayList<>());
		try {
			final Subscriber subscriber = new Subscriber(owner, subscriberMethod);
			listenerList.add(subscriber);
		} catch (final Throwable e) {
			log.error("error creating subscriber for: {} in {}",
			         subscriberMethod, owner.getClass().getCanonicalName(), e);
		}
	}

	/**
	 * Publishes the {@link IEventContext} to its subscribers.
	 *
	 * @param eventContext The {@link IEventContext} for the event's subscribers.
	 * @throws NullPointerException If {@code eventContext} is null.
	 */
	public void publish(@NotNull final IEventContext eventContext) {
		Objects.requireNonNull(eventContext, "eventContext must not be null");
		final List<Subscriber> subscribers = mappedSubscribers.get(eventContext.getClass());
		if (Objects.isNull(subscribers)) {
			log.warn(String.format("%s has no subscribers", eventContext.getClass().getCanonicalName()));
			return;
		}
		for (final Subscriber subscriber : subscribers) subscriber.publish(eventContext);
	}
}