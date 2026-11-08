package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.example.armorvisibility.ArmorRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {
    @Unique
    private Float armorvisibility$previousOffhandOpacity;

    @Unique
    private boolean armorvisibility$offhandContextSet;

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), remap = false)
    private void armorvisibility$beginOffhandRender(
            ArmedEntityRenderState state,
            ItemStackRenderState itemState,
            ItemStack stack,
            HumanoidArm arm,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            CallbackInfo ci
    ) {
        if (arm == state.mainArm || stack.isEmpty()) {
            return;
        }

        boolean appliesSettings = !(ArmorConfig.INSTANCE.isPlayerOnly()
                && state.entityType != EntityType.PLAYER)
                && (!armorvisibility$isOtherPlayer(state)
                || ArmorConfig.INSTANCE.shouldHideForOtherPlayers());
        if (!appliesSettings) {
            return;
        }

        armorvisibility$previousOffhandOpacity =
                ArmorRenderContext.setOffhandOpacity(ArmorConfig.INSTANCE.getOffhandOpacity());
        armorvisibility$offhandContextSet = true;
    }

    @Inject(method = "submitArmWithItem", at = @At("RETURN"), remap = false)
    private void armorvisibility$endOffhandRender(
            ArmedEntityRenderState state,
            ItemStackRenderState itemState,
            ItemStack stack,
            HumanoidArm arm,
            PoseStack poseStack,
            SubmitNodeCollector collector,
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
    private boolean armorvisibility$isOtherPlayer(ArmedEntityRenderState state) {
        if (state.entityType != EntityType.PLAYER || state.nameTag == null) {
            return false;
        }

        Minecraft client = Minecraft.getInstance();
        return client.player != null && client.player.getDisplayName() != null
                && !state.nameTag.equals(client.player.getDisplayName());
    }
}
