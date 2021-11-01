package xyz.catcake.awaymute;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.catcake.event.EventManager;

import java.util.HashMap;
import java.util.stream.Stream;

public final class AwayMuteMod implements ModInitializer {
	public static final String MOD_ID = "awaymute";
	public static final String MOD_NAME = "AwayMute";

	private static final Logger backupLog;
	private static final HashMap<String, Logger> mappedLoggers;

	private static AwayMute awayMute;
	private static EventManager eventManager;

	static {
		backupLog = LogManager.getLogger();
		mappedLoggers = new HashMap<>();
	}

	/**
	 * <code>NullPointerException</code> is thrown in situations where the <code>awayMute</code> field is
	 * 	<code>null</code>. This can happen if <code>onInitialize</code> has not yet been called or was not called.
	 * 	Both of these are considered exceptional behavior.
	 * @return an AwayMute instance.
	 * @throws NullPointerException if awayMute is null.
	 */
	public static AwayMute getAwayMute() throws NullPointerException {
		if (awayMute == null) throw new NullPointerException();
		return awayMute;
	}

	public static EventManager getEventManager() throws NullPointerException {
		if (eventManager == null) throw new NullPointerException();
		return eventManager;
	}

	@Override
	public void onInitialize() {
		info("initializing...");
		awayMute = new AwayMute();
		eventManager = new EventManager(new HashMap<>());
		eventManager.subscribe(awayMute);
		info("finished initializing!");
	}

	/**
	 * Prints an info message with the calling class's logger.
	 * @param message The message to be published.
	 */
	public static void info(final String message) {
		try {
			getLog().info(prefixMessage(message));
		} catch (IllegalStateException e) {
			backupLog.info(prefixMessage(message));
		}
	}

	/**
	 * Prints a log message with the calling class's logger.
	 * @param level The level of the log.
	 * @param message The message to be published.
	 */
	public static void log(final Level level, final String message) {
		try {
			getLog().log(level, prefixMessage(message));
		} catch (IllegalStateException e) {
			backupLog.log(level, prefixMessage(message));
		}
	}

	private static String prefixMessage(String message) {
		return String.format("[%s] %s", MOD_NAME, message);
	}

	private static Logger getLog() throws IllegalStateException {
		final var caller = getCaller();
		final var logAttempt = mappedLoggers.get(caller.getCanonicalName());
		if (logAttempt != null) return logAttempt;
		return registerLog(caller);
	}

	private static Class<?> getCaller() throws IllegalStateException {
		final var minRequiredDepth = 4;
		final var frames = StackWalker
				.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
				.walk(Stream::toList);
		if (frames.size() < minRequiredDepth)
			throw new IllegalStateException("unexpectedly small amount of stack frames");

		// 0 is this method, 1 is getLog
		final var logMethodName = frames.get(2).getMethodName();
		final var caller = frames.get(3).getDeclaringClass();
		if (!logMethodName.equals("log") && !logMethodName.equals("info"))
			throw new IllegalStateException("getCaller is only permitted to be invoked by log and info");

		return caller;
	}

	private static Logger registerLog(Class<?> clazz) {
		final var log = LogManager.getLogger(clazz);
		mappedLoggers.put(clazz.getCanonicalName(), log);
		return log;
	}
}