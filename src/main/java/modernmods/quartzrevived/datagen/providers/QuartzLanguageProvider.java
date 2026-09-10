package modernmods.quartzrevived.datagen.providers;

import modernmods.quartzrevived.Quartz;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class QuartzLanguageProvider extends LanguageProvider {

    public static final String[] LOCALES = {"en_us", "es_es", "es_mx", "es_ar"};

    private final String locale;

    public QuartzLanguageProvider(PackOutput output, String locale) {
        super(output, Quartz.modid, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        switch (locale) {
            case "es_es", "es_mx", "es_ar" -> spanish();
            default -> english();
        }
    }

    private void english() {
        add("item_group.quartz", "QuartzRenderingLib");
        add("block.quartz.quartz_test_block", "QuartzRenderingTestBlock");
    }

    private void spanish() {
        add("item_group.quartz", "QuartzRenderingLib");
        add("block.quartz.quartz_test_block", "Bloque de Prueba de Renderizado de Quartz");
    }
}
