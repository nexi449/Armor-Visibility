package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

        if (ArmorVisibilityConfig.INSTANCE.isPlayerOnly() && state.entityType != EntityType.PLAYER) {
            return;
        }

        if (!ArmorVisibilityConfig.INSTANCE.isArmorVisible()) {
            ci.cancel();
            return;
        }

        boolean isOtherPlayer = isOtherPlayer(state);
        boolean shouldHideForCurrentEntity = ArmorVisibilityConfig.INSTANCE.isArmorPartHidden(slot)
                && (!isOtherPlayer || ArmorVisibilityConfig.INSTANCE.shouldHideForOtherPlayers());

        if (shouldHideForCurrentEntity) {
            ci.cancel();
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
