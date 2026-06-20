package com.gunlock.weapons;

import com.gunlock.config.GunlockConfig;
import com.gunlock.registry.GunlockItems;
import com.gunlock.registry.GunlockSounds;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Base item for every firearm.
 *
 * Ammo rule:
 *   - Creative (player.getAbilities().instabuild): unlimited, consumes nothing.
 *   - Survival: each shot consumes ONE Bullet from the inventory. With no
 *     bullets left, the gun will not fire.
 *
 * Shooting is instant hitscan: raycast from the eyes, particle tracer to the
 * impact, damage the first living entity hit, fire sound, per-gun cooldown.
 *
 * Lines marked (RISK) use APIs whose exact 26.2 form I couldn't verify.
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
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        WeaponStats s = effectiveStats();

        boolean creative = player.getAbilities().instabuild;   // (RISK) abilities/instabuild

        // Survival must pay one bullet per shot; out of ammo = no fire.
        if (!creative && !consumeOneBullet(player)) {
            player.getCooldowns().addCooldown(stack, 6);   // brief dry-fire delay
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel server) {
            fire(server, player, s);
            server.playSound(null, player.getX(), player.getY(), player.getZ(),
                    GunlockSounds.fire(s.fireSound()).get(), SoundSource.PLAYERS, 0.9F, 1.0F);
        }

        player.getCooldowns().addCooldown(stack, s.cooldownTicks());   // (RISK) addCooldown(ItemStack,int)
        return InteractionResult.SUCCESS;
    }

    /** Removes one Bullet from the player's inventory; false if none found. */
    private boolean consumeOneBullet(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack st = inv.getItem(i);
            if (st.is(GunlockItems.BULLET.get())) {
                st.shrink(1);
                return true;
            }
        }
        return false;
    }

    private void fire(ServerLevel level, Player player, WeaponStats s) {
        Vec3 eye = player.getEyePosition();
        Vec3 dir = player.getViewVector(1.0F).normalize();
        Vec3 end = eye.add(dir.scale(s.rangeBlocks()));

        BlockHitResult block = level.clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }

        LivingEntity target = null;
        double best = Double.MAX_VALUE;
        AABB scan = new AABB(eye, end).inflate(1.0);
        for (Entity e : level.getEntities(player, scan,
                x -> x instanceof LivingEntity && x.isPickable() && x != player)) {
            Optional<Vec3> hit = e.getBoundingBox().inflate(0.3).clip(eye, end);
            if (hit.isPresent()) {
                double d = eye.distanceToSqr(hit.get());
                if (d < best) { best = d; target = (LivingEntity) e; end = hit.get(); }
            }
        }

        Vec3 path = end.subtract(eye);
        int steps = (int) Math.max(1, path.length() * 2);
        for (int i = 0; i <= steps; i++) {
            Vec3 p = eye.add(path.scale((double) i / steps));
            level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0, 0, 0, 0.0); // (RISK) sendParticles sig
        }

        if (target != null) {
            target.hurt(level.damageSources().playerAttack(player), (float) s.damage()); // (RISK) hurt vs hurtServer
        }
    }
}
