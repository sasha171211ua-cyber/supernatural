package com.supernatural.client;

import com.supernatural.gun.GunItem;
import com.supernatural.gun.GunNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Клієнтська частина зброї: клавіша перезарядки (R) і віддача. */
public final class GunClient {
    private GunClient() {
    }

    public static void init() {
        KeyBinding reload = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.supernatural.reload", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.supernatural"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (reload.wasPressed()) {
                if (client.player != null && client.player.getMainHandStack().getItem() instanceof GunItem) {
                    ClientPlayNetworking.send(new GunNetworking.ReloadPayload());
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(GunNetworking.RecoilPayload.ID, (payload, context) -> {
            var p = context.client().player;
            if (p != null) p.setPitch(p.getPitch() - payload.kick());
        });
    }
}
