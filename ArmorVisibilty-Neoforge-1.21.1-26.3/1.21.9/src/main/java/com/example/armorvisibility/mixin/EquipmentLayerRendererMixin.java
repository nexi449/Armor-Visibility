package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.example.armorvisibility.ArmorRenderContext;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.resources.ResourceLocation;
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
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/ResourceLocation;II)V";

    @Redirect(
            method = RENDER_LAYERS_METHOD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderType;"
                            + "armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)"
                            + "Lnet/minecraft/client/renderer/RenderType;"
            )
    )
    private RenderType armorvisibility$useTranslucentLayer(ResourceLocation texture) {
        if (ArmorRenderContext.getCurrentSlot() != null
                && ArmorRenderContext.getCurrentOpacity() < 1.0F) {
            return RenderType.armorTranslucent(texture);
        }
        return RenderType.armorCutoutNoCull(texture);
    }

    @Redirect(
            method = RENDER_LAYERS_METHOD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;hasFoil()Z"
            )
    )
    private boolean armorvisibility$respectGlintSetting(ItemStack stack) {
        var slot = ArmorRenderContext.getCurrentSlot();
        return stack.hasFoil() && (slot == null
                || !ArmorRenderContext.shouldApplySettings()
                || ArmorConfig.INSTANCE.shouldShowGlint(slot));
    }

    @ModifyArg(
            method = RENDER_LAYERS_METHOD,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;"
                            + "submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
                            + "ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
                    ordinal = 0
            ),
            index = 6
    )
    private int armorvisibility$applyOpacity(int color) {
        if (ArmorRenderContext.getCurrentSlot() == null) {
            return color;
        }

        int alpha = Math.round((color >>> 24) * ArmorRenderContext.getCurrentOpacity());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
