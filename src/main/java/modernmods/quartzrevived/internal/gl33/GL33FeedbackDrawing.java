package modernmods.quartzrevived.internal.gl33;


import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import modernmods.quartzrevived.DrawBatch;
import modernmods.quartzrevived.internal.Buffer;
import modernmods.quartzrevived.internal.IrisDetection;
import modernmods.quartzrevived.internal.QuartzCore;
import modernmods.quartzrevived.internal.common.B3DStateHelper;
import modernmods.quartzrevived.internal.gl33.batching.GL33DrawBatch;
import modernmods.quartzrevived.internal.gl46.GL46FeedbackDrawing;
import modernmods.quartzrevived.internal.util.VertexFormatOutput;
import org.joml.Matrix4f;

import java.lang.ref.WeakReference;
import java.util.List;

import static modernmods.quartzrevived.internal.util.ShitMojangShouldHaveButDoesnt.drawRenderTypeVertexBuffer;
import static org.lwjgl.opengl.GL33C.*;

public class GL33FeedbackDrawing {
    
    private static final ReferenceArrayList<WeakReference<GL33DrawBatch>> drawBatches = new ReferenceArrayList<>();
    
    public static DrawBatch createDrawBatch() {
        final var batcher = new GL33DrawBatch();
        final var ref = new WeakReference<>(batcher);
        drawBatches.add(ref);
        QuartzCore.mainThreadClean(batcher, () -> drawBatches.remove(ref));
        return batcher;
    }
    
    
    private static final Reference2IntMap<RenderType> renderTypeUsages = new Reference2IntArrayMap<>();
    private static final Reference2IntMap<RenderType> renderTypeDrawnVertices = new Reference2IntArrayMap<>();
    private static final ReferenceArrayList<RenderType> inUseRenderTypes = new ReferenceArrayList<>();
    
    private static final Object2ObjectMap<RenderType, FeedbackBuffer> renderTypeFeedbackBuffers = new Object2ObjectArrayMap<>();
    private static final Object2ObjectMap<RenderType, GpuBuffer> renderTypeDrawBuffer = new Object2ObjectArrayMap<>();
    
    private static Buffer.CallbackHandle rebuildCallbackHandle;
    
    private static final class FeedbackBuffer {
        final GpuBuffer gpuBuffer1;
        final GpuBuffer gpuBuffer2;
        final int buffer1;
        final int buffer2;
        final int size;
        final int VAO1;
        final int VAO2;
        
