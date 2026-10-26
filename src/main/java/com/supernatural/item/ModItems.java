package com.supernatural.item;

import com.supernatural.SupernaturalMod;
import com.supernatural.block.ModBlocks;
import com.supernatural.entity.ModEntities;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public class ModItems {
    // Сіль = предмет, який кладе блок лінії солі
    public static final Item SALT = reg("salt", new BlockItem(ModBlocks.SALT_LINE, new Item.Settings()));
    public static final Item SALT_ORE = reg("salt_ore", new BlockItem(ModBlocks.SALT_ORE, new Item.Settings()));
    public static final Item DEVILS_TRAP = reg("devils_trap", new BlockItem(ModBlocks.DEVILS_TRAP, new Item.Settings()));
    public static final Item HOLY_WATER = reg("holy_water",
            new HolyWaterItem(new Item.Settings().maxCount(16).recipeRemainder(Items.GLASS_BOTTLE)));
    public static final Item IRON_POKER = reg("iron_poker", new SwordItem(ToolMaterials.IRON,
            new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 2, -2.2f))));
    public static final Item GHOST_SPAWN_EGG = reg("ghost_spawn_egg",
            new SpawnEggItem(ModEntities.GHOST, 0xCFE8FF, 0x223344, new Item.Settings()));

    public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, SupernaturalMod.id("main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(HOLY_WATER))
                    .displayName(Text.translatable("itemGroup.supernatural"))
                    .entries((ctx, entries) -> {
                        entries.add(SALT);
                        entries.add(SALT_ORE);
                        entries.add(HOLY_WATER);
                        entries.add(DEVILS_TRAP);
                        entries.add(IRON_POKER);
                        entries.add(GHOST_SPAWN_EGG);
                    }).build());

    private static Item reg(String name, Item item) {
        return Registry.register(Registries.ITEM, SupernaturalMod.id(name), item);
    }

    public static void register() {
    }
}
