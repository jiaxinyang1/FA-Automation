package com.kirasame.faautomation.automode;

import com.stal111.forbidden_arcanus.common.block.HephaestusForgeBlock;
import com.stal111.forbidden_arcanus.common.block.entity.forge.HephaestusForgeBlockEntity;
import com.stal111.forbidden_arcanus.core.init.ModBlockEntities;
import com.stal111.forbidden_arcanus.core.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Persistently stores which Hephaestus Forges have auto mode enabled (toggled
 * by sneak-right-clicking with a Blacksmith Gavel) and which player enabled
 * them. While the enabling player is online, such forges automatically start
 * a ritual whenever one is valid.
 *
 * @author marisa
 * @since 2026-10-17
 */
public class AutoModeData extends SavedData {

    private static final String DATA_NAME = "fa_automation_auto_mode";

    private final Map<BlockPos, UUID> enabledForges = new HashMap<>();

    public static AutoModeData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(AutoModeData::new, AutoModeData::load, null), DATA_NAME);
    }

    public boolean toggle(ServerLevel level, BlockPos pos, ServerPlayer player) {
        if (this.enabledForges.containsKey(pos)) {
            this.enabledForges.remove(pos);
        } else {
            this.enabledForges.put(pos, player.getUUID());
        }

        this.setDirty();

        return this.isEnabled(pos);
    }

    public boolean isEnabled(BlockPos pos) {
        return this.enabledForges.containsKey(pos);
    }

    public void tick(ServerLevel level) {
        if (this.enabledForges.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<BlockPos, UUID>> iterator = this.enabledForges.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, UUID> entry = iterator.next();
            BlockPos pos = entry.getKey();

            if (!(level.getBlockEntity(pos, ModBlockEntities.HEPHAESTUS_FORGE.get()).orElse(null) instanceof HephaestusForgeBlockEntity blockEntity)) {
                iterator.remove();
                this.setDirty();
                continue;
            }

            if (!blockEntity.getBlockState().getValue(HephaestusForgeBlock.ACTIVATED) || blockEntity.getRitualManager().isRitualActive() || blockEntity.getRitualManager().getValidRitual().isEmpty()) {
                continue;
            }

            // The vanilla startRitual call requires a player; rituals are attributed to the player who enabled auto mode
            if (!(level.getPlayerByUUID(entry.getValue()) instanceof ServerPlayer player)) {
                continue;
            }

            if (blockEntity.getRitualManager().startRitual(player, blockEntity.getEssenceManager().getStorage())) {
                level.playSound(null, pos, ModSounds.BLACKSMITH_GAVEL_RITUAL_START.get(), SoundSource.BLOCKS, 0.85F, level.getRandom().nextFloat() * 0.15F + 0.9F);
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        ListTag list = new ListTag();

        for (Map.Entry<BlockPos, UUID> entry : this.enabledForges.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putLong("Pos", entry.getKey().asLong());
            entryTag.putUUID("Owner", entry.getValue());
            list.add(entryTag);
        }

        tag.put("Forges", list);

        return tag;
    }

    public static AutoModeData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        AutoModeData data = new AutoModeData();

        for (Tag entryTag : tag.getList("Forges", Tag.TAG_COMPOUND)) {
            CompoundTag compound = (CompoundTag) entryTag;

            if (compound.hasUUID("Owner")) {
                data.enabledForges.put(BlockPos.of(compound.getLong("Pos")), compound.getUUID("Owner"));
            }
        }

        return data;
    }
}
