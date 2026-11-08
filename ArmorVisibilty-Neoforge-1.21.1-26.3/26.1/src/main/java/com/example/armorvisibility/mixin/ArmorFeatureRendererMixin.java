package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.example.armorvisibility.ArmorRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorFeatureRendererMixin {
    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void armorvisibility$cancelArmorRender(
            PoseStack matrices,
            SubmitNodeCollector queue,
            ItemStack stack,
            EquipmentSlot slot,
            int light,
            HumanoidRenderState state,
            CallbackInfo ci
    ) {
        if (slot == null) {
            return;
        }

        if (stack.is(Items.ELYTRA)) {
            if (!ArmorConfig.INSTANCE.shouldKeepElytraVisible()) {
                ci.cancel();
            }
            return;
        }

        if (ArmorConfig.INSTANCE.isPlayerOnly() && state.entityType != EntityType.PLAYER) {
            return;
        }

        if (!ArmorConfig.INSTANCE.isArmorVisible()) {
            ci.cancel();
            return;
        }

        boolean isOtherPlayer = isOtherPlayer(state);
        boolean shouldHideForCurrentEntity = ArmorConfig.INSTANCE.isArmorPartHidden(slot)
                && (!isOtherPlayer || ArmorConfig.INSTANCE.shouldHideForOtherPlayers());

        if (shouldHideForCurrentEntity) {
            ci.cancel();
        }
    }

    @Redirect(
            method = "renderArmorPiece",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;"
                            + "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;"
                            + "Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;"
                            + "Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/SubmitNodeCollector;II)V"
            )
    )
    private <S> void armorvisibility$renderWithOpacity(
            EquipmentLayerRenderer renderer,
            EquipmentClientInfo.LayerType layerType,
            ResourceKey<EquipmentAsset> equipmentAsset,
            Model<? super S> model,
            S modelState,
            ItemStack armorStack,
            PoseStack matrices,
            SubmitNodeCollector queue,
            int light,
            int outlineColor,
            PoseStack originalMatrices,
            SubmitNodeCollector originalQueue,
            ItemStack stack,
            EquipmentSlot slot,
            int originalLight,
            HumanoidRenderState state
    ) {
        boolean appliesSettings = !(ArmorConfig.INSTANCE.isPlayerOnly()
                && state.entityType != EntityType.PLAYER)
                && (!isOtherPlayer(state) || ArmorConfig.INSTANCE.shouldHideForOtherPlayers());
        float opacity = appliesSettings ? ArmorConfig.INSTANCE.getArmorOpacity(slot) : 1.0F;
        ArmorRenderContext.State previousState =
                ArmorRenderContext.setCurrentState(slot, opacity, appliesSettings);
        try {
            renderer.renderLayers(layerType, equipmentAsset, model, modelState, armorStack,
                    matrices, queue, light, outlineColor);
        } finally {
            ArmorRenderContext.restoreState(previousState);
        }
    }

    private boolean isOtherPlayer(HumanoidRenderState state) {
        if (state == null || state.entityType != EntityType.PLAYER || state.nameTag == null) {
            return false;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) {
            return false;
        }

        return !state.nameTag.equals(client.player.getDisplayName());
    }
}
