package com.kirasame.faautomation.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A non-placeable fluid representing one of the four forge essences, so essences
 * can be stored and transported by mods that work with fluids.
 *
 * @author marisa
 * @since 2026-10-17
 */
public class EssenceFluid extends Fluid {

    private final Supplier<? extends FluidType> fluidType;

    public EssenceFluid(Supplier<? extends FluidType> fluidType) {
        this.fluidType = fluidType;
    }

    @Override
    public FluidType getFluidType() {
        return this.fluidType.get();
    }

    @Override
    public Item getBucket() {
        return Items.AIR;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    protected Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState state) {
        return Vec3.ZERO;
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return 0;
    }

    @Override
    protected float getExplosionResistance() {
        return 0.0F;
    }

    @Override
    public float getHeight(FluidState state, BlockGetter level, BlockPos pos) {
        return 0.0F;
    }

    @Override
    public float getOwnHeight(FluidState state) {
        return 0.0F;
    }

    @Override
    public boolean isSource(FluidState state) {
        return true;
    }

    @Override
    public int getAmount(FluidState state) {
        return 8;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.empty();
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }
}
