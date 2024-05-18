/*? if =3.3 {*/ // <- opener

package lightningtow.hudify;
import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
import lightningtow.hudify.util.SpotifyData;

import static com.minenash.customhud.mod_compat.CustomHudRegistry.registerElement;
import static lightningtow.hudify.util.SpotifyData.*;

public class CustomhudIntegrationThree {
    public static void initCustomhudThree() {

        UpdateMaps();
        // don't put log messages here, messages already in HudifyMain
        for ( String key : stringmap.keySet() ) {
            registerElement(key, (_str) -> new StringSupplierElement(() -> stringmap.get(key).isEmpty() ? null : stringmap.get(key))); }

        for ( String key : boolmap.keySet() ) { // make this bool when jakob fixes
            registerElement(key, (_str) -> new StringSupplierElement(() -> boolmap.get(key).toString())); }

        for ( String key : intmap.keySet() ) {
            registerElement(key, (_int) -> new NumberSupplierElement(() -> intmap.get(key), 1)); }

        for ( String key : specialmap.keySet() ) {
            registerElement(key, (_special) -> new SpecialSupplierElement(SpecialSupplierElement.of(
                    () -> specialmap.get(key).getA(),
                    () -> specialmap.get(key).getB(),
                    () -> specialmap.get(key).getC()
            )));
        }
    }
}
/*?} */
