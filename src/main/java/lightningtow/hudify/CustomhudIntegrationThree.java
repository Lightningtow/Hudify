package lightningtow.hudify;

import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
import lightningtow.hudify.util.SpotifyData;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

import static com.minenash.customhud.mod_compat.CustomHudRegistry.registerElement;
//import static com.minenash.customhud.data.Flags.wrap;
import static lightningtow.hudify.util.SpotifyData.*;

public class CustomhudIntegrationThree implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(HudifyMain.MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Integrating with CustomHud");

        /** strings **/
        StringSupplierElement track = new StringSupplierElement(() -> sp_track.isEmpty() ? null : sp_track);
        registerElement("sp_song", (_str) ->  track);
        registerElement("sp_track", (_str) ->  track);

        StringSupplierElement fancy_track = new StringSupplierElement(() -> sp_fancy_track.isEmpty() ? null : sp_fancy_track);
        registerElement("sp_fancy_track", (_str) ->  fancy_track);

        StringSupplierElement artists = new StringSupplierElement(() -> sp_artists.isEmpty() ? null : sp_artists);
        registerElement("sp_artist", (_str) ->  artists);
        registerElement("sp_artists", (_str) ->  artists);

        StringSupplierElement first_artist = new StringSupplierElement(() -> sp_first_artist.isEmpty() ? null : sp_first_artist);
        registerElement("sp_first_artist", (_str) ->  first_artist);

        StringSupplierElement context_type = new StringSupplierElement(() -> sp_context_type.isEmpty() ? null : sp_context_type);
        registerElement("sp_context_type", (_str) ->  context_type);

        StringSupplierElement context_name = new StringSupplierElement(() -> sp_context_name);
        registerElement("sp_context_name", (_str) ->  context_name);

        StringSupplierElement album = new StringSupplierElement(() -> sp_album.isEmpty() ? null : sp_album);
        registerElement("sp_album", (_str) ->  album);

        StringSupplierElement repeat_state = new StringSupplierElement(() -> sp_repeat_state.isEmpty() ? null : sp_repeat_state);
        registerElement("sp_repeat", (_str) ->  repeat_state);


        /** booleans **/
        StringSupplierElement shuffle_state = new StringSupplierElement(() -> String.valueOf(sp_shuffle_state));
        registerElement("sp_shuffle", (_str) ->  shuffle_state);
//
        StringSupplierElement is_podcast = new StringSupplierElement(() -> String.valueOf((Objects.equals(sp_media_type, "episode"))));
        registerElement("sp_is_podcast", (_str) ->  is_podcast);

        /** numbers **/
        NumberSupplierElement status_code = new NumberSupplierElement(() -> sp_status_code, 0);
        registerElement("sp_status_code", (_int) ->  status_code);

//        StringSupplierElement media_type = new StringSupplierElement(() -> sp_media_type.isEmpty() ? null : sp_media_type); // track or episode
//        CustomHudRegistry.registerElement("sp_media_type", (flags, context) ->  wrap(media_type, flags));  // removed in favor of `is_podcast`

        /** specials **/
        SpecialSupplierElement.Entry progress_entry = SpecialSupplierElement.of(
                () -> (sp_progress / 60) + ":" + String.format("%02d", sp_progress % 60), /* string */
                () -> sp_progress /* number */ , () -> sp_progress > 0 /* bool */ );
        registerElement("sp_progress", (_special) -> new SpecialSupplierElement(progress_entry));
        registerElement("sp_prog", (_special) -> new SpecialSupplierElement(progress_entry));


        SpecialSupplierElement.Entry duration = SpecialSupplierElement.of(
                () -> (sp_duration / 60) + ":" + String.format("%02d", sp_duration % 60), /* string */
                () -> sp_duration /* number */, () -> sp_duration > 0 /* bool */ );
        registerElement("sp_duration", (_special) -> new SpecialSupplierElement(duration));
        registerElement("sp_dur", (_special) -> new SpecialSupplierElement(duration));


        SpecialSupplierElement.Entry message = SpecialSupplierElement.of(
//                () -> (sp_message), // string
                SpotifyData::get_sp_message, /* string */
                () -> msg_time_rem /* number */, () -> (!get_sp_message().isEmpty()) /* bool */ );
        registerElement("sp_message", (_special) -> new SpecialSupplierElement(message));
        registerElement("sp_msg",  (_special) -> new SpecialSupplierElement(message));





    }

}
