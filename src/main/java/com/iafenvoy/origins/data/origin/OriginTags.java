package com.iafenvoy.origins.data.origin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record OriginTags(List<String> tags) {

    public static final OriginTags EMPTY = new OriginTags(List.of());

    public static final Codec<OriginTags> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.listOf().optionalFieldOf("tags", List.of()).forGetter(OriginTags::tags)
    ).apply(i, OriginTags::new));
}