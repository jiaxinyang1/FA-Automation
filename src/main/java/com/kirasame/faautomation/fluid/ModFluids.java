package com.kirasame.faautomation.fluid;

import com.kirasame.faautomation.FAAutomation;
import com.stal111.forbidden_arcanus.common.block.entity.forge.essence.EssenceType;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Registers one non-placeable fluid per forge essence. Names reuse the essence
 * translation keys of Forbidden Arcanus; textures reuse the jar liquid textures
 * (the souls textures are shipped with this addon since the mod has none).
 *
 * @author marisa
 * @since 2026-10-17
 */
public class ModFluids {

    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, FAAutomation.MOD_ID);
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, FAAutomation.MOD_ID);

    public static final Supplier<FluidType> AUREAL_TYPE = FLUID_TYPES.register("aureal", () -> new EssenceFluidType(EssenceType.AUREAL, false));
    public static final Supplier<FluidType> SOULS_TYPE = FLUID_TYPES.register("souls", () -> new EssenceFluidType(EssenceType.SOULS, true));
    public static final Supplier<FluidType> BLOOD_TYPE = FLUID_TYPES.register("blood", () -> new EssenceFluidType(EssenceType.BLOOD, false));
    public static final Supplier<FluidType> EXPERIENCE_TYPE = FLUID_TYPES.register("experience", () -> new EssenceFluidType(EssenceType.EXPERIENCE, false));

    public static final Supplier<Fluid> AUREAL = FLUIDS.register("aureal", () -> new EssenceFluid(AUREAL_TYPE));
    public static final Supplier<Fluid> SOULS = FLUIDS.register("souls", () -> new EssenceFluid(SOULS_TYPE));
    public static final Supplier<Fluid> BLOOD = FLUIDS.register("blood", () -> new EssenceFluid(BLOOD_TYPE));
    public static final Supplier<Fluid> EXPERIENCE = FLUIDS.register("experience", () -> new EssenceFluid(EXPERIENCE_TYPE));

    public static void register(IEventBus modEventBus) {
        FLUID_TYPES.register(modEventBus);
        FLUIDS.register(modEventBus);
    }

    public static Fluid getFluid(EssenceType type) {
        return switch (type) {
            case AUREAL -> AUREAL.get();
            case SOULS -> SOULS.get();
            case BLOOD -> BLOOD.get();
            case EXPERIENCE -> EXPERIENCE.get();
        };
    }

    public static EssenceType getEssenceType(Fluid fluid) {
        for (EssenceType type : EssenceType.values()) {
            if (getFluid(type) == fluid) {
                return type;
            }
        }

        return null;
    }

    private static class EssenceFluidType extends FluidType {

        private final ResourceLocation stillTexture;
        private final ResourceLocation flowingTexture;

        EssenceFluidType(EssenceType type, boolean ownTexture) {
            super(FluidType.Properties.create().descriptionId(Util.makeDescriptionId("essence", ResourceLocation.fromNamespaceAndPath("forbidden_arcanus", type.getSerializedName()))));

            String path = (ownTexture ? "fluid/" : "block/liquid/") + type.getSerializedName();
            String namespace = ownTexture ? FAAutomation.MOD_ID : "forbidden_arcanus";
            this.stillTexture = ResourceLocation.fromNamespaceAndPath(namespace, path + "_still");
            this.flowingTexture = ResourceLocation.fromNamespaceAndPath(namespace, path + "_flow");
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return stillTexture;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return flowingTexture;
                }
            });
        }
    }
}
