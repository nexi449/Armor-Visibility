package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WingsLayer.class)
public abstract class ElytraFeatureRendererMixin {
	@Inject(method = "submit", at = @At("HEAD"), cancellable = true)
	private void armorvisibility$hideElytraWhenDisabled(
			PoseStack poseStack,
			SubmitNodeCollector collector,
			int light,
			HumanoidRenderState state,
			float limbAngle,
			float limbDistance,
			CallbackInfo ci
	) {
		if (!ArmorVisibilityConfig.INSTANCE.shouldKeepElytraVisible()) {
			ci.cancel();
		}
	}
}