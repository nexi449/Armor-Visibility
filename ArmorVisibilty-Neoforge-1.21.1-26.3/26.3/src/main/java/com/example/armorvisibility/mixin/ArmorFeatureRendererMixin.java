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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorFeatureRendererMixin {
    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void armorvisibility$cancelArmorRender(PoseStack matrices, SubmitNodeCollector queue,
                                                    ItemStack stack, EquipmentSlot slot, int light,
                                                    HumanoidRenderState state, CallbackInfo ci) {
        if (slot == null) return;
        if (stack.is(Items.ELYTRA)) {
            if (!ArmorConfig.INSTANCE.shouldKeepElytraVisible()) ci.cancel();
            return;
        }
        if (ArmorConfig.INSTANCE.isPlayerOnly() && (Minecraft.getInstance().player == null
            || state.entityType != Minecraft.getInstance().player.getType())) return;
        if (!ArmorConfig.INSTANCE.isArmorVisible()) {
            ci.cancel();
            return;
        }
        boolean isOtherPlayer = isOtherPlayer(state);
        if (ArmorConfig.INSTANCE.isArmorPartHidden(slot)
                && (!isOtherPlayer || ArmorConfig.INSTANCE.shouldHideForOtherPlayers())) ci.cancel();
    }

    @Redirect(
            method = "renderArmorPiece",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;II)V"
            )
    )
    private <S> void armorvisibility$renderArmorWithOpacity(
            EquipmentLayerRenderer renderer,
            EquipmentClientInfo.LayerType layerType,
            ResourceKey<EquipmentAsset> equipmentAsset,
            Model<? super S> model,
            S modelState,
            ItemStack armorStack,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            int outlineColor,
            PoseStack originalPoseStack,
            SubmitNodeCollector originalCollector,
            ItemStack originalStack,
            EquipmentSlot slot,
            int originalLight,
            HumanoidRenderState state
    ) {
        boolean appliesSettings = !(ArmorConfig.INSTANCE.isPlayerOnly()
                && (Minecraft.getInstance().player == null
                || state.entityType != Minecraft.getInstance().player.getType()))
                && (!isOtherPlayer(state) || ArmorConfig.INSTANCE.shouldHideForOtherPlayers());
        ArmorRenderContext.ArmorState previous =
                ArmorRenderContext.setArmorState(
                        slot,
                        appliesSettings ? ArmorConfig.INSTANCE.getArmorOpacity(slot) : 1.0F,
                        appliesSettings
                );
        try {
            renderer.renderLayers(layerType, equipmentAsset, model, modelState, armorStack,
                    poseStack, collector, light, outlineColor);
        } finally {
            ArmorRenderContext.restoreArmorState(previous);
        }
    }

    private boolean isOtherPlayer(HumanoidRenderState state) {
        if (state == null || state.nameTag == null) return false;
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || state.entityType != client.player.getType()) return false;
        return !state.nameTag.equals(client.player.getDisplayName());
    }
}
