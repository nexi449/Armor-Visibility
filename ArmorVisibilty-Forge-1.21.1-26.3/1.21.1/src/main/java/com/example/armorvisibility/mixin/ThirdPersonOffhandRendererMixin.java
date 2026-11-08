package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.example.armorvisibility.ArmorRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ThirdPersonOffhandRendererMixin {
    @Unique
    private Float armorvisibility$previousOffhandOpacity;

    @Unique
    private boolean armorvisibility$offhandContextSet;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), remap = false)
    private void armorvisibility$beginThirdPersonOffhandRender(
            LivingEntity entity,
            net.minecraft.world.item.ItemStack stack,
            net.minecraft.world.item.ItemDisplayContext displayContext,
            net.minecraft.world.entity.HumanoidArm arm,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffers,
            int light,
            CallbackInfo ci
    ) {
        if (arm == entity.getMainArm() || stack.isEmpty() || !armorvisibility$shouldApply(entity)) {
            return;
        }

        armorvisibility$previousOffhandOpacity =
                ArmorRenderContext.setOffhandOpacity(ArmorConfig.INSTANCE.getOffhandOpacity());
        armorvisibility$offhandContextSet = true;
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"), remap = false)
    private void armorvisibility$endThirdPersonOffhandRender(
            LivingEntity entity,
            net.minecraft.world.item.ItemStack stack,
            net.minecraft.world.item.ItemDisplayContext displayContext,
            net.minecraft.world.entity.HumanoidArm arm,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffers,
            int light,
            CallbackInfo ci
    ) {
        if (armorvisibility$offhandContextSet) {
            ArmorRenderContext.restoreOffhandOpacity(armorvisibility$previousOffhandOpacity);
            armorvisibility$previousOffhandOpacity = null;
            armorvisibility$offhandContextSet = false;
        }
    }

    @Unique
    private boolean armorvisibility$shouldApply(LivingEntity entity) {
        if (ArmorConfig.INSTANCE.isPlayerOnly() && !(entity instanceof Player)) {
            return false;
        }

        Minecraft client = Minecraft.getInstance();
        return !(entity instanceof Player)
                || client.player == null
                || entity == client.player
                || ArmorConfig.INSTANCE.shouldHideForOtherPlayers();
    }
}
