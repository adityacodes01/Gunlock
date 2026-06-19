# Developer Guide

## Architecture in one breath

Everything radiates from **one data map**: `weapons/Weapons.ALL`
(`id -> WeaponStats`). The registry builds items from it, the HUD reads
stats from it, combat reads config-resolved stats from it. Add or rebalance
a gun by editing that file and nothing else.

```
Weapons.ALL ──► GunlockItems (registers 1 item per entry)
     │                 │
     │                 └─► GunItem (per-stack ammo + fire mode)
     │
     └──► GunItem.effectiveStats()
                 └─► GunlockConfig.apply()  (runtime multipliers)
                         └─► BulletEntity.configureFrom()  (server combat)
```

## Package layout

```
com.gunlock
├── Gunlock.java            mod entry; wires registries + bus listeners
├── weapons/                WeaponClass, FireMode, WeaponStats, Weapons, GunItem
├── items/                  AmmoItem (the universal Bullet)
├── entities/               BulletEntity (server-only projectile + combat)
├── registry/               GunlockItems (+ add Entities/Sounds/Particles)
├── networking/             GunlockNetwork (server-authority packet design)
├── config/                 GunlockConfig (no-recompile balancing)
├── client/                 GunlockHudOverlay, keybinds (client-only)
└── util/                   shared helpers
```

## The server-authority rule (read before touching combat)

The client may *predict* feel — muzzle flash, recoil, the sound, a tracer.
The client never decides ammo, hits, or damage. The fire packet is a
button-press; `GunlockNetwork.onFireServer` validates (item is a gun →
cooldown elapsed → ammo > 0 → not reloading) and only then mutates state
and spawns the bullet. This ordering is the anti-cheat. Keep new features
on the same side of that line.

## Why bullets are server-only

A naive gun mod spawns a synced projectile entity per shot; at 60 players
firing autos that's thousands of tracked entities and the TPS collapses.
Here the bullet lives only on the logical server and a single cheap S2C
packet tells nearby clients to draw a tracer particle. That's the design
lever for the scale target.

## Adding a weapon

```java
register("nail_gun", WeaponStats.builder(PISTOL)
        .damage(5).fireRateRpm(450).magSize(20).reloadSeconds(1.6)
        .fireModes(SEMI, AUTO).build());
```
Then add a texture, a model JSON, and a lang entry (the en_us.json is
generated from the same id list — regenerate or add `item.gunlock.nail_gun`).

## Stats reference (units)

See the doc comment on `WeaponStats` — damage is in half-hearts, fire rate
in RPM (converted to a tick cooldown), velocity in blocks/tick, accuracy
0–1, spread in degrees, armour pen 0–1.
