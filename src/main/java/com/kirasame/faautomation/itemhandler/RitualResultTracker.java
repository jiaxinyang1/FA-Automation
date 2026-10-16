package com.kirasame.faautomation.itemhandler;

import com.stal111.forbidden_arcanus.common.block.entity.forge.HephaestusForgeBlockEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Tracks ritual state transitions of forges to know when the main slot holds a
 * finished ritual result. Only such results may be extracted by pipes, so an
 * ingredient waiting for a ritual to start cannot be pulled back out.
 *
 * @author marisa
 * @since 2026-10-17
 */
public class RitualResultTracker {

    private static final Map<HephaestusForgeBlockEntity, Boolean> RITUAL_WAS_ACTIVE = new WeakHashMap<>();
    private static final Map<HephaestusForgeBlockEntity, ItemStack> RESULT_SNAPSHOTS = new WeakHashMap<>();

    public static void track(HephaestusForgeBlockEntity blockEntity) {
        RITUAL_WAS_ACTIVE.putIfAbsent(blockEntity, blockEntity.getRitualManager().isRitualActive());
    }

    public static void tick() {
        Iterator<Map.Entry<HephaestusForgeBlockEntity, Boolean>> iterator = RITUAL_WAS_ACTIVE.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<HephaestusForgeBlockEntity, Boolean> entry = iterator.next();
            HephaestusForgeBlockEntity blockEntity = entry.getKey();

            if (blockEntity.isRemoved()) {
                iterator.remove();
                RESULT_SNAPSHOTS.remove(blockEntity);
                continue;
            }

            boolean active = blockEntity.getRitualManager().isRitualActive();

            if (entry.getValue() && !active) {
                RESULT_SNAPSHOTS.put(blockEntity, blockEntity.getStack(HephaestusForgeBlockEntity.MAIN_SLOT).copy());
            }

            entry.setValue(active);
        }
    }

    public static boolean isRitualResult(HephaestusForgeBlockEntity blockEntity) {
        track(blockEntity);

        ItemStack snapshot = RESULT_SNAPSHOTS.get(blockEntity);

        if (snapshot == null || snapshot.isEmpty()) {
            return false;
        }

        ItemStack current = blockEntity.getStack(HephaestusForgeBlockEntity.MAIN_SLOT);

        return current.getCount() == snapshot.getCount() && ItemStack.isSameItemSameComponents(current, snapshot);
    }
}
