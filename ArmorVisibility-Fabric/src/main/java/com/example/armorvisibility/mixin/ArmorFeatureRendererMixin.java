package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.example.armorvisibility.ArmorRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin {

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private void armorvisibility$cancelArmorRender(
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            ItemStack stack,
            EquipmentSlot slot,
            int light,
            BipedEntityRenderState state,
            CallbackInfo ci
    ) {
        if (slot == null) {
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
            method = "renderArmor",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/equipment/EquipmentRenderer;render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;II)V"
            )
    )
    private <S> void armorvisibility$renderWithOpacity(
            EquipmentRenderer equipmentRenderer,
            EquipmentModel.LayerType layerType,
            RegistryKey<EquipmentAsset> equipmentAsset,
            Model<? super S> model,
            S modelState,
            ItemStack armorStack,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            int overlay,
            MatrixStack armorMatrices,
            OrderedRenderCommandQueue armorQueue,
            ItemStack armorItem,
            EquipmentSlot slot,
            int armorLight,
            BipedEntityRenderState state
    ) {
        boolean appliesSettings = !(ArmorConfig.INSTANCE.isPlayerOnly() && state.entityType != EntityType.PLAYER)
                && (!isOtherPlayer(state) || ArmorConfig.INSTANCE.shouldHideForOtherPlayers());
        float opacity = appliesSettings ? ArmorConfig.INSTANCE.getArmorOpacity(slot) : 1.0F;
        ArmorRenderContext.State previousState =
                ArmorRenderContext.setCurrentState(slot, opacity, appliesSettings);
        try {
            equipmentRenderer.render(layerType, equipmentAsset, model, modelState, armorStack, matrices, queue, light, overlay);
        } finally {
            ArmorRenderContext.restoreState(previousState);
        }
    }

    private boolean isOtherPlayer(BipedEntityRenderState state) {
        if (state == null || state.entityType != EntityType.PLAYER || state.displayName == null) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.player.getDisplayName() == null) {
            return false;
        }

        return !state.displayName.equals(client.player.getDisplayName());
    }
}
