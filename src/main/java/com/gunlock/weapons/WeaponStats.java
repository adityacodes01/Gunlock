package com.gunlock.weapons;

import java.util.EnumSet;
import java.util.Set;

/**
 * Immutable balance sheet for a single firearm. BASE values; config and
 * attachments apply multipliers at runtime via the config-resolved accessor.
 *
 * Two asset/behaviour fields beyond the raw combat numbers:
 *   fireSound  the sound event id to play on fire ("fire_standard" or
 *              "fire_energy") — must match a key in assets/gunlock/sounds.json
 *   gravity    if true, hits apply a gravitational impulse to nearby
 *              entities instead of plain damage (the Gravity Gun)
 */
public record WeaponStats(
        WeaponClass weaponClass,
        double damage,
        int fireRateRpm,
        double reloadSeconds,
        double recoil,
        double accuracy,
        double spreadDegrees,
        int durability,
        double rangeBlocks,
        double critMultiplier,
        double headshotMultiplier,
        double bulletVelocity,
        double armorPen,
        int pellets,
        int magSize,
        int burstCount,
        Set<FireMode> fireModes,
        FireMode defaultFireMode,
        String fireSound,
        boolean gravity
) {
    public int cooldownTicks() { return Math.max(1, (int) Math.round(1200.0 / fireRateRpm)); }
    public int reloadTicks()   { return Math.max(1, (int) Math.round(reloadSeconds * 20.0)); }

    public static Builder builder(WeaponClass weaponClass) { return new Builder(weaponClass); }

    public static final class Builder {
        private final WeaponClass weaponClass;
        private double damage = 6;
        private int fireRateRpm = 400;
        private double reloadSeconds = 2.0;
        private double recoil = 0.25;
        private double accuracy = 0.85;
        private double spreadDegrees = 2.5;
        private int durability = 800;
        private double rangeBlocks = 35;
        private double critMultiplier = 1.5;
        private double headshotMultiplier = 1.8;
        private double bulletVelocity = 3.4;
        private double armorPen = 0.15;
        private int pellets = 1;
        private int magSize = 15;
        private int burstCount = 3;
        private Set<FireMode> fireModes = EnumSet.of(FireMode.SEMI);
        private FireMode defaultFireMode = FireMode.SEMI;
        private String fireSound = "fire_standard";
        private boolean gravity = false;

        private Builder(WeaponClass weaponClass) { this.weaponClass = weaponClass; }

        public Builder damage(double v)            { this.damage = v; return this; }
        public Builder fireRateRpm(int v)          { this.fireRateRpm = v; return this; }
        public Builder reloadSeconds(double v)     { this.reloadSeconds = v; return this; }
        public Builder recoil(double v)            { this.recoil = v; return this; }
        public Builder accuracy(double v)          { this.accuracy = v; return this; }
        public Builder spreadDegrees(double v)     { this.spreadDegrees = v; return this; }
        public Builder durability(int v)           { this.durability = v; return this; }
        public Builder rangeBlocks(double v)       { this.rangeBlocks = v; return this; }
        public Builder critMultiplier(double v)    { this.critMultiplier = v; return this; }
        public Builder headshotMultiplier(double v){ this.headshotMultiplier = v; return this; }
        public Builder bulletVelocity(double v)    { this.bulletVelocity = v; return this; }
        public Builder armorPen(double v)          { this.armorPen = v; return this; }
        public Builder pellets(int v)              { this.pellets = v; return this; }
        public Builder magSize(int v)              { this.magSize = v; return this; }
        public Builder burstCount(int v)           { this.burstCount = v; return this; }
        public Builder fireSound(String v)         { this.fireSound = v; return this; }
        public Builder gravity(boolean v)          { this.gravity = v; return this; }

        public Builder fireModes(FireMode first, FireMode... rest) {
            this.fireModes = EnumSet.of(first, rest);
            this.defaultFireMode = first;
            return this;
        }
        public Builder defaultFireMode(FireMode v) { this.defaultFireMode = v; return this; }

        public WeaponStats build() {
            return new WeaponStats(weaponClass, damage, fireRateRpm, reloadSeconds, recoil,
                    accuracy, spreadDegrees, durability, rangeBlocks, critMultiplier,
                    headshotMultiplier, bulletVelocity, armorPen, pellets, magSize,
                    burstCount, fireModes, defaultFireMode, fireSound, gravity);
        }
    }
}
