package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityRenderContext;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(BannerRenderer.class)
public abstract class OffhandShieldPatternMixin {
    @ModifyArgs(
            method = "submitPatternLayer(Lnet/minecraft/client/resources/model/sprite/SpriteGetter;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;IILnet/minecraft/client/model/Model;"
                    + "Ljava/lang/Object;Lnet/minecraft/client/resources/model/sprite/SpriteId;"
                    + "Lnet/minecraft/world/item/DyeColor;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel("
                            + "Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"
            )
    )
    private static void armorvisibility$applyOffhandOpacityToBannerPattern(Args args) {
        if (!ArmorVisibilityRenderContext.hasOffhandOpacity()
                || ArmorVisibilityRenderContext.getOffhandOpacity() >= 1.0F) {
            return;
        }

        if (args.get(7) instanceof TextureAtlasSprite sprite) {
            args.set(3, RenderTypes.entityTranslucentCull(sprite.atlasLocation()));
        }
        int color = args.get(6);
        int alpha = Math.round((color >>> 24) * ArmorVisibilityRenderContext.getOffhandOpacity());
        args.set(6, (color & 0x00FFFFFF) | (alpha << 24));
    }
}
