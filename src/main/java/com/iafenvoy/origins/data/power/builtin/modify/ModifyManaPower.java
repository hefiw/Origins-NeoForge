package com.iafenvoy.origins.data.power.builtin.modify;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.util.codec.CombinedCodecs;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Power type для модификации атрибутов маны из Iron Spellbooks.
 * Поддерживает:
 * - max_mana (максимальная мана)
 * - mana_regen (регенерация маны)
 *
 * Пример использования в JSON origin:
 * {
 *   "type": "origins:modify_mana",
 *   "attribute": "irons_spellbooks:max_mana",
 *   "modifier": {
 *     "id": "origin_mana_bonus",
 *     "amount": 50.0,
 *     "operation": "add_value"
 *   }
 * }
 */
public class ModifyManaPower extends Power {
    public static final MapCodec<ModifyManaPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            Attribute.CODEC.fieldOf("attribute").forGetter(ModifyManaPower::getAttribute),
            CombinedCodecs.ATTRIBUTE_MODIFIER.fieldOf("modifier").forGetter(ModifyManaPower::getModifiers)
    ).apply(i, ModifyManaPower::new));

    private final Holder<Attribute> attribute;
    private final List<AttributeModifier> modifiers;
    private static Boolean ironSpellbooksLoaded = null;

    public ModifyManaPower(BaseSettings settings, Holder<Attribute> attribute, List<AttributeModifier> modifiers) {
        super(settings);
        this.attribute = attribute;
        this.modifiers = modifiers;
    }

    public Holder<Attribute> getAttribute() {
        return this.attribute;
    }

    public List<AttributeModifier> getModifiers() {
        return this.modifiers;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

    /**
     * Проверяет, загружен ли мод Iron Spellbooks
     */
    private boolean isIronSpellbooksLoaded() {
        if (ironSpellbooksLoaded == null) {
            ironSpellbooksLoaded = ModList.get().isLoaded("irons_spellbooks");
        }
        return ironSpellbooksLoaded;
    }

    private Optional<AttributeInstance> getAttributeInstance(LivingEntity entity) {
        if (!isIronSpellbooksLoaded()) {
            return Optional.empty();
        }

        if (entity.getAttributes().hasAttribute(this.attribute)) {
            return Optional.ofNullable(entity.getAttribute(this.attribute));
        }
        return Optional.empty();
    }

    @Override
    public void active(@NotNull OriginDataHolder holder) {
        if (!(holder.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }

        this.getAttributeInstance(livingEntity).ifPresent(instance ->
            this.modifiers.stream()
                .filter(mod -> !instance.hasModifier(mod.id()))
                .forEach(instance::addTransientModifier)
        );
    }

    @Override
    public void inactive(@NotNull OriginDataHolder holder) {
        if (!(holder.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }

        this.getAttributeInstance(livingEntity).ifPresent(instance ->
            this.modifiers.stream()
                .filter(mod -> instance.hasModifier(mod.id()))
                .forEach(instance::removeModifier)
        );
    }
}