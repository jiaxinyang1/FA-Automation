package com.kirasame.faautomation.event;

import com.kirasame.faautomation.automode.AutoModeData;
import com.kirasame.faautomation.itemhandler.RitualResultTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * @author marisa
 * @since 2026-10-17
 */
public class ServerTickHandler {

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        for (ServerLevel level : server.getAllLevels()) {
            AutoModeData.get(level).tick(level);
        }

        RitualResultTracker.tick();
    }
}
