package com.gunlock.server;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transient, server-only combat state per player: fire cooldown, reload
 * timer, and pending burst shots. This is NOT saved to disk — it is purely
 * runtime gating, which is exactly what the anti-cheat needs.
 *
 * <p>Kept in a {@link ConcurrentHashMap} keyed by player UUID to stay
 * engine-API-independent. The "proper" modern home is a per-entity
 * attachment, but a map is correct and simpler for the slice. Clear a
 * player's entry on logout (SEAM: hook the disconnect event in
 * {@code GunlockCombatTicker}).
 */
public final class ServerGunState {
    private static final Map<UUID, ServerGunState> STATES = new ConcurrentHashMap<>();

    /** Game tick before which the player may not fire again. */
    public long nextFireTick = 0;

    /** -1 when not reloading, otherwise the tick the reload completes. */
    public long reloadEndTick = -1;

    /** Remaining shots queued by a BURST trigger pull. */
    public int burstRemaining = 0;

    /** Next tick a queued burst shot is allowed (paces the burst). */
    public long nextBurstTick = 0;

    public static ServerGunState of(UUID playerId) {
        return STATES.computeIfAbsent(playerId, k -> new ServerGunState());
    }

    public static void clear(UUID playerId) {
        STATES.remove(playerId);
    }

    public boolean isReloading(long now) {
        return reloadEndTick >= 0 && now < reloadEndTick;
    }

    /** Cancels an in-progress reload (e.g. player fired or swapped weapons). */
    public void cancelReload() {
        reloadEndTick = -1;
    }
}
