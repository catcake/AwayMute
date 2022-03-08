package xyz.catcake.awaymute;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.catcake.awaymute.config.AwayMuteConfig;
import xyz.catcake.awaymute.impl.AwayMute;
import xyz.catcake.event.EventBus;
import xyz.catcake.event.IEventContext;
import xyz.catcake.log.PrefixedMessageFactory;

import java.util.Objects;

public final class AwayMuteMod implements ClientModInitializer {

	// Other static initializers rely on this value, therefore it must(?) be set before the initializer.
	public static final  String      LOG_PREFIX = "[AwayMute]";
	public static final  String      MOD_ID     = "awaymute";
	private static final String      MOD_NAME   = "AwayMute";
	private static final Logger      log;
	private static       boolean     initialized;
	private static       AwayMuteMod instance;

	static {
		log         = LogManager.getLogger(new PrefixedMessageFactory(LOG_PREFIX));
		initialized = false;
		instance    = null;
	}

	private final EventBus eventManager;
	private       AwayMute awayMute;

	/**
	 * @throws IllegalStateException if {@link AwayMuteMod} is constructed more than once during the lifespan of the
	 *                               application.
	 */
	public AwayMuteMod() {
		if (Objects.nonNull(instance)) throw new IllegalStateException("only one instance allowed");
		instance     = this;
		eventManager = new EventBus(MOD_NAME);
	}

	public static AwayMuteMod Instance() { return instance; }

	/**
	 * Publish an event to the mod's event bus.
	 * @param eventContext An instance of an event type to published.
	 */
	public void publishEvent(final IEventContext eventContext) { eventManager.publish(eventContext); }

	/** {@inheritDoc} */
	@Override
	public void onInitializeClient() {
		if (initialized) throw new IllegalStateException("#onInitializeClient should only be called once, by fabric");
		initialized = true;
		log.info("initializing...");
		final ConfigHolder<AwayMuteConfig> configHolder;
		AutoConfig.register(AwayMuteConfig.class, GsonConfigSerializer::new);
		configHolder = AutoConfig.getConfigHolder(AwayMuteConfig.class);
		configHolder.registerSaveListener((holder, config) -> {
			setupAwayMute(config.enabled(), config.volumeRampRate());
			return ActionResult.SUCCESS;
		});
		setupAwayMute(configHolder.get().enabled(), configHolder.get().volumeRampRate());
		log.info("finished initializing!");
	}

	private void setupAwayMute(final boolean enabled, final int volumeRampRate) {
		if (Objects.isNull(awayMute)) awayMute = new AwayMute(volumeRampRate);
		else eventManager.unsubscribe(awayMute);
		if (enabled) eventManager.subscribe(awayMute);
	}
}