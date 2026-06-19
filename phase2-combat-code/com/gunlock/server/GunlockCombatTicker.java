package com.gunlock.server;

import com.gunlock.weapons.GunItem;
import com.gunlock.weapons.WeaponStats;

/**
 * Runs once per server tick per online player. Two jobs:
 *   1. pace out queued BURST shots at the weapon's cooldown
 *   2. complete a reload when its timer elapses
 *
 * Hook {@link #onPlayerTick} into the server player-tick event and
 * {@link #onLogout} into the disconnect event (SEAM(26.2): both are
 * standard Forge events; names are stable but confirm against the MDK).
 */
public final class GunlockCombatTicker {
    private GunlockCombatTicker() {}

    public static void onPlayerTick(Object player) {
        long now = gameTime(player);                        // SEAM(26.2)
        ServerGunState st = ServerGunState.of(playerId(player)); // SEAM(26.2)

        // 1. reload completion
        if (st.reloadEndTick >= 0 && now >= st.reloadEndTick) {
            WeaponFireService.completeReload(player, st);
        }

        // 2. burst pacing
        if (st.burstRemaining > 0 && now >= st.nextBurstTick) {
            Object stack = heldStack(player);               // SEAM(26.2)
            if (itemOf(stack) instanceof GunItem gun) {
                WeaponStats s = gun.effectiveStats();
                WeaponFireService.fireOneShot(player, gun, stack, s, st, now);
            } else {
                st.burstRemaining = 0; // weapon swapped mid-burst → drop it
            }
        }
    }

    public static void onLogout(Object player) {
        ServerGunState.clear(playerId(player));             // SEAM(26.2)
    }

    private static long gameTime(Object player) { throw seam(); }
    private static java.util.UUID playerId(Object player) { throw seam(); }
    private static Object heldStack(Object player) { throw seam(); }
    private static Object itemOf(Object stack) { throw seam(); }
    private static RuntimeException seam() {
        return new UnsupportedOperationException("SEAM(26.2): wire to MDK");
    }
}
