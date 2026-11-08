package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeFeatureRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void armorvisibility$hideCapeWhenDisabled(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            PlayerRenderState state,
            float limbAngle,
            float limbDistance,
            CallbackInfo ci
    ) {
        if (ArmorConfig.INSTANCE.isArmorPartHidden(EquipmentSlot.CHEST)
                && !ArmorConfig.INSTANCE.shouldKeepCapeVisible()) {
            ci.cancel();
        }
    }
}