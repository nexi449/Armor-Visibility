package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeFeatureRendererMixin {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void armorvisibility$hideCapeWhenArmorIsHidden(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            AvatarRenderState state,
            float limbAngle,
            float limbDistance,
            CallbackInfo ci
    ) {
        boolean armorIsHidden = ArmorVisibilityConfig.INSTANCE.isArmorPartHidden(EquipmentSlot.CHEST);
        if (armorIsHidden && !ArmorVisibilityConfig.INSTANCE.shouldKeepCapeVisible()) {
            ci.cancel();
        }
    }
}