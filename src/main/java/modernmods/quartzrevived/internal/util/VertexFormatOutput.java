package modernmods.quartzrevived.internal.util;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.Map;

public record VertexFormatOutput(VertexFormat format, String[] varyings, int vertexSize) {
    
    private static final Map<VertexFormatElement, String> elementVaryingNames;
    private static final Object2ObjectOpenHashMap<VertexFormat, VertexFormatOutput> outputs = new Object2ObjectOpenHashMap<>();
    
    static {
        final var map = new Object2ObjectArrayMap<VertexFormatElement, String>();
        map.put(VertexFormatElement.POSITION, "positionOutput");
        map.put(VertexFormatElement.NORMAL, "normalOutput");
        map.put(VertexFormatElement.COLOR, "colorOutput");
        map.put(VertexFormatElement.UV0, "textureOutput");
        map.put(VertexFormatElement.UV1, "overlayOutput");
        map.put(VertexFormatElement.UV2, "lightmapOutput");
        elementVaryingNames = Collections.unmodifiableMap(map);
    }
    
    public static VertexFormatOutput of(VertexFormat format) {
        return outputs.computeIfAbsent(format, (VertexFormat e) -> new VertexFormatOutput(e));
    }
    
    private VertexFormatOutput(VertexFormat format) {
        this(format, generateVaryings(format), format.getVertexSize());
    }
    
    private static int offsetOf(VertexFormat format, VertexFormatElement element) {
        if (!format.contains(element)) {
            return 0;
        }
        return format.getOffset(element);
    }
    
    private static String[] generateVaryings(VertexFormat format) {
        final var elements = format.getElements();
        final var list = new ObjectArrayList<String>();
        for (final var element : elements) {
            var elementName = elementVaryingNames.get(element);
            if (elementName == null) {
                throw new IllegalStateException("Unknown vertex format element");
            }
            list.add(elementName);
        }
        return list.toArray(new String[]{});
    }
    
    public static String outputName(VertexFormatElement element) {
        return elementVaryingNames.get(element);
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof VertexFormatOutput other && format == other.format;
    }
}
