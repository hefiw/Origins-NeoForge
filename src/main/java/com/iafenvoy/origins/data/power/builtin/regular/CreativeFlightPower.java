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
public class CreativeFlightPower extends Power {

    public static final MapCodec<CreativeFlightPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            Codec.DOUBLE.optionalFieldOf("mana_cost", 6.0).forGetter(p -> p.manaCostPerSecond)
    ).apply(i, CreativeFlightPower::new));

    private final double manaCostPerSecond;

    public CreativeFlightPower(BaseSettings settings, double manaCostPerSecond) {
        super(settings);
        this.manaCostPerSecond = manaCostPerSecond;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

    @Override
    public void active(@NotNull OriginDataHolder holder) {
        if (holder.getEntity() instanceof Player player) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    @Override
    public void inactive(@NotNull OriginDataHolder holder) {
        if (holder.getEntity() instanceof Player player && !player.isCreative()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    @Override
    public void tick(@NotNull OriginDataHolder holder) {
        System.out.println("-1");
        if (!(holder.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 == 0) return;

        System.out.println("0");
        System.out.println(player.tickCount);
        System.out.println(player.getAbilities().flying);
        System.out.println(!player.isCreative());
        if (player.getAbilities().flying && !player.isCreative()) {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            System.out.println("1");
            System.out.println(manaCostPerSecond);
            //не работает, надо чтоб если маны меньше чем manaCostPerSecond*5 персонаж не мог летать

            if (magicData.getMana() >= manaCostPerSecond) {
                magicData.setMana((float) (magicData.getMana() - manaCostPerSecond));

                if (player.tickCount % 20 == 0) {
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