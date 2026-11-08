package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeFeatureRendererMixin {
    @Inject(
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;I"
                    + "Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void armorvisibility$hideCapeWhenDisabled(PoseStack poseStack, SubmitNodeCollector collector,
                                                       int light, AvatarRenderState state, float limbAngle,
                                                       float limbDistance, CallbackInfo ci) {
        if (!ArmorConfig.INSTANCE.shouldKeepCapeVisible() && armorvisibility$shouldApplyCapeSetting(state)) {
            ci.cancel();
        }
    }

    private boolean armorvisibility$shouldApplyCapeSetting(AvatarRenderState state) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || state == null) {
            return false;
        }
        boolean localPlayer = state.id == client.player.getId();
        if (ArmorConfig.INSTANCE.isPlayerOnly()
                && !localPlayer) {
            return false;
        }

        boolean isOtherPlayer = !localPlayer && state.entityType == client.player.getType();
        return !isOtherPlayer || ArmorConfig.INSTANCE.shouldHideForOtherPlayers();
    }
}