package com.talismans.jca.gui;

import com.talismans.jca.JCATalismans;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModContainers {
    public static final DeferredRegister<MenuType<?>> CONTAINERS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, JCATalismans.MOD_ID);

    public static final RegistryObject<MenuType<TalismanBoxMenu>> TALISMAN_BOX =
            CONTAINERS.register("talisman_box",
                    () -> IForgeMenuType.create(TalismanBoxMenu::new));
}
