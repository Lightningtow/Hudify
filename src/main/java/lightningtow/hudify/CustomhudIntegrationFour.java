package lightningtow.hudify;

import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
import com.minenash.customhud.data.Flags;
import lightningtow.hudify.util.SpotifyData;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static com.minenash.customhud.data.Flags.wrap;
import static com.minenash.customhud.registry.CustomHudRegistry.registerElement;
import static lightningtow.hudify.util.SpotifyData.*;

public class CustomhudIntegrationFour {// implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(HudifyMain.MOD_ID);

//    @Override
//    public void onInitializeClient() {
        public static void initCustomhud() {

        LOGGER.info("Integrating with CustomHud");

        /** strings **/
        StringSupplierElement track = new StringSupplierElement(() -> sp_track.isEmpty() ? null : sp_track);
        registerElement("sp_song",  (flags, context) ->  wrap(track, flags));
        registerElement("sp_track",  (flags, context) ->  wrap(track, flags));

        StringSupplierElement fancy_track = new StringSupplierElement(() -> sp_fancy_track.isEmpty() ? null : sp_fancy_track);
        registerElement("sp_fancy_track",  (flags, context) ->  wrap(fancy_track, flags));

        StringSupplierElement artists = new StringSupplierElement(() -> sp_artists.isEmpty() ? null : sp_artists);
        registerElement("sp_artist",  (flags, context) ->  wrap(artists, flags));
        registerElement("sp_artists",  (flags, context) ->  wrap(artists, flags));

        StringSupplierElement first_artist = new StringSupplierElement(() -> sp_first_artist.isEmpty() ? null : sp_first_artist);
        registerElement("sp_first_artist",  (flags, context) ->  wrap(first_artist, flags));

        StringSupplierElement context_type = new StringSupplierElement(() -> sp_context_type.isEmpty() ? null : sp_context_type);
        registerElement("sp_context_type", (flags, context) ->  wrap(context_type, flags));

        StringSupplierElement context_name = new StringSupplierElement(() -> sp_context_name);
        registerElement("sp_context_name", (flags, context) ->  wrap(context_name, flags));

        StringSupplierElement album = new StringSupplierElement(() -> sp_album.isEmpty() ? null : sp_album);
        registerElement("sp_album", (flags, context) ->  wrap(album, flags));

        StringSupplierElement repeat_state = new StringSupplierElement(() -> sp_repeat_state.isEmpty() ? null : sp_repeat_state);
        registerElement("sp_repeat_enabled", (flags, context) ->  wrap(repeat_state, flags));
        registerElement("sp_repeat", (flags, context) ->  wrap(repeat_state, flags));


        StringSupplierElement device_id = new StringSupplierElement(() -> sp_device_id.isEmpty() ? null : sp_device_id);
        registerElement("sp_device_id", (flags, context) ->  wrap(device_id, flags));

        StringSupplierElement device_name = new StringSupplierElement(() -> sp_device_name.isEmpty() ? null : sp_device_name);
        registerElement("sp_device_name", (flags, context) ->  wrap(device_name, flags));


        /** booleans **/
        BooleanSupplierElement device_is_active = new BooleanSupplierElement(() -> sp_device_is_active);
        registerElement("sp_device_is_active", (flags, context) ->  wrap(device_is_active, flags));

        BooleanSupplierElement shuffle_state = new BooleanSupplierElement(() -> sp_shuffle_enabled);
        registerElement("sp_shuffle", (flags, context) ->  wrap(shuffle_state, flags));
//
        BooleanSupplierElement is_podcast = new BooleanSupplierElement(() -> sp_is_podcast);
        registerElement("sp_is_podcast", (flags, context) ->  wrap(is_podcast, flags));

        BooleanSupplierElement is_playing = new BooleanSupplierElement(() -> sp_is_playing);
        registerElement("sp_is_playing", (flags, context) -> is_playing);

        BooleanSupplierElement is_authorized = new BooleanSupplierElement(() -> sp_is_authorized);
        registerElement("sp_is_authorized", (flags, context) -> is_authorized);


        /** debug variables **/
        NumberSupplierElement status_code = new NumberSupplierElement(() -> sp_status_code, new Flags());
        registerElement("sp_status_code", (flags, context) -> status_code);

        StringSupplierElement status_string = new StringSupplierElement(() -> get_status_string(sp_status_code));
        registerElement("sp_status_string", (flags, context) ->  wrap(status_string, flags));



        /** specials **/
        SpecialSupplierElement.Entry progress_entry = SpecialSupplierElement.of(
                () -> (sp_progress / 60) + ":" + String.format("%02d", sp_progress % 60), /* string */
                () -> sp_progress /* number */ , () -> sp_progress > 0 /* bool */ );
        registerElement("sp_progress", (flags, context) -> new SpecialSupplierElement(progress_entry));
        registerElement("sp_prog",  (flags, context) -> new SpecialSupplierElement(progress_entry));


        SpecialSupplierElement.Entry duration = SpecialSupplierElement.of(
                () -> (sp_duration / 60) + ":" + String.format("%02d", sp_duration % 60), /* string */
                () -> sp_duration /* number */, () -> sp_duration > 0 /* bool */ );
        registerElement("sp_duration", (flags, context) -> new SpecialSupplierElement(duration));
        registerElement("sp_dur",  (flags, context) -> new SpecialSupplierElement(duration));


        SpecialSupplierElement.Entry message = SpecialSupplierElement.of(
//                () -> (sp_message), // string
                SpotifyData::get_sp_message, /* string */
                () -> sp_msg_time_rem /* number */, () -> (!get_sp_message().isEmpty()) /* bool */ );
        registerElement("sp_message", (flags, context) -> new SpecialSupplierElement(message));
        registerElement("sp_msg",  (flags, context) -> new SpecialSupplierElement(message));





    }

}