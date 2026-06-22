package com.talismans.jca.client;

import com.talismans.jca.JCATalismans;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端事件处理 - 蛇符咒完全隐身（含盔甲和手持物品）
 *
 * 原版 INVISIBILITY 效果在有盔甲时只隐藏身体不隐藏盔甲，
 * 因此通过在客户端取消整个玩家渲染来实现完全隐身。
 *
 * 蛇符咒使用 amplifier=10 作为特殊标记，
 * 客户端检测到该标记后取消 RenderPlayerEvent.Pre 即可隐藏一切。
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = JCATalismans.MOD_ID)
public class ClientEvents {

    /** 蛇符咒隐身标记 - amplifier 值 */
    private static final int SNAKE_INVIS_MARKER = 10;

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        MobEffectInstance invis = player.getEffect(MobEffects.INVISIBILITY);
        if (invis != null && invis.getAmplifier() == SNAKE_INVIS_MARKER) {
            event.setCanceled(true);
        }
    }
}
