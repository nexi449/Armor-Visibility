package com.example.armorvisibility.mixin;

import com.example.armorvisibility.OffhandRenderBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class OffhandItemRendererMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private MultiBufferSource armorvisibility$applyOffhandOpacity(MultiBufferSource buffers) {
        return OffhandRenderBuffers.withOpacity(buffers);
    }
}
