package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WingsLayer.class)
public abstract class WingsLayerMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void armorvisibility$hideElytra(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            HumanoidRenderState state,
            float limbAngle,
            float limbDistance,
            CallbackInfo ci
    ) {
        if (!ArmorConfig.INSTANCE.shouldKeepElytraVisible()) {
            ci.cancel();
        }
    }
}