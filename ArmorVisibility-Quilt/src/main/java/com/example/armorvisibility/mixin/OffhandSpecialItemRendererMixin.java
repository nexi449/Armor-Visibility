package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(OrderedRenderCommandQueueImpl.class)
public abstract class OffhandSpecialItemRendererMixin {
    @ModifyArgs(
            method = "submitModelPart",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/BatchingRenderCommandQueue;submitModelPart(Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IILnet/minecraft/client/texture/Sprite;ZZILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;I)V"
            )
    )
    private void armorvisibility$useTranslucentLayerForSpecialOffhandItems(Args args) {
        if (!ArmorVisibilityRenderContext.hasOffhandOpacity()
                || ArmorVisibilityRenderContext.getOffhandOpacity() >= 1.0F) {
            return;
        }

        Sprite sprite = args.get(5);
        if (sprite != null) {
            args.set(2, RenderLayers.entityTranslucent(sprite.getAtlasId()));
        }
    }

    @ModifyArg(
            method = "submitModelPart",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/BatchingRenderCommandQueue;submitModelPart(Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IILnet/minecraft/client/texture/Sprite;ZZILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;I)V"
            ),
            index = 8
    )
    private int armorvisibility$applyOpacityToSpecialOffhandItems(int color) {
        if (!ArmorVisibilityRenderContext.hasOffhandOpacity()) {
            return color;
        }

        int alpha = Math.round((color >>> 24) * ArmorVisibilityRenderContext.getOffhandOpacity());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
