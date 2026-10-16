package com.kirasame.faautomation.fluidhandler;

import com.kirasame.faautomation.fluid.ModFluids;
import com.stal111.forbidden_arcanus.common.block.HephaestusForgeBlock;
import com.stal111.forbidden_arcanus.common.block.entity.forge.HephaestusForgeBlockEntity;
import com.stal111.forbidden_arcanus.common.block.entity.forge.HephaestusForgeLevel;
import com.stal111.forbidden_arcanus.common.block.entity.forge.essence.EssenceManager;
import com.stal111.forbidden_arcanus.common.block.entity.forge.essence.EssenceType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Fluid handler for the Hephaestus Forge, exposing the four essence storages
 * as fluid tanks. One millibucket corresponds to one essence unit.
 *
 * @author marisa
 * @since 2026-10-17
 */
public class ForgeFluidHandler implements IFluidHandler {

    private final HephaestusForgeBlockEntity blockEntity;

    public ForgeFluidHandler(HephaestusForgeBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getTanks() {
        return EssenceType.values().length;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        EssenceType type = this.getEssenceType(tank);

        return new FluidStack(ModFluids.getFluid(type), this.blockEntity.getEssenceManager().getEssence(type));
    }

    @Override
    public int getTankCapacity(int tank) {
        EssenceType type = this.getEssenceType(tank);

        if (this.blockEntity.getBlockState().getBlock() instanceof HephaestusForgeBlock forgeBlock) {
            return forgeBlock.getLevel().getMaxAmount(type);
        }

        return HephaestusForgeLevel.ONE.getMaxAmount(type);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return ModFluids.getFluid(this.getEssenceType(tank)) == stack.getFluid();
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        EssenceType type = ModFluids.getEssenceType(resource.getFluid());

        if (type == null) {
            return 0;
        }

        EssenceManager manager = this.blockEntity.getEssenceManager();
        int filled = Math.min(this.getTankCapacity(type.ordinal()) - manager.getEssence(type), resource.getAmount());

        if (filled > 0 && action.execute()) {
            manager.increaseEssence(type, filled);

            this.blockEntity.setChanged();
        }

        return filled;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        EssenceType type = ModFluids.getEssenceType(resource.getFluid());

        if (type == null) {
            return FluidStack.EMPTY;
        }

        EssenceManager manager = this.blockEntity.getEssenceManager();
        int drained = Math.min(manager.getEssence(type), resource.getAmount());

        if (drained <= 0) {
            return FluidStack.EMPTY;
        }

        if (action.execute()) {
            manager.setEssence(type, manager.getEssence(type) - drained);

            this.blockEntity.setChanged();
        }

        return new FluidStack(ModFluids.getFluid(type), drained);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        for (EssenceType type : EssenceType.values()) {
            if (this.blockEntity.getEssenceManager().getEssence(type) > 0) {
                return this.drain(new FluidStack(ModFluids.getFluid(type), maxDrain), action);
            }
        }

        return FluidStack.EMPTY;
    }

    private EssenceType getEssenceType(int tank) {
        return EssenceType.values()[tank];
    }
}
