package com.gunlock.networking;

import com.gunlock.server.WeaponFireService;

/**
 * Network layer. Three client→server intent packets and one server→client
 * feedback packet. The server-side handlers below are the real dispatch
 * logic; they hand straight to {@link WeaponFireService}, which is the
 * authority. The only SEAM is the transport registration in {@link #register}
 * — every recent Forge/NeoForge line ships a payload registrar; the handler
 * bodies don't change with it.
 *
 * Packets (all tiny — intent only, never trusted state):
 *   C2S FireWeaponC2SPacket(offhand)      -> onFire
 *   C2S ReloadC2SPacket(offhand)          -> onReload
 *   C2S SwitchFireModeC2SPacket(offhand)  -> onSwitchMode
 *   S2C FeedbackS2CPacket(kind, data)     -> HUD/tracer/sound on the client
 */
public final class GunlockNetwork {
    private GunlockNetwork() {}

    public static final String PROTOCOL = "1";

    public static void register() {
        // SEAM(26.2): register the four payload types and bind:
        //   FireWeaponC2SPacket      -> GunlockNetwork::onFire        (server thread)
        //   ReloadC2SPacket          -> GunlockNetwork::onReload      (server thread)
        //   SwitchFireModeC2SPacket  -> GunlockNetwork::onSwitchMode  (server thread)
        //   FeedbackS2CPacket        -> client handler (HUD/tracer)
        // Always run C2S handlers on the server thread (enqueueWork) so the
        // service mutates world state safely.
    }

    // --- server handlers: validate-by-delegation -------------------------
    // `player` is the ServerPlayer the packet arrived from (provided by the
    // transport's context, NOT read from the packet — that's the trust line).

    public static void onFire(Object player, FireWeaponC2SPacket pkt) {
        WeaponFireService.onFireRequest(player, pkt.offhand());
    }

    public static void onReload(Object player, ReloadC2SPacket pkt) {
        WeaponFireService.onReloadRequest(player, pkt.offhand());
    }

    public static void onSwitchMode(Object player, SwitchFireModeC2SPacket pkt) {
        WeaponFireService.onSwitchFireMode(player, pkt.offhand());
    }
}
