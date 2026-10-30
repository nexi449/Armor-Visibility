package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityRenderContext;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public abstract class ItemStackRenderStateLayerMixin {
    @Shadow
    @Final
    private IntList tintLayers;

    @ModifyArg(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;III[ILnet/minecraft/client/resources/model/geometry/ItemQuads;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"
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
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;III[ILnet/minecraft/client/resources/model/geometry/ItemQuads;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"
            ),
            index = 6
    )
    private ItemQuads armorvisibility$makeOffhandQuadsTranslucent(ItemQuads quads) {
        if (!ArmorVisibilityRenderContext.hasOffhandOpacity()
                || ArmorVisibilityRenderContext.getOffhandOpacity() >= 1.0F) {
            return quads;
        }

        List<BakedQuad> adjustedQuads = new ArrayList<>(quads.all().size());
        for (BakedQuad quad : quads.all()) {
            BakedQuad.MaterialInfo material = quad.materialInfo();
            int tintIndex = material.isTinted() ? material.tintIndex() : tintLayers.size();
            TextureAtlasSprite sprite = material.sprite();
            BakedQuad.MaterialInfo adjustedMaterial = new BakedQuad.MaterialInfo(
                    sprite,
                    ChunkSectionLayer.TRANSLUCENT,
                    RenderTypes.itemTranslucent(sprite.atlasLocation()),
                    RenderTypes.itemTranslucentGlint(sprite.atlasLocation()),
                    RenderTypes.itemTranslucentGlintSpecial(sprite.atlasLocation()),
                    tintIndex,
                    material.shadeDirectionOverride(),
                    material.lightEmission()
            );
            adjustedQuads.add(new BakedQuad(
                    quad.position0(),
                    quad.position1(),
                    quad.position2(),
                    quad.position3(),
                    quad.packedUV0(),
                    quad.packedUV1(),
                    quad.packedUV2(),
                    quad.packedUV3(),
                    quad.direction(),
                    adjustedMaterial
            ));
        }
        return new ItemQuads(adjustedQuads, List.of(), adjustedQuads);
    }
}
