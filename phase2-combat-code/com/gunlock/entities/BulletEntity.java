package com.gunlock.entities;

import com.gunlock.weapons.WeaponStats;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Server-only projectile fired by every gun. Ticked only on the logical
 * server; clients draw a tracer particle via a feedback packet, so we never
 * pay for a full entity sync per bullet (the scale lever).
 *
 * The combat below uses Minecraft's own (vanilla) APIs — ClipContext,
 * ProjectileUtil, AABB, damageSources, setDeltaMovement — which are stable
 * across 1.21.x / 26.x. The one version-sensitive signature is
 * defineSynchedData (changed to take a Builder in 1.20.5+); it's overridden
 * with the modern form.
 */
public class BulletEntity extends Projectile {
    private double damage = 6.0;
    private double critMultiplier = 1.5;
    private double headshotMultiplier = 1.8;
    private double armorPen = 0.15;
    private double rangeBlocks = 35;
    private double maxLifeBlocks = 200;
    private boolean affectedByDrop = true;
    private boolean gravity = false;
    private double travelled = 0;

    public BulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public void configureFrom(WeaponStats stats) {
        this.damage = stats.damage();
        this.critMultiplier = stats.critMultiplier();
        this.headshotMultiplier = stats.headshotMultiplier();
        this.armorPen = stats.armorPen();
        this.rangeBlocks = stats.rangeBlocks();
        this.affectedByDrop = stats.bulletVelocity() < 7.0;
        this.gravity = stats.gravity();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        Vec3 motion = getDeltaMovement();
        Vec3 from = position();
        Vec3 to = from.add(motion);
        travelled += motion.length();

        // Nearest of (block hit, entity hit) along this tick's segment.
        BlockHitResult blockHit = level().clip(new ClipContext(
                from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 segEnd = blockHit.getType() == HitResult.Type.MISS ? to : blockHit.getLocation();

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level(), this, from, segEnd,
                new AABB(from, segEnd).inflate(0.3),
                e -> e != getOwner() && e.isAlive() && e.isPickable());

        HitResult hit = entityHit != null ? entityHit
                : (blockHit.getType() != HitResult.Type.MISS ? blockHit : null);

        if (hit != null) {
            if (gravity) applyGravityWell(hit.getLocation());
            onHit(hit);
            discard();
            return;
        }

        if (affectedByDrop) setDeltaMovement(motion.add(0, -0.02, 0));
        setPos(to.x, to.y, to.z);
        if (travelled >= maxLifeBlocks) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        double dmg = damageWithFalloff();

        if (isHeadHit(result, target)) dmg *= headshotMultiplier;
        if (this.random.nextFloat() < 0.10f) dmg *= critMultiplier; // base crit

        if (target instanceof LivingEntity living) {
            // Owner attribution gives correct kill credit + PvP rules.
            LivingEntity owner = getOwner() instanceof LivingEntity le ? le : null;
            // armorPen note: true armour bypass needs a custom damage-type tag;
            // applied here as a proportional bonus as a first approximation.
            float finalDmg = (float) (dmg * (1.0 + armorPen * 0.5));
            living.hurt(level().damageSources().mobProjectile(this, owner), finalDmg);
        }
    }

    private void applyGravityWell(Vec3 center) {
        final double radius = 6.0, strength = 1.6;
        AABB box = new AABB(center, center).inflate(radius);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != getOwner())) {
            Vec3 toCenter = center.subtract(e.position());
            double dist = toCenter.length();
            if (dist < 0.01) continue;
            double falloff = Math.max(0.0, 1.0 - dist / radius);
            Vec3 pull = toCenter.normalize().scale(strength * falloff);
            e.setDeltaMovement(e.getDeltaMovement().add(pull.x, pull.y + 0.35 * falloff, pull.z));
            e.hurtMarked = true; // forces velocity sync to clients
        }
    }

    private double damageWithFalloff() {
        if (travelled <= rangeBlocks) return damage;
        double over = travelled - rangeBlocks;
        return damage * Math.max(0.4, 1.0 - over / (rangeBlocks * 1.5));
    }

    private boolean isHeadHit(EntityHitResult result, Entity target) {
        double rel = (result.getLocation().y - target.getY()) / Math.max(0.1, target.getBbHeight());
        return rel >= 0.82;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        // Bullets are server-only + rendered as particles, so no synced data.
        // Modern (1.20.5+) signature.
    }
}
