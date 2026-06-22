package com.talismans.jca.item;

import com.talismans.jca.JCATalismans;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 12符咒 + 符咒盒 物品注册
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, JCATalismans.MOD_ID);

    // 鼠符咒 - 静与动
    public static final RegistryObject<Item> RAT_TALISMAN = ITEMS.register("rat_talisman",
            () -> new TalismanItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC).fireResistant(), TalismanType.RAT));

    // 牛符咒 - 力
    public static final RegistryObject<Item> OX_TALISMAN = ITEMS.register("ox_talisman",
            () -> new TalismanItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC).fireResistant(), TalismanType.OX));

    // 虎符咒 - 平衡
    public static final RegistryObject<Item> TIGER_TALISMAN = ITEMS.register("tiger_talisman",
            () -> new TalismanItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC).fireResistant(), TalismanType.TIGER));

    // 兔符咒 - 速度
    public static final RegistryObject<Item> RABBIT_TALISMAN = ITEMS.register("rabbit_talisman",
            () -> new TalismanItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC).fireResistant(), TalismanType.RABBIT));

    // 龙符咒 - 爆破
    public static final RegistryObject<Item> DRAGON_TALISMAN = ITEMS.register("dragon_talisman",
            () -> new TalismanItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC).fireResistant(), TalismanType.DRAGON));

    // 蛇符咒 - 隐形
    public static final RegistryObject<Item> SNAKE_TALISMAN = ITEMS.register("snake_talisman",
            () -> new TalismanItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC).fireResistant(), TalismanType.SNAKE));

    // 马符咒 - 治愈
    public static final RegistryObject<Item> HORSE_TALISMAN = ITEMS.register("horse_talisman",
            () -> new TalismanItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC).fireResistant(), TalismanType.HORSE));

    // 羊符咒 - 灵魂出窍
    public static final RegistryObject<Item> SHEEP_TALISMAN = ITEMS.register("sheep_talisman",
            () -> new TalismanItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC).fireResistant(), TalismanType.SHEEP));

    // 猴符咒 - 变形
    public static final RegistryObject<Item> MONKEY_TALISMAN = ITEMS.register("monkey_talisman",
            () -> new TalismanItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC).fireResistant(), TalismanType.MONKEY));

    // 鸡符咒 - 悬浮
    public static final RegistryObject<Item> ROOSTER_TALISMAN = ITEMS.register("rooster_talisman",
            () -> new TalismanItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC).fireResistant(), TalismanType.ROOSTER));

    // 狗符咒 - 永生
    public static final RegistryObject<Item> DOG_TALISMAN = ITEMS.register("dog_talisman",
            () -> new TalismanItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC).fireResistant(), TalismanType.DOG));

    // 猪符咒 - 热视力
    public static final RegistryObject<Item> PIG_TALISMAN = ITEMS.register("pig_talisman",
            () -> new TalismanItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.EPIC).fireResistant(), TalismanType.PIG));

    // 符咒盒 - 收纳12符咒
    public static final RegistryObject<Item> TALISMAN_BOX = ITEMS.register("talisman_box",
            TalismanBoxItem::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
