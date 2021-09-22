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
import net.mi