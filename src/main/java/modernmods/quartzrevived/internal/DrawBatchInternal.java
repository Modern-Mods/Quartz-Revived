package modernmods.quartzrevived.internal;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import modernmods.phosphophylliterevived.util.NonnullDefault;
import modernmods.quartzrevived.DrawBatch;
import modernmods.quartzrevived.internal.common.DrawInfo;

@NonnullDefault
public interface DrawBatchInternal extends DrawBatch {
    
    void updateAndCull(@SuppressWarnings("SameParameterValue") DrawInfo drawInfo);
    
    void drawFeedback(RenderType renderType, boolean shadowsEnabled);
}
