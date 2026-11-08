package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.example.armorvisibility.ArmorRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorFeatureRendererMixin {
    @Unique
    private ArmorRenderContext.State armorvisibility$previousState;

    @Unique
    private ArmorRenderContext.State armorvisibility$piecePreviousState;

    @Inject(method = "render", at = @At("HEAD"))
    private void armorvisibility$beginEntityRender(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            HumanoidRenderState state,
            float limbAngle,
            float limbDistance,
            CallbackInfo ci
    ) {
        boolean appliesSettings = !(ArmorConfig.INSTANCE.isPlayerOnly()
                && !(state instanceof PlayerRenderState))
                && (!armorvisibility$isOtherPlayer(state)
                || ArmorConfig.INSTANCE.shouldHideForOtherPlayers());
        armorvisibility$previousState =
                ArmorRenderContext.setCurrentState(null, 1.0F, appliesSettings);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void armorvisibility$endEntityRender(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            HumanoidRenderState state,
            float limbAngle,
            float limbDistance,
            CallbackInfo ci
    ) {
        ArmorRenderContext.restoreState(armorvisibility$previousState);
        armorvisibility$previousState = null;
    }

    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void armorvisibility$applyArmorSettings(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ItemStack stack,
            EquipmentSlot slot,
            int packedLight,
            HumanoidModel<?> model,
            CallbackInfo ci
    ) {
        if (slot == null) {
            return;
        }

        if (stack.is(Items.ELYTRA)) {
            if (!ArmorConfig.INSTANCE.shouldKeepElytraVisible()) {
                ci.cancel();
            }
            return;
        }

        if (!ArmorRenderContext.shouldApplySettings()) {
            return;
        }

        armorvisibility$piecePreviousState = ArmorRenderContext.setCurrentState(
                slot, ArmorConfig.INSTANCE.getArmorOpacity(slot), true);

        if (!ArmorConfig.INSTANCE.isArmorVisible()
                || ArmorConfig.INSTANCE.isArmorPartHidden(slot)) {
            ArmorRenderContext.restoreState(armorvisibility$piecePreviousState);
            armorvisibility$piecePreviousState = null;
            ci.cancel();
        }
    }

    @Inject(method = "renderArmorPiece", at = @At("RETURN"))
    private void armorvisibility$endArmorPiece(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ItemStack stack,
            EquipmentSlot slot,
            int packedLight,
            HumanoidModel<?> model,
            CallbackInfo ci
    ) {
        if (armorvisibility$piecePreviousState != null
                || ArmorRenderContext.getCurrentSlot() != null) {
            ArmorRenderContext.restoreState(armorvisibility$piecePreviousState);
            armorvisibility$piecePreviousState = null;
        }
    }

    @Unique
    private boolean armorvisibility$isOtherPlayer(HumanoidRenderState state) {
        if (!(state instanceof PlayerRenderState) || state.nameTag == null) {
            return false;
        }

        Minecraft client = Minecraft.getInstance();
        return client.player != null && !state.nameTag.equals(client.player.getDisplayName());
    }
}
