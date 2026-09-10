package modernmods.quartzrevived.datagen.providers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import modernmods.quartzrevived.Quartz;

public class QuartzBlockStateProvider extends BlockStateProvider {

    public QuartzBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Quartz.modid, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        final var testBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(Quartz.modid, "quartz_test_block"));
        simpleBlock(testBlock, models().getExistingFile(ResourceLocation.parse("phosphophyllite:block/phosphophyllite_ore")));
    }
}
