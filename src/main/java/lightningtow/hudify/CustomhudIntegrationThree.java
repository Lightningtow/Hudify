//package lightningtow.hudify;
//
//import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
//import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
//import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
//import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
//import lightningtow.hudify.util.SpotifyData;
//
//import static com.minenash.customhud.mod_compat.CustomHudRegistry.registerElement;
//import static lightningtow.hudify.util.SpotifyData.*;
//
//public class CustomhudIntegrationThree {
//
//    public static void initCustomhud() {
//
//        UpdateMaps();
//
//        for ( String key : stringmap.keySet() ) {
//            StringSupplierElement elem = new StringSupplierElement(() -> stringmap.get(key).isEmpty() ? null : stringmap.get(key));
//            registerElement(key, (_str) -> elem);
//        }
//        for ( String key : boolmap.keySet() ) {
//            StringSupplierElement elem = new StringSupplierElement(() -> boolmap.get(key).toString());
//            registerElement(key, (_str) -> elem);
//        }
//        for ( String key : intmap.keySet() ) {
//            NumberSupplierElement elem = new NumberSupplierElement(() -> intmap.get(key), 1);
//            registerElement(key, (_int) -> elem);
//        }
//
//        for ( String key : specialmap.keySet() ) {
//            SpecialSupplierElement.Entry elem = SpecialSupplierElement.of(
//                    /* string */ () -> specialmap.get(key).getA(),
//                    /* number */ () -> specialmap.get(key).getB(),
//                    /*  bool  */ () -> specialmap.get(key).getC()
//            );
//            registerElement(key, (_special) -> new SpecialSupplierElement(elem));
//        }
//
//
//    }
//
//}
