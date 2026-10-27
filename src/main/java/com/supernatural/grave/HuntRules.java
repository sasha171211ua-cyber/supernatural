package com.supernatural.grave;

import com.supernatural.item.ModItems;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

/**
 * Охота активується, коли гравець у цьому світі хоч раз СКРАФТИВ і запальничку (вогнище-кресало), і кочергу.
 * Використовується ванільна статистика «створено» (Stats.CRAFTED): вона зберігається для кожного гравця
 * окремо в даних світу, не залежить від інвентаря й не рахує креатив, команди чи підбір з землі.
 */
public final class HuntRules {
    private HuntRules() {
    }

    public static boolean isActive(ServerPlayerEntity p) {
        return crafted(p, Items.FLINT_AND_STEEL) && crafted(p, ModItems.IRON_POKER);
    }

    private static boolean crafted(ServerPlayerEntity p, net.minecraft.item.Item item) {
        return p.getStatHandler().getStat(Stats.CRAFTED.getOrCreateStat(item)) > 0;
    }
}
