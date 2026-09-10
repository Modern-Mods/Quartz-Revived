package modernmods.quartzrevived;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.resources.ResourceLocation;
import modernmods.phosphophylliterevived.Phosphophyllite;

import java.util.List;

public final class CreativeTabOrder {

    public static List<ResourceLocation> before() {
        return ReferenceArrayList.of(ResourceLocation.fromNamespaceAndPath(Phosphophyllite.modid, "creative_tab"));
    }

    public static List<ResourceLocation> after() {
        return new ReferenceArrayList<>();
    }
}
