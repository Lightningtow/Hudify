package lightningtow.hudify;

import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
import com.minenash.customhud.data.Flags;
import lightningtow.hudify.util.SpotifyData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.minenash.customhud.data.Flags.wrap;
import static com.minenash.customhud.registry.CustomHudRegistry.registerElement;
import static lightningtow.hudify.util.SpotifyData.*;

public class CustomhudIntegrationFour {
    public static final Logger LOGGER = LogManager.getLogger(HudifyMain.MOD_ID);

        public static Map<String, String> stringmap;
        public static Map<String, Boolean> boolmap;
        public static Map<String, Integer> intmap;

        public static void UpdateMaps() {
                stringmap.put("sp_song", sp_track);
                stringmap.put("sp_track", sp_track);
                stringmap.put("sp_fancy_track", sp_fancy_track);
                stringmap.put("sp_artist", sp_artists);
                stringmap.put("sp_artists", sp_artists);
                stringmap.put("sp_first_artist", sp_first_artist);
                stringmap.put("sp_context_type", sp_context_type);
                stringmap.put("sp_context_name", sp_context_name);
                stringmap.put("sp_album", sp_album);
                stringmap.put("sp_repeat", sp_repeat_state);
                stringmap.put("sp_repeat_state", sp_repeat_state);
                stringmap.put("sp_device_id", sp_device_id);
                stringmap.put("sp_device_name", sp_device_name);
                stringmap.put("sp_status_string", get_status_string(sp_status_code));

                boolmap.put("sp_device_is_active", sp_device_is_active);
                boolmap.put("sp_shuffle", sp_shuffle_enabled);
                boolmap.put("sp_is_podcast", sp_is_podcast);
                boolmap.put("sp_is_playing", sp_is_playing);
                boolmap.put("sp_is_authorized", sp_is_authorized);

                intmap.put("sp_status_code", sp_status_code);

        }



        public static void RegisterStrings() {
                UpdateMaps();

                for ( String key : stringmap.keySet() ) {
                        StringSupplierElement elem = new StringSupplierElement(() -> stringmap.get(key).isEmpty() ? null : stringmap.get(key));
                        registerElement(key,  (flags, context) ->  wrap(elem, flags));
                }
                for ( String key : boolmap.keySet() ) {
                        BooleanSupplierElement elem = new BooleanSupplierElement(() -> boolmap.get(key));
                        registerElement(key, (flags, context) ->  wrap(elem, flags));
                }
                for ( String key : intmap.keySet() ) {
                        NumberSupplierElement elem = new NumberSupplierElement(() -> intmap.get(key), new Flags());
                        registerElement(key, (flags, context) -> elem);
                }

                LOGGER.info("Successfully integrated with CustomHud");
        }




        public static void initCustomhud() {

                LOGGER.info("Integrating with CustomHud");







                /** progress **/
                SpecialSupplierElement.Entry progress_entry = SpecialSupplierElement.of(
                        /* string */ () -> (sp_progress / 60) + ":" + String.format("%02d", sp_progress % 60),
                        /* number */ () -> sp_progress,
                        /*  bool  */ () -> sp_progress > 0
                );
                registerElement("sp_progress", (flags, context) -> new SpecialSupplierElement(progress_entry));
                registerElement("sp_prog",  (flags, context) -> new SpecialSupplierElement(progress_entry));


                /** duration **/
                SpecialSupplierElement.Entry duration = SpecialSupplierElement.of(
                        /* string */ () -> (sp_duration / 60) + ":" + String.format("%02d", sp_duration % 60),
                        /* number */ () -> sp_duration,
                        /*  bool  */ () -> sp_duration > 0
                );
                registerElement("sp_duration", (flags, context) -> new SpecialSupplierElement(duration));
                registerElement("sp_dur",  (flags, context) -> new SpecialSupplierElement(duration));


                /** message **/
                SpecialSupplierElement.Entry message = SpecialSupplierElement.of(
        //                /* string */() -> (SpotifyData.get_sp_message()),
                        /* string */ SpotifyData::get_sp_message,
                        /* number */ () -> sp_msg_time_rem,
                        /*  bool  */ () -> (!get_sp_message().isEmpty())
                );
                registerElement("sp_message", (flags, context) -> new SpecialSupplierElement(message));
                registerElement("sp_msg",  (flags, context) -> new SpecialSupplierElement(message));





        }

}
