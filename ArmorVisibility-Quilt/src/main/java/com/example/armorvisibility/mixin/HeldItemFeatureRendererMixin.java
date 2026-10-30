package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityConfig;
import com.example.armorvisibility.ArmorVisibilityRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemFeatureRendererMixin {
    @Unique
    private Float armorvisibility$previousOffhandOpacity;

    @Unique
    private boolean armorvisibility$offhandContextSet;

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void armorvisibility$beginOffhandRender(
            ArmedEntityRenderState state,
            ItemRenderState itemState,
            ItemStack stack,
            Arm arm,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            CallbackInfo ci
    ) {
        if (arm == state.mainArm || stack.isEmpty()) {
            return;
        }

        boolean appliesSettings = !(ArmorVisibilityConfig.INSTANCE.isPlayerOnly()
                && state.entityType != EntityType.PLAYER)
                && (!armorvisibility$isOtherPlayer(state)
                || ArmorVisibilityConfig.INSTANCE.shouldHideForOtherPlayers());
        if (!appliesSettings) {
            return;
        }

        armorvisibility$previousOffhandOpacity =
                ArmorVisibilityRenderContext.setOffhandOpacity(ArmorVisibilityConfig.INSTANCE.getOffhandOpacity());
        armorvisibility$offhandContextSet = true;
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void armorvisibility$endOffhandRender(
            ArmedEntityRenderState state,
            ItemRenderState itemState,
            ItemStack stack,
            Arm arm,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            CallbackInfo ci
    ) {
        if (armorvisibility$offhandContextSet) {
            ArmorVisibilityRenderContext.restoreOffhandOpacity(armorvisibility$previousOffhandOpacity);
            armorvisibility$previousOffhandOpacity = null;
            armorvisibility$offhandContextSet = false;
        }
    }

    @Unique
    private boolean armorvisibility$isOtherPlayer(ArmedEntityRenderState state) {
        if (state.entityType != EntityType.PLAYER || state.displayName == null) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        return client.player != null && client.player.getDisplayName() != null
                && !state.displayName.equals(client.player.getDisplayName());
    }
}
