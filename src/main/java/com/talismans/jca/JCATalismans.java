package com.talismans.jca;

import com.mojang.logging.LogUtils;
import com.talismans.jca.event.ModEvents;
import com.talismans.jca.gui.ModContainers;
import com.talismans.jca.gui.TalismanBoxScreen;
import com.talismans.jca.item.ModCreativeTab;
import com.talismans.jca.item.ModItems;
import com.talismans.jca.network.ModNetwork;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(JCATalismans.MOD_ID)
public class JCATalismans {
    public static final String MOD_ID = "jca";
    private static final Logger LOGGER = LogUtils.getLogger();

    public JCATalismans() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModCreativeTab.register(modEventBus);
        ModContainers.CONTAINERS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ModEvents());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModNetwork.register();
        LOGGER.info("JCA Talismans mod initialized! All 12 talismans ready.");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModContainers.TALISMAN_BOX.get(), TalismanBoxScreen::new);
        });
    }
}
