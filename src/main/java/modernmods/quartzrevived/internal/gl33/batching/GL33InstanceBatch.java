package modernmods.quartzrevived.internal.gl33.batching;

import modernmods.quartzrevived.AABB;
import org.joml.Matrix4fc;
import org.joml.Vector3ic;
import modernmods.phosphophylliterevived.util.NonnullDefault;
import modernmods.quartzrevived.DrawBatch;
import modernmods.quartzrevived.DynamicMatrix;
import modernmods.quartzrevived.Mesh;
import modernmods.quartzrevived.internal.QuartzCore;

import javax.annotation.Nullable;

@NonnullDefault
public class GL33InstanceBatch implements DrawBatch.InstanceBatch {
    
    private final GL33InstanceManager manager;
    
    GL33InstanceBatch(GL33InstanceManager manager){
        QuartzCore.mainThreadClean(this, manager::delete);
        this.manager = manager;
    }
    
    @Override
    public void updateMesh(Mesh mesh) {
        manager.updateMesh(mesh);
    }
    
    @Nullable
    @Override
    public DrawBatch.Instance createInstance(Vector3ic position, @Nullable DynamicMatrix dynamicMatrix, @Nullable Matrix4fc staticMatrix, @Nullable AABB aabb) {
        return manager.createInstance(position, dynamicMatrix, staticMatrix, aabb);
    }
}
