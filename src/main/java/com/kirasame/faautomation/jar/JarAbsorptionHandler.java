package com.kirasame.faautomation.jar;

import com.stal111.forbidden_arcanus.common.block.entity.EssenceUtremJarBlockEntity;
import com.stal111.forbidden_arcanus.core.init.ModBlocks;
import com.stal111.forbidden_arcanus.core.init.other.ModPOITypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Automatic essence generation for essence utrem jars (jar POIs of the mod are
 * used to find nearby jars without tracking them):
 *
 * - Blood jars absorb essence when a living entity dies nearby
 *   (max health x factor per death).
 * - Experience jars pull in nearby experience orbs and absorb their value
 *   x factor, shrinking orbs that would overflow the jar.
 * - Souls jars slowly extract essence from soul sand / soul soil in a 5x5x5
 *   area around them, turning it into soulless sand.
 *
 * @author marisa
 * @since 2026-10-17
 */
public class JarAbsorptionHandler {

    private static final int BLOOD_FACTOR = 10;
    private static final int SOULS_FACTOR = 10;
    private static final int EXPERIENCE_FACTOR = 10;

    private static final int DEATH_ABSORB_RADIUS = 4;
    private static final int ORB_SCAN_INTERVAL = 3;
    private static final int ORB_MAGNET_RADIUS = 8;
    private static final int SOULS_TICK_INTERVAL = 40;
    private static final int SOULS_PLAYER_RADIUS = 48;

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        level.getPoiManager().getInRange(holder -> holder.value() == ModPOITypes.BLOOD_UTREM_JAR.get(), entity.blockPosition(), DEATH_ABSORB_RADIUS, PoiManager.Occupancy.ANY).forEach(poi -> {
            if (level.getBlockEntity(poi.getPos()) instanceof EssenceUtremJarBlockEntity jar && jar.getAmount() < jar.getLimit()) {
                jar.addEssence((int) (entity.getMaxHealth() * BLOOD_FACTOR));

                level.sendBlockUpdated(poi.getPos(), jar.getBlockState(), jar.getBlockState(), 3);
            }
        });
    }

    @SubscribeEvent
    public void onOrbTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ExperienceOrb orb) || !(orb.level() instanceof ServerLevel level) || orb.tickCount % ORB_SCAN_INTERVAL != 0) {
            return;
        }

        level.getPoiManager().findAllClosestFirstWithType(holder -> holder.value() == ModPOITypes.EXPERIENCE_UTREM_JAR.get(), pos -> true, orb.blockPosition(), ORB_MAGNET_RADIUS, PoiManager.Occupancy.ANY).findFirst().ifPresent(pair -> {
            BlockPos jarPos = pair.getSecond();

            if (!(level.getBlockEntity(jarPos) instanceof EssenceUtremJarBlockEntity jar) || jar.getAmount() >= jar.getLimit()) {
                return;
            }

            Vec3 center = Vec3.atCenterOf(jarPos);

            if (orb.position().distanceTo(center) < 1.2D) {
                int essence = orb.getValue() * EXPERIENCE_FACTOR;
                int space = jar.getLimit() - jar.getAmount();

                if (essence <= space) {
                    jar.addEssence(essence);

                    orb.discard();
                } else {
                    int absorbedValue = space / EXPERIENCE_FACTOR;

                    jar.addEssence(absorbedValue * EXPERIENCE_FACTOR);

                    orb.value -= absorbedValue;
                }

                level.sendBlockUpdated(jarPos, jar.getBlockState(), jar.getBlockState(), 3);
            } else {
                Vec3 motion = center.subtract(orb.position()).normalize().scale(0.06D);

                orb.setDeltaMovement(motion);
                orb.hasImpulse = true;
            }
        });
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.getGameTime() % SOULS_TICK_INTERVAL != 0 || level.players().isEmpty()) {
                continue;
            }

            Set<BlockPos> jars = new HashSet<>();

            for (ServerPlayer player : level.players()) {
                level.getPoiManager().getInSquare(holder -> holder.value() == ModPOITypes.SOULS_UTREM_JAR.get(), player.blockPosition(), SOULS_PLAYER_RADIUS, PoiManager.Occupancy.ANY).forEach(poi -> jars.add(poi.getPos()));
            }

            for (BlockPos pos : jars) {
                if (!(level.getBlockEntity(pos) instanceof EssenceUtremJarBlockEntity jar) || jar.getAmount() >= jar.getLimit()) {
                    continue;
                }

                BlockPos target = pos.offset(level.random.nextInt(5) - 2, level.random.nextInt(5) - 2, level.random.nextInt(5) - 2);
                BlockState state = level.getBlockState(target);

                if (state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL)) {
                    level.setBlockAndUpdate(target, ModBlocks.SOULLESS_SAND.get().defaultBlockState());

                    jar.addEssence(SOULS_FACTOR);

                    level.sendBlockUpdated(pos, jar.getBlockState(), jar.getBlockState(), 3);
                }
            }
        }
    }
}
