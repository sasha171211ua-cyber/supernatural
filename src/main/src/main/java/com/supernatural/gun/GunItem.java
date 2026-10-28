package com.supernatural.gun;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

/**
 * Вогнепальна зброя. ПКМ — постріл, R — перезарядка. Постріл — миттєвий промінь (hitscan) на сервері.
 * Кількість набоїв у зброї зберігається в компоненті CUSTOM_DATA.
 */
public class GunItem extends Item {
    public final GunType gun;

    public GunItem(GunType gun) {
        super(new Item.Settings().maxCount(1));
        this.gun = gun;
    }

    // ---------- дані зброї ----------
    public static int getAmmo(ItemStack s) {
        return s.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getInt("Ammo");
    }

    public static void setAmmo(ItemStack s, int n) {
        s.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, c -> c.apply(nbt -> nbt.putInt("Ammo", n)));
    }

    private static long getReloadEnd(ItemStack s) {
        return s.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().getLong("ReloadEnd");
    }

    private static void setReloadEnd(ItemStack s, long t) {
        s.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, c -> c.apply(nbt -> nbt.putLong("ReloadEnd", t)));
    }

    private Item ammoItem() {
        return Registries.ITEM.get(Identifier.of("supernatural", gun.ammoId));
    }

    // ---------- постріл ----------
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (getReloadEnd(stack) > 0) return TypedActionResult.fail(stack);
        if (player.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
        if (getAmmo(stack) <= 0) {
            if (!world.isClient) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.8f, 1.5f);
                player.sendMessage(Text.translatable("message.supernatural.gun.empty"), true);
            }
            return TypedActionResult.fail(stack);
        }
        if (world instanceof ServerWorld sw && player instanceof ServerPlayerEntity sp) {
            fire(sw, sp, stack);
        }
        return TypedActionResult.consume(stack);
    }

    private void fire(ServerWorld sw, ServerPlayerEntity p, ItemStack stack) {
        setAmmo(stack, getAmmo(stack) - 1);
        p.getItemCooldownManager().set(this, Math.max(1, gun.cooldown));

        Vec3d eye = p.getEyePos();
        for (int i = 0; i < gun.pellets; i++) {
            float yaw = p.getYaw() + (float) (sw.random.nextGaussian() * gun.spread * 0.5);
            float pitch = p.getPitch() + (float) (sw.random.nextGaussian() * gun.spread * 0.5);
            Vec3d dir = Vec3d.fromPolar(pitch, yaw);
            Vec3d end = eye.add(dir.multiply(gun.range));

            BlockHitResult bhr = sw.raycast(new RaycastContext(eye, end,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
            double maxDistSq = bhr.getType() == HitResult.Type.MISS ? gun.range * (double) gun.range
                    : eye.squaredDistanceTo(bhr.getPos());
            Box box = p.getBoundingBox().stretch(dir.multiply(gun.range)).expand(1.0);
            EntityHitResult ehr = ProjectileUtil.raycast(p, eye, end, box,
                    e -> !e.isSpectator() && e.canHit() && e != p, maxDistSq);

            if (ehr != null) {
                Entity target = ehr.getEntity();
                target.damage(sw.getDamageSources().playerAttack(p), gun.pelletDamage());
                sw.spawnParticles(ParticleTypes.CRIT, ehr.getPos().x, ehr.getPos().y, ehr.getPos().z, 3, 0.1, 0.1, 0.1, 0.02);
            } else if (bhr.getType() != HitResult.Type.MISS) {
                Vec3d h = bhr.getPos();
                sw.spawnParticles(ParticleTypes.SMOKE, h.x, h.y, h.z, 2, 0.05, 0.05, 0.05, 0.01);
            }
        }

        // дульний спалах, звук, віддача
        Vec3d muzzle = eye.add(p.getRotationVec(1.0f).multiply(0.9));
        sw.spawnParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y - 0.1, muzzle.z, 3, 0.04, 0.04, 0.04, 0.01);
        sw.spawnParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y - 0.1, muzzle.z, 1, 0.02, 0.02, 0.02, 0.0);
        sw.playSound(null, p.getX(), p.getY(), p.getZ(), ModGuns.fireSound(gun), SoundCategory.PLAYERS, 1.2f, 1.0f);
        ServerPlayNetworking.send(p, new GunNetworking.RecoilPayload(gun.recoilAngle * 0.5f));
        p.sendMessage(Text.translatable("message.supernatural.gun.ammo", getAmmo(stack), gun.capacity), true);
    }

    // ---------- перезарядка ----------
    public void startReload(ServerWorld sw, ServerPlayerEntity p, ItemStack stack) {
        if (getReloadEnd(stack) > 0) return;
        if (getAmmo(stack) >= gun.capacity) return;
        if (!p.isCreative() && countAmmo(p) <= 0) {
            p.sendMessage(Text.translatable("message.supernatural.gun.no_ammo"), true);
            return;
        }
        setReloadEnd(stack, sw.getTime() + gun.reloadTicks(gun.capacity - getAmmo(stack)));
        sw.playSound(null, p.getX(), p.getY(), p.getZ(), ModGuns.reloadStartSound(gun), SoundCategory.PLAYERS, 1.0f, 1.0f);
        p.sendMessage(Text.translatable("message.supernatural.gun.reloading"), true);
    }

    private int countAmmo(PlayerEntity p) {
        int n = 0;
        Item ammo = ammoItem();
        for (int i = 0; i < p.getInventory().size(); i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isOf(ammo)) n += s.getCount();
        }
        return n;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient || !(entity instanceof ServerPlayerEntity p)) return;
        long end = getReloadEnd(stack);
        if (end <= 0) return;
        if (!selected) { // зброю прибрали з руки: перезарядка скасована
            setReloadEnd(stack, 0);
            return;
        }
        if (world.getTime() < end) return;

        int need = gun.capacity - getAmmo(stack);
        int taken = 0;
        if (p.isCreative()) {
            taken = need;
        } else {
            Item ammo = ammoItem();
            for (int i = 0; i < p.getInventory().size() && taken < need; i++) {
                ItemStack s = p.getInventory().getStack(i);
                if (s.isOf(ammo)) {
                    int t = Math.min(s.getCount(), need - taken);
                    s.decrement(t);
                    taken += t;
                }
            }
        }
        setAmmo(stack, getAmmo(stack) + taken);
        setReloadEnd(stack, 0);
        world.playSound(null, p.getX(), p.getY(), p.getZ(), ModGuns.reloadEndSound(gun), SoundCategory.PLAYERS, 1.0f, 1.0f);
        p.sendMessage(Text.translatable("message.supernatural.gun.ammo", getAmmo(stack), gun.capacity), true);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.supernatural.gun.ammo", getAmmo(stack), gun.capacity));
        tooltip.add(Text.translatable("tooltip.supernatural.gun.uses", Text.translatable("item.supernatural." + gun.ammoId)));
        tooltip.add(Text.translatable("tooltip.supernatural.gun.controls"));
    }
}
