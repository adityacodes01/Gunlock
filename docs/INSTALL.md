# Installation & Build Guide

## For players (once a build exists)

1. Install **Minecraft Java 26.2** and the matching **Forge 26.2** from
   <https://files.minecraftforge.net/>. 26.2 requires **Java 25** — the
   Forge installer bundles a suitable runtime, but confirm your launcher
   profile uses Java 25.
2. Run the Forge installer → *Install client*.
3. Drop `gunlock-26.2-0.1.0.jar` into `.minecraft/mods/`.
4. Launch the Forge 26.2 profile.

## For developers

This is a source tree, not a pre-configured workspace, because the 26.2
MDK is days old and its exact ForgeGradle plugin version should come from
the official template rather than a guess.

1. Download the **Forge 26.2 MDK** (Mod Development Kit) zip.
2. Copy from this repo into the MDK folder:
   - `src/` (the whole tree)
   - `build.gradle`, `settings.gradle`, `gradle.properties`
   Keep the MDK's `gradle/` wrapper and `gradlew` scripts.
3. Open `gradle.properties` and set the three placeholder lines:
   - `forge_version` → the real 26.2 build number from files.minecraftforge.net
   - `mappings_version` → whatever the MDK template uses (26.x is unobfuscated)
   - confirm `minecraft_version=26.2`
4. Import as a Gradle project (IntelliJ IDEA recommended). Use a **JDK 25**.
5. Resolve every `TODO(26.2)` the compiler flags — these are the
   engine-facing seams (DataComponents, ClipContext, DamageSource,
   networking transport, GUI layer registration). The logic around them
   is done.
6. `./gradlew runClient` to test in-game; `./gradlew build` for the jar.

## Crafting

Iron Nugget + Gunpowder (shapeless) → **8 Bullets**
(`data/gunlock/recipes/bullet.json`).
