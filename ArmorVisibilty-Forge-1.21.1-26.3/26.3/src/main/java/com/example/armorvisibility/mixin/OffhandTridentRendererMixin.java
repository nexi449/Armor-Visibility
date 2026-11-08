package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.TridentSpecialRenderer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TridentSpecialRenderer.class)
public abstract class OffhandTridentRendererMixin {
    private static final String TRIDENT_SUBMIT =
            "submit(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V";
    private static final String SUBMIT_MODEL =
            "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel("
                    + "Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/rendertype/RenderType;III)V";
    private static final String SUBMIT_MODEL_TEXTURE =
            "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel("
                    + "Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/resources/Identifier;III)V";

    @Redirect(
            method = TRIDENT_SUBMIT,
            at = @At(value = "INVOKE", target = SUBMIT_MODEL_TEXTURE)
    )
    private <S> void armorvisibility$submitTranslucentOffhandTrident(
            SubmitNodeCollector collector,
            Model<? super S> model,
            S state,
            PoseStack poseStack,
            Identifier texture,
            int light,
            int overlay,
            int color
    ) {
        if (!armorvisibility$hasTranslucentOffhand()) {
            collector.submitModel(model, state, poseStack, texture, light, overlay, color);
            return;
        }

        collector.submitModel(
                model,
                state,
                poseStack,
                RenderTypes.entityTranslucent(texture),
                light,
                overlay,
                armorvisibility$applyOpacity(color)
        );
    }

    @ModifyArg(
            method = TRIDENT_SUBMIT,
            at = @At(value = "INVOKE", target = SUBMIT_MODEL),
            index = 3
    )
    private RenderType armorvisibility$useTranslucentGlint(RenderType renderType) {
        if (!armorvisibility$hasTranslucentOffhand()) {
            return renderType;
        }
        return RenderTypes.itemTranslucentGlint(TridentModel.TEXTURE);
    }

    @ModifyArg(
            method = TRIDENT_SUBMIT,
            at = @At(value = "INVOKE", target = SUBMIT_MODEL),
            index = 6
    )
    private int armorvisibility$applyOffhandOpacityToTrident(int color) {
        if (!armorvisibility$hasTranslucentOffhand()) {
            return color;
        }
        return armorvisibility$applyOpacity(color);
    }

    private static int armorvisibility$applyOpacity(int color) {
        int alpha = Math.round((color >>> 24) * ArmorRenderContext.getOffhandOpacity());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static boolean armorvisibility$hasTranslucentOffhand() {
        return ArmorRenderContext.hasOffhandOpacity()
                && ArmorRenderContext.getOffhandOpacity() < 1.0F;
    }
}
