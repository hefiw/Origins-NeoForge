package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IFactionPlayerHandler;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class BecomeVampirePower extends Power {

    public static final MapCodec<BecomeVampirePower> CODEC = RecordCodecBuilder.mapCodec(i ->
            i.group(BaseSettings.CODEC.forGetter(Power::getSettings))
                    .apply(i, BecomeVampirePower::new));

    public BecomeVampirePower(BaseSettings settings) {
        super(settings);
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

    @Override
    public void grant(@NotNull OriginDataHolder holder) {
        convertToVampire(holder);
    }

    @Override
    public void active(@NotNull OriginDataHolder holder) {
        convertToVampire(holder);
    }

    private void convertToVampire(OriginDataHolder holder) {
        if (!(holder.getEntity() instanceof ServerPlayer player)) return;
        try {
            IFactionPlayerHandler factionHandler = VampirismAPI.factionPlayerHandler(player);
            IFaction<?> faction = VampirismAPI.factionRegistry().getFactionByID(ResourceLocation.fromNamespaceAndPath("vampirism", "vampire"));
            if (faction instanceof IPlayableFaction<?> playableFaction) {
                if (factionHandler.getCurrentFaction() != playableFaction) {
                    factionHandler.joinFaction(playableFaction);
                    factionHandler.setFactionLevel(playableFaction, 4);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}