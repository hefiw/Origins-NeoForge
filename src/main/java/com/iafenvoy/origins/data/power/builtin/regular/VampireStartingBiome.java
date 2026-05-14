package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.VampireSpawnData;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class VampireStartingBiome extends Power {

    public static final MapCodec<VampireStartingBiome> CODEC = RecordCodecBuilder.mapCodec(i ->
            i.group(BaseSettings.CODEC.forGetter(Power::getSettings))
                    .apply(i, VampireStartingBiome::new));

    public VampireStartingBiome(BaseSettings settings) {
        super(settings);
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

    // При респавне
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.isEndConquered()) return;
        OriginDataHolder holder = OriginDataHolder.get(player);
        if (!holder.isPowerActive(VampireStartingBiome.class)) return;
        if (player.getRespawnPosition() != null) return;
        teleportToVampireSpawn(player);
        //VampireSpawnData data = VampireSpawnData.get(player.server.overworld());
        //BlockPos vampireSpawn = data.getSpawnPos();
        //if (vampireSpawn != BlockPos.ZERO) {
            //player.setRespawnPosition(player.level().dimension(), vampireSpawn, 0f, true, false);
        //}
    }

    public static void teleportToVampireSpawn(ServerPlayer player) {
        VampireSpawnData data = VampireSpawnData.get(player.server.overworld());
        BlockPos spawn = data.getSpawnPos();

        if (spawn != BlockPos.ZERO) {
            player.teleportTo(spawn.getX() + 0.5, spawn.getY() + 1.2, spawn.getZ() + 0.5);
        } else {
            BlockPos cave = findNearestCave(player, player.blockPosition());
            if (cave != null) {
                data.setSpawnPos(cave);
                player.teleportTo(cave.getX() + 0.5, cave.getY() + 1.2, cave.getZ() + 0.5);
                //player.setRespawnPosition(player.level().dimension(), spawn, 0f, true, false);
            }
        }
    }

    public static BlockPos findNearestCave(ServerPlayer player, BlockPos start) {
        int radius = 30;
        int minY = -10;
        int maxY = start.getY() - 5;

        for (int y = minY; y <= maxY; y++) {
            for (int x = -radius; x <= radius; x += 3) {      // шаг 3 для производительности
                for (int z = -radius; z <= radius; z += 3) {

                    BlockPos check = new BlockPos(start.getX() + x, y, start.getZ() + z);

                    // Проверяем, является ли это большой пещерой
                    if (isBigCave(player, check)) {
                        return check;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isBigCave(ServerPlayer player, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {           // высота 3 блока
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = pos.offset(dx, dy, dz);

                    // Должно быть пусто (воздух или жидкость)
                    if (!player.level().isEmptyBlock(p)) {
                        return false;
                    }
                }
            }
        }

        if (player.level().canSeeSky(pos)) return false;                    // не должно быть видно небо
        if (player.level().isEmptyBlock(pos.below())) return false;         // под ногами должен быть пол

        return true;
    }
}