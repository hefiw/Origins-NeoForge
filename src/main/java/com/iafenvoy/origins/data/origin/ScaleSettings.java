package com.iafenvoy.origins.data.origin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ScaleSettings(double scale) {
    public static final ScaleSettings DEFAULT = new ScaleSettings(1.0);

    public static final Codec<ScaleSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("scale", 1.0).forGetter(ScaleSettings::scale)
    ).apply(i, ScaleSettings::new));
}