package xyz.catcake.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.stream.Stream;

public final class LogFactoryAbstractionL4J {
	private final Logger backupLog;
	private final Map<String,Logger> mappedLoggers;
	private final String prefix;
	private final int requiredStackDepth;
	private final int callerDepth;

	public LogFactoryAbstractionL4J(final Logger backupLog,
									final String prefix,
									final Map<String,Logger> mappedLoggers) {
		this.backupLog = backupLog;
		this.prefix = prefix;
		this.mappedLoggers = mappedLoggers;
		requiredStackDepth = 4;
		callerDepth = 3;
		// log() || info() = 2, getLog() = 1, getCaller() = 0
	}

	/**
	 * Prints an info message with the calling class's logger.
	 * @param message The message to be published.
	 */
	public void info(final String message) {
		try {
			getLog().info(prefixMessage(message));
		} catch (IllegalStateException e) {
			backupLog.info(prefixMessage(message));
		}
	}

	/**
	 * Prints a log message with the calling class's logger.
	 * @param level The level of the message.
	 * @param message The message to be published.
	 */
	public void log(final Level level, final String message) {
		try {
			getLog().log(level, prefixMessage(message));
		} catch (IllegalStateException e) {
			backupLog.log(level, prefixMessage(message));
		}
	}

	private String prefixMessage(String message) { return String.format("[%s] %s", prefix, message); }

	private Logger getLog() throws IllegalStateException {
		final var caller = getCaller();
		final var logAttempt = mappedLoggers.get(caller.getCanonicalName());
		if (logAttempt != null) return logAttempt;
		return registerLog(caller);
	}

	private Class<?> getCaller() throws IllegalStateException {
		final var frames = StackWalker
			.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
			.walk(Stream::toList);
		if (frames.size() < requiredStackDepth)
			throw new IllegalStateException(String.format(
				"unexpectedly small number of stack frames (actual: %s expected: n >= %s)",
				frames.size(), requiredStackDepth
			));
		return frames.get(callerDepth).getDeclaringClass();
	}

	private Logger registerLog(Class<?> clazz) {
		final var log = LogManager.getLogger(clazz);
		mappedLoggers.put(clazz.getCanonicalName(), log);
		return log;
	}
}