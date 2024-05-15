//package lightningtow.hudify;
//import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
//import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
//import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
//import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
//import com.minenash.customhud.data.Flags;
//
//import static com.minenash.customhud.data.Flags.wrap;
//import static com.minenash.customhud.registry.CustomHudRegistry.registerElement;
//import static lightningtow.hudify.util.SpotifyData.*;
//
//public class CustomhudIntegrationFour {
//        public static void initCustomhud() {
//
//            // don't put log messages here, messages already in HudifyMain
//            UpdateMaps();
//
//            for ( String key : stringmap.keySet() ) { // f, c == flags, context
//                registerElement(key, (f, c) ->  wrap(new StringSupplierElement(() -> stringmap.get(key).isEmpty() ? null : stringmap.get(key)), f)); }
//
//            for ( String key : boolmap.keySet() ) {
//                registerElement(key, (f, c) ->  wrap(new BooleanSupplierElement(() -> boolmap.get(key)), f)); }
//            for ( String key : intmap.keySet() ) {
//                registerElement(key, (f, c) -> new NumberSupplierElement(() -> intmap.get(key), new Flags())); }
//
//            for ( String key : specialmap.keySet() ) {
//                registerElement(key, (f, c) -> new SpecialSupplierElement(SpecialSupplierElement.of(
//                        () -> specialmap.get(key).getA(),
//                        () -> specialmap.get(key).getB(),
//                        () -> specialmap.get(key).getC()
//                )));
//            }
//            /** why doesnt this work */
////            stringmap.forEach((key, value) -> {
////              registerElement(key, (f, c) ->  wrap(new StringSupplierElement(() -> value.isEmpty() ? null : value), f));
////            });
////            boolmap.forEach((key, value) -> { registerElement(key, (f, c) -> wrap(new BooleanSupplierElement(() -> value), f)); } );
////            intmap.forEach((key, value) -> registerElement(key, (f, c) -> new NumberSupplierElement(() -> value, new Flags())));
////            specialmap.forEach((key, v) ->
////                    registerElement(key, (f, c) -> new SpecialSupplierElement(SpecialSupplierElement.of(v::getA, v::getB, v::getC))));
//        }
//}
