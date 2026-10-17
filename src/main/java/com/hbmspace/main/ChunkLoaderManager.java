// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.main;

import com.hbm.registration.NeoForgeRegistrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.HbmSpace;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import com.hbm.backport.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import com.hbm.backport.SavedDataType;
import com.hbm.backport.SavedDataCompat;
import com.hbm.backport.Nbt;

public final class ChunkLoaderManager {
    public static final RegistryHandle<TicketType> TICKET =
            NeoForgeRegistrar.registerTicketType(
                    HbmSpace.MOD_ID + ":chunk_loader",
                    () ->
                            new TicketType(
                                    TicketType.NO_TIMEOUT,
                                    TicketType.FLAG_PERSIST
                                            | TicketType.FLAG_LOADING
                                            | TicketType.FLAG_SIMULATION
                                            | TicketType.FLAG_KEEP_DIMENSION_ACTIVE));
    private static final int ENTITY_TICKING_RADIUS = 2;

    private ChunkLoaderManager() {}

    public static void forceChunk(ServerLevel level, BlockPos owner) {
        ChunkPos chunk = new ChunkPos(owner);
        Owners owners = SavedDataType.computeIfAbsent(level.getDataStorage(), Owners.TYPE);
        owners.forcedBy.put(owner.immutable(), chunk.toLong());
        owners.setDirty();

        TicketType.addTicketWithRadius(level.getChunkSource(), TICKET.get(), chunk, ENTITY_TICKING_RADIUS);
    }

    public static void unforceChunk(ServerLevel level, BlockPos owner) {
        Owners owners = SavedDataType.computeIfAbsent(level.getDataStorage(), Owners.TYPE);
        if (!owners.forcedBy.containsKey(owner)) return;
        long chunk = owners.forcedBy.removeLong(owner);
        owners.setDirty();

        if (!owners.forcedBy.containsValue(chunk)) {
            TicketType.removeTicketWithRadius(level.getChunkSource(), TICKET.get(), new ChunkPos(chunk), ENTITY_TICKING_RADIUS);
        }
    }

    public static boolean isForcedBy(ServerLevel level, BlockPos owner) {
        return SavedDataType.computeIfAbsent(level.getDataStorage(), Owners.TYPE).forcedBy.containsKey(owner);
    }

    private static final class Owners extends SavedDataCompat {

        static final SavedDataType<Owners> TYPE =
                new SavedDataType<>(
                        ResourceLocation.fromNamespaceAndPath(HbmSpace.MOD_ID, "chunk_loader"),
                        Owners::new,
                        CompoundTag.CODEC.xmap(Owners::load, Owners::save),
                        null);

        final Object2LongLinkedOpenHashMap<BlockPos> forcedBy =
                new Object2LongLinkedOpenHashMap<>();

        private static Owners load(CompoundTag nbt) {
            Owners owners = new Owners();
            ListTag list = Nbt.getListOrEmpty(nbt, "chunks");
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = Nbt.getCompoundOrEmpty(list, i);
                owners.forcedBy.put(
                        BlockPos.of(Nbt.getLongOr(tag, "pos", 0L)), Nbt.getLongOr(tag, "chunk", 0L));
            }
            return owners;
        }

        private CompoundTag save() {
            ListTag list = new ListTag();
            for (var entry : forcedBy.object2LongEntrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putLong("pos", entry.getKey().asLong());
                tag.putLong("chunk", entry.getLongValue());
                list.add(tag);
            }
            CompoundTag nbt = new CompoundTag();
            nbt.put("chunks", list);
            return nbt;
        }
    }
}
