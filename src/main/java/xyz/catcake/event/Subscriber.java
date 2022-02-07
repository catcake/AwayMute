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
 * This creates a "mapping" (programmatically created lambda) function to the subscriber method, which can be called
 * much more quickly than {@link Method#invoke}.
 *
 * @param <T> An event context.
 */
// TODO: support static methods?
// package private
final class Subscriber <T extends IEventContext> {

	private static final Logger log;

	static { log = LogManager.getLogger(new PrefixedMessageFactory(AwayMuteMod.LOG_PREFIX)); }

	private final Class<T>             eventType;
	private final Object               owner;
	private final BiConsumer<Object,T> callMap;

	/**
	 * @param owner An instance of the owner of subscribingMethod.
	 * @param subscriberMethod The subscribing method.
	 * @throws Throwable From {@link #setupCallMapping}.
	 */
	@SuppressWarnings("unchecked")
	public Subscriber(@NotNull final Object owner, @NotNull final Method subscriberMethod) throws Throwable {
		Objects.requireNonNull(subscriberMethod, "subscriber must not be null");
		final Class<?> eventType = subscriberMethod.getParameterTypes()[0];
		if (!IEventContext.class.isAssignableFrom(eventType)) throw new IllegalArgumentException(
				"methods with @EventSubscribe must have 1 parameter, deriving IEventContext");
		this.eventType    = (Class<T>) eventType;
		this.owner = owner;
		callMap    = setupCallMapping(subscriberMethod);
	}

	/**
	 * @param subscriberMethod The subscribing method.
	 * @return A {@link BiConsumer} where T is the owner of the subscriber and U is the event type.
	 *
	 * @throws IllegalAccessException    From {@link Lookup#unreflect}.
	 * @throws LambdaConversionException From {@link LambdaMetafactory#metafactory}.
	 * @throws Throwable                 From {@link MethodHandle#invokeExact}.
	 */
	@SuppressWarnings("unchecked")
	private BiConsumer<Object,T> setupCallMapping(final Method subscriberMethod)
			throws IllegalAccessException, LambdaConversionException, Throwable {
		final String       mappingName = "accept";
		final Lookup       caller      = MethodHandles.lookup();
		final MethodHandle subscriberHandle;

		subscriberMethod.setAccessible(true);
		subscriberHandle = caller.unreflect(subscriberMethod);

		return (BiConsumer<Object,T>) LambdaMetafactory.metafactory(
				caller,
				mappingName,
				MethodType.methodType(BiConsumer.class),
				subscriberHandle.type().erase(),
				subscriberHandle,
				subscriberHandle.type()).getTarget().invokeExact();
	}

	public void publish(@NotNull final IEventContext eventContext) {
		Objects.requireNonNull(eventContext, "eventContext must not be null");
		Objects.requireNonNull(
				callMap,
				"subscriberMethod must not be null. Has #setupSubscriberHandle been called?");
		if (!eventType.equals(eventContext.getClass())) throw new IllegalArgumentException(
				"published an event to an incorrect subscriber (type mismatch)");
		callMap.accept(owner, eventType.cast(eventContext));
	}
}