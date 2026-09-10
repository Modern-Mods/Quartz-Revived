package modernmods.quartzrevived.internal;

import modernmods.phosphophylliterevived.registry.ClientOnly;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@ClientOnly
public final class IrisDetection {

    private static final Object irisApi;
    private static final MethodHandle isShaderPackInUse;
    private static final MethodHandle isRenderingShadowPass;
    private static final MethodHandle getPipelineNullable;
    private static final MethodHandle pipelineManager;

    static {
        Object api = null;
        MethodHandle inUse = null;
        MethodHandle shadowPass = null;
        MethodHandle manager = null;
        MethodHandle pipeline = null;
        try {
            final var lookup = MethodHandles.publicLookup();
            final var apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            api = lookup.findStatic(apiClass, "getInstance", MethodType.methodType(apiClass)).invoke();
            inUse = lookup.findVirtual(apiClass, "isShaderPackInUse", MethodType.methodType(boolean.class));
            shadowPass = lookup.findVirtual(apiClass, "isRenderingShadowPass", MethodType.methodType(boolean.class));

            final var irisClass = Class.forName("net.irisshaders.iris.Iris");
            final var managerClass = Class.forName("net.irisshaders.iris.pipeline.PipelineManager");
            final var pipelineClass = Class.forName("net.irisshaders.iris.pipeline.WorldRenderingPipeline");
            manager = lookup.findStatic(irisClass, "getPipelineManager", MethodType.methodType(managerClass));
            pipeline = lookup.findVirtual(managerClass, "getPipelineNullable", MethodType.methodType(pipelineClass));
        } catch (Throwable e) {
            api = null;
            inUse = null;
            shadowPass = null;
            manager = null;
            pipeline = null;
        }
        irisApi = api;
        isShaderPackInUse = inUse;
        isRenderingShadowPass = shadowPass;
        pipelineManager = manager;
        getPipelineNullable = pipeline;
    }

    public static boolean areShadersActive() {
        if (irisApi == null) {
            return false;
        }
        try {
            return (boolean) isShaderPackInUse.invoke(irisApi);
        } catch (Throwable e) {
            return false;
        }
    }

    public static void bindIrisFramebuffer() {
        if (!areShadersActive() || pipelineManager == null) {
            return;
        }
        try {
            final var manager = pipelineManager.invoke();
            if (manager == null) {
                return;
            }
            final var pipeline = getPipelineNullable.invoke(manager);
            if (pipeline == null) {
                return;
            }
            final var bindDefault = MethodHandles.publicLookup().findVirtual(pipeline.getClass(), "bindDefault", MethodType.methodType(void.class));
            bindDefault.invoke(pipeline);
        } catch (Throwable ignored) {
        }
    }

    public static boolean isRenderingShadows() {
        if (irisApi == null) {
            return false;
        }
        try {
            return (boolean) isRenderingShadowPass.invoke(irisApi);
        } catch (Throwable e) {
            return false;
        }
    }
}
