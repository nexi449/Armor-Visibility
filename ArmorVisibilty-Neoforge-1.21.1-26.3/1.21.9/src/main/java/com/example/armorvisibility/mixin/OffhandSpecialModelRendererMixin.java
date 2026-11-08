package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(OrderedSubmitNodeCollector.class)
public interface OffhandSpecialModelRendererMixin {
    @Overwrite
    default void submitModelPart(
            ModelPart modelPart,
            PoseStack poseStack,
            RenderType renderType,
            int lightCoords,
            int overlayCoords,
            TextureAtlasSprite sprite
    ) {
        boolean applyOpacity = armorvisibility$hasTranslucentOffhand();
        ((OrderedSubmitNodeCollector) this).submitModelPart(modelPart, poseStack,
                applyOpacity && sprite != null ? RenderType.entityTranslucent(sprite.atlasLocation()) : renderType,
                lightCoords, overlayCoords, sprite, false, false,
                applyOpacity ? armorvisibility$applyOffhandOpacity(-1) : -1, null, 0);
    }

    @Overwrite
    default void submitModelPart(
            ModelPart modelPart,
            PoseStack poseStack,
            RenderType renderType,
            int lightCoords,
            int overlayCoords,
            TextureAtlasSprite sprite,
            int tintedColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        boolean applyOpacity = armorvisibility$hasTranslucentOffhand();
        ((OrderedSubmitNodeCollector) this).submitModelPart(modelPart, poseStack,
                applyOpacity && sprite != null ? RenderType.entityTranslucent(sprite.atlasLocation()) : renderType,
                lightCoords, overlayCoords, sprite, false, false,
                applyOpacity ? armorvisibility$applyOffhandOpacity(tintedColor) : tintedColor,
                crumblingOverlay, 0);
    }

    @Overwrite
    default void submitModelPart(
            ModelPart modelPart,
            PoseStack poseStack,
            RenderType renderType,
            int lightCoords,
            int overlayCoords,
            TextureAtlasSprite sprite,
            boolean sheeted,
            boolean hasFoil
    ) {
        boolean applyOpacity = armorvisibility$hasTranslucentOffhand();
        ((OrderedSubmitNodeCollector) this).submitModelPart(modelPart, poseStack,
                applyOpacity && sprite != null ? RenderType.entityTranslucent(sprite.atlasLocation()) : renderType,
                lightCoords, overlayCoords, sprite, sheeted, hasFoil,
                applyOpacity ? armorvisibility$applyOffhandOpacity(-1) : -1, null, 0);
    }

    @Unique
    private static boolean armorvisibility$hasTranslucentOffhand() {
        return ArmorRenderContext.hasOffhandOpacity()
                && ArmorRenderContext.getOffhandOpacity() < 1.0F;
    }

    @Unique
    private static int armorvisibility$applyOffhandOpacity(int color) {
        int alpha = Math.round((color >>> 24) * ArmorRenderContext.getOffhandOpacity());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
