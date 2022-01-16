package io.github.prismwork.emitrades.recipe;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.prismwork.emitrades.EMITradesPlugin;
import io.github.prismwork.emitrades.util.EntityEmiStack;
import io.github.prismwork.emitrades.util.TradeProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VillagerTrade implements EmiRecipe {
    private final TradeProfile profile;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    private final List<EmiIngredient> catalysts;
    private final int id;
    private final MutableText title;

    public VillagerTrade(TradeProfile profile, int id) {
        this.profile = profile;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.catalysts = profile.villager() != null ?
                List.of(EntityEmiStack.ofScaled(profile.villager(), 12.0f)) : List.of();
        this.id = id;
        VillagerProfession internalProf = profile.profession();
        if (internalProf.equals(EMITradesPlugin.WANDERING_TRADER_PLACEHOLDER)) {
            this.title = EmiPort.translatable("emi.emitrades.placeholder.wandering_trader");
        } else {
            this.title = EmiPort.translatable("entity.minecraft.villager." + profile.profession().id().substring(profile.profession().id().lastIndexOf(":") + 1))
                    .append(" - ").append(EmiPort.translatable("emi.emitrades.profession.lvl." + profile.level()));
        }
        TradeOffers.Factory offer = profile.offer();
        if (offer instanceof TradeOffers.BuyForOneEmeraldFactory factory) {
            inputs.add(0, EmiStack.of(factory.buy, factory.price));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(Items.EMERALD));
        } else if (offer instanceof TradeOffers.SellItemFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(factory.sell, factory.count));
        } else if (offer instanceof TradeOffers.SellSuspiciousStewFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, 1));
            inputs.add(1, EmiStack.EMPTY);
            ItemStack stack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
            SuspiciousStewItem.addEffectToStew(stack, factory.effect, factory.duration);
            outputs.add(0, EmiStack.of(stack));
        } else if (offer instanceof TradeOffers.ProcessItemFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.of(factory.secondBuy, factory.secondCount));
            outputs.add(0, EmiStack.of(factory.sell, factory.sellCount));
        } else if (offer instanceof TradeOffers.SellEnchantedToolFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, Math.min(factory.basePrice + 5, 64)));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(factory.tool));
        } else if (offer instanceof TradeOffers.TypeAwareBuyForOneEmeraldFactory factory) {
            List<EmiStack> stacks = new ArrayList<>();
            factory.map.values().forEach(item -> stacks.add(EmiStack.of(item)));
            inputs.add(0, new ListEmiIngredient(stacks, factory.count));
            inputs.add(1, EmiStack.EMPTY);
            outputs.add(0, EmiStack.of(Items.EMERALD));
        } else if (offer instanceof TradeOffers.SellPotionHoldingItemFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.of(factory.secondBuy, factory.secondCount));
            outputs.add(0, EmiStack.of(factory.sell, factory.sellCount));
        } else if (offer instanceof TradeOffers.EnchantBookFactory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, 5));
            inputs.add(1, EmiStack.of(Items.BOOK));
            outputs.add(0, EmiStack.of(Items.ENCHANTED_BOOK));
        } else if (offer instanceof TradeOffers.SellMapFactory factory) {
            inputs.add(0, EmiStack.of(Items.EMERALD, factory.price));
            inputs.add(1, EmiStack.of(Items.COMPASS));
            outputs.add(0, EmiStack.of(Items.FILLED_MAP));
    