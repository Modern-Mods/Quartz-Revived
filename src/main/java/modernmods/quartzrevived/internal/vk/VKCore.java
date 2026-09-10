package modernmods.quartzrevived.internal.vk;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import modernmods.quartzrevived.DrawBatch;
import modernmods.quartzrevived.internal.Buffer;
import modernmods.quartzrevived.internal.QuartzCore;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public class VKCore extends QuartzCore {
    
    public static final VKCore INSTANCE;
    
    static {
        if (VKStatics.AVAILABLE) {
            LOGGER.info("Quartz initializing VKCore");
            INSTANCE = new VKCore();
            LOGGER.info("Quartz VKCore initialized");
        }else {
            INSTANCE = null;
            LOGGER.info("Vulkan not available");
        }
    }
    
    @Override
    protected void startupInternal() {
    
    }
    
    @Override
    protected void shutdownInternal() {
    
    }
    
    @Override
    protected void resourcesReloadedInternal() {
    
    }
    
    @Override
    public DrawBatch createDrawBatch() {
        return null;
    }
    
    @Override
    public Buffer allocBuffer(boolean GPUOnly) {
        return null;
    }
    
    @Override
    public void frameStart(Matrix4f pModelViewMatrix, float pPartialTicks, Vec3 pCameraPosition, Matrix4f pProjection) {
    
    }
    
    @Override
    public void lightUpdated() {
    
    }
    
    @Override
    public void preTerrainSetup() {
    
    }
    
    @Override
    public void shadowPass(Matrix4f modelViewMatrix) {
    
    }
    
    @Override
    public void preOpaque() {
    
    }
    
    @Override
    public void endOpaque() {
    
    }
    
    @Override
    public void endTranslucent() {
    
    }
    
    @Override
    public void waitIdle() {
    
    }
    
    @Override
    public int frameInFlight() {
        return 0;
    }
    
    @Override
    public void sectionDirty(int x, int y, int z) {
    
    }
    
    @Override
    public void addDebugText(List<String> list) {
        list.add("Quartz backend: Vulkan");
    }
}
