package com.talismans.jca.item;

import com.talismans.jca.gui.TalismanBoxMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class TalismanBoxItem extends Item {

    private static final String TAG_ITEMS = "Items";

    public TalismanBoxItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public boolean isFoil(ItemStack stack) { return !getStoredTalismans(stack).isEmpty(); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6◆ 十二符咒盒 ◆§r").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("§7右键打开 | 拖入符咒存入 | 副手激活内部符咒§r"));
        tooltip.add(Component.literal(""));

        Set<TalismanType> stored = getStoredTalismans(stack);
        if (stored.isEmpty()) {
            tooltip.add(Component.literal("§8[ 空的 ]§r"));
        } else {
            tooltip.add(Component.literal("§e已存入 " + stored.size() + "/12 枚符咒：§r"));
            for (TalismanType t : stored) {
                tooltip.add(Component.literal("  " + t.getColor() + "● " + t.getDisplayName()));
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack box = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            NetworkHooks.openScreen(sp,
                    new SimpleMenuProvider((id, inv, p) ->
                            new TalismanBoxMenu(id, inv, box),
                            box.getHoverName()),
                    buf -> {});
        }
        level.playSound(player, player.blockPosition(),
                SoundEvents.ENDER_CHEST_OPEN, SoundSource.PLAYERS, 0.6f, 1.5f);
        return InteractionResultHolder.sidedSuccess(box, level.isClientSide());
    }

    /**
     * 从NBT "Items" 列表读取符咒类型（不改原API，内部改用ItemStack解析）
     */
    public static Set<TalismanType> getStoredTalismans(ItemStack stack) {
        Set<TalismanType> result = EnumSet.noneOf(TalismanType.class);
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(TAG_ITEMS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag slotTag = list.getCompound(i);
                if (slotTag.contains("item")) {
                    ItemStack s = ItemStack.of(slotTag.getCompound("item"));
                    if (s.getItem() instanceof TalismanItem t)
                        result.add(t.getTalismanType());
                }
            }
        }
        return result;
    }

    public static boolean isHoldingBox(Player player) {
        return player.getMainHandItem().getItem() instanceof TalismanBoxItem
                || player.getOffhandItem().getItem() instanceof TalismanBoxItem;
    }
}
