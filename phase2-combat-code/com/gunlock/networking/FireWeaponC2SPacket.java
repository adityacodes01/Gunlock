package com.gunlock.networking;

/**
 * Client → server fire intent. Carries only which hand the gun is in.
 * The server reads the actual held item itself and validates everything
 * (cooldown, ammo, reload state); this flag is the sole client-supplied
 * value and is harmless if spoofed.
 *
 * SEAM(26.2): implement the MDK's payload interface (id + stream codec).
 *   encode: buf.writeBoolean(offhand)
 *   decode: new FireWeaponC2SPacket(buf.readBoolean())
 */
public record FireWeaponC2SPacket(boolean offhand) {
    public static final String ID = "gunlock:fire";
}
