package com.example.armorvisibility.mixin;

import com.example.armorvisibility.OffhandRenderBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public abstract class OffhandRenderStateLayerMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0, remap = false)
    private MultiBufferSource armorvisibility$applyOffhandOpacity(MultiBufferSource buffers) {
        return OffhandRenderBuffers.withOpacity(buffers);
    }
}
