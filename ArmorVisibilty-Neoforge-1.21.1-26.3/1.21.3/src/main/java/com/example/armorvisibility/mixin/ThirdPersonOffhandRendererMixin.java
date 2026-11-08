package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.example.armorvisibility.ArmorRenderContext;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ThirdPersonOffhandRendererMixin {
    @Unique
    private Float armorvisibility$previousOffhandOpacity;

    @Unique
    private boolean armorvisibility$offhandContextSet;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void armorvisibility$beginThirdPersonOffhandRender(
            LivingEntityRenderState state,
            BakedModel model,
            ItemStack stack,
            ItemDisplayContext displayContext,
            HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            CallbackInfo ci
    ) {
        if (arm == state.mainArm || stack.isEmpty() || !armorvisibility$shouldApply(state)) {
            return;
        }

        armorvisibility$previousOffhandOpacity =
                ArmorRenderContext.setOffhandOpacity(ArmorConfig.INSTANCE.getOffhandOpacity());
        armorvisibility$offhandContextSet = true;
    }

    @Inject(method = "renderArmWithItem", at = @At("RETURN"))
    private void armorvisibility$endThirdPersonOffhandRender(
            LivingEntityRenderState state,
            BakedModel model,
            ItemStack stack,
            ItemDisplayContext displayContext,
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
    private boolean armorvisibility$shouldApply(LivingEntityRenderState state) {
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
