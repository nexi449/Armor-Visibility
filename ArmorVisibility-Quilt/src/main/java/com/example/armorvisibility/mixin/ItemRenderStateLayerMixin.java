package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemRenderState.LayerRenderState.class)
public abstract class ItemRenderStateLayerMixin {
    @Shadow
    Sprite particle;

    @Shadow
    int[] tints;

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V"
            ),
            index = 5
    )
    private int[] armorvisibility$applyOffhandOpacity(int[] tints) {
        if (!ArmorVisibilityRenderContext.hasOffhandOpacity()) {
            return tints;
        }

        float opacity = ArmorVisibilityRenderContext.getOffhandOpacity();
        if (opacity >= 1.0F) {
            return tints;
        }

        int[] adjustedTints = new int[tints.length + 1];
        for (int i = 0; i < tints.length; i++) {
            int color = tints[i];
            int alpha = Math.round((color >>> 24) * opacity);
            adjustedTints[i] = (color & 0x00FFFFFF) | (alpha << 24);
        }
        adjustedTints[tints.length] = (Math.round(255.0F * opacity) << 24) | 0x00FFFFFF;
        return adjustedTints;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V"
            ),
            index = 6
    )
    private List<BakedQuad> armorvisibility$applyOpacityToUntintedQuads(List<BakedQuad> quads) {
        if (!ArmorVisibilityRenderContext.hasOffhandOpacity()
                || ArmorVisibilityRenderContext.getOffhandOpacity() >= 1.0F) {
            return quads;
        }

        List<BakedQuad> adjustedQuads = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            if (quad.hasTint()) {
                adjustedQuads.add(quad);
            } else {
                adjustedQuads.add(new BakedQuad(
                        quad.position0(),
                        quad.position1(),
                        quad.position2(),
                        quad.position3(),
                        quad.packedUV0(),
                        quad.packedUV1(),
                        quad.packedUV2(),
                        quad.packedUV3(),
                        tints.length,
                        quad.face(),
                        quad.sprite(),
                        quad.shade(),
                        quad.lightEmission()
                ));
            }
        }
        return adjustedQuads;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V"
            ),
            index = 7
    )
    private RenderLayer armorvisibility$useTranslucentOffhandLayer(RenderLayer renderLayer) {
        if (!ArmorVisibilityRenderContext.hasOffhandOpacity()
                || ArmorVisibilityRenderContext.getOffhandOpacity() >= 1.0F
                || particle == null) {
            return renderLayer;
        }
        return RenderLayers.itemEntityTranslucentCull(particle.getAtlasId());
    }
}
