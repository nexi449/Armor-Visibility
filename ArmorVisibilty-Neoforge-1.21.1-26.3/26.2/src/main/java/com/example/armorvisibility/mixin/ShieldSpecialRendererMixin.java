package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorRenderContext;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ShieldSpecialRenderer.class)
public abstract class ShieldSpecialRendererMixin {
    @ModifyArgs(
            method = "submit(Lnet/minecraft/core/component/DataComponentMap;"
                    + "Lnet/minecraft/world/item/ItemDisplayContext;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                            + "submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
                            + "ZZILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;I)V"
            )
    )
    private void armorvisibility$applyOffhandOpacity(Args args) {
        if (!ArmorRenderContext.hasOffhandOpacity()) {
            return;
        }

        TextureAtlasSprite sprite = args.get(5);
        if (sprite != null) {
            args.set(2, RenderTypes.entityTranslucent(sprite.atlasLocation()));
        }
        args.set(8, armorvisibility$scaleAlpha(args.get(8)));
    }

    private int armorvisibility$scaleAlpha(int color) {
        int alpha = Math.round((color >>> 24) * ArmorRenderContext.getOffhandOpacity());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
