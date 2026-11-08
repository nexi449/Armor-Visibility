package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorRenderContext;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public abstract class ItemStackRenderStateLayerMixin {
    @Shadow
    private TextureAtlasSprite particleIcon;

    @Shadow
    private int[] tintLayers;

    @ModifyArg(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                            + "submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;"
                            + "Lnet/minecraft/client/renderer/rendertype/RenderType;"
                            + "Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"
            ),
            index = 5
    )
    private int[] armorvisibility$applyOffhandOpacity(int[] tints) {
        if (!ArmorRenderContext.hasOffhandOpacity()) {
            return tints;
        }

        float opacity = ArmorRenderContext.getOffhandOpacity();
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
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                            + "submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;"
                            + "Lnet/minecraft/client/renderer/rendertype/RenderType;"
                            + "Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"
            ),
            index = 6
    )
    private List<BakedQuad> armorvisibility$applyOpacityToUntintedQuads(List<BakedQuad> quads) {
        return quads;
    }

    @ModifyArg(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                            + "submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;"
                            + "Lnet/minecraft/client/renderer/rendertype/RenderType;"
                            + "Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"
            ),
            index = 7
    )
    private RenderType armorvisibility$useTranslucentOffhandLayer(RenderType renderType) {
        if (!ArmorRenderContext.hasOffhandOpacity()
                || ArmorRenderContext.getOffhandOpacity() >= 1.0F
                || particleIcon == null) {
            return renderType;
        }
        return RenderTypes.entityTranslucentCullItemTarget(particleIcon.atlasLocation());
    }
}
