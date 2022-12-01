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
    }

    @Override
    public List<TooltipComponent> getTooltip() {
        List<TooltipComponent> list = new ArrayList<>();
        if (entity != null) {
            list.addAll(getTooltipText().stream().map(EmiPort::ordered).map(TooltipComponent::of).toList());
            String mod;
            if (entity instanceof VillagerEntity villager) {
                mod = EmiUtil.getModName(Registries.VILLAGER_PROFESSION.getId(villager.getVillagerData().getProfession()).getNamespace());
            } else {
                mod = EmiUtil.getModName(Registries.ENTITY_TYPE.getId(entity.getType()).getNamespace());
            }
            list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(mod, Formatting.BLUE, Formatting.ITALIC))));
            if (!getRemainder().isEmpty()) {
                list.add(new RemainderTooltipComponent(this));
            }
        }
        return list;
    }

    @Override
    public Text getName() {
        return entity != null ? entity.getName() : EmiPort.literal("yet another missingno");
    }

    public static void renderEntity(int x, int y, double size, LivingEntity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        Mouse mouse = client.mouse;
        float width = 1920;
        float height = 1080;
        Screen screen = client.currentScreen;
        if (screen != null) {
            width = screen.width;
            height = screen.height;
        }
        float mouseX = (float) ((width + 51) - mouse.getX());
        float mouseY = (float) ((height + 75 - 50) - mouse.getY());
        float f = (float)Math.atan(mouseX / 40.0F);
        float g = (float)Math.atan(mouseY / 40.0F);
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(x, y, 1050.0);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        MatrixStack matrixStack2 = new MatrixStack();
        matrixStack2.translate(0.0, 0.0, 1000.0);
        matrixStack2.scale((float) size, (float) size, (float) size);
        Quaternionf quaternion = new Quaternionf().rotateZ(3.1415927F);
        Quaternionf quaternion2 = new Quaternionf().rotateX(g * 20.0F * 0.017453292F);
        quaternion.mul(quaternion2);
        matrixStack2.multiply(quaternion);
        float h = entity.bodyYaw;
        float i = entity.getYaw();
        float j = entity.getPitch();
        float k = entity.prevHeadYaw;
        float l = entity.headYaw;
        entity.bodyYaw = 180.0F + f * 20.0F;
        entity.setYaw(180.0F + f * 40.0F);
        entity.setPitch(-g * 20.0F);
        entity.headYaw = entity.getYaw();
        entity.prevHeadYaw = entity.getYaw();
        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        quaternion2.conjugate();
        entityRenderDispatcher.setRotation(quaternion2);
        entityRenderDispatcher.setRenderShadows(false);
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        RenderSyst