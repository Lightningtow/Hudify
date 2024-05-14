//package lightningtow.hudify;
//
//import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
//import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
//import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
//import lightningtow.hudify.util.SpotifyData;
//import lightningtow.hudify.util.SpotifyUtil;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import static com.minenash.customhud.mod_compat.CustomHudRegistry.registerElement;
////import static com.minenash.customhud.data.Flags.wrap;
//import static lightningtow.hudify.util.SpotifyData.*;
//
//public class CustomhudIntegrationThree {//} implements ClientModInitializer {
//    public static final Logger LOGGER = LogManager.getLogger(HudifyMain.MOD_ID);
//
////    @Override
//    public static void initCustomhud() {
//
//
//
//            /** strings **/
//            StringSupplierElement track = new StringSupplierElement(() -> sp_track.isEmpty() ? null : sp_track);
//            registerElement("sp_song", (_str) -> track);
//            registerElement("sp_track", (_str) -> track);
//
//            StringSupplierElement fancy_track = new StringSupplierElement(() -> sp_fancy_track.isEmpty() ? null : sp_fancy_track);
//            registerElement("sp_fancy_track", (_str) -> fancy_track);
//
//            StringSupplierElement artists = new StringSupplierElement(() -> sp_artists.isEmpty() ? null : sp_artists);
//            registerElement("sp_artist", (_str) -> artists);
//            registerElement("sp_artists", (_str) -> artists);
//
//            StringSupplierElement first_artist = new StringSupplierElement(() -> sp_first_artist.isEmpty() ? null : sp_first_artist);
//            registerElement("sp_first_artist", (_str) -> first_artist);
//
//            StringSupplierElement context_type = new StringSupplierElement(() -> sp_context_type.isEmpty() ? null : sp_context_type);
//            registerElement("sp_context_type", (_str) -> context_type);
//
//            StringSupplierElement context_name = new StringSupplierElement(() -> sp_context_name);
//            registerElement("sp_context_name", (_str) -> context_name);
//
//            StringSupplierElement album = new StringSupplierElement(() -> sp_album.isEmpty() ? null : sp_album);
//            registerElement("sp_album", (_str) -> album);
//
//            StringSupplierElement repeat_state = new StringSupplierElement(() -> sp_repeat_state.isEmpty() ? null : sp_repeat_state);
//            registerElement("sp_repeat", (_str) -> repeat_state);
//
//
//            /** booleans **/
////            BooleanSupplierElement shuffle_state = new BooleanSupplierElement(() -> sp_shuffle_enabled);
////            registerElement("sp_shuffle", (_str) -> shuffle_state);
////
////            BooleanSupplierElement is_podcast = new BooleanSupplierElement(() -> (Objects.equals(sp_media_type, "episode")));
////            registerElement("sp_is_podcast", (_str) -> is_podcast);
//
////            BooleanSupplierElement is_playing = new BooleanSupplierElement(() -> is_playing);
////            registerElement("sp_is_playing", (_str) -> is_playing);
////
////            BooleanSupplierElement is_authorized = new BooleanSupplierElement(() -> (is_authorized));
////            registerElement("sp_is_authorized", (_str) -> is_authorized);
//
//            StringSupplierElement shuffle_state = new StringSupplierElement(() -> sp_shuffle_enabled.toString());
//            registerElement("sp_shuffle", (_str) -> shuffle_state);
////
//            StringSupplierElement is_podcast = new StringSupplierElement(() -> String.valueOf(sp_is_podcast));
//            registerElement("sp_is_podcast", (_str) -> is_podcast);
//
//            StringSupplierElement is_playing = new StringSupplierElement(() -> String.valueOf(sp_is_playing));
//            registerElement("sp_is_playing", (_str) -> is_playing);
//
//            StringSupplierElement is_authorized = new StringSupplierElement(() -> String.valueOf(sp_is_authorized));
//            registerElement("sp_is_authorized", (_str) -> is_authorized);
//
//
//            /** numbers **/
//            NumberSupplierElement status_code = new NumberSupplierElement(() -> sp_status_code, 1);
//            registerElement("sp_status_code", (_int) -> status_code);
//
//
//            /** specials **/
//            SpecialSupplierElement.Entry progress_entry = SpecialSupplierElement.of(
//                    () -> (sp_progress / 60) + ":" + String.format("%02d", sp_progress % 60), /* string */
//                    () -> sp_progress /* number */, () -> sp_progress > 0 /* bool */);
//            registerElement("sp_progress", (_special) -> new SpecialSupplierElement(progress_entry));
//            registerElement("sp_prog", (_special) -> new SpecialSupplierElement(progress_entry));
//
//
//            SpecialSupplierElement.Entry duration = SpecialSupplierElement.of(
//                    () -> (sp_duration / 60) + ":" + String.format("%02d", sp_duration % 60), /* string */
//                    () -> sp_duration /* number */, () -> sp_duration > 0 /* bool */);
//            registerElement("sp_duration", (_special) -> new SpecialSupplierElement(duration));
//            registerElement("sp_dur", (_special) -> new SpecialSupplierElement(duration));
//
//
//            SpecialSupplierElement.Entry message = SpecialSupplierElement.of(
////                () -> (sp_message), // string
//                    SpotifyData::get_sp_message, /* string */
//                    () -> sp_msg_time_rem /* number */, () -> (!get_sp_message().isEmpty()) /* bool */);
//            registerElement("sp_message", (_special) -> new SpecialSupplierElement(message));
//            registerElement("sp_msg", (_special) -> new SpecialSupplierElement(message));
//
//
//            NumberSupplierElement message_duration = new NumberSupplierElement(() -> sp_msg_time_rem, 1);
//            registerElement("sp_message_duration", (_int) -> message_duration);
//            registerElement("sp_msg_dur", (_int) -> message_duration); // only needed for customhud 3.3
//
//
//
//
//    }
//
//}
