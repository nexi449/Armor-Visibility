package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorFeatureRendererMixin {
    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void armorvisibility$cancelArmorRender(PoseStack matrices, SubmitNodeCollector queue,
                                                    ItemStack stack, EquipmentSlot slot, int light,
                                                    HumanoidRenderState state, CallbackInfo ci) {
        if (slot == null) return;
        if (stack.is(Items.ELYTRA)) {
            if (!ArmorVisibilityConfig.INSTANCE.shouldKeepElytraVisible()) ci.cancel();
            return;
        }
        if (ArmorVisibilityConfig.INSTANCE.isPlayerOnly() && (Minecraft.getInstance().player == null
            || state.entityType != Minecraft.getInstance().player.getType())) return;
        if (!ArmorVisibilityConfig.INSTANCE.isArmorVisible()) {
            ci.cancel();
            return;
        }
        boolean isOtherPlayer = isOtherPlayer(state);
        if (ArmorVisibilityConfig.INSTANCE.isArmorPartHidden(slot)
                && (!isOtherPlayer || ArmorVisibilityConfig.INSTANCE.shouldHideForOtherPlayers())) ci.cancel();
    }

    private boolean isOtherPlayer(HumanoidRenderState state) {
        if (state == null || state.nameTag == null) return false;
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || state.entityType != client.player.getType()) return false;
        return !state.nameTag.equals(client.player.getDisplayName());
    }
}
