package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorConfig;
import com.example.armorvisibility.ArmorRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EquipmentRenderer.class)
public abstract class EquipmentRendererMixin {
    @Redirect(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/RenderLayers;armorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"
            )
    )
    private RenderLayer armorvisibility$useTranslucentLayer(Identifier texture) {
        EquipmentSlot slot = ArmorRenderContext.getCurrentSlot();
        if (slot != null && ArmorRenderContext.getCurrentOpacity() < 1.0F) {
            return RenderLayers.armorTranslucent(texture);
        }
        return RenderLayers.armorCutoutNoCull(texture);
    }

    @Redirect(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;hasGlint()Z"
            )
    )
    private boolean armorvisibility$respectGlintSetting(ItemStack stack) {
        EquipmentSlot slot = ArmorRenderContext.getCurrentSlot();
        return stack.hasGlint() && (slot == null
                || !ArmorRenderContext.shouldApplySettings()
                || ArmorConfig.INSTANCE.shouldShowGlint(slot));
    }

    @ModifyArg(
            method = "render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/command/RenderCommandQueue;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/texture/Sprite;ILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V",
                    ordinal = 0
            ),
            index = 6
    )
    private int armorvisibility$applyOpacity(int color) {
        EquipmentSlot slot = ArmorRenderContext.getCurrentSlot();
        if (slot == null) {
            return color;
        }

        float opacity = ArmorRenderContext.getCurrentOpacity();
        int alpha = Math.round((color >>> 24) * opacity);
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}
