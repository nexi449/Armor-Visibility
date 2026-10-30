package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityRenderContext;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

@Mixin(OrderedSubmitNodeCollector.class)
public interface OffhandSpecialModelRendererMixin {
    @Redirect(
            method = "submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/Identifier;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;renderType(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"
            )
    )
    default RenderType armorvisibility$useTranslucentTypeForSpecialOffhand(
            Model<?> model, Identifier texture) {
        if (armorvisibility$hasTranslucentOffhand()) {
            return RenderTypes.entityTranslucentCull(texture);
        }
        return model.renderType(texture);
    }

    @Redirect(
            method = "submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;"
                    + "Lnet/minecraft/client/resources/model/sprite/SpriteGetter;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/sprite/SpriteId;renderType(Ljava/util/function/Function;)Lnet/minecraft/client/renderer/rendertype/RenderType;"
            )
    )
    default RenderType armorvisibility$useTranslucentTypeForSpecialOffhand(
            SpriteId sprite, Function<Identifier, RenderType> renderType) {
        if (armorvisibility$hasTranslucentOffhand()) {
            return sprite.renderType(RenderTypes::entityTranslucentCull);
        }
        return sprite.renderType(renderType);
    }

    @ModifyArg(
            method = "submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/Identifier;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel("
                    + "Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"),
            index = 6
    )
    default int armorvisibility$applyOpacityToTexturedSpecialOffhand(int color) {
        return armorvisibility$applyOffhandOpacity(color);
    }

    @ModifyArg(
            method = "submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;"
                    + "Lnet/minecraft/client/resources/model/sprite/SpriteGetter;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel("
                    + "Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"),
            index = 6
    )
    default int armorvisibility$applyOpacityToSpriteSpecialOffhand(int color) {
        return armorvisibility$applyOffhandOpacity(color);
    }

    default int armorvisibility$applyOffhandOpacity(int color) {
        if (!armorvisibility$hasTranslucentOffhand()) {
            return color;
        }
        int alpha = Math.round((color >>> 24) * ArmorVisibilityRenderContext.getOffhandOpacity());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private static boolean armorvisibility$hasTranslucentOffhand() {
        return ArmorVisibilityRenderContext.hasOffhandOpacity()
                && ArmorVisibilityRenderContext.getOffhandOpacity() < 1.0F;
    }
}
