import eu.midnightdust.lib.config.MidnightConfig;
import lightningtow.hudify.HudifyMain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> MidnightConfig.getScreen(parent, "hudify");
    }
}
