package xyz.catcake.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import xyz.catcake.awaymute.AwayMuteMod;
import xyz.catcake.log.PrefixedMessageFactory;

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Creates a "map" (programmatically created lambda) function to the subscriber method, which can be called much more
 * quickly than {@link Method#invoke}.
 */
// TODO: support static & private subscriber methods?
final class Subscriber {

	private static final Logger log;

	static { log = LogManager.getLogger(new PrefixedMessageFactory(AwayMuteMod.LOG_PREFIX)); }

	private final BiConsumer<Object,IEventContext> callMap;
	private final Class<? extends IEventContext>   eventType;
	private final Object                           owner;
	private final Method                           subscriberMethod;

	/**
	 * @param owner            An instance of the owner of subscribingMethod.
	 * @param subscriberMethod The method subscribing to an {@link IEventContext}.
	 * @throws Throwable From {@link #setupCallMapping} when a lambda map to subscriberMethod cannot be created...
	 *                   Sorry about this one but {@link MethodHandle#invokeExact} literally throws {@link Throwable}.
	 */
	@SuppressWarnings("unchecked")
	public Subscriber(@NotNull final Object owner, @NotNull final Method subscriberMethod) throws Throwable {
		Objects.requireNonNull(subscriberMethod, "subscriber must not be null");
		final Class<?> eventType = subscriberMethod.getParameterTypes()[0];
		if (!IEventContext.isEventContext(eventType)) throw new IllegalArgumentException(
				"methods with @EventSubscribe must have 1 parameter, deriving IEventContext");
		this.eventType        = (Class<? extends IEventContext>) eventType;
		this.owner            = owner;
		this.subscriberMethod = subscriberMethod;
		callMap               = setupCallMapping(subscriberMethod);
	}

	/**
	 * @param subscriberMethod The method subscribing to an {@link IEventContext}.
	 * @return A {@link BiConsumer} where T is the owner of the subscriber and U is the {@link IEventContext} being
	 *         subscribed to.
	 *
	 * @throws IllegalAccessException    From {@link Lookup#unreflect}.
	 * @throws LambdaConversionException From {@link LambdaMetafactory#metafactory}.
	 * @throws Throwable                 From {@link MethodHandle#invokeExact}. Sorry about this one but
	 *                                   {@link MethodHandle#invokeExact} literally throws {@link Throwable}.
	 */
	@SuppressWarnings("unchecked")
	private BiConsumer<Object,IEventContext> setupCallMapping(final Method subscriberMethod)
			throws IllegalAccessException, LambdaConversionException, Throwable {
		final String       mappingName = "accept";
		final Lookup       caller      = MethodHandles.lookup();
		final MethodHandle subscriberHandle;

		subscriberMethod.setAccessible(true);
		subscriberHandle = caller.unreflect(subscriberMethod);

		return (BiConsumer<Object,IEventContext>) LambdaMetafactory.metafactory(
				caller,
				mappingName,
				MethodType.methodType(BiConsumer.class),
				subscriberHandle.type().erase(),
				subscriberHandle,
				subscriberHandle.type()).getTarget().invokeExact();
	}

	/**
	 * Checks if {@link #eventType} is assignable from the class of an {@link IEventContext}.
	 *
	 * @param event The event to test.
	 * @return True if {@link #eventType} is assignable from {@code event}'s class.
	 */
	@SuppressWarnings("unchecked")
	public boolean canConsumeEvent(@NotNull final IEventContext event)
	{ return canConsumeEvent((Class<IEventContext>) event.getClass()); }

	/**
	 * Checks if {@link #eventType} is assignable from {@code eventType}.
	 *
	 * @param eventType The class to test.
	 * @return True if {@link #eventType} is assignable from the parameter {@code eventType}.
	 */
	public boolean canConsumeEvent(@NotNull final Class<IEventContext> eventType)
	{ return eventType.isAssignableFrom(this.eventType); }

	/**
	 * Publishes the {@link IEventContext} to the subscribing method.
	 *
	 * @param eventContext The {@link IEventContext} to publish to the subscriber.
	 * @throws IllegalArgumentException When {@code eventContext} is not compatible with {@link #eventType}.
	 * @throws NullPointerException     When {@code eventContext} is null.
	 */
	public void publish(@NotNull final IEventContext eventContext) {
		Objects.requireNonNull(eventContext, "eventContext must not be null");
		if (!canConsumeEvent(eventContext)) throw new IllegalArgumentException(
				"published an event to an incorrect subscriber (type mismatch)");
		callMap.accept(owner, eventContext);
	}

	/**
	 * @param o An instance of the class to test.
	 * @return Whether this subscriber comes from {@code o}.
	 */
	public boolean from(final Object o) { return from(o.getClass()); }

	/**
	 * @param c The class to test.
	 * @return Whether this subscriber comes from {@code c}.
	 */
	public boolean from(final Class<?> c) { return owner.getClass().equals(c); }

	/**
	 * @param o An object of the class to test.
	 * @param m The method to test.
	 * @return Whether this subscriber maps to {@code m}.
	 */
	public boolean is(final Object o, final Method m) { return from(o) && subscriberMethod.equals(m); }
}