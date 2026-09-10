package modernmods.quartzrevived.internal.common;

import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import modernmods.phosphophylliterevived.util.NonnullDefault;

import static org.lwjgl.opengl.GL33C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL33C.GL_ELEMENT_ARRAY_BUFFER;

/**
 * Blaze3d caches some state, sometimes i override this state, so, need to make sure it will set it back after i modify it
 */
@NonnullDefault
public class B3DStateHelper {
    
    public static void bindArrayBuffer(int buffer) {
        GlStateManager._glBindBuffer(GL_ARRAY_BUFFER, buffer);
    }
    
    public static void bindElementBuffer(int buffer) {
        GlStateManager._glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffer);
    }
    
    public static void bindVertexArray(int vertexArray) {
        GlStateManager._glBindVertexArray(vertexArray);
    }
    
    public static void useProgram(int program) {
        GlStateManager._glUseProgram(program);
        final Object commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        if (commandEncoder instanceof GlCommandEncoder encoder) {
            encoder.lastProgram = null;
            encoder.lastPipeline = null;
        }
    }
}
