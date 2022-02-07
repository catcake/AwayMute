package xyz.catcake.event;

/** {@link IEventContext} is how the {@link EventBus} routes events to their subscribers without mix-ups. */
public interface IEventContext {

	/**
	 * Determines if an object is an instance of {@link IEventContext}.
	 *
	 * @param o The object to test.
	 * @return True if {@code o} is an instance of {@link IEventContext}, otherwise False.
	 */
	static boolean isEventContext(final Object o) { return o instanceof IEventContext; }

	/**
	 * Determines if a class derives {@link IEventContext}.
	 *
	 * @param c The class to test.
	 * @return True if {@code c} derives {@link IEventContext}, otherwise False.
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	static boolean isEventContext(final Class<?> c) { return IEventContext.class.isAssignableFrom(c); }
}