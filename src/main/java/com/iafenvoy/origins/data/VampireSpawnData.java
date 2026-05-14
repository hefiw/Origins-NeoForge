package com.iafenvoy.origins.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class VampireSpawnData extends SavedData {

    private static final String NAME = "vampire_spawn_point";

    private BlockPos spawnPos = BlockPos.ZERO;

    public static VampireSpawnData get(ServerLevel level) {
        Supplier<VampireSpawnData> supplier = VampireSpawnData::new;
        BiFunction<CompoundTag, HolderLookup.Provider, VampireSpawnData> deserializer = VampireSpawnData::load;

        SavedData.Factory<VampireSpawnData> factory = new SavedData.Factory<>(supplier, deserializer);

        return level.getDataStorage().computeIfAbsent(factory, NAME);
    }

    public static VampireSpawnData load(CompoundTag tag, HolderLookup.Provider provider) {
        VampireSpawnData data = new VampireSpawnData();
        if (tag.contains("SpawnX") && tag.contains("SpawnY") && tag.contains("SpawnZ")) {
            data.spawnPos = new BlockPos(
                    tag.getInt("SpawnX"),
                    tag.getInt("SpawnY"),
                    tag.getInt("SpawnZ")
            );
        }
        return data;
    }

    public BlockPos getSpawnPos() {
        return spawnPos;
    }

    public void setSpawnPos(BlockPos pos) {
        this.spawnPos = pos;
        this.setDirty(true);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("SpawnX", spawnPos.getX());
        tag.putInt("SpawnY", spawnPos.getY());
        tag.putInt("SpawnZ", spawnPos.getZ());
        return tag;
    }
}