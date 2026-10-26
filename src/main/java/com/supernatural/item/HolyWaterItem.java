package com.supernatural.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** ПКМ: бризкає святою водою навколо себе, обпікає нечисть у радіусі 4 блоків. */
public class HolyWaterItem extends Item {
    public HolyWaterItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.SPLASH, user.getX(), user.getY() + 1.0, user.getZ(),
                    40, 1.5, 0.6, 1.5, 0.1);
            sw.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_SPLASH_POTION_BREAK,
                    SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        return TypedActionResult.success(
                ItemUsage.exchangeStack(stack, user, new ItemStack(Items.GLASS_BOTTLE)), world.isClient());
    }
}
