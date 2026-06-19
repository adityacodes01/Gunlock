package com.gunlock.networking;

/**
 * Client → server reload intent (default key R). The server checks the mag
 * isn't already full and that the player has bullets in reserve before
 * starting the reload timer.
 *
 * SEAM(26.2): implement the MDK payload interface.
 *   encode: buf.writeBoolean(offhand) / decode: read it back
 */
public record ReloadC2SPacket(boolean offhand) {
    public static final String ID = "gunlock:reload";
}
