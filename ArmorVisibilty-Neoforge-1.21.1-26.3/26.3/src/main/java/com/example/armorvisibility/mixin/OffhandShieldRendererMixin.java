package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorRenderContext;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ShieldSpecialRenderer.class)
public abstract class OffhandShieldRendererMixin {
    private static final String SHIELD_SUBMIT =
            "submit(Lnet/minecraft/core/component/DataComponentMap;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V";
    private static final String SUBMIT_MODEL =
            "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel("
                    + "Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/"
                    + "UvMapping;I)V";

    @ModifyArg(
            method = SHIELD_SUBMIT,
            at = @At(value = "INVOKE", target = SUBMIT_MODEL),
            index = 3
    )
    private RenderType armorvisibility$useTranslucentGlint(RenderType renderType) {
        if (!armorvisibility$hasTranslucentOffhand()) {
            return renderType;
        }
        return RenderTypes.itemTranslucentGlint(Sheets.SHIELD_BASE.atlasLocation());
    }

    @ModifyArg(
            method = SHIELD_SUBMIT,
            at = @At(value = "INVOKE", target = SUBMIT_MODEL),
            index = 6
    )
    private int armorvisibility$applyOffhandOpacityToGlint(int color) {
        if (!armorvisibility$hasTranslucentOffhand()) {
            return color;
        }
        int alpha = Math.round((color >>> 24) * ArmorRenderContext.getOffhandOpacity());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static boolean armorvisibility$hasTranslucentOffhand() {
        return ArmorRenderContext.hasOffhandOpacity()
                && ArmorRenderContext.getOffhandOpacity() < 1.0F;
    }
}
