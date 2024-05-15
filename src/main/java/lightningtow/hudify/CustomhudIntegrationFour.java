package lightningtow.hudify;

import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
import com.minenash.customhud.data.Flags;
import org.apache.logging.log4j.Level;

import static com.minenash.customhud.data.Flags.wrap;
import static com.minenash.customhud.registry.CustomHudRegistry.registerElement;
import static lightningtow.hudify.util.SpotifyData.*;

public class CustomhudIntegrationFour {

        public static void initCustomhud() {

            HudifyMain.Log(Level.INFO,"Integrating with CustomHud");

            UpdateMaps();
            stringmap.forEach((key, value) -> registerElement(key, (f, c) ->  wrap(new StringSupplierElement(() -> value.isEmpty() ? null : value), f)));
            boolmap.forEach((key, value) -> registerElement(key, (f, c) -> wrap(new BooleanSupplierElement(() -> value), f)));
            intmap.forEach((key, value) -> registerElement(key, (f, c) -> new NumberSupplierElement(() -> value, new Flags())));

            for ( String key : specialmap.keySet() ) {
                SpecialSupplierElement.Entry elem = SpecialSupplierElement.of(
                        /* string */ () -> specialmap.get(key).getA(),
                        /* number */ () -> specialmap.get(key).getB(),
                        /*  bool  */ () -> specialmap.get(key).getC()
                );
                registerElement(key, (flags, context) -> new SpecialSupplierElement(elem));
            }


//            specialmap.forEach((key, value) -> registerElement(key, (f, c) -> new SpecialSupplierElement(() -> value.getA(), value.getB(), value.getC())));
//            SpecialSupplierElement.Entry elem2 = SpecialSupplierElement.of(
//                    /* string */ () -> "test",
//                    /* number */ () -> 42,
//                    /*  bool  */ () -> true
//            );
//            SpecialSupplierElement.Entry elem3 = SpecialSupplierElement.of(() -> "test", () -> 42, () -> true);
//            registerElement(key, (flags, context) -> new SpecialSupplierElement.Entry(() -> "test", () -> 42, () -> true));
//            registerElement("test", (flags, context) -> new SpecialSupplierElement(elem2));
//            registerElement("test2", (flags, context) -> new SpecialSupplierElement.Entry(() -> (()->"test", ()->42, ()->true) ));


//            for ( String key : stringmap.keySet() ) { // f, c == flags, context
//                registerElement(key, (f, c) ->  wrap(new StringSupplierElement(() -> stringmap.get(key).isEmpty() ? null : stringmap.get(key)), f));
////                StringSupplierElement elem = new StringSupplierElement(() -> stringmap.get(key).isEmpty() ? null : stringmap.get(key));
////                registerElement(key,  (flags, context) ->  wrap(elem, flags));
//            }
//            for ( String key : boolmap.keySet() ) {
//                registerElement(key, (f, c) ->  wrap(new BooleanSupplierElement(() -> boolmap.get(key)), f));
////                BooleanSupplierElement elem = new BooleanSupplierElement(() -> boolmap.get(key));
////                registerElement(key, (flags, context) ->  wrap(elem, flags));
//            }
//            for ( String key : intmap.keySet() ) {
//                registerElement(key, (f, c) -> new NumberSupplierElement(() -> intmap.get(key), new Flags()));
////                NumberSupplierElement elem = new NumberSupplierElement(() -> intmap.get(key), new Flags());
////                registerElement(key, (flags, context) -> elem);
//            }
//            for ( String key : specialmap.keySet() ) {
//                SpecialSupplierElement.Entry elem = SpecialSupplierElement.of(
//                        /* string */ () -> specialmap.get(key).getA(),
//                        /* number */ () -> specialmap.get(key).getB(),
//                        /*  bool  */ () -> specialmap.get(key).getC()
//                );
//                registerElement(key, (flags, context) -> new SpecialSupplierElement(elem));
//            }

            HudifyMain.Log(Level.INFO,"Successfully integrated with CustomHud");



        }

}
