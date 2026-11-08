package com.example.armorvisibility.mixin;

import com.example.armorvisibility.ArmorClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class PauseMenuIconMixin {
    @Inject(method = "renderWithTooltip", at = @At("TAIL"))
    private void armorvisibility$renderPauseMenuIcon(DrawContext context, int mouseX, int mouseY,
                                                     float delta, CallbackInfo ci) {
        ArmorClient.renderPauseMenuIcon((Screen) (Object) this, context);
    }
}
