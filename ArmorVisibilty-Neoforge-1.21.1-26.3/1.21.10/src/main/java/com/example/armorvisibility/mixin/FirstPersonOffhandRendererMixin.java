package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.example.armorvisibility.ArmorRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class FirstPersonOffhandRendererMixin {
    @Unique
    private Float armorvisibility$previousOffhandOpacity;

    @Unique
    private boolean armorvisibility$offhandContextSet;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void armorvisibility$beginOffhandRender(
            AbstractClientPlayer player,
            float tickProgress,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack item,
            float equipProgress,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            CallbackInfo ci
    ) {
        if (hand != InteractionHand.OFF_HAND || item.isEmpty()) {
            return;
        }

        armorvisibility$previousOffhandOpacity =
                ArmorRenderContext.setOffhandOpacity(ArmorConfig.INSTANCE.getOffhandOpacity());
        armorvisibility$offhandContextSet = true;
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void armorvisibility$endOffhandRender(
            AbstractClientPlayer player,
            float tickProgress,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack item,
            float equipProgress,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            CallbackInfo ci
    ) {
        if (armorvisibility$offhandContextSet) {
            ArmorRenderContext.restoreOffhandOpacity(armorvisibility$previousOffhandOpacity);
            armorvisibility$previousOffhandOpacity = null;
            armorvisibility$offhandContextSet = false;
        }
    }
}
