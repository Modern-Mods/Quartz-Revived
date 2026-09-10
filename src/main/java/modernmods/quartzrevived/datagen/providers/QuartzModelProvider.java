package modernmods.quartzrevived.datagen.providers;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import modernmods.quartzrevived.Quartz;

public class QuartzModelProvider extends ModelProvider {

    public QuartzModelProvider(PackOutput output) {
        super(output, Quartz.modid);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        final var testBlock = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(Quartz.modid, "quartz_test_block"));
        final var oreModel = Identifier.parse("phosphophyllite:block/phosphophyllite_ore");
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(testBlock, BlockModelGenerators.plainVariant(oreModel)));
        blockModels.registerSimpleItemModel(testBlock, oreModel);
    }
}
