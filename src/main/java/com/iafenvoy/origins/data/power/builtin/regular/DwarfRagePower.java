package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.Objects;

@EventBusSubscriber
public class DwarfRagePower extends Power {

    public static final MapCodec<DwarfRagePower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            Codec.DOUBLE.optionalFieldOf("mana_cost_per_second", 1.0).forGetter(p -> p.manaCostPerSecond),
            Codec.DOUBLE.optionalFieldOf("size", 1.25).forGetter(p -> p.size),
            Codec.DOUBLE.optionalFieldOf("rage_hp", 10.0).forGetter(p -> p.rageHp)
    ).apply(i, DwarfRagePower::new));

    private final double manaCostPerSecond;
    private final double size;
    private final double rageHp;

    private static boolean isRageActive = false;
    private static final ResourceLocation RED_OVERLAY_SHADER = ResourceLocation.fromNamespaceAndPath("origins", "shaders/post/red_rage.json");
    private long lastToggleTime = 0;

    public DwarfRagePower(BaseSettings settings, double manaCostPerSecond, double size, double rageHp) {
        super(settings);
        this.manaCostPerSecond = manaCostPerSecond;
        this.size = size;
        this.rageHp = rageHp;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

    @Override
    public void tick(@NotNull OriginDataHolder holder) {
        super.tick(holder);
        if (!(holder.getEntity() instanceof ServerPlayer player)) return;
        long currentTime = System.currentTimeMillis();
        int state = GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_Z);
        boolean keyPressed = state == GLFW.GLFW_PRESS;

        if (keyPressed && (currentTime - lastToggleTime > 5000)) {
            lastToggleTime = currentTime;
            if (keyPressed && !isRageActive) {
                isRageActive = true;
                activateRage(player);
            } else if (keyPressed && isRageActive) {
                isRageActive = false;
                deactivateRage(player);
            }
        }
        if (isRageActive) {
            handleRageTick(holder);
        }

    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        OriginDataHolder holder = OriginDataHolder.get(player);
        if (holder.isPowerActive(DwarfRagePower.class)) {
            isRageActive = false;
            deactivateRage(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        OriginDataHolder holder = OriginDataHolder.get(player);
        if (holder.isPowerActive(DwarfRagePower.class)) {
            isRageActive = false;
            deactivateRage(player);
        }
    }

    private void activateRage(ServerPlayer player) {
        // Размер
        ScaleData scaleData = ScaleTypes.BASE.getScaleData(player);
        scaleData.setTargetScale((float) size);
        scaleData.setScale((float) size);

        // Золотые сердца
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 999999, (int)(rageHp / 2) - 1, false, true));

        // Урон ×2
        Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).addTransientModifier(
                new AttributeModifier(ResourceLocation.fromNamespaceAndPath("origins", "dwarf_rage"),
                        1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
        );
    }

    private static void deactivateRage(ServerPlayer player) {
        ScaleData scaleData = ScaleTypes.BASE.getScaleData(player);
        scaleData.setTargetScale(1.0f);
        scaleData.setScale(1.0f);

        player.removeEffect(MobEffects.ABSORPTION);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20*60, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20*60, 1));
        Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE)).removeModifier(
                ResourceLocation.fromNamespaceAndPath("origins", "dwarf_rage")
        );
    }

    private void handleRageTick(OriginDataHolder holder) {
        ServerPlayer player = (ServerPlayer) holder.getEntity();
        MagicData magicData = MagicData.getPlayerMagicData(player);
        double costPerTick = manaCostPerSecond / 20.0;

        if (magicData.getMana() < costPerTick) {
            deactivateRage(player);
            isRageActive = false;
            return;
        }
        magicData.setMana((float) (magicData.getMana() - costPerTick));
    }
}