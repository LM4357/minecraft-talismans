package com.talismans.jca.item;

import com.talismans.jca.JCATalismans;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JCATalismans.MOD_ID);

    public static final RegistryObject<CreativeModeTab> JCA_TALISMANS_TAB =
            CREATIVE_MODE_TABS.register("jca_talismans_tab",
                    () -> CreativeModeTab.builder()
                            .icon(() -> new ItemStack(ModItems.TALISMAN_BOX.get()))
                            .title(Component.translatable("itemGroup.jca_talismans"))
                            .displayItems((params, output) -> {
                                output.accept(ModItems.TALISMAN_BOX.get());
                                output.accept(ModItems.RAT_TALISMAN.get());
                                output.accept(ModItems.OX_TALISMAN.get());
                                output.accept(ModItems.TIGER_TALISMAN.get());
                                output.accept(ModItems.RABBIT_TALISMAN.get());
                                output.accept(ModItems.DRAGON_TALISMAN.get());
                                output.accept(ModItems.SNAKE_TALISMAN.get());
                                output.accept(ModItems.HORSE_TALISMAN.get());
                                output.accept(ModItems.SHEEP_TALISMAN.get());
                                output.accept(ModItems.MONKEY_TALISMAN.get());
                                output.accept(ModItems.ROOSTER_TALISMAN.get());
                                output.accept(ModItems.DOG_TALISMAN.get());
                                output.accept(ModItems.PIG_TALISMAN.get());
                            })
                            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
