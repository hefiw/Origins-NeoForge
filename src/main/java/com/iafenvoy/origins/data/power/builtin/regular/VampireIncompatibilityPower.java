package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class VampireIncompatibilityPower extends Power {

    public static final MapCodec<VampireIncompatibilityPower> CODEC = RecordCodecBuilder.mapCodec(i ->
            i.group(BaseSettings.CODEC.forGetter(Power::getSettings))
                    .apply(i, VampireIncompatibilityPower::new));

    public VampireIncompatibilityPower(BaseSettings settings) {
        super(settings);
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        OriginDataHolder holder = OriginDataHolder.get(player);

        if (!holder.isPowerActive(VampireIncompatibilityPower.class)) return;
        if (event.getEffectInstance() == null) return;
        Holder<MobEffect> effect = event.getEffectInstance().getEffect();

        ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect.value());

        if (effectId == null) return;

        String idStr = effectId.toString().toLowerCase();

        if (idStr.contains("sanguinare") ||
            idStr.contains("vampirism:sanguinare") ||
            idStr.contains("vampire_infection") ||
            idStr.contains("sanguine")) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }
}