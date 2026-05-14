package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data._common.AttributeEntry;
import com.iafenvoy.origins.data._common.helper.AttributePowerHelper;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AttributePower extends Power implements AttributePowerHelper {
    public static final MapCodec<AttributePower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            AttributeEntry.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(AttributePower::getModifiers),
            Codec.BOOL.optionalFieldOf("update_health", true).forGetter(AttributePower::shouldUpdateHealth)
    ).apply(i, AttributePower::new));
    private final List<AttributeEntry> modifiers;
    private final boolean updateHealth;

    public AttributePower(BaseSettings settings, List<AttributeEntry> modifiers, boolean updateHealth) {
        super(settings);
        this.modifiers = modifiers;
        this.updateHealth = updateHealth;
    }

    @Override
    public List<AttributeEntry> getModifiers() {
        return this.modifiers;
    }

    @Override
    public boolean shouldUpdateHealth() {
        return this.updateHealth;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

    @Override
    public void grant(@NotNull OriginDataHolder holder) {
        this.modify(holder, true);
    }

    @Override
    public void revoke(@NotNull OriginDataHolder holder) {
        this.modify(holder, false);
    }

    @Override
    public void active(@NotNull OriginDataHolder holder) {
        this.modify(holder, true);
    }

    @Override
    public void inactive(@NotNull OriginDataHolder holder) {
        this.modify(holder, false);
    }

    @Override
    public void tick(@NotNull OriginDataHolder holder) {
        super.tick(holder);

        // Применяем раз в 40 тиков (~2 секунды) — достаточно и не нагружает
        if (holder.getEntity().tickCount % 20 == 0) {
            this.modify(holder, true);
        }
    }
}
