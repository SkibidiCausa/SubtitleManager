package com.skibidicausa.subtitlemanager;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("subtitlemanager")
public class SubtitleManagerMod {

    public static final String MOD_ID = "subtitlemanager";
    public static final Logger LOGGER = LogManager.getLogger();

    public SubtitleManagerMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            SubtitleNetworking.register();
            LOGGER.info("Subtitle Manager Mod - Common Setup Complete");
        });
    }

    private void clientSetup(FMLClientSetupEvent event) {
        LOGGER.info("Subtitle Manager Mod - Client Setup Complete");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SubtitleCommand.register(event.getDispatcher());
        LOGGER.info("Subtitle Manager Command Registered");
    }
}