package com.supernatural.block;

import com.supernatural.SupernaturalMod;
import com.supernatural.ghost.GhostType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.shape.VoxelShape;

public class ModBlocks {
    public static final TagKey<Block> WARDING =
            TagKey.of(RegistryKeys.BLOCK, Identifier.of(SupernaturalMod.MOD_ID, "warding"));

    private static final VoxelShape TOMB_SHAPE = Block.createCuboidShape(2, 0, 5, 14, 14, 11);
    private static final VoxelShape URN_SHAPE = Block.createCuboidShape(4, 0, 4, 12, 10, 12);

    // Сольовий кристал (замість руди): росте в підземеллях на стелі й підлозі
    public static final Block SALT_CRYSTAL = reg("salt_crystal", new AmethystClusterBlock(7, 3,
            AbstractBlock.Settings.copy(Blocks.AMETHYST_CLUSTER).luminance(s -> 3)));

    public static final Block SALT_LINE = reg("salt_line", new WardingBlock(
            AbstractBlock.Settings.create().noCollision().breakInstantly().nonOpaque()
                    .sounds(BlockSoundGroup.SAND).pistonBehavior(PistonBehavior.DESTROY)));

    public static final Block DEVILS_TRAP = reg("devils_trap", new WardingBlock(
            AbstractBlock.Settings.create().noCollision().strength(0.1f).nonOpaque()
                    .sounds(BlockSoundGroup.STONE).pistonBehavior(PistonBehavior.DESTROY)));

    public static final Block TOMBSTONE_NORMAL = tomb("tombstone_normal");
    public static final Block TOMBSTONE_OLD = tomb("tombstone_old");
    public static final Block TOMBSTONE_CURSED = tomb("tombstone_cursed");
    public static final Block TOMBSTONE_ANCIENT = tomb("tombstone_ancient");
    public static final Block TOMBSTONE_PEACEFUL = tomb("tombstone_peaceful");

    public static final Block REMAINS_NORMAL = remains("remains_normal", GhostType.NORMAL);
    public static final Block REMAINS_EVIL = remains("remains_evil", GhostType.EVIL);
    public static final Block REMAINS_STRONG = remains("remains_strong", GhostType.STRONG);
    public static final Block REMAINS_ANCIENT = remains("remains_ancient", GhostType.ANCIENT);
    public static final Block REMAINS_CURSED = remains("remains_cursed", GhostType.CURSED);
    public static final Block REMAINS_PEACEFUL = remains("remains_peaceful", GhostType.PEACEFUL);

    public static final Block BURIAL_URN = reg("burial_urn", new DecorBlock(
            AbstractBlock.Settings.create().strength(0.5f).nonOpaque().sounds(BlockSoundGroup.STONE), URN_SHAPE));

    private static Block tomb(String name) {
        return reg(name, new DecorBlock(AbstractBlock.Settings.create().strength(1.5f, 6.0f)
                .requiresTool().nonOpaque().sounds(BlockSoundGroup.STONE), TOMB_SHAPE));
    }

    private static Block remains(String name, GhostType t) {
        return reg(name, new RemainsBlock(AbstractBlock.Settings.create().strength(-1.0f, 3600000.0f)
                .dropsNothing().nonOpaque().sounds(BlockSoundGroup.BONE), t));
    }

    public static Block remainsOf(GhostType t) {
        return switch (t) {
            case NORMAL -> REMAINS_NORMAL;
            case EVIL -> REMAINS_EVIL;
            case STRONG -> REMAINS_STRONG;
            case ANCIENT -> REMAINS_ANCIENT;
            case CURSED -> REMAINS_CURSED;
            case PEACEFUL -> REMAINS_PEACEFUL;
        };
    }

    public static Block tombstoneOf(GhostType t) {
        return switch (t) {
            case NORMAL -> TOMBSTONE_NORMAL;
            case EVIL, STRONG -> TOMBSTONE_OLD;
            case CURSED -> TOMBSTONE_CURSED;
            case ANCIENT -> TOMBSTONE_ANCIENT;
            case PEACEFUL -> TOMBSTONE_PEACEFUL;
        };
    }

    private static Block reg(String name, Block block) {
        return Registry.register(Registries.BLOCK, SupernaturalMod.id(name), block);
    }

    public static void register() {
    }
}
