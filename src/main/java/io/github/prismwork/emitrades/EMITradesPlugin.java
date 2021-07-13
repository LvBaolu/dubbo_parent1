package io.github.prismwork.emitrades;

import com.google.common.collect.ImmutableSet;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import io.github.prismwork.emitrades.config.EMITradesConfig;
import io.github.prismwork.emitrades.recipe.VillagerTrade;
import io.github.prismwork.emitrades.util.EntityEmiStack;
import io.github.prismwork.emitrades.util.TradeProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class EMITradesPlugin implements EmiPlugin {
    public static final VillagerProfession WANDERING_TRADER_PLACEHOLDER = new VillagerProfession(
            "wandering_trader",
            entry -> false,
            entry -> false,
            ImmutableSet.<Item>builder().build(),
            ImmutableSet.<Block>builder().build(),
            SoundEvents.ENTITY_WANDERING_TRADER_YES
    );
    public static final EmiRecipeCategory VILLAGER_TRADES
            = new EmiRecipeCategory(new Identifier("emitrades", "villager_trades"), EmiStack.of(Items.EMERALD));
    public static EMITradesConfig.Config CONFIG;
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("emitrades.json5").toFile();

    @Override
    public void register(EmiRegistry registry) {
        CONFIG = EMITradesConfig.load(CONFIG_FILE);
        registry.addCategory(VILLAGER_TRADES);
        Random random = Random.create();
        for (VillagerProfession profession : Registries.VILLAGER_PROFESSION) {
            VillagerEntity villager = (VillagerEntity)
                    Registries.ENTITY_TYPE.get(new Identifier("minecraft", "villager")).create(MinecraftClient.getInstance().world);
            if (villager != null) {
                villager.setVillagerData(villager.getVillagerData().withProfession(profession).withLevel(5));
                registry.addWorkstation(VILLAGER_TRADES, EntityEmiStack.ofScaled(villager, 8.0f));
            }
            AtomicInteger id = new AtomicInteger();
            Int2ObjectMap<TradeOffers.Factory[]> offers = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(profession);
            if (offers == null || offers.isEmpty()) continue;
            int level = 0;
            while (level < 5) {
                VillagerEntity villager1 = (VillagerEntity)
                        Registries.ENTITY_TYPE.get(new Identifier("minecraft", "villager")).create(MinecraftClient.getInstance().world);
                if (villager1 != null) {
                    villager1.setVillagerData(villager1.getVillagerData().withProfession(profession).withLevel(level + 1));
                }
                for (TradeOffers.Factory offer : offers.get(level + 1)) {
                    if (isVanillaFactory(offer)) {
                        registry.addRecipe(new VillagerTrade(new TradeProfile.DefaultImpl(profession, offer, level + 1, villager1), id.get()));
                        id.getAndIncrement();
                    } else {
                        try {
                            int attempts = 5;
                            TreeSet<TradeOffer> genOffers = new TreeSet<>(this::compareOffers);
                            TradeOffer inOffer;
                            while (attempts > 0) {
                                inOffer = offer.create(MinecraftClient.getInstance().player, random);
                                if (genOffers.add(inOffer))
                                    attempts++;
                                else
                                    attempts--;
                            }
                            int finalLevel = level;
                            genOffers.forEach(tradeOffer -> {
                                registry.addRecipe(new VillagerTrade(new TradeProfile.DefaultImpl(profession, new FakeFactory(tradeOffer), finalLevel + 1, villager1), id.get()));
                                id.getAndIncrement();
                            });
                        } catch (Exception ignored) {}
                    }
                }
                level++;
            }
        }
        WanderingTraderEntity wanderingTrader = (WanderingTraderEntity) Registries.ENTITY_TYPE.get(new Identifier("minecraft", "wandering_trader"))
                .create(MinecraftClient.getInstance().world);
        registry.addWorkstation(VILLAGER_TRADES, EntityEmiStack.of(wanderingTrader));
        AtomicInteger wanderingTraderId = new AtomicInteger();
        TradeOffers.WANDERING_TRADER_TRADES.forEach((lvl, offers) -> {
            for (TradeOffers.Factory offer : offers) {
                if (isVanillaFactory(offer)) {
                    registry.addRecipe(new VillagerTrade(new TradeProfile.DefaultImpl(WANDERING_TRADER_PLACEHOLDER, offer, lvl, wanderingTrader), wanderingTraderId.get()));
                    wanderingTraderId.getAndIncrement();
                } else {
                    try {
                        int attempts = 5;
                        TreeSet<TradeOffer> genOffers = new TreeSet<>(this::compareOffers);
                        TradeOffer inOffer;
                        while (attempts > 0) {
                            inOffer = offer.create(MinecraftCl