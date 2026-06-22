package com.talismans.jca.gui;

import com.talismans.jca.item.TalismanBoxItem;
import com.talismans.jca.item.TalismanItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TalismanBoxMenu extends AbstractContainerMenu {
    private final Container boxInventory;
    private final ItemStack boxStack;
    public static final int SLOT_COUNT = 12;

    public TalismanBoxMenu(int id, Inventory playerInv, FriendlyByteBuf extra) {
        this(id, playerInv, playerInv.player.getMainHandItem().getItem() instanceof TalismanBoxItem
                ? playerInv.player.getMainHandItem()
                : playerInv.player.getOffhandItem());
    }

    public TalismanBoxMenu(int id, Inventory playerInv, ItemStack box) {
        super(ModContainers.TALISMAN_BOX.get(), id);
        this.boxStack = box;
        this.boxInventory = new SimpleContainer(SLOT_COUNT) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return stack.getItem() instanceof TalismanItem;
            }
            @Override
            public void setChanged() {
                super.setChanged();
                saveToNBT();
            }
        };

        loadFromNBT();

        // 3x4 紧凑格子，间距4px
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 4; c++)
                addSlot(new Slot(boxInventory, r * 4 + c, 20 + c * 24, 18 + r * 24) {
                    @Override
                    public boolean mayPlace(ItemStack s) {
                        return s.getItem() instanceof TalismanItem;
                    }
                });

        // 底部玩家快捷栏，便于拖入拖出
        for (int c = 0; c < 9; c++)
            addSlot(new Slot(playerInv, c, 8 + c * 18, 100));
    }

    private void loadFromNBT() {
        CompoundTag tag = boxStack.getOrCreateTag();
        if (tag.contains("Items", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(list.size(), SLOT_COUNT); i++) {
                CompoundTag slotTag = list.getCompound(i);
                int slot = slotTag.getInt("Slot");
                if (slot >= 0 && slot < SLOT_COUNT) {
                    boxInventory.setItem(slot, ItemStack.of(slotTag.getCompound("item")));
                }
            }
        }
    }

    private void saveToNBT() {
        CompoundTag tag = boxStack.getOrCreateTag();
        ListTag list = new ListTag();
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack s = boxInventory.getItem(i);
            if (!s.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putInt("Slot", i);
                slotTag.put("item", s.save(new CompoundTag()));
                list.add(slotTag);
            }
        }
        tag.put("Items", list);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, SLOT_COUNT, this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else {
                if (stack.getItem() instanceof TalismanItem) {
                    if (!this.moveItemStackTo(stack, 0, SLOT_COUNT, false))
                        return ItemStack.EMPTY;
                } else {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty())
                slot.setByPlayer(ItemStack.EMPTY);
            else
                slot.setChanged();
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        saveToNBT();
        super.removed(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return TalismanBoxItem.isHoldingBox(player);
    }
}
