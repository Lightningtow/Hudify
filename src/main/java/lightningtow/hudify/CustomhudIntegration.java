package lightningtow.hudify;

//import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
//import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
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

        StringSupplierElement track = new StringSupplierElement(() -> HudifyMain.sp_track);
        CustomHudRegistry.registerElement("sp_track",  (_str) ->  track);
        CustomHudRegistry.registerElement("sp_song",  (_str) ->  track);

//        Supplier<String> track = (_str) -> new StringSupplierElement(() -> HudifyMain.sp_track);
//        CustomHudRegistry.registerElement("spotify_track", track);
//        CustomHudRegistry.registerElement("sp_track", track);

        StringSupplierElement artists = new StringSupplierElement(() -> HudifyMain.sp_artists);
        CustomHudRegistry.registerElement("sp_artist",  (_str) ->  artists);
        CustomHudRegistry.registerElement("sp_artists",  (_str) ->  artists);

        StringSupplierElement first_artist = new StringSupplierElement(() -> HudifyMain.sp_first_artist);
        CustomHudRegistry.registerElement("sp_first_artist",  (_str) ->  first_artist);

        StringSupplierElement context_type = new StringSupplierElement(() -> HudifyMain.sp_context_type);
        CustomHudRegistry.registerElement("sp_context_type", (_str) -> context_type);

//        StringSupplierElement context_name = new StringSupplierElement(() -> HudifyMain.sp_context_name);
//        CustomHudRegistry.registerElement("sp_context_name", (_str) -> context_name);

        StringSupplierElement album = new StringSupplierElement(() -> HudifyMain.sp_album);
        CustomHudRegistry.registerElement("sp_album", (_str) -> album);

        StringSupplierElement repeat_state = new StringSupplierElement(() -> HudifyMain.sp_repeat_state);
        CustomHudRegistry.registerElement("sp_repeat", (_str) -> repeat_state);
//        CustomHudRegistry.registerElement("sp_repeat_state", (_str) -> repeat_state);

//        Supplier<String> sp_


//            CustomHudRegistry.registerElement((new StringSupplierElement(() -> HudifyMain.sp_repeat_state)));


//        BooleanSupplierElement shuffle_state = new BooleanSupplierElement(() -> HudifyMain.sp_shuffle_state);
//        CustomHudRegistry.registerElement("sp_shuffle", (_bool) -> shuffle_state);
////        CustomHudRegistry.registerElement("sp_shuffle_state", (_bool) -> shuffle_state);
//
//        BooleanSupplierElement is_podcast = new BooleanSupplierElement(() -> (Objects.equals(HudifyMain.sp_media_type, "episode")));
//        CustomHudRegistry.registerElement("sp_is_podcast", (_bool) -> is_podcast);


        StringSupplierElement shuffle_state = new StringSupplierElement(() -> HudifyMain.sp_shuffle_state ? "true" : "false");
        CustomHudRegistry.registerElement("sp_shuffle", (_str) -> shuffle_state);

        StringSupplierElement is_podcast = new StringSupplierElement(() -> (Objects.equals(HudifyMain.sp_media_type, "episode")) ? "true" : "false");
        CustomHudRegistry.registerElement("sp_is_podcast", (_str) -> is_podcast);


        StringSupplierElement progress = new StringSupplierElement(() ->
                (HudifyMain.sp_progress / 60) + ":" + String.format("%02d", HudifyMain.sp_progress % 60));
        CustomHudRegistry.registerElement("sp_progress", (_str) -> progress);
        CustomHudRegistry.registerElement("sp_prog",  (_str) ->  progress);

        StringSupplierElement duration = new StringSupplierElement(() ->
                (HudifyMain.sp_duration / 60) + ":" + String.format("%02d", HudifyMain.sp_duration % 60));
        CustomHudRegistry.registerElement("sp_duration", (_str) -> duration);
        CustomHudRegistry.registerElement("sp_dur",  (_str) ->  duration);

        StringSupplierElement media_type = new StringSupplierElement(() -> HudifyMain.sp_media_type);
        CustomHudRegistry.registerElement("sp_media_type", (_str) -> media_type);



//        NumberSupplierElement status_code = new NumberSupplierElement(() -> HudifyMain.status_code, 1.0);
//        NumberSupplierElement status_code = new NumberSupplierElement(() -> HudifyMain.status_code, new Flags());
//        CustomHudRegistry.registerElement("sp_status_code", (what_does_this_do) -> status_code);


        //	response code 429 -> Too Many Requests - Rate limiting has been applied.
        // approximately 180 calls per minute without throwing 429, 3 calls per second

    }

}
