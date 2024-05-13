package lightningtow.hudify;

import com.minenash.customhud.HudElements.interfaces.HudElement;
import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
import com.minenash.customhud.data.Flags;
import lightningtow.hudify.util.SpotifyData;
import net.fabricmc.api.ClientModInitializer;
import com.minenash.customhud.registry.CustomHudRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class CustomhudIntegration implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(HudifyMain.MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Integrating with CustomHud");

        /** strings **/
        StringSupplierElement track = new StringSupplierElement(() -> SpotifyData.sp_track.isEmpty() ? null : SpotifyData.sp_track);
        CustomHudRegistry.registerElement("sp_song",  (flags, context) ->  Flags.wrap(track, flags));
        CustomHudRegistry.registerElement("sp_track",  (flags, context) ->  Flags.wrap(track, flags));

        StringSupplierElement artists = new StringSupplierElement(() -> SpotifyData.sp_artists.isEmpty() ? null : SpotifyData.sp_artists);
        CustomHudRegistry.registerElement("sp_artist",  (flags, context) ->  Flags.wrap(artists, flags));
        CustomHudRegistry.registerElement("sp_artists",  (flags, context) ->  Flags.wrap(artists, flags));

        StringSupplierElement first_artist = new StringSupplierElement(() -> SpotifyData.sp_first_artist.isEmpty() ? null : SpotifyData.sp_first_artist);
        CustomHudRegistry.registerElement("sp_first_artist",  (flags, context) ->  Flags.wrap(first_artist, flags));

        StringSupplierElement context_type = new StringSupplierElement(() -> SpotifyData.sp_context_type.isEmpty() ? null : SpotifyData.sp_context_type);
        CustomHudRegistry.registerElement("sp_context_type", (flags, context) ->  Flags.wrap(context_type, flags));

        StringSupplierElement context_name = new StringSupplierElement(() -> SpotifyData.sp_context_name);
        CustomHudRegistry.registerElement("sp_context_name", (flags, context) ->  Flags.wrap(context_name, flags));

        StringSupplierElement album = new StringSupplierElement(() -> SpotifyData.sp_album.isEmpty() ? null : SpotifyData.sp_album);
        CustomHudRegistry.registerElement("sp_album", (flags, context) ->  Flags.wrap(album, flags));

        StringSupplierElement repeat_state = new StringSupplierElement(() -> SpotifyData.sp_repeat_state.isEmpty() ? null : SpotifyData.sp_repeat_state);
        CustomHudRegistry.registerElement("sp_repeat", (flags, context) ->  Flags.wrap(repeat_state, flags));
//        CustomHudRegistry.registerElement("sp_repeat_state", (_str) -> repeat_state);

        /** booleans **/
        BooleanSupplierElement shuffle_state = new BooleanSupplierElement(() -> SpotifyData.sp_shuffle_state);
        CustomHudRegistry.registerElement("sp_shuffle", (flags, context) ->  Flags.wrap(shuffle_state, flags));
//
        BooleanSupplierElement is_podcast = new BooleanSupplierElement(() -> (Objects.equals(SpotifyData.sp_media_type, "episode")));
        CustomHudRegistry.registerElement("sp_is_podcast", (flags, context) ->  Flags.wrap(is_podcast, flags));

        /** numbers **/
        NumberSupplierElement status_code = new NumberSupplierElement(() -> SpotifyData.sp_status_code, new Flags());
        CustomHudRegistry.registerElement("sp_status_code", (flags, context) -> status_code);

//        StringSupplierElement media_type = new StringSupplierElement(() -> SpotifyData.sp_media_type.isEmpty() ? null : SpotifyData.sp_media_type); // track or episode
//        CustomHudRegistry.registerElement("sp_media_type", (flags, context) ->  Flags.wrap(media_type, flags));  // removed in favor of `is_podcast`

        /** specials **/
        SpecialSupplierElement.Entry progress_entry = SpecialSupplierElement.of(
                () -> (SpotifyData.sp_progress / 60) + ":" + String.format("%02d", SpotifyData.sp_progress % 60), /* string */
                () -> SpotifyData.sp_progress /* number */ , () -> SpotifyData.sp_progress > 0 /* bool */ );
        CustomHudRegistry.registerElement("sp_progress", (flags, context) -> new SpecialSupplierElement(progress_entry));
        CustomHudRegistry.registerElement("sp_prog",  (flags, context) -> new SpecialSupplierElement(progress_entry));


        SpecialSupplierElement.Entry duration = SpecialSupplierElement.of(
                () -> (SpotifyData.sp_duration / 60) + ":" + String.format("%02d", SpotifyData.sp_duration % 60), /* string */
                () -> SpotifyData.sp_duration /* number */, () -> SpotifyData.sp_duration > 0 /* bool */ );
        CustomHudRegistry.registerElement("sp_duration", (flags, context) -> new SpecialSupplierElement(duration));
        CustomHudRegistry.registerElement("sp_dur",  (flags, context) -> new SpecialSupplierElement(duration));


        SpecialSupplierElement.Entry message = SpecialSupplierElement.of(
//                () -> (SpotifyData.sp_message), // string
                SpotifyData::get_sp_message, /* string */
                () -> SpotifyData.msg_time_rem /* number */, () -> (!SpotifyData.get_sp_message().isEmpty()) /* bool */ );
        CustomHudRegistry.registerElement("sp_message", (flags, context) -> new SpecialSupplierElement(message));
        CustomHudRegistry.registerElement("sp_msg",  (flags, context) -> new SpecialSupplierElement(message));





    }

}
