package com.kirasame.faautomation;

import com.kirasame.faautomation.event.CapabilityEvents;
import com.kirasame.faautomation.event.InteractionEvents;
import com.kirasame.faautomation.event.ServerTickHandler;
import com.kirasame.faautomation.fluid.ModFluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forbidden Arcanus Automation <br>
 * An addon that adds item and fluid automation support to Forbidden Arcanus
 * without modifying the original mod.
 *
 * @author marisa
 * @since 2026-10-17
 */
@Mod(FAAutomation.MOD_ID)
public class FAAutomation {

    public static final String MOD_ID = "fa_automation";
    public static final String VERSION = "1.0.3";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public FAAutomation(IEventBus modEventBus) {
        ModFluids.register(modEventBus);

        // RegisterCapabilitiesEvent is a mod-bus event
        modEventBus.register(new CapabilityEvents());

        NeoForge.EVENT_BUS.register(new InteractionEvents());
        NeoForge.EVENT_BUS.register(new ServerTickHandler());
    }
}
