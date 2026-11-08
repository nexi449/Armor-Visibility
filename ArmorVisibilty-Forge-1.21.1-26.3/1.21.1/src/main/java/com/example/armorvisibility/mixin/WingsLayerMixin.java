package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraLayer.class)
public abstract class WingsLayerMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void armorvisibility$hideElytra(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            LivingEntity entity,
            float limbAngle,
            float limbDistance,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (!ArmorConfig.INSTANCE.shouldKeepElytraVisible()) {
            ci.cancel();
        }
    }
}
