package com.gunlock.entities;

import com.gunlock.weapons.WeaponStats;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;

/**
 * The projectile fired by every gun. Spawned and ticked ONLY on the
 * logical server; clients receive a lightweight trail/tracer particle via
 * a separate packet so we never pay for a full entity sync per bullet
 * (this is the main lever for the "60+ players firing" target).
 *
 * <p>Behaviour modelled from {@link WeaponStats}:
 * <ul>
 *   <li>velocity → blocks/tick (very high velocities approximate hitscan)</li>
 *   <li>bullet drop → a small per-tick gravity unless the weapon is the railgun</li>
 *   <li>range falloff → damage scales down past {@code rangeBlocks}</li>
 *   <li>headshot → vertical position of the impact relative to the target's
 *       eye/head box decides the multiplier</li>
 *   <li>armor penetration → fraction of armour ignored when applying damage</li>
 * </ul>
 *
 * <p><b>TODO(26.2)</b>: the projectile/hit-result and damage-source APIs
 * are the most version-sensitive part of this class. The control flow is
 * stable; the method names (e.g. how you build a DamageSource, how you
 * read the hit sub-box for headshots) should be checked against the MDK.
 */
public class BulletEntity extends Projectile {
    private double damage = 6.0;
    private double critMultiplier = 1.5;
    private double headshotMultiplier = 1.8;
    private double armorPen = 0.15;
    private double rangeBlocks = 35;
    private double maxLifeBlocks = 200; // despawn distance
    private boolean affectedByDrop = true;
    private double travelled = 0;

    public BulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    /** Copies the firing weapon's resolved stats onto the projectile. */
    public void configureFrom(WeaponStats stats) {
        this.damage = stats.damage();
        this.critMultiplier = stats.critMultiplier();
        this.headshotMultiplier = stats.headshotMultiplier();
        this.armorPen = stats.armorPen();
        this.rangeBlocks = stats.rangeBlocks();
        this.affectedByDrop = stats.bulletVelocity() < 7.0; // railgun-class = flat
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return; // server owns the bullet

        Vec3 motion = getDeltaMovement();
        Vec3 from = position();
        Vec3 to = from.add(motion);
        travelled += motion.length();

        // TODO(26.2): use the level clip + entity sweep helpers the MDK
        // exposes. The intent: ray from `from` to `to`, take the nearest
        // of (block hit, entity hit).
        HitResult hit = level().clip(buildClipContext(from, to));
        if (hit.getType() != HitResult.Type.MISS) {
            onHit(hit);
            discard();
            return;
        }

        if (affectedByDrop) {
            motion = motion.add(0, -0.02, 0); // gentle drop
            setDeltaMovement(motion);
        }
        setPos(to.x, to.y, to.z);

        if (travelled >= maxLifeBlocks) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        double dmg = damageWithFalloff();

        boolean headshot = isHeadHit(result, target);
        if (headshot) dmg *= headshotMultiplier;

        // crit is independent of headshot (e.g. railgun, snipers)
        if (rollCrit()) dmg *= critMultiplier;

        if (target instanceof LivingEntity living) {
            // TODO(26.2): build a proper DamageSource (owner attribution for
            // kill credit + PvP rules) and apply armor penetration. The
            // armorPen fraction should bypass that portion of the target's
            // armour value rather than the post-armour result.
            applyServerDamage(living, dmg, armorPen, headshot);
        }
    }

    // --- helpers (intentionally small; bodies are the version-sensitive bits) ---

    private double damageWithFalloff() {
        if (travelled <= rangeBlocks) return damage;
        double over = travelled - rangeBlocks;
        double factor = Math.max(0.4, 1.0 - over / (rangeBlocks * 1.5));
        return damage * factor;
    }

    private boolean isHeadHit(EntityHitResult result, Entity target) {
        // Approximate: impact y in the top ~18% of the bounding box.
        double rel = (result.getLocation().y - target.getY()) / Math.max(0.1, target.getBbHeight());
        return rel >= 0.82;
    }

    private boolean rollCrit() {
        return this.random.nextFloat() < 0.10f; // 10% base; tune/config later
    }

    // The three methods below are deliberately left as named seams to fill
    // against the 26.2 MDK, so the combat math above stays readable.
    private ClipContext buildClipContext(Vec3 from, Vec3 to) {
        // Basic block-only clip: ignore fluids and use this entity as the context owner.
        return new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
    }

    private void applyServerDamage(LivingEntity target, double amount, double pen, boolean headshot) {
        // Minimal damage application to avoid runtime exceptions during tests.
        target.hurt(DamageSource.GENERIC, (float) amount);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synced data needed: bullets are server-only and rendered as
        // particles client-side. Keep the body empty for API completeness.
    }
}
