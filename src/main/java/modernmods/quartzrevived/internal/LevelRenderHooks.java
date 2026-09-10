package modernmods.quartzrevived.internal;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import modernmods.phosphophylliterevived.registry.ClientOnly;
import modernmods.phosphophylliterevived.registry.OnModLoad;
import modernmods.phosphophylliterevived.util.NonnullDefault;
import org.joml.Matrix4f;

@ClientOnly
@NonnullDefault
public final class LevelRenderHooks {

    @OnModLoad
    private static void onModLoad() {
        NeoForge.EVENT_BUS.addListener(LevelRenderHooks::afterOpaqueBlocks);
        NeoForge.EVENT_BUS.addListener(LevelRenderHooks::afterTranslucentBlocks);
        NeoForge.EVENT_BUS.addListener(LevelRenderHooks::afterLevel);
    }

    private static void afterOpaqueBlocks(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        if (IrisDetection.isRenderingShadows()) {
            QuartzCore.INSTANCE.shadowPass(new Matrix4f(event.getModelViewMatrix()));
            return;
        }
        QuartzCore.INSTANCE.preOpaque();
    }

    private static void afterTranslucentBlocks(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        if (IrisDetection.isRenderingShadows()) {
            return;
        }
        QuartzCore.INSTANCE.endOpaque();
    }

    private static void afterLevel(RenderLevelStageEvent.AfterLevel event) {
        if (IrisDetection.isRenderingShadows()) {
            return;
        }
        QuartzCore.INSTANCE.endTranslucent();
    }
}
