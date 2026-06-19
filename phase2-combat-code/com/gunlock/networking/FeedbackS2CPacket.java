package com.gunlock.networking;

/**
 * Server → client cosmetic feedback. One packet covers every cheap visual
 * so we never sync a bullet entity. {@code kind} selects the effect; the
 * floats carry whatever that effect needs (e.g. tracer end-point, reload
 * duration). Purely cosmetic — losing one drops a particle, nothing more.
 *
 * SEAM(26.2): implement the MDK payload interface; handle on the client
 * thread (enqueueWork) and route by kind into GunlockHudOverlay /
 * particle spawns / sound plays.
 */
public record FeedbackS2CPacket(Kind kind, float a, float b, float c, float d) {
    public static final String ID = "gunlock:feedback";

    public enum Kind {
        TRACER,        // a..c = direction; client draws a trail from the shooter
        HIT_MARKER,    // flash the crosshair hit marker
        EMPTY_CLICK,   // play the empty-magazine click
        RELOAD_START,  // a = duration ticks; client starts the progress bar
        MODE_SWITCH    // a = ordinal of new FireMode; update HUD + click
    }
}