        private FeedbackBuffer(int size) {
            this.size = roundUpPo2(Math.max(size, 1));
            this.gpuBuffer1 = RenderSystem.getDevice().createBuffer(() -> "Quartz feedback buffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, this.size);
            this.gpuBuffer2 = RenderSystem.getDevice().createBuffer(() -> "Quartz feedback buffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, this.size);
            this.buffer1 = ((GlBuffer) this.gpuBuffer1).handle;
            this.buffer2 = ((GlBuffer) this.gpuBuffer2).handle;
            this.VAO1 = glGenVertexArrays();
            this.VAO2 = glGenVertexArrays();
        }
        
        GpuBuffer gpuBufferFor(int handle) {
            return handle == buffer1 ? gpuBuffer1 : gpuBuffer2;
        }
        
        void delete() {
            gpuBuffer1.close();
            gpuBuffer2.close();
            glDeleteVertexArrays(VAO1);
            glDeleteVertexArrays(VAO2);
        }
        
        void setupVAO(VertexFormatOutput formatOutput) {
            B3DStateHelper.bindVertexArray(VAO1);
            B3DStateHelper.bindArrayBuffer(buffer1);
            GL33FeedbackPrograms.setupVAOForPostProgramOutputFormat(formatOutput);
            B3DStateHelper.bindVertexArray(VAO2);
            B3DStateHelper.bindArrayBuffer(buffer2);
            GL33FeedbackPrograms.setupVAOForPostProgramOutputFormat(formatOutput);
            B3DStateHelper.bindArrayBuffer(0);
            B3DStateHelper.bindVertexArray(0);
        }
        
        private static int roundUpPo2(int minSize) {
            int size = Integer.highestOneBit(minSize);
            if (size < minSize) {
                size <<= 1;
            }
            return size;
        }
    }
    
    public static void setupVAO(int instanceDataBuffer, int instanceDataOffset) {
        
        // vertex data
        B3DStateHelper.bindArrayBuffer(QuartzCore.INSTANCE.meshManager.vertexBuffer.as(GL33Buffer.class).handle());
        
        glEnableVertexAttribArray(GL33Statics.POSITION_LOCATION);
        glEnableVertexAttribArray(GL33Statics.COLOR_LOCATION);
        glEnableVertexAttribArray(GL33Statics.TEX_COORD_LOCATION);
        glEnableVertexAttribArray(GL33Statics.NORMAL_LOCATION);
        
        glVertexAttribPointer(GL33Statics.POSITION_LOCATION, 3, GL_FLOAT, false, 32, 0);
        glVertexAttribIPointer(GL33Statics.COLOR_LOCATION, 1, GL_INT, 32, 12);
        glVertexAttribPointer(GL33Statics.TEX_COORD_LOCATION, 2, GL_FLOAT, false, 32, 16);
        glVertexAttribPointer(GL33Statics.NORMAL_LOCATION, 3, GL_SHORT, true, 32, 24);
        
        // instance data
        B3DStateHelper.bindArrayBuffer(instanceDataBuffer);
        
        glEnableVertexAttribArray(GL33Statics.WORLD_POSITION_LOCATION);
        glEnableVertexAttribArray(GL33Statics.DYNAMIC_MATRIX_ID_LOCATION);
        glEnableVertexAttribArray(GL33Statics.STATIC_MATRIX_LOCATION);
        glEnableVertexAttribArray(GL33Statics.STATIC_MATRIX_LOCATION + 1);
        glEnableVertexAttribArray(GL33Statics.STATIC_MATRIX_LOCATION + 2);
        glEnableVertexAttribArray(GL33Statics.STATIC_MATRIX_LOCATION + 3);
        glEnableVertexAttribArray(GL33Statics.STATIC_NORMAL_MATRIX_LOCATION);
        glEnableVertexAttribArray(GL33Statics.STATIC_NORMAL_MATRIX_LOCATION + 1);
        glEnableVertexAttribArray(GL33Statics.STATIC_NORMAL_MATRIX_LOCATION + 2);
        
        glVertexAttribDivisor(GL33Statics.WORLD_POSITION_LOCATION, 1);
        glVertexAttribDivisor(GL33Statics.DYNAMIC_MATRIX_ID_LOCATION, 1);
        glVertexAttribDivisor(GL33Statics.STATIC_MATRIX_LOCATION, 1);
        glVertexAttribDivisor(GL33Statics.STATIC_MATRIX_LOCATION + 1, 1);
        glVertexAttribDivisor(GL33Statics.STATIC_MATRIX_LOCATION + 2, 1);
        glVertexAttribDivisor(GL33Statics.STATIC_MATRIX_LOCATION + 3, 1);
        glVertexAttribDivisor(GL33Statics.STATIC_NORMAL_MATRIX_LOCATION, 1);
        glVertexAttribDivisor(GL33Statics.STATIC_NORMAL_MATRIX_LOCATION + 1, 1);
        glVertexAttribDivisor(GL33Statics.STATIC_NORMAL_MATRIX_LOCATION + 2, 1);
        
        glVertexAttribIPointer(GL33Statics.WORLD_POSITION_LOCATION, 3, GL_INT, GL33Statics.INSTANCE_DATA_BYTE_SIZE, GL33Statics.WORLD_POSITION_OFFSET + instanceDataOffset);
        glVertexAttribIPointer(GL33Statics.DYNAMIC_MATRIX_ID_LOCATION, 1, GL_INT, GL33Statics.INSTANCE_DATA_BYTE_SIZE, GL33Statics.DYNAMIC_MATRIX_ID_OFFSET + instanceDataOffset);
        glVertexAttribPointer(GL33Statics.STATIC_MATRIX_LOCATION, 4, GL_FLOAT, false, GL33Statics.INSTANCE_DATA_BYTE_SIZE, GL33Statics.STATIC_MATRIX_OFFSET + instanceDataOffset);
        glVertexAttribPointer(GL33Statics.STATIC_MATRIX_LOCATION + 1, 4, GL_FLOAT, false, GL33Statics.INSTANCE_DATA_BYTE_SIZE, GL33Statics.STATIC_MATRIX_OFFSET + 16 + instanceDataOffset);
        glVertexAttribPointer(GL33Statics.STATIC_MATRIX_LOCATION + 2, 4, GL_FLOAT, false, GL33Statics.INSTANCE_DATA_BYTE_SIZE, GL33Statics.STATIC_MATRIX_OFFSET + 32 + instanceDataOffset);
        glVertexAttribPointer(GL33Statics.STATIC_MATRIX_LOCATION + 3, 4, GL_FLOAT, false, GL33Statics.INSTANCE_DATA_BYTE_SIZE, GL33Statics.STATIC_MATRIX_OFFSET + 48 + instanceDataOffset);
        glVertexAttribPointer(GL33Statics.STATIC_NORMAL_MATRIX_LOCATION, 4, GL_FLOAT, false, GL33Statics.INSTANCE_DATA_BYTE_SIZE, GL33Statics.STATIC_NORMAL_MATRIX_OFFSET + instanceDataOffset);
        glVertexAttribPointer(GL33Statics.STATIC_NORMAL_MATRIX_LOCATION + 1, 4, GL_FLOAT, false, GL33Statics.INSTANCE_DATA_BYTE_SIZE, GL33Statics.STATIC_NORMAL_MATRIX_OFFSET + 16 + instanceDataOffset);
        glVertexAttribPointer(GL33Statics.STATIC_NORMAL_MATRIX_LOCATION + 2, 4, GL_FLOAT, false, GL33Statics.INSTANCE_DATA_BYTE_SIZE, GL33Statics.STATIC_NORMAL_MATRIX_OFFSET + 32 + instanceDataOffset);
    }
    
    public static void startup() {
        QuartzCore.INSTANCE.meshManager.vertexBuffer.addReallocCallback(false, buffer -> {
            GL33FeedbackDrawing.rebuildAllVAOs();
        });
    }
    
    public static void shutdown() {
        renderTypeFeedbackBuffers.values().forEach(FeedbackBuffer::delete);
    }
    
    public static void rebuildAllVAOs() {
        drawBatches.forEach(batchRef -> {
            final var batch = batchRef.get();
            if (batch != null) {
                batch.rebuildVAOs();
            }
        });
    }
    
    public static void addRenderTypeUse(RenderType renderType) {
        if (renderTypeUsages.put(renderType, renderTypeUsages.getOrDefault(renderType, 0) + 1) == 0) {
            inUseRenderTypes.add(renderType);
        }
    }
    
    public static void removeRenderTypeUse(RenderType renderType) {
        final var usages = renderTypeUsages.getInt(renderType);
        if (usages == 1) {
            renderTypeUsages.removeInt(renderType);
            inUseRenderTypes.remove(renderType);
            final var feedbackBuffer = renderTypeFeedbackBuffers.remove(renderType);
            if (feedbackBuffer != null) {
                feedbackBuffer.delete();
            }
            return;
        }
        renderTypeUsages.put(renderType, usages - 1);
    }
    
    public static List<RenderType> getActiveRenderTypes() {
        return inUseRenderTypes;
    }
    
    public static boolean hasBatch() {
        return !drawBatches.isEmpty();
    }
    
    
    private static final GL33Buffer UBOBuffer = new GL33Buffer(false);
    private static final Buffer.Allocation UBOAllocation = UBOBuffer.alloc(64);
    
    public static void beginFrame() {
        modernmods.quartzrevived.internal.common.B3DStateHelper.useProgram(GL33ComputePrograms.dynamicMatrixProgram());
        for (final var batchRef : drawBatches) {
            final var batch = batchRef.get();
            if (batch == null) {
                return;
            }
            batch.updateAndCull(GL33Core.INSTANCE.drawInfo);
        }
        glBindTexture(GL_TEXTURE_BUFFER, 0);
        glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, 0);
        modernmods.quartzrevived.internal.common.B3DStateHelper.useProgram(0);
    }
    
