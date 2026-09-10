package modernmods.quartzrevived.internal.gl46;

import modernmods.phosphophylliterevived.config.ConfigValue;

public class GL46Config {
    public static final GL46Config INSTANCE = new GL46Config();
    
    @ConfigValue(advanced = ConfigValue.BoolOption.False)
    public final boolean ALLOW_SPARSE_TEXTURE;
    
    {
        ALLOW_SPARSE_TEXTURE = true;
    }
}
