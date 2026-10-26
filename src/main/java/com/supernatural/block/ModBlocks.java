package com.supernatural.block;

import com.supernatural.SupernaturalMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    /** Блоки, які відлякують нечисть (теги: data/supernatural/tags/block/warding.json) */
    public static final TagKey<Block> WARDING =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of(SupernaturalMod.MOD_ID, "warding"));

    public static final Block SALT_ORE = reg("salt_ore",
            new Block(AbstractBlock.Settings.copy(Blocks.IRON_ORE)));

    public static final Block SALT_LINE = reg("salt_line", new WardingBlock(
            AbstractBlock.Settings.create().noCollision().breakInstantly().nonOpaque()
                    .sounds(BlockSoundGroup.SAND).pistonBehavior(PistonBehavior.DESTROY)));

    public static final Block DEVILS_TRAP = reg("devils_trap", new WardingBlock(
            AbstractBlock.Settings.create().noCollision().strength(0.1f).nonOpaque()
                    .sounds(BlockSoundGroup.STONE).pistonBehavior(PistonBehavior.DESTROY)));

    private static Block reg(String name, Block block) {
        return Registry.register(Registries.BLOCK, SupernaturalMod.id(name), block);
    }

    public static void register() {
    }
}
