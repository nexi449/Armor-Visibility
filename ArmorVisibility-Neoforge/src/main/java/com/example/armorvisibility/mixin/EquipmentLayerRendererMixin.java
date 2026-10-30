package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityConfig;
import com.example.armorvisibility.ArmorVisibilityRenderContext;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EquipmentLayerRenderer.class)
public abstract class EquipmentLayerRendererMixin {
    private static final String RENDER_LAYERS_METHOD =
            "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;"
                    + "Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;"
                    + "Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V";

    @Redirect(
            method = RENDER_LAYERS_METHOD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;"
                            + "armorCutoutNoCull(Lnet/minecraft/resources/Identifier;)"
                            + "Lnet/minecraft/client/renderer/rendertype/RenderType;"
            )
    )
    private RenderType armorvisibility$useTranslucentLayer(Identifier texture) {
        if (ArmorVisibilityRenderContext.getCurrentSlot() != null
                && ArmorVisibilityRenderContext.getCurrentOpacity() < 1.0F) {
            return RenderTypes.armorTranslucent(texture);
        }
        return RenderTypes.armorCutoutNoCull(texture);
    }

    @Redirect(
            method = RENDER_LAYERS_METHOD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;hasFoil()Z"
            )
    )
    private boolean armorvisibility$respectGlintSetting(ItemStack stack) {
        var slot = ArmorVisibilityRenderContext.getCurrentSlot();
        return stack.hasFoil() && (slot == null
                || !ArmorVisibilityRenderContext.shouldApplySettings()
                || ArmorVisibilityConfig.INSTANCE.shouldShowGlint(slot));
    }

    @ModifyArg(
            method = RENDER_LAYERS_METHOD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;"
                            + "submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
                            + "ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
                    ordinal = 0
            ),
            index = 6
    )
    private int armorvisibility$applyOpacity(int color) {
        if (ArmorVisibilityRenderContext.getCurrentSlot() == null) {
            return color;
        }

        int alpha = Math.round((color >>> 24) * ArmorVisibilityRenderContext.getCurrentOpacity());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
