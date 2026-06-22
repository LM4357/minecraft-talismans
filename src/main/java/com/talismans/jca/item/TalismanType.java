package com.talismans.jca.item;

import net.minecraft.ChatFormatting;

/**
 * 12符咒类型枚举 - 每种符咒的能力描述（更新后）
 */
public enum TalismanType {
    RAT("rat", ChatFormatting.GRAY,
            "鼠符咒 — 静与动",
            "§7赋予方块生命之力§r\n§8右键方块赋予1点生命§r\n§8打一下即碎并掉落其自身§r"),

    OX("ox", ChatFormatting.DARK_RED,
            "牛符咒 — 力",
            "§7赋予超级力量§r\n§8手持获得力量X效果§r\n§8近战攻击力提升10倍§r"),

    TIGER("tiger", ChatFormatting.YELLOW,
            "虎符咒 — 平衡",
            "§7精神与身体的完美平衡§r\n§8手持时免疫击退、缓降、跳跃提升§r\n§8获得抗性提升II效果§r"),

    RABBIT("rabbit", ChatFormatting.AQUA,
            "兔符咒 — 速度",
            "§7赋予超级速度§r\n§8手持获得速度XX效果（20级）§r\n§8与鸡符咒共用时飞行速度大幅提升§r"),

    DRAGON("dragon", ChatFormatting.GOLD,
            "龙符咒 — 爆破",
            "§7释放毁灭性的火焰爆破§r\n§8右键发射高速火龙弹§r\n§8爆炸伤害：10点（5颗心）§r"),

    SNAKE("snake", ChatFormatting.GREEN,
            "蛇符咒 — 隐形",
            "§7赋予完全隐身的能力（含盔甲）§r\n§8右键切换隐身状态§r\n§8隐身时全身不可见包括装备§r"),

    HORSE("horse", ChatFormatting.WHITE,
            "马符咒 — 治愈",
            "§7治愈一切伤痛与异常§r\n§8手持获得生命恢复IV效果§r\n§8自动清除所有负面效果§r"),

    SHEEP("sheep", ChatFormatting.LIGHT_PURPLE,
            "羊符咒 — 灵魂出窍",
            "§7灵魂脱离肉体自由行动§r\n§8右键进入旁观模式§r\n§8再次右键返回身体和原始模式§r\n§8灵魂状态下可穿墙探索§r"),

    MONKEY("monkey", ChatFormatting.BLUE,
            "猴符咒 — 变形",
            "§7将生物变成其他动物§r\n§8右键生物将其随机变形§r\n§8在潜行时右键可变为羊§r"),

    ROOSTER("rooster", ChatFormatting.RED,
            "鸡符咒 — 悬浮",
            "§7赋予浮空与飞行之力§r\n§8右键切换飞行模式§r\n§8与兔符咒共用时飞行速度极快§r"),

    DOG("dog", ChatFormatting.DARK_PURPLE,
            "狗符咒 — 永生",
            "§7赐予不死不灭之力§r\n§8手持时免疫死亡（无冷却）§r\n§8受到致命伤害时保留1点生命§r"),

    PIG("pig", ChatFormatting.DARK_AQUA,
            "猪符咒 — 热视力",
            "§7从双眼射出灼热射线§r\n§8右键发射远距离激光束§r\n§8持续伤害并点燃目标§r");

    private final String name;
    private final ChatFormatting color;
    private final String displayName;
    private final String description;

    TalismanType(String name, ChatFormatting color, String displayName, String description) {
        this.name = name;
        this.color = color;
        this.displayName = displayName;
        this.description = description;
    }

    public String getName() { return name; }
    public ChatFormatting getColor() { return color; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
