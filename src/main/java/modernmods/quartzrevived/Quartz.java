package modernmods.quartzrevived;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import modernmods.phosphophylliterevived.Phosphophyllite;
import modernmods.phosphophylliterevived.registry.Registry;
import org.joml.Vector3ic;
import modernmods.phosphophylliterevived.util.NonnullDefault;
import modernmods.quartzrevived.internal.EventListener;
import modernmods.quartzrevived.internal.QuartzCore;

import java.util.List;
import java.util.function.Consumer;

@Mod(Quartz.modid)
@NonnullDefault
public final class Quartz {
    public static final String modid = "quartz";
    
    public Quartz(){
        new Registry(modid, CreativeTabOrder.before(), CreativeTabOrder.after());
    }
    
    public static IEventBus EVENT_BUS = BusBuilder.builder().build();
    
    private static ModelBlockRenderer blockRenderer() {
        return new ModelBlockRenderer(false, false, Minecraft.getInstance().getBlockColors());
    }
    
    private static void tesselate(Mesh.Builder builder, BlockState blockState, BlockStateModel model) {
        final var poseStack = builder.matrixStack();
        final var bufferSource = builder.bufferSource();
        blockRenderer().tesselateBlock((x, y, z, quad, instance) -> {
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            final var renderType = switch (quad.materialInfo().layer()) {
                case ChunkSectionLayer.SOLID -> RenderTypes.solidMovingBlock();
                case ChunkSectionLayer.CUTOUT -> RenderTypes.cutoutMovingBlock();
                case ChunkSectionLayer.TRANSLUCENT -> RenderTypes.translucentMovingBlock();
            };
            bufferSource.getBuffer(renderType).putBakedQuad(poseStack.last(), quad, instance);
            poseStack.popPose();
        }, 0.0F, 0.0F, 0.0F, BlockAndTintGetter.EMPTY, BlockPos.ZERO, blockState, model, 42L);
    }
    
    public static Mesh createStaticMesh(BlockState blockState) {
        return createStaticMesh(builder -> {
            final var model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
            tesselate(builder, blockState, model);
        });
    }
    
    public static void registerModel(Identifier modelLocation) {
        EventListener.registerModel(modelLocation);
    }
    
    public static Mesh createStaticMesh(Identifier modelLocation) {
        final var modelKey = EventListener.registerModel(modelLocation);
        return createStaticMesh(builder -> {
            final var part = Minecraft.getInstance().getModelManager().getStandaloneModel(modelKey);
            if (part == null) {
                return;
            }
            tesselate(builder, Blocks.STONE.defaultBlockState(), new BlockStateModel() {
                @Override
                public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
                    output.add(part);
                }

                @Override
                public Material.Baked particleMaterial() {
                    return part.particleMaterial();
                }

                @Override
                public int materialFlags() {
                    return part.materialFlags();
                }
            });
        });
    }
    
    public static Mesh createStaticMesh(Consumer<Mesh.Builder> buildFunc) {
        return QuartzCore.INSTANCE.meshManager.createMesh(buildFunc);
    }
    
    public static DrawBatch getDrawBatchForBlock(BlockPos blockPos) {
        return getDrawBatcherForSection(SectionPos.asLong(blockPos));
    }
    
    public static DrawBatch getDrawBatcherForBlock(Vector3ic blockPos) {
        return getDrawBatcherForSection(SectionPos.asLong(blockPos.x() >> 4, blockPos.y() >> 4, blockPos.z() >> 4));
    }
    
    public static DrawBatch getDrawBatcherForSection(long sectionPos) {
        return QuartzCore.INSTANCE.getWorldEngine().getBatcherForSection(sectionPos);
    }
    
    public static DrawBatch getDrawBatcherForAABB(AABB aabb) {
        return QuartzCore.INSTANCE.getWorldEngine().getBatcherForAABB(aabb);
    }
    
    public static DrawBatch getEntityBatcher() {
        return QuartzCore.INSTANCE.getEntityBatcher();
    }
}
