package modernmods.quartzrevived;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.resources.Identifier;
import modernmods.phosphophylliterevived.Phosphophyllite;

import java.util.List;

public final class CreativeTabOrder {

    public static List<Identifier> before() {
        return ReferenceArrayList.of(Identifier.fromNamespaceAndPath(Phosphophyllite.modid, "creative_tab"));
    }

    public static List<Identifier> after() {
        return new ReferenceArrayList<>();
    }
}
