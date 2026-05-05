package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class ManaFlightPower extends Power {

    public static final MapCodec<ManaFlightPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            Codec.DOUBLE.optionalFieldOf("mana_cost_per_tick", 5.0).forGetter(p -> p.manaCostPerTick)
    ).apply(i, ManaFlightPower::new));

    private final double manaCostPerTick;

    public ManaFlightPower(BaseSettings settings, double manaCostPerTick) {
        super(settings);
        this.manaCostPerTick = manaCostPerTick;
    }

    public double getManaCostPerTick() {
        return manaCostPerTick;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

    @Override
    public void active(@NotNull OriginDataHolder holder) {
        if (holder.getEntity() instanceof Player player) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();           // ← обязательно
        }
    }

    @Override
    public void inactive(@NotNull OriginDataHolder holder) {
        if (holder.getEntity() instanceof Player player) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();           // ← обязательно
        }
    }

    @Override
    public void tick(@NotNull OriginDataHolder holder) {
        super.tick(holder);   // ← Это обязательно! Здесь тикается ActiveComponent

        if (!(holder.getEntity() instanceof ServerPlayer player)) return;

        if (!player.getAbilities().mayfly && !player.isCreative()) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }

        if (player.getAbilities().flying && !player.isCreative()) {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            double costPerTick = manaCostPerTick / 20.0;

            if (magicData.getMana() >= manaCostPerTick*2) {
                magicData.setMana((float) (magicData.getMana() - costPerTick));

                // Частицы
                if (player.tickCount % 10 == 0) {
                    Vec3 pos = player.position();
                    ServerLevel level = (ServerLevel) player.level();
                    level.sendParticles(ParticleTypes.FIREWORK,
                            pos.x, pos.y + 0.2, pos.z,
                            2, 0.1, 0.1, 0.1, 0.05);
                }
            } else {
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }
}