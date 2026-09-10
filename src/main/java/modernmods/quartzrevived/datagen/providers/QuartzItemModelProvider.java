package modernmods.quartzrevived.datagen.providers;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import modernmods.quartzrevived.Quartz;

public class QuartzItemModelProvider extends ItemModelProvider {

    public QuartzItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Quartz.modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent("quartz_test_block", ResourceLocation.parse("phosphophyllite:block/phosphophyllite_ore"));
    }
}
