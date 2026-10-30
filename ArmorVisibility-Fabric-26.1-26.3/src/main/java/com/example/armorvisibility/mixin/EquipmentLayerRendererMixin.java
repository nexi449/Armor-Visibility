package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorVisibilityConfig;
import com.example.armorvisibility.ArmorVisibilityRenderContext;
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
    private static final String RENDER_LAYERS =
            "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;"
                    + "Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;"
                    + "Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V";

    @Redirect(
            method = RENDER_LAYERS,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;armorCutoutNoCull(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"
            )
    )
    private RenderType armorvisibility$useTranslucentArmorLayer(Identifier texture) {
        ArmorVisibilityRenderContext.ArmorState state = ArmorVisibilityRenderContext.getArmorState();
        if (state != null && state.applySettings() && state.opacity() < 1.0F) {
            return RenderTypes.entityTranslucentCull(texture);
        }
        return RenderTypes.armorCutoutNoCull(texture);
    }

    @Redirect(
            method = RENDER_LAYERS,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;armorCutoutNoCullGlint(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"
            )
    )
    private RenderType armorvisibility$useTranslucentGlintLayer(Identifier texture) {
        ArmorVisibilityRenderContext.ArmorState state = ArmorVisibilityRenderContext.getArmorState();
        if (state != null && state.applySettings() && state.opacity() < 1.0F) {
            return RenderTypes.entityTranslucentCull(texture);
        }
        return RenderTypes.armorCutoutNoCullGlint(texture);
    }

    @Redirect(
            method = RENDER_LAYERS,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;hasFoil()Z"
            )
    )
    private boolean armorvisibility$respectGlintSetting(ItemStack stack) {
        ArmorVisibilityRenderContext.ArmorState state = ArmorVisibilityRenderContext.getArmorState();
        return stack.hasFoil() && (state == null || !state.applySettings()
                || state.slot() == null || ArmorVisibilityConfig.INSTANCE.shouldShowGlint(state.slot()));
    }

    @ModifyArg(
            method = RENDER_LAYERS,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"
            ),
            index = 6
    )
    private int armorvisibility$applyArmorOpacity(int color) {
        ArmorVisibilityRenderContext.ArmorState state = ArmorVisibilityRenderContext.getArmorState();
        if (state == null || !state.applySettings()) {
            return color;
        }
        int alpha = Math.round((color >>> 24) * state.opacity());
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
