package modernmods.quartzrevived.internal;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.common.NeoForge;
import modernmods.phosphophylliterevived.registry.ClientOnly;
import modernmods.phosphophylliterevived.registry.OnModLoad;
import modernmods.phosphophylliterevived.threading.WorkQueue;
import modernmods.phosphophylliterevived.util.NonnullDefault;
import modernmods.quartzrevived.DrawBatch;
import modernmods.quartzrevived.Quartz;
import modernmods.quartzrevived.QuartzConfig;
import modernmods.quartzrevived.QuartzEvent;
import modernmods.quartzrevived.internal.common.InternalMesh;
import modernmods.quartzrevived.internal.gl33.GL33Core;
import modernmods.quartzrevived.internal.gl46.GL46Core;
import modernmods.quartzrevived.internal.vk.VKCore;
import modernmods.quartzrevived.internal.world.WorldEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.Cleaner;
import java.util.List;

@ClientOnly
@NonnullDefault
public abstract class QuartzCore {
    
    public static final Logger LOGGER = LogManager.getLogger("Quartz");
    public static final boolean DEBUG;
    
    @Nonnull
    public static final QuartzCore INSTANCE;
    public static final Cleaner CLEANER = Cleaner.create();
    public static final WorkQueue deletionQueue = new WorkQueue();
    
    public static void mainThreadClean(Object referent, Runnable cleanFunc) {
        CLEANER.register(referent, () -> deletionQueue.enqueueUntracked(cleanFunc));
    }
    
    static {
        boolean fromEventListener = false;
        for (final var frame : Thread.currentThread().getStackTrace()) {
            if (frame.getClassName().equals(EventListener.class.getName())) {
                fromEventListener = true;
                break;
            }
        }
        if (!fromEventListener) {
            throw new IllegalStateException("Attempt to init quartz before it is ready");
        }
        LOGGER.info("Quartz Init");
        if (QuartzConfig.INSTANCE.debug) {
            LOGGER.warn("Debug mode enabled, performance may suffer");
        }
        DEBUG = QuartzConfig.INSTANCE.debug;
        QuartzCore instance = null;
        try {
            instance = createCore(QuartzConfig.INSTANCE.mode);
            if (instance == null && QuartzConfig.INSTANCE.mode != QuartzConfig.Mode.Automatic) {
                LOGGER.error("Failed to create QuartzCore of requested type, attempting automatic creation");
                instance = createCore(QuartzConfig.Mode.Automatic);
            }
            if (instance == null) {
                throw new IllegalStateException("QuartzCore failed to load, this shouldn't be possible");
            }
        } catch (NoClassDefFoundError e) {
            if (!e.getMessage().contains("phosphophyllite")) {
                throw e;
            }
            // Phosphophyllite isn't present, print but ignore
            e.printStackTrace();
        }
        // in the event this is null, phos isn't present
        //noinspection ConstantConditions
        INSTANCE = instance;
    }
    
    @Nullable
    private static QuartzCore createCore(QuartzConfig.Mode mode) {
        return switch (mode) {
            case Vulkan10 -> VKCore.INSTANCE;
            case OpenGL46 -> GL46Core.INSTANCE;
            case OpenGL33 -> GL33Core.INSTANCE;
            case Automatic -> {
                for (QuartzConfig.Mode value : QuartzConfig.Mode.values()) {
                    if (value == QuartzConfig.Mode.Automatic) {
                        yield null;
                    }
                    final var core = createCore(value);
                    if (core != null) {
                        yield core;
                    }
                }
                yield null;
            }
            default -> null;
        };
    }
    
    static void init() {
    }
    
    private static boolean wasInit = false;
    
    public static void ensureStarted() {
        if (wasInit) {
            return;
        }
        INSTANCE.startupInternal();
        Quartz.EVENT_BUS.post(new QuartzEvent.Startup());
        wasInit = true;
    }
    
    public static void registerDebugEntries(RegisterDebugEntriesEvent event) {
        final var id = net.minecraft.resources.Identifier.fromNamespaceAndPath(Quartz.modid, "stats");
        event.register(id, (DebugScreenEntry) (displayer, serverOrClientLevel, clientChunk, serverChunk) -> {
            if (!wasInit) {
                return;
            }
            final var list = new java.util.ArrayList<String>();
            INSTANCE.addDebugText(list);
            for (final var line : list) {
                displayer.addLine(line);
            }
        });
        event.includeInProfile(id, DebugScreenProfile.DEFAULT, DebugScreenEntryStatus.IN_OVERLAY);
    }
    
    protected abstract void startupInternal();
    
    public static void shutdown() {
        if (!wasInit) {
            return;
        }
        INSTANCE.entityBatch = null;
        Quartz.EVENT_BUS.post(new QuartzEvent.Shutdown());
        INSTANCE.shutdownInternal();
        // clean everything up, hopefully
        do {
            System.gc();
        } while (deletionQueue.runAll());
        System.gc();
    }
    
    protected abstract void shutdownInternal();
    
    public static void resourcesReloaded() {
        if (!wasInit) {
            return;
        }
        INSTANCE.meshManager.buildAllMeshes();
        INSTANCE.resourcesReloadedInternal();
    }
    
    protected abstract void resourcesReloadedInternal();
    
    @Nullable
    public DrawBatch entityBatch = null;
    public final WorldEngine worldEngine = new WorldEngine();
    public final InternalMesh.Manager meshManager = new InternalMesh.Manager(allocBuffer(false));
    
    public WorldEngine getWorldEngine() {
        return worldEngine;
    }
    
    public abstract DrawBatch createDrawBatch();
    
    public DrawBatch getEntityBatcher() {
        if (entityBatch == null){
            entityBatch = createDrawBatch();
        }
        return entityBatch;
    }
    
    public abstract Buffer allocBuffer(boolean GPUOnly);
    
    public abstract void frameStart(Matrix4f pModelViewMatrix, float pPartialTicks, Vec3 pCameraPosition, Matrix4f pProjection);
    
    /**
     * This is abstract because VK can handle the writes being done on a separate thread
     */
    public abstract void lightUpdated();
    
    public abstract void preTerrainSetup();
    
    public abstract void shadowPass(Matrix4f modelViewMatrix);
    
    public abstract void preOpaque();
    
    public abstract void endOpaque();
    
    public abstract void endTranslucent();
    
    public abstract void waitIdle();
    
    public abstract int frameInFlight();
    
    public abstract void sectionDirty(int x, int y, int z);
    
    public abstract void addDebugText(List<String> list);
}
