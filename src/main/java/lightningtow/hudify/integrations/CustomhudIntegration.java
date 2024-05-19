package lightningtow.hudify.integrations;
import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
import static lightningtow.hudify.util.SpotifyData.*;

/*? if =4.0 {*//* // <- opener

import com.minenash.customhud.data.Flags;
import static com.minenash.customhud.data.Flags.wrap;
import static com.minenash.customhud.registry.CustomHudRegistry.registerElement;
public class CustomhudIntegration {
        public static void initCustomhud() {

            // don't put log messages here, messages already in HudifyMain
            UpdateMaps();

            for ( String key : stringmap.keySet() ) { // f, c == flags, context
                registerElement(key, (f, c) ->  wrap(new StringSupplierElement(() -> stringmap.get(key).isEmpty() ? null : stringmap.get(key)), f)); }

            for ( String key : boolmap.keySet() ) {
                registerElement(key, (f, c) ->  wrap(new BooleanSupplierElement(() -> boolmap.get(key)), f)); }
            for ( String key : intmap.keySet() ) {
                registerElement(key, (f, c) -> new NumberSupplierElement(() -> intmap.get(key), new Flags())); }

            for ( String key : specialmap.keySet() ) {
                registerElement(key, (f, c) -> new SpecialSupplierElement(SpecialSupplierElement.of(
                        () -> specialmap.get(key).getA(),
                        () -> specialmap.get(key).getB(),
                        () -> specialmap.get(key).getC()
                )));
            }
        }
}
*//*?} */
/*? if =3.3 {*/ // <- opener
import static com.minenash.customhud.mod_compat.CustomHudRegistry.registerElement;
public class CustomhudIntegration {
        public static void initCustomhud() {

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
