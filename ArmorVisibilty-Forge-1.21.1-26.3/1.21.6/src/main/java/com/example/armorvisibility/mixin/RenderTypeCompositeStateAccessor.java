package com.example.armorvisibility.mixin;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderType.CompositeState.class)
public interface RenderTypeCompositeStateAccessor {
    @Accessor(value = "textureState", remap = false)
    RenderStateShard.EmptyTextureStateShard armorvisibility$getTextureState();
}
