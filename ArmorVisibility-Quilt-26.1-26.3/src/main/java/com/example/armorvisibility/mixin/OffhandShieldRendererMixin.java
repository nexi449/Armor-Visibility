package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityRenderContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ShieldSpecialRenderer.class)
public abstract class OffhandShieldRendererMixin {
    @ModifyArgs(
            method = "submit(Lnet/minecraft/core/component/DataComponentMap;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel("
                            + "Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/rendertype/RenderType;III"
                            + "Lnet/minecraft/client/renderer/texture/UvMapping;I)V"
            )
    )
    private void armorvisibility$applyOffhandOpacityToGlint(Args args) {
        if (!armorvisibility$hasTranslucentOffhand()) {
            return;
        }

        if (args.get(7) instanceof TextureAtlasSprite sprite) {
            args.set(3, RenderTypes.itemTranslucentGlint(sprite.atlasLocation()));
        }
        args.set(6, armorvisibility$applyOpacity(args.get(6)));
    }

    private static boolean armorvisibility$hasTranslucentOffhand() {
        return ArmorVisibilityRenderContext.hasOffhandOpacity()
                && ArmorVisibilityRenderContext.getOffhandOpacity() < 1.0F;
    }

    private static int armorvisibility$applyOpacity(int color) {
        int alpha = Math.round((color >>> 24) * ArmorVisibilityRenderContext.getOffhandOpacity());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
