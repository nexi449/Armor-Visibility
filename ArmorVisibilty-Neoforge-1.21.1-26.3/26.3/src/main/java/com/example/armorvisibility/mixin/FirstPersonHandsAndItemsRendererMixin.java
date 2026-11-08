package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.example.armorvisibility.ArmorRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsAndItemsRendererMixin {
    @Unique
    private Float armorvisibility$previousOffhandOpacity;

    @Unique
    private boolean armorvisibility$offhandContextSet;

    @Inject(method = "submitArmWithItem", at = @At("HEAD"))
    private void armorvisibility$beginFirstPersonOffhand(
            PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState renderState,
            float partialTick,
            float pitch,
            InteractionHand hand,
            float swing,
            ItemStack itemStack,
            float equipProgress,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            CallbackInfo ci
    ) {
        if (hand == InteractionHand.OFF_HAND && !itemStack.isEmpty()) {
            armorvisibility$previousOffhandOpacity =
                    ArmorRenderContext.setOffhandOpacity(ArmorConfig.INSTANCE.getOffhandOpacity());
            armorvisibility$offhandContextSet = true;
        }
    }

    @Inject(method = "submitArmWithItem", at = @At("RETURN"))
    private void armorvisibility$endFirstPersonOffhand(
            PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState renderState,
            float partialTick,
            float pitch,
            InteractionHand hand,
            float swing,
            ItemStack itemStack,
            float equipProgress,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            CallbackInfo ci
    ) {
        if (armorvisibility$offhandContextSet) {
            ArmorRenderContext.restoreOffhandOpacity(armorvisibility$previousOffhandOpacity);
            armorvisibility$previousOffhandOpacity = null;
            armorvisibility$offhandContextSet = false;
        }
    }
}
