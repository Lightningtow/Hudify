package lightningtow.hudify;

//import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
//import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
import com.minenash.customhud.HudElements.supplier.SpecialSupplierElement;
import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
import com.minenash.customhud.data.Flags;
import net.fabricmc.api.ClientModInitializer;
import com.minenash.customhud.mod_compat.CustomHudRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Supplier;

public class CustomhudIntegration implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(HudifyMain.MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Integrating with CustomHud");

        StringSupplierElement track = new StringSupplierElement(() -> HudifyMain.sp_track.isEmpty() ? null : HudifyMain.sp_track);
        CustomHudRegistry.registerElement("sp_track",  (_str) ->  track);
        CustomHudRegistry.registerElement("sp_song",  (_str) ->  track);

//        Supplier<String> track = (_str) -> new StringSupplierElement(() -> HudifyMain.sp_track);
//        CustomHudRegistry.registerElement("spotify_track", track);
//        CustomHudRegistry.registerElement("sp_track", track);

        StringSupplierElement artists = new StringSupplierElement(() -> HudifyMain.sp_artists.isEmpty() ? null : HudifyMain.sp_artists);
        CustomHudRegistry.registerElement("sp_artist",  (_str) ->  artists);
        CustomHudRegistry.registerElement("sp_artists",  (_str) ->  artists);

        StringSupplierElement first_artist = new StringSupplierElement(() -> HudifyMain.sp_first_artist.isEmpty() ? null : HudifyMain.sp_first_artist);
        CustomHudRegistry.registerElement("sp_first_artist",  (_str) ->  first_artist);

        StringSupplierElement context_type = new StringSupplierElement(() -> HudifyMain.sp_context_type.isEmpty() ? null : HudifyMain.sp_context_type);
        CustomHudRegistry.registerElement("sp_context_type", (_str) -> context_type);

        StringSupplierElement context_name = new StringSupplierElement(() -> HudifyMain.sp_context_name);
        CustomHudRegistry.registerElement("sp_context_name", (_str) -> context_name);

        StringSupplierElement album = new StringSupplierElement(() -> HudifyMain.sp_album.isEmpty() ? null : HudifyMain.sp_album);
        CustomHudRegistry.registerElement("sp_album", (_str) -> album);

        StringSupplierElement repeat_state = new StringSupplierElement(() -> HudifyMain.sp_repeat_state.isEmpty() ? null : HudifyMain.sp_repeat_state);
        CustomHudRegistry.registerElement("sp_repeat", (_str) -> repeat_state);
//        CustomHudRegistry.registerElement("sp_repeat_state", (_str) -> repeat_state);


        BooleanSupplierElement shuffle_state = new BooleanSupplierElement(() -> HudifyMain.sp_shuffle_state);
        CustomHudRegistry.registerElement("sp_shuffle", (_bool) -> shuffle_state);
//
        BooleanSupplierElement is_podcast = new BooleanSupplierElement(() -> (Objects.equals(HudifyMain.sp_media_type, "episode")));
        CustomHudRegistry.registerElement("sp_is_podcast", (_bool) -> is_podcast);


        SpecialSupplierElement.Entry progress_entry = SpecialSupplierElement.of(
                () -> (HudifyMain.sp_progress / 60) + ":" + String.format("%02d", HudifyMain.sp_progress % 60), // string
                () -> HudifyMain.sp_progress, // number
                () -> HudifyMain.sp_progress > 0 // bool
        );
        CustomHudRegistry.registerElement("sp_progress", (_special) -> new SpecialSupplierElement(progress_entry));
        CustomHudRegistry.registerElement("sp_prog",  (_special) -> new SpecialSupplierElement(progress_entry));



        SpecialSupplierElement.Entry duration = SpecialSupplierElement.of(
                () -> (HudifyMain.sp_duration / 60) + ":" + String.format("%02d", HudifyMain.sp_duration % 60), // string
                () -> HudifyMain.sp_duration, // number
                () -> HudifyMain.sp_duration > 0 // bool
        );
        CustomHudRegistry.registerElement("sp_duration", (_special) -> new SpecialSupplierElement(duration));
        CustomHudRegistry.registerElement("sp_dur",  (_special) -> new SpecialSupplierElement(duration));


        StringSupplierElement media_type = new StringSupplierElement(() -> HudifyMain.sp_media_type.isEmpty() ? null : HudifyMain.sp_media_type);
        CustomHudRegistry.registerElement("sp_media_type", (_str) -> media_type);

        SpecialSupplierElement.Entry message = SpecialSupplierElement.of(
//                () -> (HudifyMain.sp_message), // string
//                () -> HudifyMain.sp_message.isEmpty() ? 0 : 1, // number
//                () -> (!HudifyMain.sp_message.isEmpty()) // bool
                () -> (HudifyMain.get_sp_message()), // string
                () -> HudifyMain.get_sp_message().isEmpty() ? 0 : 1, // number
                () -> (!HudifyMain.get_sp_message().isEmpty()) // bool
        );
        CustomHudRegistry.registerElement("sp_message", (_special) -> new SpecialSupplierElement(message));
        CustomHudRegistry.registerElement("sp_msg",  (_special) -> new SpecialSupplierElement(message));

        NumberSupplierElement status_code = new NumberSupplierElement(() -> HudifyMain.sp_status_code, new Flags());
        CustomHudRegistry.registerElement("sp_status_code", (_int) -> status_code);

//        StringSupplierElement duration = new StringSupplierElement(() ->
//                (HudifyMain.sp_duration / 60) + ":" + String.format("%02d", HudifyMain.sp_duration % 60));
//        CustomHudRegistry.registerElement("sp_duration", (_str) -> duration);
//        CustomHudRegistry.registerElement("sp_dur",  (_str) ->  duration);



    }

}
