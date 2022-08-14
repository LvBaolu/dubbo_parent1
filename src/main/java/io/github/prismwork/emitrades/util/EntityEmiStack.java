package io.github.prismwork.emitrades.util;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class EntityEmiStack extends EmiStack {
    private final @Nullable Entity entity;
    private final EntityEntry entry;
    private final double scale;

    protected EntityEmiStack(@Nullable Entity entity) {
        this.entity = entity;
        this.entry = new EntityEntry(entity);
        this.scale = 8.0f;
    }

    protected EntityEmiStack(@Nullable Entity entity, double scale) {
        this.entity = entity;
        this.entry = new EntityEntry(entity);
        this.scale = scale;
    }

    public static EntityEmiStack of(@Nullable Entity entity) {
        return new EntityEmiStack(entity);
    }

    public static EntityEmiStack ofScaled(@Nullable Entity entity, double scale) {
        return new EntityEmiStack(entity, scale);
    }

    @Override
    public EmiStack copy() {
        EntityEmiStack stack = new EntityEmiStack(entity);
        stack.setRemainder(getRemainder().copy());
        stack.comparison = comparison;
        return stack;
    }

    @Override
    public boolean isEmpty() {
        return entity == null;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, float delta, int flags) {
        if (entity != null) {
            if (entity instanceof LivingEntity living)
                renderEntity(x + 8, (int) (y + 8 + scale), scale, living);
            else
                renderEntity((int) (x + (2 * scale / 2)), (int) (y + (2 * scale)), scale, entity);
        }
    }

    @Override
    public NbtCompound getNbt() {
        throw new UnsupportedOperationException("EntityEmiStack is not intended for NBT handling");
    }

    @Override
    public Object getKey() {
        return entity;
    }

    @Override
    public Entry<?> getEntry() {
        return entry;
    }

    @Override
    public Identifier getId() {
        if (entity == null) throw new RuntimeException("Entity is null");
        return Registries.ENTITY_TYPE.getId(entity.getType());
    }

    @Override
    public List<Text> getTooltipText() {
        return List.of(getName());
 