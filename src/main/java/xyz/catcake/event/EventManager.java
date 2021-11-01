package xyz.catcake.event;

import xyz.catcake.awaymute.AwayMuteMod;
import xyz.catcake.util.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class EventManager {
	private final Map<List<Class<?>>,List<Pair<Object,Method>>> listeners;

	public EventManager(final Map<List<Class<?>>,List<Pair<Object,Method>>> listeners) {
		this.listeners = listeners;
	}

//	@SuppressWarnings("UnstableApiUsage")
//	public EventManager subscribeAllInPackage(final String searchPackage) {
//		AwayMuteMod.info("beginning");
//		try {
//			ClassPath.from(this.getClass().getClassLoader())
//				.getTopLevelClassesRecursive(searchPackage)
//				.stream()
//				.map(ClassPath.ClassInfo::load)
//				.forEach(this::subscribe);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		AwayMuteMod.info("end");
//		return this;
//	}

	public <T> void subscribe(final T subscriber) {
		for (final var method : subscriber.getClass().getDeclaredMethods()) {
			if (!method.isAnnotationPresent(EventSubscribe.class)) continue;
			AwayMuteMod.info(String.format("found: %s#%s", subscriber.getClass().getCanonicalName(), method.getName()));

			final var parameterTypes = Arrays.stream(method.getParameterTypes()).toList();
			final var listenerList = listeners.computeIfAbsent(parameterTypes, k -> new ArrayList<>());
			listenerList.add(new Pair<>(subscriber, method));
		}
	}

	public void publish(final Object... eventArguments) {
		final var retrievedListeners = listeners.get(
			Arrays.stream(eventArguments)
				.map(Object::getClass)
				.toList());
		if (retrievedListeners == null) return;
		for (final var pair : retrievedListeners) {
			final var listenerInstance = pair.a;
			final var listener = pair.b;

			try {
				listener.invoke(listenerInstance, eventArguments);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

//	private void desubscribe() {}
}