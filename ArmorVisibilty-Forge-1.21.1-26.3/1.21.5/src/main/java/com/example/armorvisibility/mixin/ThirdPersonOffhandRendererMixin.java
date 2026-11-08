package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.example.armorvisibility.ArmorRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(ItemInHandLayer.class)
public abstract class ThirdPersonOffhandRendererMixin {
    @Unique
    private Float armorvisibility$previousOffhandOpacity;

    @Unique
    private boolean armorvisibility$offhandContextSet;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), remap = false)
    private void armorvisibility$beginThirdPersonOffhandRender(
            ArmedEntityRenderState state,
            ItemStackRenderState itemState,
            HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            CallbackInfo ci
    ) {
        if (arm == state.mainArm || !armorvisibility$shouldApply(state)) {
            return;
        }

        armorvisibility$previousOffhandOpacity =
                ArmorRenderContext.setOffhandOpacity(ArmorConfig.INSTANCE.getOffhandOpacity());
        armorvisibility$offhandContextSet = true;
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"), remap = false)
    private void armorvisibility$endThirdPersonOffhandRender(
            ArmedEntityRenderState state,
            ItemStackRenderState itemState,
            HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource buffers,
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
    private boolean armorvisibility$shouldApply(ArmedEntityRenderState state) {
        boolean player = state instanceof PlayerRenderState;
        if (ArmorConfig.INSTANCE.isPlayerOnly() && !player) {
            return false;
        }
        return !player
                || state.nameTag == null
                || Minecraft.getInstance().player == null
                || state.nameTag.equals(Minecraft.getInstance().player.getDisplayName())
                || ArmorConfig.INSTANCE.shouldHideForOtherPlayers();
    }
}
