package lightningtow.hudify;

//import eu.midnightdust.lib.config.MidnightConfig;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> MidnightConfig.getScreen(parent, HudifyMain.MOD_ID.toLowerCase());
    }
}
