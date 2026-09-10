package modernmods.quartzrevived.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import modernmods.quartzrevived.Quartz;
import modernmods.quartzrevived.datagen.providers.QuartzLanguageProvider;
import modernmods.quartzrevived.datagen.providers.QuartzModelProvider;

@EventBusSubscriber(modid = Quartz.modid)
public final class QuartzDatagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        for (final var locale : QuartzLanguageProvider.LOCALES) {
            event.createProvider(output -> new QuartzLanguageProvider(output, locale));
        }
        event.createProvider(QuartzModelProvider::new);
    }
}
