package modernmods.quartzrevived.internal.gl46;


import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import modernmods.phosphophylliterevived.util.NonnullDefault;
import modernmods.quartzrevived.DrawBatch;
import modernmods.quartzrevived.internal.Buffer;
import modernmods.quartzrevived.internal.IrisDetection;
import modernmods.quartzrevived.internal.QuartzCore;
import modernmods.quartzrevived.internal.common.DrawInfo;
import org.joml.Matrix4f;
import org.lwjgl.opengl.KHRDebug;

import java.util.List;

import static org.lwjgl.opengl.GL46C.glDepthMask;
import static org.lwjgl.opengl.GL46C.glFinish;

@NonnullDefault
public class GL46Core extends QuartzCore {
    
    public static final GL46Core INSTANCE;
    
    static {
        if (GL46Statics.AVAILABLE) {
            try {
                LOGGER.info("Quartz initializing GL46Core");
                KHRDebug.glPushDebugGroup(KHRDebug.GL_DEBUG_SOURCE_THIRD_PARTY, 0, "Quartz GL46 Renderer Setup");
                INSTANCE = new GL46Core();
                LOGGER.info("Quartz GL46Core initialized");
            } finally {
                KHRDebug.glPopDebugGroup();
            }
        } else {
            // null signals not available
            //noinspection DataFlowIssue
            INSTANCE = null;
            LOGGER.info("GL46 not available");
        }
    }
    
    private int frameInFlight;
    private long lastTimeNano = 0;
    public final DrawInfo drawInfo = new DrawInfo();
    @Override
    protected void startupInternal() {
        GL46ComputePrograms.startup();
        GL46FeedbackPrograms.startup();
        GL46LightEngine.startup();
        GL46FeedbackDrawing.startup();
    }
    
    @Override
    protected void shutdownInternal() {
        GL46FeedbackDrawing.shutdown();
        GL46LightEngine.shutdown();
        GL46FeedbackPrograms.shutdown();
        GL46ComputePrograms.shutdown();
    }
    
    @Override
    protected void resourcesReloadedInternal() {
        GL46FeedbackPrograms.reload();
        GL46ComputePrograms.reload();
//        GL46LightEngine.dirtyAll();
    }
    
    @Override
    public DrawBatch createDrawBatch() {
        return GL46FeedbackDrawing.createDrawBatch();
    }
    
    @Override
    public Buffer allocBuffer(boolean GPUOnly) {
        return new GL46Buffer(GPUOnly);
    }
    
    @Override
    public void frameStart(Matrix4f pModelViewMatrix, float pPartialTicks, Vec3 pCameraPosition, Matrix4f pProjection) {
        deletionQueue.runAll();
        
        frameInFlight++;
        frameInFlight %= GL46Statics.FRAMES_IN_FLIGHT;
        
        GL46FeedbackDrawing.aboutToBeginFrame();
        
        long timeNanos = System.nanoTime();
        long deltaNano = timeNanos - lastTimeNano;
        lastTimeNano = timeNanos;
        if (lastTimeNano == 0) {
            deltaNano = 0;
        }
        
        var playerPosition = pCameraPosition;
        drawInfo.playerPosition.set((int) playerPosition.x, (int) playerPosition.y, (int) playerPosition.z);
        drawInfo.playerPositionNegative.set(drawInfo.playerPosition).negate();
        drawInfo.playerSubBlock.set(playerPosition.x - (int) playerPosition.x, playerPosition.y - (int) playerPosition.y, playerPosition.z - (int) playerPosition.z);
        drawInfo.playerSubBlockNegative.set(drawInfo.playerSubBlockNegative).negate();
        
        drawInfo.projectionMatrix.set(pProjection);
        drawInfo.projectionMatrix.mul(pModelViewMatrix);
        drawInfo.projectionMatrix.get(drawInfo.projectionMatrixFloatBuffer);
        
        drawInfo.modelViewMatrix.set(pModelViewMatrix);
        
        drawInfo.deltaNano = deltaNano;
        drawInfo.partialTicks = pPartialTicks;
        
        GL46FeedbackDrawing.beginFrame();
    }
    
    @Override
    public void lightUpdated() {
        GL46LightEngine.update(Minecraft.getInstance().level);
        
        if(!GL46FeedbackDrawing.hasBatch()){
            return;
        }
        
        GL46FeedbackDrawing.collectAllFeedback(IrisDetection.areShadersActive());
    }
    
    @Override
    public void preTerrainSetup() {
    }
    
    @Override
    public void shadowPass(Matrix4f modelViewMatrix) {
        if(!GL46FeedbackDrawing.hasBatch()){
            return;
        }
        GL46FeedbackDrawing.setMatrices(modelViewMatrix);
        GL46FeedbackDrawing.getActiveRenderTypes().forEach(GL46FeedbackDrawing::drawRenderType);
    }
    
    @Override
    public void preOpaque() {
        if(!GL46FeedbackDrawing.hasBatch()){
            return;
        }
        
        
        GL46FeedbackDrawing.setMatrices(drawInfo.modelViewMatrix);
        
        for (final var renderType : GL46FeedbackDrawing.getActiveRenderTypes()) {
            if (renderType.hasBlending()) {
                continue;
            }
            GL46FeedbackDrawing.drawRenderType(renderType);
        }
    }
    
    @Override
    public void endOpaque() {
        if(!GL46FeedbackDrawing.hasBatch()){
            return;
        }
        
        
        for (final var renderType : GL46FeedbackDrawing.getActiveRenderTypes()) {
            if (!renderType.hasBlending()) {
                continue;
            }
            GlStateManager._depthMask(false);
            GL46FeedbackDrawing.drawRenderType(renderType);
        }
        GlStateManager._depthMask(true);
    }
    
    @Override
    public void endTranslucent() {
    
    }
    
    @Override
    public void waitIdle() {
        glFinish();
    }
    
    @Override
    public int frameInFlight() {
        return frameInFlight;
    }
    
    @Override
    public void sectionDirty(int x, int y, int z) {
        GL46LightEngine.sectionDirty(x, y, z);
    }
    
    @Override
    public void addDebugText(List<String> list) {
        list.add("Quartz backend: OpenGL 4.6");
        list.add("Spare texture: " + (GL46Statics.SPARSE_TEXTURE_ENABLED ? "enabled" : "disabled"));
    }
}
