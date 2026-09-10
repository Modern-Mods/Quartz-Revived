package modernmods.quartzrevived.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import modernmods.quartzrevived.Quartz;
import modernmods.quartzrevived.datagen.providers.QuartzBlockStateProvider;
import modernmods.quartzrevived.datagen.providers.QuartzItemModelProvider;
import modernmods.quartzrevived.datagen.providers.QuartzLanguageProvider;

@EventBusSubscriber(modid = Quartz.modid, bus = EventBusSubscriber.Bus.MOD)
public final class QuartzDatagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        final var generator = event.getGenerator();
        final var output = generator.getPackOutput();
        final var existingFileHelper = event.getExistingFileHelper();

        for (final var locale : QuartzLanguageProvider.LOCALES) {
            generator.addProvider(event.includeClient(), new QuartzLanguageProvider(output, locale));
        }
        generator.addProvider(event.includeClient(), new QuartzBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new QuartzItemModelProvider(output, existingFileHelper));
    }
}
