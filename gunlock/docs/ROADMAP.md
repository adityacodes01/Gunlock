# Roadmap & Honest Scope

The original brief asks for a "production-quality, commercial-grade,
fully-tested" mod with custom art, sounds, and animations, validated with
50+ simultaneous players. That is genuinely months of work plus art and
audio assets that a code generator cannot produce. This document is the
honest split so nothing is a surprise.

## ✅ Done (real, usable now)

- **Weapon system**: `WeaponClass`, `FireMode`, `WeaponStats` (with a
  defaulted builder), and `Weapons` — all 26 firearms with a coherent,
  distinct balance pass. This is the tedious part and it's finished.
- **Data-driven registry**: add a weapon to `Weapons.ALL` and it
  auto-registers as an item with a lang entry. No per-gun boilerplate.
- **Gun logic**: magazine state, fire-mode cycling, durability hookup.
- **Universal Bullet** + crafting recipe + reserve-from-inventory model.
- **Combat math** in `BulletEntity`: range falloff, headshot detection,
  crits, armour penetration, bullet drop vs. flat (railgun).
- **Config funnel**: one `apply()` path so server multipliers reach every
  consumer consistently.
- **Server-authority design**: the fire/validate/spawn ordering is
  written out as the security contract.

## ✅ Vertical slice logic (NEW — the fire-to-damage-to-HUD flow)

The full firing loop is now written as real control flow, not stubs. Only
the leaf engine calls are left as `SEAM(26.2)` one-liners:

- `util/AimMath` — spread-cone sampling, **unit-verified** (deviation never
  exceeds the cone; uniform over the cap; zero spread = zero deviation).
- `server/WeaponFireService` — the authority: fire validation order
  (reload → cooldown → ammo), ammo spend, per-pellet spread, burst queueing,
  reload start/complete with reserve consumption, mode cycling.
- `server/ServerGunState` — per-player cooldown/reload/burst gating.
- `server/GunlockCombatTicker` — paces bursts, completes reloads each tick.
- `networking/*` — three C2S intent packets + one S2C feedback packet, with
  the server handlers wired to the service (trust line documented).
- `client/GunlockClientFire` — SEMI/BURST/AUTO trigger logic + client-side
  prediction (recoil/flash/sound) kept in lockstep with the server cooldown.
- `client/GunlockHudOverlay` — full HUD layout + draw logic (name, ammo,
  reserve, mode, crosshair, hit marker, reload bar).

What "SEAM(26.2)" means: a single engine call (read held item, spawn entity,
count inventory, send packet bytes, draw text) whose signature comes from the
26.2 MDK. Filling them is mechanical and changes none of the logic above.

## ✅ Real vanilla code (NEW — converted from placeholders)

`GunItem` and `BulletEntity` no longer use placeholders. They now call
Minecraft's own APIs directly — `CUSTOM_DATA` for ammo/fire-mode state,
`ClipContext` + `ProjectileUtil` raycasting, `damageSources().mobProjectile`
for attributed damage, and `setDeltaMovement` for the Gravity Gun pull.
These are *vanilla* (not Forge) APIs, stable across 1.21.x/26.x, so they
have a high chance of compiling as-is.

What's still Forge-glue (flagged, version-sensitive — this is where Forge
26.2 being 3 days old bites): entity-type / payload registration, the
`@Mod` constructor, and GUI-layer registration. Untested; expect small
fixes against the real 26.2 MDK.

## 🟨 Scaffolded (structure + documented logic; needs engine wiring)

Each has a clear `TODO(26.2)` where the engine API plugs in:

- Item state persistence → migrate the two helpers in `GunItem` to a
  registered `DataComponentType`.
- `BulletEntity` engine seams → `ClipContext`, `DamageSource`, synched
  data signature.
- Networking transport → register the 3 C2S packets + 1 S2C tracer packet.
- Config → register the `ForgeConfigSpec` so values load from TOML.
- HUD + keybinds → register the GUI layer and key mappings; draw logic
  is specified.
- Entity / sound / particle registries → mirror `GunlockItems`.

## ✅ Assets — NOW INCLUDED AND WIRED

The roster is now the **15 supplied guns** (AK-47, Chaos Gun, Pistol, Jerry
Chine Gun, Battle Rifle, Laser Plasma Gun, Overwatch Gun, Nerf Gun, Sniper,
Shotgun, Shotgun Delta, MP5-SD, Minigun, Future Gun, Gravity Gun) plus the
Bullet. For each:

- **Texture** at `assets/gunlock/textures/item/<id>.png` (your PNGs; the
  512/1024/128 px ones were downscaled to 64 px so the item renderer doesn't
  generate absurd geometry — the originals are untouched in your uploads).
- **Model** at `assets/gunlock/models/item/<id>.json` (`item/generated`,
  pointing at that texture) — this is the wiring that makes the picture show.
- **Lang** name in `en_us.json`.

**Sounds** converted from your MP3s to mono OGG Vorbis (Minecraft can't play
MP3, and mono is required for 3D distance attenuation):
- `fire_standard.ogg` — guns 1–13
- `fire_energy.ogg` — Future Gun + Gravity Gun
- `reload.ogg` — all weapons
Declared in `sounds.json` and registered in `GunlockSounds`.

**Gravity Gun** behaviour is implemented in `BulletEntity.applyGravityWell`:
on impact it pulls nearby living entities toward the point with a slight
upward lift (a small singularity), force falling off with distance. The
vector math is complete; only the final "set entity velocity" call is a seam.

## 🟥 Still not generated (lower priority now)

- 3D in-hand/world models (the current models are flat extruded sprites,
  which is fine for inventory + a basic held look; bespoke 3D needs Blockbench).
- First-person animations (GeckoLib) — the heaviest remaining item.
- Per-gun unique sounds (currently shared standard/energy pools, which is the
  sensible approach anyway).

## Note on the roster change

The original brief described an abstract 26-gun roster. With real textures
and sounds supplied for 15 specific guns, the roster was switched to match
those assets (it's cleaner than 11 missing-texture guns). The architecture
is unchanged — add more guns anytime by dropping a texture in and adding one
entry to `Weapons.ALL`.

## Suggested build order

1. Resolve the `TODO(26.2)` / `SEAM(26.2)` markers against the MDK so it
   compiles. Textures, sounds, and the firing logic are already in.
2. Confirm one gun fires end to end (AK-47): client packet → server validate
   → bullet → damage → HUD + fire sound. The data already covers all 15.
3. Verify reload (with the reload sound + progress bar) and fire-mode switch.
4. Polish the Gravity Gun pull, then particles/tracers.
5. 3D models + first-person animations (GeckoLib) — the heaviest, last.

## A note on the loader

You asked for Forge specifically, and Forge 26.2 exists, so that's what
this targets. If you later care about the larger modern content-mod
ecosystem, **NeoForge** is the Forge-family loader most new mods build on
in 2026. Porting is mechanical (the same concepts, renamed APIs), and the
data-driven core here ports cleanly. Say the word and I'll retarget.
