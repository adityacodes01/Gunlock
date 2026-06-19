package com.gunlock.config;

import com.gunlock.Gunlock;
import com.gunlock.weapons.WeaponStats;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Runtime, no-recompile balancing via a real ForgeConfigSpec (pattern taken
 * from the 26.2 MDK example). Server owners edit config/gunlock-server.toml;
 * {@link #apply} layers the multipliers over a weapon's authored stats.
 */
@Mod.EventBusSubscriber(modid = Gunlock.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GunlockConfig {
    private GunlockConfig() {}

    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.DoubleValue DAMAGE = B
            .comment("Global damage multiplier").defineInRange("damageMultiplier", 1.0, 0.1, 10.0);
    private static final ForgeConfigSpec.DoubleValue FIRE_RATE = B
            .comment("Global fire-rate multiplier").defineInRange("fireRateMultiplier", 1.0, 0.1, 10.0);
    private static final ForgeConfigSpec.DoubleValue RELOAD = B
            .comment("Global reload-time multiplier").defineInRange("reloadTimeMultiplier", 1.0, 0.1, 10.0);
    private static final ForgeConfigSpec.DoubleValue RECOIL = B
            .comment("Global recoil multiplier").defineInRange("recoilMultiplier", 1.0, 0.0, 10.0);
    private static final ForgeConfigSpec.DoubleValue HEADSHOT = B
            .comment("Global headshot multiplier").defineInRange("headshotMultiplier", 1.0, 0.1, 10.0);
    private static final ForgeConfigSpec.DoubleValue VELOCITY = B
            .comment("Global bullet-velocity multiplier").defineInRange("velocityMultiplier", 1.0, 0.1, 10.0);
    private static final ForgeConfigSpec.DoubleValue DURABILITY = B
            .comment("Global durability multiplier").defineInRange("durabilityMultiplier", 1.0, 0.1, 10.0);

    public static final ForgeConfigSpec SPEC = B.build();

    // resolved values (1.0 = authored)
    public static double GLOBAL_DAMAGE = 1.0, GLOBAL_FIRE_RATE = 1.0, GLOBAL_RELOAD_TIME = 1.0,
            GLOBAL_RECOIL = 1.0, GLOBAL_HEADSHOT = 1.0, GLOBAL_VELOCITY = 1.0, GLOBAL_DURABILITY = 1.0;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        GLOBAL_DAMAGE = DAMAGE.get();
        GLOBAL_FIRE_RATE = FIRE_RATE.get();
        GLOBAL_RELOAD_TIME = RELOAD.get();
        GLOBAL_RECOIL = RECOIL.get();
        GLOBAL_HEADSHOT = HEADSHOT.get();
        GLOBAL_VELOCITY = VELOCITY.get();
        GLOBAL_DURABILITY = DURABILITY.get();
    }

    /** Returns a copy of {@code base} with configured multipliers applied. */
    public static WeaponStats apply(String weaponId, WeaponStats base) {
        return WeaponStats.builder(base.weaponClass())
                .damage(base.damage() * GLOBAL_DAMAGE)
                .fireRateRpm((int) Math.round(base.fireRateRpm() * GLOBAL_FIRE_RATE))
                .reloadSeconds(base.reloadSeconds() * GLOBAL_RELOAD_TIME)
                .recoil(base.recoil() * GLOBAL_RECOIL)
                .accuracy(base.accuracy())
                .spreadDegrees(base.spreadDegrees())
                .durability((int) Math.round(base.durability() * GLOBAL_DURABILITY))
                .rangeBlocks(base.rangeBlocks())
                .critMultiplier(base.critMultiplier())
                .headshotMultiplier(base.headshotMultiplier() * GLOBAL_HEADSHOT)
                .bulletVelocity(base.bulletVelocity() * GLOBAL_VELOCITY)
                .armorPen(base.armorPen())
                .pellets(base.pellets())
                .magSize(base.magSize())
                .burstCount(base.burstCount())
                .fireSound(base.fireSound())
                .gravity(base.gravity())
                .fireModes(base.defaultFireMode(),
                        base.fireModes().stream()
                                .filter(m -> m != base.defaultFireMode())
                                .toArray(com.gunlock.weapons.FireMode[]::new))
                .build();
    }
}
