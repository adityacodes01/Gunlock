package com.gunlock;

import com.gunlock.config.GunlockConfig;
import com.gunlock.registry.GunlockItems;
import com.gunlock.registry.GunlockSounds;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Gunlock — mod entry point, wired to the real Forge 26.2 (65.0.0) API.
 * This is the core build: registers the bullet, 15 guns, sounds, a creative
 * tab, and the balance config. Combat/shooting is the phase-2 layer.
 */
@Mod(Gunlock.MOD_ID)
public final class Gunlock {
    public static final String MOD_ID = "gunlock";
    public static final Logger LOG = LogUtils.getLogger();

    public Gunlock(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        GunlockItems.ITEMS.register(modBusGroup);
        GunlockItems.CREATIVE_MODE_TABS.register(modBusGroup);
        GunlockSounds.SOUNDS.register(modBusGroup);

        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);

        context.registerConfig(ModConfig.Type.SERVER, GunlockConfig.SPEC);

        LOG.info("Gunlock loaded {} firearms", com.gunlock.weapons.Weapons.count());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOG.info("Gunlock common setup complete");
    }
}
