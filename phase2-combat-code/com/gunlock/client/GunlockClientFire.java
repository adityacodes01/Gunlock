package com.gunlock.client;

import com.gunlock.weapons.FireMode;
import com.gunlock.weapons.GunItem;
import com.gunlock.weapons.WeaponStats;

/**
 * Client-side trigger handling. Runs each client tick. Decides WHEN to send
 * a fire packet based on the selected fire mode, and plays the immediate
 * feel (recoil kick, muzzle flash, fire sound) so the gun feels responsive
 * without waiting for the server round-trip. The server still owns the
 * outcome — this is prediction, not authority.
 *
 * <p>Cooldown here mirrors the server's so the client doesn't spam packets
 * the server would only reject; the two stay in lockstep.
 */
public final class GunlockClientFire {
    private GunlockClientFire() {}

    private static long clientNextFireTick = 0;
    private static boolean triggerWasDown = false; // for SEMI edge detection

    /** Call every client tick. {@code triggerDown} = attack button held. */
    public static void clientTick(boolean triggerDown) {
        Object stack = heldStack();                         // SEAM(26.2)
        if (!(itemOf(stack) instanceof GunItem gun)) {
            triggerWasDown = triggerDown;
            return;
        }
        WeaponStats s = gun.effectiveStats();
        long now = clientTime();                            // SEAM(26.2)

        boolean justPressed = triggerDown && !triggerWasDown;
        boolean canFire = now >= clientNextFireTick && gun.getAmmo(stack) > 0;

        FireMode mode = gun.getFireMode(stack);
        boolean wantShot = switch (mode) {
            case SEMI  -> justPressed;            // one per click
            case BURST -> justPressed;            // server paces the rest
            case AUTO  -> triggerDown;            // continuous while held
        };

        if (wantShot && canFire) {
            sendFirePacket();                               // SEAM(26.2): C2S
            predictShot(s);                                 // recoil + muzzle flash + sound
            clientNextFireTick = now + s.cooldownTicks();
        } else if (triggerDown && gun.getAmmo(stack) <= 0 && justPressed) {
            playEmptyClick();                               // SEAM(26.2)
        }

        triggerWasDown = triggerDown;
    }

    /** Reload key (default R). */
    public static void onReloadKey() { sendReloadPacket(); }      // SEAM(26.2)

    /** Fire-mode key (default B). */
    public static void onFireModeKey() { sendModeSwitchPacket(); } // SEAM(26.2)

    /** Applies the immediate visual/audio kick for a predicted shot. */
    private static void predictShot(WeaponStats s) {
        applyRecoil(s.recoil());                            // SEAM(26.2): camera pitch kick
        spawnMuzzleFlash();                                 // SEAM(26.2): particle
        playFireSound(s);                                   // SEAM(26.2)
    }

    // SEAMS
    private static Object heldStack() { throw seam(); }
    private static Object itemOf(Object stack) { throw seam(); }
    private static long clientTime() { throw seam(); }
    private static void sendFirePacket() { throw seam(); }
    private static void sendReloadPacket() { throw seam(); }
    private static void sendModeSwitchPacket() { throw seam(); }
    private static void applyRecoil(double amount) { throw seam(); }
    private static void spawnMuzzleFlash() { throw seam(); }
    private static void playFireSound(WeaponStats s) { throw seam(); }
    private static void playEmptyClick() { throw seam(); }
    private static RuntimeException seam() {
        return new UnsupportedOperationException("SEAM(26.2): wire to MDK");
    }
}
