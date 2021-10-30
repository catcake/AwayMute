package xyz.catcake.awaymute;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.stream.Collectors;

public final class AwayMuteMod implements ModInitializer {
	public static final String MOD_ID = "awaymute";
	public static final String MOD_NAME = "AwayMute";

	private static final Logger backupLog;
	private static final HashMap<String, Logger> mappedLoggers;

	private static AwayMute awayMute;

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

	@Override
	public void onInitialize() {
		info("initializing...");
		awayMute = new AwayMute();
		info("finished initializing!");
	}

	public static void info(final String message) {
		try {
			getCallerLog().info(prefixMessage(message));
		} catch (IllegalStateException e) {
			backupLog.info(prefixMessage(message));
		}
	}

	public static void log(final Level level, final String message) {
		try {
			getCallerLog().log(level, prefixMessage(message));
		} catch (IllegalStateException e) {
			backupLog.log(level, prefixMessage(message));
		}
	}

	private static String prefixMessage(String message) {
		return String.format("[%s] %s", MOD_NAME, message);
	}

	private static Logger getCallerLog() throws IllegalStateException {
		final var logCaller = getStackFrame().getDeclaringClass();

		final var logAttempt = mappedLoggers.get(logCaller.getCanonicalName());
		if (logAttempt != null) return logAttempt;

		return registerLog(logCaller);
	}

	private static StackWalker.StackFrame getStackFrame() throws IllegalStateException {
		final var stackframes = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
				.walk((frame) -> frame.collect(Collectors.toList()));

		if (stackframes.size() < 4) throw new IllegalStateException("unexpectedly small amount of stack frames");
		if (!stackframes.get(2).getMethodName().equals("log") && !stackframes.get(2).getMethodName().equals("info"))
			throw new IllegalStateException("getLog is permitted to be invoked only by log and info");

		return stackframes.get(3);
	}

	private static Logger registerLog(Class<?> clazz) {
		final var log = LogManager.getLogger(clazz);
		mappedLoggers.put(clazz.getCanonicalName(), log);
		return log;
	}
}