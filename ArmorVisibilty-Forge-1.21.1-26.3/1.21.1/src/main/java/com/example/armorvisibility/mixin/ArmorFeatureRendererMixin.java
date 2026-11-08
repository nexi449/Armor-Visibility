package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.example.armorvisibility.ArmorRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorFeatureRendererMixin {
    @Unique
    private static final String RENDER_ARMOR_PIECE_METHOD =
            "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;"
                    + "Lnet/minecraft/world/entity/LivingEntity;"
                    + "Lnet/minecraft/world/entity/EquipmentSlot;I"
                    + "Lnet/minecraft/client/model/HumanoidModel;)V";

    @Unique
    private static final String RENDER_MODEL_METHOD =
            "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;I"
                    + "Lnet/minecraft/client/model/Model;I"
                    + "Lnet/minecraft/resources/ResourceLocation;)V";

    @Unique
    private ArmorRenderContext.State armorvisibility$previousState;

    @Unique
    private ArmorRenderContext.State armorvisibility$piecePreviousState;

    @Inject(method = "render", at = @At("HEAD"), remap = false)
    private void armorvisibility$beginEntityRender(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            LivingEntity entity,
            float limbAngle,
            float limbDistance,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        boolean appliesSettings = !(ArmorConfig.INSTANCE.isPlayerOnly()
                && !(entity instanceof Player))
                && (!(entity instanceof Player player) || !armorvisibility$isOtherPlayer(player)
                || ArmorConfig.INSTANCE.shouldHideForOtherPlayers());
        armorvisibility$previousState =
                ArmorRenderContext.setCurrentState(null, 1.0F, appliesSettings);
    }

    @Inject(method = "render", at = @At("RETURN"), remap = false)
    private void armorvisibility$endEntityRender(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            LivingEntity entity,
            float limbAngle,
            float limbDistance,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        ArmorRenderContext.restoreState(armorvisibility$previousState);
        armorvisibility$previousState = null;
    }

    @Inject(method = RENDER_ARMOR_PIECE_METHOD, at = @At("HEAD"), cancellable = true, remap = false)
    private void armorvisibility$applyArmorSettings(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            LivingEntity entity,
            EquipmentSlot slot,
            int packedLight,
            HumanoidModel<?> model,
            CallbackInfo ci
    ) {
        ItemStack stack = entity.getItemBySlot(slot);
        if (stack.is(Items.ELYTRA)) {
            if (!ArmorConfig.INSTANCE.shouldKeepElytraVisible()) {
                ci.cancel();
            }
            return;
        }

        if (!ArmorRenderContext.shouldApplySettings()) {
            return;
        }

        armorvisibility$piecePreviousState = ArmorRenderContext.setCurrentState(
                slot, ArmorConfig.INSTANCE.getArmorOpacity(slot), true);

        if (!ArmorConfig.INSTANCE.isArmorVisible()
                || ArmorConfig.INSTANCE.isArmorPartHidden(slot)) {
            ArmorRenderContext.restoreState(armorvisibility$piecePreviousState);
            armorvisibility$piecePreviousState = null;
            ci.cancel();
        }
    }

    @Inject(method = RENDER_ARMOR_PIECE_METHOD, at = @At("RETURN"), remap = false)
    private void armorvisibility$endArmorPiece(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            LivingEntity entity,
            EquipmentSlot slot,
            int packedLight,
            HumanoidModel<?> model,
            CallbackInfo ci
    ) {
        if (armorvisibility$piecePreviousState != null
                || ArmorRenderContext.getCurrentSlot() != null) {
            ArmorRenderContext.restoreState(armorvisibility$piecePreviousState);
            armorvisibility$piecePreviousState = null;
        }
    }

    @Redirect(
            method = RENDER_ARMOR_PIECE_METHOD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasFoil()Z"), remap = false
    )
    private boolean armorvisibility$respectGlintSetting(ItemStack stack) {
        EquipmentSlot slot = ArmorRenderContext.getCurrentSlot();
        return stack.hasFoil() && (slot == null
                || !ArmorRenderContext.shouldApplySettings()
                || ArmorConfig.INSTANCE.shouldShowGlint(slot));
    }

    @Redirect(
            method = RENDER_MODEL_METHOD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull("
                            + "Lnet/minecraft/resources/ResourceLocation;)"
                            + "Lnet/minecraft/client/renderer/RenderType;"
            ), remap = false
    )
    private net.minecraft.client.renderer.RenderType armorvisibility$useTranslucentLayer(
            net.minecraft.resources.ResourceLocation texture
    ) {
        if (ArmorRenderContext.getCurrentSlot() != null
                && ArmorRenderContext.getCurrentOpacity() < 1.0F) {
            return net.minecraft.client.renderer.RenderType.entityTranslucent(texture);
        }
        return net.minecraft.client.renderer.RenderType.armorCutoutNoCull(texture);
    }

    @ModifyArg(
            method = RENDER_MODEL_METHOD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;renderToBuffer("
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            ),
            index = 4, remap = false
    )
    private int armorvisibility$applyOpacity(int color) {
        if (ArmorRenderContext.getCurrentSlot() == null) {
            return color;
        }

        int alpha = Math.round((color >>> 24) * ArmorRenderContext.getCurrentOpacity());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    @Unique
    private boolean armorvisibility$isOtherPlayer(Player player) {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && player != client.player
                && !player.getDisplayName().equals(client.player.getDisplayName());
    }
}
