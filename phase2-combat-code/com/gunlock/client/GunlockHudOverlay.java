package com.gunlock.client;

import com.gunlock.weapons.GunItem;
import com.gunlock.weapons.WeaponStats;

/**
 * Bottom-right weapon HUD + crosshair + hit marker + reload bar. The layout
 * math and the values to draw are all here; only the actual draw calls
 * (text, rect, blit) and the "read screen size / held item" lookups are
 * SEAM(26.2) leaf calls.
 *
 * Layout (anchored bottom-right, x/y in pixels from that corner):
 *     ASSAULT RIFLE          name      (right-aligned)
 *        28 / 30             mag        (large)
 *     164 RESERVE            reserve    (small, dim)
 *         AUTO               fire mode  (small)
 */
public final class GunlockHudOverlay {
    private GunlockHudOverlay() {}

    // transient client feedback timers (ticks), set by incoming S2C packets
    public static int hitMarkerTicks = 0;
    public static float reloadProgress = -1f; // 0..1 while reloading, else <0
    public static float currentSpreadPx = 4f; // crosshair gap, grows with bloom

    public static void render(Object gfx, int screenW, int screenH, float partialTick) {
        Object stack = heldStack();                         // SEAM(26.2)
        boolean holdingGun = itemOf(stack) instanceof GunItem;

        drawCrosshair(gfx, screenW / 2, screenH / 2, currentSpreadPx); // SEAM(26.2) draw
        if (hitMarkerTicks > 0) {
            drawHitMarker(gfx, screenW / 2, screenH / 2);   // SEAM(26.2) draw
        }

        if (holdingGun) {
            GunItem gun = (GunItem) itemOf(stack);
            WeaponStats s = gun.effectiveStats();
            int ammo = gun.getAmmo(stack);
            int reserve = reserveCount();                   // SEAM(26.2): inventory bullets
            String name = weaponDisplayName(gun);           // SEAM(26.2): I18n of lang key
            String mode = gun.getFireMode(stack).hudLabel();

            int rightX = screenW - 12;
            int baseY  = screenH - 64;
            drawTextRight(gfx, name,                      rightX, baseY);      // SEAM(26.2)
            drawTextRightLarge(gfx, ammo + " / " + s.magSize(), rightX, baseY + 12); // SEAM
            drawTextRightDim(gfx, reserve + " RESERVE",   rightX, baseY + 30);  // SEAM(26.2)
            drawTextRight(gfx, mode,                      rightX, baseY + 42);  // SEAM(26.2)

            if (reloadProgress >= 0f) {
                drawReloadBar(gfx, screenW / 2 - 40, screenH / 2 + 20, 80, 4, reloadProgress); // SEAM
            }
        }
    }

    /** Called each client tick to age out transient feedback. */
    public static void tick() {
        if (hitMarkerTicks > 0) hitMarkerTicks--;
    }

    // SEAMS — leaf engine calls only
    private static Object heldStack() { throw seam(); }
    private static Object itemOf(Object stack) { throw seam(); }
    private static int reserveCount() { throw seam(); }
    private static String weaponDisplayName(GunItem gun) { throw seam(); }
    private static void drawCrosshair(Object gfx, int cx, int cy, float gap) { throw seam(); }
    private static void drawHitMarker(Object gfx, int cx, int cy) { throw seam(); }
    private static void drawReloadBar(Object gfx, int x, int y, int w, int h, float p) { throw seam(); }
    private static void drawTextRight(Object gfx, String t, int x, int y) { throw seam(); }
    private static void drawTextRightLarge(Object gfx, String t, int x, int y) { throw seam(); }
    private static void drawTextRightDim(Object gfx, String t, int x, int y) { throw seam(); }
    private static RuntimeException seam() {
        return new UnsupportedOperationException("SEAM(26.2): wire to MDK");
    }
}
