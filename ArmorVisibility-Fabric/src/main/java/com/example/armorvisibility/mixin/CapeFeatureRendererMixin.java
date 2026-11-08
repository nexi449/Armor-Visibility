package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void armorvisibility$hideCapeWhenDisabled(
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            PlayerEntityRenderState state,
            float limbAngle,
            float limbDistance,
            CallbackInfo ci
    ) {
        if (!ArmorConfig.INSTANCE.shouldKeepCapeVisible()) {
            ci.cancel();
        }
    }
}