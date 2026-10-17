package com.kirasame.faautomation.event;

import com.kirasame.faautomation.automode.AutoModeData;
import com.stal111.forbidden_arcanus.common.block.EssenceUtremJarBlock;
import com.stal111.forbidden_arcanus.common.block.HephaestusForgeBlock;
import com.stal111.forbidden_arcanus.common.block.entity.EssenceUtremJarBlockEntity;
import com.stal111.forbidden_arcanus.common.block.entity.forge.essence.EssenceType;
import com.stal111.forbidden_arcanus.core.init.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Two interactions:
 * - Sneak-right-clicking the Hephaestus Forge with a Blacksmith Gavel (any item
 *   with the ritual starter component) toggles auto mode for that forge.
 * - Sneak-right-clicking an empty essence utrem jar with an empty hand cycles
 *   the essence type it stores. Jars with essence inside cannot be switched.
 *
 * @author marisa
 * @since 2026-10-17
 */
public class InteractionEvents {

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (this.cycleJarType(event, player)) {
            return;
        }

        this.toggleAutoMode(event, player);
    }

    private boolean cycleJarType(PlayerInteractEvent.RightClickBlock event, Player player) {
        if (!player.isSecondaryUseActive() || !event.getItemStack().isEmpty() || !(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof EssenceUtremJarBlock)) {
            return false;
        }

        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof EssenceUtremJarBlockEntity jarEntity) || jarEntity.getAmount() != 0) {
            return false;
        }

        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        event.setCanceled(true);

        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BlockState state = serverLevel.getBlockState(event.getPos());
            EssenceType[] types = EssenceType.values();
            EssenceType next = types[(state.getValue(EssenceUtremJarBlock.ESSENCE_TYPE).ordinal() + 1) % types.length];

            serverLevel.setBlockAndUpdate(event.getPos(), state.setValue(EssenceUtremJarBlock.ESSENCE_TYPE, next));

            serverLevel.playSound(null, event.getPos(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.6F, 1.0F);
            player.displayClientMessage(Component.translatable("message.fa_automation.jar_type_set", next.getComponent()), true);
        }

        return true;
    }

    private void toggleAutoMode(PlayerInteractEvent.RightClickBlock event, Player player) {
        boolean hasStarter = event.getItemStack().get(ModDataComponents.RITUAL_STARTER.get()) != null;
        boolean isForge = event.getLevel().getBlockState(event.getPos()).getBlock() instanceof HephaestusForgeBlock;

        if (!hasStarter || !isForge || !player.isSecondaryUseActive()) {
            return;
        }

        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        event.setCanceled(true);

        if (event.getLevel() instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            boolean enabled = AutoModeData.get(serverLevel).toggle(serverLevel, event.getPos(), serverPlayer);

            serverLevel.playSound(null, event.getPos(), enabled ? SoundEvents.EXPERIENCE_ORB_PICKUP : SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 0.8F, enabled ? 0.9F : 0.7F);
            serverPlayer.displayClientMessage(Component.translatable(enabled ? "block.fa_automation.auto_mode_enabled" : "block.fa_automation.auto_mode_disabled"), true);
        }
    }
}
