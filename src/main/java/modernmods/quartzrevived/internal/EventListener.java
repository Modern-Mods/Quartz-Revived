package modernmods.quartzrevived.internal;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import modernmods.phosphophylliterevived.registry.ClientOnly;
import modernmods.phosphophylliterevived.registry.OnModLoad;
import modernmods.phosphophylliterevived.util.NonnullDefault;

@ClientOnly
@NonnullDefault
public class EventListener {

    private static final Object2ObjectMap<Identifier, StandaloneModelKey<BlockStateModelPart>> modelsToRegister = new Object2ObjectOpenHashMap<>();

    public static synchronized StandaloneModelKey<BlockStateModelPart> registerModel(Identifier modelLocation) {
        return modelsToRegister.computeIfAbsent(modelLocation, (Identifier location) -> new StandaloneModelKey<>(location::toString));
    }

    private static void onModelRegisterEvent(ModelEvent.RegisterStandalone event) {
        modelsToRegister.forEach((location, key) -> event.register(key, SimpleUnbakedStandaloneModel.simpleModelWrapper(location)));
    }

    @OnModLoad
    private static void onModLoad() {
        final var modBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        if (modBus == null) {
            return;
        }
        modBus.addListener(EventListener::onModelRegisterEvent);
        modBus.addListener(QuartzCore::registerDebugEntries);
    }

    public static void initQuartz() {
        if (DatagenModLoader.isRunningDataGen()) {
            return;
        }
        try {
            QuartzCore.init();
        } catch (Throwable e) {
            throw new IllegalStateException("Quartz failed to startup", e);
        }
    }
}
