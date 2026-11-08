package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorRenderContext;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.TridentSpecialRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(TridentSpecialRenderer.class)
public abstract class OffhandTridentRendererMixin {
    @ModifyArgs(
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel("
                            + "Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/rendertype/RenderType;III)V"
            )
    )
    private void armorvisibility$applyOffhandOpacityToGlint(Args args) {
        if (!ArmorRenderContext.hasOffhandOpacity()
                || ArmorRenderContext.getOffhandOpacity() >= 1.0F) {
            return;
        }

        args.set(3, RenderTypes.entityTranslucentCull(TridentModel.TEXTURE));
        int color = args.get(6);
        int alpha = Math.round((color >>> 24) * ArmorRenderContext.getOffhandOpacity());
        args.set(6, (color & 0x00FFFFFF) | (alpha << 24));
    }
}
