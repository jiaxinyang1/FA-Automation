package com.kirasame.faautomation.fluidhandler;

import com.kirasame.faautomation.fluid.ModFluids;
import com.stal111.forbidden_arcanus.common.block.entity.EssenceUtremJarBlockEntity;
import com.stal111.forbidden_arcanus.common.block.entity.forge.essence.EssenceType;
import com.stal111.forbidden_arcanus.common.block.properties.ModBlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Fluid handler for an essence utrem jar, exposing it as a single tank whose
 * fluid matches the jar's essence type. One millibucket corresponds to one
 * essence unit.
 *
 * @author marisa
 * @since 2026-10-17
 */
public class UtremJarFluidHandler implements IFluidHandler {

    private final EssenceUtremJarBlockEntity blockEntity;

    public UtremJarFluidHandler(EssenceUtremJarBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return tank == 0 ? new FluidStack(ModFluids.getFluid(this.getEssenceType()), this.blockEntity.getAmount()) : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? this.blockEntity.getLimit() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tank == 0 && stack.getFluid() == ModFluids.getFluid(this.getEssenceType());
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.getFluid() != ModFluids.getFluid(this.getEssenceType())) {
            return 0;
        }

        int filled = Math.min(this.blockEntity.getLimit() - this.blockEntity.getAmount(), resource.getAmount());

        if (filled > 0 && action.execute()) {
            this.blockEntity.addEssence(filled);
        }

        return filled;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.getFluid() != ModFluids.getFluid(this.getEssenceType())) {
            return FluidStack.EMPTY;
        }

        int drained = Math.min(this.blockEntity.getAmount(), resource.getAmount());

        if (drained <= 0) {
            return FluidStack.EMPTY;
        }

        if (action.execute()) {
            this.blockEntity.addEssence(-drained);
        }

        return new FluidStack(ModFluids.getFluid(this.getEssenceType()), drained);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return this.drain(new FluidStack(ModFluids.getFluid(this.getEssenceType()), maxDrain), action);
    }

    private EssenceType getEssenceType() {
        return this.blockEntity.getBlockState().getValue(ModBlockStateProperties.ESSENCE_TYPE);
    }
}
