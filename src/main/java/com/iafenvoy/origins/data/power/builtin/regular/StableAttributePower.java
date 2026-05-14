package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data._common.AttributeEntry;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class StableAttributePower extends Power {

    public static final MapCodec<StableAttributePower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            AttributeEntry.CODEC.listOf().optionalFieldOf("modifiers", List.of()).forGetter(p -> p.modifiers),
            AttributeEntry.CODEC.optionalFieldOf("modifier").forGetter(p -> p.modifier)
    ).apply(i, StableAttributePower::new));

    private final List<AttributeEntry> modifiers;
    private final java.util.Optional<AttributeEntry> modifier;

    public StableAttributePower(BaseSettings settings, List<AttributeEntry> modifiers, java.util.Optional<AttributeEntry> modifier) {
        super(settings);
        this.modifiers = modifiers;
        this.modifier = modifier;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

    @Override
    public void grant(@NotNull OriginDataHolder holder) {
        apply(holder);
    }

    @Override
    public void active(@NotNull OriginDataHolder holder) {
        apply(holder);
    }

    @Override
    public void inactive(@NotNull OriginDataHolder holder) {
        revoke(holder);
    }

    private void apply(OriginDataHolder holder) {
        revoke(holder); // сначала чистим старые

        if (!modifiers.isEmpty()) {
            new AttributePower(getSettings(), modifiers, false).grant(holder);
        } else if (modifier.isPresent()) {
            // Поддержка singular "modifier"
            List<AttributeEntry> single = List.of(modifier.get());
            new AttributePower(getSettings(), single, false).grant(holder);
        }
    }

    public void revoke(OriginDataHolder holder) {
        if (!modifiers.isEmpty()) {
            new AttributePower(getSettings(), modifiers, false).revoke(holder);
        } else if (modifier.isPresent()) {
            List<AttributeEntry> single = List.of(modifier.get());
            new AttributePower(getSettings(), single, false).revoke(holder);
        }
    }

    @Override
    public void tick(@NotNull OriginDataHolder holder) {
        // Лёгкая страховка
        if (holder.getEntity().tickCount % 60 == 0) {
            apply(holder);
        }
    }
}