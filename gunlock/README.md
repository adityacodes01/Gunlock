# Gunlock

A premium pixel-art firearms system for **Minecraft Java Edition 26.2**
("Chaos Cubed"), built to stay inside the vanilla aesthetic. Universal
bullet, 26 firearms, server-authoritative combat, attachments, fire modes,
and a no-recompile balance config.

> **Status: foundation (v0.1.0).** This repository is a real, structured
> starting point — not a finished commercial mod. Read
> [`docs/ROADMAP.md`](docs/ROADMAP.md) for exactly what is implemented,
> what is scaffolded, and what no code generator can produce for you
> (textures, 3D models, and sound files).

## What's here

| Area | State |
|------|-------|
| Project + build files (`build.gradle`, `gradle.properties`, `mods.toml`) | Drafted; **version numbers need confirming** against the 26.2 Forge MDK |
| Weapon data model + **all 26 weapons with distinct, balanced stats** | Complete |
| Gun item logic (ammo, fire mode cycling, durability) | Complete (persistence API flagged for 26.2) |
| Universal Bullet item + crafting recipe | Complete |
| Bullet entity (falloff, headshot, crit, armour pen, drop) | Combat math complete; engine seams flagged `TODO(26.2)` |
| Config (per-weapon runtime multipliers) | Funnel complete; ForgeConfigSpec wiring stubbed |
| Registry (auto-registers every weapon from the data map) | Complete |
| Networking (server-authority design) | Designed + documented; transport wiring stubbed |
| HUD, keybinds | Layout + draw logic documented; render wiring stubbed |
| Textures + item models (15 guns + bullet) | **Included & wired** (your PNGs) |
| Sounds (fire / energy-fire / reload) | **Included & wired** (MP3→OGG converted) |
| 3D models + first-person animations | Not yet — see roadmap |

## Why the "26.2" details matter

Minecraft 26.2 dropped on **June 16, 2026** and runs on **Java 25**. It's the
second release in Mojang's new `year.drop.hotfix` versioning and ships
**unobfuscated**. Forge published its first 26.2 build the next day, so it's
brand new — every `TODO(26.2)` marker in the source is a spot where a
class name or method signature should be checked against the official MDK
before the first compile. The architecture doesn't change; only the
engine-facing bindings might.

## Build

See [`docs/INSTALL.md`](docs/INSTALL.md). Short version: install the 26.2
Forge MDK, drop this `src/` and the gradle files in, fix the three version
lines in `gradle.properties`, then `./gradlew build`.

> Heads up: this project could not be compiled in the environment it was
> generated in (no access to the Forge Maven), so treat the first
> `./gradlew build` as the real compile check.
