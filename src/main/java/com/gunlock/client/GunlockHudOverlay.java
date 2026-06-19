package com.gunlock.client;

/**
 * Client-only systems: keybinds and the HUD. Everything in this package
 * must be guarded so it never loads on a dedicated server.
 *
 * <p>Keybinds (defaults from the spec):
 * <ul>
 *   <li><b>R</b> — reload (sends ReloadC2SPacket; client shows progress bar)</li>
 *   <li><b>B</b> — cycle fire mode (sends SwitchFireModeC2SPacket)</li>
 *   <li>left mouse — fire; AUTO is driven by the client tick loop while held,
 *       each shot gated by the same cooldown the server enforces, so the
 *       prediction matches the authority</li>
 * </ul>
 *
 * <p>HUD layout (bottom-right), drawn from the held {@code GunItem}'s
 * effective stats and per-stack state:
 * <pre>
 *        ASSAULT RIFLE
 *           28 / 30
 *        164 RESERVE          (= count of bullets in inventory)
 *            AUTO             (current fire mode)
 * </pre>
 * Plus: centered crosshair whose gap scales with current spread, a hit
 * marker on confirmed hits (driven by the S2C tracer/hit packet), a
 * reload progress bar, and a damage-direction indicator.
 *
 * <p>TODO(26.2): the overlay registration API changed across the
 * 1.20→1.21 line ({@code RegisterGuiOverlaysEvent} →
 * {@code RegisterGuiLayersEvent}). Register the layer in client setup;
 * the drawing math is version-independent.
 */
public final class GunlockHudOverlay {
    private GunlockHudOverlay() {}

    public static void renderHud(/* GuiGraphics g, float partialTick */) {
        // 1. resolve held GunItem (else return)
        // 2. name  = I18n of the weapon's lang key
        // 3. ammo  = gun.getAmmo(stack) + " / " + stats.magSize()
        // 4. reserve = player's bullet count in inventory
        // 5. mode  = gun.getFireMode(stack).hudLabel()
        // 6. draw crosshair with gap = f(currentSpread)
        // 7. if reloading: draw progress bar (0..1)
        // 8. if hitMarkerTimer > 0: draw hit marker
    }
}
