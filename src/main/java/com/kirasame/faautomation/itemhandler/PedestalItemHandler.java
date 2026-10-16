package com.kirasame.faautomation.itemhandler;

import com.stal111.forbidden_arcanus.common.block.entity.PedestalBlockEntity;
import com.stal111.forbidden_arcanus.common.block.entity.forge.HephaestusForgeBlockEntity;
import com.stal111.forbidden_arcanus.common.block.pedestal.effect.PedestalEffectTrigger;
import com.stal111.forbidden_arcanus.core.init.ModBlockEntities;
import com.stal111.forbidden_arcanus.core.init.other.ModPOITypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Item handler for pedestals. Insertions and extractions go through the
 * pedestal's setStack path (using triggers that update the forge ingredient
 * cache without side effects like entity summons), and both are blocked while
 * a ritual on the nearby forge is running, since changing pedestal items
 * would make it fail.
 *
 * @author marisa
 * @since 2026-10-17
 */
public class PedestalItemHandler implements IItemHandler {

    private final PedestalBlockEntity blockEntity;

    @Nullable
    private HephaestusForgeBlockEntity forgeBlockEntity;

    public PedestalItemHandler(PedestalBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return slot == 0 ? this.blockEntity.getStack() : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot != 0 || stack.isEmpty() || this.blockEntity.hasStack() || this.isRitualActive()) {
            return stack;
        }

        if (!simulate) {
            this.blockEntity.setStack(stack.copyWithCount(1), null, PedestalEffectTrigger.MAGNETIZED_PICKUP);
        }

        return stack.getCount() == 1 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - 1);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = this.getStackInSlot(slot);

        if (stack.isEmpty() || this.isRitualActive()) {
            return ItemStack.EMPTY;
        }

        if (!simulate) {
            this.blockEntity.clearStack(null, PedestalEffectTrigger.PLAYER_REMOVE_ITEM);
        }

        return stack.copy();
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return slot == 0;
    }

    private boolean isRitualActive() {
        if (!(this.blockEntity.getLevel() instanceof ServerLevel serverLevel)) {
            return false;
        }

        if (this.forgeBlockEntity == null || this.forgeBlockEntity.isRemoved()) {
            BlockPos forgePos = serverLevel.getPoiManager()
                    .findClosest(holder -> holder.value() == ModPOITypes.HEPHAESTUS_FORGE.get(), this.blockEntity.getBlockPos(), 4, PoiManager.Occupancy.ANY)
                    .orElse(null);

            this.forgeBlockEntity = forgePos != null ? serverLevel.getBlockEntity(forgePos, ModBlockEntities.HEPHAESTUS_FORGE.get()).orElse(null) : null;
        }

        return this.forgeBlockEntity != null && this.forgeBlockEntity.getRitualManager().isRitualActive();
    }
}
