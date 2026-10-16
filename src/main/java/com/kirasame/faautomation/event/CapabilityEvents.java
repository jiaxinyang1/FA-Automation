package com.kirasame.faautomation.event;

import com.kirasame.faautomation.fluidhandler.ForgeFluidHandler;
import com.kirasame.faautomation.fluidhandler.UtremJarFluidHandler;
import com.kirasame.faautomation.itemhandler.ForgeItemHandler;
import com.kirasame.faautomation.itemhandler.PedestalItemHandler;
import com.kirasame.faautomation.itemhandler.RitualResultTracker;
import com.stal111.forbidden_arcanus.common.block.entity.EssenceUtremJarBlockEntity;
import com.stal111.forbidden_arcanus.common.block.entity.PedestalBlockEntity;
import com.stal111.forbidden_arcanus.common.block.entity.clibano.ClibanoItemHandler;
import com.stal111.forbidden_arcanus.common.block.entity.clibano.ClibanoMainBlockEntity;
import com.stal111.forbidden_arcanus.common.block.entity.forge.HephaestusForgeBlockEntity;
import com.stal111.forbidden_arcanus.core.init.ModBlockEntities;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Registers item and fluid handler capabilities for the Forbidden Arcanus
 * block entities, using only public APIs of the mod.
 *
 * @author marisa
 * @since 2026-10-17
 */
public class CapabilityEvents {

    private static final Map<HephaestusForgeBlockEntity, ForgeItemHandler> FORGE_ITEM_HANDLERS = new WeakHashMap<>();
    private static final Map<HephaestusForgeBlockEntity, ForgeFluidHandler> FORGE_FLUID_HANDLERS = new WeakHashMap<>();
    private static final Map<PedestalBlockEntity, PedestalItemHandler> PEDESTAL_ITEM_HANDLERS = new WeakHashMap<>();
    private static final Map<EssenceUtremJarBlockEntity, UtremJarFluidHandler> JAR_FLUID_HANDLERS = new WeakHashMap<>();
    private static final Map<ClibanoMainBlockEntity, EnumMap<Direction, ClibanoItemHandler>> CLIBANO_ITEM_HANDLERS = new WeakHashMap<>();

    @SubscribeEvent
    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.HEPHAESTUS_FORGE.get(), (blockEntity, side) -> {
            RitualResultTracker.track(blockEntity);

            return FORGE_ITEM_HANDLERS.computeIfAbsent(blockEntity, be -> new ForgeItemHandler(be, be.getItemStackHandler()));
        });

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.HEPHAESTUS_FORGE.get(), (blockEntity, side) ->
                FORGE_FLUID_HANDLERS.computeIfAbsent(blockEntity, ForgeFluidHandler::new));

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.PEDESTAL.get(), (blockEntity, side) ->
                PEDESTAL_ITEM_HANDLERS.computeIfAbsent(blockEntity, PedestalItemHandler::new));

        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.ESSENCE_UTREM_JAR.get(), (blockEntity, side) ->
                JAR_FLUID_HANDLERS.computeIfAbsent(blockEntity, UtremJarFluidHandler::new));

        // Reuses the mod's own ClibanoItemHandler: top face = smelting inputs, bottom face = results, horizontal sides = fuel and soul items
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CLIBANO_MAIN.get(), (blockEntity, side) -> {
            if (side == null) {
                return null;
            }

            EnumMap<Direction, ClibanoItemHandler> handlers = CLIBANO_ITEM_HANDLERS.computeIfAbsent(blockEntity, be -> new EnumMap<>(Direction.class));

            return handlers.computeIfAbsent(side, dir -> switch (dir) {
                case UP -> new ClibanoItemHandler(blockEntity.getItemStackHandler(), Direction.UP);
                case DOWN -> new ClibanoItemHandler(blockEntity.getItemStackHandler(), Direction.DOWN);
                default -> new ClibanoItemHandler(blockEntity.getItemStackHandler(), Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
            });
        });
    }
}
