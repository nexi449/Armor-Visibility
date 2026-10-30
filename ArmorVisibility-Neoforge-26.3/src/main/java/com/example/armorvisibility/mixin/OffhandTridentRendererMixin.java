package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityRenderContext;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.TridentSpecialRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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
        int alpha = Math.round((color >>> 24) * ArmorVisibilityRenderContext.getOffhandOpacity());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static boolean armorvisibility$hasTranslucentOffhand() {
        return ArmorVisibilityRenderContext.hasOffhandOpacity()
                && ArmorVisibilityRenderContext.getOffhandOpacity() < 1.0F;
    }
}
