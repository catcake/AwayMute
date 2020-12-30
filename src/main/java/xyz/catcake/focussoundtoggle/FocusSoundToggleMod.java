package xyz.catcake.focussoundtoggle;

import net.minecraft.client.Minecraft;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

@Mod(modid = FocusSoundToggleMod.MODID, name = FocusSoundToggleMod.NAME, version = FocusSoundToggleMod.VERSION)
@Mod.EventBusSubscriber(Side.CLIENT)
public class FocusSoundToggleMod {
    public static final String MODID = "focussoundtoggle";
    public static final String NAME = "Focus Sound Toggle";
    public static final String VERSION = "1.0";

    private static Logger logger;
    private static boolean windowFocused = true;
    private static float originalVolume = 0;

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (Display.isCreated()) {
            if (windowFocused && !Display.isActive()) {
                originalVolume = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER);
                Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.MASTER, 0);
                windowFocused = false;
                logger.info("window unfocused, setting volume to 0. Old volume was " + originalVolume);
            } else if (!windowFocused && Display.isActive()) {
                Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.MASTER, originalVolume);
                windowFocused = true;
                logger.info("window focused, restoring volume to " + originalVolume);
            }
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }
}