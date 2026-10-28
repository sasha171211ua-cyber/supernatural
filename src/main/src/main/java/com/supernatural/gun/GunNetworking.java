package com.supernatural.gun;

import com.supernatural.SupernaturalMod;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/** Мережеві пакети зброї: клієнт просить перезарядку, сервер надсилає віддачу. */
public final class GunNetworking {
    private GunNetworking() {
    }

    public record ReloadPayload() implements CustomPayload {
        public static final CustomPayload.Id<ReloadPayload> ID = new CustomPayload.Id<>(SupernaturalMod.id("gun_reload"));
        public static final PacketCodec<ByteBuf, ReloadPayload> CODEC = PacketCodec.unit(new ReloadPayload());

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record RecoilPayload(float kick) implements CustomPayload {
        public static final CustomPayload.Id<RecoilPayload> ID = new CustomPayload.Id<>(SupernaturalMod.id("gun_recoil"));
        public static final PacketCodec<ByteBuf, RecoilPayload> CODEC =
                PacketCodecs.FLOAT.xmap(RecoilPayload::new, RecoilPayload::kick);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ReloadPayload.ID, ReloadPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RecoilPayload.ID, RecoilPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ReloadPayload.ID, (payload, context) -> {
            ServerPlayerEntity p = context.player();
            context.server().execute(() -> {
                ItemStack s = p.getMainHandStack();
                if (s.getItem() instanceof GunItem g && p.getWorld() instanceof ServerWorld sw) {
                    g.startReload(sw, p, s);
                }
            });
        });
    }
}
