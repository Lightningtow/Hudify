package lightningtow.chyma.integrations;

import lightningtow.chyma.ChymaMain;
import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;

import static com.minenash.customhud.data.Flags.wrap;
import static com.minenash.customhud.registry.CustomHudRegistry.registerElement;

public class CustomhudIntegration {
        public static void initCustomhud() {

            // don't put log messages here, messages already in Main

            registerElement("minimap_displayed", (f, c)
                    ->  wrap(new BooleanSupplierElement(() -> ChymaMain.minimap_displayed), f));
            registerElement("minimap", (f, c)
                    ->  wrap(new BooleanSupplierElement(() -> ChymaMain.minimap_displayed), f));


        }
}
