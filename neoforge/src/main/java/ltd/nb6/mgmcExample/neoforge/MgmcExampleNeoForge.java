package ltd.nb6.mgmcExample.neoforge;

import ltd.nb6.mgmcExample.MgmcExample;
import net.neoforged.fml.common.Mod;

@Mod(MgmcExample.MOD_ID)
public final class MgmcExampleNeoForge {
    public MgmcExampleNeoForge() {
        // Run our common setup.
        MgmcExample.init();
    }
}
