package com.gunlock.weapons;

import com.gunlock.config.GunlockConfig;
import com.gunlock.registry.GunlockSounds;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Base item for every firearm.
 *
 * Instant-fire shooting (phase 2a): right-click spawns a fast arrow "bullet"
 * in the look direction, deals the gun's damage, plays the fire sound, and
 * applies a per-gun cooldown derived from fire-rate RPM. Using a vanilla
 * Arrow keeps this to ONE file with no custom entity/renderer registration;
 * we upgrade the projectile to the bullet sprite once this is confirmed.
 *
 * The four lines marked (A)-(D) use Minecraft/Forge APIs whose exact form is
 * 26.2-version-sensitive; if the build errors, those are the spots to adjust.
 */
public class GunItem extends Item {
    private final String weaponId;
    private final WeaponStats baseStats;

    public GunItem(String weaponId, WeaponStats baseStats, Item.Properties props) {
        super(configure(props, baseStats));
        this.weaponId = weaponId;
        this.baseStats = baseStats;
    }

    private static Item.Properties configure(Item.Properties p, WeaponStats s) {
        p.stacksTo(1);
        if (s.durability() > 0) p.durability(s.durability());
        return p;
    }

    public String weaponId()            { return weaponId; }
    public WeaponStats baseStats()      { return baseStats; }
    public WeaponStats effectiveStats() { return GunlockConfig.apply(weaponId, baseStats); }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {   // (A) signature/return
        ItemStack stack = player.getItemInHand(hand);
        WeaponStats s = effectiveStats();

        if (!level.isClientSide) {
            Arrow bullet = new Arrow(level, player, new ItemStack(Items.ARROW), ItemStack.EMPTY); // (D) constructor
            float inaccuracy = (float) Math.max(0.0, (1.0 - s.accuracy()) * 6.0);
            bullet.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                    (float) s.bulletVelocity(), inaccuracy);
            bullet.setBaseDamage(s.damage());
            level.addFreshEntity(bullet);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    GunlockSounds.fire(s.fireSound()).get(), SoundSource.PLAYERS, 0.9F, 1.0F);
        }

        player.getCooldowns().addCooldown(stack, s.cooldownTicks());   // (B) addCooldown arg type
        return InteractionResult.SUCCESS;                              // (C) return constant
    }
}
