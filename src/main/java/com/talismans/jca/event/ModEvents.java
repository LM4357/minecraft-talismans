package com.talismans.jca.event;

import com.talismans.jca.JCATalismans;
import com.talismans.jca.item.TalismanItem;
import com.talismans.jca.item.TalismanType;
import com.talismans.jca.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JCATalismans.MOD_ID)
public class ModEvents {

    /** 玩家Tick - 处理被动符咒效果 */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            TalismanItem.onPlayerPreTick(event.player);
        } else if (event.phase == TickEvent.Phase.END) {
            TalismanItem.onPlayerTick(event.player);
        }
    }

    /** 服务器Tick - 清理已消失火球的UUID，防内存泄漏 */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer().getTickCount() % 200 != 0) return;  // 每10秒
        TalismanItem.dragonFireballs.removeIf(uuid -> {
            for (var lvl : event.getServer().getAllLevels()) {
                if (lvl.getEntity(uuid) != null) return false;
            }
            return true;
        });
    }

    /** 实体受伤 - 狗符咒永生 + 龙火球不伤自己 + 牛符咒10倍攻击 */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 狗符咒永生
            if (TalismanItem.onPlayerHurt(player, event.getAmount())) {
                event.setCanceled(true);
                return;
            }
            // 龙符咒火球爆炸不伤自己
            if (event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.LargeFireball fb
                    && TalismanItem.dragonFireballs.contains(fb.getUUID())) {
                TalismanItem.dragonFireballs.remove(fb.getUUID());  // 用后即清
                event.setCanceled(true);
                return;
            }
        }
        if (event.getSource().getEntity() instanceof Player attacker) {
            java.util.Set<TalismanType> types = TalismanItem.getActiveTalismanTypes(attacker);
            if (types.contains(TalismanType.OX)) {
                event.setAmount(event.getAmount() * 10.0f);
            }
        }
    }

    /** 龙符咒火球爆炸加深 — 不规则深坑12格 */
    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getExploder() instanceof net.minecraft.world.entity.projectile.LargeFireball fb
                && TalismanItem.dragonFireballs.contains(fb.getUUID())) {
            Vec3 center = event.getExplosion().getPosition();
            BlockPos base = BlockPos.containing(center.x, center.y, center.z);
            java.util.List<BlockPos> extra = new java.util.ArrayList<>();
            java.util.Random rng = new java.util.Random();
            for (int dy = 1; dy <= 12; dy++) {
                BlockPos bp = base.below(dy);
                if (bp.getY() <= event.getLevel().getMinBuildHeight()) break;
                int radius = dy <= 6 ? 2 : 1;  // 前半宽后半窄
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        // 切角 + 随机削弱，不规则形状
                        if (Math.abs(dx) + Math.abs(dz) > radius + 1) continue;
                        if (Math.abs(dx) == Math.abs(dz) && Math.abs(dx) == radius) continue;
                        if (rng.nextFloat() > 0.6f) continue;  // 40%概率跳过
                        BlockPos p = bp.offset(dx, 0, dz);
                        BlockState state = event.getLevel().getBlockState(p);
                        if (state.getExplosionResistance(event.getLevel(), p, event.getExplosion()) < 100) {
                            extra.add(p.immutable());
                        }
                    }
                }
            }
            event.getAffectedBlocks().addAll(extra);
        }
    }

    /** 实体交互 - 猴符咒变形 */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        Entity target = event.getTarget();
        InteractionResult result = TalismanItem.onEntityInteract(player, player.level(), hand, target);
        if (result == InteractionResult.SUCCESS) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    /** 左键点击空气 - 羊符咒返回身体 */
    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getLevel().isClientSide()) return;
        TalismanItem.tryReturnFromSheep(event.getEntity());
    }

    /** 左键点击方块 - 羊符咒返回身体 > 鼠符咒一击即碎 */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        // 羊符咒优先
        if (TalismanItem.tryReturnFromSheep(event.getEntity())) return;

        BlockPos pos = event.getPos();
        if (TalismanItem.ratMarkedBlocks.containsKey(pos)) {
            Block block = TalismanItem.ratMarkedBlocks.remove(pos);
            Level level = event.getLevel();
            level.removeBlock(pos, false);
            ItemEntity drop = new ItemEntity(level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    new ItemStack(block.asItem()));
            level.addFreshEntity(drop);
            event.getEntity().sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§7鼠符咒：一击击杀！掉落 " + block.getName().getString() + "§r"));
        }
    }

    /** 实体死亡 - Boss掉落符咒 */
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide()) return;

        if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) {
            dropRandomTalisman(level, entity, 1.0f);
        } else if (entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss) {
            dropRandomTalisman(level, entity, 0.5f);
        } else if (entity instanceof net.minecraft.world.entity.monster.ElderGuardian) {
            dropRandomTalisman(level, entity, 0.1f);
        }
    }

    private static void dropRandomTalisman(Level level, LivingEntity entity, float chance) {
        if (level.random.nextFloat() < chance) {
            ItemStack[] talismans = {
                    new ItemStack(ModItems.RAT_TALISMAN.get()),
                    new ItemStack(ModItems.OX_TALISMAN.get()),
                    new ItemStack(ModItems.TIGER_TALISMAN.get()),
                    new ItemStack(ModItems.RABBIT_TALISMAN.get()),
                    new ItemStack(ModItems.DRAGON_TALISMAN.get()),
                    new ItemStack(ModItems.SNAKE_TALISMAN.get()),
                    new ItemStack(ModItems.HORSE_TALISMAN.get()),
                    new ItemStack(ModItems.SHEEP_TALISMAN.get()),
                    new ItemStack(ModItems.MONKEY_TALISMAN.get()),
                    new ItemStack(ModItems.ROOSTER_TALISMAN.get()),
                    new ItemStack(ModItems.DOG_TALISMAN.get()),
                    new ItemStack(ModItems.PIG_TALISMAN.get())
            };
            ItemStack drop = talismans[level.random.nextInt(talismans.length)];
            ItemEntity itemEntity = new ItemEntity(level,
                    entity.getX(), entity.getY() + 1, entity.getZ(), drop);
            itemEntity.setGlowingTag(true);
            level.addFreshEntity(itemEntity);
        }
    }
}
