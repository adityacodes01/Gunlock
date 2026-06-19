package com.gunlock.weapons;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.gunlock.weapons.FireMode.*;
import static com.gunlock.weapons.WeaponClass.*;

/**
 * The roster. One entry per supplied texture/asset set. Stats are a
 * coherent first balance pass and are fully config-overridable.
 *
 * Sound mapping (per the asset set):
 *   guns 1-13          -> "fire_standard"
 *   future_gun, gravity_gun -> "fire_energy"
 *   reload (all)       -> "reload"
 */
public final class Weapons {
    private Weapons() {}

    public static final Map<String, WeaponStats> ALL = new LinkedHashMap<>();
    private static void register(String id, WeaponStats stats) { ALL.put(id, stats); }

    static {
        // 1. AK47 — classic full-auto assault rifle
        register("ak47", WeaponStats.builder(ASSAULT_RIFLE)
                .damage(9).fireRateRpm(600).magSize(30).reloadSeconds(2.2)
                .recoil(0.30).accuracy(0.85).spreadDegrees(2.4).durability(1000)
                .rangeBlocks(42).headshotMultiplier(1.9).bulletVelocity(3.6)
                .armorPen(0.22).fireModes(AUTO, SEMI).build());

        // 2. Chaos Gun — heavy, erratic, high damage
        register("chaos_gun", WeaponStats.builder(HEAVY)
                .damage(13).fireRateRpm(520).magSize(40).reloadSeconds(3.0)
                .recoil(0.45).accuracy(0.74).spreadDegrees(4.5).durability(1200)
                .rangeBlocks(38).critMultiplier(1.8).headshotMultiplier(2.0)
                .bulletVelocity(3.8).armorPen(0.35).fireModes(AUTO).build());

        // 3. Pistol — reliable sidearm
        register("pistol", WeaponStats.builder(PISTOL)
                .damage(7).fireRateRpm(350).magSize(15).reloadSeconds(1.5)
                .recoil(0.20).accuracy(0.88).spreadDegrees(2.0).durability(650)
                .rangeBlocks(30).headshotMultiplier(1.8).bulletVelocity(3.3)
                .armorPen(0.12).fireModes(SEMI).build());

        // 4. Jerry Chine Gun — meme bullet-hose, tiny damage, absurd RPM
        register("jerry_chine_gun", WeaponStats.builder(SMG)
                .damage(3).fireRateRpm(1300).magSize(60).reloadSeconds(2.4)
                .recoil(0.18).accuracy(0.70).spreadDegrees(4.0).durability(900)
                .rangeBlocks(22).headshotMultiplier(1.4).bulletVelocity(2.9)
                .armorPen(0.08).fireModes(AUTO).build());

        // 5. Battle Rifle — hard-hitting semi/burst
        register("battle_rifle", WeaponStats.builder(ASSAULT_RIFLE)
                .damage(11).fireRateRpm(450).magSize(20).reloadSeconds(2.3)
                .recoil(0.34).accuracy(0.88).spreadDegrees(2.0).durability(1050)
                .rangeBlocks(46).critMultiplier(1.6).headshotMultiplier(2.0)
                .bulletVelocity(4.0).armorPen(0.30).fireModes(SEMI, BURST).build());

        // 6. Laser Plasma Gun — fast energy bolts, flat trajectory
        register("laser_plasma_gun", WeaponStats.builder(HEAVY)
                .damage(8).fireRateRpm(500).magSize(25).reloadSeconds(2.6)
                .recoil(0.20).accuracy(0.92).spreadDegrees(1.2).durability(1100)
                .rangeBlocks(60).critMultiplier(1.6).headshotMultiplier(1.8)
                .bulletVelocity(7.5).armorPen(0.45).fireModes(AUTO, SEMI).build());

        // 7. Overwatch Gun — versatile mid-range
        register("overwatch_gun", WeaponStats.builder(ASSAULT_RIFLE)
                .damage(8).fireRateRpm(650).magSize(32).reloadSeconds(2.1)
                .recoil(0.26).accuracy(0.87).spreadDegrees(2.2).durability(1000)
                .rangeBlocks(40).headshotMultiplier(1.9).bulletVelocity(3.7)
                .armorPen(0.20).fireModes(AUTO, BURST, SEMI).build());

        // 8. Nerf Gun — toy: very low damage, harmless fun
        register("nerf_gun", WeaponStats.builder(PISTOL)
                .damage(2).fireRateRpm(300).magSize(12).reloadSeconds(1.3)
                .recoil(0.08).accuracy(0.80).spreadDegrees(3.0).durability(400)
                .rangeBlocks(18).critMultiplier(1.2).headshotMultiplier(1.3)
                .bulletVelocity(2.4).armorPen(0.0).fireModes(SEMI, AUTO).build());

        // 9. Sniper — long-range one-shot threat
        register("sniper", WeaponStats.builder(SNIPER)
                .damage(28).fireRateRpm(40).magSize(5).reloadSeconds(2.8)
                .recoil(0.70).accuracy(0.98).spreadDegrees(0.3).durability(700)
                .rangeBlocks(90).critMultiplier(2.2).headshotMultiplier(3.0)
                .bulletVelocity(5.6).armorPen(0.50).fireModes(SEMI).build());

        // 10. Shotgun — pump, big close burst
        register("shotgun", WeaponStats.builder(SHOTGUN)
                .damage(4).pellets(8).fireRateRpm(70).magSize(6).reloadSeconds(2.8)
                .recoil(0.60).accuracy(0.70).spreadDegrees(9.0).durability(800)
                .rangeBlocks(12).headshotMultiplier(1.4).bulletVelocity(2.4)
                .armorPen(0.05).fireModes(SEMI).build());

        // 11. Shotgun Delta — automatic, tighter spread
        register("shotgun_delta", WeaponStats.builder(SHOTGUN)
                .damage(3.5).pellets(8).fireRateRpm(180).magSize(10).reloadSeconds(2.4)
                .recoil(0.45).accuracy(0.74).spreadDegrees(7.5).durability(900)
                .rangeBlocks(13).headshotMultiplier(1.4).bulletVelocity(2.5)
                .armorPen(0.07).fireModes(AUTO, SEMI).build());

        // 12. MP5-SD — suppressed SMG, controllable
        register("mp5_sd", WeaponStats.builder(SMG)
                .damage(5.5).fireRateRpm(800).magSize(30).reloadSeconds(1.9)
                .recoil(0.20).accuracy(0.86).spreadDegrees(2.4).durability(900)
                .rangeBlocks(28).headshotMultiplier(1.6).bulletVelocity(3.1)
                .armorPen(0.12).fireModes(AUTO, BURST, SEMI).build());

        // 13. Minigun — spin-up suppression, huge belt
        register("minigun", WeaponStats.builder(HEAVY)
                .damage(6).fireRateRpm(1400).magSize(200).reloadSeconds(6.0)
                .recoil(0.50).accuracy(0.72).spreadDegrees(4.0).durability(1600)
                .rangeBlocks(40).critMultiplier(1.3).headshotMultiplier(1.5)
                .bulletVelocity(3.4).armorPen(0.20).fireModes(AUTO).build());

        // 14. Future Gun — high-tech energy rifle (energy fire sound)
        register("future_gun", WeaponStats.builder(HEAVY)
                .damage(12).fireRateRpm(450).magSize(30).reloadSeconds(2.5)
                .recoil(0.22).accuracy(0.94).spreadDegrees(1.0).durability(1300)
                .rangeBlocks(70).critMultiplier(1.8).headshotMultiplier(2.2)
                .bulletVelocity(8.0).armorPen(0.60).fireSound("fire_energy")
                .fireModes(AUTO, SEMI).build());

        // 15. Gravity Gun — hits fling/pull entities (energy fire sound)
        register("gravity_gun", WeaponStats.builder(HEAVY)
                .damage(2).fireRateRpm(90).magSize(8).reloadSeconds(3.0)
                .recoil(0.30).accuracy(0.95).spreadDegrees(0.8).durability(1000)
                .rangeBlocks(40).critMultiplier(1.0).headshotMultiplier(1.0)
                .bulletVelocity(6.0).armorPen(0.0).gravity(true).fireSound("fire_energy")
                .fireModes(SEMI).build());
    }

    public static int count() { return ALL.size(); } // 15
}
