package xyz.catcake.awaymute;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import xyz.catcake.event.EventManager;
import xyz.catcake.log.LogFactoryAbstractionL4J;

import java.util.HashMap;

public final class AwayMuteMod implements ClientModInitializer {
	public static final String MOD_ID = "awaymute";
	public static final String MOD_NAME = "AwayMute";
	public static final LogFactoryAbstractionL4J LOG;

	private static boolean instantiated;
	private static EventManager eventManager;

	static {
		LOG = new LogFactoryAbstractionL4J(LogManager.getLogger(), MOD_NAME, new HashMap<>());
		instantiated = false;
	}

	/**
	 * @throws IllegalStateException if {@link AwayMuteMod} is constructed
	 * more than once during the lifespan of the application.
	 */
	public AwayMuteMod() throws IllegalStateException {
		if (instantiated) throw new IllegalStateException("only one instance allowed");
		instantiated = true;
	}

	/**
	 * {@link NullPointerException} happens  in situations where the
	 * {@link xyz.catcake.awaymute.AwayMuteMod#eventManager} is <code>null</code>.
	 * This can happen if {@link AwayMuteMod#onInitializeClient()} has not yet
	 * been called or was not called.
	 * @return an {@link xyz.catcake.event.EventManager}
	 * @throws NullPointerException when {@link xyz.catcake.awaymute.AwayMuteMod#eventManager} is <code>null</code>.
	 */
	public static EventManager getEventManager() throws NullPointerException {
		if (eventManager == null) throw new NullPointerException();
		return eventManager;
	}

	/** {@inheritDoc} */
	@Override
	public void onInitializeClient() {
		LOG.info("initializing...");
		final var awayMute = new AwayMute();
		eventManager = new EventManager(new HashMap<>());
		eventManager.subscribe(awayMute);
		LOG.info("finished initializing!");
	}
}