package modernmods.quartzrevived.internal.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import modernmods.quartzrevived.internal.IrisDetection;
import modernmods.quartzrevived.internal.QuartzCore;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    public void frameStart(DeltaTracker deltaTracker, boolean drawBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        QuartzCore.INSTANCE.frameStart(frustumMatrix, deltaTracker.getGameTimeDeltaPartialTick(false), 0, drawBlockOutline, camera, gameRenderer, lightTexture, projectionMatrix);
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;getPosition()Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            )
    )
    public void lightUpdated(DeltaTracker deltaTracker, boolean drawBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        QuartzCore.INSTANCE.lightUpdated();
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V",
                    ordinal = 0
            )
    )
    public void preTerrainSetup(DeltaTracker deltaTracker, boolean drawBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        QuartzCore.INSTANCE.preTerrainSetup();
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSectionLayer(Lnet/minecraft/client/renderer/RenderType;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
                    ordinal = 0
            )
    )
    public void preOpaque(DeltaTracker deltaTracker, boolean drawBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        QuartzCore.INSTANCE.preOpaque();
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V",
                    ordinal = 0
            )
    )
    public void endOpaque(DeltaTracker deltaTracker, boolean drawBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        QuartzCore.INSTANCE.endOpaque();
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"
            )
    )
    public void endTranslucent(DeltaTracker deltaTracker, boolean drawBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        QuartzCore.INSTANCE.endTranslucent();
    }

    @Inject(method = "setSectionDirty(IIIZ)V", at = @At("HEAD"))
    public void setSectionDirty(int x, int y, int z, boolean updateNow, CallbackInfo ci) {
        QuartzCore.INSTANCE.sectionDirty(x, y, z);
    }

    @Inject(method = "renderSectionLayer", at = @At(value = "HEAD"))
    void invokeRenderSectionLayer(RenderType renderType, double cameraX, double cameraY, double cameraZ, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (renderType == RenderType.solid() && IrisDetection.isRenderingShadows()) {
            QuartzCore.INSTANCE.shadowPass(frustumMatrix, projectionMatrix);
        }
    }
}
