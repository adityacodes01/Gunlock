package com.gunlock.networking;

/**
 * Network layer. Three client→server packets carry player INTENT; the
 * server validates and is the sole authority over ammo, damage, and hit
 * registration. One server→client packet broadcasts cheap visual effects
 * (tracer, muzzle flash, hit marker) so we never sync a bullet entity.
 *
 * <p>Validation the server performs on a FIRE packet, in order:
 * <ol>
 *   <li>the held item really is a {@code GunItem}</li>
 *   <li>the per-stack fire cooldown has elapsed (rate-limit; rejects
 *       rapid-fire packet spam — the core anti-cheat lever)</li>
 *   <li>magazine ammo &gt; 0</li>
 *   <li>the player isn't reloading</li>
 * </ol>
 * Only then does it decrement ammo, spawn the server-side
 * {@code BulletEntity}, and broadcast the tracer packet.
 *
 * <p>TODO(26.2): pick the transport. Recent Forge/NeoForge moved from
 * {@code SimpleChannel} to a payload-registrar API. The handler logic
 * below is transport-agnostic; only the registration in {@link #register}
 * changes. Do NOT trust any field in these packets — treat them as a
 * button-press, nothing more.
 */
public final class GunlockNetwork {
    private GunlockNetwork() {}

    public static final String PROTOCOL = "1";

    public static void register() {
        // TODO(26.2): register FireWeaponC2SPacket, ReloadC2SPacket,
        // SwitchFireModeC2SPacket, and TracerS2CPacket here.
    }

    /**
     * Server-side handling of a fire request. Pseudocode kept explicit so
     * the security-relevant ordering is obvious.
     *
     * <pre>
     * void onFire(ServerPlayer player) {
     *     ItemStack stack = player.getMainHandItem();
     *     if (!(stack.getItem() instanceof GunItem gun)) return;     // (1)
     *     WeaponStats s = gun.effectiveStats();
     *     if (!cooldownElapsed(player, s.cooldownTicks())) return;     // (2)
     *     if (gun.getAmmo(stack) <= 0) { playEmptyClick(player); return; } // (3)
     *     if (isReloading(player)) return;                            // (4)
     *
     *     int pellets = s.pellets();
     *     gun.setAmmo(stack, gun.getAmmo(stack) - 1);
     *     for (int i = 0; i < pellets; i++) spawnBullet(player, s);   // server-auth
     *     broadcastTracer(player, s);                                  // S2C visual
     *     stampCooldown(player);
     * }
     * </pre>
     */
    public static void onFireServer(/* ServerPlayer player */) {
        // Implemented against the 26.2 server API.
    }
}
