package modernmods.quartzrevived.internal.util;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.renderer.rendertype.RenderType;
import modernmods.phosphophylliterevived.registry.ClientOnly;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

@ClientOnly
public class ShitMojangShouldHaveButDoesnt {

    public static void drawRenderTypeVertexBuffer(RenderType renderType, GpuBuffer vertexBuffer, int vertexCount) {
        drawRenderTypeVertexBuffer(RenderSystem.getModelViewMatrix(), renderType, vertexBuffer, vertexCount);
    }

    public static void drawRenderTypeVertexBuffer(Matrix4f modelViewMatrix, RenderType renderType, GpuBuffer vertexBuffer, int vertexCount) {
        final var mode = renderType.mode();
        final var indexCount = mode.indexCount(vertexCount);
        if (indexCount == 0) {
            return;
        }

        final var sequentialIndices = RenderSystem.getSequentialBuffer(mode);
        final var indexBuffer = sequentialIndices.getBuffer(indexCount);

        final var setup = renderType.state;

        final var modelViewStack = RenderSystem.getModelViewStack();
        final var layeringModifier = setup.layeringTransform.getModifier();
        if (layeringModifier != null) {
            modelViewStack.pushMatrix();
            layeringModifier.accept(modelViewStack);
        }

        final var dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                modelViewMatrix,
                new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                new Vector3f(),
                setup.textureTransform.getMatrix());

        final var renderTarget = renderType.outputTarget().getRenderTarget();
        final GpuTextureView colorTexture = RenderSystem.outputColorTextureOverride != null
                ? RenderSystem.outputColorTextureOverride
                : renderTarget.getColorTextureView();
        final GpuTextureView depthTexture = renderTarget.useDepth
                ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.getDepthTextureView())
                : null;

        try (final var renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "Quartz draw for " + renderType, colorTexture, OptionalInt.empty(), depthTexture, OptionalDouble.empty())) {
            renderPass.setPipeline(renderType.pipeline());

            final var scissorState = RenderSystem.getScissorStateForRenderTypeDraws();
            if (scissorState.enabled()) {
                renderPass.enableScissor(scissorState.x(), scissorState.y(), scissorState.width(), scissorState.height());
            }

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertexBuffer);

            for (final Map.Entry<String, net.minecraft.client.renderer.rendertype.RenderSetup.TextureAndSampler> entry : setup.getTextures().entrySet()) {
                renderPass.bindTexture(entry.getKey(), entry.getValue().textureView(), entry.getValue().sampler());
            }

            renderPass.setIndexBuffer(indexBuffer, sequentialIndices.type());
            renderPass.drawIndexed(0, 0, indexCount, 1);
        }

        if (layeringModifier != null) {
            modelViewStack.popMatrix();
        }
    }
}
