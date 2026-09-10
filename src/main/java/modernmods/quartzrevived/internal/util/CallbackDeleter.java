package modernmods.quartzrevived.internal.util;

import modernmods.quartzrevived.internal.Buffer;
import modernmods.quartzrevived.internal.QuartzCore;

public class CallbackDeleter implements Buffer.CallbackHandle {
    private final Runnable deleteFunc;
    private final boolean[] deleted;
    
    public CallbackDeleter(Runnable func) {
        final var deleted = new boolean[]{false};
        QuartzCore.CLEANER.register(this, () -> QuartzCore.deletionQueue.enqueue(() -> {
            if (!deleted[0]) {
                func.run();
            }
        }));
        this.deleteFunc = func;
        this.deleted = deleted;
    }
    
    @Override
    public void delete() {
        deleted[0] = true;
        deleteFunc.run();
    }
}