    public static void collectAllFeedback(boolean shadowsEnabled) {
        final var UBOPointer = UBOAllocation.address();
        
        UBOPointer.putVector3i(0, GL33Core.INSTANCE.drawInfo.playerPosition);
        UBOPointer.putVector3f(16, GL33Core.INSTANCE.drawInfo.playerSubBlock);
        UBOPointer.putVector3i(32, GL33LightEngine.lookupOffset());
        UBOPointer.putInt(44, IrisDetection.areShadersActive() ? 1 : 0);
        UBOAllocation.dirty();
        UBOBuffer.flush();
        
        glBindBufferBase(GL_UNIFORM_BUFFER, 0, UBOBuffer.handle());
        
        GL33LightEngine.bindIndex();
        
        renderTypeDrawBuffer.clear();
        for (final var renderType : inUseRenderTypes) {
            final var outputFormat = VertexFormatOutput.of(renderType.format());
            
            int requiredVertices = 0;
            for (final var batchRef : drawBatches) {
                final var drawBatch = batchRef.get();
                if (drawBatch == null) {
                    return;
                }
                requiredVertices += drawBatch.verticesForRenderType(renderType, shadowsEnabled);
            }
            
            renderTypeDrawnVertices.put(renderType, requiredVertices);
            
            int requiredFeedbackBufferSize = requiredVertices * outputFormat.vertexSize();
            
            var buffer = renderTypeFeedbackBuffers.get(renderType);
            if (buffer == null || buffer.size < requiredFeedbackBufferSize) {
                if (buffer != null) {
                    buffer.delete();
                }
                buffer = new FeedbackBuffer(requiredFeedbackBufferSize);
                buffer.setupVAO(outputFormat);
                renderTypeFeedbackBuffers.put(renderType, buffer);
            }
            
            modernmods.quartzrevived.internal.common.B3DStateHelper.useProgram(GL33FeedbackPrograms.getProgramForOutputFormat(outputFormat));
            
            glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, buffer.buffer1);
            glBeginTransformFeedback(GL_POINTS);
            for (final var batchRef : drawBatches) {
                final var drawBatch = batchRef.get();
                if (drawBatch == null) {
                    return;
                }
                drawBatch.drawFeedback(renderType, shadowsEnabled);
            }
            glEndTransformFeedback();
            
            final var program = GL33FeedbackPrograms.getPostProgramForOutputFormat(outputFormat);
            modernmods.quartzrevived.internal.common.B3DStateHelper.useProgram(program.firstInt());
            int bufferToDraw = GL33LightEngine.drawForEachLayer(requiredVertices, program.secondInt(), buffer.buffer1, buffer.buffer2, buffer.VAO1, buffer.VAO2);
            renderTypeDrawBuffer.put(renderType, buffer.gpuBufferFor(bufferToDraw));
        }
        
        for (int j = 0; j < 8; j++) {
            GlStateManager._activeTexture(GL_TEXTURE0 + j);
            GlStateManager._bindTexture(0);
        }
        B3DStateHelper.bindVertexArray(0);
    }
    
    private static Matrix4f modelView;
    
    public static void setMatrices(Matrix4f modelView) {
        GL33FeedbackDrawing.modelView = modelView;
    }
    
    public static void drawRenderType(RenderType renderType) {
        final var feedbackBuffer = renderTypeDrawBuffer.get(renderType);
        if (feedbackBuffer == null) {
            return;
        }
        final var drawnVertices = renderTypeDrawnVertices.getInt(renderType);
        if (drawnVertices == 0) {
            return;
        }
        
        drawRenderTypeVertexBuffer(modelView, renderType, feedbackBuffer, drawnVertices);
    }
}
