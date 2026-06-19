package com.gunlock.items;

import net.minecraft.world.item.Item;

/**
 * The universal Bullet. Every firearm consumes this single ammo type
 * from the player's inventory. Stack size 64, craftable (see
 * data/gunlock/recipes/bullet.json), dispenser-compatible (the dispenser
 * behaviour is registered in the mod's common setup).
 *
 * The "reserve" shown on the HUD is simply the count of this item in the
 * player's inventory; there is no separate reserve container, which keeps
 * it survival-friendly and multiplayer-safe (the server owns inventory).
 */
public class AmmoItem extends Item {
    public AmmoItem(Properties props) {
        super(props.stacksTo(64));
    }
}
