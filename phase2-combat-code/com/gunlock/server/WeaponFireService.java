package com.gunlock.server;

import com.gunlock.entities.BulletEntity;
import com.gunlock.util.AimMath;
import com.gunlock.weapons.FireMode;
import com.gunlock.weapons.GunItem;
import com.gunlock.weapons.WeaponStats;

/**
 * The authority. Every shot, reload, and mode switch resolves here on the
 * logical server. Clients only ever send intent (see the C2S packets);
 * this class decides what actually happens. Nothing below trusts a value
 * the client supplied beyond "which hand" and "I pressed the button".
 *
 * <p>The method bodies are the real control flow. The handful of
 * {@code // SEAM(26.2)} lines are leaf engine calls (read held item, spawn
 * entity, count inventory items, broadcast a packet) — each one is a
 * single call whose exact signature comes from the 26.2 MDK. Wiring them
 * does not change any logic here.
 *
 * <p>Types written as {@code Object player} are the server player handle
 * (ServerPlayer). Left as Object so this file does not depend on an import
 * whose package may have shifted; replace with the real type when wiring.
 */
public final class WeaponFireService {
    private WeaponFireService() {}

    /** Extra cooldown after a dry trigger pull so an empty gun can't spam clicks. */
    private static final int EMPTY_RECLICK_TICKS = 6;

    // === FIRE ============================================================

    /**
     * Handle a single fire request. For AUTO the client sends one of these
     * per client tick while the trigger is held; the cooldown gate below is
     * what actually enforces the fire rate, so packet spam cannot exceed it.
     * For BURST, one request queues {@link WeaponStats#burstCount} shots that
     * the {@link GunlockCombatTicker} paces out.
     */
    public static void onFireRequest(Object player, boolean offhand) {
        long now = gameTime(player);
        ServerGunState st = ServerGunState.of(playerId(player));

        Object stack = heldStack(player, offhand);          // SEAM(26.2)
        if (!(itemOf(stack) instanceof GunItem gun)) return;
        WeaponStats s = gun.effectiveStats();

        if (st.isReloading(now)) return;                    // can't fire mid-reload
        if (now < st.nextFireTick) return;                  // rate-limit (anti-cheat)

        FireMode mode = gun.getFireMode(stack);
        if (mode == FireMode.BURST) {
            // Queue the burst; first shot fires now, the rest are paced by the ticker.
            st.burstRemaining = Math.max(st.burstRemaining, s.burstCount());
        }
        fireOneShot(player, gun, stack, s, st, now);
    }

    /**
     * Fires exactly one shot: spend ammo, spawn pellet(s), pace the next
     * shot, broadcast the tracer. Shared by single fire and burst pacing.
     */
    static void fireOneShot(Object player, GunItem gun, Object stack,
                            WeaponStats s, ServerGunState st, long now) {
        int ammo = gun.getAmmo(stack);
        if (ammo <= 0) {
            broadcastEmptyClick(player);                    // SEAM(26.2)
            st.nextFireTick = now + EMPTY_RECLICK_TICKS;
            st.burstRemaining = 0;
            return;
        }

        gun.setAmmo(stack, ammo - 1);

        AimMath.Dir aim = lookDir(player);                  // SEAM(26.2)
        var rng = random(player);                           // SEAM(26.2)
        double cone = s.spreadDegrees();
        for (int i = 0; i < s.pellets(); i++) {
            AimMath.Dir dir = AimMath.applySpread(aim, cone, rng);
            spawnBullet(player, s, dir);                    // SEAM(26.2)
        }

        st.nextFireTick = now + s.cooldownTicks();
        if (st.burstRemaining > 0) {
            st.burstRemaining--;
            st.nextBurstTick = now + s.cooldownTicks();
        }

        broadcastTracer(player, s, aim);                    // SEAM(26.2)
        damageWeapon(stack, player);                        // SEAM(26.2) durability
    }

    // === RELOAD ==========================================================

    public static void onReloadRequest(Object player, boolean offhand) {
        long now = gameTime(player);
        ServerGunState st = ServerGunState.of(playerId(player));
        if (st.isReloading(now)) return;

        Object stack = heldStack(player, offhand);          // SEAM(26.2)
        if (!(itemOf(stack) instanceof GunItem gun)) return;
        WeaponStats s = gun.effectiveStats();

        if (gun.getAmmo(stack) >= s.magSize()) return;      // already full
        if (reserveAmmo(player) <= 0) return;               // SEAM(26.2): no bullets

        st.reloadEndTick = now + s.reloadTicks();
        broadcastReloadStart(player, s.reloadTicks());      // SEAM(26.2): client bar+sound
    }

    /** Called by the ticker when a reload timer elapses. */
    static void completeReload(Object player, ServerGunState st) {
        st.reloadEndTick = -1;
        Object stack = heldStack(player, false);            // SEAM(26.2)
        if (!(itemOf(stack) instanceof GunItem gun)) return;
        WeaponStats s = gun.effectiveStats();

        int need = s.magSize() - gun.getAmmo(stack);
        if (need <= 0) return;
        int available = reserveAmmo(player);                // SEAM(26.2)
        int load = Math.min(need, available);
        if (load <= 0) return;

        consumeReserve(player, load);                       // SEAM(26.2): remove bullets
        gun.setAmmo(stack, gun.getAmmo(stack) + load);      // tactical reload = refill to what's available
    }

    // === MODE SWITCH =====================================================

    public static void onSwitchFireMode(Object player, boolean offhand) {
        Object stack = heldStack(player, offhand);          // SEAM(26.2)
        if (!(itemOf(stack) instanceof GunItem gun)) return;
        FireMode now = gun.cycleFireMode(stack);
        broadcastModeSwitch(player, now);                   // SEAM(26.2): HUD + click sound
    }

    // === SEAMS (single engine calls; wire to the 26.2 MDK) ===============
    // Each returns/does exactly what its name says. Bodies intentionally
    // omitted so the logic above reads cleanly; fill against the MDK.

    private static long gameTime(Object player) { throw seam(); }
    private static java.util.UUID playerId(Object player) { throw seam(); }
    private static Object heldStack(Object player, boolean offhand) { throw seam(); }
    private static Object itemOf(Object stack) { throw seam(); }
    private static AimMath.Dir lookDir(Object player) { throw seam(); }
    private static java.util.random.RandomGenerator random(Object player) { throw seam(); }
    private static int reserveAmmo(Object player) { throw seam(); }       // count gunlock:bullet in inventory
    private static void consumeReserve(Object player, int n) { throw seam(); }
    private static void spawnBullet(Object player, WeaponStats s, AimMath.Dir dir) { throw seam(); }
    private static void damageWeapon(Object stack, Object player) { throw seam(); }
    private static void broadcastTracer(Object player, WeaponStats s, AimMath.Dir aim) { throw seam(); }
    private static void broadcastEmptyClick(Object player) { throw seam(); }
    private static void broadcastReloadStart(Object player, int ticks) { throw seam(); }
    private static void broadcastModeSwitch(Object player, FireMode mode) { throw seam(); }

    private static RuntimeException seam() {
        return new UnsupportedOperationException("SEAM(26.2): wire to MDK");
    }
}
