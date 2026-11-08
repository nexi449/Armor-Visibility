package com.example.armorvisibility;

import com.example.armorvisibility.mixin.RenderStateTextureAccessor;
import com.example.armorvisibility.mixin.RenderTypeCompositeAccessor;
import com.example.armorvisibility.mixin.RenderTypeCompositeStateAccessor;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public final class OffhandRenderBuffers {
    private OffhandRenderBuffers() {
    }

    public static MultiBufferSource withOpacity(MultiBufferSource buffers) {
        float opacity = ArmorRenderContext.getOffhandOpacity();
        if (!ArmorRenderContext.hasOffhandOpacity()
                || opacity >= 1.0F
                || buffers instanceof OffhandOpacityBufferSource) {
            return buffers;
        }

        return new OffhandOpacityBufferSource(buffers, opacity);
    }

    private static RenderType translucentType(RenderType renderType) {
        if (!(renderType instanceof RenderTypeCompositeAccessor composite)) {
            return RenderType.itemEntityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
        }

        RenderStateShard.EmptyTextureStateShard textureState =
                ((RenderTypeCompositeStateAccessor) (Object) composite.armorvisibility$getState())
                        .armorvisibility$getTextureState();
        Optional<ResourceLocation> texture =
                ((RenderStateTextureAccessor) (Object) textureState).armorvisibility$getTexture();
        return texture.map(RenderType::itemEntityTranslucentCull)
                .orElseGet(() -> RenderType.itemEntityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
    }

    private static final class OffhandOpacityBufferSource implements MultiBufferSource {
        private final MultiBufferSource delegate;
        private final float opacity;

        private OffhandOpacityBufferSource(MultiBufferSource delegate, float opacity) {
            this.delegate = delegate;
            this.opacity = opacity;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new OpacityVertexConsumer(delegate.getBuffer(translucentType(renderType)), opacity);
        }
    }

    private static final class OpacityVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float opacity;

        private OpacityVertexConsumer(VertexConsumer delegate, float opacity) {
            this.delegate = delegate;
            this.opacity = opacity;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, Math.round(alpha * opacity));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }
    }
}
