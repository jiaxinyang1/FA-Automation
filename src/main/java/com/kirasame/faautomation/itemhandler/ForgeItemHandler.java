package com.kirasame.faautomation.itemhandler;

import com.stal111.forbidden_arcanus.common.block.entity.forge.HephaestusForgeBlockEntity;
import com.stal111.forbidden_arcanus.common.block.entity.forge.essence.EssenceType;
import com.stal111.forbidden_arcanus.core.registry.FARegistries;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Item handler for the Hephaestus Forge.
 *
 * @author marisa
 * @since 2026-10-17
 */
public class ForgeItemHandler implements IItemHandler {

    private static final Map<Integer, EssenceType> ESSENCE_TYPE_FROM_SLOT_MAP = Util.make(new HashMap<>(), map -> {
        HephaestusForgeBlockEntity.SLOT_FROM_ESSENCE_TYPE_MAP.forEach((essenceType, slot) -> map.put(slot, essenceType));
    });

    private final HephaestusForgeBlockEntity blockEntity;
    private final IItemHandler itemHandler;

    @Nullable
    private final Direction side;

    /**
     * @param side the face the interaction comes from: the top face feeds the main
     *             crafting slot, all other faces (and no side) feed the essence input slots
     */
    public ForgeItemHandler(HephaestusForgeBlockEntity blockEntity, IItemHandler itemHandler, @Nullable Direction side) {
        this.blockEntity = blockEntity;
        this.itemHandler = itemHandler;
        this.side = side;
    }

    public static boolean canInput(Level level, EssenceType type, ItemStack stack) {
        return level.registryAccess().registryOrThrow(FARegistries.FORGE_INPUT).holders()
                .map(Holder.Reference::value)
                .anyMatch(input -> input.canInput(type, stack));
    }

    @Override
    public int getSlots() {
        return this.itemHandler.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return this.itemHandler.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return stack;
        }

        if (slot == HephaestusForgeBlockEntity.MAIN_SLOT) {
            if (this.side != Direction.UP) {
                return stack;
            }

            if (this.blockEntity.getRitualManager().isRitualActive() || !this.itemHandler.getStackInSlot(slot).isEmpty()) {
                return stack;
            }

            if (!simulate) {
                this.blockEntity.setStack(slot, stack.copyWithCount(1));
            }

            return stack.getCount() == 1 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - 1);
        }

        EssenceType essenceType = ESSENCE_TYPE_FROM_SLOT_MAP.get(slot);
        Level level = this.blockEntity.getLevel();

        if (this.side == Direction.UP || essenceType == null || level == null || !canInput(level, essenceType, stack)) {
            return stack;
        }

        return this.itemHandler.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == HephaestusForgeBlockEntity.MAIN_SLOT) {
            // Only the ritual result may be pulled out, never an ingredient that is waiting for the ritual to start
            if (this.blockEntity.getRitualManager().isRitualActive() || !RitualResultTracker.isRitualResult(this.blockEntity)) {
                return ItemStack.EMPTY;
            }
        } else if (!ESSENCE_TYPE_FROM_SLOT_MAP.containsKey(slot)) {
            return ItemStack.EMPTY;
        }

        return this.itemHandler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot == HephaestusForgeBlockEntity.MAIN_SLOT) {
            return 1;
        }

        return this.itemHandler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot == HephaestusForgeBlockEntity.MAIN_SLOT) {
            return this.side == Direction.UP;
        }

        EssenceType essenceType = ESSENCE_TYPE_FROM_SLOT_MAP.get(slot);
        Level level = this.blockEntity.getLevel();

        return this.side != Direction.UP && essenceType != null && level != null && canInput(level, essenceType, stack);
    }
}
