package com.gunlock.weapons;

/**
 * Fire modes a weapon can support. A weapon declares the set of modes
 * it allows (see {@link WeaponStats#fireModes}); the player cycles
 * through that set with the mode-switch key (default B).
 */
public enum FireMode {
    /** One shot per trigger pull. */
    SEMI("Semi"),
    /** A fixed-count burst (see {@link WeaponStats#burstCount}) per trigger pull. */
    BURST("Burst"),
    /** Continuous fire while the trigger is held. */
    AUTO("Auto");

    private final String hudLabel;

    FireMode(String hudLabel) {
        this.hudLabel = hudLabel;
    }

    /** Short label shown on the HUD, e.g. "AUTO". */
    public String hudLabel() {
        return hudLabel.toUpperCase();
    }
}
