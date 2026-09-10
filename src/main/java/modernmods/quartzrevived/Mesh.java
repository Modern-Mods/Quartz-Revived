package modernmods.quartzrevived;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import modernmods.phosphophylliterevived.util.NonnullDefault;

@NonnullDefault
public interface Mesh {
    interface Builder {
        MultiBufferSource bufferSource();
        
        PoseStack matrixStack();
    }
    
    void rebuild();
}
