package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorRenderContext;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BannerRenderer.class)
public abstract class BannerRendererMixin {
    @ModifyArgs(
            method = {"submitPatterns", "submitPatternLayer"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                            + "submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
                            + "ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
            )
    )
    private static void armorvisibility$applyOffhandOpacity(Args args) {
        if (!ArmorRenderContext.hasOffhandOpacity()) {
            return;
        }

        TextureAtlasSprite sprite = args.get(7);
        if (sprite != null) {
            args.set(3, RenderTypes.entityTranslucent(sprite.atlasLocation()));
        }

        int color = args.get(6);
        int alpha = Math.round((color >>> 24) * ArmorRenderContext.getOffhandOpacity());
        args.set(6, (color & 0x00FFFFFF) | (alpha << 24));
    }
}
