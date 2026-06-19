package com.gunlock.weapons;

import com.gunlock.config.GunlockConfig;
import net.minecraft.world.item.Item;

/**
 * Base item for every firearm. Minimal, compile-safe version: the gun is a
 * single-stack item with optional durability. Per-stack ammo/fire-mode state
 * and the combat behaviour live in the phase-2 combat layer, added back once
 * this base is confirmed to build and load.
 */
public class GunItem extends Item {
    private final String weaponId;
    private final WeaponStats baseStats;

    public GunItem(String weaponId, WeaponStats baseStats, Item.Properties props) {
        super(configure(props, baseStats));
        this.weaponId = weaponId;
        this.baseStats = baseStats;
    }

    private static Item.Properties configure(Item.Properties p, WeaponStats s) {
        p.stacksTo(1);
        if (s.durability() > 0) p.durability(s.durability());
        return p;
    }

    public String weaponId()       { return weaponId; }
    public WeaponStats baseStats() { return baseStats; }
    public WeaponStats effectiveStats() { return GunlockConfig.apply(weaponId, baseStats); }
}
