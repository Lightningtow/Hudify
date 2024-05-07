package lightningtow.hudify;

import com.minenash.customhud.ComplexData;
import com.minenash.customhud.HudElements.HudElement;
import com.minenash.customhud.HudElements.StringElement;
import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
import com.minenash.customhud.HudElements.supplier.IntegerSuppliers;
import com.minenash.customhud.HudElements.supplier.StringSupplierElement;
import com.minenash.customhud.data.Flags;
import lightningtow.hudify.util.SpotifyUtil;
import net.fabricmc.api.ClientModInitializer;
import com.minenash.customhud.HudElements.supplier.NumberSupplierElement;
import com.minenash.customhud.mod_compat.CustomHudRegistry;
import net.minecraft.world.Heightmap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.minenash.customhud.mod_compat.CustomHudRegistry.registerElement;

public class CustomhudIntegration implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(HudifyMain.MOD_ID);

    @Override
    public void onInitializeClient() {
//        LOGGER.info("running CHIntegration.onInitializeClient()");
        LOGGER.info("integrating with CustomHud");


    //    registerElement("spotify_ready", (_str) -> new BooleanSupplierElement(() -> Objects.equals(HudifyHUD.hudInfo[0], "-")));

//       // HudElement track = new StringSupplierElement(HudifyHUD.hudInfo[0]);
//        registerElement("name1", (_str) -> VERSION);
//        registerElement("name2", (_str) -> VERSION);

//        Supplier<String> tracksupplier = () -> HudifyHUD.hudInfo[0];
//        track  = (str) -> new StringSupplierElement(HudifyHUD.hudInfo[0]);

//        Supplier<String> track =   HudifyHUD.hudInfo[0];
        StringSupplierElement track = new StringSupplierElement(() -> HudifyMain.track);
        CustomHudRegistry.registerElement("sp_track",  (_str) ->  track);

        StringSupplierElement artists = new StringSupplierElement(() -> HudifyMain.artists);
        CustomHudRegistry.registerElement("sp_artists",  (_str) ->  artists);

        StringSupplierElement first_artist = new StringSupplierElement(() -> HudifyMain.first_artist);
        CustomHudRegistry.registerElement("sp_first_artist",  (_str) ->  first_artist);

        StringSupplierElement context_type = new StringSupplierElement(() -> HudifyMain.context_type);
        CustomHudRegistry.registerElement("sp_context_type", (_str) -> context_type);

        StringSupplierElement context_name = new StringSupplierElement(() -> HudifyMain.context_name);
        CustomHudRegistry.registerElement("sp_context_name", (_str) -> context_name);

        StringSupplierElement album = new StringSupplierElement(() -> HudifyMain.album);
        CustomHudRegistry.registerElement("sp_album", (_str) -> album);

        StringSupplierElement repeat_state = new StringSupplierElement(() -> HudifyMain.repeat_state);
        CustomHudRegistry.registerElement("sp_repeat_state", (_str) -> repeat_state);

        BooleanSupplierElement shuffle_state = new BooleanSupplierElement(() -> HudifyMain.shuffle_state);
        CustomHudRegistry.registerElement("sp_shuffle_state", (_bool) -> shuffle_state);

        StringSupplierElement progress = new StringSupplierElement(() ->
                (HudifyMain.progress / 60) + ":" + String.format("%02d", HudifyMain.progress % 60));
        CustomHudRegistry.registerElement("spotify_progress", (_str) -> progress);
        CustomHudRegistry.registerElement("sp_prog",  (_str) ->  progress);

        StringSupplierElement duration = new StringSupplierElement(() ->
                (HudifyMain.duration / 60) + ":" + String.format("%02d", HudifyMain.duration % 60));
        CustomHudRegistry.registerElement("spotify_duration", (_str) -> duration);
        CustomHudRegistry.registerElement("sp_dur",  (_str) ->  duration);


//        NumberSupplierElement status_code = new NumberSupplierElement(() -> HudifyMain.status_code, 1.0);
//        NumberSupplierElement status_code = new NumberSupplierElement(() -> HudifyMain.status_code, new Flags());
//        CustomHudRegistry.registerElement("sp_status_code", (what_does_this_do) -> status_code);


        //	response code 429 -> Too Many Requests - Rate limiting has been applied.
        // approximately 180 calls per minute without throwing 429, 3 calls per second

    }

}
