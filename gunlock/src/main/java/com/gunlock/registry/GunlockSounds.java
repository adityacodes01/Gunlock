package com.gunlock.registry;

import com.gunlock.Gunlock;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Sound events backed by the OGG files and declared in sounds.json.
 * Forge 26.2 renamed ResourceLocation -> Identifier (seen in the MDK).
 */
public final class GunlockSounds {
    private GunlockSounds() {}

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Gunlock.MOD_ID);

    public static final RegistryObject<SoundEvent> FIRE_STANDARD = register("fire_standard");
    public static final RegistryObject<SoundEvent> FIRE_ENERGY   = register("fire_energy");
    public static final RegistryObject<SoundEvent> RELOAD        = register("reload");

    private static RegistryObject<SoundEvent> register(String name) {
        Identifier id = Identifier.tryParse(Gunlock.MOD_ID + ":" + name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static RegistryObject<SoundEvent> fire(String key) {
        return "fire_energy".equals(key) ? FIRE_ENERGY : FIRE_STANDARD;
    }
}
