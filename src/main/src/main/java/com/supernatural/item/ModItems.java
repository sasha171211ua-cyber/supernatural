package com.supernatural.item;

import com.supernatural.SupernaturalMod;
import com.supernatural.block.ModBlocks;
import com.supernatural.entity.ModEntities;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
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
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.List;

public class ModItems {
    private static final List<Item> TAB = new ArrayList<>();

    // Сіль = предмет, який кладе блок лінії солі
    public static final Item SALT = reg("salt", new BlockItem(ModBlocks.SALT_LINE, new Item.Settings()));
    public static final Item SALT_CRYSTAL = block("salt_crystal", ModBlocks.SALT_CRYSTAL);
    public static final Item DEVILS_TRAP = block("devils_trap", ModBlocks.DEVILS_TRAP);
    public static final Item HOLY_WATER = reg("holy_water",
            new HolyWaterItem(new Item.Settings().maxCount(16).recipeRemainder(Items.GLASS_BOTTLE)));
    // Урон 5 (1 база + 4), швидкість атаки 1.25/с (інтервал ~0.8 с), міцність 250 (залізо)
    public static final Item IRON_POKER = reg("iron_poker", new SwordItem(ToolMaterials.IRON,
            new Item.Settings().attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.IRON, 2, -2.75f))));

    // Матеріали й трофеї (ефектів немає)
    public static final Item ECTOPLASM = reg("ectoplasm", new Item(new Item.Settings()));
    public static final Item GHOST_SHARD = reg("ghost_shard", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));
    public static final Item OLD_AMULET = reg("old_amulet", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));
    public static final Item RELIC_COMPONENT = reg("hunter_relic_component", new Item(new Item.Settings().rarity(Rarity.RARE)));
    public static final Item CURSED_FRAGMENT = reg("cursed_fragment", new Item(new Item.Settings().rarity(Rarity.RARE)));

    // Артефакти мирних привидів (поки без ефектів)
    public static final Item WARNING_AMULET = reg("warning_amulet", new Item(new Item.Settings().maxCount(1)));
    public static final Item PROTECTIVE_TALISMAN = reg("protective_talisman", new Item(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));
    public static final Item GRAVE_DETECTOR = reg("grave_detector", new Item(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));
    public static final Item HUNTER_RELIC = reg("hunter_relic", new Item(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));

    // Блоки захоронень
    public static final Item TOMBSTONE_NORMAL = block("tombstone_normal", ModBlocks.TOMBSTONE_NORMAL);
    public static final Item TOMBSTONE_OLD = block("tombstone_old", ModBlocks.TOMBSTONE_OLD);
    public static final Item TOMBSTONE_CURSED = block("tombstone_cursed", ModBlocks.TOMBSTONE_CURSED);
    public static final Item TOMBSTONE_ANCIENT = block("tombstone_ancient", ModBlocks.TOMBSTONE_ANCIENT);
    public static final Item TOMBSTONE_PEACEFUL = block("tombstone_peaceful", ModBlocks.TOMBSTONE_PEACEFUL);
    public static final Item REMAINS_NORMAL = block("remains_normal", ModBlocks.REMAINS_NORMAL);
    public static final Item REMAINS_EVIL = block("remains_evil", ModBlocks.REMAINS_EVIL);
    public static final Item REMAINS_STRONG = block("remains_strong", ModBlocks.REMAINS_STRONG);
    public static final Item REMAINS_ANCIENT = block("remains_ancient", ModBlocks.REMAINS_ANCIENT);
    public static final Item REMAINS_CURSED = block("remains_cursed", ModBlocks.REMAINS_CURSED);
    public static final Item REMAINS_PEACEFUL = block("remains_peaceful", ModBlocks.REMAINS_PEACEFUL);
    public static final Item BURIAL_URN = block("burial_urn", ModBlocks.BURIAL_URN);

    public static final Item GHOST_SPAWN_EGG = reg("ghost_spawn_egg",
            new SpawnEggItem(ModEntities.GHOST, 0xCFE8FF, 0x223344, new Item.Settings()));

    public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, SupernaturalMod.id("main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(HOLY_WATER))
                    .displayName(Text.translatable("itemGroup.supernatural"))
                    .entries((ctx, entries) -> {
                        for (Item i : TAB) entries.add(i);
                    }).build());

    private static Item block(String name, Block b) {
        return reg(name, new BlockItem(b, new Item.Settings()));
    }

    private static Item reg(String name, Item item) {
        TAB.add(item);
        return Registry.register(Registries.ITEM, SupernaturalMod.id(name), item);
    }

    public static void addToTab(Item item) {
        TAB.add(item);
    }

    public static void register() {
    }
}
