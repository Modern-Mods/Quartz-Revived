package modernmods.quartzrevived.internal;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import modernmods.phosphophylliterevived.registry.ClientOnly;
import modernmods.phosphophylliterevived.registry.OnModLoad;
import modernmods.phosphophylliterevived.util.NonnullDefault;

@ClientOnly
@NonnullDefault
public class EventListener {

    private static final ReferenceSet<ModelResourceLocation> modelsToRegister = new ReferenceArraySet<>();

    public static void registerModel(ResourceLocation modelLocation) {
        modelsToRegister.add(ModelResourceLocation.standalone(modelLocation));
    }

    private static void onModelRegisterEvent(ModelEvent.RegisterAdditional event) {
        modelsToRegister.forEach(event::register);
    }

    @OnModLoad
    private static void onModLoad() {
        final var modBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        if (modBus == null) {
            return;
        }
        modBus.addListener(EventListener::onModelRegisterEvent);
        if (!DatagenModLoader.isRunningDataGen()) {
            modBus.addListener(EventListener::clientSetup);
        }
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                QuartzCore.startup();
            } catch (Throwable e) {
                final var minecraft = Minecraft.getInstance();
                Minecraft.crash(minecraft, minecraft.gameDirectory, new CrashReport("Quartz startup exception", e));
            }
        });
    }

    public static void initQuartz() {
    }

    static {
        if (!DatagenModLoader.isRunningDataGen()) {
            try {
                QuartzCore.init();
            } catch (Throwable e) {
                final var minecraft = Minecraft.getInstance();
                Minecraft.crash(minecraft, minecraft.gameDirectory, new CrashReport("Quartz failed to startup", e));
            }
        }
    }
}
