package com.example.armorvisibility.mixin;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.RenderType$CompositeRenderType")
public interface RenderTypeCompositeAccessor {
    @Accessor(value = "state", remap = false)
    RenderType.CompositeState armorvisibility$getState();
}
