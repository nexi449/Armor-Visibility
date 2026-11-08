package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Function;

@Mixin(OrderedSubmitNodeCollector.class)
public interface OffhandSpecialModelRendererMixin {
    @Overwrite
    default <S> void submitModel(
            Model<? super S> model,
            S state,
            PoseStack poseStack,
            Identifier texture,
            int lightCoords,
            int overlayCoords,
            int outlineColor
    ) {
        boolean translucent = armorvisibility$hasTranslucentOffhand();
        RenderType renderType = translucent
                ? RenderTypes.entityTranslucentCull(texture)
                : model.renderType(texture);
        int tintedColor = translucent ? armorvisibility$applyOffhandOpacity(-1) : -1;
        ((OrderedSubmitNodeCollector) this).submitModel(model, state, poseStack, renderType, lightCoords, overlayCoords,
                tintedColor, null, outlineColor);
    }

    @Overwrite
    default <S> void submitModel(
            Model<S> model,
            S state,
            PoseStack poseStack,
            int lightCoords,
            int overlayCoords,
            int tintedColor,
            SpriteId sprite,
            SpriteGetter sprites,
            int outlineColor
    ) {
        boolean translucent = armorvisibility$hasTranslucentOffhand();
        Function<Identifier, RenderType> renderType = translucent
                ? RenderTypes::entityTranslucentCull
                : model.renderType();
        ((OrderedSubmitNodeCollector) this).submitModel(model, state, poseStack, sprite.renderType(renderType), lightCoords,
                overlayCoords, translucent ? armorvisibility$applyOffhandOpacity(tintedColor) : tintedColor,
                sprites.get(sprite), outlineColor);
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
