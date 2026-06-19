package com.gunlock.networking;

/**
 * Client → server fire-mode cycle intent (default key B). The server cycles
 * to the next mode the weapon actually allows and echoes it back for the HUD.
 *
 * SEAM(26.2): implement the MDK payload interface.
 *   encode: buf.writeBoolean(offhand) / decode: read it back
 */
public record SwitchFireModeC2SPacket(boolean offhand) {
    public static final String ID = "gunlock:switch_mode";
}
