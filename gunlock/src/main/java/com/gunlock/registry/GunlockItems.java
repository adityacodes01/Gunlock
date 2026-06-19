package com.gunlock.registry;

import java.util.HashMap;
import java.util.Map;

import com.gunlock.Gunlock;
import com.gunlock.items.AmmoItem;
import com.gunlock.weapons.GunItem;
import com.gunlock.weapons.WeaponStats;
import com.gunlock.weapons.Weapons;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers the Bullet + one GunItem per entry in {@link Weapons}, all from
 * the data map. Forge 26.2 requires every Item.Properties to carry its id
 * via .setId(ITEMS.key(name)) — handled here for each registration.
 */
public final class GunlockItems {
    private GunlockItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Gunlock.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Gunlock.MOD_ID);

    public static final Map<String, RegistryObject<Item>> GUNS = new HashMap<>();

    public static final RegistryObject<Item> BULLET = ITEMS.register("bullet",
            () -> new AmmoItem(new Item.Properties().setId(ITEMS.key("bullet"))));

    static {
        for (Map.Entry<String, WeaponStats> e : Weapons.ALL.entrySet()) {
            final String id = e.getKey();
            final WeaponStats stats = e.getValue();
            GUNS.put(id, ITEMS.register(id,
                    () -> new GunItem(id, stats, new Item.Properties().setId(ITEMS.key(id)))));
        }
    }

    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("gunlock",
            () -> CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.gunlock"))
                    .icon(() -> GUNS.get("ak47").get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(BULLET.get());
                        GUNS.values().forEach(ro -> output.accept(ro.get()));
                    })
                    .build());
}
