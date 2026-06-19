package com.gunlock.weapons;

/**
 * Top-level category for every firearm. Drives default behaviour
 * (e.g. shotguns fire pellets, snipers get scope slots by default)
 * and is used for creative-tab grouping and config sectioning.
 */
public enum WeaponClass {
    PISTOL("Pistols"),
    SMG("SMGs"),
    ASSAULT_RIFLE("Assault Rifles"),
    SHOTGUN("Shotguns"),
    SNIPER("Snipers"),
    HEAVY("Heavy Weapons");

    private final String displayName;

    WeaponClass(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
