package com.talismans.jca.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.LargeFireball;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 符咒物品基类 - 实现12种符咒的主动/被动能力
 * 支持符咒盒从副手读取
 */
public class TalismanItem extends Item {
    private final TalismanType type;
    private static final Map<UUID, Boolean> snakeInvis = new HashMap<>();
    private static final Map<UUID, Boolean> sheepProjecting = new HashMap<>();
    private static final Map<UUID, BlockPos> sheepReturnPos = new HashMap<>();
    private static final Map<UUID, GameType> sheepOriginalGameMode = new HashMap<>();
    private static final Map<UUID, Long> sheepCooldown = new HashMap<>();
    public static final Set<UUID> dragonFireballs = java.util.Collections.newSetFromMap(
            new java.util.concurrent.ConcurrentHashMap<>());

    public TalismanItem(Properties properties, TalismanType type) {
        super(properties);
        this.type = type;
    }

    public TalismanType getTalismanType() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(type.getDisplayName()).withStyle(type.getColor()));
        String[] lines = type.getDescription().split("\n");
        for (String line : lines) {
            tooltip.add(Component.literal(line));
        }
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§6◆ 右键使用能力 ◆§r").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return false; }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) { return false; }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (type == TalismanType.PIG) {
            // 猪符咒：持续发射激光
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        if (!level.isClientSide()) {
            switch (type) {
                case RAT -> useRatAbility(level, player);
                case DRAGON -> useDragonAbility(level, player);
                case SNAKE -> useSnakeAbility(player);
                case MONKEY -> {}
                case ROOSTER -> {}
                case SHEEP -> useSheepAbility(level, player);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        if (type == TalismanType.PIG) return 72000;  // 长达1小时=无限
        return 0;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (type != TalismanType.PIG || level.isClientSide() || !(entity instanceof Player player)) return;

        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double range = 200.0;

        // 每 tick 粒子光束
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer sp) {
            for (double d = 0; d <= range; d += 0.15) {
                Vec3 pos = eyePos.add(look.scale(d));
                serverLevel.sendParticles(sp, ParticleTypes.END_ROD, true,
                        pos.x, pos.y, pos.z, 3, 0.0, 0.0, 0.0, 0.0);
                if ((int)(d * 10) % 4 == 0) {
                    serverLevel.sendParticles(sp, ParticleTypes.FLAME, true,
                            pos.x, pos.y, pos.z, 2, 0.05, 0.05, 0.05, 0.0);
                }
            }
        }

        // 持续音效
        if (player.level().getGameTime() % 10 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.6f, 2.0f);
        }

        // 每3 tick伤害一次（每tick太密集）
        if (player.level().getGameTime() % 3 != 0) return;

        // 每轮伤害重新记录——同一轮内不重复伤，下一轮可以再伤
        Set<Integer> hit = new java.util.HashSet<>();
        boolean foundBlock = false;
        for (double d = 0; d <= range; d += 0.5) {
            Vec3 rayPos = eyePos.add(look.scale(d));
            // 激光命中方块着火 — 第一个命中的固体方块
            if (!foundBlock) {
                BlockPos bp = BlockPos.containing(rayPos);
                if (!level.isEmptyBlock(bp) && level.getBlockState(bp).isSolidRender(level, bp)) {
                    foundBlock = true;
                    BlockPos firePos = bp.above();
                    if (level.isEmptyBlock(firePos) || level.getBlockState(firePos).isAir()) {
                        level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
                    }
                }
            }
            // 实体伤害
            AABB box = new AABB(rayPos.x - 1.5, rayPos.y - 1.5, rayPos.z - 1.5,
                    rayPos.x + 1.5, rayPos.y + 1.5, rayPos.z + 1.5);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && e.isAttackable() && !hit.contains(e.getId()))) {
                hit.add(target.getId());
                target.hurt(player.damageSources().indirectMagic(player, player), 24.0f);
                target.setSecondsOnFire(8);
            }
        }
    }

    // ==================== 鼠符咒 - 静与动：给方块加血条，打掉掉落原方块 ====================
    /** 被标记的方块位置 → 方块类型 */
    public static final Map<BlockPos, net.minecraft.world.level.block.Block> ratMarkedBlocks = new HashMap<>();

    private void useRatAbility(Level level, Player player) {
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() == BlockHitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            var blockState = level.getBlockState(pos);
            if (blockState.isAir()) return;
            if (ratMarkedBlocks.containsKey(pos)) {
                player.sendSystemMessage(Component.literal("§7鼠符咒：该方块已被赋予生命§r"));
                return;
            }

            // 标记方块，给它1点"生命"——打掉即掉落自身
            ratMarkedBlocks.put(pos.immutable(), blockState.getBlock());

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        30, 0.3, 0.3, 0.3, 0.05);
                serverLevel.sendParticles(ParticleTypes.HEART,
                        pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                        10, 0.2, 0.2, 0.2, 0.02);
            }
            level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.PLAYERS, 1.0f, 1.0f);
            player.sendSystemMessage(Component.literal("§7鼠符咒：方块获得生命之力[1HP]！破坏后将掉落自身§r"));
        }
    }

    // ==================== 龙符咒 - 爆破（加速火球，不伤自己） ====================
    private void useDragonAbility(Level level, Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 spawnPos = player.getEyePosition().add(look.scale(2.5));  // 更远生成，防连发撞膛

        LargeFireball fireball = new LargeFireball(level, player, look.x, look.y, look.z, 16) {
            @Override
            public boolean canHitEntity(net.minecraft.world.entity.Entity target) {
                // 不伤主人、不伤其他龙火球（防连发炸膛）
                if (target == getOwner()) return false;
                if (target instanceof LargeFireball && dragonFireballs.contains(target.getUUID())) return false;
                return super.canHitEntity(target);
            }
        };
             fireball.setPos(spawnPos);
        fireball.setDeltaMovement(look.x * 12.0, look.y * 12.0, look.z * 12.0);
        dragonFireballs.add(fireball.getUUID());
        level.addFreshEntity(fireball);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0f, 1.2f);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    spawnPos.x, spawnPos.y, spawnPos.z,
                    25, 0.2, 0.2, 0.2, 0.1);
        }
    }

    // ==================== 蛇符咒 - 隐形（含盔甲和手持物品） ====================
    /** 蛇符咒隐身标记 amplifier 值，客户端 ClientEvents 通过此值识别并取消整个玩家渲染 */
    private static final int SNAKE_INVIS_MARKER = 10;

    private void useSnakeAbility(Player player) {
        UUID id = player.getUUID();
        boolean isInvis = snakeInvis.getOrDefault(id, false);

        if (isInvis) {
            // 解除隐身
            player.removeEffect(MobEffects.INVISIBILITY);
            player.setInvisible(false);
            snakeInvis.put(id, false);
            player.sendSystemMessage(Component.literal("§a蛇符咒：隐身已解除，盔甲可见§r"));
        } else {
            // 开启隐身：amplifier=10 作为蛇符咒标记，客户端检测到后取消整个玩家渲染
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
                    MobEffectInstance.INFINITE_DURATION, SNAKE_INVIS_MARKER, false, false, true));
            player.setInvisible(true);
            snakeInvis.put(id, true);
            player.sendSystemMessage(Component.literal("§a蛇符咒：已完全隐身（含盔甲与手持物品）§r"));
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 2.0f);
        }
    }

    // ==================== 羊符咒 - 灵魂出窍 ====================
    private void useSheepAbility(Level level, Player player) {
        if (!(player instanceof ServerPlayer sp)) return;
        UUID id = player.getUUID();
        boolean isProjecting = sheepProjecting.getOrDefault(id, false);

        if (isProjecting) {
            returnToBody(sp);
        } else {
            sheepOriginalGameMode.put(id, sp.gameMode.getGameModeForPlayer());
            sheepReturnPos.put(id, sp.blockPosition());
            sheepProjecting.put(id, true);
            sheepCooldown.put(id, sp.level().getGameTime() + 20);  // 1秒冷却
            // 把羊符咒自动装备到主手，确保右键能返回
            ItemStack sheepStack = findSheepTalisman(sp);
            if (!sheepStack.isEmpty()) {
                sp.getInventory().setItem(sp.getInventory().selected, sheepStack);
            }
            sp.setGameMode(GameType.SPECTATOR);
            sp.sendSystemMessage(Component.literal("§d羊符咒：灵魂出窍！右键空气或左键点击返回身体§r"));
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.0f, 0.5f);
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL, sp.getX(), sp.getY() + 1, sp.getZ(),
                        30, 0.3, 0.5, 0.3, 0.05);
            }
        }
    }

    private static ItemStack findSheepTalisman(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.getItem() instanceof TalismanItem ti && ti.getTalismanType() == TalismanType.SHEEP) {
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void returnToBody(ServerPlayer sp) {
        UUID id = sp.getUUID();
        GameType originalMode = sheepOriginalGameMode.getOrDefault(id, GameType.SURVIVAL);
        BlockPos returnPos = sheepReturnPos.get(id);
        sheepProjecting.remove(id);
        sheepOriginalGameMode.remove(id);
        sheepReturnPos.remove(id);
        sheepCooldown.remove(id);

        sp.setGameMode(originalMode);
        if (returnPos != null) {
            sp.teleportTo(returnPos.getX() + 0.5, returnPos.getY(), returnPos.getZ() + 0.5);
        }
        sp.sendSystemMessage(Component.literal("§d羊符咒：灵魂已返回身体§r"));
        sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /** 羊符咒 - 左键点击返回身体（供ModEvents调用） */
    public static boolean tryReturnFromSheep(Player player) {
        if (sheepProjecting.getOrDefault(player.getUUID(), false) && player instanceof ServerPlayer sp) {
            returnToBody(sp);
            return true;
        }
        return false;
    }

    // ==================== 被动能力 (tick处理) ====================
    public static void onPlayerTick(Player player) {
        if (player.level().isClientSide()) return;

        // 收集手持和符咒盒中的符咒类型
        Set<TalismanType> activeTypes = getActiveTalismanTypes(player);

        boolean hasOx = activeTypes.contains(TalismanType.OX);
        boolean hasTiger = activeTypes.contains(TalismanType.TIGER);
        boolean hasRabbit = activeTypes.contains(TalismanType.RABBIT);
        boolean hasHorse = activeTypes.contains(TalismanType.HORSE);
        boolean hasDog = activeTypes.contains(TalismanType.DOG);
        boolean hasRooster = activeTypes.contains(TalismanType.ROOSTER);

        // 牛符咒 - 10倍攻击 (Strength X = amplifier 9 → +30攻击力 ≈ 31倍, 但实际Minecraft中每级+3)
        // Strength IX = amplifier 8 = +27 damage ≈ 28x with fist. 用户要10倍 -> amplifier 9 = Strength X
        if (hasOx) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
                    40, 9, false, false, true));
        }

        // 虎符咒 - 平衡
        if (hasTiger) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                    40, 1, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
                    40, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP,
                    40, 1, false, false, true));
        }

        // 兔符咒 - 速度20级 (amplifier 19 = Speed XX)
        if (hasRabbit) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
                    40, 19, false, false, true));
        }

        // 鸡符咒 - 允许飞行（双击空格开关）
        if (hasRooster) {
            player.getAbilities().mayfly = true;
            // 没有兔时恢复默认飞行速度
            if (!hasRabbit && player.getAbilities().getFlyingSpeed() > 0.06f) {
                player.getAbilities().setFlyingSpeed(0.05f);
            }
            player.onUpdateAbilities();
        } else if (!player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        // 马符咒 - 治愈
        if (hasHorse) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                    60, 4, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.HEAL,
                    60, 0, false, false, true));
            // 清除负面效果
            var snapshot = new ArrayList<>(player.getActiveEffects());
            for (var instance : snapshot) {
                if (!instance.getEffect().isBeneficial()) {
                    player.removeEffect(instance.getEffect());
                }
            }
        }

        // 羊符咒 - 旁观模式挥臂返回（冷却期内不触发）
        if (sheepProjecting.getOrDefault(player.getUUID(), false)) {
            long cd = sheepCooldown.getOrDefault(player.getUUID(), 0L);
            if (player.swinging && player instanceof ServerPlayer sp && player.level().getGameTime() > cd) {
                returnToBody(sp);
            }
        }
    }

    /**
     * 获取玩家当前生效的符咒类型集合（手持 + 副手 + 符咒盒）
     */
    public static Set<TalismanType> getActiveTalismanTypes(Player player) {
        Set<TalismanType> types = EnumSet.noneOf(TalismanType.class);

        // 主手
        checkItem(types, player.getMainHandItem());
        // 副手
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        checkItem(types, offHand);
        // 符咒盒在副手时，读取其中所有符咒
        if (offHand.getItem() instanceof TalismanBoxItem) {
            for (TalismanType t : TalismanBoxItem.getStoredTalismans(offHand)) {
                types.add(t);
            }
        }

        return types;
    }

    private static void checkItem(Set<TalismanType> types, ItemStack stack) {
        if (stack.getItem() instanceof TalismanItem t) {
            types.add(t.getTalismanType());
        }
    }

    /**
     * Phase.START 调用 — 鸡+兔飞行加速，原版方向全保留，只提速
     */
    public static void onPlayerPreTick(Player player) {
        Set<TalismanType> types = getActiveTalismanTypes(player);
        if (!types.contains(TalismanType.ROOSTER) || !types.contains(TalismanType.RABBIT)) return;
        if (!player.getAbilities().flying) return;

        player.getAbilities().setFlyingSpeed(0.5f);
        player.onUpdateAbilities();
    }

    // ==================== 狗符咒 - 永生（锁1血不死） ====================
    public static boolean onPlayerHurt(Player player, float damage) {
        Set<TalismanType> activeTypes = getActiveTalismanTypes(player);
        if (activeTypes.contains(TalismanType.DOG)) {
            if (player.getHealth() - damage <= 0) {
                player.setHealth(1.0f);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 4, false, false));
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
                if (player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                            player.getX(), player.getY() + 1, player.getZ(),
                            50, 0.5, 1.0, 0.5, 0.1);
                }
                player.sendSystemMessage(Component.literal("§5狗符咒：永生之力救了你一命！§r"));
                return true;
            }
        }
        return false;
    }

    // ==================== 猴符咒 - 实体交互 ====================
    public static InteractionResult onEntityInteract(Player player, Level level, InteractionHand hand, Entity target) {
        ItemStack stack = player.getItemInHand(hand);

        if (isTalisman(stack, TalismanType.MONKEY) && target instanceof LivingEntity livingTarget) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                EntityType<?>[] animalTypes = {
                        EntityType.SHEEP, EntityType.COW, EntityType.PIG,
                        EntityType.CHICKEN, EntityType.RABBIT, EntityType.WOLF,
                        EntityType.CAT, EntityType.FOX, EntityType.PARROT
                };
                EntityType<?> targetType;
                if (player.isShiftKeyDown()) {
                    targetType = EntityType.SHEEP;
                } else {
                    targetType = animalTypes[level.random.nextInt(animalTypes.length)];
                }
                Vec3 pos = target.position();
                float yRot = target.getYRot();
                float xRot = target.getXRot();
                Entity newEntity = targetType.create(level);
                if (newEntity != null) {
                    target.discard();
                    newEntity.moveTo(pos.x, pos.y, pos.z, yRot, xRot);
                    newEntity.setCustomName(Component.literal("§b被变形生物§r"));
                    newEntity.setCustomNameVisible(true);
                    level.addFreshEntity(newEntity);
                    serverLevel.sendParticles(ParticleTypes.WITCH,
                            pos.x, pos.y + 1, pos.z,
                            30, 0.5, 0.5, 0.5, 0.1);
                    level.playSound(null, pos.x, pos.y, pos.z,
                            SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // ==================== 工具方法 ====================
    public static boolean isTalisman(ItemStack stack, TalismanType targetType) {
        return stack.getItem() instanceof TalismanItem talisman &&
                talisman.getTalismanType() == targetType;
    }

    public static boolean isAnyTalisman(ItemStack stack) {
        return stack.getItem() instanceof TalismanItem;
    }

    public static boolean isAnyTalismanOrBox(ItemStack stack) {
        return stack.getItem() instanceof TalismanItem || stack.getItem() instanceof TalismanBoxItem;
    }
}
