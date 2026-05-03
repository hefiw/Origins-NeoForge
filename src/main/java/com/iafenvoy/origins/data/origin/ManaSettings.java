package com.iafenvoy.origins.data.origin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ManaSettings(double maxMana, double regen) {

    public static final ManaSettings DEFAULT = new ManaSettings(100.0, 1.0);

    public static final Codec<ManaSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("max_mana", 100.0).forGetter(ManaSettings::maxMana),
            Codec.DOUBLE.optionalFieldOf("regen", 1.0).forGetter(ManaSettings::regen)
    ).apply(instance, ManaSettings::new));
}