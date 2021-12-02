package xyz.catcake.event;

import java.lang.reflect.Method;

// package private
record Subscriber(Object owner, Method subscriber) {

	public Subscriber {
		if (owner == null) throw new NullPointerException();
		if (subscriber == null) throw new NullPointerException();
	}

	public Class<?>[] getEventContext() { return subscriber.getParameterTypes(); }
}